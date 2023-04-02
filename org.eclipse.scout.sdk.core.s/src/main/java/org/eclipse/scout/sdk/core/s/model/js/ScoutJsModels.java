/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.NodeModulesProvider;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;

public final class ScoutJsModels {

  @SuppressWarnings("StaticCollection")
  private static final Map<INodeModule, Optional<ScoutJsModel>> scoutJsModels = new ConcurrentHashMap<>();

  static {
    // discard cached scout models as soon as underlying node-module is removed
    NodeModulesProvider.addListener(ScoutJsModels::onNodeModulesRemoved);
  }

  private ScoutJsModels() {
  }

  static void onNodeModulesRemoved(Iterable<NodeModuleSpi> removed) {
    if (removed == null) {
      // clear
      scoutJsModels.clear();
      return;
    }
    removed.forEach(removedModule -> scoutJsModels.keySet().removeIf(m -> m.spi() == removedModule));
  }

  public static Optional<ScoutJsModel> create(Path nodeModuleDir, Object context) {
    return NodeModulesProvider.createNodeModule(nodeModuleDir, context)
        .map(NodeModuleSpi::api)
        .flatMap(ScoutJsModels::create);
  }

  public static Optional<ScoutJsModel> create(INodeModule module) {
    return create(module, null);
  }

  static Optional<ScoutJsModel> create(INodeModule module, IES6Class widgetClass) {
    return Optional.ofNullable(module).flatMap(m -> getOrCreate(m, widgetClass));
  }

  private static Optional<ScoutJsModel> getOrCreate(INodeModule module, IES6Class widgetClass) {
    return scoutJsModels.computeIfAbsent(module, m -> Optional.ofNullable(widgetClass)
        .or(() -> findWidgetClassInDependencies(m))
        .map(widget -> new ScoutJsModel(m, widget)));
  }

  private static Optional<IES6Class> findWidgetClassInDependencies(INodeModule start) {
    return Optional.ofNullable(start)
        .flatMap(ScoutJsModels::findScoutJsCoreModule)
        .flatMap(m -> m.export(ScoutJsCoreConstants.CLASS_NAME_WIDGET))
        .map(IES6Class.class::cast);
  }

  private static Optional<INodeModule> findScoutJsCoreModule(INodeModule start) {
    return start.packageJson().dependencies()
        .withSelf(true)
        .withRecursive(true)
        .withName(ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME)
        .first();
  }
}
