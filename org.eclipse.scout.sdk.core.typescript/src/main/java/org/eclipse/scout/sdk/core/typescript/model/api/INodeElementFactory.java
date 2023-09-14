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

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;

/**
 * Factory that creates {@link INodeElement}s.
 */
public interface INodeElementFactory {
  /**
   * Creates a new synthetic (not existing in source code) {@link IField} with given attributes.
   * 
   * @param name
   *          The name of the field (must not be {@code null}).
   * @param dataType
   *          The {@link IDataType} of the field (must not be {@code null}).
   * @param declaringClass
   *          The {@link IES6Class} of the field (must not be {@code null}).
   * @return A new {@link IField} with given attributes.
   */
  IField createSyntheticField(String name, IDataType dataType, IES6Class declaringClass);

  /**
   * Creates an {@link IDataType} from given {@link IObjectLiteral}.<br>
   * This may be necessary if a variable uses an object literal as initializer:
   * 
   * <pre>
   *     let variable = {
   *        A: 1,
   *        B: 2,
   *        C: 3
   *     };
   * </pre>
   * 
   * @param name
   *          The name of the data type (must not be {@code null}).
   * @param objectLiteral
   *          The object literal of the data type (must not be {@code null}).
   * @return The created {@link IDataType}. The object literal may be retrieved again using
   *         {@link IDataType#objectLiteral()}.
   * @see IDataType#objectLiteral()
   */
  IDataType createObjectLiteralDataType(String name, IObjectLiteral objectLiteral);

  /**
   * Creates an array data type with the given component type and array dimension.<br>
   * If this instance is already an array type, the given dimension is added to the existing dimension.
   *
   * @param componentDataType
   *          The component type. May be {@code null}.
   * @param arrayDimension
   *          The number of dimensions to add. If < 1 componentDataType is returned.
   * @return A new array data type having dimension: dimensionOf(componentDataType) + dimension
   */
  IDataType createArrayDataType(IDataType componentDataType, int arrayDimension);

  /**
   * Creates a new TypeScript union type (A | B | C) consisting of the given data types.<br>
   * If the collection is empty or only contains {@code null} elements, the resulting data type will be {@code null} as
   * well.<br>
   * If the collection only contains one element, this data type is returned directly.<br>
   * It is therefore not guaranteed, that the resulting {@link IDataType} is of flavor {@link DataTypeFlavor#Union}!
   * 
   * @param componentDataTypes
   *          The component data types. May be {@code null}.
   * @return The created {@link IDataType} or the single input data type or {@code null} (see above).
   */
  IDataType createUnionDataType(Collection<IDataType> componentDataTypes);

  /**
   * Creates a new TypeScript intersection type (A & B & C) consisting of the given data types.<br>
   * If the collection is empty or only contains {@code null} elements, the resulting data type will be {@code null} as
   * well.<br>
   * If the collection only contains one element, this data type is returned directly.<br>
   * It is therefore not guaranteed, that the resulting {@link IDataType} is of flavor
   * {@link DataTypeFlavor#Intersection}!
   *
   * @param componentDataTypes
   *          The component data types. May be {@code null}.
   * @return The created {@link IDataType} or the single input data type or {@code null} (see above).
   */
  IDataType createIntersectionDataType(Collection<IDataType> componentDataTypes);

  /**
   * Converts an {@link IConstantValue} to a data type.<br>
   * <br>
   * Example for a union data type consisting of 3 child types which each is a data type with a constant value:
   * 
   * <pre>
   *   export type Alignment = -1 | 0 | 1;
   * </pre>
   * 
   * @param constantValue
   *          The constant value to convert. May be {@code null}.
   * @return The created {@link IDataType} or {@code null} if the constant value is {@code null}. The original
   *         {@link IConstantValue} can be obtained again using {@link IDataType#constantValue()}.
   * @see IDataType#constantValue()
   */
  IDataType createConstantValueDataType(IConstantValue constantValue);

  /**
   * Creates a new {@link IES6Class} data type based on the given class. The type parameters of the class given are
   * initialized with the given arguments.
   * 
   * @param clazz
   *          The base class (must not be {@code null}).
   * @param arguments
   *          The arguments (must not be {@code null}).
   * @return The created {@link IES6Class}. The arguments are available using {@link IES6Class#typeArguments()}.
   * @see IES6Class#typeArguments()
   */
  IES6Class createClassWithTypeArguments(IES6Class clazz, List<IDataType> arguments);
}
