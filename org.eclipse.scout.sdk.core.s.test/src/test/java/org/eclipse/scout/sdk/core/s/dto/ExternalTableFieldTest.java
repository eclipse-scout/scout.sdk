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

public class ExternalTableFieldTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield.AbstractCompanyTableField", ExternalTableFieldTest::testApiOfAbstractCompanyTableFieldData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfAbstractCompanyTableFieldData(IType abstractCompanyTableFieldData) {
    var scoutApi = abstractCompanyTableFieldData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(abstractCompanyTableFieldData, Flags.AccPublic | Flags.AccAbstract);
    assertHasSuperClass(abstractCompanyTableFieldData, scoutApi.AbstractTableFieldBeanData());
    assertEquals(1, abstractCompanyTableFieldData.annotations().stream().count(), "annotation count");
    assertAnnotation(abstractCompanyTableFieldData, scoutApi.Generated());

    // fields of AbstractCompanyTableFieldData
    assertEquals(1, abstractCompanyTableFieldData.fields().stream().count(), "field count of 'formdata.shared.services.process.AbstractCompanyTableFieldData'");
    var serialVersionUID = assertFieldExist(abstractCompanyTableFieldData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(7, abstractCompanyTableFieldData.methods().stream().count(), "method count of 'formdata.shared.services.process.AbstractCompanyTableFieldData'");
    var addRow = assertMethodExist(abstractCompanyTableFieldData, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(abstractCompanyTableFieldData, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(abstractCompanyTableFieldData, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(abstractCompanyTableFieldData, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(abstractCompanyTableFieldData, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(abstractCompanyTableFieldData, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(abstractCompanyTableFieldData, "setRows", new String[]{"formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, abstractCompanyTableFieldData.innerTypes().stream().count(), "inner types count of 'AbstractCompanyTableFieldData'");
    // type AbstractCompanyTableRowData
    var abstractCompanyTableRowData = assertTypeExists(abstractCompanyTableFieldData, "AbstractCompanyTableRowData");
    assertHasFlags(abstractCompanyTableRowData, Flags.AccPublic | Flags.AccStatic | Flags.AccAbstract);
    assertHasSuperClass(abstractCompanyTableRowData, scoutApi.AbstractTableRowData());
    assertEquals(0, abstractCompanyTableRowData.annotations().stream().count(), "annotation count");

    // fields of AbstractCompanyTableRowData
    assertEquals(3, abstractCompanyTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData'");
    var serialVersionUID1 = assertFieldExist(abstractCompanyTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");
    var name = assertFieldExist(abstractCompanyTableRowData, "name");
    assertHasFlags(name, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(name, "java.lang.String");
    assertEquals(0, name.annotations().stream().count(), "annotation count");
    var m_name = assertFieldExist(abstractCompanyTableRowData, "m_name");
    assertHasFlags(m_name, Flags.AccPrivate);
    assertFieldType(m_name, "java.lang.String");
    assertEquals(0, m_name.annotations().stream().count(), "annotation count");

    assertEquals(2, abstractCompanyTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData'");
    var getName = assertMethodExist(abstractCompanyTableRowData, "getName");
    assertMethodReturnType(getName, "java.lang.String");
    assertEquals(0, getName.annotations().stream().count(), "annotation count");
    var setName = assertMethodExist(abstractCompanyTableRowData, "setName", new String[]{"java.lang.String"});
    assertMethodReturnType(setName, "void");
    assertEquals(0, setName.annotations().stream().count(), "annotation count");

    assertEquals(0, abstractCompanyTableRowData.innerTypes().stream().count(), "inner types count of 'AbstractCompanyTableRowData'");
  }
}
