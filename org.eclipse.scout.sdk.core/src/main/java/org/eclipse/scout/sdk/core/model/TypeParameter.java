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
package org.eclipse.scout.sdk.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * <h3>{@link TypeParameter}</h3>
 *
 * @author Andreas Hoegger
 * @since 4.1.0 09.11.2014
 */
public class TypeParameter implements ITypeParameter {

  private final org.eclipse.jdt.internal.compiler.ast.TypeParameter m_typeParam;
  private final String m_name;
  private final IType m_type;
  private final char[] m_id;
  private final int m_hash;
  private ListOrderedSet<IType> m_bounds;

  public TypeParameter(org.eclipse.jdt.internal.compiler.ast.TypeParameter typeParam, IType type) {
    m_typeParam = Validate.notNull(typeParam);
    m_type = Validate.notNull(type);
    m_name = new String(m_typeParam.name);
    m_id = computeUniqueKey(typeParam);
    m_hash = new HashCodeBuilder().append(m_id).append(m_type).toHashCode();
  }

  protected static char[] computeUniqueKey(org.eclipse.jdt.internal.compiler.ast.TypeParameter typeParam) {
    StringBuffer sig = new StringBuffer();
    typeParam.printStatement(0, sig);

    int sigLength = sig.length();
    char[] uniqueKey = new char[sigLength];
    sig.getChars(0, sigLength, uniqueKey, 0);
    return uniqueKey;
  }

  @Override
  public ListOrderedSet<IType> getBounds() {
    if (m_bounds == null) {
      boolean hasType = m_typeParam.type != null && m_typeParam.type.resolvedType != null;
      boolean hasBounds = m_typeParam.bounds != null && m_typeParam.bounds.length > 0;
      int size = 0;
      if (hasType) {
        size++;
      }
      if (hasBounds) {
        size += m_typeParam.bounds.length;
      }
      List<IType> result = new ArrayList<>(size);
      if (hasType) {
        IType t = JavaModelUtils.bindingToType(m_typeParam.type.resolvedType, m_type.getLookupEnvironment());
        if (t != null) {
          result.add(t);
        }
      }
      if (hasBounds) {
        for (TypeReference r : m_typeParam.bounds) {
          TypeBinding b = r.resolvedType;
          if (b != null) {
            IType t = JavaModelUtils.bindingToType(b, m_type.getLookupEnvironment());
            if (t != null) {
              result.add(t);
            }
          }
        }
      }
      m_bounds = ListOrderedSet.listOrderedSet(result);
    }
    return m_bounds;
  }

  @Override
  public IType getType() {
    return m_type;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public int hashCode() {
    return m_hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof TypeParameter)) {
      return false;
    }
    TypeParameter other = (TypeParameter) obj;
    if (!Arrays.equals(m_id, other.m_id)) {
      return false;
    }
    return m_type.equals(other.m_type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    ModelPrinter.print(this, sb);
    return sb.toString();
  }
}
