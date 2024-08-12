/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator.type;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.typescript.builder.ITypeScriptSourceBuilder;
import org.eclipse.scout.sdk.core.typescript.generator.ITypeScriptElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;

/**
 * <h3>{@link ICompositeTypeGenerator}</h3>
 * <p>
 * An {@link ITypeScriptElementGenerator} that creates unions, intersections and arrays.
 *
 * @since 13.0
 */
public interface ICompositeTypeGenerator<TYPE extends ICompositeTypeGenerator<TYPE>> extends IDataTypeGenerator<TYPE> {

  /**
   * @return The data type flavor.
   */
  Optional<DataTypeFlavor> flavor();

  /**
   * Sets the flavor of this {@link ICompositeTypeGenerator}.
   *
   * @param flavor
   *          Must not be {@code null}.
   * @return This generator.
   */
  TYPE withFlavor(DataTypeFlavor flavor);

  /**
   * @return The array dimension of this {@link ICompositeTypeGenerator}.
   */
  int arrayDimension();

  /**
   * Set the array dimension of this {@link ICompositeTypeGenerator}.
   *
   * @param dimension
   *          The array dimension (>= 0)
   * @return This generator.
   */
  TYPE withArrayDimension(int dimension);

  /**
   * @return A {@link Stream} returning all types of this {@link ICompositeTypeGenerator}.
   */
  Stream<ISourceGenerator<? super ITypeScriptSourceBuilder<?>>> types();

  /**
   * Adds the specified type to this {@link ICompositeTypeGenerator}.
   *
   * @param type
   *          The {@link ISourceGenerator} to add. Must not be {@code null}.
   * @return This generator.
   */
  TYPE withType(ISourceGenerator<? super ITypeScriptSourceBuilder<?>> type);
}
