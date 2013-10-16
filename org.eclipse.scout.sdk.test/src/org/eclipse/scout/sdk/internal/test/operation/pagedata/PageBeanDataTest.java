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
package org.eclipse.scout.sdk.internal.test.operation.pagedata;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.test.operation.formdata.AbstractSdkTestWithFormDataProject;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.pagedata.PageDataDtoUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Test;

/**
 * <h3>{@link PageBeanDataTest}</h3>
 * 
 * @author aho
 * @since 3.10.0 19.08.2013
 */
public class PageBeanDataTest extends AbstractSdkTestWithFormDataProject {

  public static final String BaseTablePage = "formdata.client.ui.desktop.outline.pages.BaseTablePage";
  public static final String ExtendedTablePage = "formdata.client.ui.desktop.outline.pages.ExtendedTablePage";
  public static final String ExtendedTablePageWithoutExtendedTableFqn = "formdata.client.ui.desktop.outline.pages.ExtendedTablePageWithoutExtendedTable";
  public static final String BaseWithExtendedTableTablePage = "formdata.client.ui.desktop.outline.pages.BaseWithExtendedTableTablePage";

  private IType createPageData(String typeName) throws Exception {
    IType field = SdkAssert.assertTypeExists(typeName);

    ITypeHierarchy superTypeHierarchy = TypeUtility.getSuperTypeHierarchy(field);
    PageDataAnnotation annotation = ScoutTypeUtility.findPageDataAnnotation(field, superTypeHierarchy);
    PageDataDtoUpdateOperation op = new PageDataDtoUpdateOperation(field, annotation);
    TestWorkspaceUtility.executeAndBuildWorkspace(op);

    IType pageData = op.getDerivedType();
    return pageData;
  }

  @Test
  public void testAbstractTableField() throws Exception {
    createPageData(BaseTablePage);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfBaseTablePageData();
  }

  @Test
  public void testExtendedTablePage() throws Exception {
    createPageData(ExtendedTablePage);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfExtendedTablePageData();
  }

  @Test
  public void testExtendedTablePageWithoutExtendedTable() throws Exception {
    createPageData(ExtendedTablePageWithoutExtendedTableFqn);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfExtendedTablePageWithoutExtendedTableData();
  }

  @Test
  public void testBaseWithExtendedTableTablePage() throws Exception {
    createPageData(BaseWithExtendedTableTablePage);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfBaseWithExtendedTableTablePageData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfBaseTablePageData() throws Exception {
    // type BaseTablePageData
    IType baseTablePageData = SdkAssert.assertTypeExists("formdata.shared.services.pages.BaseTablePageData");
    SdkAssert.assertHasFlags(baseTablePageData, 1);
    SdkAssert.assertHasSuperTypeSignature(baseTablePageData, "QAbstractTablePageData;");

    // fields of BaseTablePageData
    SdkAssert.assertEquals("field count of 'BaseTablePageData'", 1, baseTablePageData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(baseTablePageData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'BaseTablePageData'", 8, baseTablePageData.getMethods().length);
    IMethod baseTablePageData1 = SdkAssert.assertMethodExist(baseTablePageData, "BaseTablePageData", new String[]{});
    SdkAssert.assertTrue(baseTablePageData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(baseTablePageData1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(baseTablePageData, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QBaseTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(baseTablePageData, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QBaseTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(baseTablePageData, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QBaseTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(baseTablePageData, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(baseTablePageData, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QBaseTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(baseTablePageData, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QBaseTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(baseTablePageData, "setRows", new String[]{"[QBaseTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'BaseTablePageData'", 1, baseTablePageData.getTypes().length);
    // type BaseTableRowData
    IType baseTableRowData = SdkAssert.assertTypeExists(baseTablePageData, "BaseTableRowData");
    SdkAssert.assertHasFlags(baseTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(baseTableRowData, "QAbstractTableRowData;");

    // fields of BaseTableRowData
    SdkAssert.assertEquals("field count of 'BaseTableRowData'", 5, baseTableRowData.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(baseTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    IField first = SdkAssert.assertFieldExist(baseTableRowData, "first");
    SdkAssert.assertHasFlags(first, 25);
    SdkAssert.assertFieldSignature(first, "QString;");
    IField second = SdkAssert.assertFieldExist(baseTableRowData, "second");
    SdkAssert.assertHasFlags(second, 25);
    SdkAssert.assertFieldSignature(second, "QString;");
    IField m_first = SdkAssert.assertFieldExist(baseTableRowData, "m_first");
    SdkAssert.assertHasFlags(m_first, 2);
    SdkAssert.assertFieldSignature(m_first, "QString;");
    IField m_second = SdkAssert.assertFieldExist(baseTableRowData, "m_second");
    SdkAssert.assertHasFlags(m_second, 2);
    SdkAssert.assertFieldSignature(m_second, "QDate;");

    SdkAssert.assertEquals("method count of 'BaseTableRowData'", 5, baseTableRowData.getMethods().length);
    IMethod baseTableRowData1 = SdkAssert.assertMethodExist(baseTableRowData, "BaseTableRowData", new String[]{});
    SdkAssert.assertTrue(baseTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(baseTableRowData1, "V");
    IMethod getFirst = SdkAssert.assertMethodExist(baseTableRowData, "getFirst", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirst, "QString;");
    IMethod setFirst = SdkAssert.assertMethodExist(baseTableRowData, "setFirst", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setFirst, "V");
    IMethod getSecond = SdkAssert.assertMethodExist(baseTableRowData, "getSecond", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecond, "QDate;");
    IMethod setSecond = SdkAssert.assertMethodExist(baseTableRowData, "setSecond", new String[]{"QDate;"});
    SdkAssert.assertMethodReturnTypeSignature(setSecond, "V");

    SdkAssert.assertEquals("inner types count of 'BaseTableRowData'", 0, baseTableRowData.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfExtendedTablePageData() throws Exception {
    // type ExtendedTablePageData
    IType extendedTablePageData = SdkAssert.assertTypeExists("formdata.shared.services.pages.ExtendedTablePageData");
    SdkAssert.assertHasFlags(extendedTablePageData, 1);
    SdkAssert.assertHasSuperTypeSignature(extendedTablePageData, "QBaseTablePageData;");

    // fields of ExtendedTablePageData
    SdkAssert.assertEquals("field count of 'ExtendedTablePageData'", 1, extendedTablePageData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(extendedTablePageData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'ExtendedTablePageData'", 8, extendedTablePageData.getMethods().length);
    IMethod extendedTablePageData1 = SdkAssert.assertMethodExist(extendedTablePageData, "ExtendedTablePageData", new String[]{});
    SdkAssert.assertTrue(extendedTablePageData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedTablePageData1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(extendedTablePageData, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QExtendedTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(extendedTablePageData, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QExtendedTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(extendedTablePageData, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QExtendedTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(extendedTablePageData, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(extendedTablePageData, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QExtendedTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(extendedTablePageData, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QExtendedTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(extendedTablePageData, "setRows", new String[]{"[QExtendedTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'ExtendedTablePageData'", 1, extendedTablePageData.getTypes().length);
    // type ExtendedTableRowData
    IType extendedTableRowData = SdkAssert.assertTypeExists(extendedTablePageData, "ExtendedTableRowData");
    SdkAssert.assertHasFlags(extendedTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(extendedTableRowData, "QBaseTableRowData;");

    // fields of ExtendedTableRowData
    SdkAssert.assertEquals("field count of 'ExtendedTableRowData'", 3, extendedTableRowData.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(extendedTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    IField intermediate = SdkAssert.assertFieldExist(extendedTableRowData, "intermediate");
    SdkAssert.assertHasFlags(intermediate, 25);
    SdkAssert.assertFieldSignature(intermediate, "QString;");
    IField m_intermediate = SdkAssert.assertFieldExist(extendedTableRowData, "m_intermediate");
    SdkAssert.assertHasFlags(m_intermediate, 2);
    SdkAssert.assertFieldSignature(m_intermediate, "QBigDecimal;");

    SdkAssert.assertEquals("method count of 'ExtendedTableRowData'", 3, extendedTableRowData.getMethods().length);
    IMethod extendedTableRowData1 = SdkAssert.assertMethodExist(extendedTableRowData, "ExtendedTableRowData", new String[]{});
    SdkAssert.assertTrue(extendedTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedTableRowData1, "V");
    IMethod getIntermediate = SdkAssert.assertMethodExist(extendedTableRowData, "getIntermediate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIntermediate, "QBigDecimal;");
    IMethod setIntermediate = SdkAssert.assertMethodExist(extendedTableRowData, "setIntermediate", new String[]{"QBigDecimal;"});
    SdkAssert.assertMethodReturnTypeSignature(setIntermediate, "V");

    SdkAssert.assertEquals("inner types count of 'ExtendedTableRowData'", 0, extendedTableRowData.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfExtendedTablePageWithoutExtendedTableData() throws Exception {
    // type ExtendedTablePageWithoutExtendedTableData
    IType extendedTablePageWithoutExtendedTableData = SdkAssert.assertTypeExists("formdata.shared.services.pages.ExtendedTablePageWithoutExtendedTableData");
    SdkAssert.assertHasFlags(extendedTablePageWithoutExtendedTableData, 1);
    SdkAssert.assertHasSuperTypeSignature(extendedTablePageWithoutExtendedTableData, "QBaseTablePageData;");

    // fields of ExtendedTablePageWithoutExtendedTableData
    SdkAssert.assertEquals("field count of 'ExtendedTablePageWithoutExtendedTableData'", 1, extendedTablePageWithoutExtendedTableData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(extendedTablePageWithoutExtendedTableData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'ExtendedTablePageWithoutExtendedTableData'", 8, extendedTablePageWithoutExtendedTableData.getMethods().length);
    IMethod extendedTablePageWithoutExtendedTableData1 = SdkAssert.assertMethodExist(extendedTablePageWithoutExtendedTableData, "ExtendedTablePageWithoutExtendedTableData", new String[]{});
    SdkAssert.assertTrue(extendedTablePageWithoutExtendedTableData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedTablePageWithoutExtendedTableData1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(extendedTablePageWithoutExtendedTableData, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QBaseTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(extendedTablePageWithoutExtendedTableData, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QBaseTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(extendedTablePageWithoutExtendedTableData, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QBaseTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(extendedTablePageWithoutExtendedTableData, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(extendedTablePageWithoutExtendedTableData, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QBaseTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(extendedTablePageWithoutExtendedTableData, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QBaseTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(extendedTablePageWithoutExtendedTableData, "setRows", new String[]{"[QBaseTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'ExtendedTablePageWithoutExtendedTableData'", 1, extendedTablePageWithoutExtendedTableData.getTypes().length);
    // type BaseTableRowData
    IType baseTableRowData = SdkAssert.assertTypeExists(extendedTablePageWithoutExtendedTableData, "BaseTableRowData");
    SdkAssert.assertHasFlags(baseTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(baseTableRowData, "Qformdata.shared.services.pages.BaseTablePageData.BaseTableRowData;");

    // fields of BaseTableRowData
    SdkAssert.assertEquals("field count of 'BaseTableRowData'", 1, baseTableRowData.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(baseTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'BaseTableRowData'", 1, baseTableRowData.getMethods().length);
    IMethod baseTableRowData1 = SdkAssert.assertMethodExist(baseTableRowData, "BaseTableRowData", new String[]{});
    SdkAssert.assertTrue(baseTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(baseTableRowData1, "V");

    SdkAssert.assertEquals("inner types count of 'BaseTableRowData'", 0, baseTableRowData.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfBaseWithExtendedTableTablePageData() throws Exception {
    // type BaseWithExtendedTableTablePageData
    IType baseWithExtendedTableTablePageData = SdkAssert.assertTypeExists("formdata.shared.services.BaseWithExtendedTableTablePageData");
    SdkAssert.assertHasFlags(baseWithExtendedTableTablePageData, 1);
    SdkAssert.assertHasSuperTypeSignature(baseWithExtendedTableTablePageData, "QAbstractTablePageData;");

    // fields of BaseWithExtendedTableTablePageData
    SdkAssert.assertEquals("field count of 'BaseWithExtendedTableTablePageData'", 1, baseWithExtendedTableTablePageData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(baseWithExtendedTableTablePageData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'BaseWithExtendedTableTablePageData'", 8, baseWithExtendedTableTablePageData.getMethods().length);
    IMethod baseWithExtendedTableTablePageData1 = SdkAssert.assertMethodExist(baseWithExtendedTableTablePageData, "BaseWithExtendedTableTablePageData", new String[]{});
    SdkAssert.assertTrue(baseWithExtendedTableTablePageData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(baseWithExtendedTableTablePageData1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(baseWithExtendedTableTablePageData, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QBaseWithExtendedTableTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(baseWithExtendedTableTablePageData, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QBaseWithExtendedTableTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(baseWithExtendedTableTablePageData, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QBaseWithExtendedTableTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(baseWithExtendedTableTablePageData, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(baseWithExtendedTableTablePageData, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QBaseWithExtendedTableTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(baseWithExtendedTableTablePageData, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QBaseWithExtendedTableTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(baseWithExtendedTableTablePageData, "setRows", new String[]{"[QBaseWithExtendedTableTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'BaseWithExtendedTableTablePageData'", 1, baseWithExtendedTableTablePageData.getTypes().length);
    // type BaseWithExtendedTableTableRowData
    IType baseWithExtendedTableTableRowData = SdkAssert.assertTypeExists(baseWithExtendedTableTablePageData, "BaseWithExtendedTableTableRowData");
    SdkAssert.assertHasFlags(baseWithExtendedTableTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(baseWithExtendedTableTableRowData, "QAbstractTableRowData;");

    // fields of BaseWithExtendedTableTableRowData
    SdkAssert.assertEquals("field count of 'BaseWithExtendedTableTableRowData'", 5, baseWithExtendedTableTableRowData.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(baseWithExtendedTableTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    IField colInAbstractTable = SdkAssert.assertFieldExist(baseWithExtendedTableTableRowData, "colInAbstractTable");
    SdkAssert.assertHasFlags(colInAbstractTable, 25);
    SdkAssert.assertFieldSignature(colInAbstractTable, "QString;");
    IField colInTable = SdkAssert.assertFieldExist(baseWithExtendedTableTableRowData, "colInTable");
    SdkAssert.assertHasFlags(colInTable, 25);
    SdkAssert.assertFieldSignature(colInTable, "QString;");
    IField m_colInAbstractTable = SdkAssert.assertFieldExist(baseWithExtendedTableTableRowData, "m_colInAbstractTable");
    SdkAssert.assertHasFlags(m_colInAbstractTable, 2);
    SdkAssert.assertFieldSignature(m_colInAbstractTable, "QString;");
    IField m_colInTable = SdkAssert.assertFieldExist(baseWithExtendedTableTableRowData, "m_colInTable");
    SdkAssert.assertHasFlags(m_colInTable, 2);
    SdkAssert.assertFieldSignature(m_colInTable, "QString;");

    SdkAssert.assertEquals("method count of 'BaseWithExtendedTableTableRowData'", 5, baseWithExtendedTableTableRowData.getMethods().length);
    IMethod baseWithExtendedTableTableRowData1 = SdkAssert.assertMethodExist(baseWithExtendedTableTableRowData, "BaseWithExtendedTableTableRowData", new String[]{});
    SdkAssert.assertTrue(baseWithExtendedTableTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(baseWithExtendedTableTableRowData1, "V");
    IMethod getColInAbstractTable = SdkAssert.assertMethodExist(baseWithExtendedTableTableRowData, "getColInAbstractTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getColInAbstractTable, "QString;");
    IMethod setColInAbstractTable = SdkAssert.assertMethodExist(baseWithExtendedTableTableRowData, "setColInAbstractTable", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setColInAbstractTable, "V");
    IMethod getColInTable = SdkAssert.assertMethodExist(baseWithExtendedTableTableRowData, "getColInTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getColInTable, "QString;");
    IMethod setColInTable = SdkAssert.assertMethodExist(baseWithExtendedTableTableRowData, "setColInTable", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setColInTable, "V");

    SdkAssert.assertEquals("inner types count of 'BaseWithExtendedTableTableRowData'", 0, baseWithExtendedTableTableRowData.getTypes().length);
  }

}
