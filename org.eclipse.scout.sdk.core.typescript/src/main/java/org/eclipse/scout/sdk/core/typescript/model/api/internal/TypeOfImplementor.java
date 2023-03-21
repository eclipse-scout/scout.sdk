/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.internal;

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataTypeOwner;
import org.eclipse.scout.sdk.core.typescript.model.api.ITypeOf;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeOwnerSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.TypeOfSpi;

public class TypeOfImplementor extends DataTypeImplementor<TypeOfSpi> implements ITypeOf {

  public TypeOfImplementor(TypeOfSpi spi) {
    super(spi);
  }

  @Override
  public Optional<IDataType> dataType() {
    return Optional.ofNullable(spi().dataType()).map(DataTypeSpi::api);
  }

  @Override
  public Optional<IDataTypeOwner> dataTypeOwner() {
    return Optional.ofNullable(spi().dataTypeOwner()).map(DataTypeOwnerSpi::api);
  }
}
