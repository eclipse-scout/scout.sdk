/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.form;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;

import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.permission.PermissionGenerator;
import org.eclipse.scout.sdk.core.s.service.ServiceInterfaceGenerator;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutClientJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.UsernameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link FormGeneratorTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(UsernameExtension.class)
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutClientJavaEnvironmentFactory.class)
public class FormGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/s/generator/form/";

  @Test
  public void testFullFormStack(IJavaEnvironment env) {
    // permission
    PermissionGenerator<?> permGenerator = new PermissionGenerator<>()
        .withElementName("MyPermission")
        .withPackageName("org.eclipse.scout.sdk.core.s.test.shared");
    assertEqualsRefFile(env, REF_FILE_FOLDER + "FormTest1.txt", permGenerator);
    IType createdPermission = assertNoCompileErrors(env, permGenerator);

    // formData
    PrimaryTypeGenerator<?> formDataGenerator = PrimaryTypeGenerator.create()
        .withElementName("MyFormData")
        .withPackageName("org.eclipse.scout.sdk.core.s.test.shared")
        .withSuperClass(IScoutRuntimeTypes.AbstractFormData);
    assertEqualsRefFile(env, REF_FILE_FOLDER + "FormTest2.txt", formDataGenerator);
    IType createdFormData = assertNoCompileErrors(env, formDataGenerator);

    // service interface
    ServiceInterfaceGenerator<?> svcIfcGenerator = new ServiceInterfaceGenerator<>()
        .withElementName("IMyFormService")
        .withPackageName("org.eclipse.scout.sdk.core.s.test.shared")
        .withMethod(MethodGenerator.create()
            .withElementName(FormGenerator.SERVICE_PREPARECREATE_METHOD_NAME)
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
    IType createdSvcIfc = assertNoCompileErrors(env, svcIfcGenerator);

    // service Impl
    PrimaryTypeGenerator<?> svcImplGenerator = PrimaryTypeGenerator.create()
        .withElementName("MyFormService")
        .withPackageName("org.eclipse.scout.sdk.core.s.test.server")
        .withInterface(svcIfcGenerator.fullyQualifiedName())
        .withAllMethodsImplemented();
    assertEqualsRefFile(env, REF_FILE_FOLDER + "FormTest4.txt", svcImplGenerator);
    assertNoCompileErrors(env, svcImplGenerator);

    // form
    String[] classIdValues = new String[FormGenerator.NUM_CLASS_IDS];
    for (int i = 0; i < classIdValues.length; i++) {
      classIdValues[i] = "whatever" + i;
    }
    FormGenerator<?> formGenerator = new FormGenerator<>()
        .withElementName("MyForm")
        .withPackageName("org.eclipse.scout.sdk.core.s.test.client")
        .withSuperClass(IScoutRuntimeTypes.AbstractForm)
        .withClassIdValues(classIdValues)
        .withFormData(createdFormData.name())
        .withServiceInterface(createdSvcIfc.name())
        .withPermissionUpdate(createdPermission.name())
        .withPermissionCreate(createdPermission.name());

    assertEqualsRefFile(env, REF_FILE_FOLDER + "FormTest5.txt", formGenerator);
    assertNoCompileErrors(env, formGenerator);
  }
}