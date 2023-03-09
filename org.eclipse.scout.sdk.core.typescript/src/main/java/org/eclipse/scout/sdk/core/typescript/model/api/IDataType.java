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

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;

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

  default Stream<IDataType> componentDataTypes() {
    return spi().componentDataTypes()
        .map(DataTypeSpi::api);
  }

  Stream<IDataType> leafTypes();

  default int arrayDimension() {
    return spi().arrayDimension();
  }

  enum DataTypeFlavor {
    Single,
    Array
  }
}
