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

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataDtoUpdateOperation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.RowDataDtoUpdateOperation;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link ExtensionFormDataTest}</h3>
 *
 * @author Matthias Villiger
 * @since 4.2.0
 */
public class ExtensionFormDataTest extends AbstractSdkTestWithFormDataProject {
  @Test
  public void testFormPropertyExtension() throws Exception {
    String extensionName = "FormPropertyExtension";
    IType extension = TypeUtility.getType("formdata.client.extensions." + extensionName);
    Assert.assertTrue(TypeUtility.exists(extension));

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(extension);

    executeBuildAssertNoCompileErrors(op);

    testApiOfPropertyExtensionData();
  }

  @Test
  public void testMultiColumnExtension() throws Exception {
    String extensionName = "MultiColumnExtension";
    IType extension = TypeUtility.getType("formdata.client.extensions." + extensionName);
    Assert.assertTrue(TypeUtility.exists(extension));

    RowDataDtoUpdateOperation op = new RowDataDtoUpdateOperation(extension);

    executeBuildAssertNoCompileErrors(op);

    testApiOfMultiColumnExtensionData();
  }

  @Test
  public void testMultipleExtGroupBoxExtension() throws Exception {
    String formName = "MultipleExtGroupBoxExtension";
    IType form = TypeUtility.getType("formdata.client.extensions." + formName);
    Assert.assertTrue(TypeUtility.exists(form));

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(form);

    executeBuildAssertNoCompileErrors(op);

    testApiOfMultipleExtGroupBoxExtensionData();
  }

  @Test
  public void testThirdIntegerColumn() throws Exception {
    String extensionName = "ThirdIntegerColumn";
    IType extension = TypeUtility.getType("formdata.client.extensions." + extensionName);
    Assert.assertTrue(TypeUtility.exists(extension));

    RowDataDtoUpdateOperation op = new RowDataDtoUpdateOperation(extension);

    executeBuildAssertNoCompileErrors(op);

    testApiOfThirdIntegerColumnData();
  }

  private void testApiOfMultiColumnExtensionData() throws Exception {
    // type MultiColumnExtensionData
    IType multiColumnExtensionData = SdkAssert.assertTypeExists("formdata.shared.extension.MultiColumnExtensionData");
    SdkAssert.assertHasFlags(multiColumnExtensionData, 1);
    SdkAssert.assertHasSuperIntefaceSignatures(multiColumnExtensionData, new String[]{"QSerializable;"});
    SdkAssert.assertAnnotation(multiColumnExtensionData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(multiColumnExtensionData, "javax.annotation.Generated");

    // fields of MultiColumnExtensionData
    SdkAssert.assertEquals("field count of 'MultiColumnExtensionData'", 5, multiColumnExtensionData.getFields().length);
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
    SdkAssert.assertFieldSignature(m_fourthDouble, "QDouble;");

    SdkAssert.assertEquals("method count of 'MultiColumnExtensionData'", 5, multiColumnExtensionData.getMethods().length);
    IMethod multiColumnExtensionData1 = SdkAssert.assertMethodExist(multiColumnExtensionData, "MultiColumnExtensionData", new String[]{});
    SdkAssert.assertTrue(multiColumnExtensionData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(multiColumnExtensionData1, "V");
    IMethod getThirdLong = SdkAssert.assertMethodExist(multiColumnExtensionData, "getThirdLong", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdLong, "QLong;");
    IMethod setThirdLong = SdkAssert.assertMethodExist(multiColumnExtensionData, "setThirdLong", new String[]{"QLong;"});
    SdkAssert.assertMethodReturnTypeSignature(setThirdLong, "V");
    IMethod getFourthDouble = SdkAssert.assertMethodExist(multiColumnExtensionData, "getFourthDouble", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFourthDouble, "QDouble;");
    IMethod setFourthDouble = SdkAssert.assertMethodExist(multiColumnExtensionData, "setFourthDouble", new String[]{"QDouble;"});
    SdkAssert.assertMethodReturnTypeSignature(setFourthDouble, "V");

    SdkAssert.assertEquals("inner types count of 'MultiColumnExtensionData'", 0, multiColumnExtensionData.getTypes().length);
  }

  private void testApiOfPropertyExtensionData() throws Exception {
    // type PropertyExtensionData
    IType propertyExtensionData = SdkAssert.assertTypeExists("formdata.shared.extension.PropertyExtensionData");
    SdkAssert.assertHasFlags(propertyExtensionData, 1);
    SdkAssert.assertHasSuperTypeSignature(propertyExtensionData, "QAbstractFormFieldData;");
    SdkAssert.assertAnnotation(propertyExtensionData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(propertyExtensionData, "javax.annotation.Generated");

    // fields of PropertyExtensionData
    SdkAssert.assertEquals("field count of 'PropertyExtensionData'", 1, propertyExtensionData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(propertyExtensionData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'PropertyExtensionData'", 4, propertyExtensionData.getMethods().length);
    IMethod propertyExtensionData1 = SdkAssert.assertMethodExist(propertyExtensionData, "PropertyExtensionData", new String[]{});
    SdkAssert.assertTrue(propertyExtensionData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(propertyExtensionData1, "V");
    IMethod getLongValue = SdkAssert.assertMethodExist(propertyExtensionData, "getLongValue", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getLongValue, "QLong;");
    IMethod setLongValue = SdkAssert.assertMethodExist(propertyExtensionData, "setLongValue", new String[]{"QLong;"});
    SdkAssert.assertMethodReturnTypeSignature(setLongValue, "V");
    IMethod getLongValueProperty = SdkAssert.assertMethodExist(propertyExtensionData, "getLongValueProperty", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getLongValueProperty, "QLongValueProperty;");

    SdkAssert.assertEquals("inner types count of 'PropertyExtensionData'", 1, propertyExtensionData.getTypes().length);
    // type LongValueProperty
    IType longValueProperty = SdkAssert.assertTypeExists(propertyExtensionData, "LongValueProperty");
    SdkAssert.assertHasFlags(longValueProperty, 9);
    SdkAssert.assertHasSuperTypeSignature(longValueProperty, "QAbstractPropertyData<QLong;>;");

    // fields of LongValueProperty
    SdkAssert.assertEquals("field count of 'LongValueProperty'", 1, longValueProperty.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(longValueProperty, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'LongValueProperty'", 1, longValueProperty.getMethods().length);
    IMethod longValueProperty1 = SdkAssert.assertMethodExist(longValueProperty, "LongValueProperty", new String[]{});
    SdkAssert.assertTrue(longValueProperty1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(longValueProperty1, "V");

    SdkAssert.assertEquals("inner types count of 'LongValueProperty'", 0, longValueProperty.getTypes().length);
  }

  private void testApiOfThirdIntegerColumnData() throws Exception {
    // type ThirdIntegerColumnData
    IType thirdIntegerColumnData = SdkAssert.assertTypeExists("formdata.shared.extension.ThirdIntegerColumnData");
    SdkAssert.assertHasFlags(thirdIntegerColumnData, 1);
    SdkAssert.assertHasSuperIntefaceSignatures(thirdIntegerColumnData, new String[]{"QSerializable;"});
    SdkAssert.assertAnnotation(thirdIntegerColumnData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(thirdIntegerColumnData, "javax.annotation.Generated");

    // fields of ThirdIntegerColumnData
    SdkAssert.assertEquals("field count of 'ThirdIntegerColumnData'", 3, thirdIntegerColumnData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(thirdIntegerColumnData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    IField thirdInteger = SdkAssert.assertFieldExist(thirdIntegerColumnData, "thirdInteger");
    SdkAssert.assertHasFlags(thirdInteger, 25);
    SdkAssert.assertFieldSignature(thirdInteger, "QString;");
    IField m_thirdInteger = SdkAssert.assertFieldExist(thirdIntegerColumnData, "m_thirdInteger");
    SdkAssert.assertHasFlags(m_thirdInteger, 2);
    SdkAssert.assertFieldSignature(m_thirdInteger, "QInteger;");

    SdkAssert.assertEquals("method count of 'ThirdIntegerColumnData'", 3, thirdIntegerColumnData.getMethods().length);
    IMethod thirdIntegerColumnData1 = SdkAssert.assertMethodExist(thirdIntegerColumnData, "ThirdIntegerColumnData", new String[]{});
    SdkAssert.assertTrue(thirdIntegerColumnData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdIntegerColumnData1, "V");
    IMethod getThirdInteger = SdkAssert.assertMethodExist(thirdIntegerColumnData, "getThirdInteger", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdInteger, "QInteger;");
    IMethod setThirdInteger = SdkAssert.assertMethodExist(thirdIntegerColumnData, "setThirdInteger", new String[]{"QInteger;"});
    SdkAssert.assertMethodReturnTypeSignature(setThirdInteger, "V");

    SdkAssert.assertEquals("inner types count of 'ThirdIntegerColumnData'", 0, thirdIntegerColumnData.getTypes().length);
  }

  private void testApiOfMultipleExtGroupBoxExtensionData() throws Exception {
    // type MultipleExtGroupBoxExtensionData
    IType multipleExtGroupBoxExtensionData = SdkAssert.assertTypeExists("formdata.shared.extension.MultipleExtGroupBoxExtensionData");
    SdkAssert.assertHasFlags(multipleExtGroupBoxExtensionData, 1);
    SdkAssert.assertHasSuperTypeSignature(multipleExtGroupBoxExtensionData, "QAbstractFormFieldData;");
    SdkAssert.assertAnnotation(multipleExtGroupBoxExtensionData, "org.eclipse.scout.commons.annotations.Extends");
    SdkAssert.assertAnnotation(multipleExtGroupBoxExtensionData, "javax.annotation.Generated");

    // fields of MultipleExtGroupBoxExtensionData
    SdkAssert.assertEquals("field count of 'MultipleExtGroupBoxExtensionData'", 1, multipleExtGroupBoxExtensionData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(multipleExtGroupBoxExtensionData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'MultipleExtGroupBoxExtensionData'", 3, multipleExtGroupBoxExtensionData.getMethods().length);
    IMethod multipleExtGroupBoxExtensionData1 = SdkAssert.assertMethodExist(multipleExtGroupBoxExtensionData, "MultipleExtGroupBoxExtensionData", new String[]{});
    SdkAssert.assertTrue(multipleExtGroupBoxExtensionData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(multipleExtGroupBoxExtensionData1, "V");
    IMethod getSecondDouble = SdkAssert.assertMethodExist(multipleExtGroupBoxExtensionData, "getSecondDouble", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecondDouble, "QSecondDouble;");
    IMethod getThirdDate = SdkAssert.assertMethodExist(multipleExtGroupBoxExtensionData, "getThirdDate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThirdDate, "QThirdDate;");

    SdkAssert.assertEquals("inner types count of 'MultipleExtGroupBoxExtensionData'", 2, multipleExtGroupBoxExtensionData.getTypes().length);
    // type SecondDouble
    IType secondDouble = SdkAssert.assertTypeExists(multipleExtGroupBoxExtensionData, "SecondDouble");
    SdkAssert.assertHasFlags(secondDouble, 9);
    SdkAssert.assertHasSuperTypeSignature(secondDouble, "QAbstractValueFieldData<QDouble;>;");

    // fields of SecondDouble
    SdkAssert.assertEquals("field count of 'SecondDouble'", 1, secondDouble.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(secondDouble, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'SecondDouble'", 2, secondDouble.getMethods().length);
    IMethod secondDouble1 = SdkAssert.assertMethodExist(secondDouble, "SecondDouble", new String[]{});
    SdkAssert.assertTrue(secondDouble1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(secondDouble1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(secondDouble, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_VALUE, Double.MAX_VALUE);", "ruleMap.put(ValidationRule.MIN_VALUE, -Double.MAX_VALUE);"}, true);

    SdkAssert.assertEquals("inner types count of 'SecondDouble'", 0, secondDouble.getTypes().length);
    // type ThirdDate
    IType thirdDate = SdkAssert.assertTypeExists(multipleExtGroupBoxExtensionData, "ThirdDate");
    SdkAssert.assertHasFlags(thirdDate, 9);
    SdkAssert.assertHasSuperTypeSignature(thirdDate, "QAbstractValueFieldData<QDate;>;");

    // fields of ThirdDate
    SdkAssert.assertEquals("field count of 'ThirdDate'", 1, thirdDate.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(thirdDate, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'ThirdDate'", 1, thirdDate.getMethods().length);
    IMethod thirdDate1 = SdkAssert.assertMethodExist(thirdDate, "ThirdDate", new String[]{});
    SdkAssert.assertTrue(thirdDate1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(thirdDate1, "V");

    SdkAssert.assertEquals("inner types count of 'ThirdDate'", 0, thirdDate.getTypes().length);
  }

}
