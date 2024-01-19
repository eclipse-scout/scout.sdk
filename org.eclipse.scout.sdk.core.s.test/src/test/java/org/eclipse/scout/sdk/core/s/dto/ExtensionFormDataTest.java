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
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperInterfaces;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertTypeExists;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createRowDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.annotation.ExtendsAnnotation;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ExtensionFormDataTest}</h3>
 *
 * @since 4.2.0
 */
public class ExtensionFormDataTest {
  @Test
  public void testFormPropertyExtension() {
    createFormDataAssertNoCompileErrors("formdata.client.extensions.FormPropertyExtension", ExtensionFormDataTest::testApiOfPropertyExtensionData);
  }

  @Test
  public void testMultiColumnExtension() {
    createRowDataAssertNoCompileErrors("formdata.client.extensions.MultiColumnExtension", ExtensionFormDataTest::testApiOfMultiColumnExtensionData);
  }

  @Test
  public void testMultipleExtGroupBoxExtension() {
    createFormDataAssertNoCompileErrors("formdata.client.extensions.MultipleExtGroupBoxExtension", ExtensionFormDataTest::testApiOfMultipleExtGroupBoxExtensionData);
  }

  @Test
  public void testThirdIntegerColumn() {
    createRowDataAssertNoCompileErrors("formdata.client.extensions.ThirdIntegerColumn", ExtensionFormDataTest::testApiOfThirdIntegerColumnData);
  }

  @Test
  public void testSimpleTableFormExtension() {
    createRowDataAssertNoCompileErrors("formdata.client.extensions.SimpleTableFormExtension", dto -> {
      testApiOfSimpleTableFormExtensionData(dto);

      // verify the value of the @Extends annotation
      var annotation = dto.annotations().withManagedWrapper(ExtendsAnnotation.class).first().orElseThrow();
      var originalRowData = annotation.value();
      assertEquals("formdata.shared.services.process.SimpleTableFormData$TestTable$TestTableRowData", originalRowData.name());
    });
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfSimpleTableFormExtensionData(IType simpleTableFormExtensionData) {
    var scoutApi = simpleTableFormExtensionData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(simpleTableFormExtensionData, Flags.AccPublic);
    assertHasSuperClass(simpleTableFormExtensionData, "java.lang.Object");
    assertHasSuperInterfaces(simpleTableFormExtensionData, new String[]{"java.io.Serializable"});
    assertEquals(2, simpleTableFormExtensionData.annotations().stream().count(), "annotation count");
    assertAnnotation(simpleTableFormExtensionData, scoutApi.Extends());
    assertAnnotation(simpleTableFormExtensionData, scoutApi.Generated());

    // fields of SimpleTableFormExtensionData
    assertEquals(3, simpleTableFormExtensionData.fields().stream().count(), "field count of 'formdata.shared.extension.SimpleTableFormExtensionData'");
    var serialVersionUID = assertFieldExist(simpleTableFormExtensionData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");
    var myExtension = assertFieldExist(simpleTableFormExtensionData, "myExtension");
    assertHasFlags(myExtension, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(myExtension, "java.lang.String");
    assertEquals(0, myExtension.annotations().stream().count(), "annotation count");
    var m_myExtension = assertFieldExist(simpleTableFormExtensionData, "m_myExtension");
    assertHasFlags(m_myExtension, Flags.AccPrivate);
    assertFieldType(m_myExtension, "java.math.BigDecimal");
    assertEquals(0, m_myExtension.annotations().stream().count(), "annotation count");

    assertEquals(2, simpleTableFormExtensionData.methods().stream().count(), "method count of 'formdata.shared.extension.SimpleTableFormExtensionData'");
    var getMyExtension = assertMethodExist(simpleTableFormExtensionData, "getMyExtension");
    assertMethodReturnType(getMyExtension, "java.math.BigDecimal");
    assertEquals(0, getMyExtension.annotations().stream().count(), "annotation count");
    var setMyExtension = assertMethodExist(simpleTableFormExtensionData, "setMyExtension", new String[]{"java.math.BigDecimal"});
    assertMethodReturnType(setMyExtension, "void");
    assertEquals(0, setMyExtension.annotations().stream().count(), "annotation count");

    assertEquals(0, simpleTableFormExtensionData.innerTypes().stream().count(), "inner types count of 'SimpleTableFormExtensionData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfMultiColumnExtensionData(IType multiColumnExtensionData) {
    var scoutApi = multiColumnExtensionData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(multiColumnExtensionData, Flags.AccPublic);
    assertHasSuperClass(multiColumnExtensionData, "java.lang.Object");
    assertHasSuperInterfaces(multiColumnExtensionData, new String[]{"java.io.Serializable"});
    assertEquals(2, multiColumnExtensionData.annotations().stream().count(), "annotation count");
    assertAnnotation(multiColumnExtensionData, scoutApi.Extends());
    assertAnnotation(multiColumnExtensionData, scoutApi.Generated());

    // fields of MultiColumnExtensionData
    assertEquals(5, multiColumnExtensionData.fields().stream().count(), "field count of 'formdata.shared.extension.MultiColumnExtensionData'");
    var serialVersionUID = assertFieldExist(multiColumnExtensionData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");
    var thirdLong = assertFieldExist(multiColumnExtensionData, "thirdLong");
    assertHasFlags(thirdLong, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(thirdLong, "java.lang.String");
    assertEquals(0, thirdLong.annotations().stream().count(), "annotation count");
    var fourthDouble = assertFieldExist(multiColumnExtensionData, "fourthDouble");
    assertHasFlags(fourthDouble, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(fourthDouble, "java.lang.String");
    assertEquals(0, fourthDouble.annotations().stream().count(), "annotation count");
    var m_thirdLong = assertFieldExist(multiColumnExtensionData, "m_thirdLong");
    assertHasFlags(m_thirdLong, Flags.AccPrivate);
    assertFieldType(m_thirdLong, "java.lang.Long");
    assertEquals(0, m_thirdLong.annotations().stream().count(), "annotation count");
    var m_fourthDouble = assertFieldExist(multiColumnExtensionData, "m_fourthDouble");
    assertHasFlags(m_fourthDouble, Flags.AccPrivate);
    assertFieldType(m_fourthDouble, "java.math.BigDecimal");
    assertEquals(0, m_fourthDouble.annotations().stream().count(), "annotation count");

    assertEquals(4, multiColumnExtensionData.methods().stream().count(), "method count of 'formdata.shared.extension.MultiColumnExtensionData'");
    var getThirdLong = assertMethodExist(multiColumnExtensionData, "getThirdLong");
    assertMethodReturnType(getThirdLong, "java.lang.Long");
    assertEquals(0, getThirdLong.annotations().stream().count(), "annotation count");
    var setThirdLong = assertMethodExist(multiColumnExtensionData, "setThirdLong", new String[]{"java.lang.Long"});
    assertMethodReturnType(setThirdLong, "void");
    assertEquals(0, setThirdLong.annotations().stream().count(), "annotation count");
    var getFourthDouble = assertMethodExist(multiColumnExtensionData, "getFourthDouble");
    assertMethodReturnType(getFourthDouble, "java.math.BigDecimal");
    assertEquals(0, getFourthDouble.annotations().stream().count(), "annotation count");
    var setFourthDouble = assertMethodExist(multiColumnExtensionData, "setFourthDouble", new String[]{"java.math.BigDecimal"});
    assertMethodReturnType(setFourthDouble, "void");
    assertEquals(0, setFourthDouble.annotations().stream().count(), "annotation count");

    assertEquals(0, multiColumnExtensionData.innerTypes().stream().count(), "inner types count of 'MultiColumnExtensionData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfPropertyExtensionData(IType propertyExtensionData) {
    var scoutApi = propertyExtensionData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(propertyExtensionData, Flags.AccPublic);
    assertHasSuperClass(propertyExtensionData, scoutApi.AbstractFormFieldData());
    assertEquals(2, propertyExtensionData.annotations().stream().count(), "annotation count");
    assertAnnotation(propertyExtensionData, scoutApi.Extends());
    assertAnnotation(propertyExtensionData, scoutApi.Generated());

    // fields of PropertyExtensionData
    assertEquals(1, propertyExtensionData.fields().stream().count(), "field count of 'formdata.shared.extension.PropertyExtensionData'");
    var serialVersionUID = assertFieldExist(propertyExtensionData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(3, propertyExtensionData.methods().stream().count(), "method count of 'formdata.shared.extension.PropertyExtensionData'");
    var getLongValue = assertMethodExist(propertyExtensionData, "getLongValue");
    assertMethodReturnType(getLongValue, "java.lang.Long");
    assertEquals(0, getLongValue.annotations().stream().count(), "annotation count");
    var setLongValue = assertMethodExist(propertyExtensionData, "setLongValue", new String[]{"java.lang.Long"});
    assertMethodReturnType(setLongValue, "void");
    assertEquals(0, setLongValue.annotations().stream().count(), "annotation count");
    var getLongValueProperty = assertMethodExist(propertyExtensionData, "getLongValueProperty");
    assertMethodReturnType(getLongValueProperty, "formdata.shared.extension.PropertyExtensionData$LongValueProperty");
    assertEquals(0, getLongValueProperty.annotations().stream().count(), "annotation count");

    assertEquals(1, propertyExtensionData.innerTypes().stream().count(), "inner types count of 'PropertyExtensionData'");
    // type LongValueProperty
    var longValueProperty = assertTypeExists(propertyExtensionData, "LongValueProperty");
    assertHasFlags(longValueProperty, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(longValueProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Long>");
    assertEquals(0, longValueProperty.annotations().stream().count(), "annotation count");

    // fields of LongValueProperty
    assertEquals(1, longValueProperty.fields().stream().count(), "field count of 'formdata.shared.extension.PropertyExtensionData$LongValueProperty'");
    var serialVersionUID1 = assertFieldExist(longValueProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, longValueProperty.methods().stream().count(), "method count of 'formdata.shared.extension.PropertyExtensionData$LongValueProperty'");

    assertEquals(0, longValueProperty.innerTypes().stream().count(), "inner types count of 'LongValueProperty'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfThirdIntegerColumnData(IType thirdIntegerColumnData) {
    var scoutApi = thirdIntegerColumnData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(thirdIntegerColumnData, Flags.AccPublic);
    assertHasSuperClass(thirdIntegerColumnData, "java.lang.Object");
    assertHasSuperInterfaces(thirdIntegerColumnData, new String[]{"java.io.Serializable"});
    assertEquals(2, thirdIntegerColumnData.annotations().stream().count(), "annotation count");
    assertAnnotation(thirdIntegerColumnData, scoutApi.Extends());
    assertAnnotation(thirdIntegerColumnData, scoutApi.Generated());

    // fields of ThirdIntegerColumnData
    assertEquals(3, thirdIntegerColumnData.fields().stream().count(), "field count of 'formdata.shared.extension.ThirdIntegerColumnData'");
    var serialVersionUID = assertFieldExist(thirdIntegerColumnData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");
    var thirdInteger = assertFieldExist(thirdIntegerColumnData, "thirdInteger");
    assertHasFlags(thirdInteger, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(thirdInteger, "java.lang.String");
    assertEquals(0, thirdInteger.annotations().stream().count(), "annotation count");
    var m_thirdInteger = assertFieldExist(thirdIntegerColumnData, "m_thirdInteger");
    assertHasFlags(m_thirdInteger, Flags.AccPrivate);
    assertFieldType(m_thirdInteger, "java.lang.Integer");
    assertEquals(0, m_thirdInteger.annotations().stream().count(), "annotation count");

    assertEquals(2, thirdIntegerColumnData.methods().stream().count(), "method count of 'formdata.shared.extension.ThirdIntegerColumnData'");
    var getThirdInteger = assertMethodExist(thirdIntegerColumnData, "getThirdInteger");
    assertMethodReturnType(getThirdInteger, "java.lang.Integer");
    assertEquals(0, getThirdInteger.annotations().stream().count(), "annotation count");
    var setThirdInteger = assertMethodExist(thirdIntegerColumnData, "setThirdInteger", new String[]{"java.lang.Integer"});
    assertMethodReturnType(setThirdInteger, "void");
    assertEquals(0, setThirdInteger.annotations().stream().count(), "annotation count");

    assertEquals(0, thirdIntegerColumnData.innerTypes().stream().count(), "inner types count of 'ThirdIntegerColumnData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfMultipleExtGroupBoxExtensionData(IType multipleExtGroupBoxExtensionData) {
    var scoutApi = multipleExtGroupBoxExtensionData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(multipleExtGroupBoxExtensionData, Flags.AccPublic);
    assertHasSuperClass(multipleExtGroupBoxExtensionData, scoutApi.AbstractFormFieldData());
    assertEquals(2, multipleExtGroupBoxExtensionData.annotations().stream().count(), "annotation count");
    assertAnnotation(multipleExtGroupBoxExtensionData, scoutApi.Extends());
    assertAnnotation(multipleExtGroupBoxExtensionData, scoutApi.Generated());

    // fields of MultipleExtGroupBoxExtensionData
    assertEquals(1, multipleExtGroupBoxExtensionData.fields().stream().count(), "field count of 'formdata.shared.extension.MultipleExtGroupBoxExtensionData'");
    var serialVersionUID = assertFieldExist(multipleExtGroupBoxExtensionData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(2, multipleExtGroupBoxExtensionData.methods().stream().count(), "method count of 'formdata.shared.extension.MultipleExtGroupBoxExtensionData'");
    var getSecondDouble = assertMethodExist(multipleExtGroupBoxExtensionData, "getSecondDouble");
    assertMethodReturnType(getSecondDouble, "formdata.shared.extension.MultipleExtGroupBoxExtensionData$SecondDouble");
    assertEquals(0, getSecondDouble.annotations().stream().count(), "annotation count");
    var getThirdDate = assertMethodExist(multipleExtGroupBoxExtensionData, "getThirdDate");
    assertMethodReturnType(getThirdDate, "formdata.shared.extension.MultipleExtGroupBoxExtensionData$ThirdDate");
    assertEquals(0, getThirdDate.annotations().stream().count(), "annotation count");

    assertEquals(2, multipleExtGroupBoxExtensionData.innerTypes().stream().count(), "inner types count of 'MultipleExtGroupBoxExtensionData'");
    // type SecondDouble
    var secondDouble = assertTypeExists(multipleExtGroupBoxExtensionData, "SecondDouble");
    assertHasFlags(secondDouble, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(secondDouble, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.math.BigDecimal>");
    assertEquals(0, secondDouble.annotations().stream().count(), "annotation count");

    // fields of SecondDouble
    assertEquals(1, secondDouble.fields().stream().count(), "field count of 'formdata.shared.extension.MultipleExtGroupBoxExtensionData$SecondDouble'");
    var serialVersionUID1 = assertFieldExist(secondDouble, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, secondDouble.methods().stream().count(), "method count of 'formdata.shared.extension.MultipleExtGroupBoxExtensionData$SecondDouble'");

    assertEquals(0, secondDouble.innerTypes().stream().count(), "inner types count of 'SecondDouble'");
    // type ThirdDate
    var thirdDate = assertTypeExists(multipleExtGroupBoxExtensionData, "ThirdDate");
    assertHasFlags(thirdDate, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(thirdDate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.util.Date>");
    assertEquals(0, thirdDate.annotations().stream().count(), "annotation count");

    // fields of ThirdDate
    assertEquals(1, thirdDate.fields().stream().count(), "field count of 'formdata.shared.extension.MultipleExtGroupBoxExtensionData$ThirdDate'");
    var serialVersionUID2 = assertFieldExist(thirdDate, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, thirdDate.methods().stream().count(), "method count of 'formdata.shared.extension.MultipleExtGroupBoxExtensionData$ThirdDate'");

    assertEquals(0, thirdDate.innerTypes().stream().count(), "inner types count of 'ThirdDate'");
  }
}
