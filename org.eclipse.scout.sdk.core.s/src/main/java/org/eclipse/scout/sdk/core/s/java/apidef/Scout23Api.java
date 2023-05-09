/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.apidef;

import org.eclipse.scout.sdk.core.java.apidef.MaxApiLevel;

@MaxApiLevel({23, 2})
@SuppressWarnings({"squid:S2176", "squid:S00118", "squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // naming conventions
public interface Scout23Api extends IScoutApi, IScoutChartApi, IScout22DoApi {

  IScoutVariousApi.ScoutTextProviderService SCOUT_TEXT_PROVIDER_SERVICE = new Scout23Api.ScoutTextProviderService();

  @Override
  default IScoutVariousApi.ScoutTextProviderService ScoutTextProviderService() {
    return SCOUT_TEXT_PROVIDER_SERVICE;
  }

  class ScoutTextProviderService extends Scout10Api.ScoutTextProviderService {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.nls.text.ScoutTextProviderService";
    }
  }
}
