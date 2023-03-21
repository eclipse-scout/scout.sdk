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

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.ITypeOf;

public interface TypeOfSpi extends DataTypeSpi, DataTypeOwnerSpi {

  @Override
  ITypeOf api();

  DataTypeOwnerSpi dataTypeOwner();

  @Override
  default DataTypeSpi dataType() {
    return Optional.ofNullable(dataTypeOwner())
        .map(DataTypeOwnerSpi::dataType)
        .orElse(null);
  }

  @Override
  default boolean isPrimitive() {
    return false;
  }
}
