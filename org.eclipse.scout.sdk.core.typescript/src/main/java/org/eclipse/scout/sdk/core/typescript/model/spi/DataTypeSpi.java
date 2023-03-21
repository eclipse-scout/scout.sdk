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

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;

public interface DataTypeSpi extends NodeElementSpi {

  @Override
  IDataType api();

  String name();

  boolean isPrimitive();

  default DataTypeSpi createArrayType(int dimension) {
    return containingModule().nodeElementFactory().createArrayDataType(this, dimension);
  }

  default DataTypeFlavor flavor() {
    return DataTypeFlavor.Single;
  }

  default Collection<DataTypeSpi> componentDataTypes() {
    return Collections.emptyList();
  }

  default int arrayDimension() {
    return 0;
  }

  default Optional<ObjectLiteralSpi> objectLiteral() {
    return Optional.empty();
  }

  default Optional<IConstantValue> constantValue() {
    return Optional.empty();
  }
}
