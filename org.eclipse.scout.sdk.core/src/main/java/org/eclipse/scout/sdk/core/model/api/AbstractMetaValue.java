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

import static org.eclipse.scout.sdk.core.generator.SimpleGenerators.createMetaValueGenerator;

import java.lang.reflect.Array;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.transformer.IWorkingCopyTransformer;

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
    return createMetaValueGenerator(this, transformer);
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
      Class<?> componentType = expectedType.getComponentType();
      Object val = getInternalObject(componentType);
      if (val == null) {
        // null values for annotations are not allowed according to JLS.
        // it might happen even though if e.g. a Java file contains compile errors.
        // in that case treat it as if the element is not there at all (it is invalid anyway).
        return (T) Array.newInstance(componentType, 0);
      }

      T array = (T) Array.newInstance(componentType, 1);
      Array.set(array, 0, val);
      return array;
    }
    return (T) getInternalObject(expectedType);
  }
}
