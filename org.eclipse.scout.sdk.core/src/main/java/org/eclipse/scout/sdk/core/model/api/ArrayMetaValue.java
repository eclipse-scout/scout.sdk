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
package org.eclipse.scout.sdk.core.model.api;

import static org.eclipse.scout.sdk.core.generator.SimpleGenerators.createArrayMetaValueGenerator;

import java.lang.reflect.Array;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link ArrayMetaValue}</h3>
 *
 * @since 6.1.0
 */
public class ArrayMetaValue extends AbstractMetaValue implements IArrayMetaValue {

  private final IMetaValue[] m_metaArray;

  public ArrayMetaValue(IMetaValue[] metaArray) {
    m_metaArray = withoutNullElements(metaArray);
  }

  @Override
  public MetaValueType type() {
    return MetaValueType.Array;
  }

  @Override
  public ISourceGenerator<IExpressionBuilder<?>> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return createArrayMetaValueGenerator(this, transformer);
  }

  @Override
  public Stream<IJavaElement> children() {
    return Stream.of(m_metaArray).flatMap(IMetaValue::children);
  }

  /**
   * Returns the input array with all null elements removed. Implementation detail: only creates a new array if
   * required. If the input array contains no null elements the input is returned!
   *
   * @param original
   * @return . Never returns {@code null} or an array with {@code null} elements
   */
  protected static IMetaValue[] withoutNullElements(IMetaValue[] original) {
    if (original == null) {
      return new IMetaValue[0];
    }

    int numNotNullElements = numNotNullElements(original);
    if (numNotNullElements == original.length) {
      return original;
    }
    IMetaValue[] filtered = new IMetaValue[numNotNullElements];
    int targetPos = 0;
    for (IMetaValue mv : original) {
      if (mv != null) {
        filtered[targetPos] = mv;
        targetPos++;
      }
    }
    return filtered;
  }

  private static int numNotNullElements(IMetaValue[] arr) {
    int num = 0;
    for (IMetaValue mv : arr) {
      if (mv != null) {
        num++;
      }
    }
    return num;
  }

  @Override
  public IMetaValue[] metaValueArray() {
    return m_metaArray;
  }

  @Override
  protected Object getInternalObject(Class<?> expectedType) {
    return m_metaArray;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T as(Class<T> expectedType) {
    int arraySize = m_metaArray.length;
    Class<?> arrayElementType;
    Object result;
    if (expectedType == Object.class) {
      arrayElementType = Object.class;
      result = new Object[arraySize];
    }
    else if (expectedType.isArray()) {
      arrayElementType = expectedType.getComponentType();
      result = Array.newInstance(arrayElementType, arraySize);
    }
    else if (arraySize == 1) {
      // value is array with length 1, requested is scalar (for example the SuppressWarnings annotation)
      return m_metaArray[0].as(expectedType);
    }
    else {
      throw new IllegalArgumentException("expected type must be an array type but was: " + expectedType);
    }

    for (int i = 0; i < arraySize; i++) {
      Array.set(result, i, m_metaArray[i].as(arrayElementType));
    }
    return (T) result;
  }

  @Override
  public String toString() {
    int n = m_metaArray.length;
    //use newlines on multi-dimensional arrays and annotation arrays only
    char blockSeparator;
    if (n > 0 && (m_metaArray[0].type() == MetaValueType.Array || m_metaArray[0].type() == MetaValueType.Annotation)) {
      //noinspection HardcodedLineSeparator
      blockSeparator = '\n';
    }
    else {
      blockSeparator = JavaTypes.C_SPACE;
    }
    StringBuilder buf = new StringBuilder();
    buf.append('{');
    buf.append(blockSeparator);
    if (n > 0) {
      for (int i = 0; i < n; i++) {
        IMetaValue element = m_metaArray[i];
        if (i > 0) {
          buf.append(JavaTypes.C_COMMA);
          buf.append(blockSeparator);
        }
        buf.append(element);
      }
    }
    buf.append(blockSeparator);
    buf.append('}');
    return buf.toString();
  }
}
