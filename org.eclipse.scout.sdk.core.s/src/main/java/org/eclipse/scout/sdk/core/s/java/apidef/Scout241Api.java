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

@MaxApiLevel({24, 1})
@SuppressWarnings({"squid:S2176", "squid:S00118", "squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // naming conventions
public interface Scout241Api extends IScoutApi, IScoutChartApi, IScout22DoApi {

  @Override
  default int[] supportedJavaVersions() {
    return new int[]{17};
  }

  IScoutAnnotationApi.Generated GENERATED = new Generated();

  @Override
  default IScoutAnnotationApi.Generated Generated() {
    return GENERATED;
  }

  class Generated extends Scout10Api.Generated {
    @Override
    public String fqn() {
      return "jakarta.annotation.Generated";
    }
  }

  IScoutVariousApi.WebServiceClient WEB_SERVICE_CLIENT = new WebServiceClient();

  @Override
  default IScoutVariousApi.WebServiceClient WebServiceClient() {
    return WEB_SERVICE_CLIENT;
  }

  class WebServiceClient extends Scout10Api.WebServiceClient {
    @Override
    public String fqn() {
      return "jakarta.xml.ws.WebServiceClient";
    }
  }

  IScoutVariousApi.WebService WEB_SERVICE = new WebService();

  @Override
  default IScoutVariousApi.WebService WebService() {
    return WEB_SERVICE;
  }

  class WebService extends Scout10Api.WebService {
    @Override
    public String fqn() {
      return "jakarta.jws.WebService";
    }
  }

  @Override
  default String getJaxBNamespace() {
    return "https://jakarta.ee/xml/ns/jaxb";
  }

  @Override
  default String getJaxBVersion() {
    return "3.0";
  }

  @Override
  default String getJaxWsNamespace() {
    return "https://jakarta.ee/xml/ns/jaxws";
  }

  IScoutVariousApi.UiTextContributor UI_TEXT_CONTRIBUTOR = new UiTextContributor();

  @Override
  default IScoutVariousApi.UiTextContributor UiTextContributor() {
    return UI_TEXT_CONTRIBUTOR;
  }

  class UiTextContributor extends Scout10Api.UiTextContributor {

    @Override
    public String contributeUiTextKeysMethodName() {
      return "contribute";
    }
  }
}
