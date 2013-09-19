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
import org.eclipse.scout.sdk.internal.workspace.dto.FormDataDtoUpdateOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

public class ListBoxFormTest extends AbstractSdkTestWithFormDataProject {

  @Test
  public void testCreateFormData() throws Exception {
    String formName = "ListBoxForm";
    IType form = TypeUtility.getType("formdata.client.ui.forms." + formName);
    Assert.assertTrue(TypeUtility.exists(form));

    IProject sharedProject = getProject("formdata.shared");
    Assert.assertNotNull(sharedProject);

    FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(form);
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, op);

    testApiOfListBoxFormData();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfListBoxFormData() throws Exception {
    // type ListBoxFormData
    IType listBoxFormData = SdkAssert.assertTypeExists("formdata.shared.services.process.ListBoxFormData");
    SdkAssert.assertHasFlags(listBoxFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(listBoxFormData, "QAbstractFormData;");

    // fields of ListBoxFormData
    SdkAssert.assertEquals("field count of 'ListBoxFormData'", 1, listBoxFormData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(listBoxFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'ListBoxFormData'", 2, listBoxFormData.getMethods().length);
    IMethod listBoxFormData1 = SdkAssert.assertMethodExist(listBoxFormData, "ListBoxFormData", new String[]{});
    SdkAssert.assertTrue(listBoxFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(listBoxFormData1, "V");
    IMethod getListBox = SdkAssert.assertMethodExist(listBoxFormData, "getListBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getListBox, "QListBox;");

    SdkAssert.assertEquals("inner types count of 'ListBoxFormData'", 1, listBoxFormData.getTypes().length);
    // type ListBox
    IType listBox = SdkAssert.assertTypeExists(listBoxFormData, "ListBox");
    SdkAssert.assertHasFlags(listBox, 9);
    SdkAssert.assertHasSuperTypeSignature(listBox, "QAbstractValueFieldData<[QLong;>;");

    // fields of ListBox
    SdkAssert.assertEquals("field count of 'ListBox'", 1, listBox.getFields().length);
    IField serialVersionUID1 = SdkAssert.assertFieldExist(listBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'ListBox'", 1, listBox.getMethods().length);
    IMethod listBox1 = SdkAssert.assertMethodExist(listBox, "ListBox", new String[]{});
    SdkAssert.assertTrue(listBox1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(listBox1, "V");

    SdkAssert.assertEquals("inner types count of 'ListBox'", 0, listBox.getTypes().length);
  }

}
