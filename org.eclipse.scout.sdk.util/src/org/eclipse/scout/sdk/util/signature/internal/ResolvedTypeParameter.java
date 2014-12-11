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
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.util.signature.IResolvedTypeParameter;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
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

  private final Map<String /* owner type fqn */, Set<ResolvedTypeParameter>> m_typeParamReferences;
  private ResolvedTypeParameter m_referencedTypeParam;

  protected ResolvedTypeParameter(TypeParameterMapping owner, String signature, int ordinal) {
    m_ordinal = ordinal;
    m_owner = owner;
    m_typeParamName = "?";
    m_typeParamBounds = new LinkedHashSet<String>(1);
    m_typeParamBounds.add(signature);
    m_typeParamReferences = new LinkedHashMap<String, Set<ResolvedTypeParameter>>();
  }

  protected ResolvedTypeParameter(TypeParameterMapping owner, ITypeParameter param, Set<String> childParamBinds, Map<String, ResolvedTypeParameter> ownerParamBinds, int ordinal) throws JavaModelException {
    m_owner = owner;
    m_ordinal = ordinal;
    m_typeParamName = param.getElementName();
    Set<String> bounds = null;
    if (CollectionUtility.isEmpty(childParamBinds)) {
      // nothing specified from the child. use own.
      String[] b = param.getBoundsSignatures();
      if (b.length > 0) {
        bounds = new LinkedHashSet<String>(b.length);
        for (String s : b) {
          bounds.add(SignatureUtility.getResolvedSignature(owner.getType(), ownerParamBinds, s));
        }
      }
      else {
        bounds = new LinkedHashSet<String>(1);
        String s = SignatureUtility.getResolvedSignature(owner.getType(), ownerParamBinds, SignatureCache.createTypeSignature(m_typeParamName));
        bounds.add(s);
      }
    }
    else {
      // use the bounds from the child
      bounds = new LinkedHashSet<String>(childParamBinds);
    }

    m_typeParamBounds = bounds;
    m_typeParamReferences = new LinkedHashMap<String, Set<ResolvedTypeParameter>>();
  }

  protected void addReference(ResolvedTypeParameter param) {
    if (param.m_referencedTypeParam != null) {
      throw new IllegalArgumentException("Parameter '" + param.toString() + "' already references parameter '" + param.m_referencedTypeParam.toString() + "'.");
    }

    String ownerFqn = param.getOwnerMapping().getFullyQualifiedName();
    Set<ResolvedTypeParameter> set = m_typeParamReferences.get(ownerFqn);
    if (set == null) {
      set = new LinkedHashSet<ResolvedTypeParameter>();
      m_typeParamReferences.put(ownerFqn, set);
    }
    set.add(param);
    param.m_referencedTypeParam = this;
  }

  @Override
  public Set<IResolvedTypeParameter> getReferences(String ownerTypeSignature) {
    Set<ResolvedTypeParameter> c = m_typeParamReferences.get(ownerTypeSignature);
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

    IResolvedTypeParameter curParam = this;
    do {
      if (curParam.getOwnerMapping().getType().equals(level)) {
        return curParam;
      }
      curParam = curParam.getReferencedTypeParameter();
    }
    while (curParam != null);
    return null; // not found
  }

  @Override
  public int getOrdinal() {
    return m_ordinal;
  }

  @Override
  public Map<String, Set<IResolvedTypeParameter>> getAllReferences() {
    Map<String, Set<IResolvedTypeParameter>> result = new LinkedHashMap<String, Set<IResolvedTypeParameter>>(m_typeParamReferences.size());
    for (Entry<String, Set<ResolvedTypeParameter>> entry : m_typeParamReferences.entrySet()) {
      result.put(entry.getKey(), new LinkedHashSet<IResolvedTypeParameter>(entry.getValue()));
    }
    return result;
  }

  @Override
  public ResolvedTypeParameter getReferencedTypeParameter() {
    return m_referencedTypeParam;
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
