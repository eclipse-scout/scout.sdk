/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.apidef;

import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.IChainedApiComputationAbortSupport;
import org.eclipse.scout.sdk.core.java.apidef.MaxApiLevel;

@MaxApiLevel({26, 2})
@SuppressWarnings({"squid:S2176", "squid:S00118", "squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // naming conventions
public interface Scout262Api extends IScoutApi, IScout242Api, IScoutChartApi, IScout22DoApi, IChainedApiComputationAbortSupport {
  @Override
  default String ecjVersion() {
    return "3.45.0";
  }

  @Override
  default boolean abortChainedApiComputation(Class<? extends IApiSpecification> apiDefinition) {
    // do not continue chain here as session api is removed with this version
    return IScoutSessionApi.class.equals(apiDefinition);
  }
}
