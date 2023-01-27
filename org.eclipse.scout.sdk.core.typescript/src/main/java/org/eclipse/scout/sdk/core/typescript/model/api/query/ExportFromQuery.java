/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.query;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.query.AbstractQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.IExportFrom;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.spi.ExportFromSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class ExportFromQuery extends AbstractQuery<IExportFrom> {

  private final NodeModuleSpi m_moduleSpi;
  private String m_name;
  private INodeElement m_element;

  public ExportFromQuery(NodeModuleSpi module) {
    m_moduleSpi = Ensure.notNull(module);
  }

  /**
   * Limits the {@link IExportFrom} to the one with the given name.
   *
   * @param name
   *          The name to search. Default is not filtering on name.
   * @return this
   */
  public ExportFromQuery withName(String name) {
    m_name = name;
    return this;
  }

  public String name() {
    return m_name;
  }

  public ExportFromQuery withElement(INodeElement element) {
    m_element = element;
    return this;
  }

  public INodeElement element() {
    return m_element;
  }

  protected NodeModuleSpi module() {
    return m_moduleSpi;
  }

  protected boolean test(ExportFromSpi exportFrom) {
    var element = element();
    return element == null || element.spi() == exportFrom.referencedElement();
  }

  @Override
  protected Stream<IExportFrom> createStream() {
    return createStreamForName()
        .filter(this::test)
        .map(ExportFromSpi::api);
  }

  protected Stream<ExportFromSpi> createStreamForName() {
    var name = name();
    if (name == null) {
      return module().exports().values().stream();
    }
    return Stream.ofNullable(module().exports().get(name));
  }
}
