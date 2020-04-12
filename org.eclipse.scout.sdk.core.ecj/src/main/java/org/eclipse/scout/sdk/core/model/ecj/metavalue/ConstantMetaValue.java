/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj.metavalue;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.scout.sdk.core.model.api.AbstractMetaValue;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;

/**
 * <h3>{@link ConstantMetaValue}</h3>
 *
 * @since 6.1.0
 */
class ConstantMetaValue extends AbstractMetaValue {
  private final Constant m_constant;
  private final MetaValueType m_type;

  ConstantMetaValue(Constant c, MetaValueType type) {
    m_constant = c;
    m_type = type;
  }

  @Override
  public MetaValueType type() {
    return m_type;
  }

  /**
   * @return The constant value in the type defined by this instance (m_type)
   */
  Object getInternalObject() {
    switch (type()) {
      case Bool:
        return m_constant.booleanValue();
      case Byte:
        return m_constant.byteValue();
      case Char:
        return m_constant.charValue();
      case Double:
        return m_constant.doubleValue();
      case Float:
        return m_constant.floatValue();
      case Int:
        return m_constant.intValue();
      case Long:
        return m_constant.longValue();
      case Short:
        return m_constant.shortValue();
      case String:
        return m_constant.stringValue();
      default:
        return m_constant;
    }
  }

  @Override
  @SuppressWarnings("pmd:NPathComplexity")
  protected Object getInternalObject(Class<?> expectedType) {
    if (expectedType == Object.class) {
      return getInternalObject();
    }
    if (expectedType == boolean.class || expectedType == Boolean.class) {
      return m_constant.booleanValue();
    }
    if (expectedType == byte.class || expectedType == Byte.class) {
      return m_constant.byteValue();
    }
    if (expectedType == char.class || expectedType == Character.class) {
      return m_constant.charValue();
    }
    if (expectedType == double.class || expectedType == Double.class) {
      return m_constant.doubleValue();
    }
    if (expectedType == float.class || expectedType == Float.class) {
      return m_constant.floatValue();
    }
    if (expectedType == int.class || expectedType == Integer.class) {
      return m_constant.intValue();
    }
    if (expectedType == long.class || expectedType == Long.class) {
      return m_constant.longValue();
    }
    if (expectedType == short.class || expectedType == Short.class) {
      return m_constant.shortValue();
    }
    if (expectedType == String.class) {
      return m_constant.stringValue();
    }
    return m_constant;
  }

  @Override
  public String toString() {
    Object internalObject = getInternalObject(Object.class);
    if (internalObject == null) {
      return "null";
    }
    return internalObject.toString();
  }
}
