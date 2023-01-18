/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import java.lang.reflect.Array;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.generator.SimpleGenerators;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;

/**
 * <h3>{@link AbstractMetaValue}</h3>
 *
 * @since 6.1.0
 */
public abstract class AbstractMetaValue implements IMetaValue {

  protected abstract Object getInternalObject(Class<?> expectedType);

  @Override
  public ISourceGenerator<IExpressionBuilder<?>> toWorkingCopy() {
    return toWorkingCopy(null);
  }

  @Override
  public ISourceGenerator<IExpressionBuilder<?>> toWorkingCopy(IWorkingCopyTransformer transformer) {
    return SimpleGenerators.createMetaValueGenerator(this, transformer);
  }

  @Override
  public Stream<IJavaElement> children() {
    return Stream.empty();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T as(Class<T> expectedType) {
    if (expectedType.isArray()) {
      // value is scalar, requested is array (for example the SuppressWarnings annotation)
      var componentType = expectedType.getComponentType();
      var val = getInternalObject(componentType);
      if (val == null) {
        // null values for annotations are not allowed according to JLS.
        // it might happen even though if e.g. a Java file contains compile errors.
        // in that case treat it as if the element is not there at all (it is invalid anyway).
        return (T) Array.newInstance(componentType, 0);
      }

      var array = (T) Array.newInstance(componentType, 1);
      Array.set(array, 0, val);
      return array;
    }
    return (T) getInternalObject(expectedType);
  }
}
