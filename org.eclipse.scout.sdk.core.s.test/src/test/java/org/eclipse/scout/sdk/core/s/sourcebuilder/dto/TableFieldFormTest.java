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
package org.eclipse.scout.sdk.core.s.sourcebuilder.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

public class TableFieldFormTest {

  @Test
  public void testCreateFormData() {
    String formName = "TableFieldForm";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms." + formName);

    testApiOfTableFieldFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfTableFieldFormData(IType tableFieldFormData) {
    SdkAssert.assertHasFlags(tableFieldFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(tableFieldFormData, "Lorg.eclipse.scout.rt.shared.data.form.AbstractFormData;");
    SdkAssert.assertAnnotation(tableFieldFormData, "javax.annotation.Generated");

    // fields of TableFieldFormData
    Assert.assertEquals("field count of 'formdata.shared.services.process.TableFieldFormData'", 1, tableFieldFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(tableFieldFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'formdata.shared.services.process.TableFieldFormData'", 4, tableFieldFormData.methods().list().size());
    IMethod getCompanyTable = SdkAssert.assertMethodExist(tableFieldFormData, "getCompanyTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getCompanyTable, "Lformdata.shared.services.process.TableFieldFormData$CompanyTable;");
    IMethod getConcreteTable = SdkAssert.assertMethodExist(tableFieldFormData, "getConcreteTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getConcreteTable, "Lformdata.shared.services.process.TableFieldFormData$ConcreteTable;");
    IMethod getPersonTable = SdkAssert.assertMethodExist(tableFieldFormData, "getPersonTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getPersonTable, "Lformdata.shared.services.process.TableFieldFormData$PersonTable;");
    IMethod getTableFieldWithExternalTable = SdkAssert.assertMethodExist(tableFieldFormData, "getTableFieldWithExternalTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTableFieldWithExternalTable, "Lformdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable;");

    Assert.assertEquals("inner types count of 'TableFieldFormData'", 4, tableFieldFormData.innerTypes().list().size());
    // type CompanyTable
    IType companyTable = SdkAssert.assertTypeExists(tableFieldFormData, "CompanyTable");
    SdkAssert.assertHasFlags(companyTable, 9);
    SdkAssert.assertHasSuperTypeSignature(companyTable, "Lformdata.shared.services.process.AbstractCompanyTableFieldData;");

    // fields of CompanyTable
    Assert.assertEquals("field count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable'", 1, companyTable.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(companyTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable'", 7, companyTable.methods().list().size());
    IMethod addRow = SdkAssert.assertMethodExist(companyTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "Lformdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(companyTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "Lformdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(companyTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "Lformdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(companyTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "Ljava.lang.Class<+Lorg.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(companyTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[Lformdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(companyTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "Lformdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(companyTable, "setRows", new String[]{"[Lformdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    Assert.assertEquals("inner types count of 'CompanyTable'", 1, companyTable.innerTypes().list().size());
    // type CompanyTableRowData
    IType companyTableRowData = SdkAssert.assertTypeExists(companyTable, "CompanyTableRowData");
    SdkAssert.assertHasFlags(companyTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(companyTableRowData, "Lformdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData;");

    // fields of CompanyTableRowData
    Assert.assertEquals("field count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData'", 1, companyTableRowData.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(companyTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'formdata.shared.services.process.TableFieldFormData$CompanyTable$CompanyTableRowData'", 0, companyTableRowData.methods().list().size());

    Assert.assertEquals("inner types count of 'CompanyTableRowData'", 0, companyTableRowData.innerTypes().list().size());
    // type ConcreteTable
    IType concreteTable = SdkAssert.assertTypeExists(tableFieldFormData, "ConcreteTable");
    SdkAssert.assertHasFlags(concreteTable, 9);
    SdkAssert.assertHasSuperTypeSignature(concreteTable, "Lorg.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;");

    // fields of ConcreteTable
    Assert.assertEquals("field count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable'", 1, concreteTable.fields().list().size());
    IField serialVersionUID3 = SdkAssert.assertFieldExist(concreteTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID3, 26);
    SdkAssert.assertFieldSignature(serialVersionUID3, "J");

    Assert.assertEquals("method count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable'", 7, concreteTable.methods().list().size());
    IMethod addRow2 = SdkAssert.assertMethodExist(concreteTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow2, "Lformdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData;");
    SdkAssert.assertAnnotation(addRow2, "java.lang.Override");
    IMethod addRow3 = SdkAssert.assertMethodExist(concreteTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow3, "Lformdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData;");
    SdkAssert.assertAnnotation(addRow3, "java.lang.Override");
    IMethod createRow1 = SdkAssert.assertMethodExist(concreteTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow1, "Lformdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData;");
    SdkAssert.assertAnnotation(createRow1, "java.lang.Override");
    IMethod getRowType1 = SdkAssert.assertMethodExist(concreteTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType1, "Ljava.lang.Class<+Lorg.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType1, "java.lang.Override");
    IMethod getRows1 = SdkAssert.assertMethodExist(concreteTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows1, "[Lformdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData;");
    SdkAssert.assertAnnotation(getRows1, "java.lang.Override");
    IMethod rowAt1 = SdkAssert.assertMethodExist(concreteTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt1, "Lformdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData;");
    SdkAssert.assertAnnotation(rowAt1, "java.lang.Override");
    IMethod setRows1 = SdkAssert.assertMethodExist(concreteTable, "setRows", new String[]{"[Lformdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows1, "V");

    Assert.assertEquals("inner types count of 'ConcreteTable'", 1, concreteTable.innerTypes().list().size());
    // type ConcreteTableRowData
    IType concreteTableRowData = SdkAssert.assertTypeExists(concreteTable, "ConcreteTableRowData");
    SdkAssert.assertHasFlags(concreteTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(concreteTableRowData, "Lorg.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;");

    // fields of ConcreteTableRowData
    Assert.assertEquals("field count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData'", 5, concreteTableRowData.fields().list().size());
    IField serialVersionUID4 = SdkAssert.assertFieldExist(concreteTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID4, 26);
    SdkAssert.assertFieldSignature(serialVersionUID4, "J");
    IField name = SdkAssert.assertFieldExist(concreteTableRowData, "name");
    SdkAssert.assertHasFlags(name, 25);
    SdkAssert.assertFieldSignature(name, "Ljava.lang.String;");
    IField extKey = SdkAssert.assertFieldExist(concreteTableRowData, "extKey");
    SdkAssert.assertHasFlags(extKey, 25);
    SdkAssert.assertFieldSignature(extKey, "Ljava.lang.String;");
    IField m_name = SdkAssert.assertFieldExist(concreteTableRowData, "m_name");
    SdkAssert.assertHasFlags(m_name, 2);
    SdkAssert.assertFieldSignature(m_name, "Ljava.lang.String;");
    IField m_extKey = SdkAssert.assertFieldExist(concreteTableRowData, "m_extKey");
    SdkAssert.assertHasFlags(m_extKey, 2);
    SdkAssert.assertFieldSignature(m_extKey, "Ljava.lang.Integer;");

    Assert.assertEquals("method count of 'formdata.shared.services.process.TableFieldFormData$ConcreteTable$ConcreteTableRowData'", 4, concreteTableRowData.methods().list().size());
    IMethod getName = SdkAssert.assertMethodExist(concreteTableRowData, "getName", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getName, "Ljava.lang.String;");
    IMethod setName = SdkAssert.assertMethodExist(concreteTableRowData, "setName", new String[]{"Ljava.lang.String;"});
    SdkAssert.assertMethodReturnTypeSignature(setName, "V");
    IMethod getExtKey = SdkAssert.assertMethodExist(concreteTableRowData, "getExtKey", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getExtKey, "Ljava.lang.Integer;");
    IMethod setExtKey = SdkAssert.assertMethodExist(concreteTableRowData, "setExtKey", new String[]{"Ljava.lang.Integer;"});
    SdkAssert.assertMethodReturnTypeSignature(setExtKey, "V");

    Assert.assertEquals("inner types count of 'ConcreteTableRowData'", 0, concreteTableRowData.innerTypes().list().size());
    // type PersonTable
    IType personTable = SdkAssert.assertTypeExists(tableFieldFormData, "PersonTable");
    SdkAssert.assertHasFlags(personTable, 9);
    SdkAssert.assertHasSuperTypeSignature(personTable, "Lorg.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;");

    // fields of PersonTable
    Assert.assertEquals("field count of 'formdata.shared.services.process.TableFieldFormData$PersonTable'", 1, personTable.fields().list().size());
    IField serialVersionUID5 = SdkAssert.assertFieldExist(personTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID5, 26);
    SdkAssert.assertFieldSignature(serialVersionUID5, "J");

    Assert.assertEquals("method count of 'formdata.shared.services.process.TableFieldFormData$PersonTable'", 7, personTable.methods().list().size());
    IMethod addRow4 = SdkAssert.assertMethodExist(personTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow4, "Lformdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData;");
    SdkAssert.assertAnnotation(addRow4, "java.lang.Override");
    IMethod addRow5 = SdkAssert.assertMethodExist(personTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow5, "Lformdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData;");
    SdkAssert.assertAnnotation(addRow5, "java.lang.Override");
    IMethod createRow2 = SdkAssert.assertMethodExist(personTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow2, "Lformdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData;");
    SdkAssert.assertAnnotation(createRow2, "java.lang.Override");
    IMethod getRowType2 = SdkAssert.assertMethodExist(personTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType2, "Ljava.lang.Class<+Lorg.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType2, "java.lang.Override");
    IMethod getRows2 = SdkAssert.assertMethodExist(personTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows2, "[Lformdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData;");
    SdkAssert.assertAnnotation(getRows2, "java.lang.Override");
    IMethod rowAt2 = SdkAssert.assertMethodExist(personTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt2, "Lformdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData;");
    SdkAssert.assertAnnotation(rowAt2, "java.lang.Override");
    IMethod setRows2 = SdkAssert.assertMethodExist(personTable, "setRows", new String[]{"[Lformdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows2, "V");

    Assert.assertEquals("inner types count of 'PersonTable'", 1, personTable.innerTypes().list().size());
    // type PersonTableRowData
    IType personTableRowData = SdkAssert.assertTypeExists(personTable, "PersonTableRowData");
    SdkAssert.assertHasFlags(personTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(personTableRowData, "Lorg.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;");

    // fields of PersonTableRowData
    Assert.assertEquals("field count of 'formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData'", 11, personTableRowData.fields().list().size());
    IField serialVersionUID6 = SdkAssert.assertFieldExist(personTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID6, 26);
    SdkAssert.assertFieldSignature(serialVersionUID6, "J");
    IField personNr = SdkAssert.assertFieldExist(personTableRowData, "personNr");
    SdkAssert.assertHasFlags(personNr, 25);
    SdkAssert.assertFieldSignature(personNr, "Ljava.lang.String;");
    IField name1 = SdkAssert.assertFieldExist(personTableRowData, "name");
    SdkAssert.assertHasFlags(name1, 25);
    SdkAssert.assertFieldSignature(name1, "Ljava.lang.String;");
    IField anObject = SdkAssert.assertFieldExist(personTableRowData, "anObject");
    SdkAssert.assertHasFlags(anObject, 25);
    SdkAssert.assertFieldSignature(anObject, "Ljava.lang.String;");
    IField smartLong = SdkAssert.assertFieldExist(personTableRowData, "smartLong");
    SdkAssert.assertHasFlags(smartLong, 25);
    SdkAssert.assertFieldSignature(smartLong, "Ljava.lang.String;");
    IField custom = SdkAssert.assertFieldExist(personTableRowData, "custom");
    SdkAssert.assertHasFlags(custom, 25);
    SdkAssert.assertFieldSignature(custom, "Ljava.lang.String;");
    IField m_personNr = SdkAssert.assertFieldExist(personTableRowData, "m_personNr");
    SdkAssert.assertHasFlags(m_personNr, 2);
    SdkAssert.assertFieldSignature(m_personNr, "Ljava.lang.Long;");
    IField m_name1 = SdkAssert.assertFieldExist(personTableRowData, "m_name");
    SdkAssert.assertHasFlags(m_name1, 2);
    SdkAssert.assertFieldSignature(m_name1, "Ljava.lang.String;");
    IField m_anObject = SdkAssert.assertFieldExist(personTableRowData, "m_anObject");
    SdkAssert.assertHasFlags(m_anObject, 2);
    SdkAssert.assertFieldSignature(m_anObject, "Ljava.lang.Object;");
    IField m_smartLong = SdkAssert.assertFieldExist(personTableRowData, "m_smartLong");
    SdkAssert.assertHasFlags(m_smartLong, 2);
    SdkAssert.assertFieldSignature(m_smartLong, "Ljava.lang.Long;");
    IField m_custom = SdkAssert.assertFieldExist(personTableRowData, "m_custom");
    SdkAssert.assertHasFlags(m_custom, 2);
    SdkAssert.assertFieldSignature(m_custom, "Ljava.util.Set<Ljava.util.Map<Ljava.lang.String;Ljava.lang.Integer;>;>;");

    Assert.assertEquals("method count of 'formdata.shared.services.process.TableFieldFormData$PersonTable$PersonTableRowData'", 10, personTableRowData.methods().list().size());
    IMethod getPersonNr = SdkAssert.assertMethodExist(personTableRowData, "getPersonNr", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getPersonNr, "Ljava.lang.Long;");
    IMethod setPersonNr = SdkAssert.assertMethodExist(personTableRowData, "setPersonNr", new String[]{"Ljava.lang.Long;"});
    SdkAssert.assertMethodReturnTypeSignature(setPersonNr, "V");
    IMethod getName1 = SdkAssert.assertMethodExist(personTableRowData, "getName", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getName1, "Ljava.lang.String;");
    IMethod setName1 = SdkAssert.assertMethodExist(personTableRowData, "setName", new String[]{"Ljava.lang.String;"});
    SdkAssert.assertMethodReturnTypeSignature(setName1, "V");
    IMethod getAnObject = SdkAssert.assertMethodExist(personTableRowData, "getAnObject", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getAnObject, "Ljava.lang.Object;");
    IMethod setAnObject = SdkAssert.assertMethodExist(personTableRowData, "setAnObject", new String[]{"Ljava.lang.Object;"});
    SdkAssert.assertMethodReturnTypeSignature(setAnObject, "V");
    IMethod getSmartLong = SdkAssert.assertMethodExist(personTableRowData, "getSmartLong", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getSmartLong, "Ljava.lang.Long;");
    IMethod setSmartLong = SdkAssert.assertMethodExist(personTableRowData, "setSmartLong", new String[]{"Ljava.lang.Long;"});
    SdkAssert.assertMethodReturnTypeSignature(setSmartLong, "V");
    IMethod getCustom = SdkAssert.assertMethodExist(personTableRowData, "getCustom", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getCustom, "Ljava.util.Set<Ljava.util.Map<Ljava.lang.String;Ljava.lang.Integer;>;>;");
    IMethod setCustom = SdkAssert.assertMethodExist(personTableRowData, "setCustom", new String[]{"Ljava.util.Set<Ljava.util.Map<Ljava.lang.String;Ljava.lang.Integer;>;>;"});
    SdkAssert.assertMethodReturnTypeSignature(setCustom, "V");

    Assert.assertEquals("inner types count of 'PersonTableRowData'", 0, personTableRowData.innerTypes().list().size());
    // type TableFieldWithExternalTable
    IType tableFieldWithExternalTable = SdkAssert.assertTypeExists(tableFieldFormData, "TableFieldWithExternalTable");
    SdkAssert.assertHasFlags(tableFieldWithExternalTable, 9);
    SdkAssert.assertHasSuperTypeSignature(tableFieldWithExternalTable, "Lorg.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;");

    // fields of TableFieldWithExternalTable
    Assert.assertEquals("field count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable'", 1, tableFieldWithExternalTable.fields().list().size());
    IField serialVersionUID7 = SdkAssert.assertFieldExist(tableFieldWithExternalTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID7, 26);
    SdkAssert.assertFieldSignature(serialVersionUID7, "J");

    Assert.assertEquals("method count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable'", 7, tableFieldWithExternalTable.methods().list().size());
    IMethod addRow6 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow6, "Lformdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData;");
    SdkAssert.assertAnnotation(addRow6, "java.lang.Override");
    IMethod addRow7 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow7, "Lformdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData;");
    SdkAssert.assertAnnotation(addRow7, "java.lang.Override");
    IMethod createRow3 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow3, "Lformdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData;");
    SdkAssert.assertAnnotation(createRow3, "java.lang.Override");
    IMethod getRowType3 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType3, "Ljava.lang.Class<+Lorg.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType3, "java.lang.Override");
    IMethod getRows3 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows3, "[Lformdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData;");
    SdkAssert.assertAnnotation(getRows3, "java.lang.Override");
    IMethod rowAt3 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt3, "Lformdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData;");
    SdkAssert.assertAnnotation(rowAt3, "java.lang.Override");
    IMethod setRows3 = SdkAssert.assertMethodExist(tableFieldWithExternalTable, "setRows", new String[]{"[Lformdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows3, "V");

    Assert.assertEquals("inner types count of 'TableFieldWithExternalTable'", 1, tableFieldWithExternalTable.innerTypes().list().size());
    // type TableFieldWithExternalTableRowData
    IType tableFieldWithExternalTableRowData = SdkAssert.assertTypeExists(tableFieldWithExternalTable, "TableFieldWithExternalTableRowData");
    SdkAssert.assertHasFlags(tableFieldWithExternalTableRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(tableFieldWithExternalTableRowData, "Lorg.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;");

    // fields of TableFieldWithExternalTableRowData
    Assert.assertEquals("field count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData'", 7, tableFieldWithExternalTableRowData.fields().list().size());
    IField serialVersionUID8 = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID8, 26);
    SdkAssert.assertFieldSignature(serialVersionUID8, "J");
    IField aa = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "aa");
    SdkAssert.assertHasFlags(aa, 25);
    SdkAssert.assertFieldSignature(aa, "Ljava.lang.String;");
    IField xx = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "xx");
    SdkAssert.assertHasFlags(xx, 25);
    SdkAssert.assertFieldSignature(xx, "Ljava.lang.String;");
    IField bb = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "bb");
    SdkAssert.assertHasFlags(bb, 25);
    SdkAssert.assertFieldSignature(bb, "Ljava.lang.String;");
    IField m_aa = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "m_aa");
    SdkAssert.assertHasFlags(m_aa, 2);
    SdkAssert.assertFieldSignature(m_aa, "Ljava.lang.String;");
    IField m_xx = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "m_xx");
    SdkAssert.assertHasFlags(m_xx, 2);
    SdkAssert.assertFieldSignature(m_xx, "Ljava.lang.String;");
    IField m_bb = SdkAssert.assertFieldExist(tableFieldWithExternalTableRowData, "m_bb");
    SdkAssert.assertHasFlags(m_bb, 2);
    SdkAssert.assertFieldSignature(m_bb, "Ljava.lang.String;");

    Assert.assertEquals("method count of 'formdata.shared.services.process.TableFieldFormData$TableFieldWithExternalTable$TableFieldWithExternalTableRowData'", 6, tableFieldWithExternalTableRowData.methods().list().size());
    IMethod getAa = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "getAa", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getAa, "Ljava.lang.String;");
    IMethod setAa = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "setAa", new String[]{"Ljava.lang.String;"});
    SdkAssert.assertMethodReturnTypeSignature(setAa, "V");
    IMethod getXx = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "getXx", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getXx, "Ljava.lang.String;");
    IMethod setXx = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "setXx", new String[]{"Ljava.lang.String;"});
    SdkAssert.assertMethodReturnTypeSignature(setXx, "V");
    IMethod getBb = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "getBb", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getBb, "Ljava.lang.String;");
    IMethod setBb = SdkAssert.assertMethodExist(tableFieldWithExternalTableRowData, "setBb", new String[]{"Ljava.lang.String;"});
    SdkAssert.assertMethodReturnTypeSignature(setBb, "V");

    Assert.assertEquals("inner types count of 'TableFieldWithExternalTableRowData'", 0, tableFieldWithExternalTableRowData.innerTypes().list().size());
  }

}
