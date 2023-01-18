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

import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.TableFieldForm;

public class TableFieldFormTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(TableFieldForm.class.getName(), TableFieldFormTest::testApiOfTableFieldFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfTableFieldFormData(IType tableFieldFormData) {
    assertHasFlags(tableFieldFormData, 1);
    assertHasSuperClass(tableFieldFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");
    assertAnnotation(tableFieldFormData, "javax.annotation.Generated");

    // fields of TableFieldFormData
    assertEquals(1, tableFieldFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData'");
    var serialVersionUID = assertFieldExist(tableFieldFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(4, tableFieldFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData'");
    var getCompanyTable = assertMethodExist(tableFieldFormData, "getCompanyTable");
    assertMethodReturnType(getCompanyTable, "formdata.shared.services.process.TableFieldFormData$CompanyTable");
    var getConcreteTable = assertMethodExist(tableFieldFormData, "getConcreteTable");
    assertMethodReturnType(getConcreteTable, "formdata.shared.services.process.TableFieldFormData$ConcreteTable");
    var getPersonTable = assertMethodExist(tableFieldFormData, "getPersonTable");
    assertMethodReturnType(getPersonTable, "formdata.shared.services.process.TableFieldFormData$PersonTable");
    var getTableFieldWithExternalTable = assertMethodExist(tableFieldFormData, "getTableFieldWithExternalTable");
    assertMethodReturnType(getTableFieldWithExternalTable, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable");

    assertEquals(4, tableFieldFormData.innerTypes().stream().count(), "inner types count of 'TableFieldFormData'");
    // type CompanyTable
    var companyTable = assertTypeExists(tableFieldFormData, "CompanyTable");
    assertHasFlags(companyTable, 9);
    assertHasSuperClass(companyTable, "formdata.shared.services.process.AbstractCompanyTableFieldData");

    // fields of CompanyTable
    assertEquals(1, companyTable.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable'");
    var serialVersionUID1 = assertFieldExist(companyTable, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(7, companyTable.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable'");
    var addRow = assertMethodExist(companyTable, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(companyTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(companyTable, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(companyTable, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(companyTable, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(companyTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(companyTable, "setRows", new String[]{"formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, companyTable.innerTypes().stream().count(), "inner types count of 'CompanyTable'");
    // type CompanyTableRowData
    var companyTableRowData = assertTypeExists(companyTable, "CompanyTableRowData");
    assertHasFlags(companyTableRowData, 9);
    assertHasSuperClass(companyTableRowData, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData");

    // fields of CompanyTableRowData
    assertEquals(1, companyTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData'");
    var serialVersionUID2 = assertFieldExist(companyTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, companyTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData'");

    assertEquals(0, companyTableRowData.innerTypes().stream().count(), "inner types count of 'CompanyTableRowData'");
    // type ConcreteTable
    var concreteTable = assertTypeExists(tableFieldFormData, "ConcreteTable");
    assertHasFlags(concreteTable, 9);
    assertHasSuperClass(concreteTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of ConcreteTable
    assertEquals(1, concreteTable.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable'");
    var serialVersionUID3 = assertFieldExist(concreteTable, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(7, concreteTable.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable'");
    var addRow2 = assertMethodExist(concreteTable, "addRow");
    assertMethodReturnType(addRow2, "formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData");
    assertAnnotation(addRow2, "java.lang.Override");
    var addRow3 = assertMethodExist(concreteTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow3, "formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData");
    assertAnnotation(addRow3, "java.lang.Override");
    var createRow1 = assertMethodExist(concreteTable, "createRow");
    assertMethodReturnType(createRow1, "formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData");
    assertAnnotation(createRow1, "java.lang.Override");
    var getRowType1 = assertMethodExist(concreteTable, "getRowType");
    assertMethodReturnType(getRowType1, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType1, "java.lang.Override");
    var getRows1 = assertMethodExist(concreteTable, "getRows");
    assertMethodReturnType(getRows1, "formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData[]");
    assertAnnotation(getRows1, "java.lang.Override");
    var rowAt1 = assertMethodExist(concreteTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt1, "formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData");
    assertAnnotation(rowAt1, "java.lang.Override");
    var setRows1 = assertMethodExist(concreteTable, "setRows", new String[]{"formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData[]"});
    assertMethodReturnType(setRows1, "void");

    assertEquals(1, concreteTable.innerTypes().stream().count(), "inner types count of 'ConcreteTable'");
    // type ConcreteTableRowData
    var concreteTableRowData = assertTypeExists(concreteTable, "ConcreteTableRowData");
    assertHasFlags(concreteTableRowData, 9);
    assertHasSuperClass(concreteTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of ConcreteTableRowData
    assertEquals(5, concreteTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData'");
    var serialVersionUID4 = assertFieldExist(concreteTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");
    var name = assertFieldExist(concreteTableRowData, "name");
    assertHasFlags(name, 25);
    assertFieldType(name, "java.lang.String");
    var extKey = assertFieldExist(concreteTableRowData, "extKey");
    assertHasFlags(extKey, 25);
    assertFieldType(extKey, "java.lang.String");
    var m_name = assertFieldExist(concreteTableRowData, "m_name");
    assertHasFlags(m_name, 2);
    assertFieldType(m_name, "java.lang.String");
    var m_extKey = assertFieldExist(concreteTableRowData, "m_extKey");
    assertHasFlags(m_extKey, 2);
    assertFieldType(m_extKey, "java.lang.Integer");

    assertEquals(4, concreteTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData'");
    var getName = assertMethodExist(concreteTableRowData, "getName");
    assertMethodReturnType(getName, "java.lang.String");
    var setName = assertMethodExist(concreteTableRowData, "setName", new String[]{"java.lang.String"});
    assertMethodReturnType(setName, "void");
    var getExtKey = assertMethodExist(concreteTableRowData, "getExtKey");
    assertMethodReturnType(getExtKey, "java.lang.Integer");
    var setExtKey = assertMethodExist(concreteTableRowData, "setExtKey", new String[]{"java.lang.Integer"});
    assertMethodReturnType(setExtKey, "void");

    assertEquals(0, concreteTableRowData.innerTypes().stream().count(), "inner types count of 'ConcreteTableRowData'");
    // type PersonTable
    var personTable = assertTypeExists(tableFieldFormData, "PersonTable");
    assertHasFlags(personTable, 9);
    assertHasSuperClass(personTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of PersonTable
    assertEquals(1, personTable.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$PersonTable'");
    var serialVersionUID5 = assertFieldExist(personTable, "serialVersionUID");
    assertHasFlags(serialVersionUID5, 26);
    assertFieldType(serialVersionUID5, "long");

    assertEquals(7, personTable.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$PersonTable'");
    var addRow4 = assertMethodExist(personTable, "addRow");
    assertMethodReturnType(addRow4, "formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData");
    assertAnnotation(addRow4, "java.lang.Override");
    var addRow5 = assertMethodExist(personTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow5, "formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData");
    assertAnnotation(addRow5, "java.lang.Override");
    var createRow2 = assertMethodExist(personTable, "createRow");
    assertMethodReturnType(createRow2, "formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData");
    assertAnnotation(createRow2, "java.lang.Override");
    var getRowType2 = assertMethodExist(personTable, "getRowType");
    assertMethodReturnType(getRowType2, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType2, "java.lang.Override");
    var getRows2 = assertMethodExist(personTable, "getRows");
    assertMethodReturnType(getRows2, "formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData[]");
    assertAnnotation(getRows2, "java.lang.Override");
    var rowAt2 = assertMethodExist(personTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt2, "formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData");
    assertAnnotation(rowAt2, "java.lang.Override");
    var setRows2 = assertMethodExist(personTable, "setRows", new String[]{"formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData[]"});
    assertMethodReturnType(setRows2, "void");

    assertEquals(1, personTable.innerTypes().stream().count(), "inner types count of 'PersonTable'");
    // type PersonTableRowData
    var personTableRowData = assertTypeExists(personTable, "PersonTableRowData");
    assertHasFlags(personTableRowData, 9);
    assertHasSuperClass(personTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of PersonTableRowData
    assertEquals(11, personTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData'");
    var serialVersionUID6 = assertFieldExist(personTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID6, 26);
    assertFieldType(serialVersionUID6, "long");
    var personNr = assertFieldExist(personTableRowData, "personNr");
    assertHasFlags(personNr, 25);
    assertFieldType(personNr, "java.lang.String");
    var name1 = assertFieldExist(personTableRowData, "name");
    assertHasFlags(name1, 25);
    assertFieldType(name1, "java.lang.String");
    var anObject = assertFieldExist(personTableRowData, "anObject");
    assertHasFlags(anObject, 25);
    assertFieldType(anObject, "java.lang.String");
    var smartLong = assertFieldExist(personTableRowData, "smartLong");
    assertHasFlags(smartLong, 25);
    assertFieldType(smartLong, "java.lang.String");
    var custom = assertFieldExist(personTableRowData, "custom");
    assertHasFlags(custom, 25);
    assertFieldType(custom, "java.lang.String");
    var m_personNr = assertFieldExist(personTableRowData, "m_personNr");
    assertHasFlags(m_personNr, 2);
    assertFieldType(m_personNr, "java.lang.Long");
    var m_name1 = assertFieldExist(personTableRowData, "m_name");
    assertHasFlags(m_name1, 2);
    assertFieldType(m_name1, "java.lang.String");
    var m_anObject = assertFieldExist(personTableRowData, "m_anObject");
    assertHasFlags(m_anObject, 2);
    assertFieldType(m_anObject, "java.lang.Object");
    var m_smartLong = assertFieldExist(personTableRowData, "m_smartLong");
    assertHasFlags(m_smartLong, 2);
    assertFieldType(m_smartLong, "java.lang.Long");
    var m_custom = assertFieldExist(personTableRowData, "m_custom");
    assertHasFlags(m_custom, 2);
    assertFieldType(m_custom, "java.util.Set<java.util.Map<java.lang.String,java.lang.Integer>>");

    assertEquals(10, personTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData'");
    var getPersonNr = assertMethodExist(personTableRowData, "getPersonNr");
    assertMethodReturnType(getPersonNr, "java.lang.Long");
    var setPersonNr = assertMethodExist(personTableRowData, "setPersonNr", new String[]{"java.lang.Long"});
    assertMethodReturnType(setPersonNr, "void");
    var getName1 = assertMethodExist(personTableRowData, "getName");
    assertMethodReturnType(getName1, "java.lang.String");
    var setName1 = assertMethodExist(personTableRowData, "setName", new String[]{"java.lang.String"});
    assertMethodReturnType(setName1, "void");
    var getAnObject = assertMethodExist(personTableRowData, "getAnObject");
    assertMethodReturnType(getAnObject, "java.lang.Object");
    var setAnObject = assertMethodExist(personTableRowData, "setAnObject", new String[]{"java.lang.Object"});
    assertMethodReturnType(setAnObject, "void");
    var getSmartLong = assertMethodExist(personTableRowData, "getSmartLong");
    assertMethodReturnType(getSmartLong, "java.lang.Long");
    var setSmartLong = assertMethodExist(personTableRowData, "setSmartLong", new String[]{"java.lang.Long"});
    assertMethodReturnType(setSmartLong, "void");
    var getCustom = assertMethodExist(personTableRowData, "getCustom");
    assertMethodReturnType(getCustom, "java.util.Set<java.util.Map<java.lang.String,java.lang.Integer>>");
    var setCustom = assertMethodExist(personTableRowData, "setCustom", new String[]{"java.util.Set<java.util.Map<java.lang.String,java.lang.Integer>>"});
    assertMethodReturnType(setCustom, "void");

    assertEquals(0, personTableRowData.innerTypes().stream().count(), "inner types count of 'PersonTableRowData'");
    // type TableFieldWithExternalTable
    var tableFieldWithExternalTable = assertTypeExists(tableFieldFormData, "TableFieldWithExternalTable");
    assertHasFlags(tableFieldWithExternalTable, 9);
    assertHasSuperClass(tableFieldWithExternalTable, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");

    // fields of TableFieldWithExternalTable
    assertEquals(1, tableFieldWithExternalTable.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable'");
    var serialVersionUID7 = assertFieldExist(tableFieldWithExternalTable, "serialVersionUID");
    assertHasFlags(serialVersionUID7, 26);
    assertFieldType(serialVersionUID7, "long");

    assertEquals(7, tableFieldWithExternalTable.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable'");
    var addRow6 = assertMethodExist(tableFieldWithExternalTable, "addRow");
    assertMethodReturnType(addRow6, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData");
    assertAnnotation(addRow6, "java.lang.Override");
    var addRow7 = assertMethodExist(tableFieldWithExternalTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow7, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData");
    assertAnnotation(addRow7, "java.lang.Override");
    var createRow3 = assertMethodExist(tableFieldWithExternalTable, "createRow");
    assertMethodReturnType(createRow3, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData");
    assertAnnotation(createRow3, "java.lang.Override");
    var getRowType3 = assertMethodExist(tableFieldWithExternalTable, "getRowType");
    assertMethodReturnType(getRowType3, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType3, "java.lang.Override");
    var getRows3 = assertMethodExist(tableFieldWithExternalTable, "getRows");
    assertMethodReturnType(getRows3, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData[]");
    assertAnnotation(getRows3, "java.lang.Override");
    var rowAt3 = assertMethodExist(tableFieldWithExternalTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt3, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData");
    assertAnnotation(rowAt3, "java.lang.Override");
    var setRows3 = assertMethodExist(tableFieldWithExternalTable, "setRows", new String[]{"formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData[]"});
    assertMethodReturnType(setRows3, "void");

    assertEquals(1, tableFieldWithExternalTable.innerTypes().stream().count(), "inner types count of 'TableFieldWithExternalTable'");
    // type TableFieldWithExternalTableRowData
    var tableFieldWithExternalTableRowData = assertTypeExists(tableFieldWithExternalTable, "TableFieldWithExternalTableRowData");
    assertHasFlags(tableFieldWithExternalTableRowData, 9);
    assertHasSuperClass(tableFieldWithExternalTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of TableFieldWithExternalTableRowData
    assertEquals(7, tableFieldWithExternalTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData'");
    var serialVersionUID8 = assertFieldExist(tableFieldWithExternalTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID8, 26);
    assertFieldType(serialVersionUID8, "long");
    var aa = assertFieldExist(tableFieldWithExternalTableRowData, "aa");
    assertHasFlags(aa, 25);
    assertFieldType(aa, "java.lang.String");
    var xx = assertFieldExist(tableFieldWithExternalTableRowData, "xx");
    assertHasFlags(xx, 25);
    assertFieldType(xx, "java.lang.String");
    var bb = assertFieldExist(tableFieldWithExternalTableRowData, "bb");
    assertHasFlags(bb, 25);
    assertFieldType(bb, "java.lang.String");
    var m_aa = assertFieldExist(tableFieldWithExternalTableRowData, "m_aa");
    assertHasFlags(m_aa, 2);
    assertFieldType(m_aa, "java.lang.String");
    var m_xx = assertFieldExist(tableFieldWithExternalTableRowData, "m_xx");
    assertHasFlags(m_xx, 2);
    assertFieldType(m_xx, "java.lang.String");
    var m_bb = assertFieldExist(tableFieldWithExternalTableRowData, "m_bb");
    assertHasFlags(m_bb, 2);
    assertFieldType(m_bb, "java.lang.String");

    assertEquals(6, tableFieldWithExternalTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData'");
    var getAa = assertMethodExist(tableFieldWithExternalTableRowData, "getAa");
    assertMethodReturnType(getAa, "java.lang.String");
    var setAa = assertMethodExist(tableFieldWithExternalTableRowData, "setAa", new String[]{"java.lang.String"});
    assertMethodReturnType(setAa, "void");
    var getXx = assertMethodExist(tableFieldWithExternalTableRowData, "getXx");
    assertMethodReturnType(getXx, "java.lang.String");
    var setXx = assertMethodExist(tableFieldWithExternalTableRowData, "setXx", new String[]{"java.lang.String"});
    assertMethodReturnType(setXx, "void");
    var getBb = assertMethodExist(tableFieldWithExternalTableRowData, "getBb");
    assertMethodReturnType(getBb, "java.lang.String");
    var setBb = assertMethodExist(tableFieldWithExternalTableRowData, "setBb", new String[]{"java.lang.String"});
    assertMethodReturnType(setBb, "void");

    assertEquals(0, tableFieldWithExternalTableRowData.innerTypes().stream().count(), "inner types count of 'TableFieldWithExternalTableRowData'");
  }

}
