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
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertTypeExists;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.junit.jupiter.api.Test;

public class TableFieldFormTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.TableFieldForm", TableFieldFormTest::testApiOfTableFieldFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfTableFieldFormData(IType tableFieldFormData) {
    var scoutApi = tableFieldFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(tableFieldFormData, Flags.AccPublic);
    assertHasSuperClass(tableFieldFormData, scoutApi.AbstractFormData());
    assertEquals(1, tableFieldFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(tableFieldFormData, scoutApi.Generated());

    // fields of TableFieldFormData
    assertEquals(1, tableFieldFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData'");
    var serialVersionUID = assertFieldExist(tableFieldFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(4, tableFieldFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData'");
    var getCompanyTable = assertMethodExist(tableFieldFormData, "getCompanyTable");
    assertMethodReturnType(getCompanyTable, "formdata.shared.services.process.TableFieldFormData$CompanyTable");
    assertEquals(0, getCompanyTable.annotations().stream().count(), "annotation count");
    var getConcreteTable = assertMethodExist(tableFieldFormData, "getConcreteTable");
    assertMethodReturnType(getConcreteTable, "formdata.shared.services.process.TableFieldFormData$ConcreteTable");
    assertEquals(0, getConcreteTable.annotations().stream().count(), "annotation count");
    var getPersonTable = assertMethodExist(tableFieldFormData, "getPersonTable");
    assertMethodReturnType(getPersonTable, "formdata.shared.services.process.TableFieldFormData$PersonTable");
    assertEquals(0, getPersonTable.annotations().stream().count(), "annotation count");
    var getTableFieldWithExternalTable = assertMethodExist(tableFieldFormData, "getTableFieldWithExternalTable");
    assertMethodReturnType(getTableFieldWithExternalTable, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable");
    assertEquals(0, getTableFieldWithExternalTable.annotations().stream().count(), "annotation count");

    assertEquals(4, tableFieldFormData.innerTypes().stream().count(), "inner types count of 'TableFieldFormData'");
    // type CompanyTable
    var companyTable = assertTypeExists(tableFieldFormData, "CompanyTable");
    assertHasFlags(companyTable, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(companyTable, "formdata.shared.services.process.AbstractCompanyTableFieldData");
    assertEquals(0, companyTable.annotations().stream().count(), "annotation count");

    // fields of CompanyTable
    assertEquals(1, companyTable.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable'");
    var serialVersionUID1 = assertFieldExist(companyTable, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(7, companyTable.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable'");
    var addRow = assertMethodExist(companyTable, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(companyTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(companyTable, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(companyTable, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(companyTable, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(companyTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(companyTable, "setRows", new String[]{"formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, companyTable.innerTypes().stream().count(), "inner types count of 'CompanyTable'");
    // type CompanyTableRowData
    var companyTableRowData = assertTypeExists(companyTable, "CompanyTableRowData");
    assertHasFlags(companyTableRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(companyTableRowData, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData");
    assertEquals(0, companyTableRowData.annotations().stream().count(), "annotation count");

    // fields of CompanyTableRowData
    assertEquals(1, companyTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData'");
    var serialVersionUID2 = assertFieldExist(companyTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, companyTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData'");

    assertEquals(0, companyTableRowData.innerTypes().stream().count(), "inner types count of 'CompanyTableRowData'");
    // type ConcreteTable
    var concreteTable = assertTypeExists(tableFieldFormData, "ConcreteTable");
    assertHasFlags(concreteTable, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(concreteTable, scoutApi.AbstractTableFieldBeanData());
    assertEquals(0, concreteTable.annotations().stream().count(), "annotation count");

    // fields of ConcreteTable
    assertEquals(1, concreteTable.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable'");
    var serialVersionUID3 = assertFieldExist(concreteTable, "serialVersionUID");
    assertHasFlags(serialVersionUID3, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID3, "long");
    assertEquals(0, serialVersionUID3.annotations().stream().count(), "annotation count");

    assertEquals(7, concreteTable.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable'");
    var addRow2 = assertMethodExist(concreteTable, "addRow");
    assertMethodReturnType(addRow2, "formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData");
    assertEquals(1, addRow2.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow2, "java.lang.Override");
    var addRow3 = assertMethodExist(concreteTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow3, "formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData");
    assertEquals(1, addRow3.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow3, "java.lang.Override");
    var createRow1 = assertMethodExist(concreteTable, "createRow");
    assertMethodReturnType(createRow1, "formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData");
    assertEquals(1, createRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow1, "java.lang.Override");
    var getRowType1 = assertMethodExist(concreteTable, "getRowType");
    assertMethodReturnType(getRowType1, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType1.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType1, "java.lang.Override");
    var getRows1 = assertMethodExist(concreteTable, "getRows");
    assertMethodReturnType(getRows1, "formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData[]");
    assertEquals(1, getRows1.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows1, "java.lang.Override");
    var rowAt1 = assertMethodExist(concreteTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt1, "formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData");
    assertEquals(1, rowAt1.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt1, "java.lang.Override");
    var setRows1 = assertMethodExist(concreteTable, "setRows", new String[]{"formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData[]"});
    assertMethodReturnType(setRows1, "void");
    assertEquals(0, setRows1.annotations().stream().count(), "annotation count");

    assertEquals(1, concreteTable.innerTypes().stream().count(), "inner types count of 'ConcreteTable'");
    // type ConcreteTableRowData
    var concreteTableRowData = assertTypeExists(concreteTable, "ConcreteTableRowData");
    assertHasFlags(concreteTableRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(concreteTableRowData, scoutApi.AbstractTableRowData());
    assertEquals(0, concreteTableRowData.annotations().stream().count(), "annotation count");

    // fields of ConcreteTableRowData
    assertEquals(5, concreteTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData'");
    var serialVersionUID4 = assertFieldExist(concreteTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID4, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID4, "long");
    assertEquals(0, serialVersionUID4.annotations().stream().count(), "annotation count");
    var name = assertFieldExist(concreteTableRowData, "name");
    assertHasFlags(name, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(name, "java.lang.String");
    assertEquals(0, name.annotations().stream().count(), "annotation count");
    var extKey = assertFieldExist(concreteTableRowData, "extKey");
    assertHasFlags(extKey, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(extKey, "java.lang.String");
    assertEquals(0, extKey.annotations().stream().count(), "annotation count");
    var m_name = assertFieldExist(concreteTableRowData, "m_name");
    assertHasFlags(m_name, Flags.AccPrivate);
    assertFieldType(m_name, "java.lang.String");
    assertEquals(0, m_name.annotations().stream().count(), "annotation count");
    var m_extKey = assertFieldExist(concreteTableRowData, "m_extKey");
    assertHasFlags(m_extKey, Flags.AccPrivate);
    assertFieldType(m_extKey, "java.lang.Integer");
    assertEquals(0, m_extKey.annotations().stream().count(), "annotation count");

    assertEquals(4, concreteTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData'");
    var getName = assertMethodExist(concreteTableRowData, "getName");
    assertMethodReturnType(getName, "java.lang.String");
    assertEquals(0, getName.annotations().stream().count(), "annotation count");
    var setName = assertMethodExist(concreteTableRowData, "setName", new String[]{"java.lang.String"});
    assertMethodReturnType(setName, "void");
    assertEquals(0, setName.annotations().stream().count(), "annotation count");
    var getExtKey = assertMethodExist(concreteTableRowData, "getExtKey");
    assertMethodReturnType(getExtKey, "java.lang.Integer");
    assertEquals(0, getExtKey.annotations().stream().count(), "annotation count");
    var setExtKey = assertMethodExist(concreteTableRowData, "setExtKey", new String[]{"java.lang.Integer"});
    assertMethodReturnType(setExtKey, "void");
    assertEquals(0, setExtKey.annotations().stream().count(), "annotation count");

    assertEquals(0, concreteTableRowData.innerTypes().stream().count(), "inner types count of 'ConcreteTableRowData'");
    // type PersonTable
    var personTable = assertTypeExists(tableFieldFormData, "PersonTable");
    assertHasFlags(personTable, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(personTable, scoutApi.AbstractTableFieldBeanData());
    assertEquals(0, personTable.annotations().stream().count(), "annotation count");

    // fields of PersonTable
    assertEquals(1, personTable.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$PersonTable'");
    var serialVersionUID5 = assertFieldExist(personTable, "serialVersionUID");
    assertHasFlags(serialVersionUID5, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID5, "long");
    assertEquals(0, serialVersionUID5.annotations().stream().count(), "annotation count");

    assertEquals(7, personTable.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$PersonTable'");
    var addRow4 = assertMethodExist(personTable, "addRow");
    assertMethodReturnType(addRow4, "formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData");
    assertEquals(1, addRow4.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow4, "java.lang.Override");
    var addRow5 = assertMethodExist(personTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow5, "formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData");
    assertEquals(1, addRow5.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow5, "java.lang.Override");
    var createRow2 = assertMethodExist(personTable, "createRow");
    assertMethodReturnType(createRow2, "formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData");
    assertEquals(1, createRow2.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow2, "java.lang.Override");
    var getRowType2 = assertMethodExist(personTable, "getRowType");
    assertMethodReturnType(getRowType2, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType2.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType2, "java.lang.Override");
    var getRows2 = assertMethodExist(personTable, "getRows");
    assertMethodReturnType(getRows2, "formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData[]");
    assertEquals(1, getRows2.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows2, "java.lang.Override");
    var rowAt2 = assertMethodExist(personTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt2, "formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData");
    assertEquals(1, rowAt2.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt2, "java.lang.Override");
    var setRows2 = assertMethodExist(personTable, "setRows", new String[]{"formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData[]"});
    assertMethodReturnType(setRows2, "void");
    assertEquals(0, setRows2.annotations().stream().count(), "annotation count");

    assertEquals(1, personTable.innerTypes().stream().count(), "inner types count of 'PersonTable'");
    // type PersonTableRowData
    var personTableRowData = assertTypeExists(personTable, "PersonTableRowData");
    assertHasFlags(personTableRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(personTableRowData, scoutApi.AbstractTableRowData());
    assertEquals(0, personTableRowData.annotations().stream().count(), "annotation count");

    // fields of PersonTableRowData
    assertEquals(11, personTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData'");
    var serialVersionUID6 = assertFieldExist(personTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID6, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID6, "long");
    assertEquals(0, serialVersionUID6.annotations().stream().count(), "annotation count");
    var personNr = assertFieldExist(personTableRowData, "personNr");
    assertHasFlags(personNr, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(personNr, "java.lang.String");
    assertEquals(0, personNr.annotations().stream().count(), "annotation count");
    var name1 = assertFieldExist(personTableRowData, "name");
    assertHasFlags(name1, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(name1, "java.lang.String");
    assertEquals(0, name1.annotations().stream().count(), "annotation count");
    var anObject = assertFieldExist(personTableRowData, "anObject");
    assertHasFlags(anObject, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(anObject, "java.lang.String");
    assertEquals(0, anObject.annotations().stream().count(), "annotation count");
    var smartLong = assertFieldExist(personTableRowData, "smartLong");
    assertHasFlags(smartLong, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(smartLong, "java.lang.String");
    assertEquals(0, smartLong.annotations().stream().count(), "annotation count");
    var custom = assertFieldExist(personTableRowData, "custom");
    assertHasFlags(custom, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(custom, "java.lang.String");
    assertEquals(0, custom.annotations().stream().count(), "annotation count");
    var m_personNr = assertFieldExist(personTableRowData, "m_personNr");
    assertHasFlags(m_personNr, Flags.AccPrivate);
    assertFieldType(m_personNr, "java.lang.Long");
    assertEquals(0, m_personNr.annotations().stream().count(), "annotation count");
    var m_name1 = assertFieldExist(personTableRowData, "m_name");
    assertHasFlags(m_name1, Flags.AccPrivate);
    assertFieldType(m_name1, "java.lang.String");
    assertEquals(0, m_name1.annotations().stream().count(), "annotation count");
    var m_anObject = assertFieldExist(personTableRowData, "m_anObject");
    assertHasFlags(m_anObject, Flags.AccPrivate);
    assertFieldType(m_anObject, "java.lang.Object");
    assertEquals(0, m_anObject.annotations().stream().count(), "annotation count");
    var m_smartLong = assertFieldExist(personTableRowData, "m_smartLong");
    assertHasFlags(m_smartLong, Flags.AccPrivate);
    assertFieldType(m_smartLong, "java.lang.Long");
    assertEquals(0, m_smartLong.annotations().stream().count(), "annotation count");
    var m_custom = assertFieldExist(personTableRowData, "m_custom");
    assertHasFlags(m_custom, Flags.AccPrivate);
    assertFieldType(m_custom, "java.util.Set<java.util.Map<java.lang.String,java.lang.Integer>>");
    assertEquals(0, m_custom.annotations().stream().count(), "annotation count");

    assertEquals(10, personTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData'");
    var getPersonNr = assertMethodExist(personTableRowData, "getPersonNr");
    assertMethodReturnType(getPersonNr, "java.lang.Long");
    assertEquals(0, getPersonNr.annotations().stream().count(), "annotation count");
    var setPersonNr = assertMethodExist(personTableRowData, "setPersonNr", new String[]{"java.lang.Long"});
    assertMethodReturnType(setPersonNr, "void");
    assertEquals(0, setPersonNr.annotations().stream().count(), "annotation count");
    var getName1 = assertMethodExist(personTableRowData, "getName");
    assertMethodReturnType(getName1, "java.lang.String");
    assertEquals(0, getName1.annotations().stream().count(), "annotation count");
    var setName1 = assertMethodExist(personTableRowData, "setName", new String[]{"java.lang.String"});
    assertMethodReturnType(setName1, "void");
    assertEquals(0, setName1.annotations().stream().count(), "annotation count");
    var getAnObject = assertMethodExist(personTableRowData, "getAnObject");
    assertMethodReturnType(getAnObject, "java.lang.Object");
    assertEquals(0, getAnObject.annotations().stream().count(), "annotation count");
    var setAnObject = assertMethodExist(personTableRowData, "setAnObject", new String[]{"java.lang.Object"});
    assertMethodReturnType(setAnObject, "void");
    assertEquals(0, setAnObject.annotations().stream().count(), "annotation count");
    var getSmartLong = assertMethodExist(personTableRowData, "getSmartLong");
    assertMethodReturnType(getSmartLong, "java.lang.Long");
    assertEquals(0, getSmartLong.annotations().stream().count(), "annotation count");
    var setSmartLong = assertMethodExist(personTableRowData, "setSmartLong", new String[]{"java.lang.Long"});
    assertMethodReturnType(setSmartLong, "void");
    assertEquals(0, setSmartLong.annotations().stream().count(), "annotation count");
    var getCustom = assertMethodExist(personTableRowData, "getCustom");
    assertMethodReturnType(getCustom, "java.util.Set<java.util.Map<java.lang.String,java.lang.Integer>>");
    assertEquals(0, getCustom.annotations().stream().count(), "annotation count");
    var setCustom = assertMethodExist(personTableRowData, "setCustom", new String[]{"java.util.Set<java.util.Map<java.lang.String,java.lang.Integer>>"});
    assertMethodReturnType(setCustom, "void");
    assertEquals(0, setCustom.annotations().stream().count(), "annotation count");

    assertEquals(0, personTableRowData.innerTypes().stream().count(), "inner types count of 'PersonTableRowData'");
    // type TableFieldWithExternalTable
    var tableFieldWithExternalTable = assertTypeExists(tableFieldFormData, "TableFieldWithExternalTable");
    assertHasFlags(tableFieldWithExternalTable, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableFieldWithExternalTable, scoutApi.AbstractTableFieldBeanData());
    assertEquals(0, tableFieldWithExternalTable.annotations().stream().count(), "annotation count");

    // fields of TableFieldWithExternalTable
    assertEquals(1, tableFieldWithExternalTable.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable'");
    var serialVersionUID7 = assertFieldExist(tableFieldWithExternalTable, "serialVersionUID");
    assertHasFlags(serialVersionUID7, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID7, "long");
    assertEquals(0, serialVersionUID7.annotations().stream().count(), "annotation count");

    assertEquals(7, tableFieldWithExternalTable.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable'");
    var addRow6 = assertMethodExist(tableFieldWithExternalTable, "addRow");
    assertMethodReturnType(addRow6, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData");
    assertEquals(1, addRow6.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow6, "java.lang.Override");
    var addRow7 = assertMethodExist(tableFieldWithExternalTable, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow7, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData");
    assertEquals(1, addRow7.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow7, "java.lang.Override");
    var createRow3 = assertMethodExist(tableFieldWithExternalTable, "createRow");
    assertMethodReturnType(createRow3, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData");
    assertEquals(1, createRow3.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow3, "java.lang.Override");
    var getRowType3 = assertMethodExist(tableFieldWithExternalTable, "getRowType");
    assertMethodReturnType(getRowType3, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType3.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType3, "java.lang.Override");
    var getRows3 = assertMethodExist(tableFieldWithExternalTable, "getRows");
    assertMethodReturnType(getRows3, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData[]");
    assertEquals(1, getRows3.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows3, "java.lang.Override");
    var rowAt3 = assertMethodExist(tableFieldWithExternalTable, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt3, "formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData");
    assertEquals(1, rowAt3.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt3, "java.lang.Override");
    var setRows3 = assertMethodExist(tableFieldWithExternalTable, "setRows", new String[]{"formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData[]"});
    assertMethodReturnType(setRows3, "void");
    assertEquals(0, setRows3.annotations().stream().count(), "annotation count");

    assertEquals(1, tableFieldWithExternalTable.innerTypes().stream().count(), "inner types count of 'TableFieldWithExternalTable'");
    // type TableFieldWithExternalTableRowData
    var tableFieldWithExternalTableRowData = assertTypeExists(tableFieldWithExternalTable, "TableFieldWithExternalTableRowData");
    assertHasFlags(tableFieldWithExternalTableRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(tableFieldWithExternalTableRowData, scoutApi.AbstractTableRowData());
    assertEquals(0, tableFieldWithExternalTableRowData.annotations().stream().count(), "annotation count");

    // fields of TableFieldWithExternalTableRowData
    assertEquals(7, tableFieldWithExternalTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData'");
    var serialVersionUID8 = assertFieldExist(tableFieldWithExternalTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID8, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID8, "long");
    assertEquals(0, serialVersionUID8.annotations().stream().count(), "annotation count");
    var aa = assertFieldExist(tableFieldWithExternalTableRowData, "aa");
    assertHasFlags(aa, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(aa, "java.lang.String");
    assertEquals(0, aa.annotations().stream().count(), "annotation count");
    var xx = assertFieldExist(tableFieldWithExternalTableRowData, "xx");
    assertHasFlags(xx, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(xx, "java.lang.String");
    assertEquals(0, xx.annotations().stream().count(), "annotation count");
    var bb = assertFieldExist(tableFieldWithExternalTableRowData, "bb");
    assertHasFlags(bb, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(bb, "java.lang.String");
    assertEquals(0, bb.annotations().stream().count(), "annotation count");
    var m_aa = assertFieldExist(tableFieldWithExternalTableRowData, "m_aa");
    assertHasFlags(m_aa, Flags.AccPrivate);
    assertFieldType(m_aa, "java.lang.String");
    assertEquals(0, m_aa.annotations().stream().count(), "annotation count");
    var m_xx = assertFieldExist(tableFieldWithExternalTableRowData, "m_xx");
    assertHasFlags(m_xx, Flags.AccPrivate);
    assertFieldType(m_xx, "java.lang.String");
    assertEquals(0, m_xx.annotations().stream().count(), "annotation count");
    var m_bb = assertFieldExist(tableFieldWithExternalTableRowData, "m_bb");
    assertHasFlags(m_bb, Flags.AccPrivate);
    assertFieldType(m_bb, "java.lang.String");
    assertEquals(0, m_bb.annotations().stream().count(), "annotation count");

    assertEquals(6, tableFieldWithExternalTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData'");
    var getAa = assertMethodExist(tableFieldWithExternalTableRowData, "getAa");
    assertMethodReturnType(getAa, "java.lang.String");
    assertEquals(0, getAa.annotations().stream().count(), "annotation count");
    var setAa = assertMethodExist(tableFieldWithExternalTableRowData, "setAa", new String[]{"java.lang.String"});
    assertMethodReturnType(setAa, "void");
    assertEquals(0, setAa.annotations().stream().count(), "annotation count");
    var getXx = assertMethodExist(tableFieldWithExternalTableRowData, "getXx");
    assertMethodReturnType(getXx, "java.lang.String");
    assertEquals(0, getXx.annotations().stream().count(), "annotation count");
    var setXx = assertMethodExist(tableFieldWithExternalTableRowData, "setXx", new String[]{"java.lang.String"});
    assertMethodReturnType(setXx, "void");
    assertEquals(0, setXx.annotations().stream().count(), "annotation count");
    var getBb = assertMethodExist(tableFieldWithExternalTableRowData, "getBb");
    assertMethodReturnType(getBb, "java.lang.String");
    assertEquals(0, getBb.annotations().stream().count(), "annotation count");
    var setBb = assertMethodExist(tableFieldWithExternalTableRowData, "setBb", new String[]{"java.lang.String"});
    assertMethodReturnType(setBb, "void");
    assertEquals(0, setBb.annotations().stream().count(), "annotation count");

    assertEquals(0, tableFieldWithExternalTableRowData.innerTypes().stream().count(), "inner types count of 'TableFieldWithExternalTableRowData'");
  }
}
