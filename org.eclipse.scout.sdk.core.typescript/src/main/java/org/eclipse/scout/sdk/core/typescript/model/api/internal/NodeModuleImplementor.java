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
import org.eclipse.scout.sdk.core.typescript.model.api.IExportFrom;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson;
import org.eclipse.scout.sdk.core.typescript.model.api.query.ExportFromQuery;
import org.eclipse.scout.sdk.core.typescript.model.spi.ExportFromSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class NodeModuleImplementor extends AbstractNodeElement<NodeModuleSpi> implements INodeModule {

  private final PackageJsonSpi m_packageJson;

  public NodeModuleImplementor(NodeModuleSpi spi, PackageJsonSpi packageJson) {
    super(spi);
    m_packageJson = Ensure.notNull(packageJson);
  }

  @Override
  public IPackageJson packageJson() {
    return m_packageJson.api();
  }

  @Override
  public ExportFromQuery exports() {
    return new ExportFromQuery(spi());
  }

  @Override
  public Optional<String> exportAlias() {
    return Optional.ofNullable(name());
  }

  @Override
  public Optional<IExportFrom> export(String name) {
    return Optional.ofNullable(spi().exports().get(name))
        .map(ExportFromSpi::api);
  }

  @Override
  public String name() {
    return packageJson().name();
  }

  @Override
  public String toString() {
    return packageJson().toString();
  }
}
