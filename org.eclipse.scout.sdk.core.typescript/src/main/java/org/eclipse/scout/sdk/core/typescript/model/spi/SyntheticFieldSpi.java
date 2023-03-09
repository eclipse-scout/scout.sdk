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

import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class SyntheticFieldSpi extends AbstractFieldSpi {

  private final String m_name;
  private final DataTypeSpi m_dataType;

  protected SyntheticFieldSpi(NodeModuleSpi module, String name, DataTypeSpi dataType) {
    super(module);
    m_name = Ensure.notBlank(name);
    m_dataType = Ensure.notNull(dataType);
  }

  @Override
  public String name() {
    return m_name;
  }

  @Override
  public boolean hasModifier(Modifier modifier) {
    return false;
  }

  @Override
  public boolean isOptional() {
    return false;
  }

  @Override
  public IConstantValue constantValue() {
    return null;
  }

  @Override
  public DataTypeSpi dataType() {
    return m_dataType;
  }

  @Override
  public Optional<SourceRange> source() {
    return Optional.empty();
  }
}
