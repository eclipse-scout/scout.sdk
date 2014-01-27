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
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataDtoUpdateOperation;
import org.junit.Test;

/**
 * @since 3.10.0-M5
 */
public class ReplaceWithTemplatesTest extends AbstractSdkTestWithFormDataProject {

  private static final String TemplateBoxFqn = "formdata.client.ui.template.formfield.replace.AbstractTemplateForReplaceBox";
  private static final String TemplateBasedFormFqn = "formdata.client.ui.template.formfield.replace.TemplateBasedForm";
  private static final String TemplateRadioButtonGroupWithFieldsFqn = "formdata.client.ui.template.formfield.replace.AbstractRadioButtonGroupWithFields";
  private static final String RadioButtonFormFqn = "formdata.client.ui.template.formfield.replace.RadioButtonForm";

  private void createFormData(String typeName) throws Exception {
    IType field = SdkAssert.assertTypeExists(typeName);
    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(field);
    TestWorkspaceUtility.executeAndBuildWorkspace(op);
  }

  @Test
  public void testTemplateBoxData() throws Exception {
    createFormData(TemplateBoxFqn);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfAbstractTemplateForReplaceBoxData();
  }

  @Test
  public void testTemplateBasedFormData() throws Exception {
    createFormData(TemplateBasedFormFqn);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfTemplateBasedFormData();
  }

  @Test
  public void testTemplateRadioButtonGroupWithFields() throws Exception {
    createFormData(TemplateRadioButtonGroupWithFieldsFqn);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfAbstractRadioButtonGroupWithFieldsData();
  }

  @Test
  public void testRadioButtonForm() throws Exception {
    createFormData(RadioButtonFormFqn);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfRadioButtonFormData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfAbstractTemplateForReplaceBoxData() throws Exception {
    // type AbstractTemplateForReplaceBoxData
    IType abstractTemplateForReplaceBoxData = SdkAssert.assertTypeExists("formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData");
    SdkAssert.assertHasFlags(abstractTemplateForReplaceBoxData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractTemplateForReplaceBoxData, "QAbstractFormFieldData;");
    SdkAssert.assertAnnotation(abstractTemplateForReplaceBoxData, "javax.annotation.Generated");

    // fields of AbstractTemplateForReplaceBoxData
    SdkAssert.assertEquals("field count of 'AbstractTemplateForReplaceBoxData'", 1, abstractTemplateForReplaceBoxData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractTemplateForReplaceBoxData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'AbstractTemplateForReplaceBoxData'", 4, abstractTemplateForReplaceBoxData.getMethods().length);
    IMethod abstractTemplateForReplaceBoxData1 = SdkAssert.assertMethodExist(abstractTemplateForReplaceBoxData, "AbstractTemplateForReplaceBoxData", new String[]{});
    SdkAssert.assertTrue(abstractTemplateForReplaceBoxData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractTemplateForReplaceBoxData1, "V");
    IMethod getTemplateBoxString = SdkAssert.assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateBoxString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTemplateBoxString, "QTemplateBoxString;");
    IMethod getTemplateString = SdkAssert.assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTemplateString, "QTemplateString;");
    IMethod getTemplateTable = SdkAssert.assertMethodExist(abstractTemplateForReplaceBoxData, "getTemplateTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTemplateTable, "QTemplateTable;");

    SdkAssert.assertEquals("inner types count of 'AbstractTemplateForReplaceBoxData'", 3, abstractTemplateForReplaceBoxData.getTypes().length);
    // type TemplateBoxString
    IType templateBoxString = SdkAssert.assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateBoxString");
    SdkAssert.assertHasFlags(templateBoxString, 9);
    SdkAssert.assertHasSuperTypeSignature(templateBoxString, "QAbstractValueFieldData<QString;>;");

    // fields of TemplateBoxString
    SdkAssert.assertEquals("field count of 'TemplateBoxString'", 1, templateBoxString.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(templateBoxString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'TemplateBoxString'", 2, templateBoxString.getMethods().length);
    IMethod templateBoxString1 = SdkAssert.assertMethodExist(templateBoxString, "TemplateBoxString", new String[]{});
    SdkAssert.assertTrue(templateBoxString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(templateBoxString1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(templateBoxString, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'TemplateBoxString'", 0, templateBoxString.getTypes().length);
    // type TemplateString
    IType templateString = SdkAssert.assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateString");
    SdkAssert.assertHasFlags(templateString, 9);
    SdkAssert.assertHasSuperTypeSignature(templateString, "QAbstractValueFieldData<QString;>;");

    // fields of TemplateString
    SdkAssert.assertEquals("field count of 'TemplateString'", 1, templateString.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(templateString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'TemplateString'", 2, templateString.getMethods().length);
    IMethod templateString1 = SdkAssert.assertMethodExist(templateString, "TemplateString", new String[]{});
    SdkAssert.assertTrue(templateString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(templateString1, "V");
    IMethod initValidationRules1 = SdkAssert.assertMethodExist(templateString, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules1, "V");
    SdkAssert.assertAnnotation(initValidationRules1, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules1, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'TemplateString'", 0, templateString.getTypes().length);
    // type TemplateTable
    IType templateTable = SdkAssert.assertTypeExists(abstractTemplateForReplaceBoxData, "TemplateTable");
    SdkAssert.assertHasFlags(templateTable, 9);
    SdkAssert.assertHasSuperTypeSignature(templateTable, "QAbstractTableFieldBeanData;");

    // fields of TemplateTable
    SdkAssert.assertEquals("field count of 'TemplateTable'", 1, templateTable.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(templateTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'TemplateTable'", 8, templateTable.getMethods().length);
    IMethod templateTable1 = SdkAssert.assertMethodExist(templateTable, "TemplateTable", new String[]{});
    SdkAssert.assertTrue(templateTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(templateTable1, "V");
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

    SdkAssert.assertEquals("inner types count of 'TemplateTable'", 1, templateTable.getTypes().length);
    // type TemplateTableRowData
    IType templateTableRowData = SdkAssert.assertTypeExists(templateTable, "TemplateTableRowData");
    SdkAssert.assertHasFlags(templateTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(templateTableRowData, "QAbstractTableRowData;");

    // fields of TemplateTableRowData
    SdkAssert.assertEquals("field count of 'TemplateTableRowData'", 5, templateTableRowData.getFields().length);
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

    SdkAssert.assertEquals("method count of 'TemplateTableRowData'", 5, templateTableRowData.getMethods().length);
    IMethod templateTableRowData1 = SdkAssert.assertMethodExist(templateTableRowData, "TemplateTableRowData", new String[]{});
    SdkAssert.assertTrue(templateTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(templateTableRowData1, "V");
    IMethod getFirst = SdkAssert.assertMethodExist(templateTableRowData, "getFirst", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirst, "QString;");
    IMethod setFirst = SdkAssert.assertMethodExist(templateTableRowData, "setFirst", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setFirst, "V");
    IMethod getSecond = SdkAssert.assertMethodExist(templateTableRowData, "getSecond", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecond, "QString;");
    IMethod setSecond = SdkAssert.assertMethodExist(templateTableRowData, "setSecond", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setSecond, "V");

    SdkAssert.assertEquals("inner types count of 'TemplateTableRowData'", 0, templateTableRowData.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfTemplateBasedFormData() throws Exception {
    // type TemplateBasedFormData
    IType templateBasedFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.replace.TemplateBasedFormData");
    SdkAssert.assertHasFlags(templateBasedFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(templateBasedFormData, "QAbstractFormData;");
    SdkAssert.assertAnnotation(templateBasedFormData, "javax.annotation.Generated");

    // fields of TemplateBasedFormData
    SdkAssert.assertEquals("field count of 'TemplateBasedFormData'", 1, templateBasedFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(templateBasedFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'TemplateBasedFormData'", 4, templateBasedFormData.getMethods().length);
    IMethod templateBasedFormData1 = SdkAssert.assertMethodExist(templateBasedFormData, "TemplateBasedFormData", new String[]{});
    SdkAssert.assertTrue(templateBasedFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(templateBasedFormData1, "V");
    IMethod getUsageOneBox = SdkAssert.assertMethodExist(templateBasedFormData, "getUsageOneBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsageOneBox, "QUsageOneBox;");
    IMethod getUsageTwoBox = SdkAssert.assertMethodExist(templateBasedFormData, "getUsageTwoBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsageTwoBox, "QUsageTwoBox;");
    IMethod getUsualString = SdkAssert.assertMethodExist(templateBasedFormData, "getUsualString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsualString, "QUsualString;");

    SdkAssert.assertEquals("inner types count of 'TemplateBasedFormData'", 3, templateBasedFormData.getTypes().length);
    // type UsageOneBox
    IType usageOneBox = SdkAssert.assertTypeExists(templateBasedFormData, "UsageOneBox");
    SdkAssert.assertHasFlags(usageOneBox, 9);
    SdkAssert.assertHasSuperTypeSignature(usageOneBox, "QAbstractTemplateForReplaceBoxData;");

    // fields of UsageOneBox
    SdkAssert.assertEquals("field count of 'UsageOneBox'", 1, usageOneBox.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(usageOneBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'UsageOneBox'", 2, usageOneBox.getMethods().length);
    IMethod usageOneBox1 = SdkAssert.assertMethodExist(usageOneBox, "UsageOneBox", new String[]{});
    SdkAssert.assertTrue(usageOneBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageOneBox1, "V");
    IMethod getUsageOneString = SdkAssert.assertMethodExist(usageOneBox, "getUsageOneString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsageOneString, "QUsageOneString;");

    SdkAssert.assertEquals("inner types count of 'UsageOneBox'", 1, usageOneBox.getTypes().length);
    // type UsageOneString
    IType usageOneString = SdkAssert.assertTypeExists(usageOneBox, "UsageOneString");
    SdkAssert.assertHasFlags(usageOneString, 9);
    SdkAssert.assertHasSuperTypeSignature(usageOneString, "QAbstractValueFieldData<QString;>;");

    // fields of UsageOneString
    SdkAssert.assertEquals("field count of 'UsageOneString'", 1, usageOneString.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(usageOneString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'UsageOneString'", 2, usageOneString.getMethods().length);
    IMethod usageOneString1 = SdkAssert.assertMethodExist(usageOneString, "UsageOneString", new String[]{});
    SdkAssert.assertTrue(usageOneString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageOneString1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(usageOneString, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'UsageOneString'", 0, usageOneString.getTypes().length);
    // type UsageTwoBox
    IType usageTwoBox = SdkAssert.assertTypeExists(templateBasedFormData, "UsageTwoBox");
    SdkAssert.assertHasFlags(usageTwoBox, 9);
    SdkAssert.assertHasSuperTypeSignature(usageTwoBox, "QAbstractTemplateForReplaceBoxData;");

    // fields of UsageTwoBox
    SdkAssert.assertEquals("field count of 'UsageTwoBox'", 1, usageTwoBox.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(usageTwoBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'UsageTwoBox'", 2, usageTwoBox.getMethods().length);
    IMethod usageTwoBox1 = SdkAssert.assertMethodExist(usageTwoBox, "UsageTwoBox", new String[]{});
    SdkAssert.assertTrue(usageTwoBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageTwoBox1, "V");
    IMethod getUsageTwoTemplateTable = SdkAssert.assertMethodExist(usageTwoBox, "getUsageTwoTemplateTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getUsageTwoTemplateTable, "QUsageTwoTemplateTable;");

    SdkAssert.assertEquals("inner types count of 'UsageTwoBox'", 1, usageTwoBox.getTypes().length);
    // type UsageTwoTemplateTable
    IType usageTwoTemplateTable = SdkAssert.assertTypeExists(usageTwoBox, "UsageTwoTemplateTable");
    SdkAssert.assertHasFlags(usageTwoTemplateTable, 9);
    SdkAssert.assertHasSuperTypeSignature(usageTwoTemplateTable, "QTemplateTable;");
    SdkAssert.assertAnnotation(usageTwoTemplateTable, "org.eclipse.scout.commons.annotations.Replace");

    // fields of UsageTwoTemplateTable
    SdkAssert.assertEquals("field count of 'UsageTwoTemplateTable'", 1, usageTwoTemplateTable.getFields().length);
    IField serialVersionUID4 = SdkAssert.assertFieldExist(usageTwoTemplateTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    SdkAssert.assertEquals("method count of 'UsageTwoTemplateTable'", 8, usageTwoTemplateTable.getMethods().length);
    IMethod usageTwoTemplateTable1 = SdkAssert.assertMethodExist(usageTwoTemplateTable, "UsageTwoTemplateTable", new String[]{});
    SdkAssert.assertTrue(usageTwoTemplateTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageTwoTemplateTable1, "V");
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

    SdkAssert.assertEquals("inner types count of 'UsageTwoTemplateTable'", 1, usageTwoTemplateTable.getTypes().length);
    // type UsageTwoTemplateTableRowData
    IType usageTwoTemplateTableRowData = SdkAssert.assertTypeExists(usageTwoTemplateTable, "UsageTwoTemplateTableRowData");
    SdkAssert.assertHasFlags(usageTwoTemplateTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(usageTwoTemplateTableRowData, "QTemplateTableRowData;");

    // fields of UsageTwoTemplateTableRowData
    SdkAssert.assertEquals("field count of 'UsageTwoTemplateTableRowData'", 3, usageTwoTemplateTableRowData.getFields().length);
    IField serialVersionUID5 = SdkAssert.assertFieldExist(usageTwoTemplateTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");
    IField third = SdkAssert.assertFieldExist(usageTwoTemplateTableRowData, "third");
    SdkAssert.assertHasFlags(third, 25);
    SdkAssert.assertFieldSignature(third, "QString;");
    IField m_third = SdkAssert.assertFieldExist(usageTwoTemplateTableRowData, "m_third");
    SdkAssert.assertHasFlags(m_third, 2);
    SdkAssert.assertFieldSignature(m_third, "QString;");

    SdkAssert.assertEquals("method count of 'UsageTwoTemplateTableRowData'", 3, usageTwoTemplateTableRowData.getMethods().length);
    IMethod usageTwoTemplateTableRowData1 = SdkAssert.assertMethodExist(usageTwoTemplateTableRowData, "UsageTwoTemplateTableRowData", new String[]{});
    SdkAssert.assertTrue(usageTwoTemplateTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageTwoTemplateTableRowData1, "V");
    IMethod getThird = SdkAssert.assertMethodExist(usageTwoTemplateTableRowData, "getThird", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getThird, "QString;");
    IMethod setThird = SdkAssert.assertMethodExist(usageTwoTemplateTableRowData, "setThird", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setThird, "V");

    SdkAssert.assertEquals("inner types count of 'UsageTwoTemplateTableRowData'", 0, usageTwoTemplateTableRowData.getTypes().length);
    // type UsualString
    IType usualString = SdkAssert.assertTypeExists(templateBasedFormData, "UsualString");
    SdkAssert.assertHasFlags(usualString, 9);
    SdkAssert.assertHasSuperTypeSignature(usualString, "QAbstractValueFieldData<QString;>;");

    // fields of UsualString
    SdkAssert.assertEquals("field count of 'UsualString'", 1, usualString.getFields().length);
    IField serialVersionUID6 = SdkAssert.assertFieldExist(usualString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    SdkAssert.assertEquals("method count of 'UsualString'", 2, usualString.getMethods().length);
    IMethod usualString1 = SdkAssert.assertMethodExist(usualString, "UsualString", new String[]{});
    SdkAssert.assertTrue(usualString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usualString1, "V");
    IMethod initValidationRules1 = SdkAssert.assertMethodExist(usualString, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules1, "V");
    SdkAssert.assertAnnotation(initValidationRules1, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules1, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'UsualString'", 0, usualString.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfAbstractRadioButtonGroupWithFieldsData() throws Exception {
    // type AbstractRadioButtonGroupWithFieldsData
    IType abstractRadioButtonGroupWithFieldsData = SdkAssert.assertTypeExists("formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData");
    SdkAssert.assertHasFlags(abstractRadioButtonGroupWithFieldsData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractRadioButtonGroupWithFieldsData, "QAbstractValueFieldData<QLong;>;");
    SdkAssert.assertAnnotation(abstractRadioButtonGroupWithFieldsData, "javax.annotation.Generated");

    // fields of AbstractRadioButtonGroupWithFieldsData
    SdkAssert.assertEquals("field count of 'AbstractRadioButtonGroupWithFieldsData'", 1, abstractRadioButtonGroupWithFieldsData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractRadioButtonGroupWithFieldsData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'AbstractRadioButtonGroupWithFieldsData'", 2, abstractRadioButtonGroupWithFieldsData.getMethods().length);
    IMethod abstractRadioButtonGroupWithFieldsData1 = SdkAssert.assertMethodExist(abstractRadioButtonGroupWithFieldsData, "AbstractRadioButtonGroupWithFieldsData", new String[]{});
    SdkAssert.assertTrue(abstractRadioButtonGroupWithFieldsData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractRadioButtonGroupWithFieldsData1, "V");
    IMethod getInputString = SdkAssert.assertMethodExist(abstractRadioButtonGroupWithFieldsData, "getInputString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getInputString, "QInputString;");

    SdkAssert.assertEquals("inner types count of 'AbstractRadioButtonGroupWithFieldsData'", 1, abstractRadioButtonGroupWithFieldsData.getTypes().length);
    // type InputString
    IType inputString = SdkAssert.assertTypeExists(abstractRadioButtonGroupWithFieldsData, "InputString");
    SdkAssert.assertHasFlags(inputString, 9);
    SdkAssert.assertHasSuperTypeSignature(inputString, "QAbstractValueFieldData<QString;>;");

    // fields of InputString
    SdkAssert.assertEquals("field count of 'InputString'", 1, inputString.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(inputString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'InputString'", 2, inputString.getMethods().length);
    IMethod inputString1 = SdkAssert.assertMethodExist(inputString, "InputString", new String[]{});
    SdkAssert.assertTrue(inputString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(inputString1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(inputString, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'InputString'", 0, inputString.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfRadioButtonFormData() throws Exception {
    // type RadioButtonFormData
    IType radioButtonFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.replace.RadioButtonFormData");
    SdkAssert.assertHasFlags(radioButtonFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(radioButtonFormData, "QAbstractFormData;");
    SdkAssert.assertAnnotation(radioButtonFormData, "javax.annotation.Generated");

    // fields of RadioButtonFormData
    SdkAssert.assertEquals("field count of 'RadioButtonFormData'", 1, radioButtonFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(radioButtonFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'RadioButtonFormData'", 6, radioButtonFormData.getMethods().length);
    IMethod radioButtonFormData1 = SdkAssert.assertMethodExist(radioButtonFormData, "RadioButtonFormData", new String[]{});
    SdkAssert.assertTrue(radioButtonFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(radioButtonFormData1, "V");
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

    SdkAssert.assertEquals("inner types count of 'RadioButtonFormData'", 5, radioButtonFormData.getTypes().length);
    // type InputExString
    IType inputExString = SdkAssert.assertTypeExists(radioButtonFormData, "InputExString");
    SdkAssert.assertHasFlags(inputExString, 9);
    SdkAssert.assertHasSuperTypeSignature(inputExString, "QInputString;");
    SdkAssert.assertAnnotation(inputExString, "org.eclipse.scout.commons.annotations.Replace");

    // fields of InputExString
    SdkAssert.assertEquals("field count of 'InputExString'", 1, inputExString.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(inputExString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'InputExString'", 1, inputExString.getMethods().length);
    IMethod inputExString1 = SdkAssert.assertMethodExist(inputExString, "InputExString", new String[]{});
    SdkAssert.assertTrue(inputExString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(inputExString1, "V");

    SdkAssert.assertEquals("inner types count of 'InputExString'", 0, inputExString.getTypes().length);
    // type UsageOneUsualString
    IType usageOneUsualString = SdkAssert.assertTypeExists(radioButtonFormData, "UsageOneUsualString");
    SdkAssert.assertHasFlags(usageOneUsualString, 9);
    SdkAssert.assertHasSuperTypeSignature(usageOneUsualString, "QAbstractValueFieldData<QString;>;");

    // fields of UsageOneUsualString
    SdkAssert.assertEquals("field count of 'UsageOneUsualString'", 1, usageOneUsualString.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(usageOneUsualString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'UsageOneUsualString'", 2, usageOneUsualString.getMethods().length);
    IMethod usageOneUsualString1 = SdkAssert.assertMethodExist(usageOneUsualString, "UsageOneUsualString", new String[]{});
    SdkAssert.assertTrue(usageOneUsualString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usageOneUsualString1, "V");
    IMethod initValidationRules = SdkAssert.assertMethodExist(usageOneUsualString, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules, "V");
    SdkAssert.assertAnnotation(initValidationRules, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'UsageOneUsualString'", 0, usageOneUsualString.getTypes().length);
    // type UsedRadioButtonGroup
    IType usedRadioButtonGroup = SdkAssert.assertTypeExists(radioButtonFormData, "UsedRadioButtonGroup");
    SdkAssert.assertHasFlags(usedRadioButtonGroup, 9);
    SdkAssert.assertHasSuperTypeSignature(usedRadioButtonGroup, "QAbstractRadioButtonGroupWithFieldsData;");

    // fields of UsedRadioButtonGroup
    SdkAssert.assertEquals("field count of 'UsedRadioButtonGroup'", 1, usedRadioButtonGroup.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(usedRadioButtonGroup, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'UsedRadioButtonGroup'", 1, usedRadioButtonGroup.getMethods().length);
    IMethod usedRadioButtonGroup1 = SdkAssert.assertMethodExist(usedRadioButtonGroup, "UsedRadioButtonGroup", new String[]{});
    SdkAssert.assertTrue(usedRadioButtonGroup1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usedRadioButtonGroup1, "V");

    SdkAssert.assertEquals("inner types count of 'UsedRadioButtonGroup'", 0, usedRadioButtonGroup.getTypes().length);
    // type UsualRadioButtonGroup
    IType usualRadioButtonGroup = SdkAssert.assertTypeExists(radioButtonFormData, "UsualRadioButtonGroup");
    SdkAssert.assertHasFlags(usualRadioButtonGroup, 9);
    SdkAssert.assertHasSuperTypeSignature(usualRadioButtonGroup, "QAbstractValueFieldData<QString;>;");

    // fields of UsualRadioButtonGroup
    SdkAssert.assertEquals("field count of 'UsualRadioButtonGroup'", 1, usualRadioButtonGroup.getFields().length);
    IField serialVersionUID4 = SdkAssert.assertFieldExist(usualRadioButtonGroup, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    SdkAssert.assertEquals("method count of 'UsualRadioButtonGroup'", 1, usualRadioButtonGroup.getMethods().length);
    IMethod usualRadioButtonGroup1 = SdkAssert.assertMethodExist(usualRadioButtonGroup, "UsualRadioButtonGroup", new String[]{});
    SdkAssert.assertTrue(usualRadioButtonGroup1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usualRadioButtonGroup1, "V");

    SdkAssert.assertEquals("inner types count of 'UsualRadioButtonGroup'", 0, usualRadioButtonGroup.getTypes().length);
    // type UsualString
    IType usualString = SdkAssert.assertTypeExists(radioButtonFormData, "UsualString");
    SdkAssert.assertHasFlags(usualString, 9);
    SdkAssert.assertHasSuperTypeSignature(usualString, "QAbstractValueFieldData<QString;>;");

    // fields of UsualString
    SdkAssert.assertEquals("field count of 'UsualString'", 1, usualString.getFields().length);
    IField serialVersionUID5 = SdkAssert.assertFieldExist(usualString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    SdkAssert.assertEquals("method count of 'UsualString'", 2, usualString.getMethods().length);
    IMethod usualString1 = SdkAssert.assertMethodExist(usualString, "UsualString", new String[]{});
    SdkAssert.assertTrue(usualString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(usualString1, "V");
    IMethod initValidationRules1 = SdkAssert.assertMethodExist(usualString, "initValidationRules", new String[]{"QMap<QString;QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(initValidationRules1, "V");
    SdkAssert.assertAnnotation(initValidationRules1, "java.lang.Override");
    SdkAssert.assertMethodValidationRules(initValidationRules1, new String[]{"ruleMap.put(ValidationRule.MAX_LENGTH, 4000);"}, true);

    SdkAssert.assertEquals("inner types count of 'UsualString'", 0, usualString.getTypes().length);
  }
}
