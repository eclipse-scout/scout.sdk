/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsBaseForm;
import formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsCreateExForm;
import formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsDefaultCreateExForm;
import formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsDefaultExForm;
import formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsIgnoreExForm;

/**
 * <h3>{@link TableFieldWithIgnoredColumnsTest}</h3>
 *
 * @since 3.10.0 2013-08-19
 */
public class TableFieldWithIgnoredColumnsTest {

  @Test
  public void testTableFieldWithIgnoredColumnsBaseForm() {
    createFormDataAssertNoCompileErrors(TableFieldWithIgnoredColumnsBaseForm.class.getName(), TableFieldWithIgnoredColumnsTest::testApiOfTableFieldWithIgnoredColumnsBaseFormData);
  }

  @Test
  public void testTableFieldWithIgnoredColumnsDefaultExForm() {
    createFormDataAssertNoCompileErrors(TableFieldWithIgnoredColumnsDefaultExForm.class.getName(), TableFieldWithIgnoredColumnsTest::testApiOfTableFieldWithIgnoredColumnsDefaultExFormData);
  }

  @Test
  public void testTableFieldWithIgnoredColumnsCreateExForm() {
    createFormDataAssertNoCompileErrors(TableFieldWithIgnoredColumnsCreateExForm.class.getName(), TableFieldWithIgnoredColumnsTest::testApiOfTableFieldWithIgnoredColumnsCreateExFormData);
  }

  @Test
  public void testTableFieldWithIgnoredColumnsIgnoreExForm() {
    createFormDataAssertNoCompileErrors(TableFieldWithIgnoredColumnsIgnoreExForm.class.getName(), TableFieldWithIgnoredColumnsTest::testApiOfTableFieldWithIgnoredColumnsIgnoreExFormData);
  }

  @Test
  public void testTableFieldWithIgnoredColumnsDefaultCreateExForm() {
    createFormDataAssertNoCompileErrors(TableFieldWithIgnoredColumnsDefaultCreateExForm.class.getName(), TableFieldWithIgnoredColumnsTest::testApiOfTableFieldWithIgnoredColumnsDefaultCreateExFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfTableFieldWithIgnoredColumnsDefaultExFormData(IType tableFieldWithIgnoredColumnsDefaultExFormData) {
    // type TableFieldWithIgnoredColumnsDefaultExFormData
    assertHasFlags(tableFieldWithIgnoredColumnsDefaultExFormData, 1);
    assertHasSuperClass(tableFieldWithIgnoredColumnsDefaultExFormData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData");
    assertAnnotation(tableFieldWithIgnoredColumnsDefaultExFormData, "javax.annotation.Generated");

    // fields of TableFieldWithIgnoredColumnsDefaultExFormData
    assertEquals(1, tableFieldWithIgnoredColumnsDefaultExFormData.fields().stream().count(), "field count of 'TableFieldWithIgnoredColumnsDefaultExFormData'");
    IField serialVersionUID = assertFieldExist(tableFieldWithIgnoredColumnsDefaultExFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, tableFieldWithIgnoredColumnsDefaultExFormData.methods().stream().count(), "method count of 'TableFieldWithIgnoredColumnsDefaultExFormData'");
    IMethod getTableDefaultEx = assertMethodExist(tableFieldWithIgnoredColumnsDefaultExFormData, "getTableDefaultEx", new String[]{});
    assertMethodReturnType(getTableDefaultEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx");

    assertEquals(1, tableFieldWithIgnoredColumnsDefaultExFormData.innerTypes().stream().count(), "inner types count of 'TableFieldWithIgnoredColumnsDefaultExFormData'");
    // type TableDefaultEx
    IType tableDefaultEx = assertTypeExists(tableFieldWithIgnoredColumnsDefaultExFormData, "TableDefaultEx");
    assertHasFlags(tableDefaultEx, 9);
    assertHasSuperClass(tableDefaultEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase");
    assertAnnotation(tableDefaultEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of TableDefaultEx
    assertEquals(1, tableDefaultEx.fields().stream().count(), "field count of 'TableDefaultEx'");
    IField serialVersionUID1 = assertFieldExist(tableDefaultEx, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, tableDefaultEx.methods().stream().count(), "method count of 'TableDefaultEx'");
    IMethod addRow = assertMethodExist(tableDefaultEx, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(tableDefaultEx, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(tableDefaultEx, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(tableDefaultEx, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(tableDefaultEx, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(tableDefaultEx, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = assertMethodExist(tableDefaultEx, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, tableDefaultEx.innerTypes().stream().count(), "inner types count of 'TableDefaultEx'");
    // type TableDefaultExRowData
    IType tableDefaultExRowData = assertTypeExists(tableDefaultEx, "TableDefaultExRowData");
    assertHasFlags(tableDefaultExRowData, 9);
    assertHasSuperClass(tableDefaultExRowData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");

    // fields of TableDefaultExRowData
    assertEquals(1, tableDefaultExRowData.fields().stream().count(), "field count of 'TableDefaultExRowData'");
    IField serialVersionUID2 = assertFieldExist(tableDefaultExRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, tableDefaultExRowData.methods().stream().count(), "method count of 'TableDefaultExRowData'");

    assertEquals(0, tableDefaultExRowData.innerTypes().stream().count(), "inner types count of 'TableDefaultExRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfTableFieldWithIgnoredColumnsBaseFormData(IType tableFieldWithIgnoredColumnsBaseFormData) {
    // type TableFieldWithIgnoredColumnsBaseFormData
    assertHasFlags(tableFieldWithIgnoredColumnsBaseFormData, 1);
    assertHasSuperClass(tableFieldWithIgnoredColumnsBaseFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");
    assertAnnotation(tableFieldWithIgnoredColumnsBaseFormData, "javax.annotation.Generated");

    // fields of TableFieldWithIgnoredColumnsBaseFormData
    assertEquals(1, tableFieldWithIgnoredColumnsBaseFormData.fields().stream().count(), "field count of 'TableFieldWithIgnoredColumnsBaseFormData'");
    IField serialVersionUID = assertFieldExist(tableFieldWithIgnoredColumnsBaseFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, tableFieldWithIgnoredColumnsBaseFormData.methods().stream().count(), "method count of 'TableFieldWithIgnoredColumnsBaseFormData'");
    IMethod getTableBase = assertMethodExist(tableFieldWithIgnoredColumnsBaseFormData, "getTableBase", new String[]{});
    assertMethodReturnType(getTableBase, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase");

    assertEquals(1, tableFieldWithIgnoredColumnsBaseFormData.innerTypes().stream().count(), "inner types count of 'TableFieldWithIgnoredColumnsBaseFormData'");
    // type TableBase
    IType tableBase = assertTypeExists(tableFieldWithIgnoredColumnsBaseFormData, "TableBase");
    assertHasFlags(tableBase, 9);
    assertHasSuperClass(tableBase, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of TableBase
    assertEquals(1, tableBase.fields().stream().count(), "field count of 'TableBase'");
    IField serialVersionUID1 = assertFieldExist(tableBase, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, tableBase.methods().stream().count(), "method count of 'TableBase'");
    IMethod addRow = assertMethodExist(tableBase, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(tableBase, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(tableBase, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(tableBase, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(tableBase, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(tableBase, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = assertMethodExist(tableBase, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, tableBase.innerTypes().stream().count(), "inner types count of 'TableBase'");
    // type TableBaseRowData
    IType tableBaseRowData = assertTypeExists(tableBase, "TableBaseRowData");
    assertHasFlags(tableBaseRowData, 9);
    assertHasSuperClass(tableBaseRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of TableBaseRowData
    assertEquals(5, tableBaseRowData.fields().stream().count(), "field count of 'TableBaseRowData'");
    IField serialVersionUID2 = assertFieldExist(tableBaseRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    IField default_ = assertFieldExist(tableBaseRowData, "default_");
    assertHasFlags(default_, 25);
    assertFieldType(default_, String.class.getName());
    IField create = assertFieldExist(tableBaseRowData, "create");
    assertHasFlags(create, 25);
    assertFieldType(create, String.class.getName());
    IField m_default = assertFieldExist(tableBaseRowData, "m_default");
    assertHasFlags(m_default, 2);
    assertFieldType(m_default, String.class.getName());
    IField m_create = assertFieldExist(tableBaseRowData, "m_create");
    assertHasFlags(m_create, 2);
    assertFieldType(m_create, String.class.getName());

    assertEquals(4, tableBaseRowData.methods().stream().count(), "method count of 'TableBaseRowData'");
    IMethod getDefault = assertMethodExist(tableBaseRowData, "getDefault", new String[]{});
    assertMethodReturnType(getDefault, String.class.getName());
    IMethod setDefault = assertMethodExist(tableBaseRowData, "setDefault", new String[]{String.class.getName()});
    assertMethodReturnType(setDefault, "void");
    IMethod getCreate = assertMethodExist(tableBaseRowData, "getCreate", new String[]{});
    assertMethodReturnType(getCreate, String.class.getName());
    IMethod setCreate = assertMethodExist(tableBaseRowData, "setCreate", new String[]{String.class.getName()});
    assertMethodReturnType(setCreate, "void");

    assertEquals(0, tableBaseRowData.innerTypes().stream().count(), "inner types count of 'TableBaseRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfTableFieldWithIgnoredColumnsIgnoreExFormData(IType tableFieldWithIgnoredColumnsIgnoreExFormData) {
    // type TableFieldWithIgnoredColumnsIgnoreExFormData
    assertHasFlags(tableFieldWithIgnoredColumnsIgnoreExFormData, 1);
    assertHasSuperClass(tableFieldWithIgnoredColumnsIgnoreExFormData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData");
    assertAnnotation(tableFieldWithIgnoredColumnsIgnoreExFormData, "javax.annotation.Generated");

    // fields of TableFieldWithIgnoredColumnsIgnoreExFormData
    assertEquals(1, tableFieldWithIgnoredColumnsIgnoreExFormData.fields().stream().count(), "field count of 'TableFieldWithIgnoredColumnsIgnoreExFormData'");
    IField serialVersionUID = assertFieldExist(tableFieldWithIgnoredColumnsIgnoreExFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, tableFieldWithIgnoredColumnsIgnoreExFormData.methods().stream().count(), "method count of 'TableFieldWithIgnoredColumnsIgnoreExFormData'");
    IMethod getTableIgnoreEx = assertMethodExist(tableFieldWithIgnoredColumnsIgnoreExFormData, "getTableIgnoreEx", new String[]{});
    assertMethodReturnType(getTableIgnoreEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx");

    assertEquals(1, tableFieldWithIgnoredColumnsIgnoreExFormData.innerTypes().stream().count(), "inner types count of 'TableFieldWithIgnoredColumnsIgnoreExFormData'");
    // type TableIgnoreEx
    IType tableIgnoreEx = assertTypeExists(tableFieldWithIgnoredColumnsIgnoreExFormData, "TableIgnoreEx");
    assertHasFlags(tableIgnoreEx, 9);
    assertHasSuperClass(tableIgnoreEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase");
    assertAnnotation(tableIgnoreEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of TableIgnoreEx
    assertEquals(1, tableIgnoreEx.fields().stream().count(), "field count of 'TableIgnoreEx'");
    IField serialVersionUID1 = assertFieldExist(tableIgnoreEx, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, tableIgnoreEx.methods().stream().count(), "method count of 'TableIgnoreEx'");
    IMethod addRow = assertMethodExist(tableIgnoreEx, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(tableIgnoreEx, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(tableIgnoreEx, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(tableIgnoreEx, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(tableIgnoreEx, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(tableIgnoreEx, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = assertMethodExist(tableIgnoreEx, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, tableIgnoreEx.innerTypes().stream().count(), "inner types count of 'TableIgnoreEx'");
    // type TableIgnoreExRowData
    IType tableIgnoreExRowData = assertTypeExists(tableIgnoreEx, "TableIgnoreExRowData");
    assertHasFlags(tableIgnoreExRowData, 9);
    assertHasSuperClass(tableIgnoreExRowData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");

    // fields of TableIgnoreExRowData
    assertEquals(1, tableIgnoreExRowData.fields().stream().count(), "field count of 'TableIgnoreExRowData'");
    IField serialVersionUID2 = assertFieldExist(tableIgnoreExRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, tableIgnoreExRowData.methods().stream().count(), "method count of 'TableIgnoreExRowData'");

    assertEquals(0, tableIgnoreExRowData.innerTypes().stream().count(), "inner types count of 'TableIgnoreExRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfTableFieldWithIgnoredColumnsDefaultCreateExFormData(IType tableFieldWithIgnoredColumnsDefaultCreateExFormData) {
    // type TableFieldWithIgnoredColumnsDefaultCreateExFormData
    assertHasFlags(tableFieldWithIgnoredColumnsDefaultCreateExFormData, 1);
    assertHasSuperClass(tableFieldWithIgnoredColumnsDefaultCreateExFormData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData");
    assertAnnotation(tableFieldWithIgnoredColumnsDefaultCreateExFormData, "javax.annotation.Generated");

    // fields of TableFieldWithIgnoredColumnsDefaultCreateExFormData
    assertEquals(1, tableFieldWithIgnoredColumnsDefaultCreateExFormData.fields().stream().count(), "field count of 'TableFieldWithIgnoredColumnsDefaultCreateExFormData'");
    IField serialVersionUID = assertFieldExist(tableFieldWithIgnoredColumnsDefaultCreateExFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, tableFieldWithIgnoredColumnsDefaultCreateExFormData.methods().stream().count(), "method count of 'TableFieldWithIgnoredColumnsDefaultCreateExFormData'");
    IMethod getTableDefaultCreateEx = assertMethodExist(tableFieldWithIgnoredColumnsDefaultCreateExFormData, "getTableDefaultCreateEx", new String[]{});
    assertMethodReturnType(getTableDefaultCreateEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx");

    assertEquals(1, tableFieldWithIgnoredColumnsDefaultCreateExFormData.innerTypes().stream().count(), "inner types count of 'TableFieldWithIgnoredColumnsDefaultCreateExFormData'");
    // type TableDefaultCreateEx
    IType tableDefaultCreateEx = assertTypeExists(tableFieldWithIgnoredColumnsDefaultCreateExFormData, "TableDefaultCreateEx");
    assertHasFlags(tableDefaultCreateEx, 9);
    assertHasSuperClass(tableDefaultCreateEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase");
    assertAnnotation(tableDefaultCreateEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of TableDefaultCreateEx
    assertEquals(1, tableDefaultCreateEx.fields().stream().count(), "field count of 'TableDefaultCreateEx'");
    IField serialVersionUID1 = assertFieldExist(tableDefaultCreateEx, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, tableDefaultCreateEx.methods().stream().count(), "method count of 'TableDefaultCreateEx'");
    IMethod addRow = assertMethodExist(tableDefaultCreateEx, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(tableDefaultCreateEx, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(tableDefaultCreateEx, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(tableDefaultCreateEx, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(tableDefaultCreateEx, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(tableDefaultCreateEx, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows =
        assertMethodExist(tableDefaultCreateEx, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, tableDefaultCreateEx.innerTypes().stream().count(), "inner types count of 'TableDefaultCreateEx'");
    // type TableDefaultCreateExRowData
    IType tableDefaultCreateExRowData = assertTypeExists(tableDefaultCreateEx, "TableDefaultCreateExRowData");
    assertHasFlags(tableDefaultCreateExRowData, 9);
    assertHasSuperClass(tableDefaultCreateExRowData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");

    // fields of TableDefaultCreateExRowData
    assertEquals(3, tableDefaultCreateExRowData.fields().stream().count(), "field count of 'TableDefaultCreateExRowData'");
    IField serialVersionUID2 = assertFieldExist(tableDefaultCreateExRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    IField ignoreDefaultCreate = assertFieldExist(tableDefaultCreateExRowData, "ignoreDefaultCreate");
    assertHasFlags(ignoreDefaultCreate, 25);
    assertFieldType(ignoreDefaultCreate, String.class.getName());
    IField m_ignoreDefaultCreate = assertFieldExist(tableDefaultCreateExRowData, "m_ignoreDefaultCreate");
    assertHasFlags(m_ignoreDefaultCreate, 2);
    assertFieldType(m_ignoreDefaultCreate, String.class.getName());

    assertEquals(2, tableDefaultCreateExRowData.methods().stream().count(), "method count of 'TableDefaultCreateExRowData'");
    IMethod getIgnoreDefaultCreate = assertMethodExist(tableDefaultCreateExRowData, "getIgnoreDefaultCreate", new String[]{});
    assertMethodReturnType(getIgnoreDefaultCreate, String.class.getName());
    IMethod setIgnoreDefaultCreate = assertMethodExist(tableDefaultCreateExRowData, "setIgnoreDefaultCreate", new String[]{String.class.getName()});
    assertMethodReturnType(setIgnoreDefaultCreate, "void");

    assertEquals(0, tableDefaultCreateExRowData.innerTypes().stream().count(), "inner types count of 'TableDefaultCreateExRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfTableFieldWithIgnoredColumnsCreateExFormData(IType tableFieldWithIgnoredColumnsCreateExFormData) {
    // type TableFieldWithIgnoredColumnsCreateExFormData
    assertHasFlags(tableFieldWithIgnoredColumnsCreateExFormData, 1);
    assertHasSuperClass(tableFieldWithIgnoredColumnsCreateExFormData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData");
    assertAnnotation(tableFieldWithIgnoredColumnsCreateExFormData, "javax.annotation.Generated");

    // fields of TableFieldWithIgnoredColumnsCreateExFormData
    assertEquals(1, tableFieldWithIgnoredColumnsCreateExFormData.fields().stream().count(), "field count of 'TableFieldWithIgnoredColumnsCreateExFormData'");
    IField serialVersionUID = assertFieldExist(tableFieldWithIgnoredColumnsCreateExFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, tableFieldWithIgnoredColumnsCreateExFormData.methods().stream().count(), "method count of 'TableFieldWithIgnoredColumnsCreateExFormData'");
    IMethod getTableCreateEx = assertMethodExist(tableFieldWithIgnoredColumnsCreateExFormData, "getTableCreateEx", new String[]{});
    assertMethodReturnType(getTableCreateEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx");

    assertEquals(1, tableFieldWithIgnoredColumnsCreateExFormData.innerTypes().stream().count(), "inner types count of 'TableFieldWithIgnoredColumnsCreateExFormData'");
    // type TableCreateEx
    IType tableCreateEx = assertTypeExists(tableFieldWithIgnoredColumnsCreateExFormData, "TableCreateEx");
    assertHasFlags(tableCreateEx, 9);
    assertHasSuperClass(tableCreateEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase");
    assertAnnotation(tableCreateEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of TableCreateEx
    assertEquals(1, tableCreateEx.fields().stream().count(), "field count of 'TableCreateEx'");
    IField serialVersionUID1 = assertFieldExist(tableCreateEx, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, tableCreateEx.methods().stream().count(), "method count of 'TableCreateEx'");
    IMethod addRow = assertMethodExist(tableCreateEx, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(tableCreateEx, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(tableCreateEx, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(tableCreateEx, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(tableCreateEx, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(tableCreateEx, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = assertMethodExist(tableCreateEx, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, tableCreateEx.innerTypes().stream().count(), "inner types count of 'TableCreateEx'");
    // type TableCreateExRowData
    IType tableCreateExRowData = assertTypeExists(tableCreateEx, "TableCreateExRowData");
    assertHasFlags(tableCreateExRowData, 9);
    assertHasSuperClass(tableCreateExRowData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");

    // fields of TableCreateExRowData
    assertEquals(3, tableCreateExRowData.fields().stream().count(), "field count of 'TableCreateExRowData'");
    IField serialVersionUID2 = assertFieldExist(tableCreateExRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    IField ignoreCreate = assertFieldExist(tableCreateExRowData, "ignoreCreate");
    assertHasFlags(ignoreCreate, 25);
    assertFieldType(ignoreCreate, String.class.getName());
    IField m_ignoreCreate = assertFieldExist(tableCreateExRowData, "m_ignoreCreate");
    assertHasFlags(m_ignoreCreate, 2);
    assertFieldType(m_ignoreCreate, String.class.getName());

    assertEquals(2, tableCreateExRowData.methods().stream().count(), "method count of 'TableCreateExRowData'");
    IMethod getIgnoreCreate = assertMethodExist(tableCreateExRowData, "getIgnoreCreate", new String[]{});
    assertMethodReturnType(getIgnoreCreate, String.class.getName());
    IMethod setIgnoreCreate = assertMethodExist(tableCreateExRowData, "setIgnoreCreate", new String[]{String.class.getName()});
    assertMethodReturnType(setIgnoreCreate, "void");

    assertEquals(0, tableCreateExRowData.innerTypes().stream().count(), "inner types count of 'TableCreateExRowData'");
  }
}
