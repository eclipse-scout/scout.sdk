/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataDtoUpdateOperation;
import org.junit.Assert;
import org.junit.Test;

public class TableFieldFormTest extends AbstractSdkTestWithFormDataProject {

  @Test
  public void testCreateFormData() throws Exception {
    String formName = "TableFieldForm";
    IType form = TypeUtility.getType("formdata.client.ui.forms." + formName);
    Assert.assertTrue(TypeUtility.exists(form));

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(form);

    executeBuildAssertNoCompileErrors(op);

    testApiOfTableFieldFormData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfTableFieldFormData() throws Exception {
    // type TableFieldFormData
    IType tableFieldFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.TableFieldFormData");
    SdkAssert.assertHasFlags(tableFieldFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(tableFieldFormData, "QAbstractFormData;");
    SdkAssert.assertAnnotation(tableFieldFormData, "javax.annotation.Generated");

    // fields of TableFieldFormData
    SdkAssert.assertEquals("field count of 'TableFieldFormData'", 1, tableFieldFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(tableFieldFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'TableFieldFormData'", 5, tableFieldFormData.getMethods().length);
    IMethod tableFieldFormData1 = SdkAssert.assertMethodExist(tableFieldFormData, "TableFieldFormData", new String[]{});
    SdkAssert.assertTrue(tableFieldFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableFieldFormData1, "V");
    IMethod getCompanyTable = SdkAssert.assertMethodExist(tableFieldFormData, "getCompanyTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getCompanyTable, "QCompanyTable;");
    IMethod getConcreteTable = SdkAssert.assertMethodExist(tableFieldFormData, "getConcreteTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getConcreteTable, "QConcreteTable;");
    IMethod getPersonTable = SdkAssert.assertMethodExist(tableFieldFormData, "getPersonTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getPersonTable, "QPersonTable;");
    IMethod getTableFieldWithExternalTable = SdkAssert.assertMethodExist(tableFieldFormData, "getTableFieldWithExternalTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTableFieldWithExternalTable, "QTableFieldWithExternalTable;");

    SdkAssert.assertEquals("inner types count of 'TableFieldFormData'", 4, tableFieldFormData.getTypes().length);
    // type CompanyTable
    IType companyTable = SdkAssert.assertTypeExists(tableFieldFormData, "CompanyTable");
    SdkAssert.assertHasFlags(companyTable, 9);
    SdkAssert.assertHasSuperTypeSignature(companyTable, "QAbstractCompanyTableFieldData;");

    // fields of CompanyTable
    SdkAssert.assertEquals("field count of 'CompanyTable'", 1, companyTable.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(companyTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'CompanyTable'", 1, companyTable.getMethods().length);
    IMethod companyTable1 = SdkAssert.assertMethodExist(companyTable, "CompanyTable", new String[]{});
    SdkAssert.assertTrue(companyTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(companyTable1, "V");

    SdkAssert.assertEquals("inner types count of 'CompanyTable'", 0, companyTable.getTypes().length);
    // type ConcreteTable
    IType concreteTable = SdkAssert.assertTypeExists(tableFieldFormData, "ConcreteTable");
    SdkAssert.assertHasFlags(concreteTable, 9);
    SdkAssert.assertHasSuperTypeSignature(concreteTable, "QAbstractTableFieldBeanData;");

    // fields of ConcreteTable
    SdkAssert.assertEquals("field count of 'ConcreteTable'", 1, concreteTable.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(concreteTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'ConcreteTable'", 8, concreteTable.getMethods().length);
    IMethod concreteTable1 = SdkAssert.assertMethodExist(concreteTable, "ConcreteTable", new String[]{});
    SdkAssert.assertTrue(concreteTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(concreteTable1, "V");
    IMethod addRow = SdkAssert.assertMethodExist(concreteTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "QConcreteTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(concreteTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "QConcreteTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(concreteTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "QConcreteTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(concreteTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(concreteTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[QConcreteTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(concreteTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "QConcreteTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(concreteTable, "setRows", new String[]{"[QConcreteTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    SdkAssert.assertEquals("inner types count of 'ConcreteTable'", 1, concreteTable.getTypes().length);
    // type ConcreteTableRowData
    IType concreteTableRowData = SdkAssert.assertTypeExists(concreteTable, "ConcreteTableRowData");
    SdkAssert.assertHasFlags(concreteTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(concreteTableRowData, "QAbstractTableRowData;");

    // fields of ConcreteTableRowData
    SdkAssert.assertEquals("field count of 'ConcreteTableRowData'", 5, concreteTableRowData.getFields().length);
    IField serialVersionUID3 = SdkAssert.assertFieldExist(concreteTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");
    IField name = SdkAssert.assertFieldExist(concreteTableRowData, "name");
    SdkAssert.assertHasFlags(name, 25);
    SdkAssert.assertFieldSignature(name, "QString;");
    IField extKey = SdkAssert.assertFieldExist(concreteTableRowData, "extKey");
    SdkAssert.assertHasFlags(extKey, 25);
    SdkAssert.assertFieldSignature(extKey, "QString;");
    IField m_name = SdkAssert.assertFieldExist(concreteTableRowData, "m_name");
    SdkAssert.assertHasFlags(m_name, 2);
    SdkAssert.assertFieldSignature(m_name, "QString;");
    IField m_extKey = SdkAssert.assertFieldExist(concreteTableRowData, "m_extKey");
    SdkAssert.assertHasFlags(m_extKey, 2);
    SdkAssert.assertFieldSignature(m_extKey, "QInteger;");

    SdkAssert.assertEquals("method count of 'ConcreteTableRowData'", 5, concreteTableRowData.getMethods().length);
    IMethod concreteTableRowData1 = SdkAssert.assertMethodExist(concreteTableRowData, "ConcreteTableRowData", new String[]{});
    SdkAssert.assertTrue(concreteTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(concreteTableRowData1, "V");
    IMethod getName = SdkAssert.assertMethodExist(concreteTableRowData, "getName", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getName, "QString;");
    IMethod setName = SdkAssert.assertMethodExist(concreteTableRowData, "setName", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setName, "V");
    IMethod getExtKey = SdkAssert.assertMethodExist(concreteTableRowData, "getExtKey", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getExtKey, "QInteger;");
    IMethod setExtKey = SdkAssert.assertMethodExist(concreteTableRowData, "setExtKey", new String[]{"QInteger;"});
    SdkAssert.assertMethodReturnTypeSignature(setExtKey, "V");

    SdkAssert.assertEquals("inner types count of 'ConcreteTableRowData'", 0, concreteTableRowData.getTypes().length);
    // type PersonTable
    IType personTable = SdkAssert.assertTypeExists(tableFieldFormData, "PersonTable");
    SdkAssert.assertHasFlags(personTable, 9);
    SdkAssert.assertHasSuperTypeSignature(personTable, "QAbstractTableFieldData;");

    // fields of PersonTable
    SdkAssert.assertEquals("field count of 'PersonTable'", 6, personTable.getFields().length);
    IField serialVersionUID4 = SdkAssert.assertFieldExist(personTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");
    IField pERSON_NR_COLUMN_ID = SdkAssert.assertFieldExist(personTable, "PERSON_NR_COLUMN_ID");
    SdkAssert.assertHasFlags(pERSON_NR_COLUMN_ID, 25);
    SdkAssert.assertFieldSignature(pERSON_NR_COLUMN_ID, "I");
    IField nAME_COLUMN_ID = SdkAssert.assertFieldExist(personTable, "NAME_COLUMN_ID");
    SdkAssert.assertHasFlags(nAME_COLUMN_ID, 25);
    SdkAssert.assertFieldSignature(nAME_COLUMN_ID, "I");
    IField aN_OBJECT_COLUMN_ID = SdkAssert.assertFieldExist(personTable, "AN_OBJECT_COLUMN_ID");
    SdkAssert.assertHasFlags(aN_OBJECT_COLUMN_ID, 25);
    SdkAssert.assertFieldSignature(aN_OBJECT_COLUMN_ID, "I");
    IField sMART_LONG_COLUMN_ID = SdkAssert.assertFieldExist(personTable, "SMART_LONG_COLUMN_ID");
    SdkAssert.assertHasFlags(sMART_LONG_COLUMN_ID, 25);
    SdkAssert.assertFieldSignature(sMART_LONG_COLUMN_ID, "I");
    IField cUSTOM_COLUMN_ID = SdkAssert.assertFieldExist(personTable, "CUSTOM_COLUMN_ID");
    SdkAssert.assertHasFlags(cUSTOM_COLUMN_ID, 25);
    SdkAssert.assertFieldSignature(cUSTOM_COLUMN_ID, "I");

    SdkAssert.assertEquals("method count of 'PersonTable'", 14, personTable.getMethods().length);
    IMethod personTable1 = SdkAssert.assertMethodExist(personTable, "PersonTable", new String[]{});
    SdkAssert.assertTrue(personTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(personTable1, "V");
    IMethod getAnObject = SdkAssert.assertMethodExist(personTable, "getAnObject", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(getAnObject, "QObject;");
    IMethod setAnObject = SdkAssert.assertMethodExist(personTable, "setAnObject", new String[]{"I", "QObject;"});
    SdkAssert.assertMethodReturnTypeSignature(setAnObject, "V");
    IMethod getCustom = SdkAssert.assertMethodExist(personTable, "getCustom", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(getCustom, "QSet<QMap<QString;QInteger;>;>;");
    SdkAssert.assertAnnotation(getCustom, "java.lang.SuppressWarnings");
    IMethod setCustom = SdkAssert.assertMethodExist(personTable, "setCustom", new String[]{"I", "QSet<QMap<QString;QInteger;>;>;"});
    SdkAssert.assertMethodReturnTypeSignature(setCustom, "V");
    IMethod getName1 = SdkAssert.assertMethodExist(personTable, "getName", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(getName1, "QString;");
    IMethod setName1 = SdkAssert.assertMethodExist(personTable, "setName", new String[]{"I", "QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setName1, "V");
    IMethod getPersonNr = SdkAssert.assertMethodExist(personTable, "getPersonNr", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(getPersonNr, "QLong;");
    IMethod setPersonNr = SdkAssert.assertMethodExist(personTable, "setPersonNr", new String[]{"I", "QLong;"});
    SdkAssert.assertMethodReturnTypeSignature(setPersonNr, "V");
    IMethod getSmartLong = SdkAssert.assertMethodExist(personTable, "getSmartLong", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(getSmartLong, "QLong;");
    IMethod setSmartLong = SdkAssert.assertMethodExist(personTable, "setSmartLong", new String[]{"I", "QLong;"});
    SdkAssert.assertMethodReturnTypeSignature(setSmartLong, "V");
    IMethod getColumnCount = SdkAssert.assertMethodExist(personTable, "getColumnCount", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getColumnCount, "I");
    SdkAssert.assertAnnotation(getColumnCount, "java.lang.Override");
    IMethod getValueAt = SdkAssert.assertMethodExist(personTable, "getValueAt", new String[]{"I", "I"});
    SdkAssert.assertMethodReturnTypeSignature(getValueAt, "QObject;");
    SdkAssert.assertAnnotation(getValueAt, "java.lang.Override");
    IMethod setValueAt = SdkAssert.assertMethodExist(personTable, "setValueAt", new String[]{"I", "I", "QObject;"});
    SdkAssert.assertMethodReturnTypeSignature(setValueAt, "V");
    SdkAssert.assertAnnotation(setValueAt, "java.lang.Override");
    SdkAssert.assertAnnotation(setValueAt, "java.lang.SuppressWarnings");

    SdkAssert.assertEquals("inner types count of 'PersonTable'", 0, personTable.getTypes().length);
    // type TableFieldWithExternalTable
    IType tableFieldWithExternalTable = SdkAssert.assertTypeExists(tableFieldFormData, "TableFieldWithExternalTable");
    SdkAssert.assertHasFlags(tableFieldWithExternalTable, 9);
    SdkAssert.assertHasSuperTypeSignature(tableFieldWithExternalTable, "QAbstractTableFieldBeanData;");

    // fields of TableFieldWithExternalTable
    SdkAssert.assertEquals("field count of 'TableFieldWithExternalTable'", 1, tableFieldWithExternalTable.getFields().length);
    IField serialVersionUID5 = SdkAssert.assertFieldExist(tableFieldWithExternalTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    SdkAssert.assertEquals("method count of 'TableFieldWithExternalTable'", 8, tableFieldWithExternalTable.getMethods().length);
    IMethod tableFieldWithExternalTable1 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "TableFieldWithExternalTable", new String[]{});
    SdkAssert.assertTrue(tableFieldWithExternalTable1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableFieldWithExternalTable1, "V");
    IMethod addRow2 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow2, "QTableFieldWithExternalTableRowData;");
    SdkAssert.assertAnnotation(addRow2, "java.lang.Override");
    IMethod addRow3 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow3, "QTableFieldWithExternalTableRowData;");
    SdkAssert.assertAnnotation(addRow3, "java.lang.Override");
    IMethod createRow1 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow1, "QTableFieldWithExternalTableRowData;");
    SdkAssert.assertAnnotation(createRow1, "java.lang.Override");
    IMethod getRowType1 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType1, "QClass<+QAbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType1, "java.lang.Override");
    IMethod getRows1 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows1, "[QTableFieldWithExternalTableRowData;");
    SdkAssert.assertAnnotation(getRows1, "java.lang.Override");
    IMethod rowAt1 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt1, "QTableFieldWithExternalTableRowData;");
    SdkAssert.assertAnnotation(rowAt1, "java.lang.Override");
    IMethod setRows1 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "setRows", new String[]{"[QTableFieldWithExternalTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows1, "V");

    SdkAssert.assertEquals("inner types count of 'TableFieldWithExternalTable'", 1, tableFieldWithExternalTable.getTypes().length);
    // type TableFieldWithExternalTableRowData
    IType tableFieldWithExternalTableRowData = SdkAssert.assertTypeExists(tableFieldWithExternalTable, "TableFieldWithExternalTableRowData");
    SdkAssert.assertHasFlags(tableFieldWithExternalTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(tableFieldWithExternalTableRowData, "QAbstractTableRowData;");

    // fields of TableFieldWithExternalTableRowData
    SdkAssert.assertEquals("field count of 'TableFieldWithExternalTableRowData'", 7, tableFieldWithExternalTableRowData.getFields().length);
    IField serialVersionUID6 = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");
    IField aa = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "aa");
    SdkAssert.assertHasFlags(aa, 25);
    SdkAssert.assertFieldSignature(aa, "QString;");
    IField xx = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "xx");
    SdkAssert.assertHasFlags(xx, 25);
    SdkAssert.assertFieldSignature(xx, "QString;");
    IField bb = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "bb");
    SdkAssert.assertHasFlags(bb, 25);
    SdkAssert.assertFieldSignature(bb, "QString;");
    IField m_aa = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "m_aa");
    SdkAssert.assertHasFlags(m_aa, 2);
    SdkAssert.assertFieldSignature(m_aa, "QString;");
    IField m_xx = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "m_xx");
    SdkAssert.assertHasFlags(m_xx, 2);
    SdkAssert.assertFieldSignature(m_xx, "QString;");
    IField m_bb = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "m_bb");
    SdkAssert.assertHasFlags(m_bb, 2);
    SdkAssert.assertFieldSignature(m_bb, "QString;");

    SdkAssert.assertEquals("method count of 'TableFieldWithExternalTableRowData'", 7, tableFieldWithExternalTableRowData.getMethods().length);
    IMethod tableFieldWithExternalTableRowData1 = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "TableFieldWithExternalTableRowData", new String[]{});
    SdkAssert.assertTrue(tableFieldWithExternalTableRowData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableFieldWithExternalTableRowData1, "V");
    IMethod getAa = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "getAa", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getAa, "QString;");
    IMethod setAa = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "setAa", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setAa, "V");
    IMethod getXx = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "getXx", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getXx, "QString;");
    IMethod setXx = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "setXx", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setXx, "V");
    IMethod getBb = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "getBb", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getBb, "QString;");
    IMethod setBb = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "setBb", new String[]{"QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setBb, "V");

    SdkAssert.assertEquals("inner types count of 'TableFieldWithExternalTableRowData'", 0, tableFieldWithExternalTableRowData.getTypes().length);
  }

}
