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

/**
 * Represents a constant value based on a literal (like string, boolean, number, class literal, object literal or
 * array).
 */
public interface IConstantValue extends IDataTypeOwner {

  /**
   * Tries to convert this constant value to the given Java class type.
   * 
   * @param expectedType
   *          The type this constant value should be converted.
   * @return An {@link Optional} holding the converted value if the conversion is possible, an empty {@link Optional}
   *         otherwise.
   */
  <T> Optional<T> convertTo(Class<T> expectedType);

  /**
   * @return A {@link Path} to the file containing this {@link IConstantValue}. The resulting {@link Optional} is empty
   *         if the element does not exist in a file (e.g. synthetic types).
   */
  Optional<Path> containingFile();

  /**
   * @return The constant value as {@link String}. Only possible for string literals.
   */
  default Optional<String> asString() {
    return convertTo(String.class);
  }

  /**
   * @return The constant value as {@link Boolean}. Only possible for boolean literals.
   */
  default Optional<Boolean> asBoolean() {
    return convertTo(Boolean.class);
  }

  /**
   * @return The constant value as {@link BigDecimal}. Only possible for number literals.
   */
  default Optional<BigDecimal> asBigDecimal() {
    return convertTo(BigDecimal.class);
  }

  /**
   * @return The constant value as {@link IES6Class}. Only possible for class literals.
   */
  default Optional<IES6Class> asES6Class() {
    return convertTo(IES6Class.class);
  }

  /**
   * @return The constant value as {@link IObjectLiteral}. Only possible for object literals.
   */
  default Optional<IObjectLiteral> asObjectLiteral() {
    return convertTo(IObjectLiteral.class);
  }

  /**
   * @return The constant as {@link IConstantValue[]}. Only possible if it is an array.
   */
  default Optional<IConstantValue[]> asArray() {
    return convertTo(IConstantValue[].class);
  }

  /**
   * @return The value in its original type (see #type()). May return a {@link String}, {@link Boolean},
   *         {@link BigDecimal}, {@link IES6Class}, {@link IObjectLiteral} or {@link IConstantValue[]}.
   * @see #type()
   */
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

  /**
   * @return Tries to detect the datatype of this {@link IConstantValue}. If it cannot be detected
   *         {@link ConstantValueType#Unknown} is returned.
   */
  ConstantValueType type();

  /**
   * Possible constant value types
   */
  enum ConstantValueType {
    /**
     * Constant value is an {@link IObjectLiteral}.
     */
    ObjectLiteral,
    /**
     * Constant value is a {@link Boolean}.
     */
    Boolean,
    /**
     * Constant value is a {@link Number}.
     */
    Numeric,
    /**
     * Constant value is a {@link String}.
     */
    String,
    /**
     * Constant value is an {@link IES6Class}.
     */
    ES6Class,
    /**
     * Constant value is an {@link IConstantValue[]}.
     */
    Array,
    /**
     * Constant value type cannot be determined.
     */
    Unknown
  }
}
