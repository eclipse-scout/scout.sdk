/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.spi;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;

public interface DataTypeSpi extends NodeElementSpi {

  @Override
  IDataType api();

  String name();

  boolean isPrimitive();

  default DataTypeFlavor dataTypeFlavor() {
    return DataTypeFlavor.Single;
  }

  default Stream<DataTypeSpi> componentDataTypes() {
    return Stream.empty();
  }

  default int arrayDimension() {
    return 0;
  }
}
