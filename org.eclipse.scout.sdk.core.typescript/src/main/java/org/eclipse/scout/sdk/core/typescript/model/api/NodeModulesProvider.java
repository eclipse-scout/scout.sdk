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

/**
 * Base entry point to retrieve and remove {@link INodeModule} instances.
 */
public final class NodeModulesProvider {

  @SuppressWarnings("StaticCollection")
  private static final Map<Object, NodeModulesProviderSpi> providers = new HashMap<>();
  private static final EventListenerList listeners = new EventListenerList();

  private NodeModulesProvider() {
  }

  /**
   * Register a new provider for a given context.
   */
  public static synchronized void registerProvider(Object context, NodeModulesProviderSpi provider) {
    if (provider == null) {
      providers.remove(context);
    }
    else {
      providers.put(context, provider);
    }
  }

  /**
   * @return Gets a provider for a given context.
   */
  public static synchronized Optional<NodeModulesProviderSpi> getProvider(Object context) {
    return Optional.ofNullable(providers.get(context));
  }

  /**
   * Removes a registered provider.
   */
  public static void removeProvider(Object context) {
    registerProvider(context, null);
  }

  /**
   * Creates a new node module.
   * 
   * @param nodeModuleDir
   *          The directory of the node module. Must not be {@code null} and should point to a directory containing a
   *          package.json file.
   * @param context
   *          The context for which the module should be created. For this context a corresponding provider must have
   *          been registered beforehand (see {@link #registerProvider(Object, NodeModulesProviderSpi)}).
   * @return The created module if the provider exists and is capable to create a module for the given path.
   */
  public static Optional<NodeModuleSpi> createNodeModule(Path nodeModuleDir, Object context) {
    return getProvider(context).flatMap(p -> p.create(nodeModuleDir));
  }

  /**
   * Removes a node module and releases all associated resources. All registered listeners (see
   * {@link #addListener(INodeModulesRemovedListener)}) are informed about the removed module. Furthermore, all
   * registered providers are asked to remove the modules at the given path.
   * 
   * @param changedPath
   *          The path of the removed node module.
   * @return All modules actually removed.
   */
  public static synchronized Set<NodeModuleSpi> removeNodeModule(Path changedPath) {
    var removed = providers.values().stream()
        .flatMap(p -> p.remove(changedPath).stream())
        .collect(toUnmodifiableSet());
    triggerRemoved(removed);
    return removed;
  }

  /**
   * Removes all node modules and releases all associated resources. All registered listeners (see
   * {@link #addListener(INodeModulesRemovedListener)}) are informed about the removed module. Furthermore, all
   * registered providers are asked to clear their modules.
   */
  public static synchronized void clearNodeModules() {
    providers.values().forEach(NodeModulesProviderSpi::clear);
    triggerRemoved(null);
  }

  /**
   * Adds a listener which should be informed, when a node module is removed.
   */
  public static void addListener(@SuppressWarnings("TypeMayBeWeakened") INodeModulesRemovedListener listener) {
    if (listener == null) {
      return;
    }
    listeners.add(listener);
  }

  /**
   * Removes the listener
   */
  public static void removeListener(@SuppressWarnings("TypeMayBeWeakened") INodeModulesRemovedListener listener) {
    if (listener == null) {
      return;
    }
    listeners.remove(listener);
  }

  private static void triggerRemoved(Set<NodeModuleSpi> removed) {
    if (removed != null && removed.isEmpty()) {
      return;
    }
    listeners.get(INodeModulesRemovedListener.class)
        .forEach(listener -> listener.nodeModulesRemoved(removed));
  }

  public interface INodeModulesRemovedListener extends EventListener {
    /**
     * The given modules have been removed.
     * 
     * @param removed
     *          The {@link Set} of removed modules or {@code null} if all modules have been removed (clear).
     */
    void nodeModulesRemoved(Set<NodeModuleSpi> removed);
  }
}
