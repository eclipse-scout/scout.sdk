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
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperInterfaces;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertTypeExists;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createPageDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createRowDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link PageBeanDataTest}</h3>
 *
 * @since 3.10.0 2013-08-19
 */
public class PageBeanDataTest {

  @Test
  public void testPageWithTableExtensionData() {
    createRowDataAssertNoCompileErrors("formdata.client.ui.desktop.outline.pages.PageWithTableExtension", PageBeanDataTest::testApiOfPageWithTableExtensionData);
  }

  @Test
  public void testAbstractTableField() {
    createPageDataAssertNoCompileErrors("formdata.client.ui.desktop.outline.pages.BaseTablePage", PageBeanDataTest::testApiOfBaseTablePageData);
  }

  @Test
  public void testExtendedTablePage() {
    createPageDataAssertNoCompileErrors("formdata.client.ui.desktop.outline.pages.ExtendedTablePage", PageBeanDataTest::testApiOfExtendedTablePageData);
  }

  @Test
  public void testExtendedTablePageWithoutExtendedTable() {
    createPageDataAssertNoCompileErrors("formdata.client.ui.desktop.outline.pages.ExtendedTablePageWithoutExtendedTable", PageBeanDataTest::testApiOfExtendedTablePageWithoutExtendedTableData);
  }

  @Test
  public void testBaseWithExtendedTableTablePage() {
    createPageDataAssertNoCompileErrors("formdata.client.ui.desktop.outline.pages.BaseWithExtendedTableTablePage", PageBeanDataTest::testApiOfBaseWithExtendedTableTablePageData);
  }

  @Test
  public void testExtendedExtendedTablePageWithExtendedTable() {
    createPageDataAssertNoCompileErrors("formdata.client.ui.desktop.outline.pages.ExtendedExtendedTablePageWithExtendedTable", PageBeanDataTest::testApiOfExtendedExtendedTablePageWithExtendedTableData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfBaseTablePageData(IType baseTablePageData) {
    // type BaseTablePageData
    assertHasFlags(baseTablePageData, 1);
    assertHasSuperClass(baseTablePageData, "org.eclipse.scout.rt.shared.data.page.AbstractTablePageData");

    // fields of BaseTablePageData
    assertEquals(1, baseTablePageData.fields().stream().count(), "field count of 'BaseTablePageData'");
    var serialVersionUID = assertFieldExist(baseTablePageData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(7, baseTablePageData.methods().stream().count(), "method count of 'BaseTablePageData'");
    var addRow = assertMethodExist(baseTablePageData, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.pages.BaseTablePageData$BaseTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(baseTablePageData, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.pages.BaseTablePageData$BaseTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(baseTablePageData, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.pages.BaseTablePageData$BaseTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(baseTablePageData, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(baseTablePageData, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.pages.BaseTablePageData$BaseTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(baseTablePageData, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.pages.BaseTablePageData$BaseTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(baseTablePageData, "setRows", new String[]{"formdata.shared.services.pages.BaseTablePageData$BaseTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, baseTablePageData.innerTypes().stream().count(), "inner types count of 'BaseTablePageData'");
    // type BaseTableRowData
    var baseTableRowData = assertTypeExists(baseTablePageData, "BaseTableRowData");
    assertHasFlags(baseTableRowData, 9);
    assertHasSuperClass(baseTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of BaseTableRowData
    assertEquals(5, baseTableRowData.fields().stream().count(), "field count of 'BaseTableRowData'");
    var serialVersionUID1 = assertFieldExist(baseTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    var first = assertFieldExist(baseTableRowData, "first");
    assertHasFlags(first, 25);
    assertFieldType(first, String.class.getName());
    var second = assertFieldExist(baseTableRowData, "second");
    assertHasFlags(second, 25);
    assertFieldType(second, String.class.getName());
    var m_first = assertFieldExist(baseTableRowData, "m_first");
    assertHasFlags(m_first, 2);
    assertFieldType(m_first, String.class.getName());
    var m_second = assertFieldExist(baseTableRowData, "m_second");
    assertHasFlags(m_second, 2);
    assertFieldType(m_second, "java.util.Date");

    assertEquals(4, baseTableRowData.methods().stream().count(), "method count of 'BaseTableRowData'");
    var getFirst = assertMethodExist(baseTableRowData, "getFirst");
    assertMethodReturnType(getFirst, String.class.getName());
    var setFirst = assertMethodExist(baseTableRowData, "setFirst", new String[]{String.class.getName()});
    assertMethodReturnType(setFirst, "void");
    var getSecond = assertMethodExist(baseTableRowData, "getSecond");
    assertMethodReturnType(getSecond, "java.util.Date");
    var setSecond = assertMethodExist(baseTableRowData, "setSecond", new String[]{"java.util.Date"});
    assertMethodReturnType(setSecond, "void");

    assertEquals(0, baseTableRowData.innerTypes().stream().count(), "inner types count of 'BaseTableRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfExtendedTablePageData(IType extendedTablePageData) {
    var scoutApi = extendedTablePageData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(extendedTablePageData, Flags.AccPublic);
    assertHasSuperClass(extendedTablePageData, "formdata.shared.services.pages.BaseTablePageData");
    assertEquals(1, extendedTablePageData.annotations().stream().count(), "annotation count");
    assertAnnotation(extendedTablePageData, scoutApi.Generated());

    // fields of ExtendedTablePageData
    assertEquals(1, extendedTablePageData.fields().stream().count(), "field count of 'formdata.shared.services.pages.ExtendedTablePageData'");
    var serialVersionUID = assertFieldExist(extendedTablePageData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(7, extendedTablePageData.methods().stream().count(), "method count of 'formdata.shared.services.pages.ExtendedTablePageData'");
    var addRow = assertMethodExist(extendedTablePageData, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(extendedTablePageData, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(extendedTablePageData, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(extendedTablePageData, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(extendedTablePageData, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(extendedTablePageData, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(extendedTablePageData, "setRows", new String[]{"formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, extendedTablePageData.innerTypes().stream().count(), "inner types count of 'ExtendedTablePageData'");
    // type ExtendedTableRowData
    var extendedTableRowData = assertTypeExists(extendedTablePageData, "ExtendedTableRowData");
    assertHasFlags(extendedTableRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(extendedTableRowData, "formdata.shared.services.pages.BaseTablePageData$BaseTableRowData");
    assertEquals(0, extendedTableRowData.annotations().stream().count(), "annotation count");

    // fields of ExtendedTableRowData
    assertEquals(5, extendedTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData'");
    var serialVersionUID1 = assertFieldExist(extendedTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");
    var intermediate = assertFieldExist(extendedTableRowData, "intermediate");
    assertHasFlags(intermediate, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(intermediate, "java.lang.String");
    assertEquals(0, intermediate.annotations().stream().count(), "annotation count");
    var ignoredColumnEx = assertFieldExist(extendedTableRowData, "ignoredColumnEx");
    assertHasFlags(ignoredColumnEx, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(ignoredColumnEx, "java.lang.String");
    assertEquals(0, ignoredColumnEx.annotations().stream().count(), "annotation count");
    var m_intermediate = assertFieldExist(extendedTableRowData, "m_intermediate");
    assertHasFlags(m_intermediate, Flags.AccPrivate);
    assertFieldType(m_intermediate, "java.math.BigDecimal");
    assertEquals(0, m_intermediate.annotations().stream().count(), "annotation count");
    var m_ignoredColumnEx = assertFieldExist(extendedTableRowData, "m_ignoredColumnEx");
    assertHasFlags(m_ignoredColumnEx, Flags.AccPrivate);
    assertFieldType(m_ignoredColumnEx, "java.util.Date");
    assertEquals(0, m_ignoredColumnEx.annotations().stream().count(), "annotation count");

    assertEquals(4, extendedTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData'");
    var getIntermediate = assertMethodExist(extendedTableRowData, "getIntermediate");
    assertMethodReturnType(getIntermediate, "java.math.BigDecimal");
    assertEquals(0, getIntermediate.annotations().stream().count(), "annotation count");
    var setIntermediate = assertMethodExist(extendedTableRowData, "setIntermediate", new String[]{"java.math.BigDecimal"});
    assertMethodReturnType(setIntermediate, "void");
    assertEquals(0, setIntermediate.annotations().stream().count(), "annotation count");
    var getIgnoredColumnEx = assertMethodExist(extendedTableRowData, "getIgnoredColumnEx");
    assertMethodReturnType(getIgnoredColumnEx, "java.util.Date");
    assertEquals(0, getIgnoredColumnEx.annotations().stream().count(), "annotation count");
    var setIgnoredColumnEx = assertMethodExist(extendedTableRowData, "setIgnoredColumnEx", new String[]{"java.util.Date"});
    assertMethodReturnType(setIgnoredColumnEx, "void");
    assertEquals(0, setIgnoredColumnEx.annotations().stream().count(), "annotation count");

    assertEquals(0, extendedTableRowData.innerTypes().stream().count(), "inner types count of 'ExtendedTableRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfExtendedTablePageWithoutExtendedTableData(IType extendedTablePageWithoutExtendedTableData) {
    // type ExtendedTablePageWithoutExtendedTableData
    assertHasFlags(extendedTablePageWithoutExtendedTableData, 1);
    assertHasSuperClass(extendedTablePageWithoutExtendedTableData, "formdata.shared.services.pages.BaseTablePageData");

    // fields of ExtendedTablePageWithoutExtendedTableData
    assertEquals(1, extendedTablePageWithoutExtendedTableData.fields().stream().count(), "field count of 'ExtendedTablePageWithoutExtendedTableData'");
    var serialVersionUID = assertFieldExist(extendedTablePageWithoutExtendedTableData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(7, extendedTablePageWithoutExtendedTableData.methods().stream().count(), "method count of 'ExtendedTablePageWithoutExtendedTableData'");
    var addRow = assertMethodExist(extendedTablePageWithoutExtendedTableData, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.pages.ExtendedTablePageWithoutExtendedTableData$ExtendedTablePageWithoutExtendedTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(extendedTablePageWithoutExtendedTableData, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.pages.ExtendedTablePageWithoutExtendedTableData$ExtendedTablePageWithoutExtendedTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(extendedTablePageWithoutExtendedTableData, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.pages.ExtendedTablePageWithoutExtendedTableData$ExtendedTablePageWithoutExtendedTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(extendedTablePageWithoutExtendedTableData, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(extendedTablePageWithoutExtendedTableData, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.pages.ExtendedTablePageWithoutExtendedTableData$ExtendedTablePageWithoutExtendedTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(extendedTablePageWithoutExtendedTableData, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.pages.ExtendedTablePageWithoutExtendedTableData$ExtendedTablePageWithoutExtendedTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows =
        assertMethodExist(extendedTablePageWithoutExtendedTableData, "setRows", new String[]{"formdata.shared.services.pages.ExtendedTablePageWithoutExtendedTableData$ExtendedTablePageWithoutExtendedTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, extendedTablePageWithoutExtendedTableData.innerTypes().stream().count(), "inner types count of 'ExtendedTablePageWithoutExtendedTableData'");
    // type ExtendedTablePageWithoutExtendedTableRowData
    var extendedTablePageWithoutExtendedTableRowData = assertTypeExists(extendedTablePageWithoutExtendedTableData, "ExtendedTablePageWithoutExtendedTableRowData");
    assertHasFlags(extendedTablePageWithoutExtendedTableRowData, 9);
    assertHasSuperClass(extendedTablePageWithoutExtendedTableRowData, "formdata.shared.services.pages.BaseTablePageData$BaseTableRowData");

    // fields of ExtendedTablePageWithoutExtendedTableRowData
    assertEquals(1, extendedTablePageWithoutExtendedTableRowData.fields().stream().count(), "field count of 'ExtendedTablePageWithoutExtendedTableRowData'");
    var serialVersionUID1 = assertFieldExist(extendedTablePageWithoutExtendedTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, extendedTablePageWithoutExtendedTableRowData.methods().stream().count(), "method count of 'ExtendedTablePageWithoutExtendedTableRowData'");

    assertEquals(0, extendedTablePageWithoutExtendedTableRowData.innerTypes().stream().count(), "inner types count of 'ExtendedTablePageWithoutExtendedTableRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfBaseWithExtendedTableTablePageData(IType baseWithExtendedTableTablePageData) {
    // type BaseWithExtendedTableTablePageData
    assertHasFlags(baseWithExtendedTableTablePageData, 1);
    assertHasSuperClass(baseWithExtendedTableTablePageData, "org.eclipse.scout.rt.shared.data.page.AbstractTablePageData");

    // fields of BaseWithExtendedTableTablePageData
    assertEquals(1, baseWithExtendedTableTablePageData.fields().stream().count(), "field count of 'BaseWithExtendedTableTablePageData'");
    var serialVersionUID = assertFieldExist(baseWithExtendedTableTablePageData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(7, baseWithExtendedTableTablePageData.methods().stream().count(), "method count of 'BaseWithExtendedTableTablePageData'");
    var addRow = assertMethodExist(baseWithExtendedTableTablePageData, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.BaseWithExtendedTableTablePageData$BaseWithExtendedTableTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(baseWithExtendedTableTablePageData, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.BaseWithExtendedTableTablePageData$BaseWithExtendedTableTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(baseWithExtendedTableTablePageData, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.BaseWithExtendedTableTablePageData$BaseWithExtendedTableTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(baseWithExtendedTableTablePageData, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(baseWithExtendedTableTablePageData, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.BaseWithExtendedTableTablePageData$BaseWithExtendedTableTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(baseWithExtendedTableTablePageData, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.BaseWithExtendedTableTablePageData$BaseWithExtendedTableTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(baseWithExtendedTableTablePageData, "setRows", new String[]{"formdata.shared.services.BaseWithExtendedTableTablePageData$BaseWithExtendedTableTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, baseWithExtendedTableTablePageData.innerTypes().stream().count(), "inner types count of 'BaseWithExtendedTableTablePageData'");
    // type BaseWithExtendedTableTableRowData
    var baseWithExtendedTableTableRowData = assertTypeExists(baseWithExtendedTableTablePageData, "BaseWithExtendedTableTableRowData");
    assertHasFlags(baseWithExtendedTableTableRowData, 9);
    assertHasSuperClass(baseWithExtendedTableTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of BaseWithExtendedTableTableRowData
    assertEquals(5, baseWithExtendedTableTableRowData.fields().stream().count(), "field count of 'BaseWithExtendedTableTableRowData'");
    var serialVersionUID1 = assertFieldExist(baseWithExtendedTableTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    var colInAbstractTable = assertFieldExist(baseWithExtendedTableTableRowData, "colInAbstractTable");
    assertHasFlags(colInAbstractTable, 25);
    assertFieldType(colInAbstractTable, String.class.getName());
    var colInTable = assertFieldExist(baseWithExtendedTableTableRowData, "colInTable");
    assertHasFlags(colInTable, 25);
    assertFieldType(colInTable, String.class.getName());
    var m_colInAbstractTable = assertFieldExist(baseWithExtendedTableTableRowData, "m_colInAbstractTable");
    assertHasFlags(m_colInAbstractTable, 2);
    assertFieldType(m_colInAbstractTable, String.class.getName());
    var m_colInTable = assertFieldExist(baseWithExtendedTableTableRowData, "m_colInTable");
    assertHasFlags(m_colInTable, 2);
    assertFieldType(m_colInTable, String.class.getName());

    assertEquals(4, baseWithExtendedTableTableRowData.methods().stream().count(), "method count of 'BaseWithExtendedTableTableRowData'");
    var getColInAbstractTable = assertMethodExist(baseWithExtendedTableTableRowData, "getColInAbstractTable");
    assertMethodReturnType(getColInAbstractTable, String.class.getName());
    var setColInAbstractTable = assertMethodExist(baseWithExtendedTableTableRowData, "setColInAbstractTable", new String[]{String.class.getName()});
    assertMethodReturnType(setColInAbstractTable, "void");
    var getColInTable = assertMethodExist(baseWithExtendedTableTableRowData, "getColInTable");
    assertMethodReturnType(getColInTable, String.class.getName());
    var setColInTable = assertMethodExist(baseWithExtendedTableTableRowData, "setColInTable", new String[]{String.class.getName()});
    assertMethodReturnType(setColInTable, "void");

    assertEquals(0, baseWithExtendedTableTableRowData.innerTypes().stream().count(), "inner types count of 'BaseWithExtendedTableTableRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfExtendedExtendedTablePageWithExtendedTableData(IType extendedExtendedTablePageWithExtendedTableData) {
    // type ExtendedExtendedTablePageWithExtendedTableData
    assertHasFlags(extendedExtendedTablePageWithExtendedTableData, 1);
    assertHasSuperClass(extendedExtendedTablePageWithExtendedTableData, "formdata.shared.services.pages.ExtendedTablePageWithoutExtendedTableData");

    // fields of ExtendedExtendedTablePageWithExtendedTableData
    assertEquals(1, extendedExtendedTablePageWithExtendedTableData.fields().stream().count(), "field count of 'ExtendedExtendedTablePageWithExtendedTableData'");
    var serialVersionUID = assertFieldExist(extendedExtendedTablePageWithExtendedTableData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(7, extendedExtendedTablePageWithExtendedTableData.methods().stream().count(), "method count of 'ExtendedExtendedTablePageWithExtendedTableData'");
    var addRow = assertMethodExist(extendedExtendedTablePageWithExtendedTableData, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.pages.ExtendedExtendedTablePageWithExtendedTableData$ExtendedExtendedTablePageWithExtendedTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(extendedExtendedTablePageWithExtendedTableData, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.pages.ExtendedExtendedTablePageWithExtendedTableData$ExtendedExtendedTablePageWithExtendedTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(extendedExtendedTablePageWithExtendedTableData, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.pages.ExtendedExtendedTablePageWithExtendedTableData$ExtendedExtendedTablePageWithExtendedTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(extendedExtendedTablePageWithExtendedTableData, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(extendedExtendedTablePageWithExtendedTableData, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.pages.ExtendedExtendedTablePageWithExtendedTableData$ExtendedExtendedTablePageWithExtendedTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(extendedExtendedTablePageWithExtendedTableData, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.pages.ExtendedExtendedTablePageWithExtendedTableData$ExtendedExtendedTablePageWithExtendedTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(extendedExtendedTablePageWithExtendedTableData, "setRows",
        new String[]{"formdata.shared.services.pages.ExtendedExtendedTablePageWithExtendedTableData$ExtendedExtendedTablePageWithExtendedTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, extendedExtendedTablePageWithExtendedTableData.innerTypes().stream().count(), "inner types count of 'ExtendedExtendedTablePageWithExtendedTableData'");
    // type ExtendedExtendedTablePageWithExtendedTableRowData
    var extendedExtendedTablePageWithExtendedTableRowData = assertTypeExists(extendedExtendedTablePageWithExtendedTableData, "ExtendedExtendedTablePageWithExtendedTableRowData");
    assertHasFlags(extendedExtendedTablePageWithExtendedTableRowData, 9);
    assertHasSuperClass(extendedExtendedTablePageWithExtendedTableRowData, "formdata.shared.services.pages.ExtendedTablePageWithoutExtendedTableData$ExtendedTablePageWithoutExtendedTableRowData");

    // fields of ExtendedExtendedTablePageWithExtendedTableRowData
    assertEquals(3, extendedExtendedTablePageWithExtendedTableRowData.fields().stream().count(), "field count of 'ExtendedExtendedTablePageWithExtendedTableRowData'");
    var serialVersionUID1 = assertFieldExist(extendedExtendedTablePageWithExtendedTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    var boolean_ = assertFieldExist(extendedExtendedTablePageWithExtendedTableRowData, "boolean_");
    assertHasFlags(boolean_, 25);
    assertFieldType(boolean_, String.class.getName());
    var m_boolean = assertFieldExist(extendedExtendedTablePageWithExtendedTableRowData, "m_boolean");
    assertHasFlags(m_boolean, 2);
    assertFieldType(m_boolean, Boolean.class.getName());

    assertEquals(2, extendedExtendedTablePageWithExtendedTableRowData.methods().stream().count(), "method count of 'ExtendedExtendedTablePageWithExtendedTableRowData'");
    var getBoolean = assertMethodExist(extendedExtendedTablePageWithExtendedTableRowData, "getBoolean");
    assertMethodReturnType(getBoolean, Boolean.class.getName());
    var setBoolean = assertMethodExist(extendedExtendedTablePageWithExtendedTableRowData, "setBoolean", new String[]{Boolean.class.getName()});
    assertMethodReturnType(setBoolean, "void");

    assertEquals(0, extendedExtendedTablePageWithExtendedTableRowData.innerTypes().stream().count(), "inner types count of 'ExtendedExtendedTablePageWithExtendedTableRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfPageWithTableExtensionData(IType pageWithTableExtensionData) {
    var scoutApi = pageWithTableExtensionData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(pageWithTableExtensionData, Flags.AccPublic);
    assertHasSuperClass(pageWithTableExtensionData, "java.lang.Object");
    assertHasSuperInterfaces(pageWithTableExtensionData, new String[]{"java.io.Serializable"});
    assertEquals(2, pageWithTableExtensionData.annotations().stream().count(), "annotation count");
    assertAnnotation(pageWithTableExtensionData, scoutApi.Extends());
    assertAnnotation(pageWithTableExtensionData, scoutApi.Generated());

    // fields of PageWithTableExtensionData
    assertEquals(3, pageWithTableExtensionData.fields().stream().count(), "field count of 'formdata.shared.services.pages.PageWithTableExtensionData'");
    var serialVersionUID = assertFieldExist(pageWithTableExtensionData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");
    var bigDecimalTest = assertFieldExist(pageWithTableExtensionData, "bigDecimalTest");
    assertHasFlags(bigDecimalTest, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(bigDecimalTest, "java.lang.String");
    assertEquals(0, bigDecimalTest.annotations().stream().count(), "annotation count");
    var m_bigDecimalTest = assertFieldExist(pageWithTableExtensionData, "m_bigDecimalTest");
    assertHasFlags(m_bigDecimalTest, Flags.AccPrivate);
    assertFieldType(m_bigDecimalTest, "java.math.BigDecimal");
    assertEquals(0, m_bigDecimalTest.annotations().stream().count(), "annotation count");

    assertEquals(2, pageWithTableExtensionData.methods().stream().count(), "method count of 'formdata.shared.services.pages.PageWithTableExtensionData'");
    var getBigDecimalTest = assertMethodExist(pageWithTableExtensionData, "getBigDecimalTest");
    assertMethodReturnType(getBigDecimalTest, "java.math.BigDecimal");
    assertEquals(0, getBigDecimalTest.annotations().stream().count(), "annotation count");
    var setBigDecimalTest = assertMethodExist(pageWithTableExtensionData, "setBigDecimalTest", new String[]{"java.math.BigDecimal"});
    assertMethodReturnType(setBigDecimalTest, "void");
    assertEquals(0, setBigDecimalTest.annotations().stream().count(), "annotation count");

    assertEquals(0, pageWithTableExtensionData.innerTypes().stream().count(), "inner types count of 'PageWithTableExtensionData'");
  }
}
