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
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.ObjectLiteralSpi;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * Represents a data type (e.g. string or number or MyClass or typeof x).
 */
public interface IDataType extends INodeElement {

  @Override
  DataTypeSpi spi();

  /**
   * @return {@code true} if this type is a primitive type (string, number, bigint, boolean, undefined, symbol or null).
   */
  boolean isPrimitive();

  /**
   * Creates an array data type with the given dimension and this instance as element type.<br>
   * If this instance is already an array type, the given dimension is added to the existing dimension.
   * 
   * @param dimension
   *          The number of dimensions to add. If < 1 this instance is returned.
   * @return A new array data type having dimension: dimensionOf(this) + dimension
   */
  default IDataType createArrayType(int dimension) {
    return containingModule().nodeElementFactory().createArrayDataType(this, dimension);
  }

  /**
   * @return The flavor of this data type (single, array, union, intersection).
   */
  default DataTypeFlavor flavor() {
    return spi().flavor();
  }

  /**
   * @return Gets the type arguments of this data type.
   */
  default Stream<IDataType> typeArguments() {
    return Stream.empty();
  }

  /**
   * Checks if the given childType can be assigned to this data type. For classes e.g. this means this type is either
   * the same as or a super class of the type given.
   * 
   * @param childType
   *          The type to check if it can be assigned to this type.
   * @return {@code true} if childType can be assigned to this.
   */
  boolean isAssignableFrom(IDataType childType);

  /**
   * Get the child types of this data type. <br>
   * <br>
   * Child types examples:
   * <ol>
   * <li>if this type has the {@link DataTypeFlavor#Union} or {@link DataTypeFlavor#Intersection}, the child types are
   * the components.</li>
   * <li>if this type is a typeof-datatype the child is the right-hand-side of the typeof operator.</li>
   * <li>if this type is a type alias type, the child is the right-hand-side of the alias assignment.</li>
   * </ol>
   * 
   * @return The child types of this data type.
   */
  default Stream<IDataType> childTypes() {
    return spi().childTypes().stream()
        .map(DataTypeSpi::api);
  }

  /**
   * Visits this instance and all of its {@link #childTypes()} recursively in breadth-first order.
   * 
   * @param visitor
   *          The visitor to call for each data type. Must not be {@code null}.
   * @return The result from the last call to the visitor.
   */
  TreeVisitResult visit(IDataTypeVisitor visitor);

  /**
   * @return If this data type is an array (see {@link #flavor()}), its dimension. Otherwise, 0 is returned.
   */
  default int arrayDimension() {
    return spi().arrayDimension();
  }

  /**
   * Gets this data type as object literal. <br>
   * <br>
   * Example for a data type having an object literal:
   * 
   * <pre>
   * MyEnum = {
   *    A: 1,
   *    B: 2,
   *    C: 3
   * } as const;
   * </pre>
   * 
   * @return An {@link Optional} with an {@link IObjectLiteral} in case this {@link IDataType} is based on an object
   *         literal.
   */
  default Optional<IObjectLiteral> objectLiteral() {
    return spi().objectLiteral().map(ObjectLiteralSpi::api);
  }

  /**
   * Gets this data type as {@link IConstantValue}. <br>
   * <br>
   * Example for a union data type consisting of 3 child types which each is a data type with a constant value:
   * 
   * <pre>
   *      export type Alignment = -1 | 0 | 1;
   * </pre>
   * 
   * @return An {@link Optional} with an {@link IConstantValue} in case this {@link IDataType} is based on a constant
   *         value.
   */
  default Optional<IConstantValue> constantValue() {
    return spi().constantValue();
  }

  enum DataTypeFlavor {
    /**
     * A simple single data type (like number).
     */
    Single,
    /**
     * An array data type (like number[]).
     */
    Array,
    /**
     * A union data type (number | string).
     */
    Union,
    /**
     * An intersection data type (A & B).
     */
    Intersection
  }
}
