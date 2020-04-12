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
    IField serialVersionUID = assertFieldExist(abstractAddressTableFieldData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(7, abstractAddressTableFieldData.methods().stream().count(), "method count of 'AbstractAddressTableFieldData'");
    IMethod addRow = assertMethodExist(abstractAddressTableFieldData, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(abstractAddressTableFieldData, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(abstractAddressTableFieldData, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(abstractAddressTableFieldData, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(abstractAddressTableFieldData, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(abstractAddressTableFieldData, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = assertMethodExist(abstractAddressTableFieldData, "setRows", new String[]{"formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, abstractAddressTableFieldData.innerTypes().stream().count(), "inner types count of 'AbstractAddressTableFieldData'");
    // type AbstractAddressTableRowData
    IType abstractAddressTableRowData = assertTypeExists(abstractAddressTableFieldData, "AbstractAddressTableRowData");
    assertHasFlags(abstractAddressTableRowData, 1033);
    assertHasSuperClass(abstractAddressTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of AbstractAddressTableRowData
    assertEquals(7, abstractAddressTableRowData.fields().stream().count(), "field count of 'AbstractAddressTableRowData'");
    IField serialVersionUID1 = assertFieldExist(abstractAddressTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    IField addressId = assertFieldExist(abstractAddressTableRowData, "addressId");
    assertHasFlags(addressId, 25);
    assertFieldType(addressId, String.class.getName());
    IField street = assertFieldExist(abstractAddressTableRowData, "street");
    assertHasFlags(street, 25);
    assertFieldType(street, String.class.getName());
    IField poBoxAddress = assertFieldExist(abstractAddressTableRowData, "poBoxAddress");
    assertHasFlags(poBoxAddress, 25);
    assertFieldType(poBoxAddress, String.class.getName());
    IField m_addressId = assertFieldExist(abstractAddressTableRowData, "m_addressId");
    assertHasFlags(m_addressId, 2);
    assertFieldType(m_addressId, String.class.getName());
    IField m_street = assertFieldExist(abstractAddressTableRowData, "m_street");
    assertHasFlags(m_street, 2);
    assertFieldType(m_street, String.class.getName());
    IField m_poBoxAddress = assertFieldExist(abstractAddressTableRowData, "m_poBoxAddress");
    assertHasFlags(m_poBoxAddress, 2);
    assertFieldType(m_poBoxAddress, Boolean.class.getName());

    assertEquals(6, abstractAddressTableRowData.methods().stream().count(), "method count of 'AbstractAddressTableRowData'");
    IMethod getAddressId = assertMethodExist(abstractAddressTableRowData, "getAddressId", new String[]{});
    assertMethodReturnType(getAddressId, String.class.getName());
    IMethod setAddressId = assertMethodExist(abstractAddressTableRowData, "setAddressId", new String[]{String.class.getName()});
    assertMethodReturnType(setAddressId, "void");
    IMethod getStreet = assertMethodExist(abstractAddressTableRowData, "getStreet", new String[]{});
    assertMethodReturnType(getStreet, String.class.getName());
    IMethod setStreet = assertMethodExist(abstractAddressTableRowData, "setStreet", new String[]{String.class.getName()});
    assertMethodReturnType(setStreet, "void");
    IMethod getPoBoxAddress = assertMethodExist(abstractAddressTableRowData, "getPoBoxAddress", new String[]{});
    assertMethodReturnType(getPoBoxAddress, Boolean.class.getName());
    IMethod setPoBoxAddress = assertMethodExist(abstractAddressTableRowData, "setPoBoxAddress", new String[]{Boolean.class.getName()});
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
    IField serialVersionUID = assertFieldExist(tableFieldBaseFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(5, tableFieldBaseFormData.methods().stream().count(), "method count of 'TableFieldBaseFormData'");
    IMethod getAddressTable = assertMethodExist(tableFieldBaseFormData, "getAddressTable", new String[]{});
    assertMethodReturnType(getAddressTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable");
    IMethod getEmptyTable = assertMethodExist(tableFieldBaseFormData, "getEmptyTable", new String[]{});
    assertMethodReturnType(getEmptyTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable");
    IMethod getNoTable = assertMethodExist(tableFieldBaseFormData, "getNoTable", new String[]{});
    assertMethodReturnType(getNoTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$NoTable");
    IMethod getPersonTable = assertMethodExist(tableFieldBaseFormData, "getPersonTable", new String[]{});
    assertMethodReturnType(getPersonTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable");
    IMethod getTable = assertMethodExist(tableFieldBaseFormData, "getTable", new String[]{});
    assertMethodReturnType(getTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table");

    assertEquals(5, tableFieldBaseFormData.innerTypes().stream().count(), "inner types count of 'TableFieldBaseFormData'");
    // type AddressTable
    IType addressTable = assertTypeExists(tableFieldBaseFormData, "AddressTable");
    assertHasFlags(addressTable, 9);
    assertHasSuperClass(addressTable, "formdata.shared.services.process.AbstractAddressTableFieldData");

    // fields of AddressTable
    assertEquals(1, addressTable.fields().stream().count(), "field count of 'AddressTable'");
    IField serialVersionUID1 = assertFieldExist(addressTable, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, addressTable.methods().stream().count(), "method count of 'AddressTable'");
    IMethod addRow = assertMethodExist(addressTable, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(addressTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(addressTable, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(addressTable, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(addressTable, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(addressTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = assertMethodExist(addressTable, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, addressTable.innerTypes().stream().count(), "inner types count of 'AddressTable'");
    // type AddressTableRowData
    IType addressTableRowData = assertTypeExists(addressTable, "AddressTableRowData");
    assertHasFlags(addressTableRowData, 9);
    assertHasSuperClass(addressTableRowData, "formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData");

    // fields of AddressTableRowData
    assertEquals(3, addressTableRowData.fields().stream().count(), "field count of 'AddressTableRowData'");
    IField serialVersionUID2 = assertFieldExist(addressTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    IField city = assertFieldExist(addressTableRowData, "city");
    assertHasFlags(city, 25);
    assertFieldType(city, String.class.getName());
    IField m_city = assertFieldExist(addressTableRowData, "m_city");
    assertHasFlags(m_city, 2);
    assertFieldType(m_city, String.class.getName());

    assertEquals(2, addressTableRowData.methods().stream().count(), "method count of 'AddressTableRowData'");
    IMethod getCity = assertMethodExist(addressTableRowData, "getCity", new String[]{});
    assertMethodReturnType(getCity, String.class.getName());
    IMethod setCity = assertMethodExist(addressTableRowData, "setCity", new String[]{String.class.getName()});
    assertMethodReturnType(setCity, "void");

    assertEquals(0, addressTableRowData.innerTypes().stream().count(), "inner types count of 'AddressTableRowData'");
    // type EmptyTable
    IType emptyTable = assertTypeExists(tableFieldBaseFormData, "EmptyTable");
    assertHasFlags(emptyTable, 9);
    assertHasSuperClass(emptyTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of EmptyTable
    assertEquals(1, emptyTable.fields().stream().count(), "field count of 'EmptyTable'");
    IField serialVersionUID3 = assertFieldExist(emptyTable, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(7, emptyTable.methods().stream().count(), "method count of 'EmptyTable'");
    IMethod addRow2 = assertMethodExist(emptyTable, "addRow", new String[]{});
    assertMethodReturnType(addRow2, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData");
    assertAnnotation(addRow2, "java.lang.Override");
    IMethod addRow3 = assertMethodExist(emptyTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow3, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData");
    assertAnnotation(addRow3, "java.lang.Override");
    IMethod createRow1 = assertMethodExist(emptyTable, "createRow", new String[]{});
    assertMethodReturnType(createRow1, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData");
    assertAnnotation(createRow1, "java.lang.Override");
    IMethod getRowType1 = assertMethodExist(emptyTable, "getRowType", new String[]{});
    assertMethodReturnType(getRowType1, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType1, "java.lang.Override");
    IMethod getRows1 = assertMethodExist(emptyTable, "getRows", new String[]{});
    assertMethodReturnType(getRows1, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData[]");
    assertAnnotation(getRows1, "java.lang.Override");
    IMethod rowAt1 = assertMethodExist(emptyTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt1, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData");
    assertAnnotation(rowAt1, "java.lang.Override");
    IMethod setRows1 = assertMethodExist(emptyTable, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData[]"});
    assertMethodReturnType(setRows1, "void");

    assertEquals(1, emptyTable.innerTypes().stream().count(), "inner types count of 'EmptyTable'");
    // type EmptyTableRowData
    IType emptyTableRowData = assertTypeExists(emptyTable, "EmptyTableRowData");
    assertHasFlags(emptyTableRowData, 9);
    assertHasSuperClass(emptyTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of EmptyTableRowData
    assertEquals(1, emptyTableRowData.fields().stream().count(), "field count of 'EmptyTableRowData'");
    IField serialVersionUID4 = assertFieldExist(emptyTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(0, emptyTableRowData.methods().stream().count(), "method count of 'EmptyTableRowData'");

    assertEquals(0, emptyTableRowData.innerTypes().stream().count(), "inner types count of 'EmptyTableRowData'");
    // type NoTable
    IType noTable = assertTypeExists(tableFieldBaseFormData, "NoTable");
    assertHasFlags(noTable, 9);
    assertHasSuperClass(noTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of NoTable
    assertEquals(1, noTable.fields().stream().count(), "field count of 'NoTable'");
    IField serialVersionUID5 = assertFieldExist(noTable, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(2, noTable.methods().stream().count(), "method count of 'NoTable'");
    IMethod createRow2 = assertMethodExist(noTable, "createRow", new String[]{});
    assertMethodReturnType(createRow2, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");
    assertAnnotation(createRow2, "java.lang.Override");
    IMethod getRowType2 = assertMethodExist(noTable, "getRowType", new String[]{});
    assertMethodReturnType(getRowType2, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType2, "java.lang.Override");

    assertEquals(0, noTable.innerTypes().stream().count(), "inner types count of 'NoTable'");
    // type PersonTable
    IType personTable = assertTypeExists(tableFieldBaseFormData, "PersonTable");
    assertHasFlags(personTable, 9);
    assertHasSuperClass(personTable, "formdata.shared.services.process.AbstractPersonTableFieldData");

    // fields of PersonTable
    assertEquals(1, personTable.fields().stream().count(), "field count of 'PersonTable'");
    IField serialVersionUID6 = assertFieldExist(personTable, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");

    assertEquals(7, personTable.methods().stream().count(), "method count of 'PersonTable'");
    IMethod addRow4 = assertMethodExist(personTable, "addRow", new String[]{});
    assertMethodReturnType(addRow4, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData");
    assertAnnotation(addRow4, "java.lang.Override");
    IMethod addRow5 = assertMethodExist(personTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow5, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData");
    assertAnnotation(addRow5, "java.lang.Override");
    IMethod createRow3 = assertMethodExist(personTable, "createRow", new String[]{});
    assertMethodReturnType(createRow3, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData");
    assertAnnotation(createRow3, "java.lang.Override");
    IMethod getRowType3 = assertMethodExist(personTable, "getRowType", new String[]{});
    assertMethodReturnType(getRowType3, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType3, "java.lang.Override");
    IMethod getRows2 = assertMethodExist(personTable, "getRows", new String[]{});
    assertMethodReturnType(getRows2, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData[]");
    assertAnnotation(getRows2, "java.lang.Override");
    IMethod rowAt2 = assertMethodExist(personTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt2, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData");
    assertAnnotation(rowAt2, "java.lang.Override");
    IMethod setRows2 = assertMethodExist(personTable, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData[]"});
    assertMethodReturnType(setRows2, "void");

    assertEquals(1, personTable.innerTypes().stream().count(), "inner types count of 'PersonTable'");
    // type PersonTableRowData
    IType personTableRowData = assertTypeExists(personTable, "PersonTableRowData");
    assertHasFlags(personTableRowData, 9);
    assertHasSuperClass(personTableRowData, "formdata.shared.services.process.AbstractPersonTableFieldData$AbstractPersonTableRowData");

    // fields of PersonTableRowData
    assertEquals(1, personTableRowData.fields().stream().count(), "field count of 'PersonTableRowData'");
    IField serialVersionUID7 = assertFieldExist(personTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID7, 26);
    assertFieldType(serialVersionUID7, "long");

    assertEquals(0, personTableRowData.methods().stream().count(), "method count of 'PersonTableRowData'");

    assertEquals(0, personTableRowData.innerTypes().stream().count(), "inner types count of 'PersonTableRowData'");
    // type Table
    IType table = assertTypeExists(tableFieldBaseFormData, "Table");
    assertHasFlags(table, 9);
    assertHasSuperClass(table, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of Table
    assertEquals(1, table.fields().stream().count(), "field count of 'Table'");
    IField serialVersionUID8 = assertFieldExist(table, "serialVersionUID");
    assertHasFlags(serialVersionUID8, 26);
    assertFieldType(serialVersionUID8, "long");

    assertEquals(7, table.methods().stream().count(), "method count of 'Table'");
    IMethod addRow6 = assertMethodExist(table, "addRow", new String[]{});
    assertMethodReturnType(addRow6, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData");
    assertAnnotation(addRow6, "java.lang.Override");
    IMethod addRow7 = assertMethodExist(table, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow7, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData");
    assertAnnotation(addRow7, "java.lang.Override");
    IMethod createRow4 = assertMethodExist(table, "createRow", new String[]{});
    assertMethodReturnType(createRow4, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData");
    assertAnnotation(createRow4, "java.lang.Override");
    IMethod getRowType4 = assertMethodExist(table, "getRowType", new String[]{});
    assertMethodReturnType(getRowType4, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType4, "java.lang.Override");
    IMethod getRows3 = assertMethodExist(table, "getRows", new String[]{});
    assertMethodReturnType(getRows3, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData[]");
    assertAnnotation(getRows3, "java.lang.Override");
    IMethod rowAt3 = assertMethodExist(table, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt3, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData");
    assertAnnotation(rowAt3, "java.lang.Override");
    IMethod setRows3 = assertMethodExist(table, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData[]"});
    assertMethodReturnType(setRows3, "void");

    assertEquals(1, table.innerTypes().stream().count(), "inner types count of 'Table'");
    // type TableRowData
    IType tableRowData = assertTypeExists(table, "TableRowData");
    assertHasFlags(tableRowData, 9);
    assertHasSuperClass(tableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of TableRowData
    assertEquals(5, tableRowData.fields().stream().count(), "field count of 'TableRowData'");
    IField serialVersionUID9 = assertFieldExist(tableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID9, 26);
    assertFieldType(serialVersionUID9, "long");
    IField first = assertFieldExist(tableRowData, "first");
    assertHasFlags(first, 25);
    assertFieldType(first, String.class.getName());
    IField second = assertFieldExist(tableRowData, "second");
    assertHasFlags(second, 25);
    assertFieldType(second, String.class.getName());
    IField m_first = assertFieldExist(tableRowData, "m_first");
    assertHasFlags(m_first, 2);
    assertFieldType(m_first, String.class.getName());
    IField m_second = assertFieldExist(tableRowData, "m_second");
    assertHasFlags(m_second, 2);
    assertFieldType(m_second, String.class.getName());

    assertEquals(4, tableRowData.methods().stream().count(), "method count of 'TableRowData'");
    IMethod getFirst = assertMethodExist(tableRowData, "getFirst", new String[]{});
    assertMethodReturnType(getFirst, String.class.getName());
    IMethod setFirst = assertMethodExist(tableRowData, "setFirst", new String[]{String.class.getName()});
    assertMethodReturnType(setFirst, "void");
    IMethod getSecond = assertMethodExist(tableRowData, "getSecond", new String[]{});
    assertMethodReturnType(getSecond, String.class.getName());
    IMethod setSecond = assertMethodExist(tableRowData, "setSecond", new String[]{String.class.getName()});
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
    IField serialVersionUID = assertFieldExist(tableFieldExFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(5, tableFieldExFormData.methods().stream().count(), "method count of 'TableFieldExFormData'");
    IMethod getEmptyTableExtended = assertMethodExist(tableFieldExFormData, "getEmptyTableExtended", new String[]{});
    assertMethodReturnType(getEmptyTableExtended, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended");
    IMethod getExtendedAddress = assertMethodExist(tableFieldExFormData, "getExtendedAddress", new String[]{});
    assertMethodReturnType(getExtendedAddress, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress");
    IMethod getExtendedPersonTable = assertMethodExist(tableFieldExFormData, "getExtendedPersonTable", new String[]{});
    assertMethodReturnType(getExtendedPersonTable, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable");
    IMethod getNoTableExtended = assertMethodExist(tableFieldExFormData, "getNoTableExtended", new String[]{});
    assertMethodReturnType(getNoTableExtended, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended");
    IMethod getTableExtended = assertMethodExist(tableFieldExFormData, "getTableExtended", new String[]{});
    assertMethodReturnType(getTableExtended, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended");

    assertEquals(5, tableFieldExFormData.innerTypes().stream().count(), "inner types count of 'TableFieldExFormData'");
    // type EmptyTableExtended
    IType emptyTableExtended = assertTypeExists(tableFieldExFormData, "EmptyTableExtended");
    assertHasFlags(emptyTableExtended, 9);
    assertHasSuperClass(emptyTableExtended, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable");
    assertAnnotation(emptyTableExtended, "org.eclipse.scout.rt.platform.Replace");

    // fields of EmptyTableExtended
    assertEquals(1, emptyTableExtended.fields().stream().count(), "field count of 'EmptyTableExtended'");
    IField serialVersionUID1 = assertFieldExist(emptyTableExtended, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, emptyTableExtended.methods().stream().count(), "method count of 'EmptyTableExtended'");
    IMethod addRow = assertMethodExist(emptyTableExtended, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(emptyTableExtended, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(emptyTableExtended, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(emptyTableExtended, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(emptyTableExtended, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(emptyTableExtended, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = assertMethodExist(emptyTableExtended, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldExFormData$EmptyTableExtended$EmptyTableExtendedRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, emptyTableExtended.innerTypes().stream().count(), "inner types count of 'EmptyTableExtended'");
    // type EmptyTableExtendedRowData
    IType emptyTableExtendedRowData = assertTypeExists(emptyTableExtended, "EmptyTableExtendedRowData");
    assertHasFlags(emptyTableExtendedRowData, 9);
    assertHasSuperClass(emptyTableExtendedRowData, "formdata.shared.services.process.replace.TableFieldBaseFormData$EmptyTable$EmptyTableRowData");

    // fields of EmptyTableExtendedRowData
    assertEquals(3, emptyTableExtendedRowData.fields().stream().count(), "field count of 'EmptyTableExtendedRowData'");
    IField serialVersionUID2 = assertFieldExist(emptyTableExtendedRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    IField single = assertFieldExist(emptyTableExtendedRowData, "single");
    assertHasFlags(single, 25);
    assertFieldType(single, String.class.getName());
    IField m_single = assertFieldExist(emptyTableExtendedRowData, "m_single");
    assertHasFlags(m_single, 2);
    assertFieldType(m_single, String.class.getName());

    assertEquals(2, emptyTableExtendedRowData.methods().stream().count(), "method count of 'EmptyTableExtendedRowData'");
    IMethod getSingle = assertMethodExist(emptyTableExtendedRowData, "getSingle", new String[]{});
    assertMethodReturnType(getSingle, String.class.getName());
    IMethod setSingle = assertMethodExist(emptyTableExtendedRowData, "setSingle", new String[]{String.class.getName()});
    assertMethodReturnType(setSingle, "void");

    assertEquals(0, emptyTableExtendedRowData.innerTypes().stream().count(), "inner types count of 'EmptyTableExtendedRowData'");
    // type ExtendedAddress
    IType extendedAddress = assertTypeExists(tableFieldExFormData, "ExtendedAddress");
    assertHasFlags(extendedAddress, 9);
    assertHasSuperClass(extendedAddress, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable");
    assertAnnotation(extendedAddress, "org.eclipse.scout.rt.platform.Replace");

    // fields of ExtendedAddress
    assertEquals(1, extendedAddress.fields().stream().count(), "field count of 'ExtendedAddress'");
    IField serialVersionUID3 = assertFieldExist(extendedAddress, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(7, extendedAddress.methods().stream().count(), "method count of 'ExtendedAddress'");
    IMethod addRow2 = assertMethodExist(extendedAddress, "addRow", new String[]{});
    assertMethodReturnType(addRow2, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData");
    assertAnnotation(addRow2, "java.lang.Override");
    IMethod addRow3 = assertMethodExist(extendedAddress, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow3, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData");
    assertAnnotation(addRow3, "java.lang.Override");
    IMethod createRow1 = assertMethodExist(extendedAddress, "createRow", new String[]{});
    assertMethodReturnType(createRow1, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData");
    assertAnnotation(createRow1, "java.lang.Override");
    IMethod getRowType1 = assertMethodExist(extendedAddress, "getRowType", new String[]{});
    assertMethodReturnType(getRowType1, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType1, "java.lang.Override");
    IMethod getRows1 = assertMethodExist(extendedAddress, "getRows", new String[]{});
    assertMethodReturnType(getRows1, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData[]");
    assertAnnotation(getRows1, "java.lang.Override");
    IMethod rowAt1 = assertMethodExist(extendedAddress, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt1, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData");
    assertAnnotation(rowAt1, "java.lang.Override");
    IMethod setRows1 = assertMethodExist(extendedAddress, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldExFormData$ExtendedAddress$ExtendedAddressRowData[]"});
    assertMethodReturnType(setRows1, "void");

    assertEquals(1, extendedAddress.innerTypes().stream().count(), "inner types count of 'ExtendedAddress'");
    // type ExtendedAddressRowData
    IType extendedAddressRowData = assertTypeExists(extendedAddress, "ExtendedAddressRowData");
    assertHasFlags(extendedAddressRowData, 9);
    assertHasSuperClass(extendedAddressRowData, "formdata.shared.services.process.replace.TableFieldBaseFormData$AddressTable$AddressTableRowData");

    // fields of ExtendedAddressRowData
    assertEquals(3, extendedAddressRowData.fields().stream().count(), "field count of 'ExtendedAddressRowData'");
    IField serialVersionUID4 = assertFieldExist(extendedAddressRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");
    IField state = assertFieldExist(extendedAddressRowData, "state");
    assertHasFlags(state, 25);
    assertFieldType(state, String.class.getName());
    IField m_state = assertFieldExist(extendedAddressRowData, "m_state");
    assertHasFlags(m_state, 2);
    assertFieldType(m_state, String.class.getName());

    assertEquals(2, extendedAddressRowData.methods().stream().count(), "method count of 'ExtendedAddressRowData'");
    IMethod getState = assertMethodExist(extendedAddressRowData, "getState", new String[]{});
    assertMethodReturnType(getState, String.class.getName());
    IMethod setState = assertMethodExist(extendedAddressRowData, "setState", new String[]{String.class.getName()});
    assertMethodReturnType(setState, "void");

    assertEquals(0, extendedAddressRowData.innerTypes().stream().count(), "inner types count of 'ExtendedAddressRowData'");
    // type ExtendedPersonTable
    IType extendedPersonTable = assertTypeExists(tableFieldExFormData, "ExtendedPersonTable");
    assertHasFlags(extendedPersonTable, 9);
    assertHasSuperClass(extendedPersonTable, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable");
    assertAnnotation(extendedPersonTable, "org.eclipse.scout.rt.platform.Replace");

    // fields of ExtendedPersonTable
    assertEquals(1, extendedPersonTable.fields().stream().count(), "field count of 'ExtendedPersonTable'");
    IField serialVersionUID5 = assertFieldExist(extendedPersonTable, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(7, extendedPersonTable.methods().stream().count(), "method count of 'ExtendedPersonTable'");
    IMethod addRow4 = assertMethodExist(extendedPersonTable, "addRow", new String[]{});
    assertMethodReturnType(addRow4, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData");
    assertAnnotation(addRow4, "java.lang.Override");
    IMethod addRow5 = assertMethodExist(extendedPersonTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow5, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData");
    assertAnnotation(addRow5, "java.lang.Override");
    IMethod createRow2 = assertMethodExist(extendedPersonTable, "createRow", new String[]{});
    assertMethodReturnType(createRow2, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData");
    assertAnnotation(createRow2, "java.lang.Override");
    IMethod getRowType2 = assertMethodExist(extendedPersonTable, "getRowType", new String[]{});
    assertMethodReturnType(getRowType2, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType2, "java.lang.Override");
    IMethod getRows2 = assertMethodExist(extendedPersonTable, "getRows", new String[]{});
    assertMethodReturnType(getRows2, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData[]");
    assertAnnotation(getRows2, "java.lang.Override");
    IMethod rowAt2 = assertMethodExist(extendedPersonTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt2, "formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData");
    assertAnnotation(rowAt2, "java.lang.Override");
    IMethod setRows2 = assertMethodExist(extendedPersonTable, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldExFormData$ExtendedPersonTable$ExtendedPersonTableRowData[]"});
    assertMethodReturnType(setRows2, "void");

    assertEquals(1, extendedPersonTable.innerTypes().stream().count(), "inner types count of 'ExtendedPersonTable'");
    // type ExtendedPersonTableRowData
    IType extendedPersonTableRowData = assertTypeExists(extendedPersonTable, "ExtendedPersonTableRowData");
    assertHasFlags(extendedPersonTableRowData, 9);
    assertHasSuperClass(extendedPersonTableRowData, "formdata.shared.services.process.replace.TableFieldBaseFormData$PersonTable$PersonTableRowData");

    // fields of ExtendedPersonTableRowData
    assertEquals(3, extendedPersonTableRowData.fields().stream().count(), "field count of 'ExtendedPersonTableRowData'");
    IField serialVersionUID6 = assertFieldExist(extendedPersonTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");
    IField lastName = assertFieldExist(extendedPersonTableRowData, "lastName");
    assertHasFlags(lastName, 25);
    assertFieldType(lastName, String.class.getName());
    IField m_lastName = assertFieldExist(extendedPersonTableRowData, "m_lastName");
    assertHasFlags(m_lastName, 2);
    assertFieldType(m_lastName, String.class.getName());

    assertEquals(2, extendedPersonTableRowData.methods().stream().count(), "method count of 'ExtendedPersonTableRowData'");
    IMethod getLastName = assertMethodExist(extendedPersonTableRowData, "getLastName", new String[]{});
    assertMethodReturnType(getLastName, String.class.getName());
    IMethod setLastName = assertMethodExist(extendedPersonTableRowData, "setLastName", new String[]{String.class.getName()});
    assertMethodReturnType(setLastName, "void");

    assertEquals(0, extendedPersonTableRowData.innerTypes().stream().count(), "inner types count of 'ExtendedPersonTableRowData'");
    // type NoTableExtended
    IType noTableExtended = assertTypeExists(tableFieldExFormData, "NoTableExtended");
    assertHasFlags(noTableExtended, 9);
    assertHasSuperClass(noTableExtended, "formdata.shared.services.process.replace.TableFieldBaseFormData$NoTable");
    assertAnnotation(noTableExtended, "org.eclipse.scout.rt.platform.Replace");

    // fields of NoTableExtended
    assertEquals(1, noTableExtended.fields().stream().count(), "field count of 'NoTableExtended'");
    IField serialVersionUID7 = assertFieldExist(noTableExtended, "serialVersionUID");
    assertHasFlags(serialVersionUID7, 26);
    assertFieldType(serialVersionUID7, "long");

    assertEquals(7, noTableExtended.methods().stream().count(), "method count of 'NoTableExtended'");
    IMethod addRow6 = assertMethodExist(noTableExtended, "addRow", new String[]{});
    assertMethodReturnType(addRow6, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData");
    assertAnnotation(addRow6, "java.lang.Override");
    IMethod addRow7 = assertMethodExist(noTableExtended, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow7, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData");
    assertAnnotation(addRow7, "java.lang.Override");
    IMethod createRow3 = assertMethodExist(noTableExtended, "createRow", new String[]{});
    assertMethodReturnType(createRow3, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData");
    assertAnnotation(createRow3, "java.lang.Override");
    IMethod getRowType3 = assertMethodExist(noTableExtended, "getRowType", new String[]{});
    assertMethodReturnType(getRowType3, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType3, "java.lang.Override");
    IMethod getRows3 = assertMethodExist(noTableExtended, "getRows", new String[]{});
    assertMethodReturnType(getRows3, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData[]");
    assertAnnotation(getRows3, "java.lang.Override");
    IMethod rowAt3 = assertMethodExist(noTableExtended, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt3, "formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData");
    assertAnnotation(rowAt3, "java.lang.Override");
    IMethod setRows3 = assertMethodExist(noTableExtended, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldExFormData$NoTableExtended$NoTableExtendedRowData[]"});
    assertMethodReturnType(setRows3, "void");

    assertEquals(1, noTableExtended.innerTypes().stream().count(), "inner types count of 'NoTableExtended'");
    // type NoTableExtendedRowData
    IType noTableExtendedRowData = assertTypeExists(noTableExtended, "NoTableExtendedRowData");
    assertHasFlags(noTableExtendedRowData, 9);
    assertHasSuperClass(noTableExtendedRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of NoTableExtendedRowData
    assertEquals(3, noTableExtendedRowData.fields().stream().count(), "field count of 'NoTableExtendedRowData'");
    IField serialVersionUID8 = assertFieldExist(noTableExtendedRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID8, 26);
    assertFieldType(serialVersionUID8, "long");
    IField new_ = assertFieldExist(noTableExtendedRowData, "new_");
    assertHasFlags(new_, 25);
    assertFieldType(new_, String.class.getName());
    IField m_new = assertFieldExist(noTableExtendedRowData, "m_new");
    assertHasFlags(m_new, 2);
    assertFieldType(m_new, String.class.getName());

    assertEquals(2, noTableExtendedRowData.methods().stream().count(), "method count of 'NoTableExtendedRowData'");
    IMethod getNew = assertMethodExist(noTableExtendedRowData, "getNew", new String[]{});
    assertMethodReturnType(getNew, String.class.getName());
    IMethod setNew = assertMethodExist(noTableExtendedRowData, "setNew", new String[]{String.class.getName()});
    assertMethodReturnType(setNew, "void");

    assertEquals(0, noTableExtendedRowData.innerTypes().stream().count(), "inner types count of 'NoTableExtendedRowData'");
    // type TableExtended
    IType tableExtended = assertTypeExists(tableFieldExFormData, "TableExtended");
    assertHasFlags(tableExtended, 9);
    assertHasSuperClass(tableExtended, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table");
    assertAnnotation(tableExtended, "org.eclipse.scout.rt.platform.Replace");

    // fields of TableExtended
    assertEquals(1, tableExtended.fields().stream().count(), "field count of 'TableExtended'");
    IField serialVersionUID9 = assertFieldExist(tableExtended, "serialVersionUID");
    assertHasFlags(serialVersionUID9, 26);
    assertFieldType(serialVersionUID9, "long");

    assertEquals(7, tableExtended.methods().stream().count(), "method count of 'TableExtended'");
    IMethod addRow8 = assertMethodExist(tableExtended, "addRow", new String[]{});
    assertMethodReturnType(addRow8, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData");
    assertAnnotation(addRow8, "java.lang.Override");
    IMethod addRow9 = assertMethodExist(tableExtended, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow9, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData");
    assertAnnotation(addRow9, "java.lang.Override");
    IMethod createRow4 = assertMethodExist(tableExtended, "createRow", new String[]{});
    assertMethodReturnType(createRow4, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData");
    assertAnnotation(createRow4, "java.lang.Override");
    IMethod getRowType4 = assertMethodExist(tableExtended, "getRowType", new String[]{});
    assertMethodReturnType(getRowType4, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType4, "java.lang.Override");
    IMethod getRows4 = assertMethodExist(tableExtended, "getRows", new String[]{});
    assertMethodReturnType(getRows4, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData[]");
    assertAnnotation(getRows4, "java.lang.Override");
    IMethod rowAt4 = assertMethodExist(tableExtended, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt4, "formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData");
    assertAnnotation(rowAt4, "java.lang.Override");
    IMethod setRows4 = assertMethodExist(tableExtended, "setRows", new String[]{"formdata.shared.services.process.replace.TableFieldExFormData$TableExtended$TableExtendedRowData[]"});
    assertMethodReturnType(setRows4, "void");

    assertEquals(1, tableExtended.innerTypes().stream().count(), "inner types count of 'TableExtended'");
    // type TableExtendedRowData
    IType tableExtendedRowData = assertTypeExists(tableExtended, "TableExtendedRowData");
    assertHasFlags(tableExtendedRowData, 9);
    assertHasSuperClass(tableExtendedRowData, "formdata.shared.services.process.replace.TableFieldBaseFormData$Table$TableRowData");

    // fields of TableExtendedRowData
    assertEquals(3, tableExtendedRowData.fields().stream().count(), "field count of 'TableExtendedRowData'");
    IField serialVersionUID10 = assertFieldExist(tableExtendedRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID10, 26);
    assertFieldType(serialVersionUID10, "long");
    IField boolean_ = assertFieldExist(tableExtendedRowData, "boolean_");
    assertHasFlags(boolean_, 25);
    assertFieldType(boolean_, String.class.getName());
    IField m_boolean = assertFieldExist(tableExtendedRowData, "m_boolean");
    assertHasFlags(m_boolean, 2);
    assertFieldType(m_boolean, Boolean.class.getName());

    assertEquals(2, tableExtendedRowData.methods().stream().count(), "method count of 'TableExtendedRowData'");
    IMethod getBoolean = assertMethodExist(tableExtendedRowData, "getBoolean", new String[]{});
    assertMethodReturnType(getBoolean, Boolean.class.getName());
    IMethod setBoolean = assertMethodExist(tableExtendedRowData, "setBoolean", new String[]{Boolean.class.getName()});
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
    IField serialVersionUID = assertFieldExist(baseWithExtendedTableFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, baseWithExtendedTableFormData.methods().stream().count(), "method count of 'BaseWithExtendedTableFormData'");
    IMethod getTableInForm = assertMethodExist(baseWithExtendedTableFormData, "getTableInForm", new String[]{});
    assertMethodReturnType(getTableInForm, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm");

    assertEquals(1, baseWithExtendedTableFormData.innerTypes().stream().count(), "inner types count of 'BaseWithExtendedTableFormData'");
    // type TableInForm
    IType tableInForm = assertTypeExists(baseWithExtendedTableFormData, "TableInForm");
    assertHasFlags(tableInForm, 9);
    assertHasSuperClass(tableInForm, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of TableInForm
    assertEquals(1, tableInForm.fields().stream().count(), "field count of 'TableInForm'");
    IField serialVersionUID1 = assertFieldExist(tableInForm, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, tableInForm.methods().stream().count(), "method count of 'TableInForm'");
    IMethod addRow = assertMethodExist(tableInForm, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(tableInForm, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(tableInForm, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(tableInForm, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(tableInForm, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(tableInForm, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = assertMethodExist(tableInForm, "setRows", new String[]{"formdata.shared.services.BaseWithExtendedTableFormData$TableInForm$TableInFormRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, tableInForm.innerTypes().stream().count(), "inner types count of 'TableInForm'");
    // type TableInFormRowData
    IType tableInFormRowData = assertTypeExists(tableInForm, "TableInFormRowData");
    assertHasFlags(tableInFormRowData, 9);
    assertHasSuperClass(tableInFormRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of TableInFormRowData
    assertEquals(5, tableInFormRowData.fields().stream().count(), "field count of 'TableInFormRowData'");
    IField serialVersionUID2 = assertFieldExist(tableInFormRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    IField colInAbstractTable = assertFieldExist(tableInFormRowData, "colInAbstractTable");
    assertHasFlags(colInAbstractTable, 25);
    assertFieldType(colInAbstractTable, String.class.getName());
    IField colInDesktopForm = assertFieldExist(tableInFormRowData, "colInDesktopForm");
    assertHasFlags(colInDesktopForm, 25);
    assertFieldType(colInDesktopForm, String.class.getName());
    IField m_colInAbstractTable = assertFieldExist(tableInFormRowData, "m_colInAbstractTable");
    assertHasFlags(m_colInAbstractTable, 2);
    assertFieldType(m_colInAbstractTable, String.class.getName());
    IField m_colInDesktopForm = assertFieldExist(tableInFormRowData, "m_colInDesktopForm");
    assertHasFlags(m_colInDesktopForm, 2);
    assertFieldType(m_colInDesktopForm, String.class.getName());

    assertEquals(4, tableInFormRowData.methods().stream().count(), "method count of 'TableInFormRowData'");
    IMethod getColInAbstractTable = assertMethodExist(tableInFormRowData, "getColInAbstractTable", new String[]{});
    assertMethodReturnType(getColInAbstractTable, String.class.getName());
    IMethod setColInAbstractTable = assertMethodExist(tableInFormRowData, "setColInAbstractTable", new String[]{String.class.getName()});
    assertMethodReturnType(setColInAbstractTable, "void");
    IMethod getColInDesktopForm = assertMethodExist(tableInFormRowData, "getColInDesktopForm", new String[]{});
    assertMethodReturnType(getColInDesktopForm, String.class.getName());
    IMethod setColInDesktopForm = assertMethodExist(tableInFormRowData, "setColInDesktopForm", new String[]{String.class.getName()});
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
    IField serialVersionUID = assertFieldExist(childWithExtendedTableFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, childWithExtendedTableFormData.methods().stream().count(), "method count of 'ChildWithExtendedTableFormData'");
    IMethod getChildTable = assertMethodExist(childWithExtendedTableFormData, "getChildTable", new String[]{});
    assertMethodReturnType(getChildTable, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable");

    assertEquals(1, childWithExtendedTableFormData.innerTypes().stream().count(), "inner types count of 'ChildWithExtendedTableFormData'");
    // type ChildTable
    IType childTable = assertTypeExists(childWithExtendedTableFormData, "ChildTable");
    assertHasFlags(childTable, 9);
    assertHasSuperClass(childTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of ChildTable
    assertEquals(1, childTable.fields().stream().count(), "field count of 'ChildTable'");
    IField serialVersionUID1 = assertFieldExist(childTable, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, childTable.methods().stream().count(), "method count of 'ChildTable'");
    IMethod addRow = assertMethodExist(childTable, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(childTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(childTable, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(childTable, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(childTable, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(childTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = assertMethodExist(childTable, "setRows", new String[]{"formdata.shared.services.ChildWithExtendedTableFormData$ChildTable$ChildTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, childTable.innerTypes().stream().count(), "inner types count of 'ChildTable'");
    // type ChildTableRowData
    IType childTableRowData = assertTypeExists(childTable, "ChildTableRowData");
    assertHasFlags(childTableRowData, 9);
    assertHasSuperClass(childTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of ChildTableRowData
    assertEquals(7, childTableRowData.fields().stream().count(), "field count of 'ChildTableRowData'");
    IField serialVersionUID2 = assertFieldExist(childTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    IField col1InChildForm = assertFieldExist(childTableRowData, "col1InChildForm");
    assertHasFlags(col1InChildForm, 25);
    assertFieldType(col1InChildForm, String.class.getName());
    IField colInAbstractTable = assertFieldExist(childTableRowData, "colInAbstractTable");
    assertHasFlags(colInAbstractTable, 25);
    assertFieldType(colInAbstractTable, String.class.getName());
    IField colInDesktopForm = assertFieldExist(childTableRowData, "colInDesktopForm");
    assertHasFlags(colInDesktopForm, 25);
    assertFieldType(colInDesktopForm, String.class.getName());
    IField m_col1InChildForm = assertFieldExist(childTableRowData, "m_col1InChildForm");
    assertHasFlags(m_col1InChildForm, 2);
    assertFieldType(m_col1InChildForm, String.class.getName());
    IField m_colInAbstractTable = assertFieldExist(childTableRowData, "m_colInAbstractTable");
    assertHasFlags(m_colInAbstractTable, 2);
    assertFieldType(m_colInAbstractTable, String.class.getName());
    IField m_colInDesktopForm = assertFieldExist(childTableRowData, "m_colInDesktopForm");
    assertHasFlags(m_colInDesktopForm, 2);
    assertFieldType(m_colInDesktopForm, String.class.getName());

    assertEquals(6, childTableRowData.methods().stream().count(), "method count of 'ChildTableRowData'");
    IMethod getCol1InChildForm = assertMethodExist(childTableRowData, "getCol1InChildForm", new String[]{});
    assertMethodReturnType(getCol1InChildForm, String.class.getName());
    IMethod setCol1InChildForm = assertMethodExist(childTableRowData, "setCol1InChildForm", new String[]{String.class.getName()});
    assertMethodReturnType(setCol1InChildForm, "void");
    IMethod getColInAbstractTable = assertMethodExist(childTableRowData, "getColInAbstractTable", new String[]{});
    assertMethodReturnType(getColInAbstractTable, String.class.getName());
    IMethod setColInAbstractTable = assertMethodExist(childTableRowData, "setColInAbstractTable", new String[]{String.class.getName()});
    assertMethodReturnType(setColInAbstractTable, "void");
    IMethod getColInDesktopForm = assertMethodExist(childTableRowData, "getColInDesktopForm", new String[]{});
    assertMethodReturnType(getColInDesktopForm, String.class.getName());
    IMethod setColInDesktopForm = assertMethodExist(childTableRowData, "setColInDesktopForm", new String[]{String.class.getName()});
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
    IField serialVersionUID = assertFieldExist(columnWithoutTypeFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, columnWithoutTypeFormData.methods().stream().count(), "method count of 'formdata.shared.ui.forms.ColumnWithoutTypeFormData'");
    IMethod getMyTable = assertMethodExist(columnWithoutTypeFormData, "getMyTable", new String[]{});
    assertMethodReturnType(getMyTable, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable");
    assertEquals(0, getMyTable.annotations().stream().count(), "annotation count");

    assertEquals(1, columnWithoutTypeFormData.innerTypes().stream().count(), "inner types count of 'ColumnWithoutTypeFormData'");
    // type MyTable
    IType myTable = assertTypeExists(columnWithoutTypeFormData, "MyTable");
    assertHasFlags(myTable, 9);
    assertHasSuperClass(myTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");
    assertEquals(0, myTable.annotations().stream().count(), "annotation count");

    // fields of MyTable
    assertEquals(1, myTable.fields().stream().count(), "field count of 'formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable'");
    IField serialVersionUID1 = assertFieldExist(myTable, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(7, myTable.methods().stream().count(), "method count of 'formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable'");
    IMethod addRow = assertMethodExist(myTable, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(myTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(myTable, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(myTable, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(myTable, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(myTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = assertMethodExist(myTable, "setRows", new String[]{"formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, myTable.innerTypes().stream().count(), "inner types count of 'MyTable'");
    // type MyTableRowData
    IType myTableRowData = assertTypeExists(myTable, "MyTableRowData");
    assertHasFlags(myTableRowData, 9);
    assertHasSuperClass(myTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");
    assertEquals(0, myTableRowData.annotations().stream().count(), "annotation count");

    // fields of MyTableRowData
    assertEquals(3, myTableRowData.fields().stream().count(), "field count of 'formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData'");
    IField serialVersionUID2 = assertFieldExist(myTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");
    IField my = assertFieldExist(myTableRowData, "my");
    assertHasFlags(my, 25);
    assertFieldType(my, "java.lang.String");
    assertEquals(0, my.annotations().stream().count(), "annotation count");
    IField m_my = assertFieldExist(myTableRowData, "m_my");
    assertHasFlags(m_my, 2);
    assertFieldType(m_my, "java.lang.Object");
    assertEquals(0, m_my.annotations().stream().count(), "annotation count");

    assertEquals(2, myTableRowData.methods().stream().count(), "method count of 'formdata.shared.ui.forms.ColumnWithoutTypeFormData$MyTable$MyTableRowData'");
    IMethod getMy = assertMethodExist(myTableRowData, "getMy", new String[]{});
    assertMethodReturnType(getMy, "java.lang.Object");
    assertEquals(0, getMy.annotations().stream().count(), "annotation count");
    IMethod setMy = assertMethodExist(myTableRowData, "setMy", new String[]{"java.lang.Object"});
    assertMethodReturnType(setMy, "void");
    assertEquals(0, setMy.annotations().stream().count(), "annotation count");

    assertEquals(0, myTableRowData.innerTypes().stream().count(), "inner types count of 'MyTableRowData'");
  }

}
