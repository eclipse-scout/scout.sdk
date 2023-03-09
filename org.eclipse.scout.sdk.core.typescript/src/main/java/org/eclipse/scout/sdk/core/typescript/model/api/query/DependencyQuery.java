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

import java.util.Collections;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.scout.sdk.core.model.query.AbstractQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.spliterator.PackageJsonDependencySpliterator;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class DependencyQuery extends AbstractQuery<INodeModule> {

  private final NodeModuleSpi m_rootModule;
  private String m_name;
  private boolean m_recursive;
  private boolean m_withSelf;

  public DependencyQuery(NodeModuleSpi start) {
    m_rootModule = Ensure.notNull(start);
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

  protected String name() {
    return m_name;
  }

  public DependencyQuery withSelf(boolean withSelf) {
    m_withSelf = withSelf;
    return this;
  }

  protected boolean isWithSelf() {
    return m_withSelf;
  }

  public DependencyQuery withRecursive(boolean recursive) {
    m_recursive = recursive;
    return this;
  }

  protected boolean isRecursive() {
    return m_recursive;
  }

  protected NodeModuleSpi module() {
    return m_rootModule;
  }

  @Override
  protected Stream<INodeModule> createStream() {
    var startModules = isWithSelf() ? Collections.singletonList(module()) : module().packageJson().dependencies();
    var stream = StreamSupport.stream(new PackageJsonDependencySpliterator(startModules, isRecursive(), isWithSelf()), false);
    var name = name();
    if (name != null) {
      stream = stream.filter(nodeModule -> name.equals(nodeModule.packageJson().api().name()));
    }
    return stream.map(NodeModuleSpi::api);
  }
}
