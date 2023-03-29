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
import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IVariable;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.VariableSpi;

public class VariableImplementor<SPI extends VariableSpi> extends AbstractNodeElement<SPI> implements IVariable {
  public VariableImplementor(SPI spi) {
    super(spi);
  }

  @Override
  public String name() {
    return spi().name();
  }

  @Override
  public Optional<IDataType> dataType() {
    return Optional.ofNullable(spi().dataType()).map(DataTypeSpi::api);
  }

  @Override
  public boolean hasModifier(Modifier modifier) {
    return modifier != null && spi().hasModifier(modifier);
  }

  @Override
  public IConstantValue constantValue() {
    return spi().constantValue();
  }
}
