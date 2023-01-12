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

import formdata.client.ui.forms.BaseWithExtendedTableForm;
import formdata.client.ui.forms.ChildWithExtendedTableForm;
import formdata.client.ui.forms.ColumnWithoutTypeForm;
import formdata.client.ui.forms.replace.TableFieldBaseForm;
import formdata.client.ui.forms.replace.TableFieldExForm;
import formdata.client.ui.template.formfield.AbstractAddressTableField;

/**
 * <h3>{@link TableFieldBeanTest}</h3>
 *
 * @since 3.10.0 2013-08-19
 */
public class TableFieldBeanTest {

  @Test
  public void testFormData() {
    testAbstractTableField();
    testTableFieldBaseFormData();
    testTableFieldExFormData();
    testBaseWithExtendedTableFormData();
    testChildWithExtendedTableFormData();
    testColumnWithoutType();
  }

  private static void testColumnWithoutType() {
    createFormDataAssertNoCompileErrors(ColumnWithoutTypeForm.class.getName(), TableFieldBeanTest::testApiOfColumnWithoutTypeFormData);
  }

  private static void testAbstractTableField() {
    createFormDataAssertNoCompileErrors(AbstractAddressTableField.class.getName(), TableFieldBeanTest::testApiOfAbstractAddressTableFieldData);
  }

  private static void testTableFieldBaseFormData() {
    createFormDataAssertNoCompileErrors(TableFieldBaseForm.class.getName(), TableFieldBeanTest::testApiOfTableFieldBaseFormData);
  }

  private static void testTableFieldExFormData() {
    createFormDataAssertNoCompileErrors(TableFieldExForm.class.getName(), TableFieldBeanTest::testApiOfTableFieldExFormData);
  }

  private static void testBaseWithExtendedTableFormData() {
    createFormDataAssertNoCompileErrors(BaseWithExtendedTableForm.class.getName(), TableFieldBeanTest::testApiOfBaseWithExtendedTableFormData);
  }

  private static void testChildWithExtendedTableFormData() {
    createFormDataAssertNoCompileErrors(ChildWithExtendedTableForm.class.getName(), TableFieldBeanTest::testApiOfChildWithExtendedTableFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractAddressTableFieldData(IType abstractAddressTableFieldData) {
    // type AbstractAddressTableFieldData
    assertHasFlags(abstractAddressTableFieldData, 1025);
    assertHasSuperClass(abstractAddressTableFieldData, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of AbstractAddressTableFieldData
    assertEquals(1, abstractAddressTableFieldData.fields().stream().count(), "field count of 'AbstractAddressTableFieldData'");
    var serialVersionUID = assertFieldExist(abstractAddressTableFieldData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(7, abstractAddressTableFieldData.methods().stream().count(), "method count of 'AbstractAddressTableFieldData'");
    var addRow = assertMethodExist(abstractAddressTableFieldData, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(abstractAddressTableFieldData, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(abstractAddressTableFieldData, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(abstractAddressTableFieldData, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(abstractAddressTableFieldData, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(abstractAddressTableFieldData, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(abstractAddressTableFieldData, "setRows", new String[]{"formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, abstractAddressTableFieldData.innerTypes().stream().count(), "inner types count of 'AbstractAddressTableFieldData'");
    // type AbstractAddressTableRowData
    var abstractAddressTableRowData = assertTypeExists(abstractAddressTableFieldData, "AbstractAddressTableRowData");
    assertHasFlags(abstractAddressTableRowData, 1033);
    assertHasSuperClass(abstractAddressTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of AbstractAddressTableRowData
    assertEquals(7, abstractAddressTableRowData.fields().stream().count(), "field count of 'AbstractAddressTableRowData'");
    var serialVersionUID1 = assertFieldExist(abstractAddressTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    var addressId = assertFieldExist(abstractAddressTableRowData, "addressId");
    assertHasFlags(addressId, 25);
    assertFieldType(addressId, String.class.getName());
    var street = assertFieldExist(abstractAddressTableRowData, "street");
    assertHasFlags(street, 25);
    assertFieldType(street, String.class.getName());
    var poBoxAddress = assertFieldExist(abstractAddressTableRowData, "poBoxAddress");
    assertHasFlags(poBoxAddress, 25);
    assertFieldType(poBoxAddress, String.class.getName());
    var m_addressId = assertFieldExist(abstractAddressTableRowData, "m_addressId");
    assertHasFlags(m_addressId, 2);
    assertFieldType(m_addressId, String.class.getName());
    var m_street = assertFieldExist(abstractAddressTableRowData, "m_street");
    assertHasFlags(m_street, 2);
    assertFieldType(m_street, String.class.getName());
    var m_poBoxAddress = assertFieldExist(abstractAddressTableRowData, "m_poBoxAddress");
    assertHasFlags(m_poBoxAddress, 2);
    assertFieldType(m_poBoxAddress, Boolean.class.getName());

    assertEquals(6, abstractAddressTableRowData.methods().stream().count(), "method count of 'AbstractAddressTableRowData'");
    var getAddressId = assertMethodExist(abstractAddressTableRowData, "getAddressId");
    assertMethodReturnType(getAddressId, String.class.getName());
    var setAddressId = assertMethodExist(abstractAddressTableRowData, "setAddressId", new String[]{String.class.getName()});
    assertMethodReturnType(setAddressId, "void");
    var getStreet = assertMethodExist(abstractAddressTableRowData, "getStreet");
    assertMethodReturnType(getStreet, String.class.getName());
    var setStreet = assertMethodExist(abstractAddressTableRowData, "setStreet", new String[]{String.class.getName()});
    assertMethodReturnType(setStreet, "void");
    var getPoBoxAddress = assertMethodExist(abstractAddressTableRowData, "getPoBoxAddress");
    assertMethodReturnType(getPoBoxAddress, Boolean.class.getName());
    var setPoBoxAddress = assertMethodExist(abstractAddressTableRowData, "setPoBoxAddress", new String[]{Boolean.class.getName()});
    assertMethodReturnType(setPoBoxAddress, "void");

    assertEquals(0, abstractAddressTableRowData.innerTypes().stream().count(), "inner types count of 'AbstractAddressTableRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfTableFieldBaseFormData(IType tableFieldBaseFormData) {
    // type TableFieldBaseFormData
    assertHasFlags(tableFieldBaseFormData, 1);
    assertHasSuperClass(tableFieldBaseFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");

    // fields of TableFieldBaseFormData
    assertEquals(1, tableFieldBaseFormData.fields().stream().count(), "field count of 'TableFieldBaseFormData'");
    var serialVersionUID = assertFieldExist(tableFieldBaseFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(5, tableFieldBaseFormData.methods().stream().count(), "method count of 'TableFieldBaseFormData'");
    var getAddressTable = assertMethodExist(tableFieldBaseFormData, "getAddressTable");
    assertMethodReturnType(getAddressTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable");
    var getEmptyTable = assertMethodExist(tableFieldBaseFormData, "getEmptyTable");
    assertMethodReturnType(getEmptyTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable");
    var getNoTable = assertMethodExist(tableFieldBaseFormData, "getNoTable");
    assertMethodReturnType(getNoTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$NoTable");
    var getPersonTable = assertMethodExist(tableFieldBaseFormData, "getPersonTable");
    assertMethodReturnType(getPersonTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable");
    var getTable = assertMethodExist(tableFieldBaseFormData, "getTable");
    assertMethodReturnType(getTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table");

    assertEquals(5, tableFieldBaseFormData.innerTypes().stream().count(), "inner types count of 'TableFieldBaseFormData'");
    // type AddressTable
    var addressTable = assertTypeExists(tableFieldBaseFormData, "AddressTable");
    assertHasFlags(addressTable, 9);
    assertHasSuperClass(addressTable, "formdata.shared.services.process.AbstractAddressTableFieldData");

    // fields of AddressTable
    assertEquals(1, addressTable.fields().stream().count(), "field count of 'AddressTable'");
    var serialVersionUID1 = assertFieldExist(addressTable, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, addressTable.methods().stream().count(), "method count of 'AddressTable'");
    var addRow = assertMethodExist(addressTable, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(addressTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(addressTable, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(addressTable, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(addressTable, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(addressTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(addressTable, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, addressTable.innerTypes().stream().count(), "inner types count of 'AddressTable'");
    // type AddressTableRowData
    var addressTableRowData = assertTypeExists(addressTable, "AddressTableRowData");
    assertHasFlags(addressTableRowData, 9);
    assertHasSuperClass(addressTableRowData, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData");

    // fields of AddressTableRowData
    assertEquals(3, addressTableRowData.fields().stream().count(), "field count of 'AddressTableRowData'");
    var serialVersionUID2 = assertFieldExist(addressTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    var city = assertFieldExist(addressTableRowData, "city");
    assertHasFlags(city, 25);
    assertFieldType(city, String.class.getName());
    var m_city = assertFieldExist(addressTableRowData, "m_city");
    assertHasFlags(m_city, 2);
    assertFieldType(m_city, String.class.getName());

    assertEquals(2, addressTableRowData.methods().stream().count(), "method count of 'AddressTableRowData'");
    var getCity = assertMethodExist(addressTableRowData, "getCity");
    assertMethodReturnType(getCity, String.class.getName());
    var setCity = assertMethodExist(addressTableRowData, "setCity", new String[]{String.class.getName()});
    assertMethodReturnType(setCity, "void");

    assertEquals(0, addressTableRowData.innerTypes().stream().count(), "inner types count of 'AddressTableRowData'");
    // type EmptyTable
    var emptyTable = assertTypeExists(tableFieldBaseFormData, "EmptyTable");
    assertHasFlags(emptyTable, 9);
    assertHasSuperClass(emptyTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of EmptyTable
    assertEquals(1, emptyTable.fields().stream().count(), "field count of 'EmptyTable'");
    var serialVersionUID3 = assertFieldExist(emptyTable, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(7, emptyTable.methods().stream().count(), "method count of 'EmptyTable'");
    var addRow2 = assertMethodExist(emptyTable, "addRow");
    assertMethodReturnType(addRow2, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData");
    assertAnnotation(addRow2, "java.lang.Override");
    var addRow3 = assertMethodExist(emptyTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow3, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData");
    assertAnnotation(addRow3, "java.lang.Override");
    var createRow1 = assertMethodExist(emptyTable, "createRow");
    assertMethodReturnType(createRow1, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData");
    assertAnnotation(createRow1, "java.lang.Override");
    var getRowType1 = assertMethodExist(emptyTable, "getRowType");
    assertMethodReturnType(getRowType1, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType1, "java.lang.Override");
    var getRows1 = assertMethodExist(emptyTable, "getRows");
    assertMethodReturnType(getRows1, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData[]");
    assertAnnotation(getRows1, "java.lang.Override");
    var rowAt1 = assertMethodExist(emptyTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt1, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData");
    assertAnnotation(rowAt1, "java.lang.Override");
    var setRows1 = assertMethodExist(emptyTable, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData[]"});
    assertMethodReturnType(setRows1, "void");

    assertEquals(1, emptyTable.innerTypes().stream().count(), "inner types count of 'EmptyTable'");
    // type EmptyTableRowData
    var emptyTableRowData = assertTypeExists(emptyTable, "EmptyTableRowData");
    assertHasFlags(emptyTableRowData, 9);
    assertHasSuperClass(emptyTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of EmptyTableRowData
    assertEquals(1, emptyTableRowData.fields().stream().count(), "field count of 'EmptyTableRowData'");
    var serialVersionUID4 = assertFieldExist(emptyTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(0, emptyTableRowData.methods().stream().count(), "method count of 'EmptyTableRowData'");

    assertEquals(0, emptyTableRowData.innerTypes().stream().count(), "inner types count of 'EmptyTableRowData'");
    // type NoTable
    var noTable = assertTypeExists(tableFieldBaseFormData, "NoTable");
    assertHasFlags(noTable, 9);
    assertHasSuperClass(noTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of NoTable
    assertEquals(1, noTable.fields().stream().count(), "field count of 'NoTable'");
    var serialVersionUID5 = assertFieldExist(noTable, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(2, noTable.methods().stream().count(), "method count of 'NoTable'");
    var createRow2 = assertMethodExist(noTable, "createRow");
    assertMethodReturnType(createRow2, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");
    assertAnnotation(createRow2, "java.lang.Override");
    var getRowType2 = assertMethodExist(noTable, "getRowType");
    assertMethodReturnType(getRowType2, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType2, "java.lang.Override");

    assertEquals(0, noTable.innerTypes().stream().count(), "inner types count of 'NoTable'");
    // type PersonTable
    var personTable = assertTypeExists(tableFieldBaseFormData, "PersonTable");
    assertHasFlags(personTable, 9);
    assertHasSuperClass(personTable, "formdata.shared.services.process.AbstractPersonTableFieldData");

    // fields of PersonTable
    assertEquals(1, personTable.fields().stream().count(), "field count of 'PersonTable'");
    var serialVersionUID6 = assertFieldExist(personTable, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");

    assertEquals(7, personTable.methods().stream().count(), "method count of 'PersonTable'");
    var addRow4 = assertMethodExist(personTable, "addRow");
    assertMethodReturnType(addRow4, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData");
    assertAnnotation(addRow4, "java.lang.Override");
    var addRow5 = assertMethodExist(personTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow5, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData");
    assertAnnotation(addRow5, "java.lang.Override");
    var createRow3 = assertMethodExist(personTable, "createRow");
    assertMethodReturnType(createRow3, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData");
    assertAnnotation(createRow3, "java.lang.Override");
    var getRowType3 = assertMethodExist(personTable, "getRowType");
    assertMethodReturnType(getRowType3, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType3, "java.lang.Override");
    var getRows2 = assertMethodExist(personTable, "getRows");
    assertMethodReturnType(getRows2, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData[]");
    assertAnnotation(getRows2, "java.lang.Override");
    var rowAt2 = assertMethodExist(personTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt2, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData");
    assertAnnotation(rowAt2, "java.lang.Override");
    var setRows2 = assertMethodExist(personTable, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData[]"});
    assertMethodReturnType(setRows2, "void");

    assertEquals(1, personTable.innerTypes().stream().count(), "inner types count of 'PersonTable'");
    // type PersonTableRowData
    var personTableRowData = assertTypeExists(personTable, "PersonTableRowData");
    assertHasFlags(personTableRowData, 9);
    assertHasSuperClass(personTableRowData, "formdata.shared.services.process.AbstractPersonTableFieldData$AbstractPersonTableRowData");

    // fields of PersonTableRowData
    assertEquals(1, personTableRowData.fields().stream().count(), "field count of 'PersonTableRowData'");
    var serialVersionUID7 = assertFieldExist(personTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID7, 26);
    assertFieldType(serialVersionUID7, "long");

    assertEquals(0, personTableRowData.methods().stream().count(), "method count of 'PersonTableRowData'");

    assertEquals(0, personTableRowData.innerTypes().stream().count(), "inner types count of 'PersonTableRowData'");
    // type Table
    var table = assertTypeExists(tableFieldBaseFormData, "Table");
    assertHasFlags(table, 9);
    assertHasSuperClass(table, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of Table
    assertEquals(1, table.fields().stream().count(), "field count of 'Table'");
    var serialVersionUID8 = assertFieldExist(table, "serialVersionUID");
    assertHasFlags(serialVersionUID8, 26);
    assertFieldType(serialVersionUID8, "long");

    assertEquals(7, table.methods().stream().count(), "method count of 'Table'");
    var addRow6 = assertMethodExist(table, "addRow");
    assertMethodReturnType(addRow6, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData");
    assertAnnotation(addRow6, "java.lang.Override");
    var addRow7 = assertMethodExist(table, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow7, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData");
    assertAnnotation(addRow7, "java.lang.Override");
    var createRow4 = assertMethodExist(table, "createRow");
    assertMethodReturnType(createRow4, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData");
    assertAnnotation(createRow4, "java.lang.Override");
    var getRowType4 = assertMethodExist(table, "getRowType");
    assertMethodReturnType(getRowType4, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType4, "java.lang.Override");
    var getRows3 = assertMethodExist(table, "getRows");
    assertMethodReturnType(getRows3, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData[]");
    assertAnnotation(getRows3, "java.lang.Override");
    var rowAt3 = assertMethodExist(table, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt3, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData");
    assertAnnotation(rowAt3, "java.lang.Override");
    var setRows3 = assertMethodExist(table, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData[]"});
    assertMethodReturnType(setRows3, "void");

    assertEquals(1, table.innerTypes().stream().count(), "inner types count of 'Table'");
    // type TableRowData
    var tableRowData = assertTypeExists(table, "TableRowData");
    assertHasFlags(tableRowData, 9);
    assertHasSuperClass(tableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of TableRowData
    assertEquals(5, tableRowData.fields().stream().count(), "field count of 'TableRowData'");
    var serialVersionUID9 = assertFieldExist(tableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID9, 26);
    assertFieldType(serialVersionUID9, "long");
    var first = assertFieldExist(tableRowData, "first");
    assertHasFlags(first, 25);
    assertFieldType(first, String.class.getName());
    var second = assertFieldExist(tableRowData, "second");
    assertHasFlags(second, 25);
    assertFieldType(second, String.class.getName());
    var m_first = assertFieldExist(tableRowData, "m_first");
    assertHasFlags(m_first, 2);
    assertFieldType(m_first, String.class.getName());
    var m_second = assertFieldExist(tableRowData, "m_second");
    assertHasFlags(m_second, 2);
    assertFieldType(m_second, String.class.getName());

    assertEquals(4, tableRowData.methods().stream().count(), "method count of 'TableRowData'");
    var getFirst = assertMethodExist(tableRowData, "getFirst");
    assertMethodReturnType(getFirst, String.class.getName());
    var setFirst = assertMethodExist(tableRowData, "setFirst", new String[]{String.class.getName()});
    assertMethodReturnType(setFirst, "void");
    var getSecond = assertMethodExist(tableRowData, "getSecond");
    assertMethodReturnType(getSecond, String.class.getName());
    var setSecond = assertMethodExist(tableRowData, "setSecond", new String[]{String.class.getName()});
    assertMethodReturnType(setSecond, "void");

    assertEquals(0, tableRowData.innerTypes().stream().count(), "inner types count of 'TableRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfTableFieldExFormData(IType tableFieldExFormData) {
    // type TableFieldExFormData
    assertHasFlags(tableFieldExFormData, 1);
    assertHasSuperClass(tableFieldExFormData, "formdata.shared.services.process.replace.TableFieldBaseFormData");

    // fields of TableFieldExFormData
    assertEquals(1, tableFieldExFormData.fields().stream().count(), "field count of 'TableFieldExFormData'");
    var serialVersionUID = assertFieldExist(tableFieldExFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(5, tableFieldExFormData.methods().stream().count(), "method count of 'TableFieldExFormData'");
    var getEmptyTableExtended = assertMethodExist(tableFieldExFormData, "getEmptyTableExtended");
    assertMethodReturnType(getEmptyTableExtended, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended");
    var getExtendedAddress = assertMethodExist(tableFieldExFormData, "getExtendedAddress");
    assertMethodReturnType(getExtendedAddress, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress");
    var getExtendedPersonTable = assertMethodExist(tableFieldExFormData, "getExtendedPersonTable");
    assertMethodReturnType(getExtendedPersonTable, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable");
    var getNoTableExtended = assertMethodExist(tableFieldExFormData, "getNoTableExtended");
    assertMethodReturnType(getNoTableExtended, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended");
    var getTableExtended = assertMethodExist(tableFieldExFormData, "getTableExtended");
    assertMethodReturnType(getTableExtended, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended");

    assertEquals(5, tableFieldExFormData.innerTypes().stream().count(), "inner types count of 'TableFieldExFormData'");
    // type EmptyTableExtended
    var emptyTableExtended = assertTypeExists(tableFieldExFormData, "EmptyTableExtended");
    assertHasFlags(emptyTableExtended, 9);
    assertHasSuperClass(emptyTableExtended, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable");
    assertAnnotation(emptyTableExtended, "org.eclipse.scout.rt.platform.Replace");

    // fields of EmptyTableExtended
    assertEquals(1, emptyTableExtended.fields().stream().count(), "field count of 'EmptyTableExtended'");
    var serialVersionUID1 = assertFieldExist(emptyTableExtended, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, emptyTableExtended.methods().stream().count(), "method count of 'EmptyTableExtended'");
    var addRow = assertMethodExist(emptyTableExtended, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(emptyTableExtended, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(emptyTableExtended, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(emptyTableExtended, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(emptyTableExtended, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(emptyTableExtended, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(emptyTableExtended, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, emptyTableExtended.innerTypes().stream().count(), "inner types count of 'EmptyTableExtended'");
    // type EmptyTableExtendedRowData
    var emptyTableExtendedRowData = assertTypeExists(emptyTableExtended, "EmptyTableExtendedRowData");
    assertHasFlags(emptyTableExtendedRowData, 9);
    assertHasSuperClass(emptyTableExtendedRowData, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData");

    // fields of EmptyTableExtendedRowData
    assertEquals(3, emptyTableExtendedRowData.fields().stream().count(), "field count of 'EmptyTableExtendedRowData'");
    var serialVersionUID2 = assertFieldExist(emptyTableExtendedRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    var single = assertFieldExist(emptyTableExtendedRowData, "single");
    assertHasFlags(single, 25);
    assertFieldType(single, String.class.getName());
    var m_single = assertFieldExist(emptyTableExtendedRowData, "m_single");
    assertHasFlags(m_single, 2);
    assertFieldType(m_single, String.class.getName());

    assertEquals(2, emptyTableExtendedRowData.methods().stream().count(), "method count of 'EmptyTableExtendedRowData'");
    var getSingle = assertMethodExist(emptyTableExtendedRowData, "getSingle");
    assertMethodReturnType(getSingle, String.class.getName());
    var setSingle = assertMethodExist(emptyTableExtendedRowData, "setSingle", new String[]{String.class.getName()});
    assertMethodReturnType(setSingle, "void");

    assertEquals(0, emptyTableExtendedRowData.innerTypes().stream().count(), "inner types count of 'EmptyTableExtendedRowData'");
    // type ExtendedAddress
    var extendedAddress = assertTypeExists(tableFieldExFormData, "ExtendedAddress");
    assertHasFlags(extendedAddress, 9);
    assertHasSuperClass(extendedAddress, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable");
    assertAnnotation(extendedAddress, "org.eclipse.scout.rt.platform.Replace");

    // fields of ExtendedAddress
    assertEquals(1, extendedAddress.fields().stream().count(), "field count of 'ExtendedAddress'");
    var serialVersionUID3 = assertFieldExist(extendedAddress, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(7, extendedAddress.methods().stream().count(), "method count of 'ExtendedAddress'");
    var addRow2 = assertMethodExist(extendedAddress, "addRow");
    assertMethodReturnType(addRow2, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData");
    assertAnnotation(addRow2, "java.lang.Override");
    var addRow3 = assertMethodExist(extendedAddress, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow3, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData");
    assertAnnotation(addRow3, "java.lang.Override");
    var createRow1 = assertMethodExist(extendedAddress, "createRow");
    assertMethodReturnType(createRow1, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData");
    assertAnnotation(createRow1, "java.lang.Override");
    var getRowType1 = assertMethodExist(extendedAddress, "getRowType");
    assertMethodReturnType(getRowType1, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType1, "java.lang.Override");
    var getRows1 = assertMethodExist(extendedAddress, "getRows");
    assertMethodReturnType(getRows1, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData[]");
    assertAnnotation(getRows1, "java.lang.Override");
    var rowAt1 = assertMethodExist(extendedAddress, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt1, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData");
    assertAnnotation(rowAt1, "java.lang.Override");
    var setRows1 = assertMethodExist(extendedAddress, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData[]"});
    assertMethodReturnType(setRows1, "void");

    assertEquals(1, extendedAddress.innerTypes().stream().count(), "inner types count of 'ExtendedAddress'");
    // type ExtendedAddressRowData
    var extendedAddressRowData = assertTypeExists(extendedAddress, "ExtendedAddressRowData");
    assertHasFlags(extendedAddressRowData, 9);
    assertHasSuperClass(extendedAddressRowData, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData");

    // fields of ExtendedAddressRowData
    assertEquals(3, extendedAddressRowData.fields().stream().count(), "field count of 'ExtendedAddressRowData'");
    var serialVersionUID4 = assertFieldExist(extendedAddressRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");
    var state = assertFieldExist(extendedAddressRowData, "state");
    assertHasFlags(state, 25);
    assertFieldType(state, String.class.getName());
    var m_state = assertFieldExist(extendedAddressRowData, "m_state");
    assertHasFlags(m_state, 2);
    assertFieldType(m_state, String.class.getName());

    assertEquals(2, extendedAddressRowData.methods().stream().count(), "method count of 'ExtendedAddressRowData'");
    var getState = assertMethodExist(extendedAddressRowData, "getState");
    assertMethodReturnType(getState, String.class.getName());
    var setState = assertMethodExist(extendedAddressRowData, "setState", new String[]{String.class.getName()});
    assertMethodReturnType(setState, "void");

    assertEquals(0, extendedAddressRowData.innerTypes().stream().count(), "inner types count of 'ExtendedAddressRowData'");
    // type ExtendedPersonTable
    var extendedPersonTable = assertTypeExists(tableFieldExFormData, "ExtendedPersonTable");
    assertHasFlags(extendedPersonTable, 9);
    assertHasSuperClass(extendedPersonTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable");
    assertAnnotation(extendedPersonTable, "org.eclipse.scout.rt.platform.Replace");

    // fields of ExtendedPersonTable
    assertEquals(1, extendedPersonTable.fields().stream().count(), "field count of 'ExtendedPersonTable'");
    var serialVersionUID5 = assertFieldExist(extendedPersonTable, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(7, extendedPersonTable.methods().stream().count(), "method count of 'ExtendedPersonTable'");
    var addRow4 = assertMethodExist(extendedPersonTable, "addRow");
    assertMethodReturnType(addRow4, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData");
    assertAnnotation(addRow4, "java.lang.Override");
    var addRow5 = assertMethodExist(extendedPersonTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow5, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData");
    assertAnnotation(addRow5, "java.lang.Override");
    var createRow2 = assertMethodExist(extendedPersonTable, "createRow");
    assertMethodReturnType(createRow2, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData");
    assertAnnotation(createRow2, "java.lang.Override");
    var getRowType2 = assertMethodExist(extendedPersonTable, "getRowType");
    assertMethodReturnType(getRowType2, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType2, "java.lang.Override");
    var getRows2 = assertMethodExist(extendedPersonTable, "getRows");
    assertMethodReturnType(getRows2, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData[]");
    assertAnnotation(getRows2, "java.lang.Override");
    var rowAt2 = assertMethodExist(extendedPersonTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt2, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData");
    assertAnnotation(rowAt2, "java.lang.Override");
    var setRows2 = assertMethodExist(extendedPersonTable, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData[]"});
    assertMethodReturnType(setRows2, "void");

    assertEquals(1, extendedPersonTable.innerTypes().stream().count(), "inner types count of 'ExtendedPersonTable'");
    // type ExtendedPersonTableRowData
    var extendedPersonTableRowData = assertTypeExists(extendedPersonTable, "ExtendedPersonTableRowData");
    assertHasFlags(extendedPersonTableRowData, 9);
    assertHasSuperClass(extendedPersonTableRowData, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData");

    // fields of ExtendedPersonTableRowData
    assertEquals(3, extendedPersonTableRowData.fields().stream().count(), "field count of 'ExtendedPersonTableRowData'");
    var serialVersionUID6 = assertFieldExist(extendedPersonTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");
    var lastName = assertFieldExist(extendedPersonTableRowData, "lastName");
    assertHasFlags(lastName, 25);
    assertFieldType(lastName, String.class.getName());
    var m_lastName = assertFieldExist(extendedPersonTableRowData, "m_lastName");
    assertHasFlags(m_lastName, 2);
    assertFieldType(m_lastName, String.class.getName());

    assertEquals(2, extendedPersonTableRowData.methods().stream().count(), "method count of 'ExtendedPersonTableRowData'");
    var getLastName = assertMethodExist(extendedPersonTableRowData, "getLastName");
    assertMethodReturnType(getLastName, String.class.getName());
    var setLastName = assertMethodExist(extendedPersonTableRowData, "setLastName", new String[]{String.class.getName()});
    assertMethodReturnType(setLastName, "void");

    assertEquals(0, extendedPersonTableRowData.innerTypes().stream().count(), "inner types count of 'ExtendedPersonTableRowData'");
    // type NoTableExtended
    var noTableExtended = assertTypeExists(tableFieldExFormData, "NoTableExtended");
    assertHasFlags(noTableExtended, 9);
    assertHasSuperClass(noTableExtended, "formdata.shared.services.process.replace.TableFieldBaseFormData$NoTable");
    assertAnnotation(noTableExtended, "org.eclipse.scout.rt.platform.Replace");

    // fields of NoTableExtended
    assertEquals(1, noTableExtended.fields().stream().count(), "field count of 'NoTableExtended'");
    var serialVersionUID7 = assertFieldExist(noTableExtended, "serialVersionUID");
    assertHasFlags(serialVersionUID7, 26);
    assertFieldType(serialVersionUID7, "long");

    assertEquals(7, noTableExtended.methods().stream().count(), "method count of 'NoTableExtended'");
    var addRow6 = assertMethodExist(noTableExtended, "addRow");
    assertMethodReturnType(addRow6, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData");
    assertAnnotation(addRow6, "java.lang.Override");
    var addRow7 = assertMethodExist(noTableExtended, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow7, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData");
    assertAnnotation(addRow7, "java.lang.Override");
    var createRow3 = assertMethodExist(noTableExtended, "createRow");
    assertMethodReturnType(createRow3, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData");
    assertAnnotation(createRow3, "java.lang.Override");
    var getRowType3 = assertMethodExist(noTableExtended, "getRowType");
    assertMethodReturnType(getRowType3, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType3, "java.lang.Override");
    var getRows3 = assertMethodExist(noTableExtended, "getRows");
    assertMethodReturnType(getRows3, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData[]");
    assertAnnotation(getRows3, "java.lang.Override");
    var rowAt3 = assertMethodExist(noTableExtended, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt3, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData");
    assertAnnotation(rowAt3, "java.lang.Override");
    var setRows3 = assertMethodExist(noTableExtended, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData[]"});
    assertMethodReturnType(setRows3, "void");

    assertEquals(1, noTableExtended.innerTypes().stream().count(), "inner types count of 'NoTableExtended'");
    // type NoTableExtendedRowData
    var noTableExtendedRowData = assertTypeExists(noTableExtended, "NoTableExtendedRowData");
    assertHasFlags(noTableExtendedRowData, 9);
    assertHasSuperClass(noTableExtendedRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of NoTableExtendedRowData
    assertEquals(3, noTableExtendedRowData.fields().stream().count(), "field count of 'NoTableExtendedRowData'");
    var serialVersionUID8 = assertFieldExist(noTableExtendedRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID8, 26);
    assertFieldType(serialVersionUID8, "long");
    var new_ = assertFieldExist(noTableExtendedRowData, "new_");
    assertHasFlags(new_, 25);
    assertFieldType(new_, String.class.getName());
    var m_new = assertFieldExist(noTableExtendedRowData, "m_new");
    assertHasFlags(m_new, 2);
    assertFieldType(m_new, String.class.getName());

    assertEquals(2, noTableExtendedRowData.methods().stream().count(), "method count of 'NoTableExtendedRowData'");
    var getNew = assertMethodExist(noTableExtendedRowData, "getNew");
    assertMethodReturnType(getNew, String.class.getName());
    var setNew = assertMethodExist(noTableExtendedRowData, "setNew", new String[]{String.class.getName()});
    assertMethodReturnType(setNew, "void");

    assertEquals(0, noTableExtendedRowData.innerTypes().stream().count(), "inner types count of 'NoTableExtendedRowData'");
    // type TableExtended
    var tableExtended = assertTypeExists(tableFieldExFormData, "TableExtended");
    assertHasFlags(tableExtended, 9);
    assertHasSuperClass(tableExtended, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table");
    assertAnnotation(tableExtended, "org.eclipse.scout.rt.platform.Replace");

    // fields of TableExtended
    assertEquals(1, tableExtended.fields().stream().count(), "field count of 'TableExtended'");
    var serialVersionUID9 = assertFieldExist(tableExtended, "serialVersionUID");
    assertHasFlags(serialVersionUID9, 26);
    assertFieldType(serialVersionUID9, "long");

    assertEquals(7, tableExtended.methods().stream().count(), "method count of 'TableExtended'");
    var addRow8 = assertMethodExist(tableExtended, "addRow");
    assertMethodReturnType(addRow8, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData");
    assertAnnotation(addRow8, "java.lang.Override");
    var addRow9 = assertMethodExist(tableExtended, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow9, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData");
    assertAnnotation(addRow9, "java.lang.Override");
    var createRow4 = assertMethodExist(tableExtended, "createRow");
    assertMethodReturnType(createRow4, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData");
    assertAnnotation(createRow4, "java.lang.Override");
    var getRowType4 = assertMethodExist(tableExtended, "getRowType");
    assertMethodReturnType(getRowType4, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType4, "java.lang.Override");
    var getRows4 = assertMethodExist(tableExtended, "getRows");
    assertMethodReturnType(getRows4, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData[]");
    assertAnnotation(getRows4, "java.lang.Override");
    var rowAt4 = assertMethodExist(tableExtended, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt4, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData");
    assertAnnotation(rowAt4, "java.lang.Override");
    var setRows4 = assertMethodExist(tableExtended, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData[]"});
    assertMethodReturnType(setRows4, "void");

    assertEquals(1, tableExtended.innerTypes().stream().count(), "inner types count of 'TableExtended'");
    // type TableExtendedRowData
    var tableExtendedRowData = assertTypeExists(tableExtended, "TableExtendedRowData");
    assertHasFlags(tableExtendedRowData, 9);
    assertHasSuperClass(tableExtendedRowData, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData");

    // fields of TableExtendedRowData
    assertEquals(3, tableExtendedRowData.fields().stream().count(), "field count of 'TableExtendedRowData'");
    var serialVersionUID10 = assertFieldExist(tableExtendedRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID10, 26);
    assertFieldType(serialVersionUID10, "long");
    var boolean_ = assertFieldExist(tableExtendedRowData, "boolean_");
    assertHasFlags(boolean_, 25);
    assertFieldType(boolean_, String.class.getName());
    var m_boolean = assertFieldExist(tableExtendedRowData, "m_boolean");
    assertHasFlags(m_boolean, 2);
    assertFieldType(m_boolean, Boolean.class.getName());

    assertEquals(2, tableExtendedRowData.methods().stream().count(), "method count of 'TableExtendedRowData'");
    var getBoolean = assertMethodExist(tableExtendedRowData, "getBoolean");
    assertMethodReturnType(getBoolean, Boolean.class.getName());
    var setBoolean = assertMethodExist(tableExtendedRowData, "setBoolean", new String[]{Boolean.class.getName()});
    assertMethodReturnType(setBoolean, "void");

    assertEquals(0, tableExtendedRowData.innerTypes().stream().count(), "inner types count of 'TableExtendedRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfBaseWithExtendedTableFormData(IType baseWithExtendedTableFormData) {
    // type BaseWithExtendedTableFormData
    assertHasFlags(baseWithExtendedTableFormData, 1);
    assertHasSuperClass(baseWithExtendedTableFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");

    // fields of BaseWithExtendedTableFormData
    assertEquals(1, baseWithExtendedTableFormData.fields().stream().count(), "field count of 'BaseWithExtendedTableFormData'");
    var serialVersionUID = assertFieldExist(baseWithExtendedTableFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, baseWithExtendedTableFormData.methods().stream().count(), "method count of 'BaseWithExtendedTableFormData'");
    var getTableInForm = assertMethodExist(baseWithExtendedTableFormData, "getTableInForm");
    assertMethodReturnType(getTableInForm, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm");

    assertEquals(1, baseWithExtendedTableFormData.innerTypes().stream().count(), "inner types count of 'BaseWithExtendedTableFormData'");
    // type TableInForm
    var tableInForm = assertTypeExists(baseWithExtendedTableFormData, "TableInForm");
    assertHasFlags(tableInForm, 9);
    assertHasSuperClass(tableInForm, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of TableInForm
    assertEquals(1, tableInForm.fields().stream().count(), "field count of 'TableInForm'");
    var serialVersionUID1 = assertFieldExist(tableInForm, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, tableInForm.methods().stream().count(), "method count of 'TableInForm'");
    var addRow = assertMethodExist(tableInForm, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(tableInForm, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(tableInForm, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(tableInForm, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(tableInForm, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(tableInForm, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(tableInForm, "setRows", new String[]{"formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, tableInForm.innerTypes().stream().count(), "inner types count of 'TableInForm'");
    // type TableInFormRowData
    var tableInFormRowData = assertTypeExists(tableInForm, "TableInFormRowData");
    assertHasFlags(tableInFormRowData, 9);
    assertHasSuperClass(tableInFormRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of TableInFormRowData
    assertEquals(5, tableInFormRowData.fields().stream().count(), "field count of 'TableInFormRowData'");
    var serialVersionUID2 = assertFieldExist(tableInFormRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    var colInAbstractTable = assertFieldExist(tableInFormRowData, "colInAbstractTable");
    assertHasFlags(colInAbstractTable, 25);
    assertFieldType(colInAbstractTable, String.class.getName());
    var colInDesktopForm = assertFieldExist(tableInFormRowData, "colInDesktopForm");
    assertHasFlags(colInDesktopForm, 25);
    assertFieldType(colInDesktopForm, String.class.getName());
    var m_colInAbstractTable = assertFieldExist(tableInFormRowData, "m_colInAbstractTable");
    assertHasFlags(m_colInAbstractTable, 2);
    assertFieldType(m_colInAbstractTable, String.class.getName());
    var m_colInDesktopForm = assertFieldExist(tableInFormRowData, "m_colInDesktopForm");
    assertHasFlags(m_colInDesktopForm, 2);
    assertFieldType(m_colInDesktopForm, String.class.getName());

    assertEquals(4, tableInFormRowData.methods().stream().count(), "method count of 'TableInFormRowData'");
    var getColInAbstractTable = assertMethodExist(tableInFormRowData, "getColInAbstractTable");
    assertMethodReturnType(getColInAbstractTable, String.class.getName());
    var setColInAbstractTable = assertMethodExist(tableInFormRowData, "setColInAbstractTable", new String[]{String.class.getName()});
    assertMethodReturnType(setColInAbstractTable, "void");
    var getColInDesktopForm = assertMethodExist(tableInFormRowData, "getColInDesktopForm");
    assertMethodReturnType(getColInDesktopForm, String.class.getName());
    var setColInDesktopForm = assertMethodExist(tableInFormRowData, "setColInDesktopForm", new String[]{String.class.getName()});
    assertMethodReturnType(setColInDesktopForm, "void");

    assertEquals(0, tableInFormRowData.innerTypes().stream().count(), "inner types count of 'TableInFormRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfChildWithExtendedTableFormData(IType childWithExtendedTableFormData) {
    // type ChildWithExtendedTableFormData
    assertHasFlags(childWithExtendedTableFormData, 1);
    assertHasSuperClass(childWithExtendedTableFormData, "formdata.shared.services.BaseWithExtendedTableFormData");

    // fields of ChildWithExtendedTableFormData
    assertEquals(1, childWithExtendedTableFormData.fields().stream().count(), "field count of 'ChildWithExtendedTableFormData'");
    var serialVersionUID = assertFieldExist(childWithExtendedTableFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, childWithExtendedTableFormData.methods().stream().count(), "method count of 'ChildWithExtendedTableFormData'");
    var getChildTable = assertMethodExist(childWithExtendedTableFormData, "getChildTable");
    assertMethodReturnType(getChildTable, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable");

    assertEquals(1, childWithExtendedTableFormData.innerTypes().stream().count(), "inner types count of 'ChildWithExtendedTableFormData'");
    // type ChildTable
    var childTable = assertTypeExists(childWithExtendedTableFormData, "ChildTable");
    assertHasFlags(childTable, 9);
    assertHasSuperClass(childTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of ChildTable
    assertEquals(1, childTable.fields().stream().count(), "field count of 'ChildTable'");
    var serialVersionUID1 = assertFieldExist(childTable, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, childTable.methods().stream().count(), "method count of 'ChildTable'");
    var addRow = assertMethodExist(childTable, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(childTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(childTable, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(childTable, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(childTable, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(childTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(childTable, "setRows", new String[]{"formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, childTable.innerTypes().stream().count(), "inner types count of 'ChildTable'");
    // type ChildTableRowData
    var childTableRowData = assertTypeExists(childTable, "ChildTableRowData");
    assertHasFlags(childTableRowData, 9);
    assertHasSuperClass(childTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of ChildTableRowData
    assertEquals(7, childTableRowData.fields().stream().count(), "field count of 'ChildTableRowData'");
    var serialVersionUID2 = assertFieldExist(childTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    var col1InChildForm = assertFieldExist(childTableRowData, "col1InChildForm");
    assertHasFlags(col1InChildForm, 25);
    assertFieldType(col1InChildForm, String.class.getName());
    var colInAbstractTable = assertFieldExist(childTableRowData, "colInAbstractTable");
    assertHasFlags(colInAbstractTable, 25);
    assertFieldType(colInAbstractTable, String.class.getName());
    var colInDesktopForm = assertFieldExist(childTableRowData, "colInDesktopForm");
    assertHasFlags(colInDesktopForm, 25);
    assertFieldType(colInDesktopForm, String.class.getName());
    var m_col1InChildForm = assertFieldExist(childTableRowData, "m_col1InChildForm");
    assertHasFlags(m_col1InChildForm, 2);
    assertFieldType(m_col1InChildForm, String.class.getName());
    var m_colInAbstractTable = assertFieldExist(childTableRowData, "m_colInAbstractTable");
    assertHasFlags(m_colInAbstractTable, 2);
    assertFieldType(m_colInAbstractTable, String.class.getName());
    var m_colInDesktopForm = assertFieldExist(childTableRowData, "m_colInDesktopForm");
    assertHasFlags(m_colInDesktopForm, 2);
    assertFieldType(m_colInDesktopForm, String.class.getName());

    assertEquals(6, childTableRowData.methods().stream().count(), "method count of 'ChildTableRowData'");
    var getCol1InChildForm = assertMethodExist(childTableRowData, "getCol1InChildForm");
    assertMethodReturnType(getCol1InChildForm, String.class.getName());
    var setCol1InChildForm = assertMethodExist(childTableRowData, "setCol1InChildForm", new String[]{String.class.getName()});
    assertMethodReturnType(setCol1InChildForm, "void");
    var getColInAbstractTable = assertMethodExist(childTableRowData, "getColInAbstractTable");
    assertMethodReturnType(getColInAbstractTable, String.class.getName());
    var setColInAbstractTable = assertMethodExist(childTableRowData, "setColInAbstractTable", new String[]{String.class.getName()});
    assertMethodReturnType(setColInAbstractTable, "void");
    var getColInDesktopForm = assertMethodExist(childTableRowData, "getColInDesktopForm");
    assertMethodReturnType(getColInDesktopForm, String.class.getName());
    var setColInDesktopForm = assertMethodExist(childTableRowData, "setColInDesktopForm", new String[]{String.class.getName()});
    assertMethodReturnType(setColInDesktopForm, "void");

    assertEquals(0, childTableRowData.innerTypes().stream().count(), "inner types count of 'ChildTableRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfColumnWithoutTypeFormData(IType columnWithoutTypeFormData) {
    assertHasFlags(columnWithoutTypeFormData, 1);
    assertHasSuperClass(columnWithoutTypeFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");
    assertEquals(1, columnWithoutTypeFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(columnWithoutTypeFormData, "javax.annotation.Generated");

    // fields of ColumnWithoutTypeFormData
    assertEquals(1, columnWithoutTypeFormData.fields().stream().count(), "field count of 'formdata.shared.ui.forms.ColumnWithoutTypeFormData'");
    var serialVersionUID = assertFieldExist(columnWithoutTypeFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, columnWithoutTypeFormData.methods().stream().count(), "method count of 'formdata.shared.ui.forms.ColumnWithoutTypeFormData'");
    var getMyTable = assertMethodExist(columnWithoutTypeFormData, "getMyTable");
    assertMethodReturnType(getMyTable, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable");
    assertEquals(0, getMyTable.annotations().stream().count(), "annotation count");

    assertEquals(1, columnWithoutTypeFormData.innerTypes().stream().count(), "inner types count of 'ColumnWithoutTypeFormData'");
    // type MyTable
    var myTable = assertTypeExists(columnWithoutTypeFormData, "MyTable");
    assertHasFlags(myTable, 9);
    assertHasSuperClass(myTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");
    assertEquals(0, myTable.annotations().stream().count(), "annotation count");

    // fields of MyTable
    assertEquals(1, myTable.fields().stream().count(), "field count of 'formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable'");
    var serialVersionUID1 = assertFieldExist(myTable, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(7, myTable.methods().stream().count(), "method count of 'formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable'");
    var addRow = assertMethodExist(myTable, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(myTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(myTable, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(myTable, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(myTable, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(myTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(myTable, "setRows", new String[]{"formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, myTable.innerTypes().stream().count(), "inner types count of 'MyTable'");
    // type MyTableRowData
    var myTableRowData = assertTypeExists(myTable, "MyTableRowData");
    assertHasFlags(myTableRowData, 9);
    assertHasSuperClass(myTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");
    assertEquals(0, myTableRowData.annotations().stream().count(), "annotation count");

    // fields of MyTableRowData
    assertEquals(3, myTableRowData.fields().stream().count(), "field count of 'formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData'");
    var serialVersionUID2 = assertFieldExist(myTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");
    var my = assertFieldExist(myTableRowData, "my");
    assertHasFlags(my, 25);
    assertFieldType(my, "java.lang.String");
    assertEquals(0, my.annotations().stream().count(), "annotation count");
    var m_my = assertFieldExist(myTableRowData, "m_my");
    assertHasFlags(m_my, 2);
    assertFieldType(m_my, "java.lang.Object");
    assertEquals(0, m_my.annotations().stream().count(), "annotation count");

    assertEquals(2, myTableRowData.methods().stream().count(), "method count of 'formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData'");
    var getMy = assertMethodExist(myTableRowData, "getMy");
    assertMethodReturnType(getMy, "java.lang.Object");
    assertEquals(0, getMy.annotations().stream().count(), "annotation count");
    var setMy = assertMethodExist(myTableRowData, "setMy", new String[]{"java.lang.Object"});
    assertMethodReturnType(setMy, "void");
    assertEquals(0, setMy.annotations().stream().count(), "annotation count");

    assertEquals(0, myTableRowData.innerTypes().stream().count(), "inner types count of 'MyTableRowData'");
  }

}
