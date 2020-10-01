/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.apidef;

import org.eclipse.scout.sdk.core.util.apidef.ApiLevel;

@ApiLevel(11)
public interface Scout11Api extends IScoutApi, IScoutChartApi {
  ChartUiTextContributor CHART_UI_TEXT_CONTRIBUTOR = new ChartUiTextContributor();

  @Override
  default IScoutChartApi.ChartUiTextContributor ChartUiTextContributor() {
    return CHART_UI_TEXT_CONTRIBUTOR;
  }

  class ChartUiTextContributor implements IScoutChartApi.ChartUiTextContributor {
    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.chart.ui.html.ChartUiTextContributor";
    }
  }
}
