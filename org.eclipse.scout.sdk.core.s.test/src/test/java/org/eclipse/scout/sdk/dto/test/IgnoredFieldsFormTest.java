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

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.dto.test.util.CoreScoutTestingUtils;
import org.junit.Assert;
import org.junit.Test;

public class IgnoredFieldsFormTest {

  @Test
  public void testCreateFormData() {
    String formName = "IgnoredFieldsForm";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms." + formName);
    testApiOfIgnoredFieldsFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfIgnoredFieldsFormData(IType ignoredFieldsFormData) {
    // type IgnoredFieldsFormData
    SdkAssert.assertHasFlags(ignoredFieldsFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(ignoredFieldsFormData, "QAbstractFormData;");

    // fields of IgnoredFieldsFormData
    Assert.assertEquals("field count of 'IgnoredFieldsFormData'", 1, ignoredFieldsFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(ignoredFieldsFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'IgnoredFieldsFormData'", 2, ignoredFieldsFormData.methods().list().size());
    IMethod ignoredFieldsFormData1 = SdkAssert.assertMethodExist(ignoredFieldsFormData, "IgnoredFieldsFormData", new String[]{});
    Assert.assertTrue(ignoredFieldsFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(ignoredFieldsFormData1, null);
    IMethod getNotIgnored = SdkAssert.assertMethodExist(ignoredFieldsFormData, "getNotIgnored", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getNotIgnored, "QNotIgnored;");

    Assert.assertEquals("inner types count of 'IgnoredFieldsFormData'", 1, ignoredFieldsFormData.innerTypes().list().size());
    // type NotIgnored
    IType notIgnored = SdkAssert.assertTypeExists(ignoredFieldsFormData, "NotIgnored");
    SdkAssert.assertHasFlags(notIgnored, 9);
    SdkAssert.assertHasSuperTypeSignature(notIgnored, "QAbstractValueFieldData<QString;>;");

    // fields of NotIgnored
    Assert.assertEquals("field count of 'NotIgnored'", 1, notIgnored.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(notIgnored, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'NotIgnored'", 1, notIgnored.methods().list().size());
    IMethod notIgnored1 = SdkAssert.assertMethodExist(notIgnored, "NotIgnored", new String[]{});
    Assert.assertTrue(notIgnored1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(notIgnored1, null);

    Assert.assertEquals("inner types count of 'NotIgnored'", 0, notIgnored.innerTypes().list().size());
  }
}
