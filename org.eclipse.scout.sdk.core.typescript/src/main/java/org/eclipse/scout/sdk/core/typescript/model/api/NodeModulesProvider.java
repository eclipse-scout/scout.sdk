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

import static java.util.stream.Collectors.toUnmodifiableSet;

import java.nio.file.Path;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModulesProviderSpi;
import org.eclipse.scout.sdk.core.util.EventListenerList;

public final class NodeModulesProvider {

  @SuppressWarnings("StaticCollection")
  private static final Map<Object, NodeModulesProviderSpi> providers = new HashMap<>();
  private static final EventListenerList listeners = new EventListenerList();

  private NodeModulesProvider() {
  }

  public static synchronized void registerProvider(Object context, NodeModulesProviderSpi provider) {
    if (provider == null) {
      providers.remove(context);
    }
    else {
      providers.put(context, provider);
    }
  }

  public static synchronized Optional<NodeModulesProviderSpi> getProvider(Object context) {
    return Optional.ofNullable(providers.get(context));
  }

  public static void removeProvider(Object context) {
    registerProvider(context, null);
  }

  public static Optional<NodeModuleSpi> createNodeModule(Path nodeModuleDir, Object context) {
    return getProvider(context).flatMap(p -> p.create(nodeModuleDir));
  }

  public static synchronized Set<NodeModuleSpi> removeNodeModule(Path changedPath) {
    var removed = providers.values().stream()
        .flatMap(p -> p.remove(changedPath).stream())
        .collect(toUnmodifiableSet());
    triggerRemoved(removed);
    return removed;
  }

  public static synchronized void clearNodeModules() {
    providers.values().forEach(NodeModulesProviderSpi::clear);
    triggerRemoved(null);
  }

  public static void addListener(@SuppressWarnings("TypeMayBeWeakened") INodeModulesRemovedListener listener) {
    if (listener == null) {
      return;
    }
    listeners.add(listener);
  }

  public static void removeListener(@SuppressWarnings("TypeMayBeWeakened") INodeModulesRemovedListener listener) {
    if (listener == null) {
      return;
    }
    listeners.remove(listener);
  }

  static void triggerRemoved(Set<NodeModuleSpi> removed) {
    listeners.get(INodeModulesRemovedListener.class)
        .forEach(listener -> listener.nodeModulesRemoved(removed));
  }

  public interface INodeModulesRemovedListener extends EventListener {
    void nodeModulesRemoved(Set<NodeModuleSpi> removed);
  }
}
