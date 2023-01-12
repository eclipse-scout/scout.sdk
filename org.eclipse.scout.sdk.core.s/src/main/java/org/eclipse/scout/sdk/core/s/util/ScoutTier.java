/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.util;

import java.util.Optional;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;

/**
 * <h3>{@link ScoutTier}</h3> Helper class to detect and convert scout tiers.
 *
 * @since 5.2.0
 */
@SuppressWarnings("squid:S00115")
public enum ScoutTier implements ITier<IScoutApi> {

  /**
   * Scout Client Tier
   */
  Client("client", api -> api.IClientSession().fqn()),

  /**
   * Scout Shared Tier
   */
  Shared("shared", api -> api.ISession().fqn()),

  /**
   * Scout Server Tier
   */
  Server("server", api -> api.IServerSession().fqn()),

  /**
   * Scout HTML UI Tier
   */
  HtmlUi("ui.html", api -> api.UiServlet().fqn());

  public static void initTierTree() {
    TierTree.addDependency(ScoutTier.HtmlUi, ScoutTier.Client);
    TierTree.addDependency(ScoutTier.Client, ScoutTier.Shared);
    TierTree.addDependency(ScoutTier.Server, ScoutTier.Shared);
  }

  private final String m_tierName;
  private final Function<IScoutApi, String> m_lookupFqnFunction;

  ScoutTier(String tierName, Function<IScoutApi, String> lookupFqnFunction) {
    m_tierName = tierName;
    m_lookupFqnFunction = lookupFqnFunction;
  }

  @Override
  public String tierName() {
    return m_tierName;
  }

  @Override
  public String getLookupFqn(IScoutApi api) {
    return Optional.ofNullable(api)
        .map(m_lookupFqnFunction)
        .orElse(null);
  }

  @Override
  public Class<IScoutApi> getApiClass() {
    return IScoutApi.class;
  }
}
