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
package org.eclipse.scout.sdk.dto.test;

import org.eclipse.scout.sdk.core.model.IField;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.dto.test.util.CoreScoutTestingUtils;
import org.junit.Assert;
import org.junit.Test;

public class FormWithTemplateTest {

  @Test
  public void testCreateFormData() {
    String formName = "UsingTemplateForm";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms." + formName);
    testApiOfUsingTemplateFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfUsingTemplateFormData(IType usingTemplateFormData) {
    // type UsingTemplateFormData
    SdkAssert.assertHasFlags(usingTemplateFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(usingTemplateFormData, "QAbstractFormData;");

    // fields of UsingTemplateFormData
    Assert.assertEquals("field count of 'UsingTemplateFormData'", 1, usingTemplateFormData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(usingTemplateFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'UsingTemplateFormData'", 5, usingTemplateFormData.getMethods().size());
    IMethod usingTemplateFormData1 = SdkAssert.assertMethodExist(usingTemplateFormData, "UsingTemplateFormData", new String[]{});
    Assert.assertTrue(usingTemplateFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usingTemplateFormData1, null);
    IMethod getExternalGroupBox = SdkAssert.assertMethodExist(usingTemplateFormData, "getExternalGroupBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getExternalGroupBox, "QExternalGroupBox;");
    IMethod getInternalHtml = SdkAssert.assertMethodExist(usingTemplateFormData, "getInternalHtml", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getInternalHtml, "QInternalHtml;");
    IMethod getTestCheckbox = SdkAssert.assertMethodExist(usingTemplateFormData, "getTestCheckbox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTestCheckbox, "QTestCheckbox;");
    IMethod getTestLimitedString = SdkAssert.assertMethodExist(usingTemplateFormData, "getTestLimitedString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTestLimitedString, "QTestLimitedString;");

    Assert.assertEquals("inner types count of 'UsingTemplateFormData'", 4, usingTemplateFormData.getTypes().size());
    // type ExternalGroupBox
    IType externalGroupBox = SdkAssert.assertTypeExists(usingTemplateFormData, "ExternalGroupBox");
    SdkAssert.assertHasFlags(externalGroupBox, 9);
    SdkAssert.assertHasSuperTypeSignature(externalGroupBox, "QAbstractExternalGroupBoxData;");

    // fields of ExternalGroupBox
    Assert.assertEquals("field count of 'ExternalGroupBox'", 1, externalGroupBox.getFields().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(externalGroupBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'ExternalGroupBox'", 1, externalGroupBox.getMethods().size());
    IMethod externalGroupBox1 = SdkAssert.assertMethodExist(externalGroupBox, "ExternalGroupBox", new String[]{});
    Assert.assertTrue(externalGroupBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(externalGroupBox1, null);

    Assert.assertEquals("inner types count of 'ExternalGroupBox'", 0, externalGroupBox.getTypes().size());
    // type InternalHtml
    IType internalHtml = SdkAssert.assertTypeExists(usingTemplateFormData, "InternalHtml");
    SdkAssert.assertHasFlags(internalHtml, 9);
    SdkAssert.assertHasSuperTypeSignature(internalHtml, "QAbstractValueFieldData<QString;>;");

    // fields of InternalHtml
    Assert.assertEquals("field count of 'InternalHtml'", 1, internalHtml.getFields().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(internalHtml, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'InternalHtml'", 1, internalHtml.getMethods().size());
    IMethod internalHtml1 = SdkAssert.assertMethodExist(internalHtml, "InternalHtml", new String[]{});
    Assert.assertTrue(internalHtml1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(internalHtml1, null);

    Assert.assertEquals("inner types count of 'InternalHtml'", 0, internalHtml.getTypes().size());
    // type TestCheckbox
    IType testCheckbox = SdkAssert.assertTypeExists(usingTemplateFormData, "TestCheckbox");
    SdkAssert.assertHasFlags(testCheckbox, 9);
    SdkAssert.assertHasSuperTypeSignature(testCheckbox, "QAbstractTestCheckboxFieldData;");

    // fields of TestCheckbox
    Assert.assertEquals("field count of 'TestCheckbox'", 1, testCheckbox.getFields().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(testCheckbox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'TestCheckbox'", 1, testCheckbox.getMethods().size());
    IMethod testCheckbox1 = SdkAssert.assertMethodExist(testCheckbox, "TestCheckbox", new String[]{});
    Assert.assertTrue(testCheckbox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(testCheckbox1, null);

    Assert.assertEquals("inner types count of 'TestCheckbox'", 0, testCheckbox.getTypes().size());
    // type TestLimitedString
    IType testLimitedString = SdkAssert.assertTypeExists(usingTemplateFormData, "TestLimitedString");
    SdkAssert.assertHasFlags(testLimitedString, 9);
    SdkAssert.assertHasSuperTypeSignature(testLimitedString, "QAbstractValueFieldData<QString;>;");

    // fields of TestLimitedString
    Assert.assertEquals("field count of 'TestLimitedString'", 1, testLimitedString.getFields().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(testLimitedString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    Assert.assertEquals("method count of 'TestLimitedString'", 1, testLimitedString.getMethods().size());
    IMethod testLimitedString1 = SdkAssert.assertMethodExist(testLimitedString, "TestLimitedString", new String[]{});
    Assert.assertTrue(testLimitedString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(testLimitedString1, null);

    Assert.assertEquals("inner types count of 'TestLimitedString'", 0, testLimitedString.getTypes().size());
  }
}
