/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.parser.JavaParser;
import org.eclipse.scout.sdk.core.s.dto.sourcebuilder.form.FormDataAnnotation;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.core.testing.TestingUtils;
import org.eclipse.scout.sdk.dto.test.util.CoreScoutTestingUtils;
import org.junit.Assert;
import org.junit.Test;

public class NestedMultiLevelFormFieldTest {
  @Test
  public void runTests() throws Exception {
    checkAbstractMainBoxData();
    checkAbstractTemplateFieldData();
    checkFirstLevelFormData();
    checkSecondLevelFormData();
    checkThirdLevelFormData();
  }

  private void checkAbstractMainBoxData() throws Exception {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.AbstractMainBox");
    testApiOfAbstractMainBoxData(dto);
  }

  private void checkAbstractTemplateFieldData() throws Exception {
    IType modelType = TestingUtils.getType("formdata.client.ui.template.formfield.replace.levels.AbstractTemplateField", CoreScoutTestingUtils.SOURCE_FOLDERS);
    FormDataAnnotation formDataAnnotation = DtoUtils.findFormDataAnnotation(modelType);

    ILookupEnvironment lookupEnvironment = JavaParser.create(CoreScoutTestingUtils.getSharedClasspath(), false);
    StringBuilder sourceBuilder = DtoUtils.createFormDataSource(modelType, formDataAnnotation, lookupEnvironment, "\n", null);
    Assert.assertNull(sourceBuilder); // the formdata annotation of AbstractTemplateField is set to 'use'. therefore nothing should be generated.

    IType dto = TestingUtils.getType("formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData", CoreScoutTestingUtils.SOURCE_FOLDERS);
    Assert.assertNotNull(dto);
    testApiOfAbstractTemplateFieldData(dto);
  }

  private void checkFirstLevelFormData() throws Exception {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.FirstLevelForm");
    testApiOfFirstLevelFormData(dto);
  }

  private void checkSecondLevelFormData() throws Exception {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.SecondLevelForm");
    testApiOfSecondLevelFormData(dto);
  }

  private void checkThirdLevelFormData() throws Exception {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.ThirdLevelForm");
    testApiOfThirdLevelFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfAbstractMainBoxData(IType abstractMainBoxData) throws Exception {
    // type AbstractMainBoxData
    SdkAssert.assertHasFlags(abstractMainBoxData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractMainBoxData, "QAbstractFormFieldData;");
    SdkAssert.assertAnnotation(abstractMainBoxData, "javax.annotation.Generated");

    // fields of AbstractMainBoxData
    SdkAssert.assertEquals("field count of 'AbstractMainBoxData'", 1, abstractMainBoxData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractMainBoxData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'AbstractMainBoxData'", 2, abstractMainBoxData.getMethods().size());
    IMethod abstractMainBoxData1 = SdkAssert.assertMethodExist(abstractMainBoxData, "AbstractMainBoxData", new String[]{});
    SdkAssert.assertTrue(abstractMainBoxData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractMainBoxData1, null);
    IMethod getFirstLevel = SdkAssert.assertMethodExist(abstractMainBoxData, "getFirstLevel", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirstLevel, "QFirstLevel;");

    SdkAssert.assertEquals("inner types count of 'AbstractMainBoxData'", 1, abstractMainBoxData.getTypes().size());
    // type FirstLevel
    IType firstLevel = SdkAssert.assertTypeExists(abstractMainBoxData, "FirstLevel");
    SdkAssert.assertHasFlags(firstLevel, 9);
    SdkAssert.assertHasSuperTypeSignature(firstLevel, "QAbstractTemplateFieldData<QNumber;>;");

    // fields of FirstLevel
    SdkAssert.assertEquals("field count of 'FirstLevel'", 1, firstLevel.getFields().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(firstLevel, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'FirstLevel'", 1, firstLevel.getMethods().size());
    IMethod firstLevel1 = SdkAssert.assertMethodExist(firstLevel, "FirstLevel", new String[]{});
    SdkAssert.assertTrue(firstLevel1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(firstLevel1, null);

    SdkAssert.assertEquals("inner types count of 'FirstLevel'", 0, firstLevel.getTypes().size());
  }

  private void testApiOfAbstractTemplateFieldData(IType abstractTemplateFieldData) throws Exception {
    // type AbstractTemplateFieldData
    SdkAssert.assertHasFlags(abstractTemplateFieldData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractTemplateFieldData, "QAbstractValueFieldData<QList<QT;>;>;");
    SdkAssert.assertAnnotation(abstractTemplateFieldData, "javax.annotation.Generated");

    // fields of AbstractTemplateFieldData
    SdkAssert.assertEquals("field count of 'AbstractTemplateFieldData'", 1, abstractTemplateFieldData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractTemplateFieldData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'AbstractTemplateFieldData'", 1 /*constructor*/, abstractTemplateFieldData.getMethods().size());

    SdkAssert.assertEquals("inner types count of 'AbstractTemplateFieldData'", 0, abstractTemplateFieldData.getTypes().size());
  }

  private void testApiOfFirstLevelFormData(IType firstLevelFormData) throws Exception {
    // type FirstLevelFormData
    SdkAssert.assertHasFlags(firstLevelFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(firstLevelFormData, "QAbstractFormData;");
    SdkAssert.assertAnnotation(firstLevelFormData, "javax.annotation.Generated");

    // fields of FirstLevelFormData
    SdkAssert.assertEquals("field count of 'FirstLevelFormData'", 1, firstLevelFormData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(firstLevelFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'FirstLevelFormData'", 2, firstLevelFormData.getMethods().size());
    IMethod firstLevelFormData1 = SdkAssert.assertMethodExist(firstLevelFormData, "FirstLevelFormData", new String[]{});
    SdkAssert.assertTrue(firstLevelFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(firstLevelFormData1, null);
    IMethod getFirstInnerBox = SdkAssert.assertMethodExist(firstLevelFormData, "getFirstInnerBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirstInnerBox, "QFirstInnerBox;");

    SdkAssert.assertEquals("inner types count of 'FirstLevelFormData'", 1, firstLevelFormData.getTypes().size());
    // type FirstInnerBox
    IType firstInnerBox = SdkAssert.assertTypeExists(firstLevelFormData, "FirstInnerBox");
    SdkAssert.assertHasFlags(firstInnerBox, 9);
    SdkAssert.assertHasSuperTypeSignature(firstInnerBox, "QAbstractMainBoxData;");

    // fields of FirstInnerBox
    SdkAssert.assertEquals("field count of 'FirstInnerBox'", 1, firstInnerBox.getFields().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(firstInnerBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'FirstInnerBox'", 1, firstInnerBox.getMethods().size());
    IMethod firstInnerBox1 = SdkAssert.assertMethodExist(firstInnerBox, "FirstInnerBox", new String[]{});
    SdkAssert.assertTrue(firstInnerBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(firstInnerBox1, null);

    SdkAssert.assertEquals("inner types count of 'FirstInnerBox'", 0, firstInnerBox.getTypes().size());
  }

  private void testApiOfSecondLevelFormData(IType secondLevelFormData) throws Exception {
    // type SecondLevelFormData
    SdkAssert.assertHasFlags(secondLevelFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(secondLevelFormData, "QFirstLevelFormData;");
    SdkAssert.assertAnnotation(secondLevelFormData, "javax.annotation.Generated");

    // fields of SecondLevelFormData
    SdkAssert.assertEquals("field count of 'SecondLevelFormData'", 1, secondLevelFormData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(secondLevelFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'SecondLevelFormData'", 2, secondLevelFormData.getMethods().size());
    IMethod secondLevelFormData1 = SdkAssert.assertMethodExist(secondLevelFormData, "SecondLevelFormData", new String[]{});
    SdkAssert.assertTrue(secondLevelFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(secondLevelFormData1, null);
    IMethod getSecondInnerBox = SdkAssert.assertMethodExist(secondLevelFormData, "getSecondInnerBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecondInnerBox, "QSecondInnerBox;");

    SdkAssert.assertEquals("inner types count of 'SecondLevelFormData'", 1, secondLevelFormData.getTypes().size());
    // type SecondInnerBox
    IType secondInnerBox = SdkAssert.assertTypeExists(secondLevelFormData, "SecondInnerBox");
    SdkAssert.assertHasFlags(secondInnerBox, 9);
    SdkAssert.assertHasSuperTypeSignature(secondInnerBox, "QFirstInnerBox;");
    SdkAssert.assertAnnotation(secondInnerBox, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SecondInnerBox
    SdkAssert.assertEquals("field count of 'SecondInnerBox'", 1, secondInnerBox.getFields().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(secondInnerBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'SecondInnerBox'", 2, secondInnerBox.getMethods().size());
    IMethod secondInnerBox1 = SdkAssert.assertMethodExist(secondInnerBox, "SecondInnerBox", new String[]{});
    SdkAssert.assertTrue(secondInnerBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(secondInnerBox1, null);
    IMethod getSecondLevel = SdkAssert.assertMethodExist(secondInnerBox, "getSecondLevel", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecondLevel, "QSecondLevel;");

    SdkAssert.assertEquals("inner types count of 'SecondInnerBox'", 1, secondInnerBox.getTypes().size());
    // type SecondLevel
    IType secondLevel = SdkAssert.assertTypeExists(secondInnerBox, "SecondLevel");
    SdkAssert.assertHasFlags(secondLevel, 9);
    SdkAssert.assertHasSuperTypeSignature(secondLevel, "QFirstLevel;");
    SdkAssert.assertAnnotation(secondLevel, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SecondLevel
    SdkAssert.assertEquals("field count of 'SecondLevel'", 1, secondLevel.getFields().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(secondLevel, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'SecondLevel'", 1, secondLevel.getMethods().size());
    IMethod secondLevel1 = SdkAssert.assertMethodExist(secondLevel, "SecondLevel", new String[]{});
    SdkAssert.assertTrue(secondLevel1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(secondLevel1, null);

    SdkAssert.assertEquals("inner types count of 'SecondLevel'", 0, secondLevel.getTypes().size());
  }

  private void testApiOfThirdLevelFormData(IType thirdLevelFormData) throws Exception {
    // type ThirdLevelFormData
    SdkAssert.assertHasFlags(thirdLevelFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(thirdLevelFormData, "QSecondLevelFormData;");
    SdkAssert.assertAnnotation(thirdLevelFormData, "javax.annotation.Generated");

    // fields of ThirdLevelFormData
    SdkAssert.assertEquals("field count of 'ThirdLevelFormData'", 1, thirdLevelFormData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(thirdLevelFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'ThirdLevelFormData'", 2, thirdLevelFormData.getMethods().size());
    IMethod thirdLevelFormData1 = SdkAssert.assertMethodExist(thirdLevelFormData, "ThirdLevelFormData", new String[]{});
    SdkAssert.assertTrue(thirdLevelFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdLevelFormData1, null);
    IMethod getThirdInnerBox = SdkAssert.assertMethodExist(thirdLevelFormData, "getThirdInnerBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdInnerBox, "QThirdInnerBox;");

    SdkAssert.assertEquals("inner types count of 'ThirdLevelFormData'", 1, thirdLevelFormData.getTypes().size());
    // type ThirdInnerBox
    IType thirdInnerBox = SdkAssert.assertTypeExists(thirdLevelFormData, "ThirdInnerBox");
    SdkAssert.assertHasFlags(thirdInnerBox, 9);
    SdkAssert.assertHasSuperTypeSignature(thirdInnerBox, "QSecondInnerBox;");
    SdkAssert.assertAnnotation(thirdInnerBox, "org.eclipse.scout.commons.annotations.Replace");

    // fields of ThirdInnerBox
    SdkAssert.assertEquals("field count of 'ThirdInnerBox'", 1, thirdInnerBox.getFields().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(thirdInnerBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'ThirdInnerBox'", 2, thirdInnerBox.getMethods().size());
    IMethod thirdInnerBox1 = SdkAssert.assertMethodExist(thirdInnerBox, "ThirdInnerBox", new String[]{});
    SdkAssert.assertTrue(thirdInnerBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdInnerBox1, null);
    IMethod getThirdLevel = SdkAssert.assertMethodExist(thirdInnerBox, "getThirdLevel", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdLevel, "QThirdLevel;");

    SdkAssert.assertEquals("inner types count of 'ThirdInnerBox'", 1, thirdInnerBox.getTypes().size());
    // type ThirdLevel
    IType thirdLevel = SdkAssert.assertTypeExists(thirdInnerBox, "ThirdLevel");
    SdkAssert.assertHasFlags(thirdLevel, 9);
    SdkAssert.assertHasSuperTypeSignature(thirdLevel, "QSecondLevel;");
    SdkAssert.assertAnnotation(thirdLevel, "org.eclipse.scout.commons.annotations.Replace");

    // fields of ThirdLevel
    SdkAssert.assertEquals("field count of 'ThirdLevel'", 1, thirdLevel.getFields().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(thirdLevel, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'ThirdLevel'", 1, thirdLevel.getMethods().size());
    IMethod thirdLevel1 = SdkAssert.assertMethodExist(thirdLevel, "ThirdLevel", new String[]{});
    SdkAssert.assertTrue(thirdLevel1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdLevel1, null);

    SdkAssert.assertEquals("inner types count of 'ThirdLevel'", 0, thirdLevel.getTypes().size());
  }
}
