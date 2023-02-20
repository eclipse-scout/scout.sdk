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
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.PackageJsonSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class DependencyQuery extends AbstractQuery<INodeModule> {

  private final PackageJsonSpi m_packageJsonSpi;
  private String m_name;

  public DependencyQuery(PackageJsonSpi packageJson) {
    m_packageJsonSpi = Ensure.notNull(packageJson);
  }

  /**
   * Limits the {@link INodeModule} to the one with the given name.
   *
   * @param name
   *          The name to search. Default is not filtering on name.
   * @return this
   */
  public DependencyQuery withName(String name) {
    m_name = name;
    return this;
  }

  public String name() {
    return m_name;
  }

  protected PackageJsonSpi packageJson() {
    return m_packageJsonSpi;
  }

  @Override
  protected Stream<INodeModule> createStream() {
    var name = name();
    var stream = packageJson().dependencies().stream().map(NodeModuleSpi::api);
    if (name != null) {
      return stream.filter(nodeModule -> name.equals(nodeModule.name()));
    }
    return stream;
  }
}
