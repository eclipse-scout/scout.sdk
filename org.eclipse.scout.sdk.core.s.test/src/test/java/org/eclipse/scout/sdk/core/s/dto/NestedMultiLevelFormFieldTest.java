/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.s.util.DtoUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

public class NestedMultiLevelFormFieldTest {
  @Test
  public void runTests() {
    checkAbstractMainBoxData();
    checkAbstractTemplateFieldData();
    checkFirstLevelFormData();
    checkSecondLevelFormData();
    checkThirdLevelFormData();
  }

  private static void checkAbstractMainBoxData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.AbstractMainBox");
    testApiOfAbstractMainBoxData(dto);
  }

  private static void checkAbstractTemplateFieldData() {
    IType modelType = CoreScoutTestingUtils.createClientJavaEnvironment().findType("formdata.client.ui.template.formfield.replace.levels.AbstractTemplateField");
    FormDataAnnotationDescriptor formDataAnnotation = DtoUtils.getFormDataAnnotationDescriptor(modelType);

    IJavaEnvironment sharedLookupEnvironment = CoreScoutTestingUtils.createSharedJavaEnvironment();
    ICompilationUnitSourceBuilder cuSrc = DtoUtils.createFormDataBuilder(modelType, formDataAnnotation, sharedLookupEnvironment);
    String source = DtoUtils.createJavaCode(cuSrc, sharedLookupEnvironment, "\n", null);
    Assert.assertNull(source); // the formdata annotation of AbstractTemplateField is set to 'use'. therefore nothing should be generated.

    IType dto = CoreScoutTestingUtils.createClientJavaEnvironment().findType("formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData");
    Assert.assertNotNull(dto);
    testApiOfAbstractTemplateFieldData(dto);
  }

  private static void checkFirstLevelFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.FirstLevelForm");
    testApiOfFirstLevelFormData(dto);
  }

  private static void checkSecondLevelFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.SecondLevelForm");
    testApiOfSecondLevelFormData(dto);
  }

  private static void checkThirdLevelFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.ThirdLevelForm");
    testApiOfThirdLevelFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractMainBoxData(IType abstractMainBoxData) {
    // type AbstractMainBoxData
    SdkAssert.assertHasFlags(abstractMainBoxData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractMainBoxData, "QAbstractFormFieldData;");
    SdkAssert.assertAnnotation(abstractMainBoxData, "javax.annotation.Generated");

    // fields of AbstractMainBoxData
    Assert.assertEquals("field count of 'AbstractMainBoxData'", 1, abstractMainBoxData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractMainBoxData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'AbstractMainBoxData'", 1, abstractMainBoxData.methods().list().size());
    IMethod getFirstLevel = SdkAssert.assertMethodExist(abstractMainBoxData, "getFirstLevel", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirstLevel, "QFirstLevel;");

    Assert.assertEquals("inner types count of 'AbstractMainBoxData'", 1, abstractMainBoxData.innerTypes().list().size());
    // type FirstLevel
    IType firstLevel = SdkAssert.assertTypeExists(abstractMainBoxData, "FirstLevel");
    SdkAssert.assertHasFlags(firstLevel, 9);
    SdkAssert.assertHasSuperTypeSignature(firstLevel, "QAbstractTemplateFieldData<QNumber;>;");

    // fields of FirstLevel
    Assert.assertEquals("field count of 'FirstLevel'", 1, firstLevel.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(firstLevel, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'FirstLevel'", 0, firstLevel.methods().list().size());

    Assert.assertEquals("inner types count of 'FirstLevel'", 0, firstLevel.innerTypes().list().size());
  }

  private static void testApiOfAbstractTemplateFieldData(IType abstractTemplateFieldData) {
    // type AbstractTemplateFieldData
    SdkAssert.assertHasFlags(abstractTemplateFieldData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractTemplateFieldData, "QAbstractValueFieldData<QList<QT;>;>;");
    SdkAssert.assertAnnotation(abstractTemplateFieldData, "javax.annotation.Generated");

    // fields of AbstractTemplateFieldData
    Assert.assertEquals("field count of 'AbstractTemplateFieldData'", 1, abstractTemplateFieldData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractTemplateFieldData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'AbstractTemplateFieldData'", 0 /* no constructor*/, abstractTemplateFieldData.methods().list().size());

    Assert.assertEquals("inner types count of 'AbstractTemplateFieldData'", 0, abstractTemplateFieldData.innerTypes().list().size());
  }

  private static void testApiOfFirstLevelFormData(IType firstLevelFormData) {
    // type FirstLevelFormData
    SdkAssert.assertHasFlags(firstLevelFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(firstLevelFormData, "QAbstractFormData;");
    SdkAssert.assertAnnotation(firstLevelFormData, "javax.annotation.Generated");

    // fields of FirstLevelFormData
    Assert.assertEquals("field count of 'FirstLevelFormData'", 1, firstLevelFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(firstLevelFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'FirstLevelFormData'", 1, firstLevelFormData.methods().list().size());
    IMethod getFirstInnerBox = SdkAssert.assertMethodExist(firstLevelFormData, "getFirstInnerBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirstInnerBox, "QFirstInnerBox;");

    Assert.assertEquals("inner types count of 'FirstLevelFormData'", 1, firstLevelFormData.innerTypes().list().size());
    // type FirstInnerBox
    IType firstInnerBox = SdkAssert.assertTypeExists(firstLevelFormData, "FirstInnerBox");
    SdkAssert.assertHasFlags(firstInnerBox, 9);
    SdkAssert.assertHasSuperTypeSignature(firstInnerBox, "QAbstractMainBoxData;");

    // fields of FirstInnerBox
    Assert.assertEquals("field count of 'FirstInnerBox'", 1, firstInnerBox.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(firstInnerBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'FirstInnerBox'", 0, firstInnerBox.methods().list().size());

    Assert.assertEquals("inner types count of 'FirstInnerBox'", 0, firstInnerBox.innerTypes().list().size());
  }

  private static void testApiOfSecondLevelFormData(IType secondLevelFormData) {
    // type SecondLevelFormData
    SdkAssert.assertHasFlags(secondLevelFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(secondLevelFormData, "QFirstLevelFormData;");
    SdkAssert.assertAnnotation(secondLevelFormData, "javax.annotation.Generated");

    // fields of SecondLevelFormData
    Assert.assertEquals("field count of 'SecondLevelFormData'", 1, secondLevelFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(secondLevelFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'SecondLevelFormData'", 1, secondLevelFormData.methods().list().size());
    IMethod getSecondInnerBox = SdkAssert.assertMethodExist(secondLevelFormData, "getSecondInnerBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecondInnerBox, "QSecondInnerBox;");

    Assert.assertEquals("inner types count of 'SecondLevelFormData'", 1, secondLevelFormData.innerTypes().list().size());
    // type SecondInnerBox
    IType secondInnerBox = SdkAssert.assertTypeExists(secondLevelFormData, "SecondInnerBox");
    SdkAssert.assertHasFlags(secondInnerBox, 9);
    SdkAssert.assertHasSuperTypeSignature(secondInnerBox, "QFirstInnerBox;");
    SdkAssert.assertAnnotation(secondInnerBox, "org.eclipse.scout.rt.platform.Replace");

    // fields of SecondInnerBox
    Assert.assertEquals("field count of 'SecondInnerBox'", 1, secondInnerBox.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(secondInnerBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'SecondInnerBox'", 1, secondInnerBox.methods().list().size());
    IMethod getSecondLevel = SdkAssert.assertMethodExist(secondInnerBox, "getSecondLevel", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecondLevel, "QSecondLevel;");

    Assert.assertEquals("inner types count of 'SecondInnerBox'", 1, secondInnerBox.innerTypes().list().size());
    // type SecondLevel
    IType secondLevel = SdkAssert.assertTypeExists(secondInnerBox, "SecondLevel");
    SdkAssert.assertHasFlags(secondLevel, 9);
    SdkAssert.assertHasSuperTypeSignature(secondLevel, "QFirstLevel;");
    SdkAssert.assertAnnotation(secondLevel, "org.eclipse.scout.rt.platform.Replace");

    // fields of SecondLevel
    Assert.assertEquals("field count of 'SecondLevel'", 1, secondLevel.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(secondLevel, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'SecondLevel'", 0, secondLevel.methods().list().size());

    Assert.assertEquals("inner types count of 'SecondLevel'", 0, secondLevel.innerTypes().list().size());
  }

  private static void testApiOfThirdLevelFormData(IType thirdLevelFormData) {
    // type ThirdLevelFormData
    SdkAssert.assertHasFlags(thirdLevelFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(thirdLevelFormData, "QSecondLevelFormData;");
    SdkAssert.assertAnnotation(thirdLevelFormData, "javax.annotation.Generated");

    // fields of ThirdLevelFormData
    Assert.assertEquals("field count of 'ThirdLevelFormData'", 1, thirdLevelFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(thirdLevelFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'ThirdLevelFormData'", 1, thirdLevelFormData.methods().list().size());
    IMethod getThirdInnerBox = SdkAssert.assertMethodExist(thirdLevelFormData, "getThirdInnerBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdInnerBox, "QThirdInnerBox;");

    Assert.assertEquals("inner types count of 'ThirdLevelFormData'", 1, thirdLevelFormData.innerTypes().list().size());
    // type ThirdInnerBox
    IType thirdInnerBox = SdkAssert.assertTypeExists(thirdLevelFormData, "ThirdInnerBox");
    SdkAssert.assertHasFlags(thirdInnerBox, 9);
    SdkAssert.assertHasSuperTypeSignature(thirdInnerBox, "QSecondInnerBox;");
    SdkAssert.assertAnnotation(thirdInnerBox, "org.eclipse.scout.rt.platform.Replace");

    // fields of ThirdInnerBox
    Assert.assertEquals("field count of 'ThirdInnerBox'", 1, thirdInnerBox.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(thirdInnerBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'ThirdInnerBox'", 1, thirdInnerBox.methods().list().size());
    IMethod getThirdLevel = SdkAssert.assertMethodExist(thirdInnerBox, "getThirdLevel", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdLevel, "QThirdLevel;");

    Assert.assertEquals("inner types count of 'ThirdInnerBox'", 1, thirdInnerBox.innerTypes().list().size());
    // type ThirdLevel
    IType thirdLevel = SdkAssert.assertTypeExists(thirdInnerBox, "ThirdLevel");
    SdkAssert.assertHasFlags(thirdLevel, 9);
    SdkAssert.assertHasSuperTypeSignature(thirdLevel, "QSecondLevel;");
    SdkAssert.assertAnnotation(thirdLevel, "org.eclipse.scout.rt.platform.Replace");

    // fields of ThirdLevel
    Assert.assertEquals("field count of 'ThirdLevel'", 1, thirdLevel.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(thirdLevel, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'ThirdLevel'", 0, thirdLevel.methods().list().size());

    Assert.assertEquals("inner types count of 'ThirdLevel'", 0, thirdLevel.innerTypes().list().size());
  }
}
