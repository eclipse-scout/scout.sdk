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
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.scout.sdk.core.model.JavaModelUtils.ExpressionValueDesc;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;

/**
 *
 */
public class ElementAnnotationValue implements IAnnotationValue {

  private final ElementValuePair m_evp;
  private final Object m_rawValue;
  private final IAnnotation m_owner;
  private final String m_name;
  private final ClassScope m_scope;
  private final int m_hash;
  private final ILookupEnvironment m_lookupEnvironment;
  private ExpressionValueDesc m_value;

  public ElementAnnotationValue(ElementValuePair evp, ClassScope scope, IAnnotation owner, ILookupEnvironment lookupEnv) {
    m_scope = scope;
    m_owner = Validate.notNull(owner);
    m_evp = Validate.notNull(evp);
    m_lookupEnvironment = lookupEnv;
    m_rawValue = null;
    m_name = new String(m_evp.getName());
    m_hash = new HashCodeBuilder().append(m_name).append(m_owner).toHashCode();
  }

  public ElementAnnotationValue(String name, Object rawValue, ClassScope scope, IAnnotation owner, ILookupEnvironment lookupEnv) {
    m_scope = scope;
    m_evp = null;
    m_lookupEnvironment = lookupEnv;
    m_rawValue = rawValue;
    m_owner = owner;
    m_name = name;
    m_hash = new HashCodeBuilder().append(m_name).append(m_owner).toHashCode();
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
      if (m_rawValue == null) {
        m_value = JavaModelUtils.getAnnotationValue(m_evp.getValue(), m_name, m_scope, m_owner, m_lookupEnvironment);
      }
      else {
        m_value = JavaModelUtils.getAnnotationValue(m_rawValue, m_name, m_scope, m_owner, m_lookupEnvironment);
      }
    }
    return m_value;
  }

  @Override
  public IAnnotation getOwnerAnnotation() {
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
    if (!(obj instanceof ElementAnnotationValue)) {
      return false;
    }
    ElementAnnotationValue other = (ElementAnnotationValue) obj;
    if (m_name == null) {
      if (other.m_name != null) {
        return false;
      }
    }
    else if (!m_name.equals(other.m_name)) {
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
