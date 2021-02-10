package org.eclipse.scout.sdk.doc;

import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.doc.OptionalApiSample.IJavaApi;

public final class GeneratorWithApiSample {

  private GeneratorWithApiSample() {
  }

  public static void generatorWithApi(IJavaEnvironment javaEnvironment) {
    // tag::generatorWithApi[]
    TypeGenerator.create()
        .withSuperClassFrom(IJavaApi.class, IJavaApi::listClassName)
        .withMethod(MethodGenerator.create()
            .withElementNameFrom(IJavaApi.class, IJavaApi::getMethodName));
    // end::generatorWithApi[]
  }
}
