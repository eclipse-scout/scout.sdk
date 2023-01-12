/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.form;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;

import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.permission.PermissionGenerator;
import org.eclipse.scout.sdk.core.s.service.ServiceInterfaceGenerator;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutClientJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.UsernameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link FormGeneratorTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(UsernameExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutClientJavaEnvironmentFactory.class)
public class FormGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/s/generator/form/";

  @Test
  public void testFullFormStack(IJavaEnvironment env) {
    // permission
    var permGenerator = new PermissionGenerator<>()
        .withElementName("MyPermission")
        .withPackageName("org.eclipse.scout.sdk.core.s.test.shared");
    assertEqualsRefFile(env, REF_FILE_FOLDER + "FormTest1.txt", permGenerator);
    var createdPermission = assertNoCompileErrors(env, permGenerator);

    // formData
    var formDataGenerator = PrimaryTypeGenerator.create()
        .withElementName("MyFormData")
        .withPackageName("org.eclipse.scout.sdk.core.s.test.shared")
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractFormData().fqn());
    assertEqualsRefFile(env, REF_FILE_FOLDER + "FormTest2.txt", formDataGenerator);
    var createdFormData = assertNoCompileErrors(env, formDataGenerator);

    // service interface
    var svcIfcGenerator = new ServiceInterfaceGenerator<>()
        .withElementName("IMyFormService")
        .withPackageName("org.eclipse.scout.sdk.core.s.test.shared")
        .withMethod(MethodGenerator.create()
            .withElementName(FormGenerator.SERVICE_PREPARE_CREATE_METHOD_NAME)
            .withFlags(Flags.AccInterface)
            .withReturnType(createdFormData.name())
            .withParameter(MethodParameterGenerator.create()
                .withElementName("formData")
                .withDataType(createdFormData.name())))
        .withMethod(MethodGenerator.create()
            .withElementName(FormGenerator.SERVICE_CREATE_METHOD_NAME)
            .withFlags(Flags.AccInterface)
            .withReturnType(createdFormData.name())
            .withParameter(MethodParameterGenerator.create()
                .withElementName("formData")
                .withDataType(createdFormData.name())))
        .withMethod(MethodGenerator.create()
            .withElementName(FormGenerator.SERVICE_LOAD_METHOD_NAME)
            .withFlags(Flags.AccInterface)
            .withReturnType(createdFormData.name())
            .withParameter(MethodParameterGenerator.create()
                .withElementName("formData")
                .withDataType(createdFormData.name())))
        .withMethod(MethodGenerator.create()
            .withElementName(FormGenerator.SERVICE_STORE_METHOD_NAME)
            .withFlags(Flags.AccInterface)
            .withReturnType(createdFormData.name())
            .withParameter(MethodParameterGenerator.create()
                .withElementName("formData")
                .withDataType(createdFormData.name())));

    assertEqualsRefFile(env, REF_FILE_FOLDER + "FormTest3.txt", svcIfcGenerator);
    var createdSvcIfc = assertNoCompileErrors(env, svcIfcGenerator);

    // service Impl
    var svcImplGenerator = PrimaryTypeGenerator.create()
        .withElementName("MyFormService")
        .withPackageName("org.eclipse.scout.sdk.core.s.test.server")
        .withInterface(svcIfcGenerator.fullyQualifiedName())
        .withAllMethodsImplemented();
    assertEqualsRefFile(env, REF_FILE_FOLDER + "FormTest4.txt", svcImplGenerator);
    assertNoCompileErrors(env, svcImplGenerator);

    // form
    var formGenerator = new FormGenerator<>()
        .withElementName("MyForm")
        .withPackageName("org.eclipse.scout.sdk.core.s.test.client")
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractForm().fqn())
        .withFormData(createdFormData.name())
        .withServiceInterface(createdSvcIfc.name())
        .withPermissionUpdate(createdPermission.name())
        .withPermissionCreate(createdPermission.name());

    assertEqualsRefFile(env, REF_FILE_FOLDER + "FormTest5.txt", formGenerator);
    assertNoCompileErrors(env, formGenerator);
  }
}
