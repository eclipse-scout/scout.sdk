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
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class NodeElementQuery extends AbstractQuery<INodeElement> {

  private final NodeModuleSpi m_moduleSpi;
  private String m_exportName;
  private boolean m_recursive;

  public NodeElementQuery(NodeModuleSpi module) {
    m_moduleSpi = Ensure.notNull(module);
  }

  public NodeElementQuery withExportName(String exportName) {
    m_exportName = exportName;
    return this;
  }

  protected String exportName() {
    return m_exportName;
  }

  public NodeElementQuery withRecursive(boolean recursive) {
    m_recursive = recursive;
    return this;
  }

  protected boolean isRecursive() {
    return m_recursive;
  }

  protected NodeModuleSpi module() {
    return m_moduleSpi;
  }

  @Override
  protected Stream<INodeElement> createStream() {
    var exportName = exportName();
    Stream<NodeElementSpi> result;
    if (isRecursive()) {
      result = module().api()
          .packageJson()
          .dependencies()
          .withSelf(true)
          .withRecursive(true)
          .stream()
          .flatMap(module -> createStreamForExportName(module.spi(), exportName));
    }
    else {
      result = createStreamForExportName(module(), exportName);
    }
    return result.map(NodeElementSpi::api);
  }

  protected static Stream<NodeElementSpi> createStreamForExportName(NodeModuleSpi module, String exportName) {
    if (exportName == null) {
      return module.elements().keySet().stream();
    }
    return Stream.ofNullable(module.exports().get(exportName));
  }
}
