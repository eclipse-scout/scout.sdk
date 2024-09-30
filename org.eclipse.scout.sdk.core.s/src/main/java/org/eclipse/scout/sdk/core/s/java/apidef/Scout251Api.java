/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.apidef;

import org.eclipse.scout.sdk.core.java.apidef.MaxApiLevel;

@MaxApiLevel({25, 1})
@SuppressWarnings({"squid:S2176", "squid:S00118", "squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // naming conventions
public interface Scout251Api extends IScoutApi, IScout242Api, IScoutChartApi, IScout22DoApi {
  @Override
  default int[] supportedJavaVersions() {
    return new int[]{21};
  }
}
