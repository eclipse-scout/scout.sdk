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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.spi.ObjectLiteralSpi;

/**
 * Represents an object literal like:
 * 
 * <pre>
 * {
 *   a: 4,
 *   b: ['f', 'g'],
 *   c: { 
 *     d: MyClass, 
 *     e: false 
 *   }
 * }
 * </pre>
 */
public interface IObjectLiteral extends INodeElement {
  @Override
  ObjectLiteralSpi spi();

  /**
   * @return A read only {@link Map} holding all properties of this {@link IObjectLiteral} with their corresponding
   *         {@link IConstantValue values}.
   */
  Map<String, IConstantValue> properties();

  /**
   * Searches for the {@link IConstantValue value} described by the given {@link JsonPointer}.
   * 
   * @param pointer
   *          A path describing the value to retrieve. Must not be {@code null}.
   * @return The {@link IConstantValue} of the element described by the path of the {@link JsonPointer} or an empty
   *         {@link Optional} if the element cannot be found.
   */
  Optional<IConstantValue> find(JsonPointer pointer);

  /**
   * Searches for the {@link IConstantValue value} described by the given {@link JsonPointer json pointer string}.<br>
   * <br>
   * <b>It may be more performant to pre-compile the {@link JsonPointer} using {@link JsonPointer#compile(CharSequence)}
   * and then using {@link #find(JsonPointer)} instead.</b>
   * 
   * @param jsonPointer
   *          The json pointer {@link String} or {@code null} (in that case the root element is returned).
   * @return The {@link IConstantValue} of the element described by the path of the pointer or an empty {@link Optional}
   *         if the element cannot be found.
   */
  Optional<IConstantValue> find(CharSequence jsonPointer);

  /**
   * Gets the value of the top-level property with given name.
   * 
   * @param name
   *          The name of the property or {@code null}.
   * @return The value of the property with given name or an empty {@link Optional} if the property cannot be found.
   */
  Optional<IConstantValue> property(String name);

  /**
   * Gets the value of the top-level property with given name as an {@link IObjectLiteral}.
   * 
   * @param name
   *          The name of the property or {@code null}.
   * @return The value of the property with given name if its value is a valid object literal or an empty
   *         {@link Optional} otherwise.
   */
  Optional<IObjectLiteral> propertyAsObjectLiteral(String name);

  /**
   * Gets the value of the top-level property with given name as a {@link String}.
   *
   * @param name
   *          The name of the property or {@code null}.
   * @return The value of the property with given name if its value is a valid string or an empty {@link Optional}
   *         otherwise.
   */
  Optional<String> propertyAsString(String name);

  /**
   * Gets the value of the top-level property with given name as an {@link IES6Class}.
   *
   * @param name
   *          The name of the property or {@code null}.
   * @return The value of the property with given name if its value is a valid class literal or an empty
   *         {@link Optional} otherwise.
   */
  Optional<IES6Class> propertyAsES6Class(String name);

  /**
   * Gets the value of the top-level property with given name and tries to retrieve it using the given data type.
   * 
   * @param name
   *          The name of the property or {@code null}.
   * @param type
   *          The target data type class. May be {@code null}.
   * @return The value of the top-level property with given name if its value can be converted to the given data type.
   *         An empty {@link Optional} otherwise.
   */
  <T> Optional<T> propertyAs(String name, Class<T> type);

  /**
   * @return All top-level property values which are themselves an object literal. This includes array values containing
   *         object literals.
   */
  Stream<IObjectLiteral> childObjectLiterals();
}
