/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test.operation.formdata;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Assert;
import org.junit.Test;

public class ExternalGroupboxTest extends AbstractSdkTestWithFormDataProject {

  @Test
  public void testCreateFormData() throws Exception {
    String formName = "AbstractExternalGroupBox";
    IType form = TypeUtility.getType("formdata.client.ui.template.formfield." + formName);
    Assert.assertTrue(TypeUtility.exists(form));

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataAnnotation annotation = ScoutTypeUtility.findFormDataAnnotation(form, TypeUtility.getSuperTypeHierarchy(form));
    FormDataUpdateOperation op = new FormDataUpdateOperation(form, TypeUtility.getTypeBySignature(annotation.getFormDataTypeSignature()).getCompilationUnit());
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, op);

    testApiOfAbstractExternalGroupBoxData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfAbstractExternalGroupBoxData() throws Exception {
    // type AbstractExternalGroupBoxData
    IType abstractExternalGroupBoxData = SdkAssert.assertTypeExists("formdata.shared.services.process.AbstractExternalGroupBoxData");
    SdkAssert.assertHasFlags(abstractExternalGroupBoxData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractExternalGroupBoxData, "QAbstractFormFieldData;");

    // fields of AbstractExternalGroupBoxData
    SdkAssert.assertEquals("field count of 'AbstractExternalGroupBoxData'", 1, abstractExternalGroupBoxData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractExternalGroupBoxData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'AbstractExternalGroupBoxData'", 2, abstractExternalGroupBoxData.getMethods().length);
    IMethod abstractExternalGroupBoxData1 = SdkAssert.assertMethodExist(abstractExternalGroupBoxData, "AbstractExternalGroupBoxData", new String[]{});
    SdkAssert.assertTrue(abstractExternalGroupBoxData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractExternalGroupBoxData1, "V");
    IMethod getExternalString = SdkAssert.assertMethodExist(abstractExternalGroupBoxData, "getExternalString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getExternalString, "QExternalString;");

    SdkAssert.assertEquals("inner types count of 'AbstractExternalGroupBoxData'", 1, abstractExternalGroupBoxData.getTypes().length);
    // type ExternalString
    IType externalString = SdkAssert.assertTypeExists(abstractExternalGroupBoxData, "ExternalString");
    SdkAssert.assertHasFlags(externalString, 9);
    SdkAssert.assertHasSuperTypeSignature(externalString, "QAbstractValueFieldData<QString;>;");

    // fields of ExternalString
    SdkAssert.assertEquals("field count of 'ExternalString'", 1, externalString.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(externalString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'ExternalString'", 2, externalString.getMethods().length);
    IMethod externalString1 = SdkAssert.assertMethodExist(externalString, "ExternalString", new String[]{});
    SdkAssert.assertTrue(externalString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(externalString1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(externalString, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'ExternalString'", 0, externalString.getTypes().length);
  }
}
