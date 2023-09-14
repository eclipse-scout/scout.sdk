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

/**
 * By default, this query returns all {@link INodeModule modules} which are declared in the "dependencies" attribute of
 * the package.json of a {@link INodeModule} (direct runtime dependencies).
 */
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
   *          The name to search. Default is not filtering by name.
   * @return This query.
   */
  public DependencyQuery withName(String name) {
    m_name = name;
    return this;
  }

  protected String name() {
    return m_name;
  }

  /**
   * Specifies if the start {@link INodeModule} itself should be part of the resulting dependencies or not. Default is
   * {@code false}.
   * 
   * @param withSelf
   *          {@code true} if the start module should be included.
   * @return This query.
   */
  public DependencyQuery withSelf(boolean withSelf) {
    m_withSelf = withSelf;
    return this;
  }

  protected boolean isWithSelf() {
    return m_withSelf;
  }

  /**
   * Specifies if only direct dependencies or all dependencies recursively should be returned. Default is {@code false}.
   * 
   * @param recursive
   *          {@code true} for recursive dependencies, {@code false} if only direct dependencies should be returned.
   * @return This query.
   */
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
