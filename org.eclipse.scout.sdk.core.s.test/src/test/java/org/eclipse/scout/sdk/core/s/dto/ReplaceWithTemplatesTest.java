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
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.template.formfield.replace.AbstractRadioButtonGroupWithFields;
import formdata.client.ui.template.formfield.replace.AbstractTemplateForReplaceBox;
import formdata.client.ui.template.formfield.replace.RadioButtonForm;
import formdata.client.ui.template.formfield.replace.TemplateBasedForm;

/**
 * @since 3.10.0-M5
 */
public class ReplaceWithTemplatesTest {

  @Test
  public void testTemplateBoxData() {
    createFormDataAssertNoCompileErrors(AbstractTemplateForReplaceBox.class.getName(), ReplaceWithTemplatesTest::testApiOfAbstractTemplateForReplaceBoxData);
  }

  @Test
  public void testTemplateBasedFormData() {
    createFormDataAssertNoCompileErrors(TemplateBasedForm.class.getName(), ReplaceWithTemplatesTest::testApiOfTemplateBasedFormData);
  }

  @Test
  public void testTemplateRadioButtonGroupWithFields() {
    createFormDataAssertNoCompileErrors(AbstractRadioButtonGroupWithFields.class.getName(), ReplaceWithTemplatesTest::testApiOfAbstractRadioButtonGroupWithFieldsData);
  }

  @Test
  public void testRadioButtonForm() {
    createFormDataAssertNoCompileErrors(RadioButtonForm.class.getName(), ReplaceWithTemplatesTest::testApiOfRadioButtonFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractTemplateForReplaceBoxData(IType abstractTemplateForReplaceBoxData) {
    // type AbstractTemplateForReplaceBoxData
    assertHasFlags(abstractTemplateForReplaceBoxData, 1025);
    assertHasSuperClass(abstractTemplateForReplaceBoxData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData");
    assertAnnotation(abstractTemplateForReplaceBoxData, "javax.annotation.Generated");

    // fields of AbstractTemplateForReplaceBoxData
    assertEquals(1, abstractTemplateForReplaceBoxData.fields().stream().count(), "field count of 'AbstractTemplateForReplaceBoxData'");
    var serialVersionUID = assertFieldExist(abstractTemplateForReplaceBoxData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(3, abstractTemplateForReplaceBoxData.methods().stream().count(), "method count of 'AbstractTemplateForReplaceBoxData'");
    var getTemplateBoxString = assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateBoxString", new String[]{});
    assertMethodReturnType(getTemplateBoxString, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateBoxString");
    var getTemplateString = assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateString", new String[]{});
    assertMethodReturnType(getTemplateString, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateString");
    var getTemplateTable = assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateTable", new String[]{});
    assertMethodReturnType(getTemplateTable, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable");

    assertEquals(3, abstractTemplateForReplaceBoxData.innerTypes().stream().count(), "inner types count of 'AbstractTemplateForReplaceBoxData'");
    // type TemplateBoxString
    var templateBoxString = assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateBoxString");
    assertHasFlags(templateBoxString, 9);
    assertHasSuperClass(templateBoxString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of TemplateBoxString
    assertEquals(1, templateBoxString.fields().stream().count(), "field count of 'TemplateBoxString'");
    var serialVersionUID1 = assertFieldExist(templateBoxString, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, templateBoxString.methods().stream().count(), "method count of 'TemplateBoxString'");

    assertEquals(0, templateBoxString.innerTypes().stream().count(), "inner types count of 'TemplateBoxString'");
    // type TemplateString
    var templateString = assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateString");
    assertHasFlags(templateString, 9);
    assertHasSuperClass(templateString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of TemplateString
    assertEquals(1, templateString.fields().stream().count(), "field count of 'TemplateString'");
    var serialVersionUID2 = assertFieldExist(templateString, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, templateString.methods().stream().count(), "method count of 'TemplateString'");

    assertEquals(0, templateString.innerTypes().stream().count(), "inner types count of 'TemplateString'");
    // type TemplateTable
    var templateTable = assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateTable");
    assertHasFlags(templateTable, 9);
    assertHasSuperClass(templateTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of TemplateTable
    assertEquals(1, templateTable.fields().stream().count(), "field count of 'TemplateTable'");
    var serialVersionUID3 = assertFieldExist(templateTable, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(7, templateTable.methods().stream().count(), "method count of 'TemplateTable'");
    var addRow = assertMethodExist(templateTable, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(templateTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(templateTable, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(templateTable, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(templateTable, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(templateTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(templateTable, "setRows", new String[]{"formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, templateTable.innerTypes().stream().count(), "inner types count of 'TemplateTable'");
    // type TemplateTableRowData
    var templateTableRowData = assertTypeExists(templateTable, "TemplateTableRowData");
    assertHasFlags(templateTableRowData, 9);
    assertHasSuperClass(templateTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of TemplateTableRowData
    assertEquals(5, templateTableRowData.fields().stream().count(), "field count of 'TemplateTableRowData'");
    var serialVersionUID4 = assertFieldExist(templateTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");
    var first = assertFieldExist(templateTableRowData, "first");
    assertHasFlags(first, 25);
    assertFieldType(first, String.class.getName());
    var second = assertFieldExist(templateTableRowData, "second");
    assertHasFlags(second, 25);
    assertFieldType(second, String.class.getName());
    var m_first = assertFieldExist(templateTableRowData, "m_first");
    assertHasFlags(m_first, 2);
    assertFieldType(m_first, String.class.getName());
    var m_second = assertFieldExist(templateTableRowData, "m_second");
    assertHasFlags(m_second, 2);
    assertFieldType(m_second, String.class.getName());

    assertEquals(4, templateTableRowData.methods().stream().count(), "method count of 'TemplateTableRowData'");
    var getFirst = assertMethodExist(templateTableRowData, "getFirst", new String[]{});
    assertMethodReturnType(getFirst, String.class.getName());
    var setFirst = assertMethodExist(templateTableRowData, "setFirst", new String[]{String.class.getName()});
    assertMethodReturnType(setFirst, "void");
    var getSecond = assertMethodExist(templateTableRowData, "getSecond", new String[]{});
    assertMethodReturnType(getSecond, String.class.getName());
    var setSecond = assertMethodExist(templateTableRowData, "setSecond", new String[]{String.class.getName()});
    assertMethodReturnType(setSecond, "void");

    assertEquals(0, templateTableRowData.innerTypes().stream().count(), "inner types count of 'TemplateTableRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfTemplateBasedFormData(IType templateBasedFormData) {
    // type TemplateBasedFormData
    assertHasFlags(templateBasedFormData, 1);
    assertHasSuperClass(templateBasedFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");
    assertAnnotation(templateBasedFormData, "javax.annotation.Generated");

    // fields of TemplateBasedFormData
    assertEquals(1, templateBasedFormData.fields().stream().count(), "field count of 'TemplateBasedFormData'");
    var serialVersionUID = assertFieldExist(templateBasedFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(3, templateBasedFormData.methods().stream().count(), "method count of 'TemplateBasedFormData'");
    var getUsageOneBox = assertMethodExist(templateBasedFormData, "getUsageOneBox", new String[]{});
    assertMethodReturnType(getUsageOneBox, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageOneBox");
    var getUsageTwoBox = assertMethodExist(templateBasedFormData, "getUsageTwoBox", new String[]{});
    assertMethodReturnType(getUsageTwoBox, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox");
    var getUsualString = assertMethodExist(templateBasedFormData, "getUsualString", new String[]{});
    assertMethodReturnType(getUsualString, "formdata.shared.services.process.replace.TemplateBasedFormData$UsualString");

    assertEquals(3, templateBasedFormData.innerTypes().stream().count(), "inner types count of 'TemplateBasedFormData'");
    // type UsageOneBox
    var usageOneBox = assertTypeExists(templateBasedFormData, "UsageOneBox");
    assertHasFlags(usageOneBox, 9);
    assertHasSuperClass(usageOneBox, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData");

    // fields of UsageOneBox
    assertEquals(1, usageOneBox.fields().stream().count(), "field count of 'UsageOneBox'");
    var serialVersionUID1 = assertFieldExist(usageOneBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(1, usageOneBox.methods().stream().count(), "method count of 'UsageOneBox'");
    var getUsageOneString = assertMethodExist(usageOneBox, "getUsageOneString", new String[]{});
    assertMethodReturnType(getUsageOneString, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageOneBox$UsageOneString");

    assertEquals(1, usageOneBox.innerTypes().stream().count(), "inner types count of 'UsageOneBox'");
    // type UsageOneString
    var usageOneString = assertTypeExists(usageOneBox, "UsageOneString");
    assertHasFlags(usageOneString, 9);
    assertHasSuperClass(usageOneString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of UsageOneString
    assertEquals(1, usageOneString.fields().stream().count(), "field count of 'UsageOneString'");
    var serialVersionUID2 = assertFieldExist(usageOneString, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, usageOneString.methods().stream().count(), "method count of 'UsageOneString'");

    assertEquals(0, usageOneString.innerTypes().stream().count(), "inner types count of 'UsageOneString'");
    // type UsageTwoBox
    var usageTwoBox = assertTypeExists(templateBasedFormData, "UsageTwoBox");
    assertHasFlags(usageTwoBox, 9);
    assertHasSuperClass(usageTwoBox, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData");

    // fields of UsageTwoBox
    assertEquals(1, usageTwoBox.fields().stream().count(), "field count of 'UsageTwoBox'");
    var serialVersionUID3 = assertFieldExist(usageTwoBox, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(1, usageTwoBox.methods().stream().count(), "method count of 'UsageTwoBox'");
    var getUsageTwoTemplateTable = assertMethodExist(usageTwoBox, "getUsageTwoTemplateTable", new String[]{});
    assertMethodReturnType(getUsageTwoTemplateTable, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable");

    assertEquals(1, usageTwoBox.innerTypes().stream().count(), "inner types count of 'UsageTwoBox'");
    // type UsageTwoTemplateTable
    var usageTwoTemplateTable = assertTypeExists(usageTwoBox, "UsageTwoTemplateTable");
    assertHasFlags(usageTwoTemplateTable, 9);
    assertHasSuperClass(usageTwoTemplateTable, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable");
    assertAnnotation(usageTwoTemplateTable, "org.eclipse.scout.rt.platform.Replace");

    // fields of UsageTwoTemplateTable
    assertEquals(1, usageTwoTemplateTable.fields().stream().count(), "field count of 'UsageTwoTemplateTable'");
    var serialVersionUID4 = assertFieldExist(usageTwoTemplateTable, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(7, usageTwoTemplateTable.methods().stream().count(), "method count of 'UsageTwoTemplateTable'");
    var addRow = assertMethodExist(usageTwoTemplateTable, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(usageTwoTemplateTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(usageTwoTemplateTable, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(usageTwoTemplateTable, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(usageTwoTemplateTable, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(usageTwoTemplateTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(usageTwoTemplateTable, "setRows", new String[]{"formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, usageTwoTemplateTable.innerTypes().stream().count(), "inner types count of 'UsageTwoTemplateTable'");
    // type UsageTwoTemplateTableRowData
    var usageTwoTemplateTableRowData = assertTypeExists(usageTwoTemplateTable, "UsageTwoTemplateTableRowData");
    assertHasFlags(usageTwoTemplateTableRowData, 9);
    assertHasSuperClass(usageTwoTemplateTableRowData, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData");

    // fields of UsageTwoTemplateTableRowData
    assertEquals(3, usageTwoTemplateTableRowData.fields().stream().count(), "field count of 'UsageTwoTemplateTableRowData'");
    var serialVersionUID5 = assertFieldExist(usageTwoTemplateTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");
    var third = assertFieldExist(usageTwoTemplateTableRowData, "third");
    assertHasFlags(third, 25);
    assertFieldType(third, String.class.getName());
    var m_third = assertFieldExist(usageTwoTemplateTableRowData, "m_third");
    assertHasFlags(m_third, 2);
    assertFieldType(m_third, String.class.getName());

    assertEquals(2, usageTwoTemplateTableRowData.methods().stream().count(), "method count of 'UsageTwoTemplateTableRowData'");
    var getThird = assertMethodExist(usageTwoTemplateTableRowData, "getThird", new String[]{});
    assertMethodReturnType(getThird, String.class.getName());
    var setThird = assertMethodExist(usageTwoTemplateTableRowData, "setThird", new String[]{String.class.getName()});
    assertMethodReturnType(setThird, "void");

    assertEquals(0, usageTwoTemplateTableRowData.innerTypes().stream().count(), "inner types count of 'UsageTwoTemplateTableRowData'");
    // type UsualString
    var usualString = assertTypeExists(templateBasedFormData, "UsualString");
    assertHasFlags(usualString, 9);
    assertHasSuperClass(usualString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of UsualString
    assertEquals(1, usualString.fields().stream().count(), "field count of 'UsualString'");
    var serialVersionUID6 = assertFieldExist(usualString, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");

    assertEquals(0, usualString.methods().stream().count(), "method count of 'UsualString'");

    assertEquals(0, usualString.innerTypes().stream().count(), "inner types count of 'UsualString'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractRadioButtonGroupWithFieldsData(IType abstractRadioButtonGroupWithFieldsData) {
    // type AbstractRadioButtonGroupWithFieldsData
    assertHasFlags(abstractRadioButtonGroupWithFieldsData, 1025);
    assertHasSuperClass(abstractRadioButtonGroupWithFieldsData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Long>");
    assertAnnotation(abstractRadioButtonGroupWithFieldsData, "javax.annotation.Generated");

    // fields of AbstractRadioButtonGroupWithFieldsData
    assertEquals(1, abstractRadioButtonGroupWithFieldsData.fields().stream().count(), "field count of 'AbstractRadioButtonGroupWithFieldsData'");
    var serialVersionUID = assertFieldExist(abstractRadioButtonGroupWithFieldsData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, abstractRadioButtonGroupWithFieldsData.methods().stream().count(), "method count of 'AbstractRadioButtonGroupWithFieldsData'");
    var getInputString = assertMethodExist(abstractRadioButtonGroupWithFieldsData, "getInputString", new String[]{});
    assertMethodReturnType(getInputString, "formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData$InputString");

    assertEquals(1, abstractRadioButtonGroupWithFieldsData.innerTypes().stream().count(), "inner types count of 'AbstractRadioButtonGroupWithFieldsData'");
    // type InputString
    var inputString = assertTypeExists(abstractRadioButtonGroupWithFieldsData, "InputString");
    assertHasFlags(inputString, 9);
    assertHasSuperClass(inputString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of InputString
    assertEquals(1, inputString.fields().stream().count(), "field count of 'InputString'");
    var serialVersionUID1 = assertFieldExist(inputString, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, inputString.methods().stream().count(), "method count of 'InputString'");

    assertEquals(0, inputString.innerTypes().stream().count(), "inner types count of 'InputString'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfRadioButtonFormData(IType radioButtonFormData) {
    // type RadioButtonFormData
    assertHasFlags(radioButtonFormData, 1);
    assertHasSuperClass(radioButtonFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");
    assertAnnotation(radioButtonFormData, "javax.annotation.Generated");

    // fields of RadioButtonFormData
    assertEquals(1, radioButtonFormData.fields().stream().count(), "field count of 'RadioButtonFormData'");
    var serialVersionUID = assertFieldExist(radioButtonFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(5, radioButtonFormData.methods().stream().count(), "method count of 'RadioButtonFormData'");
    var getInputExString = assertMethodExist(radioButtonFormData, "getInputExString", new String[]{});
    assertMethodReturnType(getInputExString, "formdata.shared.services.process.replace.RadioButtonFormData$InputExString");
    var getUsageOneUsualString = assertMethodExist(radioButtonFormData, "getUsageOneUsualString", new String[]{});
    assertMethodReturnType(getUsageOneUsualString, "formdata.shared.services.process.replace.RadioButtonFormData$UsageOneUsualString");
    var getUsedRadioButtonGroup = assertMethodExist(radioButtonFormData, "getUsedRadioButtonGroup", new String[]{});
    assertMethodReturnType(getUsedRadioButtonGroup, "formdata.shared.services.process.replace.RadioButtonFormData$UsedRadioButtonGroup");
    var getUsualRadioButtonGroup = assertMethodExist(radioButtonFormData, "getUsualRadioButtonGroup", new String[]{});
    assertMethodReturnType(getUsualRadioButtonGroup, "formdata.shared.services.process.replace.RadioButtonFormData$UsualRadioButtonGroup");
    var getUsualString = assertMethodExist(radioButtonFormData, "getUsualString", new String[]{});
    assertMethodReturnType(getUsualString, "formdata.shared.services.process.replace.RadioButtonFormData$UsualString");

    assertEquals(5, radioButtonFormData.innerTypes().stream().count(), "inner types count of 'RadioButtonFormData'");
    // type InputExString
    var inputExString = assertTypeExists(radioButtonFormData, "InputExString");
    assertHasFlags(inputExString, 9);
    assertHasSuperClass(inputExString, "formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData$InputString");
    assertAnnotation(inputExString, "org.eclipse.scout.rt.platform.Replace");

    // fields of InputExString
    assertEquals(1, inputExString.fields().stream().count(), "field count of 'InputExString'");
    var serialVersionUID1 = assertFieldExist(inputExString, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, inputExString.methods().stream().count(), "method count of 'InputExString'");

    assertEquals(0, inputExString.innerTypes().stream().count(), "inner types count of 'InputExString'");
    // type UsageOneUsualString
    var usageOneUsualString = assertTypeExists(radioButtonFormData, "UsageOneUsualString");
    assertHasFlags(usageOneUsualString, 9);
    assertHasSuperClass(usageOneUsualString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of UsageOneUsualString
    assertEquals(1, usageOneUsualString.fields().stream().count(), "field count of 'UsageOneUsualString'");
    var serialVersionUID2 = assertFieldExist(usageOneUsualString, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, usageOneUsualString.methods().stream().count(), "method count of 'UsageOneUsualString'");

    assertEquals(0, usageOneUsualString.innerTypes().stream().count(), "inner types count of 'UsageOneUsualString'");
    // type UsedRadioButtonGroup
    var usedRadioButtonGroup = assertTypeExists(radioButtonFormData, "UsedRadioButtonGroup");
    assertHasFlags(usedRadioButtonGroup, 9);
    assertHasSuperClass(usedRadioButtonGroup, "formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData");

    // fields of UsedRadioButtonGroup
    assertEquals(1, usedRadioButtonGroup.fields().stream().count(), "field count of 'UsedRadioButtonGroup'");
    var serialVersionUID3 = assertFieldExist(usedRadioButtonGroup, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(0, usedRadioButtonGroup.methods().stream().count(), "method count of 'UsedRadioButtonGroup'");

    assertEquals(0, usedRadioButtonGroup.innerTypes().stream().count(), "inner types count of 'UsedRadioButtonGroup'");
    // type UsualRadioButtonGroup
    var usualRadioButtonGroup = assertTypeExists(radioButtonFormData, "UsualRadioButtonGroup");
    assertHasFlags(usualRadioButtonGroup, 9);
    assertHasSuperClass(usualRadioButtonGroup, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of UsualRadioButtonGroup
    assertEquals(1, usualRadioButtonGroup.fields().stream().count(), "field count of 'UsualRadioButtonGroup'");
    var serialVersionUID4 = assertFieldExist(usualRadioButtonGroup, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(0, usualRadioButtonGroup.methods().stream().count(), "method count of 'UsualRadioButtonGroup'");

    assertEquals(0, usualRadioButtonGroup.innerTypes().stream().count(), "inner types count of 'UsualRadioButtonGroup'");
    // type UsualString
    var usualString = assertTypeExists(radioButtonFormData, "UsualString");
    assertHasFlags(usualString, 9);
    assertHasSuperClass(usualString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of UsualString
    assertEquals(1, usualString.fields().stream().count(), "field count of 'UsualString'");
    var serialVersionUID5 = assertFieldExist(usualString, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(0, usualString.methods().stream().count(), "method count of 'UsualString'");

    assertEquals(0, usualString.innerTypes().stream().count(), "inner types count of 'UsualString'");
  }
}
