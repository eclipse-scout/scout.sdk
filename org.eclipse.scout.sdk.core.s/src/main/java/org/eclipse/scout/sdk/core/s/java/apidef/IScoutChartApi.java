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

import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;

@SuppressWarnings({"squid:S00100", "squid:S2166", "squid:S2176", "squid:S00118", "findbugs:NM_METHOD_NAMING_CONVENTION"}) // method naming conventions
public interface IScoutChartApi extends IApiSpecification {

  ChartUiTextContributor ChartUiTextContributor();

  interface ChartUiTextContributor extends ITypeNameSupplier {
  }
}
