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
package org.eclipse.scout.sdk.dto.test;

import org.eclipse.scout.sdk.core.model.IField;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.dto.test.util.CoreScoutTestingUtils;
import org.junit.Assert;
import org.junit.Test;

public class ExternalTableFieldTest {

  @Test
  public void testCreateFormData() {
    String formName = "AbstractCompanyTableField";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield." + formName);
    testApiOfAbstractCompanyTableFieldData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfAbstractCompanyTableFieldData(IType abstractCompanyTableFieldData) {
    SdkAssert.assertHasFlags(abstractCompanyTableFieldData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractCompanyTableFieldData, "Lorg.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;");
    SdkAssert.assertAnnotation(abstractCompanyTableFieldData, "javax.annotation.Generated");

    // fields of AbstractCompanyTableFieldData
    Assert.assertEquals("field count of 'formdata.shared.services.process.AbstractCompanyTableFieldData'", 1, abstractCompanyTableFieldData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractCompanyTableFieldData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'formdata.shared.services.process.AbstractCompanyTableFieldData'", 8, abstractCompanyTableFieldData.getMethods().size());
    IMethod abstractCompanyTableFieldData1 = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "AbstractCompanyTableFieldData", new String[]{});
    Assert.assertTrue(abstractCompanyTableFieldData1.isConstructor());
    IMethod addRow = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "Lformdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData;");
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "Lformdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData;");
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "Lformdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData;");
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "Ljava.lang.Class<+Lorg.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;>;");
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[Lformdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData;");
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "Lformdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData;");
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "setRows", new String[]{"[Lformdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");

    Assert.assertEquals("inner types count of 'AbstractCompanyTableFieldData'", 1, abstractCompanyTableFieldData.getTypes().size());
    // type AbstractCompanyTableRowData
    IType abstractCompanyTableRowData = SdkAssert.assertTypeExists(abstractCompanyTableFieldData, "AbstractCompanyTableRowData");
    SdkAssert.assertHasFlags(abstractCompanyTableRowData, 1033);
    SdkAssert.assertHasSuperTypeSignature(abstractCompanyTableRowData, "Lorg.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;");

    // fields of AbstractCompanyTableRowData
    Assert.assertEquals("field count of 'formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData'", 3, abstractCompanyTableRowData.getFields().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(abstractCompanyTableRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    IField name = SdkAssert.assertFieldExist(abstractCompanyTableRowData, "name");
    SdkAssert.assertHasFlags(name, 25);
    SdkAssert.assertFieldSignature(name, "Ljava.lang.String;");
    IField m_name = SdkAssert.assertFieldExist(abstractCompanyTableRowData, "m_name");
    SdkAssert.assertHasFlags(m_name, 2);
    SdkAssert.assertFieldSignature(m_name, "Ljava.lang.String;");

    Assert.assertEquals("method count of 'formdata.shared.services.process.AbstractCompanyTableFieldData$AbstractCompanyTableRowData'", 3, abstractCompanyTableRowData.getMethods().size());
    IMethod abstractCompanyTableRowData1 = SdkAssert.assertMethodExist(abstractCompanyTableRowData, "AbstractCompanyTableRowData", new String[]{});
    Assert.assertTrue(abstractCompanyTableRowData1.isConstructor());
    IMethod getName = SdkAssert.assertMethodExist(abstractCompanyTableRowData, "getName", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getName, "Ljava.lang.String;");
    IMethod setName = SdkAssert.assertMethodExist(abstractCompanyTableRowData, "setName", new String[]{"Ljava.lang.String;"});
    SdkAssert.assertMethodReturnTypeSignature(setName, "V");

    Assert.assertEquals("inner types count of 'AbstractCompanyTableRowData'", 0, abstractCompanyTableRowData.getTypes().size());
  }
}
