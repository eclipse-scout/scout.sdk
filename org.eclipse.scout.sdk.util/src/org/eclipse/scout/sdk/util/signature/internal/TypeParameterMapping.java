/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.util.signature.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.util.signature.IResolvedTypeParameter;
import org.eclipse.scout.sdk.util.signature.ITypeParameterMapping;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * <h3>{@link TypeParameterMapping}</h3>
 *
 * @author Matthias Villiger
 * @since 4.2.0 09.12.2014
 */
public class TypeParameterMapping implements ITypeParameterMapping {

  private final IType m_type;
  private final String m_fullyQualifiedName;
  private final Map<String /* local param name */, ResolvedTypeParameter> m_typeParametersByName;
  private final List<ResolvedTypeParameter> m_typeParametersByIndex;
  private final Map<String /* simple or fq name */, List<String>> m_superParametersByType;
  private final Map<String /* fqn */, TypeParameterMapping> m_superMappings;
  private final Map<String /* fqn */, TypeParameterMapping> m_subMappings;

  public TypeParameterMapping(String signature, String superTypeSignature, List<String> superInterfacesSignatures) throws CoreException {
    m_type = null;
    m_superMappings = new LinkedHashMap<String, TypeParameterMapping>();
    m_subMappings = new LinkedHashMap<String, TypeParameterMapping>();
    m_fullyQualifiedName = Signature.toString(Signature.getTypeErasure(signature));

    String[] localParameterSignatures = Signature.getTypeParameters(signature);
    m_typeParametersByName = new LinkedHashMap<String, ResolvedTypeParameter>(localParameterSignatures.length);
    m_typeParametersByIndex = new ArrayList<ResolvedTypeParameter>(localParameterSignatures.length);
    for (String localParamSig : localParameterSignatures) {
      ResolvedTypeParameter parameter = new ResolvedTypeParameter(this, localParamSig, m_typeParametersByName.size());
      m_typeParametersByName.put(Signature.getSignatureSimpleName(localParamSig), parameter);
      m_typeParametersByIndex.add(parameter);
    }

    m_superParametersByType = getSuperTypeParameters(superTypeSignature, superInterfacesSignatures);
  }

  public TypeParameterMapping(IType t, TypeParameterMapping child) throws CoreException {
    m_type = t;
    m_fullyQualifiedName = t.getFullyQualifiedName();
    ITypeParameter[] typeParameters = getType().getTypeParameters();

    m_typeParametersByName = new LinkedHashMap<String, ResolvedTypeParameter>(typeParameters.length);
    m_typeParametersByIndex = new ArrayList<ResolvedTypeParameter>(typeParameters.length);
    for (int i = 0; i < typeParameters.length; i++) {
      ITypeParameter myTypeParam = typeParameters[i];

      Set<String> childBounds = null;
      String curParamNameInSuperTypeDeclaration = getParamNameInSuperTypeDeclaration(child, i);
      if (curParamNameInSuperTypeDeclaration != null) {
        ResolvedTypeParameter matchingChildTypeParam = getMatchingChildTypeParam(child, curParamNameInSuperTypeDeclaration);
        if (matchingChildTypeParam != null) {
          // reference to a type variable -> use its bounds
          childBounds = matchingChildTypeParam.getBoundsSignatures();
        }
        else {
          // it is a type signature: use as bounds (only single supported by java)
          childBounds = new LinkedHashSet<String>(1);
          childBounds.add(curParamNameInSuperTypeDeclaration);
        }
      }

      ResolvedTypeParameter parameter = new ResolvedTypeParameter(this, child, myTypeParam, childBounds, m_typeParametersByName, m_typeParametersByName.size());
      m_typeParametersByName.put(myTypeParam.getElementName(), parameter);
      m_typeParametersByIndex.add(parameter);
    }

    m_superParametersByType = getSuperTypeParameters(t.getSuperclassTypeSignature(), CollectionUtility.arrayList(t.getSuperInterfaceTypeSignatures()));
    m_superMappings = new LinkedHashMap<String, TypeParameterMapping>();
    m_subMappings = new LinkedHashMap<String, TypeParameterMapping>();

    // connect mappings
    connectWithChild(child);
  }

  private ResolvedTypeParameter getMatchingChildTypeParam(TypeParameterMapping child, String curParamNameInSuperTypeDeclaration) {
    if (Signature.getTypeSignatureKind(curParamNameInSuperTypeDeclaration) == Signature.TYPE_VARIABLE_SIGNATURE) {
      return child.getTypeParameter(Signature.getSignatureSimpleName(curParamNameInSuperTypeDeclaration));
    }
    return null;
  }

  private String getParamNameInSuperTypeDeclaration(TypeParameterMapping child, int typeParamIndex) {
    if (child != null) {
      List<String> paramNamesInSuperTypeDeclaration = child.getSuperTypeParameters(getType());
      if (paramNamesInSuperTypeDeclaration != null && paramNamesInSuperTypeDeclaration.size() > typeParamIndex) {
        String curParamNameInSuperTypeDeclaration = paramNamesInSuperTypeDeclaration.get(typeParamIndex);
        return curParamNameInSuperTypeDeclaration;
      }
    }
    return null;
  }

  private void connectWithChild(TypeParameterMapping child) {
    if (child != null) {
      child.addSuperMapping(this);

      // connect params
      int i = 0;
      for (ResolvedTypeParameter param : m_typeParametersByName.values()) {
        String curParamNameInSuperTypeDeclaration = getParamNameInSuperTypeDeclaration(child, i++);
        if (curParamNameInSuperTypeDeclaration != null) {
          ResolvedTypeParameter matchingChildTypeParam = getMatchingChildTypeParam(child, curParamNameInSuperTypeDeclaration);
          if (matchingChildTypeParam != null) {
            matchingChildTypeParam.addReference(param);
          }
        }
      }
    }
  }

  public static void buildMappingRec(IType type, ITypeHierarchy supertypeHierarchy, Map<String, TypeParameterMapping> collector, TypeParameterMapping child) throws CoreException {
    String fullyQualifiedName = type.getFullyQualifiedName();
    TypeParameterMapping existing = collector.get(fullyQualifiedName);
    if (existing != null && child != null) {
      // already calculated: just connect
      existing.connectWithChild(child);
      return;
    }

    TypeParameterMapping curLevel = new TypeParameterMapping(type, child);
    String objectClassFqn = Object.class.getName();
    collector.put(fullyQualifiedName, curLevel);
    for (IType superType : supertypeHierarchy.getSupertypes(type)) {
      if (!objectClassFqn.equals(superType.getFullyQualifiedName())) {
        buildMappingRec(superType, supertypeHierarchy, collector, curLevel);
      }
    }
  }

  private Map<String, List<String>> getSuperTypeParameters(String superclassTypeSignature, List<String> superInterfacesSignatures) throws CoreException {
    LinkedHashMap<String, List<String>> result = new LinkedHashMap<String, List<String>>(CollectionUtility.size(superInterfacesSignatures) + 1);
    if (superclassTypeSignature != null && !SignatureUtility.SIG_OBJECT.equals(superclassTypeSignature)) {
      List<String> superTypeSigParams = getTypeParametersForTypeSignature(superclassTypeSignature);
      if (CollectionUtility.hasElements(superTypeSigParams)) {
        result.put(Signature.toString(Signature.getTypeErasure(superclassTypeSignature)), superTypeSigParams);
      }
    }
    if (CollectionUtility.hasElements(superInterfacesSignatures)) {
      for (String superIfcSig : superInterfacesSignatures) {
        if (superIfcSig != null) {
          List<String> superInterfaceSigParams = getTypeParametersForTypeSignature(superIfcSig);
          if (CollectionUtility.hasElements(superInterfaceSigParams)) {
            result.put(Signature.toString(Signature.getTypeErasure(superIfcSig)), superInterfaceSigParams);
          }
        }
      }
    }
    return result;
  }

  private List<String> getTypeParametersForTypeSignature(String sig) throws CoreException {
    String[] superTypeArgs = Signature.getTypeArguments(sig);
    if (superTypeArgs.length < 1) {
      return null;
    }

    List<String> result = new ArrayList<String>(superTypeArgs.length);
    for (String superSig : superTypeArgs) {
      superSig = SignatureUtility.ensureSourceTypeParametersAreCorrect(superSig, getType());
      if (Signature.getTypeSignatureKind(superSig) == Signature.TYPE_VARIABLE_SIGNATURE) {
        // already a type variable
        result.add(superSig);
      }
      else {
        // a type variable: resolve according to current scope
        result.add(SignatureUtility.getResolvedSignature(getType(), m_typeParametersByName, superSig));
      }
    }
    return result;
  }

  private void addSuperMapping(TypeParameterMapping superMapping) {
    m_superMappings.put(superMapping.getFullyQualifiedName(), superMapping);
    superMapping.m_subMappings.put(m_fullyQualifiedName, this);
  }

  @Override
  public Map<String, ITypeParameterMapping> getSuperMappings() {
    return new LinkedHashMap<String, ITypeParameterMapping>(m_superMappings);
  }

  @Override
  public Map<String, ITypeParameterMapping> getSubMappings() {
    return new LinkedHashMap<String, ITypeParameterMapping>(m_subMappings);
  }

  @Override
  public TypeParameterMapping getSubMapping(String fullyQualifiedName) {
    return m_subMappings.get(fullyQualifiedName);
  }

  @Override
  public TypeParameterMapping getSuperMapping(String fullyQualifiedName) {
    return m_superMappings.get(fullyQualifiedName);
  }

  private List<String> getSuperTypeParameters(IType type) {
    if (!TypeUtility.exists(type)) {
      return null;
    }

    // try with fully qualified name first.
    List<String> superTypeParameters = m_superParametersByType.get(type.getFullyQualifiedName());
    if (superTypeParameters != null) {
      return superTypeParameters;
    }

    // it may be an unresolved signature -> try with simple name as well
    return m_superParametersByType.get(type.getElementName());
  }

  @Override
  public ResolvedTypeParameter getTypeParameter(int index) {
    if (index < m_typeParametersByIndex.size()) {
      return m_typeParametersByIndex.get(index);
    }
    return null;
  }

  @Override
  public Set<String> getTypeParameterBounds(int index) {
    ResolvedTypeParameter typeParameter = getTypeParameter(index);
    if (typeParameter == null) {
      return null;
    }
    return typeParameter.getBoundsSignatures();
  }

  @Override
  public Set<String> getTypeParameterBounds(String name) {
    ResolvedTypeParameter typeParameter = getTypeParameter(name);
    if (typeParameter == null) {
      return null;
    }
    return typeParameter.getBoundsSignatures();
  }

  @Override
  public int getParameterCount() {
    return m_typeParametersByName.size();
  }

  @Override
  public ResolvedTypeParameter getTypeParameter(String name) {
    return m_typeParametersByName.get(name);
  }

  @Override
  public Map<String, IResolvedTypeParameter> getTypeParameters() {
    return new LinkedHashMap<String, IResolvedTypeParameter>(m_typeParametersByName);
  }

  @Override
  public IType getType() {
    return m_type;
  }

  @Override
  public String getFullyQualifiedName() {
    return m_fullyQualifiedName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_fullyQualifiedName == null) ? 0 : m_fullyQualifiedName.hashCode());
    result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof TypeParameterMapping)) {
      return false;
    }
    TypeParameterMapping other = (TypeParameterMapping) obj;
    if (m_fullyQualifiedName == null) {
      if (other.m_fullyQualifiedName != null) {
        return false;
      }
    }
    else if (!m_fullyQualifiedName.equals(other.m_fullyQualifiedName)) {
      return false;
    }
    if (m_type == null) {
      if (other.m_type != null) {
        return false;
      }
    }
    else if (!m_type.equals(other.m_type)) {
      return false;
    }
    return true;
  }

  private void printSuperType(StringBuilder builder, Entry<String, List<String>> superType) {
    builder.append(superType.getKey());
    Iterator<String> iterator = superType.getValue().iterator();
    if (iterator.hasNext()) {
      builder.append('<');
      builder.append(iterator.next());
      while (iterator.hasNext()) {
        builder.append(", ");
        builder.append(iterator.next());
      }
      builder.append('>');
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(getFullyQualifiedName());
    Iterator<ResolvedTypeParameter> it = m_typeParametersByName.values().iterator();
    if (it.hasNext()) {
      builder.append('<');
      builder.append(it.next().toString());
      while (it.hasNext()) {
        builder.append(", ");
        builder.append(it.next().toString());
      }
      builder.append('>');
    }

    Iterator<Entry<String, List<String>>> superIt = m_superParametersByType.entrySet().iterator();
    if (superIt.hasNext()) {
      builder.append(" extends ");
      printSuperType(builder, superIt.next());
      while (superIt.hasNext()) {
        builder.append(", ");
        printSuperType(builder, superIt.next());
      }
    }
    return builder.toString();
  }
}
