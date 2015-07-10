/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 *
 */
public class BindingTypeParameter implements ITypeParameter {

  private final TypeVariableBinding m_binding;
  private final IType m_type;
  private final char[] m_id;
  private final int m_hash;
  private String m_name;
  private ListOrderedSet/*<IType>*/ m_bounds;

  public BindingTypeParameter(TypeVariableBinding binding, IType type) {
    m_binding = Validate.notNull(binding);
    m_type = Validate.notNull(type);
    m_id = binding.computeUniqueKey();
    m_hash = Arrays.hashCode(m_id);
  }

  @Override
  public String getName() {
    if (m_name == null) {
      m_name = new String(m_binding.sourceName());
    }
    return m_name;
  }

  @Override
  @SuppressWarnings("null")
  public ListOrderedSet/*<IType>*/ getBounds() {
    if (m_bounds == null) {
      ReferenceBinding superclass = m_binding.superclass();
      ReferenceBinding[] superInterfaces = m_binding.superInterfaces();

      boolean hasSuperClass = superclass != null && !CharOperation.equals(superclass.compoundName, TypeConstants.JAVA_LANG_OBJECT);
      boolean hasSuperInterfaces = superInterfaces != null && superInterfaces.length > 0;
      int size = 0;
      if (hasSuperClass) {
        size++;
      }
      if (hasSuperInterfaces) {
        size += superInterfaces.length;
      }

      List<IType> bounds = new ArrayList<>(size);
      if (hasSuperClass) {
        IType t = JavaModelUtils.bindingToType(superclass, m_type.getLookupEnvironment());
        if (t != null) {
          bounds.add(t);
        }
      }
      if (hasSuperInterfaces) {
        for (ReferenceBinding b : superInterfaces) {
          IType t = JavaModelUtils.bindingToType(b, m_type.getLookupEnvironment());
          if (t != null) {
            bounds.add(t);
          }
        }
      }
      m_bounds = ListOrderedSet.decorate(bounds);
    }
    return m_bounds;
  }

  @Override
  public IType getType() {
    return m_type;
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
    if (!(obj instanceof BindingTypeParameter)) {
      return false;
    }
    BindingTypeParameter other = (BindingTypeParameter) obj;
    if (!Arrays.equals(m_id, other.m_id)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    ModelPrinter.print(this, sb);
    return sb.toString();
  }
}
