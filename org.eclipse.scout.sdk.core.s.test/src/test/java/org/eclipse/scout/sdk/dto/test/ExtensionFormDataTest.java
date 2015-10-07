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

import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.dto.test.util.CoreScoutTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link ExtensionFormDataTest}</h3>
 *
 * @author Matthias Villiger
 * @since 4.2.0
 */
public class ExtensionFormDataTest {
  @Test
  public void testFormPropertyExtension() {
    String extensionName = "FormPropertyExtension";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.extensions." + extensionName);
    testApiOfPropertyExtensionData(dto);
  }

  @Test
  public void testMultiColumnExtension() {
    String extensionName = "MultiColumnExtension";
    IType dto = CoreScoutTestingUtils.createRowDataAssertNoCompileErrors("formdata.client.extensions." + extensionName);
    testApiOfMultiColumnExtensionData(dto);
  }

  @Test
  public void testMultipleExtGroupBoxExtension() {
    String formName = "MultipleExtGroupBoxExtension";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.extensions." + formName);
    testApiOfMultipleExtGroupBoxExtensionData(dto);
  }

  @Test
  public void testThirdIntegerColumn() {
    String extensionName = "ThirdIntegerColumn";
    IType dto = CoreScoutTestingUtils.createRowDataAssertNoCompileErrors("formdata.client.extensions." + extensionName);
    testApiOfThirdIntegerColumnData(dto);
  }

  @Test
  public void testSimpleTableFormExtension() {
    String extensionName = "SimpleTableFormExtension";
    IType dto = CoreScoutTestingUtils.createRowDataAssertNoCompileErrors("formdata.client.extensions." + extensionName);
    testApiOfSimpleTableFormExtensionData(dto);

    // verify the value of the @Extends annotation
    IAnnotation annotation = dto.annotations().withName(IScoutRuntimeTypes.Extends).first();
    IType originalRowData = annotation.element("value").value().get(IType.class);
    Assert.assertEquals("formdata.shared.services.process.SimpleTableFormData$TestTable$TestTableRowData", originalRowData.name());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfSimpleTableFormExtensionData(IType simpleTableFormExtensionData) {
    SdkAssert.assertHasFlags(simpleTableFormExtensionData, 1);
    SdkAssert.assertHasSuperIntefaceSignatures(simpleTableFormExtensionData, new String[]{"Ljava.io.Serializable;"});
    Assert.assertEquals("annotation count", 2, simpleTableFormExtensionData.annotations().list().size());
    SdkAssert.assertAnnotation(simpleTableFormExtensionData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(simpleTableFormExtensionData, "javax.annotation.Generated");

    // fields of SimpleTableFormExtensionData
    Assert.assertEquals("field count of 'formdata.shared.extension.SimpleTableFormExtensionData'", 3, simpleTableFormExtensionData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(simpleTableFormExtensionData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID.annotations().list().size());
    IField myExtension = SdkAssert.assertFieldExist(simpleTableFormExtensionData, "myExtension");
    SdkAssert.assertHasFlags(myExtension, 25);
    SdkAssert.assertFieldSignature(myExtension, "Ljava.lang.String;");
    Assert.assertEquals("annotation count", 0, myExtension.annotations().list().size());
    IField m_myExtension = SdkAssert.assertFieldExist(simpleTableFormExtensionData, "m_myExtension");
    SdkAssert.assertHasFlags(m_myExtension, 2);
    SdkAssert.assertFieldSignature(m_myExtension, "Ljava.math.BigDecimal;");
    Assert.assertEquals("annotation count", 0, m_myExtension.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.extension.SimpleTableFormExtensionData'", 3, simpleTableFormExtensionData.methods().list().size());
    IMethod simpleTableFormExtensionData1 = SdkAssert.assertMethodExist(simpleTableFormExtensionData, "SimpleTableFormExtensionData", new String[]{});
    Assert.assertTrue(simpleTableFormExtensionData1.isConstructor());
    Assert.assertEquals("annotation count", 0, simpleTableFormExtensionData1.annotations().list().size());
    IMethod getMyExtension = SdkAssert.assertMethodExist(simpleTableFormExtensionData, "getMyExtension", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getMyExtension, "Ljava.math.BigDecimal;");
    Assert.assertEquals("annotation count", 0, getMyExtension.annotations().list().size());
    IMethod setMyExtension = SdkAssert.assertMethodExist(simpleTableFormExtensionData, "setMyExtension", new String[]{"Ljava.math.BigDecimal;"});
    SdkAssert.assertMethodReturnTypeSignature(setMyExtension, "V");
    Assert.assertEquals("annotation count", 0, setMyExtension.annotations().list().size());

    Assert.assertEquals("inner types count of 'SimpleTableFormExtensionData'", 0, simpleTableFormExtensionData.innerTypes().list().size());
  }

  private static void testApiOfMultiColumnExtensionData(IType multiColumnExtensionData) {
    // type MultiColumnExtensionData
    SdkAssert.assertHasFlags(multiColumnExtensionData, 1);
    SdkAssert.assertHasSuperIntefaceSignatures(multiColumnExtensionData, new String[]{"QSerializable;"});
    SdkAssert.assertAnnotation(multiColumnExtensionData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(multiColumnExtensionData, "javax.annotation.Generated");

    // fields of MultiColumnExtensionData
    Assert.assertEquals("field count of 'MultiColumnExtensionData'", 5, multiColumnExtensionData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(multiColumnExtensionData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    IField thirdLong = SdkAssert.assertFieldExist(multiColumnExtensionData, "thirdLong");
    SdkAssert.assertHasFlags(thirdLong, 25);
    SdkAssert.assertFieldSignature(thirdLong, "QString;");
    IField fourthDouble = SdkAssert.assertFieldExist(multiColumnExtensionData, "fourthDouble");
    SdkAssert.assertHasFlags(fourthDouble, 25);
    SdkAssert.assertFieldSignature(fourthDouble, "QString;");
    IField m_thirdLong = SdkAssert.assertFieldExist(multiColumnExtensionData, "m_thirdLong");
    SdkAssert.assertHasFlags(m_thirdLong, 2);
    SdkAssert.assertFieldSignature(m_thirdLong, "QLong;");
    IField m_fourthDouble = SdkAssert.assertFieldExist(multiColumnExtensionData, "m_fourthDouble");
    SdkAssert.assertHasFlags(m_fourthDouble, 2);
    SdkAssert.assertFieldSignature(m_fourthDouble, "QBigDecimal;");

    Assert.assertEquals("method count of 'MultiColumnExtensionData'", 5, multiColumnExtensionData.methods().list().size());
    IMethod multiColumnExtensionData1 = SdkAssert.assertMethodExist(multiColumnExtensionData, "MultiColumnExtensionData", new String[]{});
    Assert.assertTrue(multiColumnExtensionData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(multiColumnExtensionData1, null);
    IMethod getThirdLong = SdkAssert.assertMethodExist(multiColumnExtensionData, "getThirdLong", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdLong, "QLong;");
    IMethod setThirdLong = SdkAssert.assertMethodExist(multiColumnExtensionData, "setThirdLong", new String[]{"QLong;"});
    SdkAssert.assertMethodReturnTypeSignature(setThirdLong, "V");
    IMethod getFourthDouble = SdkAssert.assertMethodExist(multiColumnExtensionData, "getFourthDouble", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFourthDouble, "QBigDecimal;");
    IMethod setFourthDouble = SdkAssert.assertMethodExist(multiColumnExtensionData, "setFourthDouble", new String[]{"QBigDecimal;"});
    SdkAssert.assertMethodReturnTypeSignature(setFourthDouble, "V");

    Assert.assertEquals("inner types count of 'MultiColumnExtensionData'", 0, multiColumnExtensionData.innerTypes().list().size());
  }

  private static void testApiOfPropertyExtensionData(IType propertyExtensionData) {
    // type PropertyExtensionData
    SdkAssert.assertHasFlags(propertyExtensionData, 1);
    SdkAssert.assertHasSuperTypeSignature(propertyExtensionData, "QAbstractFormFieldData;");
    SdkAssert.assertAnnotation(propertyExtensionData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(propertyExtensionData, "javax.annotation.Generated");

    // fields of PropertyExtensionData
    Assert.assertEquals("field count of 'PropertyExtensionData'", 1, propertyExtensionData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(propertyExtensionData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'PropertyExtensionData'", 4, propertyExtensionData.methods().list().size());
    IMethod propertyExtensionData1 = SdkAssert.assertMethodExist(propertyExtensionData, "PropertyExtensionData", new String[]{});
    Assert.assertTrue(propertyExtensionData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(propertyExtensionData1, null);
    IMethod getLongValue = SdkAssert.assertMethodExist(propertyExtensionData, "getLongValue", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getLongValue, "QLong;");
    IMethod setLongValue = SdkAssert.assertMethodExist(propertyExtensionData, "setLongValue", new String[]{"QLong;"});
    SdkAssert.assertMethodReturnTypeSignature(setLongValue, "V");
    IMethod getLongValueProperty = SdkAssert.assertMethodExist(propertyExtensionData, "getLongValueProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getLongValueProperty, "QLongValueProperty;");

    Assert.assertEquals("inner types count of 'PropertyExtensionData'", 1, propertyExtensionData.innerTypes().list().size());
    // type LongValueProperty
    IType longValueProperty = SdkAssert.assertTypeExists(propertyExtensionData, "LongValueProperty");
    SdkAssert.assertHasFlags(longValueProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(longValueProperty, "QAbstractPropertyData<QLong;>;");

    // fields of LongValueProperty
    Assert.assertEquals("field count of 'LongValueProperty'", 1, longValueProperty.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(longValueProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'LongValueProperty'", 1, longValueProperty.methods().list().size());
    IMethod longValueProperty1 = SdkAssert.assertMethodExist(longValueProperty, "LongValueProperty", new String[]{});
    Assert.assertTrue(longValueProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(longValueProperty1, null);

    Assert.assertEquals("inner types count of 'LongValueProperty'", 0, longValueProperty.innerTypes().list().size());
  }

  private static void testApiOfThirdIntegerColumnData(IType thirdIntegerColumnData) {
    // type ThirdIntegerColumnData
    SdkAssert.assertHasFlags(thirdIntegerColumnData, 1);
    SdkAssert.assertHasSuperIntefaceSignatures(thirdIntegerColumnData, new String[]{"QSerializable;"});
    SdkAssert.assertAnnotation(thirdIntegerColumnData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(thirdIntegerColumnData, "javax.annotation.Generated");

    // fields of ThirdIntegerColumnData
    Assert.assertEquals("field count of 'ThirdIntegerColumnData'", 3, thirdIntegerColumnData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(thirdIntegerColumnData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    IField thirdInteger = SdkAssert.assertFieldExist(thirdIntegerColumnData, "thirdInteger");
    SdkAssert.assertHasFlags(thirdInteger, 25);
    SdkAssert.assertFieldSignature(thirdInteger, "QString;");
    IField m_thirdInteger = SdkAssert.assertFieldExist(thirdIntegerColumnData, "m_thirdInteger");
    SdkAssert.assertHasFlags(m_thirdInteger, 2);
    SdkAssert.assertFieldSignature(m_thirdInteger, "QInteger;");

    Assert.assertEquals("method count of 'ThirdIntegerColumnData'", 3, thirdIntegerColumnData.methods().list().size());
    IMethod thirdIntegerColumnData1 = SdkAssert.assertMethodExist(thirdIntegerColumnData, "ThirdIntegerColumnData", new String[]{});
    Assert.assertTrue(thirdIntegerColumnData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdIntegerColumnData1, null);
    IMethod getThirdInteger = SdkAssert.assertMethodExist(thirdIntegerColumnData, "getThirdInteger", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdInteger, "QInteger;");
    IMethod setThirdInteger = SdkAssert.assertMethodExist(thirdIntegerColumnData, "setThirdInteger", new String[]{"QInteger;"});
    SdkAssert.assertMethodReturnTypeSignature(setThirdInteger, "V");

    Assert.assertEquals("inner types count of 'ThirdIntegerColumnData'", 0, thirdIntegerColumnData.innerTypes().list().size());
  }

  private static void testApiOfMultipleExtGroupBoxExtensionData(IType multipleExtGroupBoxExtensionData) {
    // type MultipleExtGroupBoxExtensionData
    SdkAssert.assertHasFlags(multipleExtGroupBoxExtensionData, 1);
    SdkAssert.assertHasSuperTypeSignature(multipleExtGroupBoxExtensionData, "QAbstractFormFieldData;");
    SdkAssert.assertAnnotation(multipleExtGroupBoxExtensionData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(multipleExtGroupBoxExtensionData, "javax.annotation.Generated");

    // fields of MultipleExtGroupBoxExtensionData
    Assert.assertEquals("field count of 'MultipleExtGroupBoxExtensionData'", 1, multipleExtGroupBoxExtensionData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(multipleExtGroupBoxExtensionData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'MultipleExtGroupBoxExtensionData'", 3, multipleExtGroupBoxExtensionData.methods().list().size());
    IMethod multipleExtGroupBoxExtensionData1 = SdkAssert.assertMethodExist(multipleExtGroupBoxExtensionData, "MultipleExtGroupBoxExtensionData", new String[]{});
    Assert.assertTrue(multipleExtGroupBoxExtensionData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(multipleExtGroupBoxExtensionData1, null);
    IMethod getSecondDouble = SdkAssert.assertMethodExist(multipleExtGroupBoxExtensionData, "getSecondDouble", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecondDouble, "QSecondDouble;");
    IMethod getThirdDate = SdkAssert.assertMethodExist(multipleExtGroupBoxExtensionData, "getThirdDate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdDate, "QThirdDate;");

    Assert.assertEquals("inner types count of 'MultipleExtGroupBoxExtensionData'", 2, multipleExtGroupBoxExtensionData.innerTypes().list().size());
    // type SecondDouble
    IType secondDouble = SdkAssert.assertTypeExists(multipleExtGroupBoxExtensionData, "SecondDouble");
    SdkAssert.assertHasFlags(secondDouble, 9);
    SdkAssert.assertHasSuperTypeSignature(secondDouble, "QAbstractValueFieldData<QBigDecimal;>;");

    // fields of SecondDouble
    Assert.assertEquals("field count of 'SecondDouble'", 1, secondDouble.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(secondDouble, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'SecondDouble'", 1, secondDouble.methods().list().size());
    IMethod secondDouble1 = SdkAssert.assertMethodExist(secondDouble, "SecondDouble", new String[]{});
    Assert.assertTrue(secondDouble1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(secondDouble1, null);

    Assert.assertEquals("inner types count of 'SecondDouble'", 0, secondDouble.innerTypes().list().size());
    // type ThirdDate
    IType thirdDate = SdkAssert.assertTypeExists(multipleExtGroupBoxExtensionData, "ThirdDate");
    SdkAssert.assertHasFlags(thirdDate, 9);
    SdkAssert.assertHasSuperTypeSignature(thirdDate, "QAbstractValueFieldData<QDate;>;");

    // fields of ThirdDate
    Assert.assertEquals("field count of 'ThirdDate'", 1, thirdDate.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(thirdDate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'ThirdDate'", 1, thirdDate.methods().list().size());
    IMethod thirdDate1 = SdkAssert.assertMethodExist(thirdDate, "ThirdDate", new String[]{});
    Assert.assertTrue(thirdDate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdDate1, null);

    Assert.assertEquals("inner types count of 'ThirdDate'", 0, thirdDate.innerTypes().list().size());
  }

}
