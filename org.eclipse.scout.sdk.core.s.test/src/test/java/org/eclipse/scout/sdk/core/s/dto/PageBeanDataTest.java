/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createPageDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createRowDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperInterfaces;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.math.BigDecimal;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.desktop.outline.pages.BaseTablePage;
import formdata.client.ui.desktop.outline.pages.BaseWithExtendedTableTablePage;
import formdata.client.ui.desktop.outline.pages.ExtendedExtendedTablePageWithExtendedTable;
import formdata.client.ui.desktop.outline.pages.ExtendedTablePage;
import formdata.client.ui.desktop.outline.pages.ExtendedTablePageWithoutExtendedTable;
import formdata.client.ui.desktop.outline.pages.PageWithTableExtension;

/**
 * <h3>{@link PageBeanDataTest}</h3>
 *
 * @since 3.10.0 2013-08-19
 */
public class PageBeanDataTest {

  @Test
  public void testPageWithTableExtensionData() {
    createRowDataAssertNoCompileErrors(PageWithTableExtension.class.getName(), PageBeanDataTest::testApiOfPageWithTableExtensionData);
  }

  @Test
  public void testAbstractTableField() {
    createPageDataAssertNoCompileErrors(BaseTablePage.class.getName(), PageBeanDataTest::testApiOfBaseTablePageData);
  }

  @Test
  public void testExtendedTablePage() {
    createPageDataAssertNoCompileErrors(ExtendedTablePage.class.getName(), PageBeanDataTest::testApiOfExtendedTablePageData);
  }

  @Test
  public void testExtendedTablePageWithoutExtendedTable() {
    createPageDataAssertNoCompileErrors(ExtendedTablePageWithoutExtendedTable.class.getName(), PageBeanDataTest::testApiOfExtendedTablePageWithoutExtendedTableData);
  }

  @Test
  public void testBaseWithExtendedTableTablePage() {
    createPageDataAssertNoCompileErrors(BaseWithExtendedTableTablePage.class.getName(), PageBeanDataTest::testApiOfBaseWithExtendedTableTablePageData);
  }

  @Test
  public void testExtendedExtendedTablePageWithExtendedTable() {
    createPageDataAssertNoCompileErrors(ExtendedExtendedTablePageWithExtendedTable.class.getName(), PageBeanDataTest::testApiOfExtendedExtendedTablePageWithExtendedTableData);
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
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfExtendedTablePageData(IType extendedTablePageData) {
    // type ExtendedTablePageData
    assertHasFlags(extendedTablePageData, 1);
    assertHasSuperClass(extendedTablePageData, "formdata.shared.services.pages.BaseTablePageData");
    assertAnnotation(extendedTablePageData, "javax.annotation.Generated");

    // fields of ExtendedTablePageData
    assertEquals(1, extendedTablePageData.fields().stream().count(), "field count of 'ExtendedTablePageData'");
    var serialVersionUID = assertFieldExist(extendedTablePageData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(7, extendedTablePageData.methods().stream().count(), "method count of 'ExtendedTablePageData'");
    var addRow = assertMethodExist(extendedTablePageData, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(extendedTablePageData, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(extendedTablePageData, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(extendedTablePageData, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(extendedTablePageData, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(extendedTablePageData, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(extendedTablePageData, "setRows", new String[]{"formdata.shared.services.pages.ExtendedTablePageData$ExtendedTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, extendedTablePageData.innerTypes().stream().count(), "inner types count of 'ExtendedTablePageData'");
    // type ExtendedTableRowData
    var extendedTableRowData = assertTypeExists(extendedTablePageData, "ExtendedTableRowData");
    assertHasFlags(extendedTableRowData, 9);
    assertHasSuperClass(extendedTableRowData, "formdata.shared.services.pages.BaseTablePageData$BaseTableRowData");

    // fields of ExtendedTableRowData
    assertEquals(5, extendedTableRowData.fields().stream().count(), "field count of 'ExtendedTableRowData'");
    var serialVersionUID1 = assertFieldExist(extendedTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    var intermediate = assertFieldExist(extendedTableRowData, "intermediate");
    assertHasFlags(intermediate, 25);
    assertFieldType(intermediate, String.class.getName());
    var ignoredColumnEx = assertFieldExist(extendedTableRowData, "ignoredColumnEx");
    assertHasFlags(ignoredColumnEx, 25);
    assertFieldType(ignoredColumnEx, String.class.getName());
    var m_intermediate = assertFieldExist(extendedTableRowData, "m_intermediate");
    assertHasFlags(m_intermediate, 2);
    assertFieldType(m_intermediate, BigDecimal.class.getName());
    var m_ignoredColumnEx = assertFieldExist(extendedTableRowData, "m_ignoredColumnEx");
    assertHasFlags(m_ignoredColumnEx, 2);
    assertFieldType(m_ignoredColumnEx, "java.util.Date");

    assertEquals(4, extendedTableRowData.methods().stream().count(), "method count of 'ExtendedTableRowData'");
    var getIntermediate = assertMethodExist(extendedTableRowData, "getIntermediate");
    assertMethodReturnType(getIntermediate, BigDecimal.class.getName());
    var setIntermediate = assertMethodExist(extendedTableRowData, "setIntermediate", new String[]{BigDecimal.class.getName()});
    assertMethodReturnType(setIntermediate, "void");
    var getIgnoredColumnEx = assertMethodExist(extendedTableRowData, "getIgnoredColumnEx");
    assertMethodReturnType(getIgnoredColumnEx, "java.util.Date");
    var setIgnoredColumnEx = assertMethodExist(extendedTableRowData, "setIgnoredColumnEx", new String[]{"java.util.Date"});
    assertMethodReturnType(setIgnoredColumnEx, "void");

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
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfPageWithTableExtensionData(IType pageWithTableExtensionData) {
    // type PageWithTableExtensionData
    assertHasFlags(pageWithTableExtensionData, 1);
    assertHasSuperInterfaces(pageWithTableExtensionData, new String[]{Serializable.class.getName()});
    assertAnnotation(pageWithTableExtensionData, "org.eclipse.scout.rt.platform.extension.Extends");
    assertAnnotation(pageWithTableExtensionData, "javax.annotation.Generated");

    // fields of PageWithTableExtensionData
    assertEquals(3, pageWithTableExtensionData.fields().stream().count(), "field count of 'PageWithTableExtensionData'");
    var serialVersionUID = assertFieldExist(pageWithTableExtensionData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    var bigDecimalTest = assertFieldExist(pageWithTableExtensionData, "bigDecimalTest");
    assertHasFlags(bigDecimalTest, 25);
    assertFieldType(bigDecimalTest, String.class.getName());
    var m_bigDecimalTest = assertFieldExist(pageWithTableExtensionData, "m_bigDecimalTest");
    assertHasFlags(m_bigDecimalTest, 2);
    assertFieldType(m_bigDecimalTest, BigDecimal.class.getName());

    assertEquals(2, pageWithTableExtensionData.methods().stream().count(), "method count of 'PageWithTableExtensionData'");
    var getBigDecimalTest = assertMethodExist(pageWithTableExtensionData, "getBigDecimalTest");
    assertMethodReturnType(getBigDecimalTest, BigDecimal.class.getName());
    var setBigDecimalTest = assertMethodExist(pageWithTableExtensionData, "setBigDecimalTest", new String[]{BigDecimal.class.getName()});
    assertMethodReturnType(setBigDecimalTest, "void");

    assertEquals(0, pageWithTableExtensionData.innerTypes().stream().count(), "inner types count of 'PageWithTableExtensionData'");
  }

}
