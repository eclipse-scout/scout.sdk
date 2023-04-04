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

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Optional;

public interface IConstantValue extends IDataTypeOwner {

  <T> Optional<T> convertTo(Class<T> expectedType);

  Optional<Path> containingFile();

  default Optional<String> asString() {
    return convertTo(String.class);
  }

  default Optional<Boolean> asBoolean() {
    return convertTo(Boolean.class);
  }

  default Optional<BigDecimal> asBigDecimal() {
    return convertTo(BigDecimal.class);
  }

  default Optional<IES6Class> asES6Class() {
    return convertTo(IES6Class.class);
  }

  default Optional<IObjectLiteral> asObjectLiteral() {
    return convertTo(IObjectLiteral.class);
  }

  default Optional<IConstantValue[]> asArray() {
    return convertTo(IConstantValue[].class);
  }

  default Optional<?> value() {
    return switch (type()) {
      case String -> asString();
      case Boolean -> asBoolean();
      case Numeric -> asBigDecimal();
      case ES6Class -> asES6Class();
      case ObjectLiteral -> asObjectLiteral();
      case Array -> asArray();
      default -> Optional.empty();
    };
  }

  ConstantValueType type();

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
