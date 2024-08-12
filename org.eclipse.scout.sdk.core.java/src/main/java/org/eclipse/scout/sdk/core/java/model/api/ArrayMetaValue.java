/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.SimpleGenerators;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;

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
    return SimpleGenerators.createArrayMetaValueGenerator(this, transformer);
  }

  @Override
  public Stream<IJavaElement> children() {
    return Arrays.stream(m_metaArray).flatMap(IMetaValue::children);
  }

  /**
   * Returns the input array with all null elements removed. Implementation detail: only creates a new array if
   * required. If the input array contains no null elements the input is returned!
   *
   * @return Never returns {@code null} or an array with {@code null} elements
   */
  protected static IMetaValue[] withoutNullElements(IMetaValue[] original) {
    if (original == null) {
      return new IMetaValue[0];
    }

    var numNotNullElements = numNotNullElements(original);
    if (numNotNullElements == original.length) {
      return original;
    }
    var filtered = new IMetaValue[numNotNullElements];
    var targetPos = 0;
    for (var mv : original) {
      if (mv != null) {
        filtered[targetPos] = mv;
        targetPos++;
      }
    }
    return filtered;
  }

  private static int numNotNullElements(IMetaValue[] arr) {
    return (int) Arrays.stream(arr).filter(Objects::nonNull).count();
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
    var arraySize = m_metaArray.length;
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

    for (var i = 0; i < arraySize; i++) {
      Array.set(result, i, m_metaArray[i].as(arrayElementType));
    }
    return (T) result;
  }

  @Override
  public String toString() {
    var n = m_metaArray.length;
    //use newlines on multidimensional arrays and annotation arrays only
    char blockSeparator;
    if (n > 0 && (m_metaArray[0].type() == MetaValueType.Array || m_metaArray[0].type() == MetaValueType.Annotation)) {
      //noinspection HardcodedLineSeparator
      blockSeparator = '\n';
    }
    else {
      blockSeparator = JavaTypes.C_SPACE;
    }
    var buf = new StringBuilder();
    buf.append('{');
    buf.append(blockSeparator);
    if (n > 0) {
      for (var i = 0; i < n; i++) {
        var element = m_metaArray[i];
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
