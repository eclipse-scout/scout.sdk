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
import org.eclipse.scout.sdk.core.typescript.model.spi.SimpleDataTypeSpi;

public interface IDataType {

  DataTypeSpi spi();

  String name();

  boolean isPrimitive();

  default DataTypeFlavor dataTypeFlavor() {
    return spi().dataTypeFlavor();
  }

  default Stream<IDataType> componentDataTypes() {
    return spi().componentDataTypes()
        .map(DataTypeSpi::api);
  }

  default int arrayDimension() {
    return spi().arrayDimension();
  }

  enum DataTypeFlavor {
    Single,
    Array
  }

  static IDataType createSimple(String dataType) {
    return new SimpleDataTypeSpi(dataType).api();
  }
}
