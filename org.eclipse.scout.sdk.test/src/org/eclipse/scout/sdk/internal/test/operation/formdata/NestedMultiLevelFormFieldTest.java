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
package org.eclipse.scout.sdk.internal.test.operation.formdata;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataDtoUpdateOperation;
import org.junit.Assert;
import org.junit.Test;

public class NestedMultiLevelFormFieldTest extends AbstractSdkTestWithFormDataProject {
  @Test
  public void runTests() throws Exception {
    checkAbstractMainBoxData();
    checkAbstractTemplateFieldData();
    checkFirstLevelFormData();
    checkSecondLevelFormData();
    checkThirdLevelFormData();
  }

  private void checkAbstractMainBoxData() throws Exception {
    IType t = SdkAssert.assertTypeExists("formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData");

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(t);
    executeBuildAssertNoCompileErrors(op);

    testApiOfAbstractMainBoxData();
  }

  private void checkAbstractTemplateFieldData() throws Exception {
    IType t = SdkAssert.assertTypeExists("formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData");

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(t);
    executeBuildAssertNoCompileErrors(op);

    testApiOfAbstractTemplateFieldData();
  }

  private void checkFirstLevelFormData() throws Exception {
    IType t = SdkAssert.assertTypeExists("formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData");

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(t);
    executeBuildAssertNoCompileErrors(op);

    testApiOfFirstLevelFormData();
  }

  private void checkSecondLevelFormData() throws Exception {
    IType t = SdkAssert.assertTypeExists("formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData");

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(t);
    executeBuildAssertNoCompileErrors(op);

    testApiOfSecondLevelFormData();
  }

  private void checkThirdLevelFormData() throws Exception {
    IType t = SdkAssert.assertTypeExists("formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData");

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(t);
    executeBuildAssertNoCompileErrors(op);

    testApiOfThirdLevelFormData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfAbstractMainBoxData() throws Exception {
    // type AbstractMainBoxData
    IType abstractMainBoxData = SdkAssert.assertTypeExists("formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData");
    SdkAssert.assertHasFlags(abstractMainBoxData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractMainBoxData, "QAbstractFormFieldData;");
    SdkAssert.assertAnnotation(abstractMainBoxData, "javax.annotation.Generated");

    // fields of AbstractMainBoxData
    SdkAssert.assertEquals("field count of 'AbstractMainBoxData'", 1, abstractMainBoxData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractMainBoxData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'AbstractMainBoxData'", 2, abstractMainBoxData.getMethods().length);
    IMethod abstractMainBoxData1 = SdkAssert.assertMethodExist(abstractMainBoxData, "AbstractMainBoxData", new String[]{});
    SdkAssert.assertTrue(abstractMainBoxData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractMainBoxData1, "V");
    IMethod getFirstLevel = SdkAssert.assertMethodExist(abstractMainBoxData, "getFirstLevel", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirstLevel, "QFirstLevel;");

    SdkAssert.assertEquals("inner types count of 'AbstractMainBoxData'", 1, abstractMainBoxData.getTypes().length);
    // type FirstLevel
    IType firstLevel = SdkAssert.assertTypeExists(abstractMainBoxData, "FirstLevel");
    SdkAssert.assertHasFlags(firstLevel, 9);
    SdkAssert.assertHasSuperTypeSignature(firstLevel, "QAbstractTemplateFieldData<QNumber;>;");

    // fields of FirstLevel
    SdkAssert.assertEquals("field count of 'FirstLevel'", 1, firstLevel.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(firstLevel, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'FirstLevel'", 1, firstLevel.getMethods().length);
    IMethod firstLevel1 = SdkAssert.assertMethodExist(firstLevel, "FirstLevel", new String[]{});
    SdkAssert.assertTrue(firstLevel1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(firstLevel1, "V");

    SdkAssert.assertEquals("inner types count of 'FirstLevel'", 0, firstLevel.getTypes().length);
  }

  private void testApiOfAbstractTemplateFieldData() throws Exception {
    // type AbstractTemplateFieldData
    IType abstractTemplateFieldData = SdkAssert.assertTypeExists("formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData");
    SdkAssert.assertHasFlags(abstractTemplateFieldData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractTemplateFieldData, "QAbstractValueFieldData<QList<QT;>;>;");
    SdkAssert.assertAnnotation(abstractTemplateFieldData, "javax.annotation.Generated");

    // fields of AbstractTemplateFieldData
    SdkAssert.assertEquals("field count of 'AbstractTemplateFieldData'", 1, abstractTemplateFieldData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractTemplateFieldData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'AbstractTemplateFieldData'", 0, abstractTemplateFieldData.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'AbstractTemplateFieldData'", 0, abstractTemplateFieldData.getTypes().length);
  }

  private void testApiOfFirstLevelFormData() throws Exception {
    // type FirstLevelFormData
    IType firstLevelFormData = SdkAssert.assertTypeExists("formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData");
    SdkAssert.assertHasFlags(firstLevelFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(firstLevelFormData, "QAbstractFormData;");
    SdkAssert.assertAnnotation(firstLevelFormData, "javax.annotation.Generated");

    // fields of FirstLevelFormData
    SdkAssert.assertEquals("field count of 'FirstLevelFormData'", 1, firstLevelFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(firstLevelFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'FirstLevelFormData'", 2, firstLevelFormData.getMethods().length);
    IMethod firstLevelFormData1 = SdkAssert.assertMethodExist(firstLevelFormData, "FirstLevelFormData", new String[]{});
    SdkAssert.assertTrue(firstLevelFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(firstLevelFormData1, "V");
    IMethod getFirstInnerBox = SdkAssert.assertMethodExist(firstLevelFormData, "getFirstInnerBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirstInnerBox, "QFirstInnerBox;");

    SdkAssert.assertEquals("inner types count of 'FirstLevelFormData'", 1, firstLevelFormData.getTypes().length);
    // type FirstInnerBox
    IType firstInnerBox = SdkAssert.assertTypeExists(firstLevelFormData, "FirstInnerBox");
    SdkAssert.assertHasFlags(firstInnerBox, 9);
    SdkAssert.assertHasSuperTypeSignature(firstInnerBox, "QAbstractMainBoxData;");

    // fields of FirstInnerBox
    SdkAssert.assertEquals("field count of 'FirstInnerBox'", 1, firstInnerBox.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(firstInnerBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'FirstInnerBox'", 1, firstInnerBox.getMethods().length);
    IMethod firstInnerBox1 = SdkAssert.assertMethodExist(firstInnerBox, "FirstInnerBox", new String[]{});
    SdkAssert.assertTrue(firstInnerBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(firstInnerBox1, "V");

    SdkAssert.assertEquals("inner types count of 'FirstInnerBox'", 0, firstInnerBox.getTypes().length);
  }

  private void testApiOfSecondLevelFormData() throws Exception {
    // type SecondLevelFormData
    IType secondLevelFormData = SdkAssert.assertTypeExists("formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData");
    SdkAssert.assertHasFlags(secondLevelFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(secondLevelFormData, "QFirstLevelFormData;");
    SdkAssert.assertAnnotation(secondLevelFormData, "javax.annotation.Generated");

    // fields of SecondLevelFormData
    SdkAssert.assertEquals("field count of 'SecondLevelFormData'", 1, secondLevelFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(secondLevelFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'SecondLevelFormData'", 2, secondLevelFormData.getMethods().length);
    IMethod secondLevelFormData1 = SdkAssert.assertMethodExist(secondLevelFormData, "SecondLevelFormData", new String[]{});
    SdkAssert.assertTrue(secondLevelFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(secondLevelFormData1, "V");
    IMethod getSecondInnerBox = SdkAssert.assertMethodExist(secondLevelFormData, "getSecondInnerBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecondInnerBox, "QSecondInnerBox;");

    SdkAssert.assertEquals("inner types count of 'SecondLevelFormData'", 1, secondLevelFormData.getTypes().length);
    // type SecondInnerBox
    IType secondInnerBox = SdkAssert.assertTypeExists(secondLevelFormData, "SecondInnerBox");
    SdkAssert.assertHasFlags(secondInnerBox, 9);
    SdkAssert.assertHasSuperTypeSignature(secondInnerBox, "QFirstInnerBox;");
    SdkAssert.assertAnnotation(secondInnerBox, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SecondInnerBox
    SdkAssert.assertEquals("field count of 'SecondInnerBox'", 1, secondInnerBox.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(secondInnerBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'SecondInnerBox'", 2, secondInnerBox.getMethods().length);
    IMethod secondInnerBox1 = SdkAssert.assertMethodExist(secondInnerBox, "SecondInnerBox", new String[]{});
    SdkAssert.assertTrue(secondInnerBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(secondInnerBox1, "V");
    IMethod getSecondLevel = SdkAssert.assertMethodExist(secondInnerBox, "getSecondLevel", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecondLevel, "QSecondLevel;");

    SdkAssert.assertEquals("inner types count of 'SecondInnerBox'", 1, secondInnerBox.getTypes().length);
    // type SecondLevel
    IType secondLevel = SdkAssert.assertTypeExists(secondInnerBox, "SecondLevel");
    SdkAssert.assertHasFlags(secondLevel, 9);
    SdkAssert.assertHasSuperTypeSignature(secondLevel, "QFirstLevel;");
    SdkAssert.assertAnnotation(secondLevel, "org.eclipse.scout.commons.annotations.Replace");

    // fields of SecondLevel
    SdkAssert.assertEquals("field count of 'SecondLevel'", 1, secondLevel.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(secondLevel, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'SecondLevel'", 1, secondLevel.getMethods().length);
    IMethod secondLevel1 = SdkAssert.assertMethodExist(secondLevel, "SecondLevel", new String[]{});
    SdkAssert.assertTrue(secondLevel1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(secondLevel1, "V");

    SdkAssert.assertEquals("inner types count of 'SecondLevel'", 0, secondLevel.getTypes().length);
  }

  private void testApiOfThirdLevelFormData() throws Exception {
    // type ThirdLevelFormData
    IType thirdLevelFormData = SdkAssert.assertTypeExists("formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData");
    SdkAssert.assertHasFlags(thirdLevelFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(thirdLevelFormData, "QSecondLevelFormData;");
    SdkAssert.assertAnnotation(thirdLevelFormData, "javax.annotation.Generated");

    // fields of ThirdLevelFormData
    SdkAssert.assertEquals("field count of 'ThirdLevelFormData'", 1, thirdLevelFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(thirdLevelFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'ThirdLevelFormData'", 2, thirdLevelFormData.getMethods().length);
    IMethod thirdLevelFormData1 = SdkAssert.assertMethodExist(thirdLevelFormData, "ThirdLevelFormData", new String[]{});
    SdkAssert.assertTrue(thirdLevelFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdLevelFormData1, "V");
    IMethod getThirdInnerBox = SdkAssert.assertMethodExist(thirdLevelFormData, "getThirdInnerBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdInnerBox, "QThirdInnerBox;");

    SdkAssert.assertEquals("inner types count of 'ThirdLevelFormData'", 1, thirdLevelFormData.getTypes().length);
    // type ThirdInnerBox
    IType thirdInnerBox = SdkAssert.assertTypeExists(thirdLevelFormData, "ThirdInnerBox");
    SdkAssert.assertHasFlags(thirdInnerBox, 9);
    SdkAssert.assertHasSuperTypeSignature(thirdInnerBox, "QSecondInnerBox;");
    SdkAssert.assertAnnotation(thirdInnerBox, "org.eclipse.scout.commons.annotations.Replace");

    // fields of ThirdInnerBox
    SdkAssert.assertEquals("field count of 'ThirdInnerBox'", 1, thirdInnerBox.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(thirdInnerBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'ThirdInnerBox'", 2, thirdInnerBox.getMethods().length);
    IMethod thirdInnerBox1 = SdkAssert.assertMethodExist(thirdInnerBox, "ThirdInnerBox", new String[]{});
    SdkAssert.assertTrue(thirdInnerBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdInnerBox1, "V");
    IMethod getThirdLevel = SdkAssert.assertMethodExist(thirdInnerBox, "getThirdLevel", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdLevel, "QThirdLevel;");

    SdkAssert.assertEquals("inner types count of 'ThirdInnerBox'", 1, thirdInnerBox.getTypes().length);
    // type ThirdLevel
    IType thirdLevel = SdkAssert.assertTypeExists(thirdInnerBox, "ThirdLevel");
    SdkAssert.assertHasFlags(thirdLevel, 9);
    SdkAssert.assertHasSuperTypeSignature(thirdLevel, "QSecondLevel;");
    SdkAssert.assertAnnotation(thirdLevel, "org.eclipse.scout.commons.annotations.Replace");

    // fields of ThirdLevel
    SdkAssert.assertEquals("field count of 'ThirdLevel'", 1, thirdLevel.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(thirdLevel, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'ThirdLevel'", 1, thirdLevel.getMethods().length);
    IMethod thirdLevel1 = SdkAssert.assertMethodExist(thirdLevel, "ThirdLevel", new String[]{});
    SdkAssert.assertTrue(thirdLevel1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdLevel1, "V");

    SdkAssert.assertEquals("inner types count of 'ThirdLevel'", 0, thirdLevel.getTypes().length);
  }
}
