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

import org.eclipse.scout.sdk.core.java.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.doc.OptionalApiSample.IJavaApi;

public final class GeneratorWithApiSample {

  private GeneratorWithApiSample() {
  }

  public static void generatorWithApi() {
    // tag::generatorWithApi[]
    TypeGenerator.create()
        .withSuperClassFrom(IJavaApi.class, IJavaApi::listClassName)
        .withMethod(MethodGenerator.create()
            .withElementNameFrom(IJavaApi.class, IJavaApi::getMethodName));
    // end::generatorWithApi[]
  }
}
