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
package org.eclipse.scout.sdk.core.s.sourcebuilder.form;

import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.sourcebuilder.permission.PermissionSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.service.ServiceImplSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.service.ServiceInterfaceSourceBuilder;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Test;

/**
 * <h3>{@link FormSourceBuilderTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class FormSourceBuilderTest {
  @Test
  public void testFullFormStack() {
    IJavaEnvironment clientEnv = CoreScoutTestingUtils.createClientJavaEnvironment();

    // permission
    PermissionSourceBuilder permBuilder = new PermissionSourceBuilder("MyPermission", "org.eclispe.scout.sdk.core.s.test");
    permBuilder.setup();
    String source = CoreUtils.createJavaCode(permBuilder, clientEnv, "\n", null);
    IType createdPermission = CoreTestingUtils.assertNoCompileErrors(clientEnv, permBuilder.getPackageName(), permBuilder.getMainType().getElementName(), source);

    // formData
    String formDataName = "MyFormData";
    ICompilationUnitSourceBuilder formDataBuilder = new CompilationUnitSourceBuilder(formDataName + SuffixConstants.SUFFIX_STRING_java, "org.eclispe.scout.sdk.core.s.test");
    ITypeSourceBuilder formDataTypeBuilder = new TypeSourceBuilder(formDataName);
    formDataTypeBuilder.setFlags(Flags.AccPublic);
    formDataTypeBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractFormData));
    formDataBuilder.addType(formDataTypeBuilder);
    source = CoreUtils.createJavaCode(formDataBuilder, clientEnv, "\n", null);
    IType createdFormData = CoreTestingUtils.assertNoCompileErrors(clientEnv, formDataBuilder.getPackageName(), formDataBuilder.getMainType().getElementName(), source);

    // Service interface
    ServiceInterfaceSourceBuilder svcIfcBuilder = new ServiceInterfaceSourceBuilder("IMyFormService", "org.eclispe.scout.sdk.core.s.test");
    svcIfcBuilder.setDtoSignature(Signature.createTypeSignature(createdFormData.name()));
    svcIfcBuilder.setup();
    source = CoreUtils.createJavaCode(svcIfcBuilder, clientEnv, "\n", null);
    IType createdSvcIfc = CoreTestingUtils.assertNoCompileErrors(clientEnv, svcIfcBuilder.getPackageName(), svcIfcBuilder.getMainType().getElementName(), source);

    // Service Impl
    ServiceImplSourceBuilder svcImplBuilder = new ServiceImplSourceBuilder("MyFormService", "org.eclispe.scout.sdk.core.s.test", svcIfcBuilder.getMainType());
    svcImplBuilder.setReadPermissionSignature(Signature.createTypeSignature(createdPermission.name()));
    svcImplBuilder.setUpdatePermissionSignature(Signature.createTypeSignature(createdPermission.name()));
    svcImplBuilder.setup();
    source = CoreUtils.createJavaCode(svcImplBuilder, clientEnv, "\n", null);
    CoreTestingUtils.assertNoCompileErrors(clientEnv, svcImplBuilder.getPackageName(), svcImplBuilder.getMainType().getElementName(), source);

    // form
    FormSourceBuilder formBuilder = new FormSourceBuilder("MyForm", "org.eclispe.scout.sdk.core.s.test");
    String[] classIdValues = new String[FormSourceBuilder.NUM_CLASS_IDS];
    for (int i = 0; i < classIdValues.length; i++) {
      classIdValues[i] = "whatever";
    }
    formBuilder.setClassIdValues(classIdValues);
    formBuilder.setFormDataSignature(Signature.createTypeSignature(createdFormData.name()));
    formBuilder.setServiceIfcSignature(Signature.createTypeSignature(createdSvcIfc.name()));
    formBuilder.setSuperTypeSignature(Signature.createTypeSignature(IScoutRuntimeTypes.AbstractForm));
    formBuilder.setUpdatePermissionSignature(Signature.createTypeSignature(createdPermission.name()));

    formBuilder.setup();
    source = CoreUtils.createJavaCode(formBuilder, clientEnv, "\n", null);
    CoreTestingUtils.assertNoCompileErrors(clientEnv, formBuilder.getPackageName(), formBuilder.getMainType().getElementName(), source);
  }
}
