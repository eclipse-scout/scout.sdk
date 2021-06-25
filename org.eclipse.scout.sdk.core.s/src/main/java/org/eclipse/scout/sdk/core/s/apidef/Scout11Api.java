/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.apidef;

import org.eclipse.scout.sdk.core.apidef.MaxApiLevel;

@MaxApiLevel(11)
@SuppressWarnings({"squid:S2176", "squid:S00118", "squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // naming conventions
public interface Scout11Api extends IScoutApi, IScoutChartApi {
  IScoutChartApi.ChartUiTextContributor CHART_UI_TEXT_CONTRIBUTOR = new ChartUiTextContributor();

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

  IScoutVariousApi.JaxWsConstants JAX_WS_CONSTANTS = new JaxWsConstants();

  @Override
  default IScoutVariousApi.JaxWsConstants JaxWsConstants() {
    return JAX_WS_CONSTANTS;
  }

  class JaxWsConstants implements IScoutVariousApi.JaxWsConstants {
    @Override
    public String mavenPluginGroupId() {
      return "com.sun.xml.ws";
    }

    @Override
    public String codeModelFactoryPath() {
      return "org/glassfish/jaxb/codemodel/2.3.3/codemodel-2.3.3.jar";
    }

    @Override
    public String servletFactoryPath() {
      return "jakarta/servlet/jakarta.servlet-api/4.0.4/jakarta.servlet-api-4.0.4.jar";
    }

    @Override
    public String slf4jFactoryPath() {
      return "org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar";
    }

    @Override
    public String jwsFactoryPath() {
      return "jakarta/jws/jakarta.jws-api/2.1.0/jakarta.jws-api-2.1.0.jar";
    }
  }
}
