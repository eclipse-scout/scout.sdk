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

import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;

public abstract class AbstractNodeElement<SPI extends NodeElementSpi> implements INodeElement {
  private final SPI m_spi;
  private final FinalValue<Optional<String>> m_exportedName;
  private final FinalValue<Optional<SourceRange>> m_source;

  protected AbstractNodeElement(SPI spi) {
    m_spi = Ensure.notNull(spi);
    m_exportedName = new FinalValue<>();
    m_source = new FinalValue<>();
  }

  @Override
  public SPI spi() {
    return m_spi;
  }

  @Override
  public INodeModule containingModule() {
    return spi().containingModule().api();
  }

  @Override
  public Optional<String> exportAlias() {
    return m_exportedName.computeIfAbsentAndGet(this::computeExportAlias);
  }

  @Override
  public Optional<SourceRange> source() {
    return m_source.computeIfAbsentAndGet(() -> spi().source());
  }

  public Optional<String> computeExportAlias() {
    return containingModule()
        .exports()
        .withElement(this)
        .first()
        .map(INodeElement::name);
  }
}
