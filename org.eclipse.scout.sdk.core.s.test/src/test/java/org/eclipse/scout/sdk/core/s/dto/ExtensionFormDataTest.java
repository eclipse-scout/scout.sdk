/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createRowDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperInterfaces;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.math.BigDecimal;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.annotation.ExtendsAnnotation;
import org.junit.jupiter.api.Test;

import formdata.client.extensions.FormPropertyExtension;
import formdata.client.extensions.MultiColumnExtension;
import formdata.client.extensions.MultipleExtGroupBoxExtension;
import formdata.client.extensions.SimpleTableFormExtension;
import formdata.client.extensions.ThirdIntegerColumn;

/**
 * <h3>{@link ExtensionFormDataTest}</h3>
 *
 * @since 4.2.0
 */
public class ExtensionFormDataTest {
  @Test
  public void testFormPropertyExtension() {
    createFormDataAssertNoCompileErrors(FormPropertyExtension.class.getName(), ExtensionFormDataTest::testApiOfPropertyExtensionData);
  }

  @Test
  public void testMultiColumnExtension() {
    createRowDataAssertNoCompileErrors(MultiColumnExtension.class.getName(), ExtensionFormDataTest::testApiOfMultiColumnExtensionData);
  }

  @Test
  public void testMultipleExtGroupBoxExtension() {
    createFormDataAssertNoCompileErrors(MultipleExtGroupBoxExtension.class.getName(), ExtensionFormDataTest::testApiOfMultipleExtGroupBoxExtensionData);
  }

  @Test
  public void testThirdIntegerColumn() {
    createRowDataAssertNoCompileErrors(ThirdIntegerColumn.class.getName(), ExtensionFormDataTest::testApiOfThirdIntegerColumnData);
  }

  @Test
  public void testSimpleTableFormExtension() {
    createRowDataAssertNoCompileErrors(SimpleTableFormExtension.class.getName(), dto -> {
      testApiOfSimpleTableFormExtensionData(dto);

      // verify the value of the @Extends annotation
      var annotation = dto.annotations().withManagedWrapper(ExtendsAnnotation.class).first().get();
      var originalRowData = annotation.value();
      assertEquals("formdata.shared.services.process.SimpleTableFormData$TestTable$TestTableRowData", originalRowData.name());
    });
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfSimpleTableFormExtensionData(IType simpleTableFormExtensionData) {
    assertHasFlags(simpleTableFormExtensionData, 1);
    assertHasSuperInterfaces(simpleTableFormExtensionData, new String[]{"java.io.Serializable"});
    assertEquals(2, simpleTableFormExtensionData.annotations().stream().count(), "annotation count");
    assertAnnotation(simpleTableFormExtensionData, "org.eclipse.scout.rt.platform.extension.Extends");
    assertAnnotation(simpleTableFormExtensionData, "javax.annotation.Generated");

    // fields of SimpleTableFormExtensionData
    assertEquals(3, simpleTableFormExtensionData.fields().stream().count(), "field count of 'formdata.shared.extension.SimpleTableFormExtensionData'");
    var serialVersionUID = assertFieldExist(simpleTableFormExtensionData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");
    var myExtension = assertFieldExist(simpleTableFormExtensionData, "myExtension");
    assertHasFlags(myExtension, 25);
    assertFieldType(myExtension, "java.lang.String");
    assertEquals(0, myExtension.annotations().stream().count(), "annotation count");
    var m_myExtension = assertFieldExist(simpleTableFormExtensionData, "m_myExtension");
    assertHasFlags(m_myExtension, 2);
    assertFieldType(m_myExtension, "java.math.BigDecimal");
    assertEquals(0, m_myExtension.annotations().stream().count(), "annotation count");

    assertEquals(2, simpleTableFormExtensionData.methods().stream().count(), "method count of 'formdata.shared.extension.SimpleTableFormExtensionData'");
    var getMyExtension = assertMethodExist(simpleTableFormExtensionData, "getMyExtension", new String[]{});
    assertMethodReturnType(getMyExtension, "java.math.BigDecimal");
    assertEquals(0, getMyExtension.annotations().stream().count(), "annotation count");
    var setMyExtension = assertMethodExist(simpleTableFormExtensionData, "setMyExtension", new String[]{"java.math.BigDecimal"});
    assertMethodReturnType(setMyExtension, "void");
    assertEquals(0, setMyExtension.annotations().stream().count(), "annotation count");

    assertEquals(0, simpleTableFormExtensionData.innerTypes().stream().count(), "inner types count of 'SimpleTableFormExtensionData'");
  }

  private static void testApiOfMultiColumnExtensionData(IType multiColumnExtensionData) {
    // type MultiColumnExtensionData
    assertHasFlags(multiColumnExtensionData, 1);
    assertHasSuperInterfaces(multiColumnExtensionData, new String[]{Serializable.class.getName()});
    assertAnnotation(multiColumnExtensionData, "org.eclipse.scout.rt.platform.extension.Extends");
    assertAnnotation(multiColumnExtensionData, "javax.annotation.Generated");

    // fields of MultiColumnExtensionData
    assertEquals(5, multiColumnExtensionData.fields().stream().count(), "field count of 'MultiColumnExtensionData'");
    var serialVersionUID = assertFieldExist(multiColumnExtensionData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    var thirdLong = assertFieldExist(multiColumnExtensionData, "thirdLong");
    assertHasFlags(thirdLong, 25);
    assertFieldType(thirdLong, String.class.getName());
    var fourthDouble = assertFieldExist(multiColumnExtensionData, "fourthDouble");
    assertHasFlags(fourthDouble, 25);
    assertFieldType(fourthDouble, String.class.getName());
    var m_thirdLong = assertFieldExist(multiColumnExtensionData, "m_thirdLong");
    assertHasFlags(m_thirdLong, 2);
    assertFieldType(m_thirdLong, Long.class.getName());
    var m_fourthDouble = assertFieldExist(multiColumnExtensionData, "m_fourthDouble");
    assertHasFlags(m_fourthDouble, 2);
    assertFieldType(m_fourthDouble, BigDecimal.class.getName());

    assertEquals(4, multiColumnExtensionData.methods().stream().count(), "method count of 'MultiColumnExtensionData'");
    var getThirdLong = assertMethodExist(multiColumnExtensionData, "getThirdLong", new String[]{});
    assertMethodReturnType(getThirdLong, Long.class.getName());
    var setThirdLong = assertMethodExist(multiColumnExtensionData, "setThirdLong", new String[]{Long.class.getName()});
    assertMethodReturnType(setThirdLong, "void");
    var getFourthDouble = assertMethodExist(multiColumnExtensionData, "getFourthDouble", new String[]{});
    assertMethodReturnType(getFourthDouble, BigDecimal.class.getName());
    var setFourthDouble = assertMethodExist(multiColumnExtensionData, "setFourthDouble", new String[]{BigDecimal.class.getName()});
    assertMethodReturnType(setFourthDouble, "void");

    assertEquals(0, multiColumnExtensionData.innerTypes().stream().count(), "inner types count of 'MultiColumnExtensionData'");
  }

  private static void testApiOfPropertyExtensionData(IType propertyExtensionData) {
    // type PropertyExtensionData
    assertHasFlags(propertyExtensionData, 1);
    assertHasSuperClass(propertyExtensionData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData");
    assertAnnotation(propertyExtensionData, "org.eclipse.scout.rt.platform.extension.Extends");
    assertAnnotation(propertyExtensionData, "javax.annotation.Generated");

    // fields of PropertyExtensionData
    assertEquals(1, propertyExtensionData.fields().stream().count(), "field count of 'PropertyExtensionData'");
    var serialVersionUID = assertFieldExist(propertyExtensionData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(3, propertyExtensionData.methods().stream().count(), "method count of 'PropertyExtensionData'");
    var getLongValue = assertMethodExist(propertyExtensionData, "getLongValue", new String[]{});
    assertMethodReturnType(getLongValue, Long.class.getName());
    var setLongValue = assertMethodExist(propertyExtensionData, "setLongValue", new String[]{Long.class.getName()});
    assertMethodReturnType(setLongValue, "void");
    var getLongValueProperty = assertMethodExist(propertyExtensionData, "getLongValueProperty", new String[]{});
    assertMethodReturnType(getLongValueProperty, "formdata.shared.extension.PropertyExtensionData$LongValueProperty");

    assertEquals(1, propertyExtensionData.innerTypes().stream().count(), "inner types count of 'PropertyExtensionData'");
    // type LongValueProperty
    var longValueProperty = assertTypeExists(propertyExtensionData, "LongValueProperty");
    assertHasFlags(longValueProperty, 9);
    assertHasSuperClass(longValueProperty, "org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData<java.lang.Long>");

    // fields of LongValueProperty
    assertEquals(1, longValueProperty.fields().stream().count(), "field count of 'LongValueProperty'");
    var serialVersionUID1 = assertFieldExist(longValueProperty, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, longValueProperty.methods().stream().count(), "method count of 'LongValueProperty'");

    assertEquals(0, longValueProperty.innerTypes().stream().count(), "inner types count of 'LongValueProperty'");
  }

  private static void testApiOfThirdIntegerColumnData(IType thirdIntegerColumnData) {
    // type ThirdIntegerColumnData
    assertHasFlags(thirdIntegerColumnData, 1);
    assertHasSuperInterfaces(thirdIntegerColumnData, new String[]{Serializable.class.getName()});
    assertAnnotation(thirdIntegerColumnData, "org.eclipse.scout.rt.platform.extension.Extends");
    assertAnnotation(thirdIntegerColumnData, "javax.annotation.Generated");

    // fields of ThirdIntegerColumnData
    assertEquals(3, thirdIntegerColumnData.fields().stream().count(), "field count of 'ThirdIntegerColumnData'");
    var serialVersionUID = assertFieldExist(thirdIntegerColumnData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    var thirdInteger = assertFieldExist(thirdIntegerColumnData, "thirdInteger");
    assertHasFlags(thirdInteger, 25);
    assertFieldType(thirdInteger, String.class.getName());
    var m_thirdInteger = assertFieldExist(thirdIntegerColumnData, "m_thirdInteger");
    assertHasFlags(m_thirdInteger, 2);
    assertFieldType(m_thirdInteger, Integer.class.getName());

    assertEquals(2, thirdIntegerColumnData.methods().stream().count(), "method count of 'ThirdIntegerColumnData'");
    var getThirdInteger = assertMethodExist(thirdIntegerColumnData, "getThirdInteger", new String[]{});
    assertMethodReturnType(getThirdInteger, Integer.class.getName());
    var setThirdInteger = assertMethodExist(thirdIntegerColumnData, "setThirdInteger", new String[]{Integer.class.getName()});
    assertMethodReturnType(setThirdInteger, "void");

    assertEquals(0, thirdIntegerColumnData.innerTypes().stream().count(), "inner types count of 'ThirdIntegerColumnData'");
  }

  private static void testApiOfMultipleExtGroupBoxExtensionData(IType multipleExtGroupBoxExtensionData) {
    // type MultipleExtGroupBoxExtensionData
    assertHasFlags(multipleExtGroupBoxExtensionData, 1);
    assertHasSuperClass(multipleExtGroupBoxExtensionData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData");
    assertAnnotation(multipleExtGroupBoxExtensionData, "org.eclipse.scout.rt.platform.extension.Extends");
    assertAnnotation(multipleExtGroupBoxExtensionData, "javax.annotation.Generated");

    // fields of MultipleExtGroupBoxExtensionData
    assertEquals(1, multipleExtGroupBoxExtensionData.fields().stream().count(), "field count of 'MultipleExtGroupBoxExtensionData'");
    var serialVersionUID = assertFieldExist(multipleExtGroupBoxExtensionData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(2, multipleExtGroupBoxExtensionData.methods().stream().count(), "method count of 'MultipleExtGroupBoxExtensionData'");
    var getSecondDouble = assertMethodExist(multipleExtGroupBoxExtensionData, "getSecondDouble", new String[]{});
    assertMethodReturnType(getSecondDouble, "formdata.shared.extension.MultipleExtGroupBoxExtensionData$SecondDouble");
    var getThirdDate = assertMethodExist(multipleExtGroupBoxExtensionData, "getThirdDate", new String[]{});
    assertMethodReturnType(getThirdDate, "formdata.shared.extension.MultipleExtGroupBoxExtensionData$ThirdDate");

    assertEquals(2, multipleExtGroupBoxExtensionData.innerTypes().stream().count(), "inner types count of 'MultipleExtGroupBoxExtensionData'");
    // type SecondDouble
    var secondDouble = assertTypeExists(multipleExtGroupBoxExtensionData, "SecondDouble");
    assertHasFlags(secondDouble, 9);
    assertHasSuperClass(secondDouble, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.math.BigDecimal>");

    // fields of SecondDouble
    assertEquals(1, secondDouble.fields().stream().count(), "field count of 'SecondDouble'");
    var serialVersionUID1 = assertFieldExist(secondDouble, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, secondDouble.methods().stream().count(), "method count of 'SecondDouble'");

    assertEquals(0, secondDouble.innerTypes().stream().count(), "inner types count of 'SecondDouble'");
    // type ThirdDate
    var thirdDate = assertTypeExists(multipleExtGroupBoxExtensionData, "ThirdDate");
    assertHasFlags(thirdDate, 9);
    assertHasSuperClass(thirdDate, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.util.Date>");

    // fields of ThirdDate
    assertEquals(1, thirdDate.fields().stream().count(), "field count of 'ThirdDate'");
    var serialVersionUID2 = assertFieldExist(thirdDate, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, thirdDate.methods().stream().count(), "method count of 'ThirdDate'");

    assertEquals(0, thirdDate.innerTypes().stream().count(), "inner types count of 'ThirdDate'");
  }

}
