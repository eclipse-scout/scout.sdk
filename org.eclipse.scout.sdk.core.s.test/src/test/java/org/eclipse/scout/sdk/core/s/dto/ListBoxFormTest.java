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
package org.eclipse.scout.sdk.core.s.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

public class ListBoxFormTest {

  @Test
  public void testCreateFormData() {
    String formName = "ListBoxForm";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms." + formName);
    testApiOfListBoxFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfListBoxFormData(IType listBoxFormData) {
    // type ListBoxFormData
    SdkAssert.assertHasFlags(listBoxFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(listBoxFormData, "QAbstractFormData;");
    SdkAssert.assertAnnotation(listBoxFormData, "javax.annotation.Generated");

    // fields of ListBoxFormData
    Assert.assertEquals("field count of 'ListBoxFormData'", 1, listBoxFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(listBoxFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'ListBoxFormData'", 1, listBoxFormData.methods().list().size());
    IMethod getListBox = SdkAssert.assertMethodExist(listBoxFormData, "getListBox", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getListBox, "QListBox;");

    Assert.assertEquals("inner types count of 'ListBoxFormData'", 1, listBoxFormData.innerTypes().list().size());
    // type ListBox
    IType listBox = SdkAssert.assertTypeExists(listBoxFormData, "ListBox");
    SdkAssert.assertHasFlags(listBox, 9);
    SdkAssert.assertHasSuperTypeSignature(listBox, "QAbstractValueFieldData<QSet<QLong;>;>;");

    // fields of ListBox
    Assert.assertEquals("field count of 'ListBox'", 1, listBox.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(listBox, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'ListBox'", 0, listBox.methods().list().size());

    Assert.assertEquals("inner types count of 'ListBox'", 0, listBox.innerTypes().list().size());
  }
}
