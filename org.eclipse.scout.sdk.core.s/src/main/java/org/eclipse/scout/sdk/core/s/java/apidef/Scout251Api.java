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

import org.eclipse.scout.sdk.core.java.apidef.MaxApiLevel;

@MaxApiLevel({25, 1})
@SuppressWarnings({"squid:S2176", "squid:S00118", "squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // naming conventions
public interface Scout251Api extends IScoutApi, IScout242Api, IScoutChartApi, IScout22DoApi {

  @Override
  default String ecjVersion() {
    return "3.40.0";
  }

  @Override
  default int[] supportedJavaVersions() {
    return new int[]{21};
  }

  IScoutVariousApi.JaxWsConstants JAX_WS_CONSTANTS = new Scout251Api.JaxWsConstants();

  @Override
  default IScoutVariousApi.JaxWsConstants JaxWsConstants() {
    return JAX_WS_CONSTANTS;
  }

  class JaxWsConstants extends Scout11Api.JaxWsConstants {

    @Override
    public String codeModelFactoryPath() {
      return "org/glassfish/jaxb/codemodel/4.0.5/codemodel-4.0.5.jar";
    }

    @Override
    public String servletFactoryPath() {
      return "jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar";
    }

    @Override
    public String slf4jFactoryPath() {
      return "org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar";
    }

    @Override
    public String jwsFactoryPath() {
      return null; // not necessary anymore since 25.1
    }

    @Override
    public String wsApiPath() {
      return "jakarta/xml/ws/jakarta.xml.ws-api/4.0.2/jakarta.xml.ws-api-4.0.2.jar";
    }

    @Override
    public String annotationApiPath() {
      return "jakarta/annotation/jakarta.annotation-api/2.1.1/jakarta.annotation-api-2.1.1.jar";
    }
  }
}
