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
