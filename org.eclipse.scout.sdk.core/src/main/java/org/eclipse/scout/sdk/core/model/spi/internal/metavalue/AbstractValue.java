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

import org.eclipse.scout.sdk.core.model.api.IMetaValue;

/**
 * <h3>{@link AbstractValue}</h3>
 *
 * @author Matthias Villiger
 * @since 6.1.0
 */
abstract class AbstractValue implements IMetaValue {

  abstract Object getInternalObject(Class<?> expectedType);

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> expectedType) {
    if (expectedType.isArray()) {
      //value is scalar, requested is array (for example the Generated annotation)
      T array = (T) Array.newInstance(expectedType.getComponentType(), 1);
      Array.set(array, 0, getInternalObject(expectedType.getComponentType()));
      return array;
    }
    return (T) getInternalObject(expectedType);
  }
}
