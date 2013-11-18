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

    // fields of TableFieldFormData
    SdkAssert.assertEquals("field count of 'TableFieldFormData'", 1, tableFieldFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(tableFieldFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'TableFieldFormData'", 3, tableFieldFormData.getMethods().length);
    IMethod tableFieldFormData1 = SdkAssert.assertMethodExist(tableFieldFormData, "TableFieldFormData", new String[]{});
    SdkAssert.assertTrue(tableFieldFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(tableFieldFormData1, "V");
    IMethod getCompanyTable = SdkAssert.assertMethodExist(tableFieldFormData, "getCompanyTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getCompanyTable, "QCompanyTable;");
    IMethod getPersonTable = SdkAssert.assertMethodExist(tableFieldFormData, "getPersonTable", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getPersonTable, "QPersonTable;");

    SdkAssert.assertEquals("inner types count of 'TableFieldFormData'", 2, tableFieldFormData.getTypes().length);
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
    // type PersonTable
    IType personTable = SdkAssert.assertTypeExists(tableFieldFormData, "PersonTable");
    SdkAssert.assertHasFlags(personTable, 9);
    SdkAssert.assertHasSuperTypeSignature(personTable, "QAbstractTableFieldData;");

    // fields of PersonTable
    SdkAssert.assertEquals("field count of 'PersonTable'", 6, personTable.getFields().length);
    IField serialVersionUID2 = SdkAssert.assertFieldExist(personTable, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");
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
    IMethod setCustom = SdkAssert.assertMethodExist(personTable, "setCustom", new String[]{"I", "QSet<QMap<QString;QInteger;>;>;"});
    SdkAssert.assertMethodReturnTypeSignature(setCustom, "V");
    IMethod getName = SdkAssert.assertMethodExist(personTable, "getName", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(getName, "QString;");
    IMethod setName = SdkAssert.assertMethodExist(personTable, "setName", new String[]{"I", "QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setName, "V");
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

    SdkAssert.assertEquals("inner types count of 'PersonTable'", 0, personTable.getTypes().length);
  }
}
