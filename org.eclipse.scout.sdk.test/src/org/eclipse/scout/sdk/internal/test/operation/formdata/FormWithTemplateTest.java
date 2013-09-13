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

public class FormWithTemplateTest extends AbstractSdkTestWithFormDataProject {

  @Test
  public void testCreateFormData() throws Exception {
    String formName = "UsingTemplateForm";
    IType form = TypeUtility.getType("formdata.client.ui.forms." + formName);
    Assert.assertTrue(TypeUtility.exists(form));

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataAnnotation annotation = ScoutTypeUtility.findFormDataAnnotation(form, TypeUtility.getSuperTypeHierarchy(form));

    FormDataUpdateOperation op = new FormDataUpdateOperation(form, TypeUtility.getTypeBySignature(annotation.getFormDataTypeSignature()).getCompilationUnit());
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, op);

    testApiOfUsingTemplateFormData();

  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfUsingTemplateFormData() throws Exception {
    // type UsingTemplateFormData
    IType usingTemplateFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.UsingTemplateFormData");
    SdkAssert.assertHasFlags(usingTemplateFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(usingTemplateFormData, "QAbstractFormData;");

    // fields of UsingTemplateFormData
    SdkAssert.assertEquals("field count of 'UsingTemplateFormData'", 1, usingTemplateFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(usingTemplateFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'UsingTemplateFormData'", 4, usingTemplateFormData.getMethods().length);
    IMethod usingTemplateFormData1 = SdkAssert.assertMethodExist(usingTemplateFormData, "UsingTemplateFormData", new String[]{});
    SdkAssert.assertTrue(usingTemplateFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usingTemplateFormData1, "V");
    IMethod getExternalGroupBox = SdkAssert.assertMethodExist(usingTemplateFormData, "getExternalGroupBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getExternalGroupBox, "QExternalGroupBox;");
    IMethod getInternalHtml = SdkAssert.assertMethodExist(usingTemplateFormData, "getInternalHtml", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getInternalHtml, "QInternalHtml;");
    IMethod getTestCheckbox = SdkAssert.assertMethodExist(usingTemplateFormData, "getTestCheckbox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTestCheckbox, "QTestCheckbox;");

    SdkAssert.assertEquals("inner types count of 'UsingTemplateFormData'", 3, usingTemplateFormData.getTypes().length);
    // type ExternalGroupBox
    IType externalGroupBox = SdkAssert.assertTypeExists(usingTemplateFormData, "ExternalGroupBox");
    SdkAssert.assertHasFlags(externalGroupBox, 9);
    SdkAssert.assertHasSuperTypeSignature(externalGroupBox, "QAbstractExternalGroupBoxData;");

    // fields of ExternalGroupBox
    SdkAssert.assertEquals("field count of 'ExternalGroupBox'", 1, externalGroupBox.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(externalGroupBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'ExternalGroupBox'", 1, externalGroupBox.getMethods().length);
    IMethod externalGroupBox1 = SdkAssert.assertMethodExist(externalGroupBox, "ExternalGroupBox", new String[]{});
    SdkAssert.assertTrue(externalGroupBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(externalGroupBox1, "V");

    SdkAssert.assertEquals("inner types count of 'ExternalGroupBox'", 0, externalGroupBox.getTypes().length);
    // type InternalHtml
    IType internalHtml = SdkAssert.assertTypeExists(usingTemplateFormData, "InternalHtml");
    SdkAssert.assertHasFlags(internalHtml, 9);
    SdkAssert.assertHasSuperTypeSignature(internalHtml, "QAbstractValueFieldData<QString;>;");

    // fields of InternalHtml
    SdkAssert.assertEquals("field count of 'InternalHtml'", 1, internalHtml.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(internalHtml, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'InternalHtml'", 2, internalHtml.getMethods().length);
    IMethod internalHtml1 = SdkAssert.assertMethodExist(internalHtml, "InternalHtml", new String[]{});
    SdkAssert.assertTrue(internalHtml1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(internalHtml1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(internalHtml, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, Integer.MAX_VALUE);"}, true);

    SdkAssert.assertEquals("inner types count of 'InternalHtml'", 0, internalHtml.getTypes().length);
    // type TestCheckbox
    IType testCheckbox = SdkAssert.assertTypeExists(usingTemplateFormData, "TestCheckbox");
    SdkAssert.assertHasFlags(testCheckbox, 9);
    SdkAssert.assertHasSuperTypeSignature(testCheckbox, "QAbstractTestCheckboxFieldData;");

    // fields of TestCheckbox
    SdkAssert.assertEquals("field count of 'TestCheckbox'", 1, testCheckbox.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(testCheckbox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'TestCheckbox'", 1, testCheckbox.getMethods().length);
    IMethod testCheckbox1 = SdkAssert.assertMethodExist(testCheckbox, "TestCheckbox", new String[]{});
    SdkAssert.assertTrue(testCheckbox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(testCheckbox1, "V");

    SdkAssert.assertEquals("inner types count of 'TestCheckbox'", 0, testCheckbox.getTypes().length);
  }

}
