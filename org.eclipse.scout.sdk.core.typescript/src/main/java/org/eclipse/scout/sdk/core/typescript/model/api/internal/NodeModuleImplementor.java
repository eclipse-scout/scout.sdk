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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElementFactory;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.IPackageJson;
import org.eclipse.scout.sdk.core.typescript.model.api.query.NodeElementQuery;
import org.eclipse.scout.sdk.core.typescript.model.spi.ES6ClassSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeElementSpi;
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
  public NodeElementQuery elements() {
    return new NodeElementQuery(spi());
  }

  @Override
  public Stream<IES6Class> classes() {
    return spi().classes().stream().map(ES6ClassSpi::api);
  }

  @Override
  public Optional<INodeElement> export(String name) {
    return Optional.ofNullable(spi().exports().get(name))
        .map(NodeElementSpi::api);
  }

  @Override
  public Set<String> moduleExportNames() {
    return Collections.singleton(name());
  }

  @Override
  public String name() {
    return packageJson().name();
  }

  @Override
  public INodeElementFactory nodeElementFactory() {
    return spi().nodeElementFactory().api();
  }

  @Override
  public String toString() {
    return packageJson().toString();
  }
}
