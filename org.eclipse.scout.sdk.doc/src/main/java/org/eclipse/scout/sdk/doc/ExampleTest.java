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

import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.TypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.RunningJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.SdkAssertions;
import org.eclipse.scout.sdk.core.testing.apidef.ApiRequirement;
import org.eclipse.scout.sdk.core.testing.apidef.EnabledFor;
import org.eclipse.scout.sdk.core.testing.context.DefaultCommentGeneratorExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.doc.OptionalApiSample.IJavaApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

// tag::exampleTest[]
@ExtendWith(DefaultCommentGeneratorExtension.class)
@ExtendWithJavaEnvironmentFactory(RunningJavaEnvironmentFactory.class)
public class ExampleTest {
  @Test
  @EnabledFor(api = IJavaApi.class, require = ApiRequirement.MIN, version = 11) // <1>
  public void testExample(IJavaEnvironment javaEnvironment) {
    var typeParamName = "T";
    var generator = PrimaryTypeGenerator.create()
        .withPackageName("org.eclipse.scout.sdk.doc")
        .asPublic()
        .asInterface()
        .withElementName("TestInterface")
        .withTypeParameter(TypeParameterGenerator.create()
            .withElementName(typeParamName)
            .withBinding(CharSequence.class.getName()))
        .withMethod(MethodGenerator.create()
            .withReturnType(typeParamName)
            .withElementName("sequence"));
    SdkAssertions.assertNoCompileErrors(javaEnvironment, generator); // <2>
  }
}
// end::exampleTest[]
