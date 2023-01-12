/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.doc;

import org.eclipse.scout.sdk.core.apidef.Api;
import org.eclipse.scout.sdk.core.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

@SuppressWarnings("MethodMayBeStatic")
public class OptionalApiSample {

  // tag::optionalApiSample[]
  public void optionalApi(IJavaEnvironment javaEnvironment) {
    var javaApi = Api.create(IJavaApi.class, javaEnvironment).orElseThrow();
    javaApi.api(IHttpClientApi.class).ifPresent(this::withHttpClient);
  }

  protected void withHttpClient(IHttpClientApi api) { // <1>
    var name = api.httpClientClassName();
    SdkLog.warning(name);
  }
  // end::optionalApiSample[]

  public interface IJavaApi extends IApiSpecification {
    String listClassName();

    String getMethodName();
  }

  public interface IHttpClientApi extends IApiSpecification {
    String httpClientClassName();
  }
}
