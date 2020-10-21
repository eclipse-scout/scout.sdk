/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper;
import org.junit.jupiter.api.Test;

import formdata.client.ui.template.formfield.replace.levels.AbstractMainBox;
import formdata.client.ui.template.formfield.replace.levels.AbstractTemplateField;
import formdata.client.ui.template.formfield.replace.levels.FirstLevelForm;
import formdata.client.ui.template.formfield.replace.levels.SecondLevelForm;
import formdata.client.ui.template.formfield.replace.levels.ThirdLevelForm;
import formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData;

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
    createFormDataAssertNoCompileErrors(AbstractMainBox.class.getName(), NestedMultiLevelFormFieldTest::testApiOfAbstractMainBoxData);
  }

  private static void checkAbstractTemplateFieldData() {
    ScoutFixtureHelper.runWithSharedAndClientEnv((shared, client) -> {
      var modelType = client.requireType(AbstractTemplateField.class.getName());
      assertFalse(DtoGeneratorFactory.createFormDataGenerator(modelType, shared).isPresent()); // must be empty because it is SdkCommand.USE

      var dto = client.requireType(AbstractTemplateFieldData.class.getName());
      testApiOfAbstractTemplateFieldData(dto);
      return null;
    });
  }

  private static void checkFirstLevelFormData() {
    createFormDataAssertNoCompileErrors(FirstLevelForm.class.getName(), NestedMultiLevelFormFieldTest::testApiOfFirstLevelFormData);
  }

  private static void checkSecondLevelFormData() {
    createFormDataAssertNoCompileErrors(SecondLevelForm.class.getName(), NestedMultiLevelFormFieldTest::testApiOfSecondLevelFormData);
  }

  private static void checkThirdLevelFormData() {
    createFormDataAssertNoCompileErrors(ThirdLevelForm.class.getName(), NestedMultiLevelFormFieldTest::testApiOfThirdLevelFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractMainBoxData(IType abstractMainBoxData) {
    // type AbstractMainBoxData
    assertHasFlags(abstractMainBoxData, 1025);
    assertHasSuperClass(abstractMainBoxData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData");
    assertAnnotation(abstractMainBoxData, "javax.annotation.Generated");

    // fields of AbstractMainBoxData
    assertEquals(1, abstractMainBoxData.fields().stream().count(), "field count of 'AbstractMainBoxData'");
    var serialVersionUID = assertFieldExist(abstractMainBoxData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, abstractMainBoxData.methods().stream().count(), "method count of 'AbstractMainBoxData'");
    var getFirstLevel = assertMethodExist(abstractMainBoxData, "getFirstLevel", new String[]{});
    assertMethodReturnType(getFirstLevel, "formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData$FirstLevel");

    assertEquals(1, abstractMainBoxData.innerTypes().stream().count(), "inner types count of 'AbstractMainBoxData'");
    // type FirstLevel
    var firstLevel = assertTypeExists(abstractMainBoxData, "FirstLevel");
    assertHasFlags(firstLevel, 9);
    assertHasSuperClass(firstLevel, "formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData<java.lang.Number>");

    // fields of FirstLevel
    assertEquals(1, firstLevel.fields().stream().count(), "field count of 'FirstLevel'");
    var serialVersionUID1 = assertFieldExist(firstLevel, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, firstLevel.methods().stream().count(), "method count of 'FirstLevel'");

    assertEquals(0, firstLevel.innerTypes().stream().count(), "inner types count of 'FirstLevel'");
  }

  private static void testApiOfAbstractTemplateFieldData(IType abstractTemplateFieldData) {
    // type AbstractTemplateFieldData
    assertHasFlags(abstractTemplateFieldData, 1025);
    assertHasSuperClass(abstractTemplateFieldData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.util.List<T>>");
    assertAnnotation(abstractTemplateFieldData, "javax.annotation.Generated");

    // fields of AbstractTemplateFieldData
    assertEquals(1, abstractTemplateFieldData.fields().stream().count(), "field count of 'AbstractTemplateFieldData'");
    var serialVersionUID = assertFieldExist(abstractTemplateFieldData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(0 /* no constructor*/, abstractTemplateFieldData.methods().stream().count(), "method count of 'AbstractTemplateFieldData'");

    assertEquals(0, abstractTemplateFieldData.innerTypes().stream().count(), "inner types count of 'AbstractTemplateFieldData'");
  }

  private static void testApiOfFirstLevelFormData(IType firstLevelFormData) {
    // type FirstLevelFormData
    assertHasFlags(firstLevelFormData, 1);
    assertHasSuperClass(firstLevelFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");
    assertAnnotation(firstLevelFormData, "javax.annotation.Generated");

    // fields of FirstLevelFormData
    assertEquals(1, firstLevelFormData.fields().stream().count(), "field count of 'FirstLevelFormData'");
    var serialVersionUID = assertFieldExist(firstLevelFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, firstLevelFormData.methods().stream().count(), "method count of 'FirstLevelFormData'");
    var getFirstInnerBox = assertMethodExist(firstLevelFormData, "getFirstInnerBox", new String[]{});
    assertMethodReturnType(getFirstInnerBox, "formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData$FirstInnerBox");

    assertEquals(1, firstLevelFormData.innerTypes().stream().count(), "inner types count of 'FirstLevelFormData'");
    // type FirstInnerBox
    var firstInnerBox = assertTypeExists(firstLevelFormData, "FirstInnerBox");
    assertHasFlags(firstInnerBox, 9);
    assertHasSuperClass(firstInnerBox, "formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData");

    // fields of FirstInnerBox
    assertEquals(1, firstInnerBox.fields().stream().count(), "field count of 'FirstInnerBox'");
    var serialVersionUID1 = assertFieldExist(firstInnerBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, firstInnerBox.methods().stream().count(), "method count of 'FirstInnerBox'");

    assertEquals(0, firstInnerBox.innerTypes().stream().count(), "inner types count of 'FirstInnerBox'");
  }

  private static void testApiOfSecondLevelFormData(IType secondLevelFormData) {
    // type SecondLevelFormData
    assertHasFlags(secondLevelFormData, 1);
    assertHasSuperClass(secondLevelFormData, "formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData");
    assertAnnotation(secondLevelFormData, "javax.annotation.Generated");

    // fields of SecondLevelFormData
    assertEquals(1, secondLevelFormData.fields().stream().count(), "field count of 'SecondLevelFormData'");
    var serialVersionUID = assertFieldExist(secondLevelFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, secondLevelFormData.methods().stream().count(), "method count of 'SecondLevelFormData'");
    var getSecondInnerBox = assertMethodExist(secondLevelFormData, "getSecondInnerBox", new String[]{});
    assertMethodReturnType(getSecondInnerBox, "formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox");

    assertEquals(1, secondLevelFormData.innerTypes().stream().count(), "inner types count of 'SecondLevelFormData'");
    // type SecondInnerBox
    var secondInnerBox = assertTypeExists(secondLevelFormData, "SecondInnerBox");
    assertHasFlags(secondInnerBox, 9);
    assertHasSuperClass(secondInnerBox, "formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData$FirstInnerBox");
    assertAnnotation(secondInnerBox, "org.eclipse.scout.rt.platform.Replace");

    // fields of SecondInnerBox
    assertEquals(1, secondInnerBox.fields().stream().count(), "field count of 'SecondInnerBox'");
    var serialVersionUID1 = assertFieldExist(secondInnerBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(1, secondInnerBox.methods().stream().count(), "method count of 'SecondInnerBox'");
    var getSecondLevel = assertMethodExist(secondInnerBox, "getSecondLevel", new String[]{});
    assertMethodReturnType(getSecondLevel, "formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox$SecondLevel");

    assertEquals(1, secondInnerBox.innerTypes().stream().count(), "inner types count of 'SecondInnerBox'");
    // type SecondLevel
    var secondLevel = assertTypeExists(secondInnerBox, "SecondLevel");
    assertHasFlags(secondLevel, 9);
    assertHasSuperClass(secondLevel, "formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData$FirstLevel");
    assertAnnotation(secondLevel, "org.eclipse.scout.rt.platform.Replace");

    // fields of SecondLevel
    assertEquals(1, secondLevel.fields().stream().count(), "field count of 'SecondLevel'");
    var serialVersionUID2 = assertFieldExist(secondLevel, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, secondLevel.methods().stream().count(), "method count of 'SecondLevel'");

    assertEquals(0, secondLevel.innerTypes().stream().count(), "inner types count of 'SecondLevel'");
  }

  private static void testApiOfThirdLevelFormData(IType thirdLevelFormData) {
    // type ThirdLevelFormData
    assertHasFlags(thirdLevelFormData, 1);
    assertHasSuperClass(thirdLevelFormData, "formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData");
    assertAnnotation(thirdLevelFormData, "javax.annotation.Generated");

    // fields of ThirdLevelFormData
    assertEquals(1, thirdLevelFormData.fields().stream().count(), "field count of 'ThirdLevelFormData'");
    var serialVersionUID = assertFieldExist(thirdLevelFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, thirdLevelFormData.methods().stream().count(), "method count of 'ThirdLevelFormData'");
    var getThirdInnerBox = assertMethodExist(thirdLevelFormData, "getThirdInnerBox", new String[]{});
    assertMethodReturnType(getThirdInnerBox, "formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData$ThirdInnerBox");

    assertEquals(1, thirdLevelFormData.innerTypes().stream().count(), "inner types count of 'ThirdLevelFormData'");
    // type ThirdInnerBox
    var thirdInnerBox = assertTypeExists(thirdLevelFormData, "ThirdInnerBox");
    assertHasFlags(thirdInnerBox, 9);
    assertHasSuperClass(thirdInnerBox, "formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox");
    assertAnnotation(thirdInnerBox, "org.eclipse.scout.rt.platform.Replace");

    // fields of ThirdInnerBox
    assertEquals(1, thirdInnerBox.fields().stream().count(), "field count of 'ThirdInnerBox'");
    var serialVersionUID1 = assertFieldExist(thirdInnerBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(1, thirdInnerBox.methods().stream().count(), "method count of 'ThirdInnerBox'");
    var getThirdLevel = assertMethodExist(thirdInnerBox, "getThirdLevel", new String[]{});
    assertMethodReturnType(getThirdLevel, "formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData$ThirdInnerBox$ThirdLevel");

    assertEquals(1, thirdInnerBox.innerTypes().stream().count(), "inner types count of 'ThirdInnerBox'");
    // type ThirdLevel
    var thirdLevel = assertTypeExists(thirdInnerBox, "ThirdLevel");
    assertHasFlags(thirdLevel, 9);
    assertHasSuperClass(thirdLevel, "formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox$SecondLevel");
    assertAnnotation(thirdLevel, "org.eclipse.scout.rt.platform.Replace");

    // fields of ThirdLevel
    assertEquals(1, thirdLevel.fields().stream().count(), "field count of 'ThirdLevel'");
    var serialVersionUID2 = assertFieldExist(thirdLevel, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, thirdLevel.methods().stream().count(), "method count of 'ThirdLevel'");

    assertEquals(0, thirdLevel.innerTypes().stream().count(), "inner types count of 'ThirdLevel'");
  }
}
