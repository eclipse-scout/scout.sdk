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
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.dto.test.util.CoreScoutTestingUtils;
import org.junit.Test;

/**
 * <h3>{@link ExtensionFormDataTest}</h3>
 *
 * @author Matthias Villiger
 * @since 4.2.0
 */
public class ExtensionFormDataTest {
  @Test
  public void testFormPropertyExtension() throws Exception {
    String extensionName = "FormPropertyExtension";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.extensions." + extensionName);
    testApiOfPropertyExtensionData(dto);
  }

  @Test
  public void testMultiColumnExtension() throws Exception {
    String extensionName = "MultiColumnExtension";
    IType dto = CoreScoutTestingUtils.createRowDataAssertNoCompileErrors("formdata.client.extensions." + extensionName);
    testApiOfMultiColumnExtensionData(dto);
  }

  @Test
  public void testMultipleExtGroupBoxExtension() throws Exception {
    String formName = "MultipleExtGroupBoxExtension";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.extensions." + formName);
    testApiOfMultipleExtGroupBoxExtensionData(dto);
  }

  @Test
  public void testThirdIntegerColumn() throws Exception {
    String extensionName = "ThirdIntegerColumn";
    IType dto = CoreScoutTestingUtils.createRowDataAssertNoCompileErrors("formdata.client.extensions." + extensionName);
    testApiOfThirdIntegerColumnData(dto);
  }

  private void testApiOfMultiColumnExtensionData(IType multiColumnExtensionData) throws Exception {
    // type MultiColumnExtensionData
    SdkAssert.assertHasFlags(multiColumnExtensionData, 1);
    SdkAssert.assertHasSuperIntefaceSignatures(multiColumnExtensionData, new String[]{"QSerializable;"});
    SdkAssert.assertAnnotation(multiColumnExtensionData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(multiColumnExtensionData, "javax.annotation.Generated");

    // fields of MultiColumnExtensionData
    SdkAssert.assertEquals("field count of 'MultiColumnExtensionData'", 5, multiColumnExtensionData.getFields().size());
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

    SdkAssert.assertEquals("method count of 'MultiColumnExtensionData'", 5, multiColumnExtensionData.getMethods().size());
    IMethod multiColumnExtensionData1 = SdkAssert.assertMethodExist(multiColumnExtensionData, "MultiColumnExtensionData", new String[]{});
    SdkAssert.assertTrue(multiColumnExtensionData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(multiColumnExtensionData1, null);
    IMethod getThirdLong = SdkAssert.assertMethodExist(multiColumnExtensionData, "getThirdLong", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdLong, "QLong;");
    IMethod setThirdLong = SdkAssert.assertMethodExist(multiColumnExtensionData, "setThirdLong", new String[]{"QLong;"});
    SdkAssert.assertMethodReturnTypeSignature(setThirdLong, "V");
    IMethod getFourthDouble = SdkAssert.assertMethodExist(multiColumnExtensionData, "getFourthDouble", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFourthDouble, "QBigDecimal;");
    IMethod setFourthDouble = SdkAssert.assertMethodExist(multiColumnExtensionData, "setFourthDouble", new String[]{"QBigDecimal;"});
    SdkAssert.assertMethodReturnTypeSignature(setFourthDouble, "V");

    SdkAssert.assertEquals("inner types count of 'MultiColumnExtensionData'", 0, multiColumnExtensionData.getTypes().size());
  }

  private void testApiOfPropertyExtensionData(IType propertyExtensionData) throws Exception {
    // type PropertyExtensionData
    SdkAssert.assertHasFlags(propertyExtensionData, 1);
    SdkAssert.assertHasSuperTypeSignature(propertyExtensionData, "QAbstractFormFieldData;");
    SdkAssert.assertAnnotation(propertyExtensionData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(propertyExtensionData, "javax.annotation.Generated");

    // fields of PropertyExtensionData
    SdkAssert.assertEquals("field count of 'PropertyExtensionData'", 1, propertyExtensionData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(propertyExtensionData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'PropertyExtensionData'", 4, propertyExtensionData.getMethods().size());
    IMethod propertyExtensionData1 = SdkAssert.assertMethodExist(propertyExtensionData, "PropertyExtensionData", new String[]{});
    SdkAssert.assertTrue(propertyExtensionData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(propertyExtensionData1, null);
    IMethod getLongValue = SdkAssert.assertMethodExist(propertyExtensionData, "getLongValue", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getLongValue, "QLong;");
    IMethod setLongValue = SdkAssert.assertMethodExist(propertyExtensionData, "setLongValue", new String[]{"QLong;"});
    SdkAssert.assertMethodReturnTypeSignature(setLongValue, "V");
    IMethod getLongValueProperty = SdkAssert.assertMethodExist(propertyExtensionData, "getLongValueProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getLongValueProperty, "QLongValueProperty;");

    SdkAssert.assertEquals("inner types count of 'PropertyExtensionData'", 1, propertyExtensionData.getTypes().size());
    // type LongValueProperty
    IType longValueProperty = SdkAssert.assertTypeExists(propertyExtensionData, "LongValueProperty");
    SdkAssert.assertHasFlags(longValueProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(longValueProperty, "QAbstractPropertyData<QLong;>;");

    // fields of LongValueProperty
    SdkAssert.assertEquals("field count of 'LongValueProperty'", 1, longValueProperty.getFields().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(longValueProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'LongValueProperty'", 1, longValueProperty.getMethods().size());
    IMethod longValueProperty1 = SdkAssert.assertMethodExist(longValueProperty, "LongValueProperty", new String[]{});
    SdkAssert.assertTrue(longValueProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(longValueProperty1, null);

    SdkAssert.assertEquals("inner types count of 'LongValueProperty'", 0, longValueProperty.getTypes().size());
  }

  private void testApiOfThirdIntegerColumnData(IType thirdIntegerColumnData) throws Exception {
    // type ThirdIntegerColumnData
    SdkAssert.assertHasFlags(thirdIntegerColumnData, 1);
    SdkAssert.assertHasSuperIntefaceSignatures(thirdIntegerColumnData, new String[]{"QSerializable;"});
    SdkAssert.assertAnnotation(thirdIntegerColumnData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(thirdIntegerColumnData, "javax.annotation.Generated");

    // fields of ThirdIntegerColumnData
    SdkAssert.assertEquals("field count of 'ThirdIntegerColumnData'", 3, thirdIntegerColumnData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(thirdIntegerColumnData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    IField thirdInteger = SdkAssert.assertFieldExist(thirdIntegerColumnData, "thirdInteger");
    SdkAssert.assertHasFlags(thirdInteger, 25);
    SdkAssert.assertFieldSignature(thirdInteger, "QString;");
    IField m_thirdInteger = SdkAssert.assertFieldExist(thirdIntegerColumnData, "m_thirdInteger");
    SdkAssert.assertHasFlags(m_thirdInteger, 2);
    SdkAssert.assertFieldSignature(m_thirdInteger, "QInteger;");

    SdkAssert.assertEquals("method count of 'ThirdIntegerColumnData'", 3, thirdIntegerColumnData.getMethods().size());
    IMethod thirdIntegerColumnData1 = SdkAssert.assertMethodExist(thirdIntegerColumnData, "ThirdIntegerColumnData", new String[]{});
    SdkAssert.assertTrue(thirdIntegerColumnData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdIntegerColumnData1, null);
    IMethod getThirdInteger = SdkAssert.assertMethodExist(thirdIntegerColumnData, "getThirdInteger", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdInteger, "QInteger;");
    IMethod setThirdInteger = SdkAssert.assertMethodExist(thirdIntegerColumnData, "setThirdInteger", new String[]{"QInteger;"});
    SdkAssert.assertMethodReturnTypeSignature(setThirdInteger, "V");

    SdkAssert.assertEquals("inner types count of 'ThirdIntegerColumnData'", 0, thirdIntegerColumnData.getTypes().size());
  }

  private void testApiOfMultipleExtGroupBoxExtensionData(IType multipleExtGroupBoxExtensionData) throws Exception {
    // type MultipleExtGroupBoxExtensionData
    SdkAssert.assertHasFlags(multipleExtGroupBoxExtensionData, 1);
    SdkAssert.assertHasSuperTypeSignature(multipleExtGroupBoxExtensionData, "QAbstractFormFieldData;");
    SdkAssert.assertAnnotation(multipleExtGroupBoxExtensionData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(multipleExtGroupBoxExtensionData, "javax.annotation.Generated");

    // fields of MultipleExtGroupBoxExtensionData
    SdkAssert.assertEquals("field count of 'MultipleExtGroupBoxExtensionData'", 1, multipleExtGroupBoxExtensionData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(multipleExtGroupBoxExtensionData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'MultipleExtGroupBoxExtensionData'", 3, multipleExtGroupBoxExtensionData.getMethods().size());
    IMethod multipleExtGroupBoxExtensionData1 = SdkAssert.assertMethodExist(multipleExtGroupBoxExtensionData, "MultipleExtGroupBoxExtensionData", new String[]{});
    SdkAssert.assertTrue(multipleExtGroupBoxExtensionData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(multipleExtGroupBoxExtensionData1, null);
    IMethod getSecondDouble = SdkAssert.assertMethodExist(multipleExtGroupBoxExtensionData, "getSecondDouble", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecondDouble, "QSecondDouble;");
    IMethod getThirdDate = SdkAssert.assertMethodExist(multipleExtGroupBoxExtensionData, "getThirdDate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdDate, "QThirdDate;");

    SdkAssert.assertEquals("inner types count of 'MultipleExtGroupBoxExtensionData'", 2, multipleExtGroupBoxExtensionData.getTypes().size());
    // type SecondDouble
    IType secondDouble = SdkAssert.assertTypeExists(multipleExtGroupBoxExtensionData, "SecondDouble");
    SdkAssert.assertHasFlags(secondDouble, 9);
    SdkAssert.assertHasSuperTypeSignature(secondDouble, "QAbstractValueFieldData<QBigDecimal;>;");

    // fields of SecondDouble
    SdkAssert.assertEquals("field count of 'SecondDouble'", 1, secondDouble.getFields().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(secondDouble, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'SecondDouble'", 1, secondDouble.getMethods().size());
    IMethod secondDouble1 = SdkAssert.assertMethodExist(secondDouble, "SecondDouble", new String[]{});
    SdkAssert.assertTrue(secondDouble1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(secondDouble1, null);

    SdkAssert.assertEquals("inner types count of 'SecondDouble'", 0, secondDouble.getTypes().size());
    // type ThirdDate
    IType thirdDate = SdkAssert.assertTypeExists(multipleExtGroupBoxExtensionData, "ThirdDate");
    SdkAssert.assertHasFlags(thirdDate, 9);
    SdkAssert.assertHasSuperTypeSignature(thirdDate, "QAbstractValueFieldData<QDate;>;");

    // fields of ThirdDate
    SdkAssert.assertEquals("field count of 'ThirdDate'", 1, thirdDate.getFields().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(thirdDate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'ThirdDate'", 1, thirdDate.getMethods().size());
    IMethod thirdDate1 = SdkAssert.assertMethodExist(thirdDate, "ThirdDate", new String[]{});
    SdkAssert.assertTrue(thirdDate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdDate1, null);

    SdkAssert.assertEquals("inner types count of 'ThirdDate'", 0, thirdDate.getTypes().size());
  }

}
