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

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutSdkAssertions;
import org.junit.jupiter.api.Test;

/**
 * @since 3.10.0-M5
 */
public class ReplaceWithTemplatesTest {

  @Test
  public void testTemplateBoxData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.AbstractTemplateForReplaceBox", ReplaceWithTemplatesTest::testApiOfAbstractTemplateForReplaceBoxData);
  }

  @Test
  public void testTemplateBasedFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.TemplateBasedForm", ReplaceWithTemplatesTest::testApiOfTemplateBasedFormData);
  }

  @Test
  public void testTemplateRadioButtonGroupWithFields() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.AbstractRadioButtonGroupWithFields", ReplaceWithTemplatesTest::testApiOfAbstractRadioButtonGroupWithFieldsData);
  }

  @Test
  public void testRadioButtonForm() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.replace.RadioButtonForm", ReplaceWithTemplatesTest::testApiOfRadioButtonFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfAbstractTemplateForReplaceBoxData(IType abstractTemplateForReplaceBoxData) {
    var scoutApi = abstractTemplateForReplaceBoxData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(abstractTemplateForReplaceBoxData, Flags.AccPublic | Flags.AccAbstract);
    assertHasSuperClass(abstractTemplateForReplaceBoxData, scoutApi.AbstractFormFieldData());
    assertEquals(1, abstractTemplateForReplaceBoxData.annotations().stream().count(), "annotation count");
    assertAnnotation(abstractTemplateForReplaceBoxData, scoutApi.Generated());

    // fields of AbstractTemplateForReplaceBoxData
    assertEquals(1, abstractTemplateForReplaceBoxData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData'");
    ScoutSdkAssertions.assertSerialVersionUidField(abstractTemplateForReplaceBoxData);

    assertEquals(3, abstractTemplateForReplaceBoxData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData'");
    var getTemplateBoxString = assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateBoxString");
    assertMethodReturnType(getTemplateBoxString, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateBoxString");
    assertEquals(0, getTemplateBoxString.annotations().stream().count(), "annotation count");
    var getTemplateString = assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateString");
    assertMethodReturnType(getTemplateString, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateString");
    assertEquals(0, getTemplateString.annotations().stream().count(), "annotation count");
    var getTemplateTable = assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateTable");
    assertMethodReturnType(getTemplateTable, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable");
    assertEquals(0, getTemplateTable.annotations().stream().count(), "annotation count");

    assertEquals(3, abstractTemplateForReplaceBoxData.innerTypes().stream().count(), "inner types count of 'AbstractTemplateForReplaceBoxData'");
    // type TemplateBoxString
    var templateBoxString = assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateBoxString");
    assertHasFlags(templateBoxString, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(templateBoxString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(0, templateBoxString.annotations().stream().count(), "annotation count");

    // fields of TemplateBoxString
    assertEquals(1, templateBoxString.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateBoxString'");
    ScoutSdkAssertions.assertSerialVersionUidField(templateBoxString);

    assertEquals(0, templateBoxString.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateBoxString'");

    assertEquals(0, templateBoxString.innerTypes().stream().count(), "inner types count of 'TemplateBoxString'");
    // type TemplateString
    var templateString = assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateString");
    assertHasFlags(templateString, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(templateString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(0, templateString.annotations().stream().count(), "annotation count");

    // fields of TemplateString
    assertEquals(1, templateString.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateString'");
    ScoutSdkAssertions.assertSerialVersionUidField(templateString);

    assertEquals(0, templateString.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateString'");

    assertEquals(0, templateString.innerTypes().stream().count(), "inner types count of 'TemplateString'");
    // type TemplateTable
    var templateTable = assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateTable");
    assertHasFlags(templateTable, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(templateTable, scoutApi.AbstractTableFieldBeanData());
    assertEquals(0, templateTable.annotations().stream().count(), "annotation count");

    // fields of TemplateTable
    assertEquals(1, templateTable.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable'");
    ScoutSdkAssertions.assertSerialVersionUidField(templateTable);

    assertEquals(7, templateTable.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable'");
    var addRow = assertMethodExist(templateTable, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(templateTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(templateTable, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(templateTable, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(templateTable, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(templateTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(templateTable, "setRows", new String[]{"formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, templateTable.innerTypes().stream().count(), "inner types count of 'TemplateTable'");
    // type TemplateTableRowData
    var templateTableRowData = assertTypeExists(templateTable, "TemplateTableRowData");
    assertHasFlags(templateTableRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(templateTableRowData, scoutApi.AbstractTableRowData());
    assertEquals(0, templateTableRowData.annotations().stream().count(), "annotation count");

    // fields of TemplateTableRowData
    assertEquals(5, templateTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData'");
    ScoutSdkAssertions.assertSerialVersionUidField(templateTableRowData);
    var first = assertFieldExist(templateTableRowData, "first");
    assertHasFlags(first, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(first, "java.lang.String");
    assertEquals(0, first.annotations().stream().count(), "annotation count");
    var second = assertFieldExist(templateTableRowData, "second");
    assertHasFlags(second, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(second, "java.lang.String");
    assertEquals(0, second.annotations().stream().count(), "annotation count");
    var m_first = assertFieldExist(templateTableRowData, "m_first");
    assertHasFlags(m_first, Flags.AccPrivate);
    assertFieldType(m_first, "java.lang.String");
    assertEquals(0, m_first.annotations().stream().count(), "annotation count");
    var m_second = assertFieldExist(templateTableRowData, "m_second");
    assertHasFlags(m_second, Flags.AccPrivate);
    assertFieldType(m_second, "java.lang.String");
    assertEquals(0, m_second.annotations().stream().count(), "annotation count");

    assertEquals(4, templateTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData'");
    var getFirst = assertMethodExist(templateTableRowData, "getFirst");
    assertMethodReturnType(getFirst, "java.lang.String");
    assertEquals(0, getFirst.annotations().stream().count(), "annotation count");
    var setFirst = assertMethodExist(templateTableRowData, "setFirst", new String[]{"java.lang.String"});
    assertMethodReturnType(setFirst, "void");
    assertEquals(0, setFirst.annotations().stream().count(), "annotation count");
    var getSecond = assertMethodExist(templateTableRowData, "getSecond");
    assertMethodReturnType(getSecond, "java.lang.String");
    assertEquals(0, getSecond.annotations().stream().count(), "annotation count");
    var setSecond = assertMethodExist(templateTableRowData, "setSecond", new String[]{"java.lang.String"});
    assertMethodReturnType(setSecond, "void");
    assertEquals(0, setSecond.annotations().stream().count(), "annotation count");

    assertEquals(0, templateTableRowData.innerTypes().stream().count(), "inner types count of 'TemplateTableRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfTemplateBasedFormData(IType templateBasedFormData) {
    var scoutApi = templateBasedFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(templateBasedFormData, Flags.AccPublic);
    assertHasSuperClass(templateBasedFormData, scoutApi.AbstractFormData());
    assertEquals(1, templateBasedFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(templateBasedFormData, scoutApi.Generated());

    // fields of TemplateBasedFormData
    assertEquals(1, templateBasedFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TemplateBasedFormData'");
    ScoutSdkAssertions.assertSerialVersionUidField(templateBasedFormData);

    assertEquals(3, templateBasedFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TemplateBasedFormData'");
    var getUsageOneBox = assertMethodExist(templateBasedFormData, "getUsageOneBox");
    assertMethodReturnType(getUsageOneBox, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageOneBox");
    assertEquals(0, getUsageOneBox.annotations().stream().count(), "annotation count");
    var getUsageTwoBox = assertMethodExist(templateBasedFormData, "getUsageTwoBox");
    assertMethodReturnType(getUsageTwoBox, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox");
    assertEquals(0, getUsageTwoBox.annotations().stream().count(), "annotation count");
    var getUsualString = assertMethodExist(templateBasedFormData, "getUsualString");
    assertMethodReturnType(getUsualString, "formdata.shared.services.process.replace.TemplateBasedFormData$UsualString");
    assertEquals(0, getUsualString.annotations().stream().count(), "annotation count");

    assertEquals(3, templateBasedFormData.innerTypes().stream().count(), "inner types count of 'TemplateBasedFormData'");
    // type UsageOneBox
    var usageOneBox = assertTypeExists(templateBasedFormData, "UsageOneBox");
    assertHasFlags(usageOneBox, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(usageOneBox, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData");
    assertEquals(0, usageOneBox.annotations().stream().count(), "annotation count");

    // fields of UsageOneBox
    assertEquals(1, usageOneBox.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsageOneBox'");
    ScoutSdkAssertions.assertSerialVersionUidField(usageOneBox);

    assertEquals(1, usageOneBox.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsageOneBox'");
    var getUsageOneString = assertMethodExist(usageOneBox, "getUsageOneString");
    assertMethodReturnType(getUsageOneString, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageOneBox$UsageOneString");
    assertEquals(0, getUsageOneString.annotations().stream().count(), "annotation count");

    assertEquals(1, usageOneBox.innerTypes().stream().count(), "inner types count of 'UsageOneBox'");
    // type UsageOneString
    var usageOneString = assertTypeExists(usageOneBox, "UsageOneString");
    assertHasFlags(usageOneString, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(usageOneString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(0, usageOneString.annotations().stream().count(), "annotation count");

    // fields of UsageOneString
    assertEquals(1, usageOneString.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsageOneBox$UsageOneString'");
    ScoutSdkAssertions.assertSerialVersionUidField(usageOneString);

    assertEquals(0, usageOneString.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsageOneBox$UsageOneString'");

    assertEquals(0, usageOneString.innerTypes().stream().count(), "inner types count of 'UsageOneString'");
    // type UsageTwoBox
    var usageTwoBox = assertTypeExists(templateBasedFormData, "UsageTwoBox");
    assertHasFlags(usageTwoBox, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(usageTwoBox, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData");
    assertEquals(0, usageTwoBox.annotations().stream().count(), "annotation count");

    // fields of UsageTwoBox
    assertEquals(1, usageTwoBox.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox'");
    ScoutSdkAssertions.assertSerialVersionUidField(usageTwoBox);

    assertEquals(1, usageTwoBox.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox'");
    var getUsageTwoTemplateTable = assertMethodExist(usageTwoBox, "getUsageTwoTemplateTable");
    assertMethodReturnType(getUsageTwoTemplateTable, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable");
    assertEquals(0, getUsageTwoTemplateTable.annotations().stream().count(), "annotation count");

    assertEquals(1, usageTwoBox.innerTypes().stream().count(), "inner types count of 'UsageTwoBox'");
    // type UsageTwoTemplateTable
    var usageTwoTemplateTable = assertTypeExists(usageTwoBox, "UsageTwoTemplateTable");
    assertHasFlags(usageTwoTemplateTable, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(usageTwoTemplateTable, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable");
    assertEquals(1, usageTwoTemplateTable.annotations().stream().count(), "annotation count");
    assertAnnotation(usageTwoTemplateTable, scoutApi.Replace());

    // fields of UsageTwoTemplateTable
    assertEquals(1, usageTwoTemplateTable.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable'");
    ScoutSdkAssertions.assertSerialVersionUidField(usageTwoTemplateTable);

    assertEquals(7, usageTwoTemplateTable.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable'");
    var addRow = assertMethodExist(usageTwoTemplateTable, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(usageTwoTemplateTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(usageTwoTemplateTable, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(usageTwoTemplateTable, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(usageTwoTemplateTable, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(usageTwoTemplateTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(usageTwoTemplateTable, "setRows", new String[]{"formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, usageTwoTemplateTable.innerTypes().stream().count(), "inner types count of 'UsageTwoTemplateTable'");
    // type UsageTwoTemplateTableRowData
    var usageTwoTemplateTableRowData = assertTypeExists(usageTwoTemplateTable, "UsageTwoTemplateTableRowData");
    assertHasFlags(usageTwoTemplateTableRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(usageTwoTemplateTableRowData, "formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData$TemplateTable$TemplateTableRowData");
    assertEquals(0, usageTwoTemplateTableRowData.annotations().stream().count(), "annotation count");

    // fields of UsageTwoTemplateTableRowData
    assertEquals(3, usageTwoTemplateTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData'");
    ScoutSdkAssertions.assertSerialVersionUidField(usageTwoTemplateTableRowData);
    var third = assertFieldExist(usageTwoTemplateTableRowData, "third");
    assertHasFlags(third, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(third, "java.lang.String");
    assertEquals(0, third.annotations().stream().count(), "annotation count");
    var m_third = assertFieldExist(usageTwoTemplateTableRowData, "m_third");
    assertHasFlags(m_third, Flags.AccPrivate);
    assertFieldType(m_third, "java.lang.String");
    assertEquals(0, m_third.annotations().stream().count(), "annotation count");

    assertEquals(2, usageTwoTemplateTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsageTwoBox$UsageTwoTemplateTable$UsageTwoTemplateTableRowData'");
    var getThird = assertMethodExist(usageTwoTemplateTableRowData, "getThird");
    assertMethodReturnType(getThird, "java.lang.String");
    assertEquals(0, getThird.annotations().stream().count(), "annotation count");
    var setThird = assertMethodExist(usageTwoTemplateTableRowData, "setThird", new String[]{"java.lang.String"});
    assertMethodReturnType(setThird, "void");
    assertEquals(0, setThird.annotations().stream().count(), "annotation count");

    assertEquals(0, usageTwoTemplateTableRowData.innerTypes().stream().count(), "inner types count of 'UsageTwoTemplateTableRowData'");
    // type UsualString
    var usualString = assertTypeExists(templateBasedFormData, "UsualString");
    assertHasFlags(usualString, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(usualString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(0, usualString.annotations().stream().count(), "annotation count");

    // fields of UsualString
    assertEquals(1, usualString.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsualString'");
    ScoutSdkAssertions.assertSerialVersionUidField(usualString);

    assertEquals(0, usualString.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TemplateBasedFormData$UsualString'");

    assertEquals(0, usualString.innerTypes().stream().count(), "inner types count of 'UsualString'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfAbstractRadioButtonGroupWithFieldsData(IType abstractRadioButtonGroupWithFieldsData) {
    var scoutApi = abstractRadioButtonGroupWithFieldsData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(abstractRadioButtonGroupWithFieldsData, Flags.AccPublic | Flags.AccAbstract);
    assertHasSuperClass(abstractRadioButtonGroupWithFieldsData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Long>");
    assertEquals(1, abstractRadioButtonGroupWithFieldsData.annotations().stream().count(), "annotation count");
    assertAnnotation(abstractRadioButtonGroupWithFieldsData, scoutApi.Generated());

    // fields of AbstractRadioButtonGroupWithFieldsData
    assertEquals(1, abstractRadioButtonGroupWithFieldsData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData'");
    ScoutSdkAssertions.assertSerialVersionUidField(abstractRadioButtonGroupWithFieldsData);

    assertEquals(1, abstractRadioButtonGroupWithFieldsData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData'");
    var getInputString = assertMethodExist(abstractRadioButtonGroupWithFieldsData, "getInputString");
    assertMethodReturnType(getInputString, "formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData$InputString");
    assertEquals(0, getInputString.annotations().stream().count(), "annotation count");

    assertEquals(1, abstractRadioButtonGroupWithFieldsData.innerTypes().stream().count(), "inner types count of 'AbstractRadioButtonGroupWithFieldsData'");
    // type InputString
    var inputString = assertTypeExists(abstractRadioButtonGroupWithFieldsData, "InputString");
    assertHasFlags(inputString, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(inputString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(0, inputString.annotations().stream().count(), "annotation count");

    // fields of InputString
    assertEquals(1, inputString.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData$InputString'");
    ScoutSdkAssertions.assertSerialVersionUidField(inputString);

    assertEquals(0, inputString.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData$InputString'");

    assertEquals(0, inputString.innerTypes().stream().count(), "inner types count of 'InputString'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfRadioButtonFormData(IType radioButtonFormData) {
    var scoutApi = radioButtonFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(radioButtonFormData, Flags.AccPublic);
    assertHasSuperClass(radioButtonFormData, scoutApi.AbstractFormData());
    assertEquals(1, radioButtonFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(radioButtonFormData, scoutApi.Generated());

    // fields of RadioButtonFormData
    assertEquals(1, radioButtonFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.RadioButtonFormData'");
    ScoutSdkAssertions.assertSerialVersionUidField(radioButtonFormData);

    assertEquals(5, radioButtonFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.RadioButtonFormData'");
    var getInputExString = assertMethodExist(radioButtonFormData, "getInputExString");
    assertMethodReturnType(getInputExString, "formdata.shared.services.process.replace.RadioButtonFormData$InputExString");
    assertEquals(0, getInputExString.annotations().stream().count(), "annotation count");
    var getUsageOneUsualString = assertMethodExist(radioButtonFormData, "getUsageOneUsualString");
    assertMethodReturnType(getUsageOneUsualString, "formdata.shared.services.process.replace.RadioButtonFormData$UsageOneUsualString");
    assertEquals(0, getUsageOneUsualString.annotations().stream().count(), "annotation count");
    var getUsedRadioButtonGroup = assertMethodExist(radioButtonFormData, "getUsedRadioButtonGroup");
    assertMethodReturnType(getUsedRadioButtonGroup, "formdata.shared.services.process.replace.RadioButtonFormData$UsedRadioButtonGroup");
    assertEquals(0, getUsedRadioButtonGroup.annotations().stream().count(), "annotation count");
    var getUsualRadioButtonGroup = assertMethodExist(radioButtonFormData, "getUsualRadioButtonGroup");
    assertMethodReturnType(getUsualRadioButtonGroup, "formdata.shared.services.process.replace.RadioButtonFormData$UsualRadioButtonGroup");
    assertEquals(0, getUsualRadioButtonGroup.annotations().stream().count(), "annotation count");
    var getUsualString = assertMethodExist(radioButtonFormData, "getUsualString");
    assertMethodReturnType(getUsualString, "formdata.shared.services.process.replace.RadioButtonFormData$UsualString");
    assertEquals(0, getUsualString.annotations().stream().count(), "annotation count");

    assertEquals(5, radioButtonFormData.innerTypes().stream().count(), "inner types count of 'RadioButtonFormData'");
    // type InputExString
    var inputExString = assertTypeExists(radioButtonFormData, "InputExString");
    assertHasFlags(inputExString, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(inputExString, "formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData$InputString");
    assertEquals(1, inputExString.annotations().stream().count(), "annotation count");
    assertAnnotation(inputExString, scoutApi.Replace());

    // fields of InputExString
    assertEquals(1, inputExString.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.RadioButtonFormData$InputExString'");
    ScoutSdkAssertions.assertSerialVersionUidField(inputExString);

    assertEquals(0, inputExString.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.RadioButtonFormData$InputExString'");

    assertEquals(0, inputExString.innerTypes().stream().count(), "inner types count of 'InputExString'");
    // type UsageOneUsualString
    var usageOneUsualString = assertTypeExists(radioButtonFormData, "UsageOneUsualString");
    assertHasFlags(usageOneUsualString, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(usageOneUsualString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(0, usageOneUsualString.annotations().stream().count(), "annotation count");

    // fields of UsageOneUsualString
    assertEquals(1, usageOneUsualString.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.RadioButtonFormData$UsageOneUsualString'");
    ScoutSdkAssertions.assertSerialVersionUidField(usageOneUsualString);

    assertEquals(0, usageOneUsualString.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.RadioButtonFormData$UsageOneUsualString'");

    assertEquals(0, usageOneUsualString.innerTypes().stream().count(), "inner types count of 'UsageOneUsualString'");
    // type UsedRadioButtonGroup
    var usedRadioButtonGroup = assertTypeExists(radioButtonFormData, "UsedRadioButtonGroup");
    assertHasFlags(usedRadioButtonGroup, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(usedRadioButtonGroup, "formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData");
    assertEquals(0, usedRadioButtonGroup.annotations().stream().count(), "annotation count");

    // fields of UsedRadioButtonGroup
    assertEquals(1, usedRadioButtonGroup.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.RadioButtonFormData$UsedRadioButtonGroup'");
    ScoutSdkAssertions.assertSerialVersionUidField(usedRadioButtonGroup);

    assertEquals(0, usedRadioButtonGroup.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.RadioButtonFormData$UsedRadioButtonGroup'");

    assertEquals(0, usedRadioButtonGroup.innerTypes().stream().count(), "inner types count of 'UsedRadioButtonGroup'");
    // type UsualRadioButtonGroup
    var usualRadioButtonGroup = assertTypeExists(radioButtonFormData, "UsualRadioButtonGroup");
    assertHasFlags(usualRadioButtonGroup, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(usualRadioButtonGroup, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(0, usualRadioButtonGroup.annotations().stream().count(), "annotation count");

    // fields of UsualRadioButtonGroup
    assertEquals(1, usualRadioButtonGroup.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.RadioButtonFormData$UsualRadioButtonGroup'");
    ScoutSdkAssertions.assertSerialVersionUidField(usualRadioButtonGroup);

    assertEquals(0, usualRadioButtonGroup.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.RadioButtonFormData$UsualRadioButtonGroup'");

    assertEquals(0, usualRadioButtonGroup.innerTypes().stream().count(), "inner types count of 'UsualRadioButtonGroup'");
    // type UsualString
    var usualString = assertTypeExists(radioButtonFormData, "UsualString");
    assertHasFlags(usualString, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(usualString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(0, usualString.annotations().stream().count(), "annotation count");

    // fields of UsualString
    assertEquals(1, usualString.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.RadioButtonFormData$UsualString'");
    ScoutSdkAssertions.assertSerialVersionUidField(usualString);

    assertEquals(0, usualString.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.RadioButtonFormData$UsualString'");

    assertEquals(0, usualString.innerTypes().stream().count(), "inner types count of 'UsualString'");
  }
}
