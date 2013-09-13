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

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Assert;
import org.junit.Test;

public class ExternalTableFieldTest extends AbstractSdkTestWithFormDataProject {

  @Test
  public void testCreateFormData() throws Exception {
    String formName = "AbstractCompanyTableField";
    IType form = TypeUtility.getType("formdata.client.ui.template.formfield." + formName);
    Assert.assertTrue(TypeUtility.exists(form));

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataAnnotation annotation = ScoutTypeUtility.findFormDataAnnotation(form, TypeUtility.getSuperTypeHierarchy(form));
    FormDataUpdateOperation op = new FormDataUpdateOperation(form, TypeUtility.getTypeBySignature(annotation.getFormDataTypeSignature()).getCompilationUnit());
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, op);

    testApiOfAbstractCompanyTableFieldData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfAbstractCompanyTableFieldData() throws Exception {
    // type AbstractCompanyTableFieldData
    IType abstractCompanyTableFieldData = SdkAssert.assertTypeExists("formdata.shared.services.process.AbstractCompanyTableFieldData");
    SdkAssert.assertHasFlags(abstractCompanyTableFieldData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractCompanyTableFieldData, "QAbstractTableFieldData;");

    // fields of AbstractCompanyTableFieldData
    SdkAssert.assertEquals("field count of 'AbstractCompanyTableFieldData'", 2, abstractCompanyTableFieldData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractCompanyTableFieldData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    IField nAME_COLUMN_ID = SdkAssert.assertFieldExist(abstractCompanyTableFieldData, "NAME_COLUMN_ID");
    SdkAssert.assertHasFlags(nAME_COLUMN_ID, 25);
    SdkAssert.assertFieldSignature(nAME_COLUMN_ID, "I");

    SdkAssert.assertEquals("method count of 'AbstractCompanyTableFieldData'", 6, abstractCompanyTableFieldData.getMethods().length);
    IMethod abstractCompanyTableFieldData1 = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "AbstractCompanyTableFieldData", new String[]{});
    SdkAssert.assertTrue(abstractCompanyTableFieldData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractCompanyTableFieldData1, "V");
    IMethod getName = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "getName", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(getName, "QString;");
    IMethod setName = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "setName", new String[]{"I", "QString;"});
    SdkAssert.assertMethodReturnTypeSignature(setName, "V");
    IMethod getColumnCount = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "getColumnCount", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getColumnCount, "I");
    SdkAssert.assertAnnotation(getColumnCount, "java.lang.Override");
    IMethod getValueAt = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "getValueAt", new String[]{"I", "I"});
    SdkAssert.assertMethodReturnTypeSignature(getValueAt, "QObject;");
    SdkAssert.assertAnnotation(getValueAt, "java.lang.Override");
    IMethod setValueAt = SdkAssert.assertMethodExist(abstractCompanyTableFieldData, "setValueAt", new String[]{"I", "I", "QObject;"});
    SdkAssert.assertMethodReturnTypeSignature(setValueAt, "V");
    SdkAssert.assertAnnotation(setValueAt, "java.lang.Override");

    SdkAssert.assertEquals("inner types count of 'AbstractCompanyTableFieldData'", 0, abstractCompanyTableFieldData.getTypes().length);
  }

}
