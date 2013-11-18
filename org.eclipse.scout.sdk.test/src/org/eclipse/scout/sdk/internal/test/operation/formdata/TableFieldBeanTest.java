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
 * <h3>{@link TableFieldBeanTest}</h3>
 * 
 *  @author Andreas Hoegger
 * @since 3.10.0 19.08.2013
 */
public class TableFieldBeanTest extends AbstractSdkTestWithFormDataProject {

  public static final String AbstractAddressTableField = "formdata.client.ui.template.formfield.AbstractAddressTableField";
  public static final String TableFieldBaseForm = "formdata.client.ui.forms.replace.TableFieldBaseForm";
  public static final String TableFieldExForm = "formdata.client.ui.forms.replace.TableFieldExForm";

  public static final String BaseWithExtendedTableForm = "formdata.client.ui.forms.BaseWithExtendedTableForm";
  public static final String ChildWithExtendedTableForm = "formdata.client.ui.forms.ChildWithExtendedTableForm";

  private void createFormData(String typeName) throws Exception {
    IType field = SdkAssert.assertTypeExists(typeName);
    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(field);
    TestWorkspaceUtility.executeAndBuildWorkspace(op);
  }

  @Test
  public void testFormData() throws Exception {
    testAbstractTableField();
    testTableFieldBaseFormData();
    testTableFieldExFormData();
    testBaseWithExtendedTableFormData();
    testChildWithExtendedTableFormData();
  }

  private void testAbstractTableField() throws Exception {
    createFormData(AbstractAddressTableField);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfAbstractAddressTableFieldData();
  }

  private void testTableFieldBaseFormData() throws Exception {
    createFormData(TableFieldBaseForm);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfTableFieldBaseFormData();
  }

  private void testTableFieldExFormData() throws Exception {
    createFormData(TableFieldExForm);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfTableFieldExFormData();
  }

  private void testBaseWithExtendedTableFormData() throws Exception {
    createFormData(BaseWithExtendedTableForm);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfBaseWithExtendedTableFormData();
  }

  private void testChildWithExtendedTableFormData() throws Exception {
    createFormData(ChildWithExtendedTableForm);
    TestWorkspaceUtility.assertNoCompileErrors();
    testApiOfChildWithExtendedTableFormData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfAbstractAddressTableFieldData() throws Exception {
    // type AbstractAddressTableFieldData
    IType abstractAddressTableFieldData = SdkAssert.assertTypeExists("formdata.shared.services.process.AbstractAddressTableFieldData");
    SdkAssert.assertHasFlags(abstractAddressTableFieldData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractAddressTableFieldData, "QAbstractTableFieldBeanData;");

    // fields of AbstractAddressTableFieldData
    SdkAssert.assertEquals("field count of 'AbstractAddressTableFieldData'", 1, abstractAddressTableFieldData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractAddressTableFieldData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'AbstractAddressTableFieldData'", 8, abstractAddressTableFieldData.getMethods().length);
    IMethod abstractAddressTableFieldData1 = SdkAssert.assertMethodExist(abstractAddressTableFieldData, "AbstractAddressTableFieldData", new String[]{});
    SdkAssert.assertTrue(abstractAddressTableFieldData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractAddressTableFieldData1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(abstractAddressTableFieldData, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QAbstractAddressTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(abstractAddressTableFieldData, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QAbstractAddressTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(abstractAddressTableFieldData, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QAbstractAddressTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(abstractAddressTableFieldData, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(abstractAddressTableFieldData, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QAbstractAddressTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(abstractAddressTableFieldData, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QAbstractAddressTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(abstractAddressTableFieldData, "setRows", new String[]{"[QAbstractAddressTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'AbstractAddressTableFieldData'", 1, abstractAddressTableFieldData.getTypes().length);
    // type AbstractAddressTableRowData
    IType abstractAddressTableRowData = SdkAssert.assertTypeExists(abstractAddressTableFieldData, "AbstractAddressTableRowData");
    SdkAssert.assertHasFlags(abstractAddressTableRowData, 1033);
    SdkAssert.assertHasSuperTypeSignature(abstractAddressTableRowData, "QAbstractTableRowData;");

    // fields of AbstractAddressTableRowData
    SdkAssert.assertEquals("field count of 'AbstractAddressTableRowData'", 7, abstractAddressTableRowData.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(abstractAddressTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    IField addressId = SdkAssert.assertFieldExist(abstractAddressTableRowData, "addressId");
    SdkAssert.assertHasFlags(addressId, 25);
    SdkAssert.assertFieldSignature(addressId, "QString;");
    IField street = SdkAssert.assertFieldExist(abstractAddressTableRowData, "street");
    SdkAssert.assertHasFlags(street, 25);
    SdkAssert.assertFieldSignature(street, "QString;");
    IField poBoxAddress = SdkAssert.assertFieldExist(abstractAddressTableRowData, "poBoxAddress");
    SdkAssert.assertHasFlags(poBoxAddress, 25);
    SdkAssert.assertFieldSignature(poBoxAddress, "QString;");
    IField m_addressId = SdkAssert.assertFieldExist(abstractAddressTableRowData, "m_addressId");
    SdkAssert.assertHasFlags(m_addressId, 2);
    SdkAssert.assertFieldSignature(m_addressId, "QString;");
    IField m_street = SdkAssert.assertFieldExist(abstractAddressTableRowData, "m_street");
    SdkAssert.assertHasFlags(m_street, 2);
    SdkAssert.assertFieldSignature(m_street, "QString;");
    IField m_poBoxAddress = SdkAssert.assertFieldExist(abstractAddressTableRowData, "m_poBoxAddress");
    SdkAssert.assertHasFlags(m_poBoxAddress, 2);
    SdkAssert.assertFieldSignature(m_poBoxAddress, "QBoolean;");

    SdkAssert.assertEquals("method count of 'AbstractAddressTableRowData'", 7, abstractAddressTableRowData.getMethods().length);
    IMethod abstractAddressTableRowData1 = SdkAssert.assertMethodExist(abstractAddressTableRowData, "AbstractAddressTableRowData", new String[]{});
    SdkAssert.assertTrue(abstractAddressTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractAddressTableRowData1, "V");
    IMethod getAddressId = SdkAssert.assertMethodExist(abstractAddressTableRowData, "getAddressId", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getAddressId, "QString;");
    IMethod setAddressId = SdkAssert.assertMethodExist(abstractAddressTableRowData, "setAddressId", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setAddressId, "V");
    IMethod getStreet = SdkAssert.assertMethodExist(abstractAddressTableRowData, "getStreet", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getStreet, "QString;");
    IMethod setStreet = SdkAssert.assertMethodExist(abstractAddressTableRowData, "setStreet", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setStreet, "V");
    IMethod getPoBoxAddress = SdkAssert.assertMethodExist(abstractAddressTableRowData, "getPoBoxAddress", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getPoBoxAddress, "QBoolean;");
    IMethod setPoBoxAddress = SdkAssert.assertMethodExist(abstractAddressTableRowData, "setPoBoxAddress", new String[]{"QBoolean;"});
    SdkAssert.assertMethodReturnTypeSignature(setPoBoxAddress, "V");

    SdkAssert.assertEquals("inner types count of 'AbstractAddressTableRowData'", 0, abstractAddressTableRowData.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfTableFieldBaseFormData() throws Exception {
    // type TableFieldBaseFormData
    IType tableFieldBaseFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.replace.TableFieldBaseFormData");
    SdkAssert.assertHasFlags(tableFieldBaseFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(tableFieldBaseFormData, "QAbstractFormData;");

    // fields of TableFieldBaseFormData
    SdkAssert.assertEquals("field count of 'TableFieldBaseFormData'", 1, tableFieldBaseFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(tableFieldBaseFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'TableFieldBaseFormData'", 6, tableFieldBaseFormData.getMethods().length);
    IMethod tableFieldBaseFormData1 = SdkAssert.assertMethodExist(tableFieldBaseFormData, "TableFieldBaseFormData", new String[]{});
    SdkAssert.assertTrue(tableFieldBaseFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableFieldBaseFormData1, "V");
    IMethod getAddressTable = SdkAssert.assertMethodExist(tableFieldBaseFormData, "getAddressTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getAddressTable, "QAddressTable;");
    IMethod getEmptyTable = SdkAssert.assertMethodExist(tableFieldBaseFormData, "getEmptyTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getEmptyTable, "QEmptyTable;");
    IMethod getNoTable = SdkAssert.assertMethodExist(tableFieldBaseFormData, "getNoTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getNoTable, "QNoTable;");
    IMethod getPersonTable = SdkAssert.assertMethodExist(tableFieldBaseFormData, "getPersonTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getPersonTable, "QPersonTable;");
    IMethod getTable = SdkAssert.assertMethodExist(tableFieldBaseFormData, "getTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTable, "QTable;");

    SdkAssert.assertEquals("inner types count of 'TableFieldBaseFormData'", 5, tableFieldBaseFormData.getTypes().length);
    // type AddressTable
    IType addressTable = SdkAssert.assertTypeExists(tableFieldBaseFormData, "AddressTable");
    SdkAssert.assertHasFlags(addressTable, 9);
    SdkAssert.assertHasSuperTypeSignature(addressTable, "QAbstractAddressTableFieldData;");

    // fields of AddressTable
    SdkAssert.assertEquals("field count of 'AddressTable'", 1, addressTable.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(addressTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'AddressTable'", 8, addressTable.getMethods().length);
    IMethod addressTable1 = SdkAssert.assertMethodExist(addressTable, "AddressTable", new String[]{});
    SdkAssert.assertTrue(addressTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(addressTable1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(addressTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QAddressTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(addressTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QAddressTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(addressTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QAddressTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(addressTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(addressTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QAddressTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(addressTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QAddressTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(addressTable, "setRows", new String[]{"[QAddressTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'AddressTable'", 1, addressTable.getTypes().length);
    // type AddressTableRowData
    IType addressTableRowData = SdkAssert.assertTypeExists(addressTable, "AddressTableRowData");
    SdkAssert.assertHasFlags(addressTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(addressTableRowData, "QAbstractAddressTableRowData;");

    // fields of AddressTableRowData
    SdkAssert.assertEquals("field count of 'AddressTableRowData'", 3, addressTableRowData.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(addressTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");
    IField city = SdkAssert.assertFieldExist(addressTableRowData, "city");
    SdkAssert.assertHasFlags(city, 25);
    SdkAssert.assertFieldSignature(city, "QString;");
    IField m_city = SdkAssert.assertFieldExist(addressTableRowData, "m_city");
    SdkAssert.assertHasFlags(m_city, 2);
    SdkAssert.assertFieldSignature(m_city, "QString;");

    SdkAssert.assertEquals("method count of 'AddressTableRowData'", 3, addressTableRowData.getMethods().length);
    IMethod addressTableRowData1 = SdkAssert.assertMethodExist(addressTableRowData, "AddressTableRowData", new String[]{});
    SdkAssert.assertTrue(addressTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(addressTableRowData1, "V");
    IMethod getCity = SdkAssert.assertMethodExist(addressTableRowData, "getCity", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getCity, "QString;");
    IMethod setCity = SdkAssert.assertMethodExist(addressTableRowData, "setCity", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setCity, "V");

    SdkAssert.assertEquals("inner types count of 'AddressTableRowData'", 0, addressTableRowData.getTypes().length);
    // type EmptyTable
    IType emptyTable = SdkAssert.assertTypeExists(tableFieldBaseFormData, "EmptyTable");
    SdkAssert.assertHasFlags(emptyTable, 9);
    SdkAssert.assertHasSuperTypeSignature(emptyTable, "QAbstractTableFieldBeanData;");

    // fields of EmptyTable
    SdkAssert.assertEquals("field count of 'EmptyTable'", 1, emptyTable.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(emptyTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'EmptyTable'", 8, emptyTable.getMethods().length);
    IMethod emptyTable1 = SdkAssert.assertMethodExist(emptyTable, "EmptyTable", new String[]{});
    SdkAssert.assertTrue(emptyTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(emptyTable1, "V");
    IMethod addRow2 = SdkAssert.assertMethodExist(emptyTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow2, "QEmptyTableRowData;");
    SdkAssert.assertAnnotation(addRow2, "java.lang.Override");
    IMethod addRow3 = SdkAssert.assertMethodExist(emptyTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow3, "QEmptyTableRowData;");
    SdkAssert.assertAnnotation(addRow3, "java.lang.Override");
    IMethod createRow1 = SdkAssert.assertMethodExist(emptyTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow1, "QEmptyTableRowData;");
    SdkAssert.assertAnnotation(createRow1, "java.lang.Override");
    IMethod getRowType1 = SdkAssert.assertMethodExist(emptyTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType1, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType1, "java.lang.Override");
    IMethod getRows1 = SdkAssert.assertMethodExist(emptyTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows1, "[QEmptyTableRowData;");
    SdkAssert.assertAnnotation(getRows1, "java.lang.Override");
    IMethod rowAt1 = SdkAssert.assertMethodExist(emptyTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt1, "QEmptyTableRowData;");
    SdkAssert.assertAnnotation(rowAt1, "java.lang.Override");
    IMethod setRows1 = SdkAssert.assertMethodExist(emptyTable, "setRows", new String[]{"[QEmptyTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows1, "V");

    SdkAssert.assertEquals("inner types count of 'EmptyTable'", 1, emptyTable.getTypes().length);
    // type EmptyTableRowData
    IType emptyTableRowData = SdkAssert.assertTypeExists(emptyTable, "EmptyTableRowData");
    SdkAssert.assertHasFlags(emptyTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(emptyTableRowData, "QAbstractTableRowData;");

    // fields of EmptyTableRowData
    SdkAssert.assertEquals("field count of 'EmptyTableRowData'", 1, emptyTableRowData.getFields().length);
    IField serialVersionUID4 = SdkAssert.assertFieldExist(emptyTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    SdkAssert.assertEquals("method count of 'EmptyTableRowData'", 1, emptyTableRowData.getMethods().length);
    IMethod emptyTableRowData1 = SdkAssert.assertMethodExist(emptyTableRowData, "EmptyTableRowData", new String[]{});
    SdkAssert.assertTrue(emptyTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(emptyTableRowData1, "V");

    SdkAssert.assertEquals("inner types count of 'EmptyTableRowData'", 0, emptyTableRowData.getTypes().length);
    // type NoTable
    IType noTable = SdkAssert.assertTypeExists(tableFieldBaseFormData, "NoTable");
    SdkAssert.assertHasFlags(noTable, 9);
    SdkAssert.assertHasSuperTypeSignature(noTable, "QAbstractTableFieldBeanData;");

    // fields of NoTable
    SdkAssert.assertEquals("field count of 'NoTable'", 1, noTable.getFields().length);
    IField serialVersionUID5 = SdkAssert.assertFieldExist(noTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    SdkAssert.assertEquals("method count of 'NoTable'", 3, noTable.getMethods().length);
    IMethod noTable1 = SdkAssert.assertMethodExist(noTable, "NoTable", new String[]{});
    SdkAssert.assertTrue(noTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(noTable1, "V");
    IMethod createRow2 = SdkAssert.assertMethodExist(noTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow2, "QAbstractTableRowData;");
    SdkAssert.assertAnnotation(createRow2, "java.lang.Override");
    IMethod getRowType2 = SdkAssert.assertMethodExist(noTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType2, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType2, "java.lang.Override");

    SdkAssert.assertEquals("inner types count of 'NoTable'", 0, noTable.getTypes().length);
    // type PersonTable
    IType personTable = SdkAssert.assertTypeExists(tableFieldBaseFormData, "PersonTable");
    SdkAssert.assertHasFlags(personTable, 9);
    SdkAssert.assertHasSuperTypeSignature(personTable, "QAbstractPersonTableFieldData;");

    // fields of PersonTable
    SdkAssert.assertEquals("field count of 'PersonTable'", 1, personTable.getFields().length);
    IField serialVersionUID6 = SdkAssert.assertFieldExist(personTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    SdkAssert.assertEquals("method count of 'PersonTable'", 8, personTable.getMethods().length);
    IMethod personTable1 = SdkAssert.assertMethodExist(personTable, "PersonTable", new String[]{});
    SdkAssert.assertTrue(personTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(personTable1, "V");
    IMethod addRow4 = SdkAssert.assertMethodExist(personTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow4, "QPersonTableRowData;");
    SdkAssert.assertAnnotation(addRow4, "java.lang.Override");
    IMethod addRow5 = SdkAssert.assertMethodExist(personTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow5, "QPersonTableRowData;");
    SdkAssert.assertAnnotation(addRow5, "java.lang.Override");
    IMethod createRow3 = SdkAssert.assertMethodExist(personTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow3, "QPersonTableRowData;");
    SdkAssert.assertAnnotation(createRow3, "java.lang.Override");
    IMethod getRowType3 = SdkAssert.assertMethodExist(personTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType3, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType3, "java.lang.Override");
    IMethod getRows2 = SdkAssert.assertMethodExist(personTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows2, "[QPersonTableRowData;");
    SdkAssert.assertAnnotation(getRows2, "java.lang.Override");
    IMethod rowAt2 = SdkAssert.assertMethodExist(personTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt2, "QPersonTableRowData;");
    SdkAssert.assertAnnotation(rowAt2, "java.lang.Override");
    IMethod setRows2 = SdkAssert.assertMethodExist(personTable, "setRows", new String[]{"[QPersonTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows2, "V");

    SdkAssert.assertEquals("inner types count of 'PersonTable'", 1, personTable.getTypes().length);
    // type PersonTableRowData
    IType personTableRowData = SdkAssert.assertTypeExists(personTable, "PersonTableRowData");
    SdkAssert.assertHasFlags(personTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(personTableRowData, "QAbstractPersonTableRowData;");

    // fields of PersonTableRowData
    SdkAssert.assertEquals("field count of 'PersonTableRowData'", 1, personTableRowData.getFields().length);
    IField serialVersionUID7 = SdkAssert.assertFieldExist(personTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    SdkAssert.assertEquals("method count of 'PersonTableRowData'", 1, personTableRowData.getMethods().length);
    IMethod personTableRowData1 = SdkAssert.assertMethodExist(personTableRowData, "PersonTableRowData", new String[]{});
    SdkAssert.assertTrue(personTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(personTableRowData1, "V");

    SdkAssert.assertEquals("inner types count of 'PersonTableRowData'", 0, personTableRowData.getTypes().length);
    // type Table
    IType table = SdkAssert.assertTypeExists(tableFieldBaseFormData, "Table");
    SdkAssert.assertHasFlags(table, 9);
    SdkAssert.assertHasSuperTypeSignature(table, "QAbstractTableFieldBeanData;");

    // fields of Table
    SdkAssert.assertEquals("field count of 'Table'", 1, table.getFields().length);
    IField serialVersionUID8 = SdkAssert.assertFieldExist(table, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");

    SdkAssert.assertEquals("method count of 'Table'", 8, table.getMethods().length);
    IMethod table1 = SdkAssert.assertMethodExist(table, "Table", new String[]{});
    SdkAssert.assertTrue(table1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(table1, "V");
    IMethod addRow6 = SdkAssert.assertMethodExist(table, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow6, "QTableRowData;");
    SdkAssert.assertAnnotation(addRow6, "java.lang.Override");
    IMethod addRow7 = SdkAssert.assertMethodExist(table, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow7, "QTableRowData;");
    SdkAssert.assertAnnotation(addRow7, "java.lang.Override");
    IMethod createRow4 = SdkAssert.assertMethodExist(table, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow4, "QTableRowData;");
    SdkAssert.assertAnnotation(createRow4, "java.lang.Override");
    IMethod getRowType4 = SdkAssert.assertMethodExist(table, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType4, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType4, "java.lang.Override");
    IMethod getRows3 = SdkAssert.assertMethodExist(table, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows3, "[QTableRowData;");
    SdkAssert.assertAnnotation(getRows3, "java.lang.Override");
    IMethod rowAt3 = SdkAssert.assertMethodExist(table, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt3, "QTableRowData;");
    SdkAssert.assertAnnotation(rowAt3, "java.lang.Override");
    IMethod setRows3 = SdkAssert.assertMethodExist(table, "setRows", new String[]{"[QTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows3, "V");

    SdkAssert.assertEquals("inner types count of 'Table'", 1, table.getTypes().length);
    // type TableRowData
    IType tableRowData = SdkAssert.assertTypeExists(table, "TableRowData");
    SdkAssert.assertHasFlags(tableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(tableRowData, "QAbstractTableRowData;");

    // fields of TableRowData
    SdkAssert.assertEquals("field count of 'TableRowData'", 5, tableRowData.getFields().length);
    IField serialVersionUID9 = SdkAssert.assertFieldExist(tableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID9, 26);
    SdkAssert.assertFieldSignature(serialVersionUID9, "J");
    IField first = SdkAssert.assertFieldExist(tableRowData, "first");
    SdkAssert.assertHasFlags(first, 25);
    SdkAssert.assertFieldSignature(first, "QString;");
    IField second = SdkAssert.assertFieldExist(tableRowData, "second");
    SdkAssert.assertHasFlags(second, 25);
    SdkAssert.assertFieldSignature(second, "QString;");
    IField m_first = SdkAssert.assertFieldExist(tableRowData, "m_first");
    SdkAssert.assertHasFlags(m_first, 2);
    SdkAssert.assertFieldSignature(m_first, "QString;");
    IField m_second = SdkAssert.assertFieldExist(tableRowData, "m_second");
    SdkAssert.assertHasFlags(m_second, 2);
    SdkAssert.assertFieldSignature(m_second, "QString;");

    SdkAssert.assertEquals("method count of 'TableRowData'", 5, tableRowData.getMethods().length);
    IMethod tableRowData1 = SdkAssert.assertMethodExist(tableRowData, "TableRowData", new String[]{});
    SdkAssert.assertTrue(tableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableRowData1, "V");
    IMethod getFirst = SdkAssert.assertMethodExist(tableRowData, "getFirst", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirst, "QString;");
    IMethod setFirst = SdkAssert.assertMethodExist(tableRowData, "setFirst", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setFirst, "V");
    IMethod getSecond = SdkAssert.assertMethodExist(tableRowData, "getSecond", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecond, "QString;");
    IMethod setSecond = SdkAssert.assertMethodExist(tableRowData, "setSecond", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setSecond, "V");

    SdkAssert.assertEquals("inner types count of 'TableRowData'", 0, tableRowData.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfTableFieldExFormData() throws Exception {
    // type TableFieldExFormData
    IType tableFieldExFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.replace.TableFieldExFormData");
    SdkAssert.assertHasFlags(tableFieldExFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(tableFieldExFormData, "QTableFieldBaseFormData;");

    // fields of TableFieldExFormData
    SdkAssert.assertEquals("field count of 'TableFieldExFormData'", 1, tableFieldExFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(tableFieldExFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'TableFieldExFormData'", 6, tableFieldExFormData.getMethods().length);
    IMethod tableFieldExFormData1 = SdkAssert.assertMethodExist(tableFieldExFormData, "TableFieldExFormData", new String[]{});
    SdkAssert.assertTrue(tableFieldExFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableFieldExFormData1, "V");
    IMethod getEmptyTableExtended = SdkAssert.assertMethodExist(tableFieldExFormData, "getEmptyTableExtended", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getEmptyTableExtended, "QEmptyTableExtended;");
    IMethod getExtendedAddress = SdkAssert.assertMethodExist(tableFieldExFormData, "getExtendedAddress", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getExtendedAddress, "QExtendedAddress;");
    IMethod getExtendedPersonTable = SdkAssert.assertMethodExist(tableFieldExFormData, "getExtendedPersonTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getExtendedPersonTable, "QExtendedPersonTable;");
    IMethod getNoTableExtended = SdkAssert.assertMethodExist(tableFieldExFormData, "getNoTableExtended", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getNoTableExtended, "QNoTableExtended;");
    IMethod getTableExtended = SdkAssert.assertMethodExist(tableFieldExFormData, "getTableExtended", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTableExtended, "QTableExtended;");

    SdkAssert.assertEquals("inner types count of 'TableFieldExFormData'", 5, tableFieldExFormData.getTypes().length);
    // type EmptyTableExtended
    IType emptyTableExtended = SdkAssert.assertTypeExists(tableFieldExFormData, "EmptyTableExtended");
    SdkAssert.assertHasFlags(emptyTableExtended, 9);
    SdkAssert.assertHasSuperTypeSignature(emptyTableExtended, "QEmptyTable;");
    SdkAssert.assertAnnotation(emptyTableExtended, "org.eclipse.scout.commons.annotations.Replace");

    // fields of EmptyTableExtended
    SdkAssert.assertEquals("field count of 'EmptyTableExtended'", 1, emptyTableExtended.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(emptyTableExtended, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'EmptyTableExtended'", 8, emptyTableExtended.getMethods().length);
    IMethod emptyTableExtended1 = SdkAssert.assertMethodExist(emptyTableExtended, "EmptyTableExtended", new String[]{});
    SdkAssert.assertTrue(emptyTableExtended1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(emptyTableExtended1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(emptyTableExtended, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QEmptyTableExtendedRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(emptyTableExtended, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QEmptyTableExtendedRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(emptyTableExtended, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QEmptyTableExtendedRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(emptyTableExtended, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(emptyTableExtended, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QEmptyTableExtendedRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(emptyTableExtended, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QEmptyTableExtendedRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(emptyTableExtended, "setRows", new String[]{"[QEmptyTableExtendedRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'EmptyTableExtended'", 1, emptyTableExtended.getTypes().length);
    // type EmptyTableExtendedRowData
    IType emptyTableExtendedRowData = SdkAssert.assertTypeExists(emptyTableExtended, "EmptyTableExtendedRowData");
    SdkAssert.assertHasFlags(emptyTableExtendedRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(emptyTableExtendedRowData, "QEmptyTableRowData;");

    // fields of EmptyTableExtendedRowData
    SdkAssert.assertEquals("field count of 'EmptyTableExtendedRowData'", 3, emptyTableExtendedRowData.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(emptyTableExtendedRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");
    IField single = SdkAssert.assertFieldExist(emptyTableExtendedRowData, "single");
    SdkAssert.assertHasFlags(single, 25);
    SdkAssert.assertFieldSignature(single, "QString;");
    IField m_single = SdkAssert.assertFieldExist(emptyTableExtendedRowData, "m_single");
    SdkAssert.assertHasFlags(m_single, 2);
    SdkAssert.assertFieldSignature(m_single, "QString;");

    SdkAssert.assertEquals("method count of 'EmptyTableExtendedRowData'", 3, emptyTableExtendedRowData.getMethods().length);
    IMethod emptyTableExtendedRowData1 = SdkAssert.assertMethodExist(emptyTableExtendedRowData, "EmptyTableExtendedRowData", new String[]{});
    SdkAssert.assertTrue(emptyTableExtendedRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(emptyTableExtendedRowData1, "V");
    IMethod getSingle = SdkAssert.assertMethodExist(emptyTableExtendedRowData, "getSingle", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSingle, "QString;");
    IMethod setSingle = SdkAssert.assertMethodExist(emptyTableExtendedRowData, "setSingle", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setSingle, "V");

    SdkAssert.assertEquals("inner types count of 'EmptyTableExtendedRowData'", 0, emptyTableExtendedRowData.getTypes().length);
    // type ExtendedAddress
    IType extendedAddress = SdkAssert.assertTypeExists(tableFieldExFormData, "ExtendedAddress");
    SdkAssert.assertHasFlags(extendedAddress, 9);
    SdkAssert.assertHasSuperTypeSignature(extendedAddress, "QAddressTable;");
    SdkAssert.assertAnnotation(extendedAddress, "org.eclipse.scout.commons.annotations.Replace");

    // fields of ExtendedAddress
    SdkAssert.assertEquals("field count of 'ExtendedAddress'", 1, extendedAddress.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(extendedAddress, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    SdkAssert.assertEquals("method count of 'ExtendedAddress'", 8, extendedAddress.getMethods().length);
    IMethod extendedAddress1 = SdkAssert.assertMethodExist(extendedAddress, "ExtendedAddress", new String[]{});
    SdkAssert.assertTrue(extendedAddress1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedAddress1, "V");
    IMethod addRow2 = SdkAssert.assertMethodExist(extendedAddress, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow2, "QExtendedAddressRowData;");
    SdkAssert.assertAnnotation(addRow2, "java.lang.Override");
    IMethod addRow3 = SdkAssert.assertMethodExist(extendedAddress, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow3, "QExtendedAddressRowData;");
    SdkAssert.assertAnnotation(addRow3, "java.lang.Override");
    IMethod createRow1 = SdkAssert.assertMethodExist(extendedAddress, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow1, "QExtendedAddressRowData;");
    SdkAssert.assertAnnotation(createRow1, "java.lang.Override");
    IMethod getRowType1 = SdkAssert.assertMethodExist(extendedAddress, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType1, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType1, "java.lang.Override");
    IMethod getRows1 = SdkAssert.assertMethodExist(extendedAddress, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows1, "[QExtendedAddressRowData;");
    SdkAssert.assertAnnotation(getRows1, "java.lang.Override");
    IMethod rowAt1 = SdkAssert.assertMethodExist(extendedAddress, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt1, "QExtendedAddressRowData;");
    SdkAssert.assertAnnotation(rowAt1, "java.lang.Override");
    IMethod setRows1 = SdkAssert.assertMethodExist(extendedAddress, "setRows", new String[]{"[QExtendedAddressRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows1, "V");

    SdkAssert.assertEquals("inner types count of 'ExtendedAddress'", 1, extendedAddress.getTypes().length);
    // type ExtendedAddressRowData
    IType extendedAddressRowData = SdkAssert.assertTypeExists(extendedAddress, "ExtendedAddressRowData");
    SdkAssert.assertHasFlags(extendedAddressRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(extendedAddressRowData, "QAddressTableRowData;");

    // fields of ExtendedAddressRowData
    SdkAssert.assertEquals("field count of 'ExtendedAddressRowData'", 3, extendedAddressRowData.getFields().length);
    IField serialVersionUID4 = SdkAssert.assertFieldExist(extendedAddressRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");
    IField state = SdkAssert.assertFieldExist(extendedAddressRowData, "state");
    SdkAssert.assertHasFlags(state, 25);
    SdkAssert.assertFieldSignature(state, "QString;");
    IField m_state = SdkAssert.assertFieldExist(extendedAddressRowData, "m_state");
    SdkAssert.assertHasFlags(m_state, 2);
    SdkAssert.assertFieldSignature(m_state, "QString;");

    SdkAssert.assertEquals("method count of 'ExtendedAddressRowData'", 3, extendedAddressRowData.getMethods().length);
    IMethod extendedAddressRowData1 = SdkAssert.assertMethodExist(extendedAddressRowData, "ExtendedAddressRowData", new String[]{});
    SdkAssert.assertTrue(extendedAddressRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedAddressRowData1, "V");
    IMethod getState = SdkAssert.assertMethodExist(extendedAddressRowData, "getState", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getState, "QString;");
    IMethod setState = SdkAssert.assertMethodExist(extendedAddressRowData, "setState", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setState, "V");

    SdkAssert.assertEquals("inner types count of 'ExtendedAddressRowData'", 0, extendedAddressRowData.getTypes().length);
    // type ExtendedPersonTable
    IType extendedPersonTable = SdkAssert.assertTypeExists(tableFieldExFormData, "ExtendedPersonTable");
    SdkAssert.assertHasFlags(extendedPersonTable, 9);
    SdkAssert.assertHasSuperTypeSignature(extendedPersonTable, "QPersonTable;");
    SdkAssert.assertAnnotation(extendedPersonTable, "org.eclipse.scout.commons.annotations.Replace");

    // fields of ExtendedPersonTable
    SdkAssert.assertEquals("field count of 'ExtendedPersonTable'", 1, extendedPersonTable.getFields().length);
    IField serialVersionUID5 = SdkAssert.assertFieldExist(extendedPersonTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    SdkAssert.assertEquals("method count of 'ExtendedPersonTable'", 8, extendedPersonTable.getMethods().length);
    IMethod extendedPersonTable1 = SdkAssert.assertMethodExist(extendedPersonTable, "ExtendedPersonTable", new String[]{});
    SdkAssert.assertTrue(extendedPersonTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedPersonTable1, "V");
    IMethod addRow4 = SdkAssert.assertMethodExist(extendedPersonTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow4, "QExtendedPersonTableRowData;");
    SdkAssert.assertAnnotation(addRow4, "java.lang.Override");
    IMethod addRow5 = SdkAssert.assertMethodExist(extendedPersonTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow5, "QExtendedPersonTableRowData;");
    SdkAssert.assertAnnotation(addRow5, "java.lang.Override");
    IMethod createRow2 = SdkAssert.assertMethodExist(extendedPersonTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow2, "QExtendedPersonTableRowData;");
    SdkAssert.assertAnnotation(createRow2, "java.lang.Override");
    IMethod getRowType2 = SdkAssert.assertMethodExist(extendedPersonTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType2, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType2, "java.lang.Override");
    IMethod getRows2 = SdkAssert.assertMethodExist(extendedPersonTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows2, "[QExtendedPersonTableRowData;");
    SdkAssert.assertAnnotation(getRows2, "java.lang.Override");
    IMethod rowAt2 = SdkAssert.assertMethodExist(extendedPersonTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt2, "QExtendedPersonTableRowData;");
    SdkAssert.assertAnnotation(rowAt2, "java.lang.Override");
    IMethod setRows2 = SdkAssert.assertMethodExist(extendedPersonTable, "setRows", new String[]{"[QExtendedPersonTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows2, "V");

    SdkAssert.assertEquals("inner types count of 'ExtendedPersonTable'", 1, extendedPersonTable.getTypes().length);
    // type ExtendedPersonTableRowData
    IType extendedPersonTableRowData = SdkAssert.assertTypeExists(extendedPersonTable, "ExtendedPersonTableRowData");
    SdkAssert.assertHasFlags(extendedPersonTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(extendedPersonTableRowData, "QPersonTableRowData;");

    // fields of ExtendedPersonTableRowData
    SdkAssert.assertEquals("field count of 'ExtendedPersonTableRowData'", 3, extendedPersonTableRowData.getFields().length);
    IField serialVersionUID6 = SdkAssert.assertFieldExist(extendedPersonTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");
    IField lastName = SdkAssert.assertFieldExist(extendedPersonTableRowData, "lastName");
    SdkAssert.assertHasFlags(lastName, 25);
    SdkAssert.assertFieldSignature(lastName, "QString;");
    IField m_lastName = SdkAssert.assertFieldExist(extendedPersonTableRowData, "m_lastName");
    SdkAssert.assertHasFlags(m_lastName, 2);
    SdkAssert.assertFieldSignature(m_lastName, "QString;");

    SdkAssert.assertEquals("method count of 'ExtendedPersonTableRowData'", 3, extendedPersonTableRowData.getMethods().length);
    IMethod extendedPersonTableRowData1 = SdkAssert.assertMethodExist(extendedPersonTableRowData, "ExtendedPersonTableRowData", new String[]{});
    SdkAssert.assertTrue(extendedPersonTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedPersonTableRowData1, "V");
    IMethod getLastName = SdkAssert.assertMethodExist(extendedPersonTableRowData, "getLastName", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getLastName, "QString;");
    IMethod setLastName = SdkAssert.assertMethodExist(extendedPersonTableRowData, "setLastName", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setLastName, "V");

    SdkAssert.assertEquals("inner types count of 'ExtendedPersonTableRowData'", 0, extendedPersonTableRowData.getTypes().length);
    // type NoTableExtended
    IType noTableExtended = SdkAssert.assertTypeExists(tableFieldExFormData, "NoTableExtended");
    SdkAssert.assertHasFlags(noTableExtended, 9);
    SdkAssert.assertHasSuperTypeSignature(noTableExtended, "QNoTable;");
    SdkAssert.assertAnnotation(noTableExtended, "org.eclipse.scout.commons.annotations.Replace");

    // fields of NoTableExtended
    SdkAssert.assertEquals("field count of 'NoTableExtended'", 1, noTableExtended.getFields().length);
    IField serialVersionUID7 = SdkAssert.assertFieldExist(noTableExtended, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    SdkAssert.assertEquals("method count of 'NoTableExtended'", 8, noTableExtended.getMethods().length);
    IMethod noTableExtended1 = SdkAssert.assertMethodExist(noTableExtended, "NoTableExtended", new String[]{});
    SdkAssert.assertTrue(noTableExtended1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(noTableExtended1, "V");
    IMethod addRow6 = SdkAssert.assertMethodExist(noTableExtended, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow6, "QNoTableExtendedRowData;");
    SdkAssert.assertAnnotation(addRow6, "java.lang.Override");
    IMethod addRow7 = SdkAssert.assertMethodExist(noTableExtended, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow7, "QNoTableExtendedRowData;");
    SdkAssert.assertAnnotation(addRow7, "java.lang.Override");
    IMethod createRow3 = SdkAssert.assertMethodExist(noTableExtended, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow3, "QNoTableExtendedRowData;");
    SdkAssert.assertAnnotation(createRow3, "java.lang.Override");
    IMethod getRowType3 = SdkAssert.assertMethodExist(noTableExtended, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType3, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType3, "java.lang.Override");
    IMethod getRows3 = SdkAssert.assertMethodExist(noTableExtended, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows3, "[QNoTableExtendedRowData;");
    SdkAssert.assertAnnotation(getRows3, "java.lang.Override");
    IMethod rowAt3 = SdkAssert.assertMethodExist(noTableExtended, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt3, "QNoTableExtendedRowData;");
    SdkAssert.assertAnnotation(rowAt3, "java.lang.Override");
    IMethod setRows3 = SdkAssert.assertMethodExist(noTableExtended, "setRows", new String[]{"[QNoTableExtendedRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows3, "V");

    SdkAssert.assertEquals("inner types count of 'NoTableExtended'", 1, noTableExtended.getTypes().length);
    // type NoTableExtendedRowData
    IType noTableExtendedRowData = SdkAssert.assertTypeExists(noTableExtended, "NoTableExtendedRowData");
    SdkAssert.assertHasFlags(noTableExtendedRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(noTableExtendedRowData, "QAbstractTableRowData;");

    // fields of NoTableExtendedRowData
    SdkAssert.assertEquals("field count of 'NoTableExtendedRowData'", 3, noTableExtendedRowData.getFields().length);
    IField serialVersionUID8 = SdkAssert.assertFieldExist(noTableExtendedRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");
    IField new_ = SdkAssert.assertFieldExist(noTableExtendedRowData, "new_");
    SdkAssert.assertHasFlags(new_, 25);
    SdkAssert.assertFieldSignature(new_, "QString;");
    IField m_new = SdkAssert.assertFieldExist(noTableExtendedRowData, "m_new");
    SdkAssert.assertHasFlags(m_new, 2);
    SdkAssert.assertFieldSignature(m_new, "QString;");

    SdkAssert.assertEquals("method count of 'NoTableExtendedRowData'", 3, noTableExtendedRowData.getMethods().length);
    IMethod noTableExtendedRowData1 = SdkAssert.assertMethodExist(noTableExtendedRowData, "NoTableExtendedRowData", new String[]{});
    SdkAssert.assertTrue(noTableExtendedRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(noTableExtendedRowData1, "V");
    IMethod getNew = SdkAssert.assertMethodExist(noTableExtendedRowData, "getNew", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getNew, "QString;");
    IMethod setNew = SdkAssert.assertMethodExist(noTableExtendedRowData, "setNew", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setNew, "V");

    SdkAssert.assertEquals("inner types count of 'NoTableExtendedRowData'", 0, noTableExtendedRowData.getTypes().length);
    // type TableExtended
    IType tableExtended = SdkAssert.assertTypeExists(tableFieldExFormData, "TableExtended");
    SdkAssert.assertHasFlags(tableExtended, 9);
    SdkAssert.assertHasSuperTypeSignature(tableExtended, "QTable;");
    SdkAssert.assertAnnotation(tableExtended, "org.eclipse.scout.commons.annotations.Replace");

    // fields of TableExtended
    SdkAssert.assertEquals("field count of 'TableExtended'", 1, tableExtended.getFields().length);
    IField serialVersionUID9 = SdkAssert.assertFieldExist(tableExtended, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID9, 26);
    SdkAssert.assertFieldSignature(serialVersionUID9, "J");

    SdkAssert.assertEquals("method count of 'TableExtended'", 8, tableExtended.getMethods().length);
    IMethod tableExtended1 = SdkAssert.assertMethodExist(tableExtended, "TableExtended", new String[]{});
    SdkAssert.assertTrue(tableExtended1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableExtended1, "V");
    IMethod addRow8 = SdkAssert.assertMethodExist(tableExtended, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow8, "QTableExtendedRowData;");
    SdkAssert.assertAnnotation(addRow8, "java.lang.Override");
    IMethod addRow9 = SdkAssert.assertMethodExist(tableExtended, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow9, "QTableExtendedRowData;");
    SdkAssert.assertAnnotation(addRow9, "java.lang.Override");
    IMethod createRow4 = SdkAssert.assertMethodExist(tableExtended, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow4, "QTableExtendedRowData;");
    SdkAssert.assertAnnotation(createRow4, "java.lang.Override");
    IMethod getRowType4 = SdkAssert.assertMethodExist(tableExtended, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType4, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType4, "java.lang.Override");
    IMethod getRows4 = SdkAssert.assertMethodExist(tableExtended, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows4, "[QTableExtendedRowData;");
    SdkAssert.assertAnnotation(getRows4, "java.lang.Override");
    IMethod rowAt4 = SdkAssert.assertMethodExist(tableExtended, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt4, "QTableExtendedRowData;");
    SdkAssert.assertAnnotation(rowAt4, "java.lang.Override");
    IMethod setRows4 = SdkAssert.assertMethodExist(tableExtended, "setRows", new String[]{"[QTableExtendedRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows4, "V");

    SdkAssert.assertEquals("inner types count of 'TableExtended'", 1, tableExtended.getTypes().length);
    // type TableExtendedRowData
    IType tableExtendedRowData = SdkAssert.assertTypeExists(tableExtended, "TableExtendedRowData");
    SdkAssert.assertHasFlags(tableExtendedRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(tableExtendedRowData, "QTableRowData;");

    // fields of TableExtendedRowData
    SdkAssert.assertEquals("field count of 'TableExtendedRowData'", 3, tableExtendedRowData.getFields().length);
    IField serialVersionUID10 = SdkAssert.assertFieldExist(tableExtendedRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID10, 26);
    SdkAssert.assertFieldSignature(serialVersionUID10, "J");
    IField boolean_ = SdkAssert.assertFieldExist(tableExtendedRowData, "boolean_");
    SdkAssert.assertHasFlags(boolean_, 25);
    SdkAssert.assertFieldSignature(boolean_, "QString;");
    IField m_boolean = SdkAssert.assertFieldExist(tableExtendedRowData, "m_boolean");
    SdkAssert.assertHasFlags(m_boolean, 2);
    SdkAssert.assertFieldSignature(m_boolean, "QBoolean;");

    SdkAssert.assertEquals("method count of 'TableExtendedRowData'", 3, tableExtendedRowData.getMethods().length);
    IMethod tableExtendedRowData1 = SdkAssert.assertMethodExist(tableExtendedRowData, "TableExtendedRowData", new String[]{});
    SdkAssert.assertTrue(tableExtendedRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableExtendedRowData1, "V");
    IMethod getBoolean = SdkAssert.assertMethodExist(tableExtendedRowData, "getBoolean", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getBoolean, "QBoolean;");
    IMethod setBoolean = SdkAssert.assertMethodExist(tableExtendedRowData, "setBoolean", new String[]{"QBoolean;"});
    SdkAssert.assertMethodReturnTypeSignature(setBoolean, "V");

    SdkAssert.assertEquals("inner types count of 'TableExtendedRowData'", 0, tableExtendedRowData.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfBaseWithExtendedTableFormData() throws Exception {
    // type BaseWithExtendedTableFormData
    IType baseWithExtendedTableFormData = SdkAssert.assertTypeExists("formdata.shared.services.BaseWithExtendedTableFormData");
    SdkAssert.assertHasFlags(baseWithExtendedTableFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(baseWithExtendedTableFormData, "QAbstractFormData;");

    // fields of BaseWithExtendedTableFormData
    SdkAssert.assertEquals("field count of 'BaseWithExtendedTableFormData'", 1, baseWithExtendedTableFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(baseWithExtendedTableFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'BaseWithExtendedTableFormData'", 2, baseWithExtendedTableFormData.getMethods().length);
    IMethod baseWithExtendedTableFormData1 = SdkAssert.assertMethodExist(baseWithExtendedTableFormData, "BaseWithExtendedTableFormData", new String[]{});
    SdkAssert.assertTrue(baseWithExtendedTableFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(baseWithExtendedTableFormData1, "V");
    IMethod getTableInForm = SdkAssert.assertMethodExist(baseWithExtendedTableFormData, "getTableInForm", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTableInForm, "QTableInForm;");

    SdkAssert.assertEquals("inner types count of 'BaseWithExtendedTableFormData'", 1, baseWithExtendedTableFormData.getTypes().length);
    // type TableInForm
    IType tableInForm = SdkAssert.assertTypeExists(baseWithExtendedTableFormData, "TableInForm");
    SdkAssert.assertHasFlags(tableInForm, 9);
    SdkAssert.assertHasSuperTypeSignature(tableInForm, "QAbstractTableFieldBeanData;");

    // fields of TableInForm
    SdkAssert.assertEquals("field count of 'TableInForm'", 1, tableInForm.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(tableInForm, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'TableInForm'", 8, tableInForm.getMethods().length);
    IMethod tableInForm1 = SdkAssert.assertMethodExist(tableInForm, "TableInForm", new String[]{});
    SdkAssert.assertTrue(tableInForm1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableInForm1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(tableInForm, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QTableInFormRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(tableInForm, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QTableInFormRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(tableInForm, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QTableInFormRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(tableInForm, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(tableInForm, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QTableInFormRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(tableInForm, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QTableInFormRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(tableInForm, "setRows", new String[]{"[QTableInFormRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'TableInForm'", 1, tableInForm.getTypes().length);
    // type TableInFormRowData
    IType tableInFormRowData = SdkAssert.assertTypeExists(tableInForm, "TableInFormRowData");
    SdkAssert.assertHasFlags(tableInFormRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(tableInFormRowData, "QAbstractTableRowData;");

    // fields of TableInFormRowData
    SdkAssert.assertEquals("field count of 'TableInFormRowData'", 5, tableInFormRowData.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(tableInFormRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");
    IField colInAbstractTable = SdkAssert.assertFieldExist(tableInFormRowData, "colInAbstractTable");
    SdkAssert.assertHasFlags(colInAbstractTable, 25);
    SdkAssert.assertFieldSignature(colInAbstractTable, "QString;");
    IField colInDesktopForm = SdkAssert.assertFieldExist(tableInFormRowData, "colInDesktopForm");
    SdkAssert.assertHasFlags(colInDesktopForm, 25);
    SdkAssert.assertFieldSignature(colInDesktopForm, "QString;");
    IField m_colInAbstractTable = SdkAssert.assertFieldExist(tableInFormRowData, "m_colInAbstractTable");
    SdkAssert.assertHasFlags(m_colInAbstractTable, 2);
    SdkAssert.assertFieldSignature(m_colInAbstractTable, "QString;");
    IField m_colInDesktopForm = SdkAssert.assertFieldExist(tableInFormRowData, "m_colInDesktopForm");
    SdkAssert.assertHasFlags(m_colInDesktopForm, 2);
    SdkAssert.assertFieldSignature(m_colInDesktopForm, "QString;");

    SdkAssert.assertEquals("method count of 'TableInFormRowData'", 5, tableInFormRowData.getMethods().length);
    IMethod tableInFormRowData1 = SdkAssert.assertMethodExist(tableInFormRowData, "TableInFormRowData", new String[]{});
    SdkAssert.assertTrue(tableInFormRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableInFormRowData1, "V");
    IMethod getColInAbstractTable = SdkAssert.assertMethodExist(tableInFormRowData, "getColInAbstractTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getColInAbstractTable, "QString;");
    IMethod setColInAbstractTable = SdkAssert.assertMethodExist(tableInFormRowData, "setColInAbstractTable", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setColInAbstractTable, "V");
    IMethod getColInDesktopForm = SdkAssert.assertMethodExist(tableInFormRowData, "getColInDesktopForm", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getColInDesktopForm, "QString;");
    IMethod setColInDesktopForm = SdkAssert.assertMethodExist(tableInFormRowData, "setColInDesktopForm", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setColInDesktopForm, "V");

    SdkAssert.assertEquals("inner types count of 'TableInFormRowData'", 0, tableInFormRowData.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfChildWithExtendedTableFormData() throws Exception {
    // type ChildWithExtendedTableFormData
    IType childWithExtendedTableFormData = SdkAssert.assertTypeExists("formdata.shared.services.ChildWithExtendedTableFormData");
    SdkAssert.assertHasFlags(childWithExtendedTableFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(childWithExtendedTableFormData, "QBaseWithExtendedTableFormData;");

    // fields of ChildWithExtendedTableFormData
    SdkAssert.assertEquals("field count of 'ChildWithExtendedTableFormData'", 1, childWithExtendedTableFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(childWithExtendedTableFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'ChildWithExtendedTableFormData'", 2, childWithExtendedTableFormData.getMethods().length);
    IMethod childWithExtendedTableFormData1 = SdkAssert.assertMethodExist(childWithExtendedTableFormData, "ChildWithExtendedTableFormData", new String[]{});
    SdkAssert.assertTrue(childWithExtendedTableFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(childWithExtendedTableFormData1, "V");
    IMethod getChildTable = SdkAssert.assertMethodExist(childWithExtendedTableFormData, "getChildTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getChildTable, "QChildTable;");

    SdkAssert.assertEquals("inner types count of 'ChildWithExtendedTableFormData'", 1, childWithExtendedTableFormData.getTypes().length);
    // type ChildTable
    IType childTable = SdkAssert.assertTypeExists(childWithExtendedTableFormData, "ChildTable");
    SdkAssert.assertHasFlags(childTable, 9);
    SdkAssert.assertHasSuperTypeSignature(childTable, "QAbstractTableFieldBeanData;");

    // fields of ChildTable
    SdkAssert.assertEquals("field count of 'ChildTable'", 1, childTable.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(childTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'ChildTable'", 8, childTable.getMethods().length);
    IMethod childTable1 = SdkAssert.assertMethodExist(childTable, "ChildTable", new String[]{});
    SdkAssert.assertTrue(childTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(childTable1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(childTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QChildTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(childTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QChildTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(childTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QChildTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(childTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(childTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QChildTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(childTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QChildTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(childTable, "setRows", new String[]{"[QChildTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'ChildTable'", 1, childTable.getTypes().length);
    // type ChildTableRowData
    IType childTableRowData = SdkAssert.assertTypeExists(childTable, "ChildTableRowData");
    SdkAssert.assertHasFlags(childTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(childTableRowData, "QAbstractTableRowData;");

    // fields of ChildTableRowData
    SdkAssert.assertEquals("field count of 'ChildTableRowData'", 7, childTableRowData.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(childTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");
    IField col1InChildForm = SdkAssert.assertFieldExist(childTableRowData, "col1InChildForm");
    SdkAssert.assertHasFlags(col1InChildForm, 25);
    SdkAssert.assertFieldSignature(col1InChildForm, "QString;");
    IField colInAbstractTable = SdkAssert.assertFieldExist(childTableRowData, "colInAbstractTable");
    SdkAssert.assertHasFlags(colInAbstractTable, 25);
    SdkAssert.assertFieldSignature(colInAbstractTable, "QString;");
    IField colInDesktopForm = SdkAssert.assertFieldExist(childTableRowData, "colInDesktopForm");
    SdkAssert.assertHasFlags(colInDesktopForm, 25);
    SdkAssert.assertFieldSignature(colInDesktopForm, "QString;");
    IField m_col1InChildForm = SdkAssert.assertFieldExist(childTableRowData, "m_col1InChildForm");
    SdkAssert.assertHasFlags(m_col1InChildForm, 2);
    SdkAssert.assertFieldSignature(m_col1InChildForm, "QString;");
    IField m_colInAbstractTable = SdkAssert.assertFieldExist(childTableRowData, "m_colInAbstractTable");
    SdkAssert.assertHasFlags(m_colInAbstractTable, 2);
    SdkAssert.assertFieldSignature(m_colInAbstractTable, "QString;");
    IField m_colInDesktopForm = SdkAssert.assertFieldExist(childTableRowData, "m_colInDesktopForm");
    SdkAssert.assertHasFlags(m_colInDesktopForm, 2);
    SdkAssert.assertFieldSignature(m_colInDesktopForm, "QString;");

    SdkAssert.assertEquals("method count of 'ChildTableRowData'", 7, childTableRowData.getMethods().length);
    IMethod childTableRowData1 = SdkAssert.assertMethodExist(childTableRowData, "ChildTableRowData", new String[]{});
    SdkAssert.assertTrue(childTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(childTableRowData1, "V");
    IMethod getCol1InChildForm = SdkAssert.assertMethodExist(childTableRowData, "getCol1InChildForm", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getCol1InChildForm, "QString;");
    IMethod setCol1InChildForm = SdkAssert.assertMethodExist(childTableRowData, "setCol1InChildForm", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setCol1InChildForm, "V");
    IMethod getColInAbstractTable = SdkAssert.assertMethodExist(childTableRowData, "getColInAbstractTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getColInAbstractTable, "QString;");
    IMethod setColInAbstractTable = SdkAssert.assertMethodExist(childTableRowData, "setColInAbstractTable", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setColInAbstractTable, "V");
    IMethod getColInDesktopForm = SdkAssert.assertMethodExist(childTableRowData, "getColInDesktopForm", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getColInDesktopForm, "QString;");
    IMethod setColInDesktopForm = SdkAssert.assertMethodExist(childTableRowData, "setColInDesktopForm", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setColInDesktopForm, "V");

    SdkAssert.assertEquals("inner types count of 'ChildTableRowData'", 0, childTableRowData.getTypes().length);
  }
}
