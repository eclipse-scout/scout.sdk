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

public interface IDataType extends INodeElement {

  @Override
  DataTypeSpi spi();

  boolean isPrimitive();

  default IDataType createArrayType(int dimension) {
    return spi().createArrayType(dimension).api();
  }

  default DataTypeFlavor flavor() {
    return spi().flavor();
  }

  default Stream<IDataType> typeArguments() {
    return Stream.empty();
  }

  default Stream<IDataType> componentDataTypes() {
    return spi().componentDataTypes().stream()
        .map(DataTypeSpi::api);
  }

  default int arrayDimension() {
    return spi().arrayDimension();
  }

  default Optional<IObjectLiteral> objectLiteral() {
    return spi().objectLiteral().map(ObjectLiteralSpi::api);
  }

  default Optional<IConstantValue> constantValue() {
    return spi().constantValue();
  }

  enum DataTypeFlavor {
    Single,
    Array,
    Union,
    Intersection
  }
}
