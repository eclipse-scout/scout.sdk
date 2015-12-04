/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link TableFieldBeanTest}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 19.08.2013
 */
public class TableFieldBeanTest {

  public static final String AbstractAddressTableField = "formdata.client.ui.template.formfield.AbstractAddressTableField";
  public static final String TableFieldBaseForm = "formdata.client.ui.forms.replace.TableFieldBaseForm";
  public static final String TableFieldExForm = "formdata.client.ui.forms.replace.TableFieldExForm";

  public static final String BaseWithExtendedTableForm = "formdata.client.ui.forms.BaseWithExtendedTableForm";
  public static final String ChildWithExtendedTableForm = "formdata.client.ui.forms.ChildWithExtendedTableForm";

  @Test
  public void testFormData() {
    testAbstractTableField();
    testTableFieldBaseFormData();
    testTableFieldExFormData();
    testBaseWithExtendedTableFormData();
    testChildWithExtendedTableFormData();
  }

  private static void testAbstractTableField() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(AbstractAddressTableField);
    testApiOfAbstractAddressTableFieldData(dto);
  }

  private static void testTableFieldBaseFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(TableFieldBaseForm);
    testApiOfTableFieldBaseFormData(dto);
  }

  private static void testTableFieldExFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(TableFieldExForm);
    testApiOfTableFieldExFormData(dto);
  }

  private static void testBaseWithExtendedTableFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(BaseWithExtendedTableForm);
    testApiOfBaseWithExtendedTableFormData(dto);
  }

  private static void testChildWithExtendedTableFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(ChildWithExtendedTableForm);
    testApiOfChildWithExtendedTableFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractAddressTableFieldData(IType abstractAddressTableFieldData) {
    // type AbstractAddressTableFieldData
    SdkAssert.assertHasFlags(abstractAddressTableFieldData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractAddressTableFieldData, "QAbstractTableFieldBeanData;");

    // fields of AbstractAddressTableFieldData
    Assert.assertEquals("field count of 'AbstractAddressTableFieldData'", 1, abstractAddressTableFieldData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractAddressTableFieldData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'AbstractAddressTableFieldData'", 8, abstractAddressTableFieldData.methods().list().size());
    IMethod abstractAddressTableFieldData1 = SdkAssert.assertMethodExist(abstractAddressTableFieldData, "AbstractAddressTableFieldData", new String[]{});
    Assert.assertTrue(abstractAddressTableFieldData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractAddressTableFieldData1, null);
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

    Assert.assertEquals("inner types count of 'AbstractAddressTableFieldData'", 1, abstractAddressTableFieldData.innerTypes().list().size());
    // type AbstractAddressTableRowData
    IType abstractAddressTableRowData = SdkAssert.assertTypeExists(abstractAddressTableFieldData, "AbstractAddressTableRowData");
    SdkAssert.assertHasFlags(abstractAddressTableRowData, 1033);
    SdkAssert.assertHasSuperTypeSignature(abstractAddressTableRowData, "QAbstractTableRowData;");

    // fields of AbstractAddressTableRowData
    Assert.assertEquals("field count of 'AbstractAddressTableRowData'", 7, abstractAddressTableRowData.fields().list().size());
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

    Assert.assertEquals("method count of 'AbstractAddressTableRowData'", 7, abstractAddressTableRowData.methods().list().size());
    IMethod abstractAddressTableRowData1 = SdkAssert.assertMethodExist(abstractAddressTableRowData, "AbstractAddressTableRowData", new String[]{});
    Assert.assertTrue(abstractAddressTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractAddressTableRowData1, null);
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

    Assert.assertEquals("inner types count of 'AbstractAddressTableRowData'", 0, abstractAddressTableRowData.innerTypes().list().size());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfTableFieldBaseFormData(IType tableFieldBaseFormData) {
    // type TableFieldBaseFormData
    SdkAssert.assertHasFlags(tableFieldBaseFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(tableFieldBaseFormData, "QAbstractFormData;");

    // fields of TableFieldBaseFormData
    Assert.assertEquals("field count of 'TableFieldBaseFormData'", 1, tableFieldBaseFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(tableFieldBaseFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'TableFieldBaseFormData'", 6, tableFieldBaseFormData.methods().list().size());
    IMethod tableFieldBaseFormData1 = SdkAssert.assertMethodExist(tableFieldBaseFormData, "TableFieldBaseFormData", new String[]{});
    Assert.assertTrue(tableFieldBaseFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableFieldBaseFormData1, null);
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

    Assert.assertEquals("inner types count of 'TableFieldBaseFormData'", 5, tableFieldBaseFormData.innerTypes().list().size());
    // type AddressTable
    IType addressTable = SdkAssert.assertTypeExists(tableFieldBaseFormData, "AddressTable");
    SdkAssert.assertHasFlags(addressTable, 9);
    SdkAssert.assertHasSuperTypeSignature(addressTable, "QAbstractAddressTableFieldData;");

    // fields of AddressTable
    Assert.assertEquals("field count of 'AddressTable'", 1, addressTable.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(addressTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'AddressTable'", 8, addressTable.methods().list().size());
    IMethod addressTable1 = SdkAssert.assertMethodExist(addressTable, "AddressTable", new String[]{});
    Assert.assertTrue(addressTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(addressTable1, null);
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

    Assert.assertEquals("inner types count of 'AddressTable'", 1, addressTable.innerTypes().list().size());
    // type AddressTableRowData
    IType addressTableRowData = SdkAssert.assertTypeExists(addressTable, "AddressTableRowData");
    SdkAssert.assertHasFlags(addressTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(addressTableRowData, "QAbstractAddressTableRowData;");

    // fields of AddressTableRowData
    Assert.assertEquals("field count of 'AddressTableRowData'", 3, addressTableRowData.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(addressTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");
    IField city = SdkAssert.assertFieldExist(addressTableRowData, "city");
    SdkAssert.assertHasFlags(city, 25);
    SdkAssert.assertFieldSignature(city, "QString;");
    IField m_city = SdkAssert.assertFieldExist(addressTableRowData, "m_city");
    SdkAssert.assertHasFlags(m_city, 2);
    SdkAssert.assertFieldSignature(m_city, "QString;");

    Assert.assertEquals("method count of 'AddressTableRowData'", 3, addressTableRowData.methods().list().size());
    IMethod addressTableRowData1 = SdkAssert.assertMethodExist(addressTableRowData, "AddressTableRowData", new String[]{});
    Assert.assertTrue(addressTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(addressTableRowData1, null);
    IMethod getCity = SdkAssert.assertMethodExist(addressTableRowData, "getCity", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getCity, "QString;");
    IMethod setCity = SdkAssert.assertMethodExist(addressTableRowData, "setCity", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setCity, "V");

    Assert.assertEquals("inner types count of 'AddressTableRowData'", 0, addressTableRowData.innerTypes().list().size());
    // type EmptyTable
    IType emptyTable = SdkAssert.assertTypeExists(tableFieldBaseFormData, "EmptyTable");
    SdkAssert.assertHasFlags(emptyTable, 9);
    SdkAssert.assertHasSuperTypeSignature(emptyTable, "QAbstractTableFieldBeanData;");

    // fields of EmptyTable
    Assert.assertEquals("field count of 'EmptyTable'", 1, emptyTable.fields().list().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(emptyTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'EmptyTable'", 8, emptyTable.methods().list().size());
    IMethod emptyTable1 = SdkAssert.assertMethodExist(emptyTable, "EmptyTable", new String[]{});
    Assert.assertTrue(emptyTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(emptyTable1, null);
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

    Assert.assertEquals("inner types count of 'EmptyTable'", 1, emptyTable.innerTypes().list().size());
    // type EmptyTableRowData
    IType emptyTableRowData = SdkAssert.assertTypeExists(emptyTable, "EmptyTableRowData");
    SdkAssert.assertHasFlags(emptyTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(emptyTableRowData, "QAbstractTableRowData;");

    // fields of EmptyTableRowData
    Assert.assertEquals("field count of 'EmptyTableRowData'", 1, emptyTableRowData.fields().list().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(emptyTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");

    Assert.assertEquals("method count of 'EmptyTableRowData'", 1, emptyTableRowData.methods().list().size());
    IMethod emptyTableRowData1 = SdkAssert.assertMethodExist(emptyTableRowData, "EmptyTableRowData", new String[]{});
    Assert.assertTrue(emptyTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(emptyTableRowData1, null);

    Assert.assertEquals("inner types count of 'EmptyTableRowData'", 0, emptyTableRowData.innerTypes().list().size());
    // type NoTable
    IType noTable = SdkAssert.assertTypeExists(tableFieldBaseFormData, "NoTable");
    SdkAssert.assertHasFlags(noTable, 9);
    SdkAssert.assertHasSuperTypeSignature(noTable, "QAbstractTableFieldBeanData;");

    // fields of NoTable
    Assert.assertEquals("field count of 'NoTable'", 1, noTable.fields().list().size());
    IField serialVersionUID5 = SdkAssert.assertFieldExist(noTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    Assert.assertEquals("method count of 'NoTable'", 3, noTable.methods().list().size());
    IMethod noTable1 = SdkAssert.assertMethodExist(noTable, "NoTable", new String[]{});
    Assert.assertTrue(noTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(noTable1, null);
    IMethod createRow2 = SdkAssert.assertMethodExist(noTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow2, "QAbstractTableRowData;");
    SdkAssert.assertAnnotation(createRow2, "java.lang.Override");
    IMethod getRowType2 = SdkAssert.assertMethodExist(noTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType2, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType2, "java.lang.Override");

    Assert.assertEquals("inner types count of 'NoTable'", 0, noTable.innerTypes().list().size());
    // type PersonTable
    IType personTable = SdkAssert.assertTypeExists(tableFieldBaseFormData, "PersonTable");
    SdkAssert.assertHasFlags(personTable, 9);
    SdkAssert.assertHasSuperTypeSignature(personTable, "QAbstractPersonTableFieldData;");

    // fields of PersonTable
    Assert.assertEquals("field count of 'PersonTable'", 1, personTable.fields().list().size());
    IField serialVersionUID6 = SdkAssert.assertFieldExist(personTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");

    Assert.assertEquals("method count of 'PersonTable'", 8, personTable.methods().list().size());
    IMethod personTable1 = SdkAssert.assertMethodExist(personTable, "PersonTable", new String[]{});
    Assert.assertTrue(personTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(personTable1, null);
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

    Assert.assertEquals("inner types count of 'PersonTable'", 1, personTable.innerTypes().list().size());
    // type PersonTableRowData
    IType personTableRowData = SdkAssert.assertTypeExists(personTable, "PersonTableRowData");
    SdkAssert.assertHasFlags(personTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(personTableRowData, "QAbstractPersonTableRowData;");

    // fields of PersonTableRowData
    Assert.assertEquals("field count of 'PersonTableRowData'", 1, personTableRowData.fields().list().size());
    IField serialVersionUID7 = SdkAssert.assertFieldExist(personTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    Assert.assertEquals("method count of 'PersonTableRowData'", 1, personTableRowData.methods().list().size());
    IMethod personTableRowData1 = SdkAssert.assertMethodExist(personTableRowData, "PersonTableRowData", new String[]{});
    Assert.assertTrue(personTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(personTableRowData1, null);

    Assert.assertEquals("inner types count of 'PersonTableRowData'", 0, personTableRowData.innerTypes().list().size());
    // type Table
    IType table = SdkAssert.assertTypeExists(tableFieldBaseFormData, "Table");
    SdkAssert.assertHasFlags(table, 9);
    SdkAssert.assertHasSuperTypeSignature(table, "QAbstractTableFieldBeanData;");

    // fields of Table
    Assert.assertEquals("field count of 'Table'", 1, table.fields().list().size());
    IField serialVersionUID8 = SdkAssert.assertFieldExist(table, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");

    Assert.assertEquals("method count of 'Table'", 8, table.methods().list().size());
    IMethod table1 = SdkAssert.assertMethodExist(table, "Table", new String[]{});
    Assert.assertTrue(table1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(table1, null);
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

    Assert.assertEquals("inner types count of 'Table'", 1, table.innerTypes().list().size());
    // type TableRowData
    IType tableRowData = SdkAssert.assertTypeExists(table, "TableRowData");
    SdkAssert.assertHasFlags(tableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(tableRowData, "QAbstractTableRowData;");

    // fields of TableRowData
    Assert.assertEquals("field count of 'TableRowData'", 5, tableRowData.fields().list().size());
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

    Assert.assertEquals("method count of 'TableRowData'", 5, tableRowData.methods().list().size());
    IMethod tableRowData1 = SdkAssert.assertMethodExist(tableRowData, "TableRowData", new String[]{});
    Assert.assertTrue(tableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableRowData1, null);
    IMethod getFirst = SdkAssert.assertMethodExist(tableRowData, "getFirst", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirst, "QString;");
    IMethod setFirst = SdkAssert.assertMethodExist(tableRowData, "setFirst", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setFirst, "V");
    IMethod getSecond = SdkAssert.assertMethodExist(tableRowData, "getSecond", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSecond, "QString;");
    IMethod setSecond = SdkAssert.assertMethodExist(tableRowData, "setSecond", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setSecond, "V");

    Assert.assertEquals("inner types count of 'TableRowData'", 0, tableRowData.innerTypes().list().size());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfTableFieldExFormData(IType tableFieldExFormData) {
    // type TableFieldExFormData
    SdkAssert.assertHasFlags(tableFieldExFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(tableFieldExFormData, "QTableFieldBaseFormData;");

    // fields of TableFieldExFormData
    Assert.assertEquals("field count of 'TableFieldExFormData'", 1, tableFieldExFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(tableFieldExFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'TableFieldExFormData'", 6, tableFieldExFormData.methods().list().size());
    IMethod tableFieldExFormData1 = SdkAssert.assertMethodExist(tableFieldExFormData, "TableFieldExFormData", new String[]{});
    Assert.assertTrue(tableFieldExFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableFieldExFormData1, null);
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

    Assert.assertEquals("inner types count of 'TableFieldExFormData'", 5, tableFieldExFormData.innerTypes().list().size());
    // type EmptyTableExtended
    IType emptyTableExtended = SdkAssert.assertTypeExists(tableFieldExFormData, "EmptyTableExtended");
    SdkAssert.assertHasFlags(emptyTableExtended, 9);
    SdkAssert.assertHasSuperTypeSignature(emptyTableExtended, "QEmptyTable;");
    SdkAssert.assertAnnotation(emptyTableExtended, "org.eclipse.scout.rt.platform.Replace");

    // fields of EmptyTableExtended
    Assert.assertEquals("field count of 'EmptyTableExtended'", 1, emptyTableExtended.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(emptyTableExtended, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'EmptyTableExtended'", 8, emptyTableExtended.methods().list().size());
    IMethod emptyTableExtended1 = SdkAssert.assertMethodExist(emptyTableExtended, "EmptyTableExtended", new String[]{});
    Assert.assertTrue(emptyTableExtended1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(emptyTableExtended1, null);
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

    Assert.assertEquals("inner types count of 'EmptyTableExtended'", 1, emptyTableExtended.innerTypes().list().size());
    // type EmptyTableExtendedRowData
    IType emptyTableExtendedRowData = SdkAssert.assertTypeExists(emptyTableExtended, "EmptyTableExtendedRowData");
    SdkAssert.assertHasFlags(emptyTableExtendedRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(emptyTableExtendedRowData, "QEmptyTableRowData;");

    // fields of EmptyTableExtendedRowData
    Assert.assertEquals("field count of 'EmptyTableExtendedRowData'", 3, emptyTableExtendedRowData.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(emptyTableExtendedRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");
    IField single = SdkAssert.assertFieldExist(emptyTableExtendedRowData, "single");
    SdkAssert.assertHasFlags(single, 25);
    SdkAssert.assertFieldSignature(single, "QString;");
    IField m_single = SdkAssert.assertFieldExist(emptyTableExtendedRowData, "m_single");
    SdkAssert.assertHasFlags(m_single, 2);
    SdkAssert.assertFieldSignature(m_single, "QString;");

    Assert.assertEquals("method count of 'EmptyTableExtendedRowData'", 3, emptyTableExtendedRowData.methods().list().size());
    IMethod emptyTableExtendedRowData1 = SdkAssert.assertMethodExist(emptyTableExtendedRowData, "EmptyTableExtendedRowData", new String[]{});
    Assert.assertTrue(emptyTableExtendedRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(emptyTableExtendedRowData1, null);
    IMethod getSingle = SdkAssert.assertMethodExist(emptyTableExtendedRowData, "getSingle", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSingle, "QString;");
    IMethod setSingle = SdkAssert.assertMethodExist(emptyTableExtendedRowData, "setSingle", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setSingle, "V");

    Assert.assertEquals("inner types count of 'EmptyTableExtendedRowData'", 0, emptyTableExtendedRowData.innerTypes().list().size());
    // type ExtendedAddress
    IType extendedAddress = SdkAssert.assertTypeExists(tableFieldExFormData, "ExtendedAddress");
    SdkAssert.assertHasFlags(extendedAddress, 9);
    SdkAssert.assertHasSuperTypeSignature(extendedAddress, "QAddressTable;");
    SdkAssert.assertAnnotation(extendedAddress, "org.eclipse.scout.rt.platform.Replace");

    // fields of ExtendedAddress
    Assert.assertEquals("field count of 'ExtendedAddress'", 1, extendedAddress.fields().list().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(extendedAddress, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'ExtendedAddress'", 8, extendedAddress.methods().list().size());
    IMethod extendedAddress1 = SdkAssert.assertMethodExist(extendedAddress, "ExtendedAddress", new String[]{});
    Assert.assertTrue(extendedAddress1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedAddress1, null);
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

    Assert.assertEquals("inner types count of 'ExtendedAddress'", 1, extendedAddress.innerTypes().list().size());
    // type ExtendedAddressRowData
    IType extendedAddressRowData = SdkAssert.assertTypeExists(extendedAddress, "ExtendedAddressRowData");
    SdkAssert.assertHasFlags(extendedAddressRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(extendedAddressRowData, "QAddressTableRowData;");

    // fields of ExtendedAddressRowData
    Assert.assertEquals("field count of 'ExtendedAddressRowData'", 3, extendedAddressRowData.fields().list().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(extendedAddressRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");
    IField state = SdkAssert.assertFieldExist(extendedAddressRowData, "state");
    SdkAssert.assertHasFlags(state, 25);
    SdkAssert.assertFieldSignature(state, "QString;");
    IField m_state = SdkAssert.assertFieldExist(extendedAddressRowData, "m_state");
    SdkAssert.assertHasFlags(m_state, 2);
    SdkAssert.assertFieldSignature(m_state, "QString;");

    Assert.assertEquals("method count of 'ExtendedAddressRowData'", 3, extendedAddressRowData.methods().list().size());
    IMethod extendedAddressRowData1 = SdkAssert.assertMethodExist(extendedAddressRowData, "ExtendedAddressRowData", new String[]{});
    Assert.assertTrue(extendedAddressRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedAddressRowData1, null);
    IMethod getState = SdkAssert.assertMethodExist(extendedAddressRowData, "getState", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getState, "QString;");
    IMethod setState = SdkAssert.assertMethodExist(extendedAddressRowData, "setState", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setState, "V");

    Assert.assertEquals("inner types count of 'ExtendedAddressRowData'", 0, extendedAddressRowData.innerTypes().list().size());
    // type ExtendedPersonTable
    IType extendedPersonTable = SdkAssert.assertTypeExists(tableFieldExFormData, "ExtendedPersonTable");
    SdkAssert.assertHasFlags(extendedPersonTable, 9);
    SdkAssert.assertHasSuperTypeSignature(extendedPersonTable, "QPersonTable;");
    SdkAssert.assertAnnotation(extendedPersonTable, "org.eclipse.scout.rt.platform.Replace");

    // fields of ExtendedPersonTable
    Assert.assertEquals("field count of 'ExtendedPersonTable'", 1, extendedPersonTable.fields().list().size());
    IField serialVersionUID5 = SdkAssert.assertFieldExist(extendedPersonTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    Assert.assertEquals("method count of 'ExtendedPersonTable'", 8, extendedPersonTable.methods().list().size());
    IMethod extendedPersonTable1 = SdkAssert.assertMethodExist(extendedPersonTable, "ExtendedPersonTable", new String[]{});
    Assert.assertTrue(extendedPersonTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedPersonTable1, null);
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

    Assert.assertEquals("inner types count of 'ExtendedPersonTable'", 1, extendedPersonTable.innerTypes().list().size());
    // type ExtendedPersonTableRowData
    IType extendedPersonTableRowData = SdkAssert.assertTypeExists(extendedPersonTable, "ExtendedPersonTableRowData");
    SdkAssert.assertHasFlags(extendedPersonTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(extendedPersonTableRowData, "QPersonTableRowData;");

    // fields of ExtendedPersonTableRowData
    Assert.assertEquals("field count of 'ExtendedPersonTableRowData'", 3, extendedPersonTableRowData.fields().list().size());
    IField serialVersionUID6 = SdkAssert.assertFieldExist(extendedPersonTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");
    IField lastName = SdkAssert.assertFieldExist(extendedPersonTableRowData, "lastName");
    SdkAssert.assertHasFlags(lastName, 25);
    SdkAssert.assertFieldSignature(lastName, "QString;");
    IField m_lastName = SdkAssert.assertFieldExist(extendedPersonTableRowData, "m_lastName");
    SdkAssert.assertHasFlags(m_lastName, 2);
    SdkAssert.assertFieldSignature(m_lastName, "QString;");

    Assert.assertEquals("method count of 'ExtendedPersonTableRowData'", 3, extendedPersonTableRowData.methods().list().size());
    IMethod extendedPersonTableRowData1 = SdkAssert.assertMethodExist(extendedPersonTableRowData, "ExtendedPersonTableRowData", new String[]{});
    Assert.assertTrue(extendedPersonTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(extendedPersonTableRowData1, null);
    IMethod getLastName = SdkAssert.assertMethodExist(extendedPersonTableRowData, "getLastName", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getLastName, "QString;");
    IMethod setLastName = SdkAssert.assertMethodExist(extendedPersonTableRowData, "setLastName", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setLastName, "V");

    Assert.assertEquals("inner types count of 'ExtendedPersonTableRowData'", 0, extendedPersonTableRowData.innerTypes().list().size());
    // type NoTableExtended
    IType noTableExtended = SdkAssert.assertTypeExists(tableFieldExFormData, "NoTableExtended");
    SdkAssert.assertHasFlags(noTableExtended, 9);
    SdkAssert.assertHasSuperTypeSignature(noTableExtended, "QNoTable;");
    SdkAssert.assertAnnotation(noTableExtended, "org.eclipse.scout.rt.platform.Replace");

    // fields of NoTableExtended
    Assert.assertEquals("field count of 'NoTableExtended'", 1, noTableExtended.fields().list().size());
    IField serialVersionUID7 = SdkAssert.assertFieldExist(noTableExtended, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    Assert.assertEquals("method count of 'NoTableExtended'", 8, noTableExtended.methods().list().size());
    IMethod noTableExtended1 = SdkAssert.assertMethodExist(noTableExtended, "NoTableExtended", new String[]{});
    Assert.assertTrue(noTableExtended1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(noTableExtended1, null);
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

    Assert.assertEquals("inner types count of 'NoTableExtended'", 1, noTableExtended.innerTypes().list().size());
    // type NoTableExtendedRowData
    IType noTableExtendedRowData = SdkAssert.assertTypeExists(noTableExtended, "NoTableExtendedRowData");
    SdkAssert.assertHasFlags(noTableExtendedRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(noTableExtendedRowData, "QAbstractTableRowData;");

    // fields of NoTableExtendedRowData
    Assert.assertEquals("field count of 'NoTableExtendedRowData'", 3, noTableExtendedRowData.fields().list().size());
    IField serialVersionUID8 = SdkAssert.assertFieldExist(noTableExtendedRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");
    IField new_ = SdkAssert.assertFieldExist(noTableExtendedRowData, "new_");
    SdkAssert.assertHasFlags(new_, 25);
    SdkAssert.assertFieldSignature(new_, "QString;");
    IField m_new = SdkAssert.assertFieldExist(noTableExtendedRowData, "m_new");
    SdkAssert.assertHasFlags(m_new, 2);
    SdkAssert.assertFieldSignature(m_new, "QString;");

    Assert.assertEquals("method count of 'NoTableExtendedRowData'", 3, noTableExtendedRowData.methods().list().size());
    IMethod noTableExtendedRowData1 = SdkAssert.assertMethodExist(noTableExtendedRowData, "NoTableExtendedRowData", new String[]{});
    Assert.assertTrue(noTableExtendedRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(noTableExtendedRowData1, null);
    IMethod getNew = SdkAssert.assertMethodExist(noTableExtendedRowData, "getNew", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getNew, "QString;");
    IMethod setNew = SdkAssert.assertMethodExist(noTableExtendedRowData, "setNew", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setNew, "V");

    Assert.assertEquals("inner types count of 'NoTableExtendedRowData'", 0, noTableExtendedRowData.innerTypes().list().size());
    // type TableExtended
    IType tableExtended = SdkAssert.assertTypeExists(tableFieldExFormData, "TableExtended");
    SdkAssert.assertHasFlags(tableExtended, 9);
    SdkAssert.assertHasSuperTypeSignature(tableExtended, "QTable;");
    SdkAssert.assertAnnotation(tableExtended, "org.eclipse.scout.rt.platform.Replace");

    // fields of TableExtended
    Assert.assertEquals("field count of 'TableExtended'", 1, tableExtended.fields().list().size());
    IField serialVersionUID9 = SdkAssert.assertFieldExist(tableExtended, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID9, 26);
    SdkAssert.assertFieldSignature(serialVersionUID9, "J");

    Assert.assertEquals("method count of 'TableExtended'", 8, tableExtended.methods().list().size());
    IMethod tableExtended1 = SdkAssert.assertMethodExist(tableExtended, "TableExtended", new String[]{});
    Assert.assertTrue(tableExtended1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableExtended1, null);
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

    Assert.assertEquals("inner types count of 'TableExtended'", 1, tableExtended.innerTypes().list().size());
    // type TableExtendedRowData
    IType tableExtendedRowData = SdkAssert.assertTypeExists(tableExtended, "TableExtendedRowData");
    SdkAssert.assertHasFlags(tableExtendedRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(tableExtendedRowData, "QTableRowData;");

    // fields of TableExtendedRowData
    Assert.assertEquals("field count of 'TableExtendedRowData'", 3, tableExtendedRowData.fields().list().size());
    IField serialVersionUID10 = SdkAssert.assertFieldExist(tableExtendedRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID10, 26);
    SdkAssert.assertFieldSignature(serialVersionUID10, "J");
    IField boolean_ = SdkAssert.assertFieldExist(tableExtendedRowData, "boolean_");
    SdkAssert.assertHasFlags(boolean_, 25);
    SdkAssert.assertFieldSignature(boolean_, "QString;");
    IField m_boolean = SdkAssert.assertFieldExist(tableExtendedRowData, "m_boolean");
    SdkAssert.assertHasFlags(m_boolean, 2);
    SdkAssert.assertFieldSignature(m_boolean, "QBoolean;");

    Assert.assertEquals("method count of 'TableExtendedRowData'", 3, tableExtendedRowData.methods().list().size());
    IMethod tableExtendedRowData1 = SdkAssert.assertMethodExist(tableExtendedRowData, "TableExtendedRowData", new String[]{});
    Assert.assertTrue(tableExtendedRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableExtendedRowData1, null);
    IMethod getBoolean = SdkAssert.assertMethodExist(tableExtendedRowData, "getBoolean", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getBoolean, "QBoolean;");
    IMethod setBoolean = SdkAssert.assertMethodExist(tableExtendedRowData, "setBoolean", new String[]{"QBoolean;"});
    SdkAssert.assertMethodReturnTypeSignature(setBoolean, "V");

    Assert.assertEquals("inner types count of 'TableExtendedRowData'", 0, tableExtendedRowData.innerTypes().list().size());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfBaseWithExtendedTableFormData(IType baseWithExtendedTableFormData) {
    // type BaseWithExtendedTableFormData
    SdkAssert.assertHasFlags(baseWithExtendedTableFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(baseWithExtendedTableFormData, "QAbstractFormData;");

    // fields of BaseWithExtendedTableFormData
    Assert.assertEquals("field count of 'BaseWithExtendedTableFormData'", 1, baseWithExtendedTableFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(baseWithExtendedTableFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'BaseWithExtendedTableFormData'", 2, baseWithExtendedTableFormData.methods().list().size());
    IMethod baseWithExtendedTableFormData1 = SdkAssert.assertMethodExist(baseWithExtendedTableFormData, "BaseWithExtendedTableFormData", new String[]{});
    Assert.assertTrue(baseWithExtendedTableFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(baseWithExtendedTableFormData1, null);
    IMethod getTableInForm = SdkAssert.assertMethodExist(baseWithExtendedTableFormData, "getTableInForm", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTableInForm, "QTableInForm;");

    Assert.assertEquals("inner types count of 'BaseWithExtendedTableFormData'", 1, baseWithExtendedTableFormData.innerTypes().list().size());
    // type TableInForm
    IType tableInForm = SdkAssert.assertTypeExists(baseWithExtendedTableFormData, "TableInForm");
    SdkAssert.assertHasFlags(tableInForm, 9);
    SdkAssert.assertHasSuperTypeSignature(tableInForm, "QAbstractTableFieldBeanData;");

    // fields of TableInForm
    Assert.assertEquals("field count of 'TableInForm'", 1, tableInForm.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(tableInForm, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'TableInForm'", 8, tableInForm.methods().list().size());
    IMethod tableInForm1 = SdkAssert.assertMethodExist(tableInForm, "TableInForm", new String[]{});
    Assert.assertTrue(tableInForm1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableInForm1, null);
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

    Assert.assertEquals("inner types count of 'TableInForm'", 1, tableInForm.innerTypes().list().size());
    // type TableInFormRowData
    IType tableInFormRowData = SdkAssert.assertTypeExists(tableInForm, "TableInFormRowData");
    SdkAssert.assertHasFlags(tableInFormRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(tableInFormRowData, "QAbstractTableRowData;");

    // fields of TableInFormRowData
    Assert.assertEquals("field count of 'TableInFormRowData'", 5, tableInFormRowData.fields().list().size());
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

    Assert.assertEquals("method count of 'TableInFormRowData'", 5, tableInFormRowData.methods().list().size());
    IMethod tableInFormRowData1 = SdkAssert.assertMethodExist(tableInFormRowData, "TableInFormRowData", new String[]{});
    Assert.assertTrue(tableInFormRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableInFormRowData1, null);
    IMethod getColInAbstractTable = SdkAssert.assertMethodExist(tableInFormRowData, "getColInAbstractTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getColInAbstractTable, "QString;");
    IMethod setColInAbstractTable = SdkAssert.assertMethodExist(tableInFormRowData, "setColInAbstractTable", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setColInAbstractTable, "V");
    IMethod getColInDesktopForm = SdkAssert.assertMethodExist(tableInFormRowData, "getColInDesktopForm", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getColInDesktopForm, "QString;");
    IMethod setColInDesktopForm = SdkAssert.assertMethodExist(tableInFormRowData, "setColInDesktopForm", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setColInDesktopForm, "V");

    Assert.assertEquals("inner types count of 'TableInFormRowData'", 0, tableInFormRowData.innerTypes().list().size());
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfChildWithExtendedTableFormData(IType childWithExtendedTableFormData) {
    // type ChildWithExtendedTableFormData
    SdkAssert.assertHasFlags(childWithExtendedTableFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(childWithExtendedTableFormData, "QBaseWithExtendedTableFormData;");

    // fields of ChildWithExtendedTableFormData
    Assert.assertEquals("field count of 'ChildWithExtendedTableFormData'", 1, childWithExtendedTableFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(childWithExtendedTableFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'ChildWithExtendedTableFormData'", 2, childWithExtendedTableFormData.methods().list().size());
    IMethod childWithExtendedTableFormData1 = SdkAssert.assertMethodExist(childWithExtendedTableFormData, "ChildWithExtendedTableFormData", new String[]{});
    Assert.assertTrue(childWithExtendedTableFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(childWithExtendedTableFormData1, null);
    IMethod getChildTable = SdkAssert.assertMethodExist(childWithExtendedTableFormData, "getChildTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getChildTable, "QChildTable;");

    Assert.assertEquals("inner types count of 'ChildWithExtendedTableFormData'", 1, childWithExtendedTableFormData.innerTypes().list().size());
    // type ChildTable
    IType childTable = SdkAssert.assertTypeExists(childWithExtendedTableFormData, "ChildTable");
    SdkAssert.assertHasFlags(childTable, 9);
    SdkAssert.assertHasSuperTypeSignature(childTable, "QAbstractTableFieldBeanData;");

    // fields of ChildTable
    Assert.assertEquals("field count of 'ChildTable'", 1, childTable.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(childTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'ChildTable'", 8, childTable.methods().list().size());
    IMethod childTable1 = SdkAssert.assertMethodExist(childTable, "ChildTable", new String[]{});
    Assert.assertTrue(childTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(childTable1, null);
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

    Assert.assertEquals("inner types count of 'ChildTable'", 1, childTable.innerTypes().list().size());
    // type ChildTableRowData
    IType childTableRowData = SdkAssert.assertTypeExists(childTable, "ChildTableRowData");
    SdkAssert.assertHasFlags(childTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(childTableRowData, "QAbstractTableRowData;");

    // fields of ChildTableRowData
    Assert.assertEquals("field count of 'ChildTableRowData'", 7, childTableRowData.fields().list().size());
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

    Assert.assertEquals("method count of 'ChildTableRowData'", 7, childTableRowData.methods().list().size());
    IMethod childTableRowData1 = SdkAssert.assertMethodExist(childTableRowData, "ChildTableRowData", new String[]{});
    Assert.assertTrue(childTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(childTableRowData1, null);
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

    Assert.assertEquals("inner types count of 'ChildTableRowData'", 0, childTableRowData.innerTypes().list().size());
  }
}
