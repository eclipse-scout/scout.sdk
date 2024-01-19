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
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link TableFieldWithIgnoredColumnsTest}</h3>
 *
 * @since 3.10.0 2013-08-19
 */
public class TableFieldWithIgnoredColumnsTest {

  @Test
  public void testTableFieldWithIgnoredColumnsBaseForm() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsBaseForm", TableFieldWithIgnoredColumnsTest::testApiOfTableFieldWithIgnoredColumnsBaseFormData);
  }

  @Test
  public void testTableFieldWithIgnoredColumnsDefaultExForm() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsDefaultExForm", TableFieldWithIgnoredColumnsTest::testApiOfTableFieldWithIgnoredColumnsDefaultExFormData);
  }

  @Test
  public void testTableFieldWithIgnoredColumnsCreateExForm() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsCreateExForm", TableFieldWithIgnoredColumnsTest::testApiOfTableFieldWithIgnoredColumnsCreateExFormData);
  }

  @Test
  public void testTableFieldWithIgnoredColumnsIgnoreExForm() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsIgnoreExForm", TableFieldWithIgnoredColumnsTest::testApiOfTableFieldWithIgnoredColumnsIgnoreExFormData);
  }

  @Test
  public void testTableFieldWithIgnoredColumnsDefaultCreateExForm() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsDefaultCreateExForm", TableFieldWithIgnoredColumnsTest::testApiOfTableFieldWithIgnoredColumnsDefaultCreateExFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfTableFieldWithIgnoredColumnsDefaultExFormData(IType tableFieldWithIgnoredColumnsDefaultExFormData) {
    var scoutApi = tableFieldWithIgnoredColumnsDefaultExFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(tableFieldWithIgnoredColumnsDefaultExFormData, Flags.AccPublic);
    assertHasSuperClass(tableFieldWithIgnoredColumnsDefaultExFormData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData");
    assertEquals(1, tableFieldWithIgnoredColumnsDefaultExFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(tableFieldWithIgnoredColumnsDefaultExFormData, scoutApi.Generated());

    // fields of TableFieldWithIgnoredColumnsDefaultExFormData
    assertEquals(1, tableFieldWithIgnoredColumnsDefaultExFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData'");
    var serialVersionUID = assertFieldExist(tableFieldWithIgnoredColumnsDefaultExFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, tableFieldWithIgnoredColumnsDefaultExFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData'");
    var getTableDefaultEx = assertMethodExist(tableFieldWithIgnoredColumnsDefaultExFormData, "getTableDefaultEx");
    assertMethodReturnType(getTableDefaultEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx");
    assertEquals(0, getTableDefaultEx.annotations().stream().count(), "annotation count");

    assertEquals(1, tableFieldWithIgnoredColumnsDefaultExFormData.innerTypes().stream().count(), "inner types count of 'TableFieldWithIgnoredColumnsDefaultExFormData'");
    // type TableDefaultEx
    var tableDefaultEx = assertTypeExists(tableFieldWithIgnoredColumnsDefaultExFormData, "TableDefaultEx");
    assertHasFlags(tableDefaultEx, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableDefaultEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase");
    assertEquals(1, tableDefaultEx.annotations().stream().count(), "annotation count");
    assertAnnotation(tableDefaultEx, scoutApi.Replace());

    // fields of TableDefaultEx
    assertEquals(1, tableDefaultEx.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx'");
    var serialVersionUID1 = assertFieldExist(tableDefaultEx, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(7, tableDefaultEx.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx'");
    var addRow = assertMethodExist(tableDefaultEx, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(tableDefaultEx, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(tableDefaultEx, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(tableDefaultEx, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(tableDefaultEx, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(tableDefaultEx, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(tableDefaultEx, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, tableDefaultEx.innerTypes().stream().count(), "inner types count of 'TableDefaultEx'");
    // type TableDefaultExRowData
    var tableDefaultExRowData = assertTypeExists(tableDefaultEx, "TableDefaultExRowData");
    assertHasFlags(tableDefaultExRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableDefaultExRowData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertEquals(0, tableDefaultExRowData.annotations().stream().count(), "annotation count");

    // fields of TableDefaultExRowData
    assertEquals(1, tableDefaultExRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData'");
    var serialVersionUID2 = assertFieldExist(tableDefaultExRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, tableDefaultExRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultExFormData$TableDefaultEx$TableDefaultExRowData'");

    assertEquals(0, tableDefaultExRowData.innerTypes().stream().count(), "inner types count of 'TableDefaultExRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfTableFieldWithIgnoredColumnsBaseFormData(IType tableFieldWithIgnoredColumnsBaseFormData) {
    var scoutApi = tableFieldWithIgnoredColumnsBaseFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(tableFieldWithIgnoredColumnsBaseFormData, Flags.AccPublic);
    assertHasSuperClass(tableFieldWithIgnoredColumnsBaseFormData, scoutApi.AbstractFormData());
    assertEquals(1, tableFieldWithIgnoredColumnsBaseFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(tableFieldWithIgnoredColumnsBaseFormData, scoutApi.Generated());

    // fields of TableFieldWithIgnoredColumnsBaseFormData
    assertEquals(1, tableFieldWithIgnoredColumnsBaseFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData'");
    var serialVersionUID = assertFieldExist(tableFieldWithIgnoredColumnsBaseFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, tableFieldWithIgnoredColumnsBaseFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData'");
    var getTableBase = assertMethodExist(tableFieldWithIgnoredColumnsBaseFormData, "getTableBase");
    assertMethodReturnType(getTableBase, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase");
    assertEquals(0, getTableBase.annotations().stream().count(), "annotation count");

    assertEquals(1, tableFieldWithIgnoredColumnsBaseFormData.innerTypes().stream().count(), "inner types count of 'TableFieldWithIgnoredColumnsBaseFormData'");
    // type TableBase
    var tableBase = assertTypeExists(tableFieldWithIgnoredColumnsBaseFormData, "TableBase");
    assertHasFlags(tableBase, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableBase, scoutApi.AbstractTableFieldBeanData());
    assertEquals(0, tableBase.annotations().stream().count(), "annotation count");

    // fields of TableBase
    assertEquals(1, tableBase.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase'");
    var serialVersionUID1 = assertFieldExist(tableBase, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(7, tableBase.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase'");
    var addRow = assertMethodExist(tableBase, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(tableBase, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(tableBase, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(tableBase, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(tableBase, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(tableBase, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(tableBase, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, tableBase.innerTypes().stream().count(), "inner types count of 'TableBase'");
    // type TableBaseRowData
    var tableBaseRowData = assertTypeExists(tableBase, "TableBaseRowData");
    assertHasFlags(tableBaseRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableBaseRowData, scoutApi.AbstractTableRowData());
    assertEquals(0, tableBaseRowData.annotations().stream().count(), "annotation count");

    // fields of TableBaseRowData
    assertEquals(5, tableBaseRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData'");
    var serialVersionUID2 = assertFieldExist(tableBaseRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");
    var default_ = assertFieldExist(tableBaseRowData, "default_");
    assertHasFlags(default_, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(default_, "java.lang.String");
    assertEquals(0, default_.annotations().stream().count(), "annotation count");
    var create = assertFieldExist(tableBaseRowData, "create");
    assertHasFlags(create, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(create, "java.lang.String");
    assertEquals(0, create.annotations().stream().count(), "annotation count");
    var m_default = assertFieldExist(tableBaseRowData, "m_default");
    assertHasFlags(m_default, Flags.AccPrivate);
    assertFieldType(m_default, "java.lang.String");
    assertEquals(0, m_default.annotations().stream().count(), "annotation count");
    var m_create = assertFieldExist(tableBaseRowData, "m_create");
    assertHasFlags(m_create, Flags.AccPrivate);
    assertFieldType(m_create, "java.lang.String");
    assertEquals(0, m_create.annotations().stream().count(), "annotation count");

    assertEquals(4, tableBaseRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData'");
    var getDefault = assertMethodExist(tableBaseRowData, "getDefault");
    assertMethodReturnType(getDefault, "java.lang.String");
    assertEquals(0, getDefault.annotations().stream().count(), "annotation count");
    var setDefault = assertMethodExist(tableBaseRowData, "setDefault", new String[]{"java.lang.String"});
    assertMethodReturnType(setDefault, "void");
    assertEquals(0, setDefault.annotations().stream().count(), "annotation count");
    var getCreate = assertMethodExist(tableBaseRowData, "getCreate");
    assertMethodReturnType(getCreate, "java.lang.String");
    assertEquals(0, getCreate.annotations().stream().count(), "annotation count");
    var setCreate = assertMethodExist(tableBaseRowData, "setCreate", new String[]{"java.lang.String"});
    assertMethodReturnType(setCreate, "void");
    assertEquals(0, setCreate.annotations().stream().count(), "annotation count");

    assertEquals(0, tableBaseRowData.innerTypes().stream().count(), "inner types count of 'TableBaseRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfTableFieldWithIgnoredColumnsIgnoreExFormData(IType tableFieldWithIgnoredColumnsIgnoreExFormData) {
    var scoutApi = tableFieldWithIgnoredColumnsIgnoreExFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(tableFieldWithIgnoredColumnsIgnoreExFormData, Flags.AccPublic);
    assertHasSuperClass(tableFieldWithIgnoredColumnsIgnoreExFormData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData");
    assertEquals(1, tableFieldWithIgnoredColumnsIgnoreExFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(tableFieldWithIgnoredColumnsIgnoreExFormData, scoutApi.Generated());

    // fields of TableFieldWithIgnoredColumnsIgnoreExFormData
    assertEquals(1, tableFieldWithIgnoredColumnsIgnoreExFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData'");
    var serialVersionUID = assertFieldExist(tableFieldWithIgnoredColumnsIgnoreExFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, tableFieldWithIgnoredColumnsIgnoreExFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData'");
    var getTableIgnoreEx = assertMethodExist(tableFieldWithIgnoredColumnsIgnoreExFormData, "getTableIgnoreEx");
    assertMethodReturnType(getTableIgnoreEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx");
    assertEquals(0, getTableIgnoreEx.annotations().stream().count(), "annotation count");

    assertEquals(1, tableFieldWithIgnoredColumnsIgnoreExFormData.innerTypes().stream().count(), "inner types count of 'TableFieldWithIgnoredColumnsIgnoreExFormData'");
    // type TableIgnoreEx
    var tableIgnoreEx = assertTypeExists(tableFieldWithIgnoredColumnsIgnoreExFormData, "TableIgnoreEx");
    assertHasFlags(tableIgnoreEx, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableIgnoreEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase");
    assertEquals(1, tableIgnoreEx.annotations().stream().count(), "annotation count");
    assertAnnotation(tableIgnoreEx, scoutApi.Replace());

    // fields of TableIgnoreEx
    assertEquals(1, tableIgnoreEx.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx'");
    var serialVersionUID1 = assertFieldExist(tableIgnoreEx, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(7, tableIgnoreEx.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx'");
    var addRow = assertMethodExist(tableIgnoreEx, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(tableIgnoreEx, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(tableIgnoreEx, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(tableIgnoreEx, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(tableIgnoreEx, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(tableIgnoreEx, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(tableIgnoreEx, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, tableIgnoreEx.innerTypes().stream().count(), "inner types count of 'TableIgnoreEx'");
    // type TableIgnoreExRowData
    var tableIgnoreExRowData = assertTypeExists(tableIgnoreEx, "TableIgnoreExRowData");
    assertHasFlags(tableIgnoreExRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableIgnoreExRowData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertEquals(0, tableIgnoreExRowData.annotations().stream().count(), "annotation count");

    // fields of TableIgnoreExRowData
    assertEquals(1, tableIgnoreExRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData'");
    var serialVersionUID2 = assertFieldExist(tableIgnoreExRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, tableIgnoreExRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData$TableIgnoreEx$TableIgnoreExRowData'");

    assertEquals(0, tableIgnoreExRowData.innerTypes().stream().count(), "inner types count of 'TableIgnoreExRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfTableFieldWithIgnoredColumnsDefaultCreateExFormData(IType tableFieldWithIgnoredColumnsDefaultCreateExFormData) {
    var scoutApi = tableFieldWithIgnoredColumnsDefaultCreateExFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(tableFieldWithIgnoredColumnsDefaultCreateExFormData, Flags.AccPublic);
    assertHasSuperClass(tableFieldWithIgnoredColumnsDefaultCreateExFormData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData");
    assertEquals(1, tableFieldWithIgnoredColumnsDefaultCreateExFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(tableFieldWithIgnoredColumnsDefaultCreateExFormData, scoutApi.Generated());

    // fields of TableFieldWithIgnoredColumnsDefaultCreateExFormData
    assertEquals(1, tableFieldWithIgnoredColumnsDefaultCreateExFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData'");
    var serialVersionUID = assertFieldExist(tableFieldWithIgnoredColumnsDefaultCreateExFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, tableFieldWithIgnoredColumnsDefaultCreateExFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData'");
    var getTableDefaultCreateEx = assertMethodExist(tableFieldWithIgnoredColumnsDefaultCreateExFormData, "getTableDefaultCreateEx");
    assertMethodReturnType(getTableDefaultCreateEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx");
    assertEquals(0, getTableDefaultCreateEx.annotations().stream().count(), "annotation count");

    assertEquals(1, tableFieldWithIgnoredColumnsDefaultCreateExFormData.innerTypes().stream().count(), "inner types count of 'TableFieldWithIgnoredColumnsDefaultCreateExFormData'");
    // type TableDefaultCreateEx
    var tableDefaultCreateEx = assertTypeExists(tableFieldWithIgnoredColumnsDefaultCreateExFormData, "TableDefaultCreateEx");
    assertHasFlags(tableDefaultCreateEx, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableDefaultCreateEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase");
    assertEquals(1, tableDefaultCreateEx.annotations().stream().count(), "annotation count");
    assertAnnotation(tableDefaultCreateEx, scoutApi.Replace());

    // fields of TableDefaultCreateEx
    assertEquals(1, tableDefaultCreateEx.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx'");
    var serialVersionUID1 = assertFieldExist(tableDefaultCreateEx, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(7, tableDefaultCreateEx.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx'");
    var addRow = assertMethodExist(tableDefaultCreateEx, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(tableDefaultCreateEx, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(tableDefaultCreateEx, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(tableDefaultCreateEx, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(tableDefaultCreateEx, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(tableDefaultCreateEx, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(tableDefaultCreateEx, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, tableDefaultCreateEx.innerTypes().stream().count(), "inner types count of 'TableDefaultCreateEx'");
    // type TableDefaultCreateExRowData
    var tableDefaultCreateExRowData = assertTypeExists(tableDefaultCreateEx, "TableDefaultCreateExRowData");
    assertHasFlags(tableDefaultCreateExRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableDefaultCreateExRowData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertEquals(0, tableDefaultCreateExRowData.annotations().stream().count(), "annotation count");

    // fields of TableDefaultCreateExRowData
    assertEquals(3, tableDefaultCreateExRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData'");
    var serialVersionUID2 = assertFieldExist(tableDefaultCreateExRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");
    var ignoreDefaultCreate = assertFieldExist(tableDefaultCreateExRowData, "ignoreDefaultCreate");
    assertHasFlags(ignoreDefaultCreate, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(ignoreDefaultCreate, "java.lang.String");
    assertEquals(0, ignoreDefaultCreate.annotations().stream().count(), "annotation count");
    var m_ignoreDefaultCreate = assertFieldExist(tableDefaultCreateExRowData, "m_ignoreDefaultCreate");
    assertHasFlags(m_ignoreDefaultCreate, Flags.AccPrivate);
    assertFieldType(m_ignoreDefaultCreate, "java.lang.String");
    assertEquals(0, m_ignoreDefaultCreate.annotations().stream().count(), "annotation count");

    assertEquals(2, tableDefaultCreateExRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData$TableDefaultCreateEx$TableDefaultCreateExRowData'");
    var getIgnoreDefaultCreate = assertMethodExist(tableDefaultCreateExRowData, "getIgnoreDefaultCreate");
    assertMethodReturnType(getIgnoreDefaultCreate, "java.lang.String");
    assertEquals(0, getIgnoreDefaultCreate.annotations().stream().count(), "annotation count");
    var setIgnoreDefaultCreate = assertMethodExist(tableDefaultCreateExRowData, "setIgnoreDefaultCreate", new String[]{"java.lang.String"});
    assertMethodReturnType(setIgnoreDefaultCreate, "void");
    assertEquals(0, setIgnoreDefaultCreate.annotations().stream().count(), "annotation count");

    assertEquals(0, tableDefaultCreateExRowData.innerTypes().stream().count(), "inner types count of 'TableDefaultCreateExRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfTableFieldWithIgnoredColumnsCreateExFormData(IType tableFieldWithIgnoredColumnsCreateExFormData) {
    var scoutApi = tableFieldWithIgnoredColumnsCreateExFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(tableFieldWithIgnoredColumnsCreateExFormData, Flags.AccPublic);
    assertHasSuperClass(tableFieldWithIgnoredColumnsCreateExFormData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData");
    assertEquals(1, tableFieldWithIgnoredColumnsCreateExFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(tableFieldWithIgnoredColumnsCreateExFormData, scoutApi.Generated());

    // fields of TableFieldWithIgnoredColumnsCreateExFormData
    assertEquals(1, tableFieldWithIgnoredColumnsCreateExFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData'");
    var serialVersionUID = assertFieldExist(tableFieldWithIgnoredColumnsCreateExFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, tableFieldWithIgnoredColumnsCreateExFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData'");
    var getTableCreateEx = assertMethodExist(tableFieldWithIgnoredColumnsCreateExFormData, "getTableCreateEx");
    assertMethodReturnType(getTableCreateEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx");
    assertEquals(0, getTableCreateEx.annotations().stream().count(), "annotation count");

    assertEquals(1, tableFieldWithIgnoredColumnsCreateExFormData.innerTypes().stream().count(), "inner types count of 'TableFieldWithIgnoredColumnsCreateExFormData'");
    // type TableCreateEx
    var tableCreateEx = assertTypeExists(tableFieldWithIgnoredColumnsCreateExFormData, "TableCreateEx");
    assertHasFlags(tableCreateEx, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableCreateEx, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase");
    assertEquals(1, tableCreateEx.annotations().stream().count(), "annotation count");
    assertAnnotation(tableCreateEx, scoutApi.Replace());

    // fields of TableCreateEx
    assertEquals(1, tableCreateEx.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx'");
    var serialVersionUID1 = assertFieldExist(tableCreateEx, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(7, tableCreateEx.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx'");
    var addRow = assertMethodExist(tableCreateEx, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(tableCreateEx, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(tableCreateEx, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(tableCreateEx, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(tableCreateEx, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(tableCreateEx, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(tableCreateEx, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, tableCreateEx.innerTypes().stream().count(), "inner types count of 'TableCreateEx'");
    // type TableCreateExRowData
    var tableCreateExRowData = assertTypeExists(tableCreateEx, "TableCreateExRowData");
    assertHasFlags(tableCreateExRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableCreateExRowData, "formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData$TableBase$TableBaseRowData");
    assertEquals(0, tableCreateExRowData.annotations().stream().count(), "annotation count");

    // fields of TableCreateExRowData
    assertEquals(3, tableCreateExRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData'");
    var serialVersionUID2 = assertFieldExist(tableCreateExRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");
    var ignoreCreate = assertFieldExist(tableCreateExRowData, "ignoreCreate");
    assertHasFlags(ignoreCreate, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(ignoreCreate, "java.lang.String");
    assertEquals(0, ignoreCreate.annotations().stream().count(), "annotation count");
    var m_ignoreCreate = assertFieldExist(tableCreateExRowData, "m_ignoreCreate");
    assertHasFlags(m_ignoreCreate, Flags.AccPrivate);
    assertFieldType(m_ignoreCreate, "java.lang.String");
    assertEquals(0, m_ignoreCreate.annotations().stream().count(), "annotation count");

    assertEquals(2, tableCreateExRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData$TableCreateEx$TableCreateExRowData'");
    var getIgnoreCreate = assertMethodExist(tableCreateExRowData, "getIgnoreCreate");
    assertMethodReturnType(getIgnoreCreate, "java.lang.String");
    assertEquals(0, getIgnoreCreate.annotations().stream().count(), "annotation count");
    var setIgnoreCreate = assertMethodExist(tableCreateExRowData, "setIgnoreCreate", new String[]{"java.lang.String"});
    assertMethodReturnType(setIgnoreCreate, "void");
    assertEquals(0, setIgnoreCreate.annotations().stream().count(), "annotation count");

    assertEquals(0, tableCreateExRowData.innerTypes().stream().count(), "inner types count of 'TableCreateExRowData'");
  }
}
