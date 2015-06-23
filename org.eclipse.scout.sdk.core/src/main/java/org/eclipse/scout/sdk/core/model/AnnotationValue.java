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

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.scout.sdk.core.model.JavaModelUtils.ExpressionValueDesc;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;

/**
 *
 */
public class AnnotationValue implements IAnnotationValue {

  private final MemberValuePair m_mvp;
  private final Expression m_expression;
  private final String m_name;
  private final IAnnotation m_ownerAnnotation;
  private final ClassScope m_scope;
  private final int m_hash;
  private final ILookupEnvironment m_lookupEnvironment;
  private ExpressionValueDesc m_value;

  public AnnotationValue(String name, Expression e, ClassScope scope, IAnnotation owner, ILookupEnvironment lookupEnv) {
    this(null, Validate.notNull(e), Validate.notNull(name), Validate.notNull(owner), Validate.notNull(scope), lookupEnv);
  }

  AnnotationValue(MemberValuePair mvp, ClassScope scope, IAnnotation owner, ILookupEnvironment lookupEnv) {
    this(Validate.notNull(mvp), null, new String(mvp.name), Validate.notNull(owner), Validate.notNull(scope), lookupEnv);
  }

  public AnnotationValue(MemberValuePair mvp, Expression expression, String name, IAnnotation ownerAnnotation, ClassScope scope, ILookupEnvironment lookupEnv) {
    super();
    m_lookupEnvironment = lookupEnv;
    m_mvp = mvp;
    m_expression = expression;
    m_name = name;
    m_ownerAnnotation = ownerAnnotation;
    m_scope = scope;
    m_hash = new HashCodeBuilder().append(name).append(ownerAnnotation).toHashCode();
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public Object getValue() {
    ExpressionValueDesc valueDesc = getValueDesc();
    if (valueDesc == null) {
      return null;
    }
    return valueDesc.value;
  }

  @Override
  public ExpressionValueType getValueType() {
    ExpressionValueDesc valueDesc = getValueDesc();
    if (valueDesc == null) {
      return ExpressionValueType.Unknown;
    }
    return valueDesc.type;
  }

  protected ExpressionValueDesc getValueDesc() {
    if (m_value == null) {
      if (m_expression == null) {
        m_value = JavaModelUtils.getAnnotationValue(m_mvp.value, m_name, m_scope, m_ownerAnnotation, m_lookupEnvironment);
      }
      else {
        m_value = JavaModelUtils.getAnnotationValue(m_expression, m_name, m_scope, m_ownerAnnotation, m_lookupEnvironment);
      }
    }
    return m_value;
  }

  @Override
  public IAnnotation getOwnerAnnotation() {
    return m_ownerAnnotation;
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
    if (!(obj instanceof AnnotationValue)) {
      return false;
    }
    AnnotationValue other = (AnnotationValue) obj;
    if (!m_name.equals(other.m_name)) {
      return false;
    }
    if (!m_ownerAnnotation.equals(other.m_ownerAnnotation)) {
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
