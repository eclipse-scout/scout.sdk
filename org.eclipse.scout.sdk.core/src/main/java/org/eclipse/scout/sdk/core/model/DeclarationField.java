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
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.JavaModelUtils.ExpressionValueDesc;

/**
 *
 */
public class DeclarationField implements IField {

  private final FieldDeclaration m_fd;
  private final char[] m_name;
  private final int m_hash;
  private final IType m_declaringType;
  private final ClassScope m_scope;

  private int m_flags;
  private String m_nameS;
  private Object m_constantValue;
  private IType m_type;
  private List<IAnnotation> m_annotations;

  public DeclarationField(FieldDeclaration fd, IType declaringType, ClassScope scope) {
    m_fd = Validate.notNull(fd);
    m_scope = Validate.notNull(scope);
    m_declaringType = Validate.notNull(declaringType);
    m_name = fd.name;
    m_hash = new HashCodeBuilder().append(declaringType).append(m_name).toHashCode();
    m_flags = -1;
  }

  @Override
  public Object getValue() {
    if (m_constantValue == null && m_fd.initialization != null) {
      Object rawVal = JavaModelUtils.computeExpressionValue(m_fd.initialization, null);
      if (rawVal instanceof Constant) {
        ExpressionValueDesc constantValue = JavaModelUtils.getConstantValue((Constant) rawVal);
        if (constantValue != null) {
          m_constantValue = constantValue.value;
        }
      }
    }
    return m_constantValue;
  }

  @Override
  public IType getDataType() {
    if (m_type == null) {
      TypeBinding tb = m_fd.type.resolvedType;
      if (tb == null) {
        tb = m_fd.type.resolveType(m_scope);
      }
      m_type = JavaModelUtils.bindingToType(tb, m_declaringType.getLookupEnvironment());
    }
    return m_type;
  }

  @Override
  public List<IAnnotation> getAnnotations() {
    if (m_annotations == null) {
      Annotation[] annots = m_fd.annotations;
      m_annotations = JavaModelUtils.annotationsToIAnnotations(annots, m_scope, this, m_declaringType.getLookupEnvironment());
    }
    return m_annotations;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = JavaModelUtils.getTypeFlags(m_fd.modifiers, null, JavaModelUtils.hasDeprecatedAnnotation(m_fd.annotations));
    }
    return m_flags;
  }

  @Override
  public String getName() {
    if (m_nameS == null) {
      m_nameS = new String(m_fd.name);
    }
    return m_nameS;
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
    if (!(obj instanceof DeclarationField)) {
      return false;
    }
    DeclarationField other = (DeclarationField) obj;
    if (m_declaringType == null) {
      if (other.m_declaringType != null) {
        return false;
      }
    }
    else if (!m_declaringType.equals(other.m_declaringType)) {
      return false;
    }
    if (!Arrays.equals(m_name, other.m_name)) {
      return false;
    }
    return true;
  }

  @Override
  public IType getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    ModelPrinter.print(this, sb);
    return sb.toString();
  }
}
