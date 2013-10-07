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

    SdkAssert.assertEquals("method countcount of 'BaseTablePageData'", 8, baseTablePageData.getMethods().length);
    IMethod baseTablePageData1 = SdkAssert.assertMethodExist(baseTablePageData, "BaseTablePageData", new String[]{});
    SdkAssert.assertTrue(baseTablePageData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(baseTablePageData1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(baseTablePageData, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QBaseTablePageRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(baseTablePageData, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QBaseTablePageRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(baseTablePageData, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QBaseTablePageRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(baseTablePageData, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(baseTablePageData, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QBaseTablePageRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(baseTablePageData, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QBaseTablePageRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(baseTablePageData, "setRows", new String[]{"[QBaseTablePageRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'BaseTablePageData'", 1, baseTablePageData.getTypes().length);
    // type BaseTablePageRowData
    IType baseTablePageRowData = SdkAssert.assertTypeExists(baseTablePageData, "BaseTablePageRowData");
    SdkAssert.assertHasFlags(baseTablePageRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(baseTablePageRowData, "QAbstractTableRowData;");

    // fields of BaseTablePageRowData
    SdkAssert.assertEquals("field count of 'BaseTablePageRowData'", 5, baseTablePageRowData.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(baseTablePageRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    IField first = SdkAssert.assertFieldExist(baseTablePageRowData, "first");
    SdkAssert.assertHasFlags(first, 25);
    SdkAssert.assertFieldSignature(first, "QString;");
    IField second = SdkAssert.assertFieldExist(baseTablePageRowData, "second");
    SdkAssert.assertHasFlags(second, 25);
    SdkAssert.assertFieldSignature(second, "QString;");
    IField m_first = SdkAssert.assertFieldExist(baseTablePageRowData, "m_first");
    SdkAssert.assertHasFlags(m_first, 2);
    SdkAssert.assertFieldSignature(m_first, "QString;");
    IField m_second = SdkAssert.assertFieldExist(baseTablePageRowData, "m_second");
    SdkAssert.assertHasFlags(m_second, 2);
    SdkAssert.assertFieldSignature(m_second, "QDate;");

    SdkAssert.assertEquals("method countcount of 'BaseTablePageRowData'", 5, baseTablePageRowData.getMethods().length);
    IMethod baseTablePageRowData1 = SdkAssert.assertMethodExist(baseTablePageRowData, "BaseTablePageRowData", new String[]{});
    SdkAssert.assertTrue(baseTablePageRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(baseTablePageRowData1, "V");
    IMethod getFirst = SdkAssert.assertMethodExist(baseTablePageRowData, "getFirst", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirst, "QString;");
    IMethod setFirst = SdkAssert.assertMethodExist(baseTablePageRowData, "setFirst", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setFirst, "V");
    IMethod getSecond = SdkAssert.assertMethodExist(baseTablePageRowData, "getSecond", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecond, "QDate;");
    IMethod setSecond = SdkAssert.assertMethodExist(baseTablePageRowData, "setSecond", new String[]{"QDate;"});
    SdkAssert.assertMethodReturnTypeSignature(setSecond, "V");

    SdkAssert.assertEquals("inner types count of 'BaseTablePageRowData'", 0, baseTablePageRowData.getTypes().length);
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

    SdkAssert.assertEquals("method countcount of 'ExtendedTablePageData'", 8, extendedTablePageData.getMethods().length);
    IMethod extendedTablePageData1 = SdkAssert.assertMethodExist(extendedTablePageData, "ExtendedTablePageData", new String[]{});
    SdkAssert.assertTrue(extendedTablePageData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedTablePageData1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(extendedTablePageData, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QExtendedTablePageRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(extendedTablePageData, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QExtendedTablePageRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(extendedTablePageData, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QExtendedTablePageRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(extendedTablePageData, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(extendedTablePageData, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QExtendedTablePageRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(extendedTablePageData, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QExtendedTablePageRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(extendedTablePageData, "setRows", new String[]{"[QExtendedTablePageRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'ExtendedTablePageData'", 1, extendedTablePageData.getTypes().length);
    // type ExtendedTablePageRowData
    IType extendedTablePageRowData = SdkAssert.assertTypeExists(extendedTablePageData, "ExtendedTablePageRowData");
    SdkAssert.assertHasFlags(extendedTablePageRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(extendedTablePageRowData, "QBaseTablePageRowData;");

    // fields of ExtendedTablePageRowData
    SdkAssert.assertEquals("field count of 'ExtendedTablePageRowData'", 3, extendedTablePageRowData.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(extendedTablePageRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    IField intermediate = SdkAssert.assertFieldExist(extendedTablePageRowData, "intermediate");
    SdkAssert.assertHasFlags(intermediate, 25);
    SdkAssert.assertFieldSignature(intermediate, "QString;");
    IField m_intermediate = SdkAssert.assertFieldExist(extendedTablePageRowData, "m_intermediate");
    SdkAssert.assertHasFlags(m_intermediate, 2);
    SdkAssert.assertFieldSignature(m_intermediate, "QBigDecimal;");

    SdkAssert.assertEquals("method countcount of 'ExtendedTablePageRowData'", 3, extendedTablePageRowData.getMethods().length);
    IMethod extendedTablePageRowData1 = SdkAssert.assertMethodExist(extendedTablePageRowData, "ExtendedTablePageRowData", new String[]{});
    SdkAssert.assertTrue(extendedTablePageRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedTablePageRowData1, "V");
    IMethod getIntermediate = SdkAssert.assertMethodExist(extendedTablePageRowData, "getIntermediate", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getIntermediate, "QBigDecimal;");
    IMethod setIntermediate = SdkAssert.assertMethodExist(extendedTablePageRowData, "setIntermediate", new String[]{"QBigDecimal;"});
    SdkAssert.assertMethodReturnTypeSignature(setIntermediate, "V");

    SdkAssert.assertEquals("inner types count of 'ExtendedTablePageRowData'", 0, extendedTablePageRowData.getTypes().length);
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

    SdkAssert.assertEquals("method countcount of 'ExtendedTablePageWithoutExtendedTableData'", 1, extendedTablePageWithoutExtendedTableData.getMethods().length);
    IMethod extendedTablePageWithoutExtendedTableData1 = SdkAssert.assertMethodExist(extendedTablePageWithoutExtendedTableData, "ExtendedTablePageWithoutExtendedTableData", new String[]{});
    SdkAssert.assertTrue(extendedTablePageWithoutExtendedTableData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedTablePageWithoutExtendedTableData1, "V");

    SdkAssert.assertEquals("inner types count of 'ExtendedTablePageWithoutExtendedTableData'", 0, extendedTablePageWithoutExtendedTableData.getTypes().length);
  }

}
