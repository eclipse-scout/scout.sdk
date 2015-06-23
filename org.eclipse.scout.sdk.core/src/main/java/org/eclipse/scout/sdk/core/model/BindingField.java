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

import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.scout.sdk.core.model.JavaModelUtils.ExpressionValueDesc;

/**
 *
 */
public class BindingField implements IField {

  private final FieldBinding m_fb;
  private final char[] m_id;
  private final int m_hash;
  private final IType m_declaringType;

  private int m_flags;
  private String m_name;
  private Object m_constantValue;
  private IType m_type;
  private ListOrderedSet<IAnnotation> m_annotations;

  public BindingField(FieldBinding fd, IType declaringType) {
    m_fb = Validate.notNull(fd);
    m_declaringType = Validate.notNull(declaringType);
    m_flags = -1;
    m_id = fd.computeUniqueKey();
    m_hash = Arrays.hashCode(m_id);
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = JavaModelUtils.getTypeFlags(m_fb.modifiers, null, JavaModelUtils.hasDeprecatedAnnotation(m_fb.getAnnotations()));
    }
    return m_flags;
  }

  @Override
  public IType getDataType() {
    if (m_type == null) {
      m_type = JavaModelUtils.bindingToType(m_fb.type, m_declaringType.getLookupEnvironment());
    }
    return m_type;
  }

  @Override
  public ListOrderedSet<IAnnotation> getAnnotations() {
    if (m_annotations == null) {
      m_annotations = JavaModelUtils.annotationBindingsToIAnnotations(m_fb.getAnnotations(), this, m_declaringType.getLookupEnvironment());
    }
    return m_annotations;
  }

  @Override
  public String getName() {
    if (m_name == null) {
      m_name = new String(m_fb.name);
    }
    return m_name;
  }

  @Override
  public Object getValue() {
    if (m_constantValue == null) {
      ExpressionValueDesc valueDesc = JavaModelUtils.getConstantValue(m_fb.constant());
      if (valueDesc == null) {
        FieldBinding origBinding = m_fb.original();
        ReferenceBinding refBinding = origBinding.declaringClass;
        if (refBinding instanceof SourceTypeBinding) {
          SourceTypeBinding stb = (SourceTypeBinding) refBinding;
          Expression initEx = stb.scope.referenceContext.declarationOf(origBinding).initialization;
          Object rawVal = JavaModelUtils.computeExpressionValue(initEx, null);
          if (rawVal instanceof Constant) {
            valueDesc = JavaModelUtils.getConstantValue((Constant) rawVal);
          }
        }
      }
      if (valueDesc != null) {
        m_constantValue = valueDesc.value;
      }
    }
    return m_constantValue;
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
    if (!(obj instanceof BindingField)) {
      return false;
    }
    BindingField other = (BindingField) obj;
    if (!Arrays.equals(m_id, other.m_id)) {
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
