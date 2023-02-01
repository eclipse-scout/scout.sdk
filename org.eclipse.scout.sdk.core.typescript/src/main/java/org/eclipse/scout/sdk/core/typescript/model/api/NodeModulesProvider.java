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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;
import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModulesProviderSpi;

public final class NodeModulesProvider {

  @SuppressWarnings("StaticCollection")
  private static final Map<Object, NodeModulesProviderSpi> providers = new HashMap<>();

  private NodeModulesProvider() {
  }

  public static synchronized void register(Object context, NodeModulesProviderSpi provider) {
    if (provider == null) {
      providers.remove(context);
    }
    else {
      providers.put(context, provider);
    }
  }

  public static Optional<NodeModuleSpi> create(Path nodeModuleDir, Object context) {
    return getProvider(context).flatMap(p -> p.create(nodeModuleDir));
  }

  public static synchronized Optional<NodeModulesProviderSpi> getProvider(Object context) {
    return Optional.ofNullable(providers.get(context));
  }

  public static synchronized void remove(Object context) {
    register(context, null);
  }
}
