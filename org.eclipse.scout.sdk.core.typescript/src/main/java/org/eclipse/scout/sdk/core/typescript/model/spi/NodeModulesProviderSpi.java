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

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.sdk.core.typescript.model.api.NodeModulesProvider;

/**
 * Service provider interface for classes capable to create a {@link NodeModuleSpi} based on a {@link Path} to a
 * directory.
 */
public interface NodeModulesProviderSpi {
  /**
   * Callback to create a new {@link NodeModuleSpi} for the given base path. Should not be called explicitly. Instead,
   * use {@link NodeModulesProvider#createNodeModule(Path, Object)}.
   * 
   * @param nodeModuleDir
   *          The directory of the node module. Must not be {@code null}.
   * @return The created node module or an empty optional if the directory does not point to a node module (does not
   *         contain a package.json file).
   */
  Optional<NodeModuleSpi> create(Path nodeModuleDir);

  /**
   * Called when a {@link NodeModuleSpi} is removed. Should not be called explicitly. Instead, use
   * {@link NodeModulesProvider#removeNodeModule(Path)}.
   * 
   * @param changedPath
   *          The path of the module removed. Must not be {@code null}.
   * @return The modules actually removed. Must not be {@code null}.
   */
  Set<NodeModuleSpi> remove(Path changedPath);

  /**
   * Called when all modules are removed. Should not be called explicitly. Instead, use
   * {@link NodeModulesProvider#clearNodeModules()}.
   */
  void clear();
}
