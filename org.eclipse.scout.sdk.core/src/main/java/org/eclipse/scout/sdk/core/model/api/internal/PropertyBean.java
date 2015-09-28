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
package org.eclipse.scout.sdk.core.model.api.internal;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IPropertyBean;
import org.eclipse.scout.sdk.core.model.api.IType;

/**
 * Default description of a Java bean property.
 */
public class PropertyBean implements IPropertyBean {

  private IMethod m_readMethod;
  private IMethod m_writeMethod;
  private String m_beanName;
  private final IType m_declaringType;

  public PropertyBean(IType declaringType, String beanName) {
    m_declaringType = declaringType;
    m_beanName = beanName;
  }

  @Override
  public String toString() {
    return m_declaringType.name() + "#" + m_beanName;
  }

  @Override
  public IType declaringType() {
    return m_declaringType;
  }

  public void setBeanName(String beanName) {
    m_beanName = beanName;
  }

  @Override
  public String name() {
    return m_beanName;
  }

  @Override
  public IType type() {
    if (m_readMethod != null) {
      return m_readMethod.returnType();
    }

    if (m_writeMethod != null) {
      IMethodParameter parameter = m_writeMethod.parameters().first();
      if (parameter != null) {
        return parameter.dataType();
      }
    }

    return null;
  }

  @Override
  public IMethod readMethod() {
    return m_readMethod;
  }

  @Override
  public IMethod writeMethod() {
    return m_writeMethod;
  }

  public void setReadMethod(IMethod readMethod) {
    m_readMethod = readMethod;
  }

  public void setWriteMethod(IMethod writeMethod) {
    m_writeMethod = writeMethod;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(m_beanName).append(m_declaringType).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof PropertyBean)) {
      return false;
    }
    PropertyBean other = (PropertyBean) obj;
    if (m_beanName == null) {
      if (other.m_beanName != null) {
        return false;
      }
    }
    else if (!m_beanName.equals(other.m_beanName)) {
      return false;
    }
    if (m_declaringType == null) {
      if (other.m_declaringType != null) {
        return false;
      }
    }
    else if (!m_declaringType.equals(other.m_declaringType)) {
      return false;
    }
    return true;
  }
}
