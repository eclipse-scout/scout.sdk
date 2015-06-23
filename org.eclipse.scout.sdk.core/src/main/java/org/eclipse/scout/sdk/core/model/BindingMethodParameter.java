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

import java.util.Arrays;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 *
 */
public class BindingMethodParameter implements IMethodParameter {

  private final TypeBinding m_type;
  private final IMethod m_owner;
  private final char[] m_name;
  private final int m_hash;

  private String m_nameS;
  private IType m_dataType;

  public BindingMethodParameter(TypeBinding type, char[] name, IMethod owner) {
    m_name = Validate.notNull(name);
    m_type = Validate.notNull(type);
    m_owner = Validate.notNull(owner);
    m_hash = new HashCodeBuilder().append(m_owner).append(m_name).toHashCode();
  }

  @Override
  public String getName() {
    if (m_nameS == null && m_name != null) {
      m_nameS = new String(m_name);
    }
    return m_nameS;
  }

  @Override
  public IType getType() {
    if (m_dataType == null) {
      m_dataType = JavaModelUtils.bindingToType(m_type, m_owner.getDeclaringType().getLookupEnvironment());
    }
    return m_dataType;
  }

  @Override
  public int getFlags() {
    return Flags.AccDefault;
  }

  @Override
  public IMethod getOwnerMethod() {
    return m_owner;
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
    if (!(obj instanceof BindingMethodParameter)) {
      return false;
    }
    BindingMethodParameter other = (BindingMethodParameter) obj;
    if (!Arrays.equals(m_name, other.m_name)) {
      return false;
    }
    if (m_owner == null) {
      if (other.m_owner != null) {
        return false;
      }
    }
    else if (!m_owner.equals(other.m_owner)) {
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
