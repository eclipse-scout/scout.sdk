/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertTypeExists;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper;
import org.junit.jupiter.api.Test;

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
    createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.AbstractMainBox", NestedMultiLevelFormFieldTest::testApiOfAbstractMainBoxData);
  }

  private static void checkAbstractTemplateFieldData() {
    ScoutFixtureHelper.runWithSharedAndClientEnv((shared, client) -> {
      var modelType = client.requireType("formdata.client.ui.template.formfield.replace.levels.AbstractTemplateField");
      assertFalse(DtoGeneratorFactory.createFormDataGenerator(modelType, shared).isPresent()); // must be empty because it is SdkCommand.USE

      var dto = client.requireType("formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData");
      testApiOfAbstractTemplateFieldData(dto);
      return null;
    });
  }

  private static void checkFirstLevelFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.FirstLevelForm", NestedMultiLevelFormFieldTest::testApiOfFirstLevelFormData);
  }

  private static void checkSecondLevelFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.SecondLevelForm", NestedMultiLevelFormFieldTest::testApiOfSecondLevelFormData);
  }

  private static void checkThirdLevelFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.levels.ThirdLevelForm", NestedMultiLevelFormFieldTest::testApiOfThirdLevelFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfAbstractMainBoxData(IType abstractMainBoxData) {
    var scoutApi = abstractMainBoxData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(abstractMainBoxData, Flags.AccPublic | Flags.AccAbstract);
    assertHasSuperClass(abstractMainBoxData, scoutApi.AbstractFormFieldData());
    assertEquals(1, abstractMainBoxData.annotations().stream().count(), "annotation count");
    assertAnnotation(abstractMainBoxData, scoutApi.Generated());

    // fields of AbstractMainBoxData
    assertEquals(1, abstractMainBoxData.fields().stream().count(), "field count of 'formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData'");
    var serialVersionUID = assertFieldExist(abstractMainBoxData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, abstractMainBoxData.methods().stream().count(), "method count of 'formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData'");
    var getFirstLevel = assertMethodExist(abstractMainBoxData, "getFirstLevel");
    assertMethodReturnType(getFirstLevel, "formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData$FirstLevel");
    assertEquals(0, getFirstLevel.annotations().stream().count(), "annotation count");

    assertEquals(1, abstractMainBoxData.innerTypes().stream().count(), "inner types count of 'AbstractMainBoxData'");
    // type FirstLevel
    var firstLevel = assertTypeExists(abstractMainBoxData, "FirstLevel");
    assertHasFlags(firstLevel, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(firstLevel, "formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData<java.lang.Number>");
    assertEquals(0, firstLevel.annotations().stream().count(), "annotation count");

    // fields of FirstLevel
    assertEquals(1, firstLevel.fields().stream().count(), "field count of 'formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData$FirstLevel'");
    var serialVersionUID1 = assertFieldExist(firstLevel, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, firstLevel.methods().stream().count(), "method count of 'formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData$FirstLevel'");

    assertEquals(0, firstLevel.innerTypes().stream().count(), "inner types count of 'FirstLevel'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfAbstractTemplateFieldData(IType abstractTemplateFieldData) {
    var scoutApi = abstractTemplateFieldData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(abstractTemplateFieldData, Flags.AccPublic | Flags.AccAbstract);
    assertHasSuperClass(abstractTemplateFieldData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.util.List<T>>");
    assertEquals(1, abstractTemplateFieldData.annotations().stream().count(), "annotation count");
    assertAnnotation(abstractTemplateFieldData, scoutApi.Generated());

    // fields of AbstractTemplateFieldData
    assertEquals(1, abstractTemplateFieldData.fields().stream().count(), "field count of 'formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData'");
    var serialVersionUID = assertFieldExist(abstractTemplateFieldData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(0, abstractTemplateFieldData.methods().stream().count(), "method count of 'formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData'");

    assertEquals(0, abstractTemplateFieldData.innerTypes().stream().count(), "inner types count of 'AbstractTemplateFieldData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfFirstLevelFormData(IType firstLevelFormData) {
    var scoutApi = firstLevelFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(firstLevelFormData, Flags.AccPublic);
    assertHasSuperClass(firstLevelFormData, scoutApi.AbstractFormData());
    assertEquals(1, firstLevelFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(firstLevelFormData, scoutApi.Generated());

    // fields of FirstLevelFormData
    assertEquals(1, firstLevelFormData.fields().stream().count(), "field count of 'formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData'");
    var serialVersionUID = assertFieldExist(firstLevelFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, firstLevelFormData.methods().stream().count(), "method count of 'formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData'");
    var getFirstInnerBox = assertMethodExist(firstLevelFormData, "getFirstInnerBox");
    assertMethodReturnType(getFirstInnerBox, "formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData$FirstInnerBox");
    assertEquals(0, getFirstInnerBox.annotations().stream().count(), "annotation count");

    assertEquals(1, firstLevelFormData.innerTypes().stream().count(), "inner types count of 'FirstLevelFormData'");
    // type FirstInnerBox
    var firstInnerBox = assertTypeExists(firstLevelFormData, "FirstInnerBox");
    assertHasFlags(firstInnerBox, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(firstInnerBox, "formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData");
    assertEquals(0, firstInnerBox.annotations().stream().count(), "annotation count");

    // fields of FirstInnerBox
    assertEquals(1, firstInnerBox.fields().stream().count(), "field count of 'formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData$FirstInnerBox'");
    var serialVersionUID1 = assertFieldExist(firstInnerBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, firstInnerBox.methods().stream().count(), "method count of 'formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData$FirstInnerBox'");

    assertEquals(0, firstInnerBox.innerTypes().stream().count(), "inner types count of 'FirstInnerBox'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfSecondLevelFormData(IType secondLevelFormData) {
    var scoutApi = secondLevelFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(secondLevelFormData, Flags.AccPublic);
    assertHasSuperClass(secondLevelFormData, "formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData");
    assertEquals(1, secondLevelFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(secondLevelFormData, scoutApi.Generated());

    // fields of SecondLevelFormData
    assertEquals(1, secondLevelFormData.fields().stream().count(), "field count of 'formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData'");
    var serialVersionUID = assertFieldExist(secondLevelFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, secondLevelFormData.methods().stream().count(), "method count of 'formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData'");
    var getSecondInnerBox = assertMethodExist(secondLevelFormData, "getSecondInnerBox");
    assertMethodReturnType(getSecondInnerBox, "formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox");
    assertEquals(0, getSecondInnerBox.annotations().stream().count(), "annotation count");

    assertEquals(1, secondLevelFormData.innerTypes().stream().count(), "inner types count of 'SecondLevelFormData'");
    // type SecondInnerBox
    var secondInnerBox = assertTypeExists(secondLevelFormData, "SecondInnerBox");
    assertHasFlags(secondInnerBox, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(secondInnerBox, "formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData$FirstInnerBox");
    assertEquals(1, secondInnerBox.annotations().stream().count(), "annotation count");
    assertAnnotation(secondInnerBox, scoutApi.Replace());

    // fields of SecondInnerBox
    assertEquals(1, secondInnerBox.fields().stream().count(), "field count of 'formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox'");
    var serialVersionUID1 = assertFieldExist(secondInnerBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(1, secondInnerBox.methods().stream().count(), "method count of 'formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox'");
    var getSecondLevel = assertMethodExist(secondInnerBox, "getSecondLevel");
    assertMethodReturnType(getSecondLevel, "formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox$SecondLevel");
    assertEquals(0, getSecondLevel.annotations().stream().count(), "annotation count");

    assertEquals(1, secondInnerBox.innerTypes().stream().count(), "inner types count of 'SecondInnerBox'");
    // type SecondLevel
    var secondLevel = assertTypeExists(secondInnerBox, "SecondLevel");
    assertHasFlags(secondLevel, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(secondLevel, "formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData$FirstLevel");
    assertEquals(1, secondLevel.annotations().stream().count(), "annotation count");
    assertAnnotation(secondLevel, scoutApi.Replace());

    // fields of SecondLevel
    assertEquals(1, secondLevel.fields().stream().count(), "field count of 'formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox$SecondLevel'");
    var serialVersionUID2 = assertFieldExist(secondLevel, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, secondLevel.methods().stream().count(), "method count of 'formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox$SecondLevel'");

    assertEquals(0, secondLevel.innerTypes().stream().count(), "inner types count of 'SecondLevel'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfThirdLevelFormData(IType thirdLevelFormData) {
    var scoutApi = thirdLevelFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(thirdLevelFormData, Flags.AccPublic);
    assertHasSuperClass(thirdLevelFormData, "formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData");
    assertEquals(1, thirdLevelFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(thirdLevelFormData, scoutApi.Generated());

    // fields of ThirdLevelFormData
    assertEquals(1, thirdLevelFormData.fields().stream().count(), "field count of 'formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData'");
    var serialVersionUID = assertFieldExist(thirdLevelFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, thirdLevelFormData.methods().stream().count(), "method count of 'formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData'");
    var getThirdInnerBox = assertMethodExist(thirdLevelFormData, "getThirdInnerBox");
    assertMethodReturnType(getThirdInnerBox, "formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData$ThirdInnerBox");
    assertEquals(0, getThirdInnerBox.annotations().stream().count(), "annotation count");

    assertEquals(1, thirdLevelFormData.innerTypes().stream().count(), "inner types count of 'ThirdLevelFormData'");
    // type ThirdInnerBox
    var thirdInnerBox = assertTypeExists(thirdLevelFormData, "ThirdInnerBox");
    assertHasFlags(thirdInnerBox, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(thirdInnerBox, "formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox");
    assertEquals(1, thirdInnerBox.annotations().stream().count(), "annotation count");
    assertAnnotation(thirdInnerBox, scoutApi.Replace());

    // fields of ThirdInnerBox
    assertEquals(1, thirdInnerBox.fields().stream().count(), "field count of 'formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData$ThirdInnerBox'");
    var serialVersionUID1 = assertFieldExist(thirdInnerBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(1, thirdInnerBox.methods().stream().count(), "method count of 'formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData$ThirdInnerBox'");
    var getThirdLevel = assertMethodExist(thirdInnerBox, "getThirdLevel");
    assertMethodReturnType(getThirdLevel, "formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData$ThirdInnerBox$ThirdLevel");
    assertEquals(0, getThirdLevel.annotations().stream().count(), "annotation count");

    assertEquals(1, thirdInnerBox.innerTypes().stream().count(), "inner types count of 'ThirdInnerBox'");
    // type ThirdLevel
    var thirdLevel = assertTypeExists(thirdInnerBox, "ThirdLevel");
    assertHasFlags(thirdLevel, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(thirdLevel, "formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData$SecondInnerBox$SecondLevel");
    assertEquals(1, thirdLevel.annotations().stream().count(), "annotation count");
    assertAnnotation(thirdLevel, scoutApi.Replace());

    // fields of ThirdLevel
    assertEquals(1, thirdLevel.fields().stream().count(), "field count of 'formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData$ThirdInnerBox$ThirdLevel'");
    var serialVersionUID2 = assertFieldExist(thirdLevel, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, thirdLevel.methods().stream().count(), "method count of 'formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData$ThirdInnerBox$ThirdLevel'");

    assertEquals(0, thirdLevel.innerTypes().stream().count(), "inner types count of 'ThirdLevel'");
  }
}
