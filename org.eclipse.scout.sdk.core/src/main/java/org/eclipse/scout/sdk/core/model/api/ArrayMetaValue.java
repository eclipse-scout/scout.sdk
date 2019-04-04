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
package org.eclipse.scout.sdk.core.model.api;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link ArrayMetaValue}</h3>
 *
 * @since 6.1.0
 */
public class ArrayMetaValue extends AbstractMetaValue implements IArrayMetaValue {

  private final IMetaValue[] m_metaArray;

  public ArrayMetaValue(IMetaValue[] metaArray) {
    m_metaArray = metaArray;
  }

  @Override
  public MetaValueType type() {
    return MetaValueType.Array;
  }

  @Override
  public ISourceGenerator<IExpressionBuilder<?>> toWorkingCopy(IWorkingCopyTransformer transformer) {
    IMetaValue[] metaArray = metaValueArray();
    List<ISourceGenerator<ISourceBuilder<?>>> generators = Arrays.stream(metaArray)
        .map(mv -> mv.toWorkingCopy(transformer))
        .map(g -> g.generalize(ExpressionBuilder::create))
        .collect(toList());

    // use newlines on multi-dimensional arrays and annotation arrays only
    boolean useNewlines = metaArray.length > 0 && (metaArray[0].type() == MetaValueType.Array || metaArray[0].type() == MetaValueType.Annotation);
    return b -> b.array(generators.stream(), useNewlines);
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
      return m_metaArray[0].as(expectedType);
    }
    else {
      throw new IllegalArgumentException("expected type must be an array type: " + expectedType);
    }
    for (int i = 0; i < n; i++) {
      Array.set(array, i, m_metaArray[i].as(elementType));
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
