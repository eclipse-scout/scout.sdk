/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;

/**
 * <h3>{@link MethodParameter}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 06.12.2012
 */
public class MethodParameter implements IMethodParameter {

  private final Argument m_argument;
  private final IMethod m_owner;
  private final ClassScope m_scope;
  private final int m_hash;
  private String m_name;
  private IType m_dataType;
  private int m_flags;

  public MethodParameter(Argument argument, IMethod owner, ClassScope scope) {
    m_argument = Validate.notNull(argument);
    m_owner = Validate.notNull(owner);
    m_scope = Validate.notNull(scope);
    m_hash = new HashCodeBuilder().append(owner).append(m_argument.name).toHashCode();
    m_flags = -1;
  }

  @Override
  public IMethod getOwnerMethod() {
    return m_owner;
  }

  @Override
  public String getName() {
    if (m_name == null) {
      m_name = new String(m_argument.name);
    }
    return m_name;
  }

  @Override
  public IType getType() {
    if (m_dataType == null) {
      if (m_argument.type.resolvedType == null) {
        m_argument.type.resolveType(m_scope);
      }
      m_dataType = JavaModelUtils.bindingToType(m_argument.type.resolvedType, m_owner.getDeclaringType().getLookupEnvironment());
    }
    return m_dataType;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = JavaModelUtils.getMethodFlags(m_argument.modifiers, false, JavaModelUtils.hasDeprecatedAnnotation(m_argument.annotations));
    }
    return m_flags;
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
    if (!(obj instanceof MethodParameter)) {
      return false;
    }
    MethodParameter other = (MethodParameter) obj;
    if (!Arrays.equals(m_argument.name, other.m_argument.name)) {
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
