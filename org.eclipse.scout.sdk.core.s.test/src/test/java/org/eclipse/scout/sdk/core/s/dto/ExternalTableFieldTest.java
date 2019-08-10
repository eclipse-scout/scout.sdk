/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

import formdata.client.ui.template.formfield.AbstractCompanyTableField;

public class ExternalTableFieldTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(AbstractCompanyTableField.class.getName(), ExternalTableFieldTest::testApiOfAbstractCompanyTableFieldData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfAbstractCompanyTableFieldData(IType abstractCompanyTableFieldData) {
    assertHasFlags(abstractCompanyTableFieldData, 1025);
    assertHasSuperClass(abstractCompanyTableFieldData, "org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData");
    assertAnnotation(abstractCompanyTableFieldData, "javax.annotation.Generated");

    // fields of AbstractCompanyTableFieldData
    assertEquals(1, abstractCompanyTableFieldData.fields().stream().count(), "field count of 'formdata.shared.services.process.AbstractCompanyTableFieldData'");
    IField serialVersionUID = assertFieldExist(abstractCompanyTableFieldData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(7, abstractCompanyTableFieldData.methods().stream().count(), "method count of 'formdata.shared.services.process.AbstractCompanyTableFieldData'");
    IMethod addRow = assertMethodExist(abstractCompanyTableFieldData, "addRow", new String[]{});
    assertMethodReturnType(addRow, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData");
    assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = assertMethodExist(abstractCompanyTableFieldData, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData");
    assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = assertMethodExist(abstractCompanyTableFieldData, "createRow", new String[]{});
    assertMethodReturnType(createRow, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData");
    assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = assertMethodExist(abstractCompanyTableFieldData, "getRowType", new String[]{});
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = assertMethodExist(abstractCompanyTableFieldData, "getRows", new String[]{});
    assertMethodReturnType(getRows, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData[]");
    assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = assertMethodExist(abstractCompanyTableFieldData, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData");
    assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = assertMethodExist(abstractCompanyTableFieldData, "setRows", new String[]{"formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData[]"});
    assertMethodReturnType(setRows, "void");

    assertEquals(1, abstractCompanyTableFieldData.innerTypes().stream().count(), "inner types count of 'AbstractCompanyTableFieldData'");
    // type AbstractCompanyTableRowData
    IType abstractCompanyTableRowData = assertTypeExists(abstractCompanyTableFieldData, "AbstractCompanyTableRowData");
    assertHasFlags(abstractCompanyTableRowData, 1033);
    assertHasSuperClass(abstractCompanyTableRowData, "org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData");

    // fields of AbstractCompanyTableRowData
    assertEquals(3, abstractCompanyTableRowData.fields().stream().count(), "field count of 'formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData'");
    IField serialVersionUID1 = assertFieldExist(abstractCompanyTableRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    IField name = assertFieldExist(abstractCompanyTableRowData, "name");
    assertHasFlags(name, 25);
    assertFieldType(name, "java.lang.String");
    IField m_name = assertFieldExist(abstractCompanyTableRowData, "m_name");
    assertHasFlags(m_name, 2);
    assertFieldType(m_name, "java.lang.String");

    assertEquals(2, abstractCompanyTableRowData.methods().stream().count(), "method count of 'formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData'");
    IMethod getName = assertMethodExist(abstractCompanyTableRowData, "getName", new String[]{});
    assertMethodReturnType(getName, "java.lang.String");
    IMethod setName = assertMethodExist(abstractCompanyTableRowData, "setName", new String[]{"java.lang.String"});
    assertMethodReturnType(setName, "void");

    assertEquals(0, abstractCompanyTableRowData.innerTypes().stream().count(), "inner types count of 'AbstractCompanyTableRowData'");
  }
}
