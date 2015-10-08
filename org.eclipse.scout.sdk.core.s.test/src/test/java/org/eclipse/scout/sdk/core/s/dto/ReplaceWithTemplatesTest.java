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
package org.eclipse.scout.sdk.core.s.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since 3.10.0-M5
 */
public class ReplaceWithTemplatesTest {

  private static final String TemplateBoxFqn = "formdata.client.ui.template.formfield.replace.AbstractTemplateForReplaceBox";
  private static final String TemplateBasedFormFqn = "formdata.client.ui.template.formfield.replace.TemplateBasedForm";
  private static final String TemplateRadioButtonGroupWithFieldsFqn = "formdata.client.ui.template.formfield.replace.AbstractRadioButtonGroupWithFields";
  private static final String RadioButtonFormFqn = "formdata.client.ui.template.formfield.replace.RadioButtonForm";

  @Test
  public void testTemplateBoxData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(TemplateBoxFqn);
    testApiOfAbstractTemplateForReplaceBoxData(dto);
  }

  @Test
  public void testTemplateBasedFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(TemplateBasedFormFqn);
    testApiOfTemplateBasedFormData(dto);
  }

  @Test
  public void testTemplateRadioButtonGroupWithFields() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(TemplateRadioButtonGroupWithFieldsFqn);
    testApiOfAbstractRadioButtonGroupWithFieldsData(dto);
  }

  @Test
  public void testRadioButtonForm() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(RadioButtonFormFqn);
    testApiOfRadioButtonFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractTemplateForReplaceBoxData(IType abstractTemplateForReplaceBoxData) {
    // type AbstractTemplateForReplaceBoxData
    SdkAssert.assertHasFlags(abstractTemplateForReplaceBoxData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractTemplateForReplaceBoxData, "QAbstractFormFieldData;");
    SdkAssert.assertAnnotation(abstractTemplateForReplaceBoxData, "javax.annotation.Generated");

    // fields of AbstractTemplateForReplaceBoxData
    Assert.assertEquals("field count of 'AbstractTemplateForReplaceBoxData'", 1, abstractTemplateForReplaceBoxData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractTemplateForReplaceBoxData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'AbstractTemplateForReplaceBoxData'", 4, abstractTemplateForReplaceBoxData.methods().list().size());
    IMethod abstractTemplateForReplaceBoxData1 = SdkAssert.assertMethodExist(abstractTemplateForReplaceBoxData, "AbstractTemplateForReplaceBoxData", new String[]{});
    Assert.assertTrue(abstractTemplateForReplaceBoxData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractTemplateForReplaceBoxData1, null);
    IMethod getTemplateBoxString = SdkAssert.assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateBoxString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTemplateBoxString, "QTemplateBoxString;");
    IMethod getTemplateString = SdkAssert.assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTemplateString, "QTemplateString;");
    IMethod getTemplateTable = SdkAssert.assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTemplateTable, "QTemplateTable;");

    Assert.assertEquals("inner types count of 'AbstractTemplateForReplaceBoxData'", 3, abstractTemplateForReplaceBoxData.innerTypes().list().size());
    // type TemplateBoxString
    IType templateBoxString = SdkAssert.assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateBoxString");
    SdkAssert.assertHasFlags(templateBoxString, 9);
    SdkAssert.assertHasSuperTypeSignature(templateBoxString, "QAbstractValueFieldData<QString;>;");

    // fields of TemplateBoxString
    Assert.assertEquals("field count of 'TemplateBoxString'", 1, templateBoxString.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(templateBoxString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'TemplateBoxString'", 1, templateBoxString.methods().list().size());
    IMethod templateBoxString1 = SdkAssert.assertMethodExist(templateBoxString, "TemplateBoxString", new String[]{});
    Assert.assertTrue(templateBoxString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(templateBoxString1, null);

    Assert.assertEquals("inner types count of 'TemplateBoxString'", 0, templateBoxString.innerTypes().list().size());
    // type TemplateString
    IType templateString = SdkAssert.assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateString");
    SdkAssert.assertHasFlags(templateString, 9);
    SdkAssert.assertHasSuperTypeSignature(templateString, "QAbstractValueFieldData<QString;>;");

    // fields of TemplateString
    Assert.assertEquals("field count of 'TemplateString'", 1, templateString.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(templateString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'TemplateString'", 1, templateString.methods().list().size());
    IMethod templateString1 = SdkAssert.assertMethodExist(templateString, "TemplateString", new String[]{});
    Assert.assertTrue(templateString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(templateString1, null);

    Assert.assertEquals("inner types count of 'TemplateString'", 0, templateString.innerTypes().list().size());
    // type TemplateTable
    IType templateTable = SdkAssert.assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateTable");
    SdkAssert.assertHasFlags(templateTable, 9);
    SdkAssert.assertHasSuperTypeSignature(templateTable, "QAbstractTableFieldBeanData;");

    // fields of TemplateTable
    Assert.assertEquals("field count of 'TemplateTable'", 1, templateTable.fields().list().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(templateTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'TemplateTable'", 8, templateTable.methods().list().size());
    IMethod templateTable1 = SdkAssert.assertMethodExist(templateTable, "TemplateTable", new String[]{});
    Assert.assertTrue(templateTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(templateTable1, null);
    IMethod addRow = SdkAssert.assertMethodExist(templateTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QTemplateTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(templateTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QTemplateTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(templateTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QTemplateTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(templateTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(templateTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QTemplateTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(templateTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QTemplateTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(templateTable, "setRows", new String[]{"[QTemplateTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    Assert.assertEquals("inner types count of 'TemplateTable'", 1, templateTable.innerTypes().list().size());
    // type TemplateTableRowData
    IType templateTableRowData = SdkAssert.assertTypeExists(templateTable, "TemplateTableRowData");
    SdkAssert.assertHasFlags(templateTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(templateTableRowData, "QAbstractTableRowData;");

    // fields of TemplateTableRowData
    Assert.assertEquals("field count of 'TemplateTableRowData'", 5, templateTableRowData.fields().list().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(templateTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");
    IField first = SdkAssert.assertFieldExist(templateTableRowData, "first");
    SdkAssert.assertHasFlags(first, 25);
    SdkAssert.assertFieldSignature(first, "QString;");
    IField second = SdkAssert.assertFieldExist(templateTableRowData, "second");
    SdkAssert.assertHasFlags(second, 25);
    SdkAssert.assertFieldSignature(second, "QString;");
    IField m_first = SdkAssert.assertFieldExist(templateTableRowData, "m_first");
    SdkAssert.assertHasFlags(m_first, 2);
    SdkAssert.assertFieldSignature(m_first, "QString;");
    IField m_second = SdkAssert.assertFieldExist(templateTableRowData, "m_second");
    SdkAssert.assertHasFlags(m_second, 2);
    SdkAssert.assertFieldSignature(m_second, "QString;");

    Assert.assertEquals("method count of 'TemplateTableRowData'", 5, templateTableRowData.methods().list().size());
    IMethod templateTableRowData1 = SdkAssert.assertMethodExist(templateTableRowData, "TemplateTableRowData", new String[]{});
    Assert.assertTrue(templateTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(templateTableRowData1, null);
    IMethod getFirst = SdkAssert.assertMethodExist(templateTableRowData, "getFirst", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirst, "QString;");
    IMethod setFirst = SdkAssert.assertMethodExist(templateTableRowData, "setFirst", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setFirst, "V");
    IMethod getSecond = SdkAssert.assertMethodExist(templateTableRowData, "getSecond", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecond, "QString;");
    IMethod setSecond = SdkAssert.assertMethodExist(templateTableRowData, "setSecond", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setSecond, "V");

    Assert.assertEquals("inner types count of 'TemplateTableRowData'", 0, templateTableRowData.innerTypes().list().size());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfTemplateBasedFormData(IType templateBasedFormData) {
    // type TemplateBasedFormData
    SdkAssert.assertHasFlags(templateBasedFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(templateBasedFormData, "QAbstractFormData;");
    SdkAssert.assertAnnotation(templateBasedFormData, "javax.annotation.Generated");

    // fields of TemplateBasedFormData
    Assert.assertEquals("field count of 'TemplateBasedFormData'", 1, templateBasedFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(templateBasedFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'TemplateBasedFormData'", 4, templateBasedFormData.methods().list().size());
    IMethod templateBasedFormData1 = SdkAssert.assertMethodExist(templateBasedFormData, "TemplateBasedFormData", new String[]{});
    Assert.assertTrue(templateBasedFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(templateBasedFormData1, null);
    IMethod getUsageOneBox = SdkAssert.assertMethodExist(templateBasedFormData, "getUsageOneBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsageOneBox, "QUsageOneBox;");
    IMethod getUsageTwoBox = SdkAssert.assertMethodExist(templateBasedFormData, "getUsageTwoBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsageTwoBox, "QUsageTwoBox;");
    IMethod getUsualString = SdkAssert.assertMethodExist(templateBasedFormData, "getUsualString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsualString, "QUsualString;");

    Assert.assertEquals("inner types count of 'TemplateBasedFormData'", 3, templateBasedFormData.innerTypes().list().size());
    // type UsageOneBox
    IType usageOneBox = SdkAssert.assertTypeExists(templateBasedFormData, "UsageOneBox");
    SdkAssert.assertHasFlags(usageOneBox, 9);
    SdkAssert.assertHasSuperTypeSignature(usageOneBox, "QAbstractTemplateForReplaceBoxData;");

    // fields of UsageOneBox
    Assert.assertEquals("field count of 'UsageOneBox'", 1, usageOneBox.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(usageOneBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'UsageOneBox'", 2, usageOneBox.methods().list().size());
    IMethod usageOneBox1 = SdkAssert.assertMethodExist(usageOneBox, "UsageOneBox", new String[]{});
    Assert.assertTrue(usageOneBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageOneBox1, null);
    IMethod getUsageOneString = SdkAssert.assertMethodExist(usageOneBox, "getUsageOneString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsageOneString, "QUsageOneString;");

    Assert.assertEquals("inner types count of 'UsageOneBox'", 1, usageOneBox.innerTypes().list().size());
    // type UsageOneString
    IType usageOneString = SdkAssert.assertTypeExists(usageOneBox, "UsageOneString");
    SdkAssert.assertHasFlags(usageOneString, 9);
    SdkAssert.assertHasSuperTypeSignature(usageOneString, "QAbstractValueFieldData<QString;>;");

    // fields of UsageOneString
    Assert.assertEquals("field count of 'UsageOneString'", 1, usageOneString.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(usageOneString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'UsageOneString'", 1, usageOneString.methods().list().size());
    IMethod usageOneString1 = SdkAssert.assertMethodExist(usageOneString, "UsageOneString", new String[]{});
    Assert.assertTrue(usageOneString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageOneString1, null);

    Assert.assertEquals("inner types count of 'UsageOneString'", 0, usageOneString.innerTypes().list().size());
    // type UsageTwoBox
    IType usageTwoBox = SdkAssert.assertTypeExists(templateBasedFormData, "UsageTwoBox");
    SdkAssert.assertHasFlags(usageTwoBox, 9);
    SdkAssert.assertHasSuperTypeSignature(usageTwoBox, "QAbstractTemplateForReplaceBoxData;");

    // fields of UsageTwoBox
    Assert.assertEquals("field count of 'UsageTwoBox'", 1, usageTwoBox.fields().list().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(usageTwoBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'UsageTwoBox'", 2, usageTwoBox.methods().list().size());
    IMethod usageTwoBox1 = SdkAssert.assertMethodExist(usageTwoBox, "UsageTwoBox", new String[]{});
    Assert.assertTrue(usageTwoBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageTwoBox1, null);
    IMethod getUsageTwoTemplateTable = SdkAssert.assertMethodExist(usageTwoBox, "getUsageTwoTemplateTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsageTwoTemplateTable, "QUsageTwoTemplateTable;");

    Assert.assertEquals("inner types count of 'UsageTwoBox'", 1, usageTwoBox.innerTypes().list().size());
    // type UsageTwoTemplateTable
    IType usageTwoTemplateTable = SdkAssert.assertTypeExists(usageTwoBox, "UsageTwoTemplateTable");
    SdkAssert.assertHasFlags(usageTwoTemplateTable, 9);
    SdkAssert.assertHasSuperTypeSignature(usageTwoTemplateTable, "QTemplateTable;");
    SdkAssert.assertAnnotation(usageTwoTemplateTable, "org.eclipse.scout.commons.annotations.Replace");

    // fields of UsageTwoTemplateTable
    Assert.assertEquals("field count of 'UsageTwoTemplateTable'", 1, usageTwoTemplateTable.fields().list().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(usageTwoTemplateTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    Assert.assertEquals("method count of 'UsageTwoTemplateTable'", 8, usageTwoTemplateTable.methods().list().size());
    IMethod usageTwoTemplateTable1 = SdkAssert.assertMethodExist(usageTwoTemplateTable, "UsageTwoTemplateTable", new String[]{});
    Assert.assertTrue(usageTwoTemplateTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageTwoTemplateTable1, null);
    IMethod addRow = SdkAssert.assertMethodExist(usageTwoTemplateTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QUsageTwoTemplateTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(usageTwoTemplateTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QUsageTwoTemplateTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(usageTwoTemplateTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QUsageTwoTemplateTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(usageTwoTemplateTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(usageTwoTemplateTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QUsageTwoTemplateTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(usageTwoTemplateTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QUsageTwoTemplateTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(usageTwoTemplateTable, "setRows", new String[]{"[QUsageTwoTemplateTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    Assert.assertEquals("inner types count of 'UsageTwoTemplateTable'", 1, usageTwoTemplateTable.innerTypes().list().size());
    // type UsageTwoTemplateTableRowData
    IType usageTwoTemplateTableRowData = SdkAssert.assertTypeExists(usageTwoTemplateTable, "UsageTwoTemplateTableRowData");
    SdkAssert.assertHasFlags(usageTwoTemplateTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(usageTwoTemplateTableRowData, "QTemplateTableRowData;");

    // fields of UsageTwoTemplateTableRowData
    Assert.assertEquals("field count of 'UsageTwoTemplateTableRowData'", 3, usageTwoTemplateTableRowData.fields().list().size());
    IField serialVersionUID5 = SdkAssert.assertFieldExist(usageTwoTemplateTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");
    IField third = SdkAssert.assertFieldExist(usageTwoTemplateTableRowData, "third");
    SdkAssert.assertHasFlags(third, 25);
    SdkAssert.assertFieldSignature(third, "QString;");
    IField m_third = SdkAssert.assertFieldExist(usageTwoTemplateTableRowData, "m_third");
    SdkAssert.assertHasFlags(m_third, 2);
    SdkAssert.assertFieldSignature(m_third, "QString;");

    Assert.assertEquals("method count of 'UsageTwoTemplateTableRowData'", 3, usageTwoTemplateTableRowData.methods().list().size());
    IMethod usageTwoTemplateTableRowData1 = SdkAssert.assertMethodExist(usageTwoTemplateTableRowData, "UsageTwoTemplateTableRowData", new String[]{});
    Assert.assertTrue(usageTwoTemplateTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageTwoTemplateTableRowData1, null);
    IMethod getThird = SdkAssert.assertMethodExist(usageTwoTemplateTableRowData, "getThird", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThird, "QString;");
    IMethod setThird = SdkAssert.assertMethodExist(usageTwoTemplateTableRowData, "setThird", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setThird, "V");

    Assert.assertEquals("inner types count of 'UsageTwoTemplateTableRowData'", 0, usageTwoTemplateTableRowData.innerTypes().list().size());
    // type UsualString
    IType usualString = SdkAssert.assertTypeExists(templateBasedFormData, "UsualString");
    SdkAssert.assertHasFlags(usualString, 9);
    SdkAssert.assertHasSuperTypeSignature(usualString, "QAbstractValueFieldData<QString;>;");

    // fields of UsualString
    Assert.assertEquals("field count of 'UsualString'", 1, usualString.fields().list().size());
    IField serialVersionUID6 = SdkAssert.assertFieldExist(usualString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    Assert.assertEquals("method count of 'UsualString'", 1, usualString.methods().list().size());
    IMethod usualString1 = SdkAssert.assertMethodExist(usualString, "UsualString", new String[]{});
    Assert.assertTrue(usualString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usualString1, null);

    Assert.assertEquals("inner types count of 'UsualString'", 0, usualString.innerTypes().list().size());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractRadioButtonGroupWithFieldsData(IType abstractRadioButtonGroupWithFieldsData) {
    // type AbstractRadioButtonGroupWithFieldsData
    SdkAssert.assertHasFlags(abstractRadioButtonGroupWithFieldsData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractRadioButtonGroupWithFieldsData, "QAbstractValueFieldData<QLong;>;");
    SdkAssert.assertAnnotation(abstractRadioButtonGroupWithFieldsData, "javax.annotation.Generated");

    // fields of AbstractRadioButtonGroupWithFieldsData
    Assert.assertEquals("field count of 'AbstractRadioButtonGroupWithFieldsData'", 1, abstractRadioButtonGroupWithFieldsData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractRadioButtonGroupWithFieldsData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'AbstractRadioButtonGroupWithFieldsData'", 2, abstractRadioButtonGroupWithFieldsData.methods().list().size());
    IMethod abstractRadioButtonGroupWithFieldsData1 = SdkAssert.assertMethodExist(abstractRadioButtonGroupWithFieldsData, "AbstractRadioButtonGroupWithFieldsData", new String[]{});
    Assert.assertTrue(abstractRadioButtonGroupWithFieldsData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractRadioButtonGroupWithFieldsData1, null);
    IMethod getInputString = SdkAssert.assertMethodExist(abstractRadioButtonGroupWithFieldsData, "getInputString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getInputString, "QInputString;");

    Assert.assertEquals("inner types count of 'AbstractRadioButtonGroupWithFieldsData'", 1, abstractRadioButtonGroupWithFieldsData.innerTypes().list().size());
    // type InputString
    IType inputString = SdkAssert.assertTypeExists(abstractRadioButtonGroupWithFieldsData, "InputString");
    SdkAssert.assertHasFlags(inputString, 9);
    SdkAssert.assertHasSuperTypeSignature(inputString, "QAbstractValueFieldData<QString;>;");

    // fields of InputString
    Assert.assertEquals("field count of 'InputString'", 1, inputString.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(inputString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'InputString'", 1, inputString.methods().list().size());
    IMethod inputString1 = SdkAssert.assertMethodExist(inputString, "InputString", new String[]{});
    Assert.assertTrue(inputString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(inputString1, null);

    Assert.assertEquals("inner types count of 'InputString'", 0, inputString.innerTypes().list().size());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfRadioButtonFormData(IType radioButtonFormData) {
    // type RadioButtonFormData
    SdkAssert.assertHasFlags(radioButtonFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(radioButtonFormData, "QAbstractFormData;");
    SdkAssert.assertAnnotation(radioButtonFormData, "javax.annotation.Generated");

    // fields of RadioButtonFormData
    Assert.assertEquals("field count of 'RadioButtonFormData'", 1, radioButtonFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(radioButtonFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'RadioButtonFormData'", 6, radioButtonFormData.methods().list().size());
    IMethod radioButtonFormData1 = SdkAssert.assertMethodExist(radioButtonFormData, "RadioButtonFormData", new String[]{});
    Assert.assertTrue(radioButtonFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(radioButtonFormData1, null);
    IMethod getInputExString = SdkAssert.assertMethodExist(radioButtonFormData, "getInputExString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getInputExString, "QInputExString;");
    IMethod getUsageOneUsualString = SdkAssert.assertMethodExist(radioButtonFormData, "getUsageOneUsualString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsageOneUsualString, "QUsageOneUsualString;");
    IMethod getUsedRadioButtonGroup = SdkAssert.assertMethodExist(radioButtonFormData, "getUsedRadioButtonGroup", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsedRadioButtonGroup, "QUsedRadioButtonGroup;");
    IMethod getUsualRadioButtonGroup = SdkAssert.assertMethodExist(radioButtonFormData, "getUsualRadioButtonGroup", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsualRadioButtonGroup, "QUsualRadioButtonGroup;");
    IMethod getUsualString = SdkAssert.assertMethodExist(radioButtonFormData, "getUsualString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsualString, "QUsualString;");

    Assert.assertEquals("inner types count of 'RadioButtonFormData'", 5, radioButtonFormData.innerTypes().list().size());
    // type InputExString
    IType inputExString = SdkAssert.assertTypeExists(radioButtonFormData, "InputExString");
    SdkAssert.assertHasFlags(inputExString, 9);
    SdkAssert.assertHasSuperTypeSignature(inputExString, "QInputString;");
    SdkAssert.assertAnnotation(inputExString, "org.eclipse.scout.commons.annotations.Replace");

    // fields of InputExString
    Assert.assertEquals("field count of 'InputExString'", 1, inputExString.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(inputExString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'InputExString'", 1, inputExString.methods().list().size());
    IMethod inputExString1 = SdkAssert.assertMethodExist(inputExString, "InputExString", new String[]{});
    Assert.assertTrue(inputExString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(inputExString1, null);

    Assert.assertEquals("inner types count of 'InputExString'", 0, inputExString.innerTypes().list().size());
    // type UsageOneUsualString
    IType usageOneUsualString = SdkAssert.assertTypeExists(radioButtonFormData, "UsageOneUsualString");
    SdkAssert.assertHasFlags(usageOneUsualString, 9);
    SdkAssert.assertHasSuperTypeSignature(usageOneUsualString, "QAbstractValueFieldData<QString;>;");

    // fields of UsageOneUsualString
    Assert.assertEquals("field count of 'UsageOneUsualString'", 1, usageOneUsualString.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(usageOneUsualString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'UsageOneUsualString'", 1, usageOneUsualString.methods().list().size());
    IMethod usageOneUsualString1 = SdkAssert.assertMethodExist(usageOneUsualString, "UsageOneUsualString", new String[]{});
    Assert.assertTrue(usageOneUsualString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageOneUsualString1, null);

    Assert.assertEquals("inner types count of 'UsageOneUsualString'", 0, usageOneUsualString.innerTypes().list().size());
    // type UsedRadioButtonGroup
    IType usedRadioButtonGroup = SdkAssert.assertTypeExists(radioButtonFormData, "UsedRadioButtonGroup");
    SdkAssert.assertHasFlags(usedRadioButtonGroup, 9);
    SdkAssert.assertHasSuperTypeSignature(usedRadioButtonGroup, "QAbstractRadioButtonGroupWithFieldsData;");

    // fields of UsedRadioButtonGroup
    Assert.assertEquals("field count of 'UsedRadioButtonGroup'", 1, usedRadioButtonGroup.fields().list().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(usedRadioButtonGroup, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'UsedRadioButtonGroup'", 1, usedRadioButtonGroup.methods().list().size());
    IMethod usedRadioButtonGroup1 = SdkAssert.assertMethodExist(usedRadioButtonGroup, "UsedRadioButtonGroup", new String[]{});
    Assert.assertTrue(usedRadioButtonGroup1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usedRadioButtonGroup1, null);

    Assert.assertEquals("inner types count of 'UsedRadioButtonGroup'", 0, usedRadioButtonGroup.innerTypes().list().size());
    // type UsualRadioButtonGroup
    IType usualRadioButtonGroup = SdkAssert.assertTypeExists(radioButtonFormData, "UsualRadioButtonGroup");
    SdkAssert.assertHasFlags(usualRadioButtonGroup, 9);
    SdkAssert.assertHasSuperTypeSignature(usualRadioButtonGroup, "QAbstractValueFieldData<QString;>;");

    // fields of UsualRadioButtonGroup
    Assert.assertEquals("field count of 'UsualRadioButtonGroup'", 1, usualRadioButtonGroup.fields().list().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(usualRadioButtonGroup, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    Assert.assertEquals("method count of 'UsualRadioButtonGroup'", 1, usualRadioButtonGroup.methods().list().size());
    IMethod usualRadioButtonGroup1 = SdkAssert.assertMethodExist(usualRadioButtonGroup, "UsualRadioButtonGroup", new String[]{});
    Assert.assertTrue(usualRadioButtonGroup1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usualRadioButtonGroup1, null);

    Assert.assertEquals("inner types count of 'UsualRadioButtonGroup'", 0, usualRadioButtonGroup.innerTypes().list().size());
    // type UsualString
    IType usualString = SdkAssert.assertTypeExists(radioButtonFormData, "UsualString");
    SdkAssert.assertHasFlags(usualString, 9);
    SdkAssert.assertHasSuperTypeSignature(usualString, "QAbstractValueFieldData<QString;>;");

    // fields of UsualString
    Assert.assertEquals("field count of 'UsualString'", 1, usualString.fields().list().size());
    IField serialVersionUID5 = SdkAssert.assertFieldExist(usualString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    Assert.assertEquals("method count of 'UsualString'", 1, usualString.methods().list().size());
    IMethod usualString1 = SdkAssert.assertMethodExist(usualString, "UsualString", new String[]{});
    Assert.assertTrue(usualString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usualString1, null);

    Assert.assertEquals("inner types count of 'UsualString'", 0, usualString.innerTypes().list().size());
  }
}
