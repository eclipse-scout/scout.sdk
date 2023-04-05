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

import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.TypeParameterSpi;

public class TypeParameterImplementor<SPI extends TypeParameterSpi> extends AbstractNodeElement<SPI> implements ITypeParameter {

  public TypeParameterImplementor(SPI spi) {
    super(spi);
  }

  @Override
  public String name() {
    return spi().name();
  }

  @Override
  public IES6Class declaringClass() {
    return spi().declaringClass().api();
  }

  @Override
  public Optional<IDataType> constraint() {
    return Optional.ofNullable(spi().constraint())
        .map(DataTypeSpi::api);
  }

  @Override
  public Optional<IDataType> defaultConstraint() {
    return Optional.ofNullable(spi().defaultConstraint())
        .map(DataTypeSpi::api);
  }
}
