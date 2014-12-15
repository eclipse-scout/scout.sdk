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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.signature.IResolvedTypeParameter;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>{@link ResolvedTypeParameter}</h3>
 *
 * @author Matthias Villiger
 * @since 4.2.0 09.12.2014
 */
public class ResolvedTypeParameter implements IResolvedTypeParameter {

  private final String m_typeParamName;
  private final Set<String> m_typeParamBounds;
  private final int m_ordinal;
  private final TypeParameterMapping m_owner;

  private final Map<String /* owner type fqn */, Set<ResolvedTypeParameter>> m_superTypeParamReferences; // all super type parameters
  private final Map<String /* owner type fqn */, ResolvedTypeParameter> m_subTypeParamReferences; // all child type parameters

  protected ResolvedTypeParameter(TypeParameterMapping owner, String signature, int ordinal) {
    m_ordinal = ordinal;
    m_owner = owner;
    m_typeParamName = "?";
    m_typeParamBounds = new LinkedHashSet<String>(1);
    m_typeParamBounds.add(signature);
    m_superTypeParamReferences = new LinkedHashMap<String, Set<ResolvedTypeParameter>>();
    m_subTypeParamReferences = new LinkedHashMap<String, ResolvedTypeParameter>();
  }

  protected ResolvedTypeParameter(TypeParameterMapping owner, TypeParameterMapping child, ITypeParameter param, Set<String> childParamBinds, Map<String, ResolvedTypeParameter> ownerParamBinds, int ordinal) throws JavaModelException {
    m_owner = owner;
    m_ordinal = ordinal;
    m_typeParamName = param.getElementName();
    Set<String> bounds = null;
    if (CollectionUtility.isEmpty(childParamBinds)) {
      // nothing specified from the child. use own.
      Set<String> b = getTypeParamBounds(param);
      if (CollectionUtility.hasElements(b)) {
        // the type parameter defines bounds itself -> resolve and use
        bounds = new LinkedHashSet<String>(b.size());
        for (String s : b) {
          bounds.add(SignatureUtility.getResolvedSignature(owner.getType(), ownerParamBinds, s));
        }
      }
      else {
        // there are no bounds on the current type parameter
        if (child == null) {
          // we are the lowest level (focus type). our type parameter defines the resolved name: use type param name as signature.
          bounds = new LinkedHashSet<String>(1);
          String string = new StringBuilder(m_typeParamName.length() + 2).append(Signature.C_TYPE_VARIABLE).append(m_typeParamName).append(Signature.C_SEMICOLON).toString();
          bounds.add(string);
        }
        else {
          // we are not the lowest level and define a new type parameter which has no bounds.
          // The bounds must be fixed to java.lang.Object as it would not be available on the focus type anyway.
          bounds = new LinkedHashSet<String>(1);
          bounds.add(SignatureUtility.SIG_OBJECT);
        }
      }
    }
    else {
      // use the bounds from the child
      bounds = new LinkedHashSet<String>(childParamBinds);
    }

    m_typeParamBounds = bounds;
    m_superTypeParamReferences = new LinkedHashMap<String, Set<ResolvedTypeParameter>>();
    m_subTypeParamReferences = new LinkedHashMap<String, ResolvedTypeParameter>();
  }

  /**
   * returns the same as {@link ITypeParameter#getBoundsSignatures()} but removes the signatures of java.lang.Object.
   * Such signatures are part of the bounds if the type is binary.
   */
  private static Set<String> getTypeParamBounds(ITypeParameter param) throws JavaModelException {
    String[] b = param.getBoundsSignatures();
    if (b.length > 0) {
      LinkedHashSet<String> bounds = new LinkedHashSet<String>(b.length);
      for (String bound : b) {
        if (!SignatureUtility.SIG_OBJECT.equals(bound)) {
          bounds.add(bound);
        }
      }
      return bounds;
    }
    return null;
  }

  protected void addReference(ResolvedTypeParameter superParam) {
    // remember that the given param refers to this (add to our references)
    String paramOwnerFqn = superParam.getOwnerMapping().getFullyQualifiedName();
    Set<ResolvedTypeParameter> set = m_superTypeParamReferences.get(paramOwnerFqn);
    if (set == null) {
      set = new LinkedHashSet<ResolvedTypeParameter>();
      m_superTypeParamReferences.put(paramOwnerFqn, set);
    }
    set.add(superParam);

    // remember that this references to the given param.
    ResolvedTypeParameter old = superParam.m_subTypeParamReferences.put(getOwnerMapping().getFullyQualifiedName(), this);
    if (old != null) {
      throw new IllegalArgumentException("Duplicate sub type param reference for owner type '" + getOwnerMapping().getFullyQualifiedName() + "' below '" + superParam.getOwnerMapping().getFullyQualifiedName() +
          "': Old: '" + old.toString() + "'. New: '" + this.toString() + "'.");
    }
  }

  @Override
  public Set<IResolvedTypeParameter> getSuperReferences(String ownerTypeFqn) {
    Set<ResolvedTypeParameter> c = m_superTypeParamReferences.get(ownerTypeFqn);
    if (c == null) {
      return null;
    }
    return new LinkedHashSet<IResolvedTypeParameter>(c);
  }

  @Override
  public IResolvedTypeParameter getCorrespondingTypeParameterOnSubLevel(IType level) {
    if (!TypeUtility.exists(level)) {
      return null;
    }
    return getCorrespondingTypeParameterOnSubLevel(level.getFullyQualifiedName());
  }

  @Override
  public IResolvedTypeParameter getCorrespondingTypeParameterOnSubLevel(String levelFullyQualifiedName) {
    if (StringUtility.isNullOrEmpty(levelFullyQualifiedName)) {
      return null;
    }
    return getCorrespondingTypeParameterOnSubLevelRec(this, levelFullyQualifiedName);
  }

  private ResolvedTypeParameter getCorrespondingTypeParameterOnSubLevelRec(ResolvedTypeParameter curParam, String levelFullyQualifiedName) {
    Map<String, ResolvedTypeParameter> curMap = curParam.m_subTypeParamReferences;

    ResolvedTypeParameter result = curMap.get(levelFullyQualifiedName);
    if (result != null) {
      // level found!
      return result;
    }

    // not on current level: step down
    for (ResolvedTypeParameter param : curMap.values()) {
      result = getCorrespondingTypeParameterOnSubLevelRec(param, levelFullyQualifiedName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public int getOrdinal() {
    return m_ordinal;
  }

  @Override
  public Map<String, Set<IResolvedTypeParameter>> getSuperReferences() {
    Map<String, Set<IResolvedTypeParameter>> result = new LinkedHashMap<String, Set<IResolvedTypeParameter>>(m_superTypeParamReferences.size());
    for (Entry<String, Set<ResolvedTypeParameter>> entry : m_superTypeParamReferences.entrySet()) {
      result.put(entry.getKey(), new LinkedHashSet<IResolvedTypeParameter>(entry.getValue()));
    }
    return result;
  }

  @Override
  public IResolvedTypeParameter getSubReference(String ownerTypeFqn) {
    return m_subTypeParamReferences.get(ownerTypeFqn);
  }

  @Override
  public Map<String, IResolvedTypeParameter> getSubReferences() {
    return new LinkedHashMap<String, IResolvedTypeParameter>(m_subTypeParamReferences);
  }

  @Override
  public String getTypeParameterName() {
    return m_typeParamName;
  }

  @Override
  public Set<String> getBoundsSignatures() {
    return new LinkedHashSet<String>(m_typeParamBounds);
  }

  @Override
  public TypeParameterMapping getOwnerMapping() {
    return m_owner;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_ordinal;
    result = prime * result + ((m_typeParamBounds == null) ? 0 : m_typeParamBounds.hashCode());
    result = prime * result + ((m_typeParamName == null) ? 0 : m_typeParamName.hashCode());
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
    if (!(obj instanceof ResolvedTypeParameter)) {
      return false;
    }
    ResolvedTypeParameter other = (ResolvedTypeParameter) obj;
    if (m_ordinal != other.m_ordinal) {
      return false;
    }
    if (m_typeParamBounds == null) {
      if (other.m_typeParamBounds != null) {
        return false;
      }
    }
    else if (!m_typeParamBounds.equals(other.m_typeParamBounds)) {
      return false;
    }
    if (m_typeParamName == null) {
      if (other.m_typeParamName != null) {
        return false;
      }
    }
    else if (!m_typeParamName.equals(other.m_typeParamName)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getTypeParameterName());
    Iterator<String> iterator = m_typeParamBounds.iterator();
    if (iterator.hasNext()) {
      sb.append(" extends ");
      sb.append(iterator.next());
      while (iterator.hasNext()) {
        sb.append(" & ");
        sb.append(iterator.next());
      }
    }
    return sb.toString();
  }
}
