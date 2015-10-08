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
package org.eclipse.scout.sdk.core.s.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

public class FormWithGroupboxesTest {

  @Test
  public void testCreateFormData() {
    String formName = "FormWithGroupBoxesForm";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms." + formName);
    testApiOfFormWithGroupBoxesFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfFormWithGroupBoxesFormData(IType formWithGroupBoxesFormData) {
    // type FormWithGroupBoxesFormData
    SdkAssert.assertHasFlags(formWithGroupBoxesFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(formWithGroupBoxesFormData, "QAbstractFormData;");

    // fields of FormWithGroupBoxesFormData
    Assert.assertEquals("field count of 'FormWithGroupBoxesFormData'", 1, formWithGroupBoxesFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(formWithGroupBoxesFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'FormWithGroupBoxesFormData'", 3, formWithGroupBoxesFormData.methods().list().size());
    IMethod formWithGroupBoxesFormData1 = SdkAssert.assertMethodExist(formWithGroupBoxesFormData, "FormWithGroupBoxesFormData", new String[]{});
    Assert.assertTrue(formWithGroupBoxesFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(formWithGroupBoxesFormData1, null);
    IMethod getFlatString = SdkAssert.assertMethodExist(formWithGroupBoxesFormData, "getFlatString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFlatString, "QFlatString;");
    IMethod getInnerInteger = SdkAssert.assertMethodExist(formWithGroupBoxesFormData, "getInnerInteger", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getInnerInteger, "QInnerInteger;");

    Assert.assertEquals("inner types count of 'FormWithGroupBoxesFormData'", 2, formWithGroupBoxesFormData.innerTypes().list().size());
    // type FlatString
    IType flatString = SdkAssert.assertTypeExists(formWithGroupBoxesFormData, "FlatString");
    SdkAssert.assertHasFlags(flatString, 9);
    SdkAssert.assertHasSuperTypeSignature(flatString, "QAbstractValueFieldData<QString;>;");

    // fields of FlatString
    Assert.assertEquals("field count of 'FlatString'", 1, flatString.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(flatString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'FlatString'", 1, flatString.methods().list().size());
    IMethod flatString1 = SdkAssert.assertMethodExist(flatString, "FlatString", new String[]{});
    Assert.assertTrue(flatString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(flatString1, null);

    Assert.assertEquals("inner types count of 'FlatString'", 0, flatString.innerTypes().list().size());
    // type InnerInteger
    IType innerInteger = SdkAssert.assertTypeExists(formWithGroupBoxesFormData, "InnerInteger");
    SdkAssert.assertHasFlags(innerInteger, 9);
    SdkAssert.assertHasSuperTypeSignature(innerInteger, "QAbstractValueFieldData<QInteger;>;");

    // fields of InnerInteger
    Assert.assertEquals("field count of 'InnerInteger'", 1, innerInteger.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(innerInteger, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    Assert.assertEquals("method count of 'InnerInteger'", 1, innerInteger.methods().list().size());
    IMethod innerInteger1 = SdkAssert.assertMethodExist(innerInteger, "InnerInteger", new String[]{});
    Assert.assertTrue(innerInteger1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(innerInteger1, null);

    Assert.assertEquals("inner types count of 'InnerInteger'", 0, innerInteger.innerTypes().list().size());
  }

}
