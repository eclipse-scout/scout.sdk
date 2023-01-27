/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api;

import java.util.Optional;

public interface IConstantValue {
  <T> Optional<T> convertTo(Class<T> expectedType);

  default Optional<String> asString() {
    return convertTo(String.class);
  }

  default Optional<IES6Class> asES6Class() {
    return convertTo(IES6Class.class);
  }

  default Optional<IObjectLiteral> asObjectLiteral() {
    return convertTo(IObjectLiteral.class);
  }

  ConstantValueType type();

  Optional<IDataType> dataType();

  enum ConstantValueType {
    ObjectLiteral,
    Boolean,
    Numeric,
    String,
    ES6Class,
    Array,
    Unknown
  }
}
