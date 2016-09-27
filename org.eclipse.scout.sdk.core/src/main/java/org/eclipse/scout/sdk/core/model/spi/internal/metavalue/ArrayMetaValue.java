/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi.internal.metavalue;

import java.lang.reflect.Array;

import org.eclipse.scout.sdk.core.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;

/**
 * <h3>{@link ArrayMetaValue}</h3>
 *
 * @author Matthias Villiger
 * @since 6.1.0
 */
class ArrayMetaValue extends AbstractValue implements IArrayMetaValue {

  private final IMetaValue[] m_metaArray;

  ArrayMetaValue(IMetaValue[] metaArray) {
    m_metaArray = metaArray;
  }

  @Override
  public MetaValueType type() {
    return MetaValueType.Array;
  }

  @Override
  public IMetaValue[] metaValueArray() {
    return m_metaArray;
  }

  @Override
  Object getInternalObject(Class<?> expectedType) {
    return m_metaArray;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> expectedType) {
    int n = m_metaArray.length;
    Object array;
    Class<?> elementType;
    if (expectedType == Object.class) {
      array = new Object[n];
      elementType = Object.class;
    }
    else if (expectedType.isArray()) {
      elementType = expectedType.getComponentType();
      array = Array.newInstance(elementType, n);
    }
    else if (n == 1) {
      //value is array with length 1, requested is scalar (for example the Generated annotation)
      return m_metaArray[0].get(expectedType);
    }
    else {
      throw new ClassCastException("expected type must be an array type: " + expectedType);
    }
    for (int i = 0; i < n; i++) {
      Array.set(array, i, m_metaArray[i].get(elementType));
    }
    return (T) array;
  }

  @Override
  public String toString() {
    int n = m_metaArray.length;
    //use newlines on multi-dimensional arrays and annotation arrays only
    char blockSeparator;
    if (n > 0 && (m_metaArray[0].type() == MetaValueType.Array || m_metaArray[0].type() == MetaValueType.Annotation)) {
      blockSeparator = '\n';
    }
    else {
      blockSeparator = ' ';
    }
    StringBuilder buf = new StringBuilder();
    buf.append('{');
    buf.append(blockSeparator);
    if (n > 0) {
      for (int i = 0; i < n; i++) {
        IMetaValue element = m_metaArray[i];
        if (i > 0) {
          buf.append(',');
          buf.append(blockSeparator);
        }
        buf.append(element.toString());
      }
    }
    buf.append(blockSeparator);
    buf.append('}');
    return buf.toString();
  }
}
