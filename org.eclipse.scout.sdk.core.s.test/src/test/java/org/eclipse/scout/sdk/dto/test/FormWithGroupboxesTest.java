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
import org.junit.Test;

public class FormWithGroupboxesTest {

  @Test
  public void testCreateFormData() throws Exception {
    String formName = "FormWithGroupBoxesForm";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms." + formName);
    testApiOfFormWithGroupBoxesFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfFormWithGroupBoxesFormData(IType formWithGroupBoxesFormData) throws Exception {
    // type FormWithGroupBoxesFormData
    SdkAssert.assertHasFlags(formWithGroupBoxesFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(formWithGroupBoxesFormData, "QAbstractFormData;");

    // fields of FormWithGroupBoxesFormData
    SdkAssert.assertEquals("field count of 'FormWithGroupBoxesFormData'", 1, formWithGroupBoxesFormData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(formWithGroupBoxesFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'FormWithGroupBoxesFormData'", 3, formWithGroupBoxesFormData.getMethods().size());
    IMethod formWithGroupBoxesFormData1 = SdkAssert.assertMethodExist(formWithGroupBoxesFormData, "FormWithGroupBoxesFormData", new String[]{});
    SdkAssert.assertTrue(formWithGroupBoxesFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(formWithGroupBoxesFormData1, null);
    IMethod getFlatString = SdkAssert.assertMethodExist(formWithGroupBoxesFormData, "getFlatString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFlatString, "QFlatString;");
    IMethod getInnerInteger = SdkAssert.assertMethodExist(formWithGroupBoxesFormData, "getInnerInteger", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getInnerInteger, "QInnerInteger;");

    SdkAssert.assertEquals("inner types count of 'FormWithGroupBoxesFormData'", 2, formWithGroupBoxesFormData.getTypes().size());
    // type FlatString
    IType flatString = SdkAssert.assertTypeExists(formWithGroupBoxesFormData, "FlatString");
    SdkAssert.assertHasFlags(flatString, 9);
    SdkAssert.assertHasSuperTypeSignature(flatString, "QAbstractValueFieldData<QString;>;");

    // fields of FlatString
    SdkAssert.assertEquals("field count of 'FlatString'", 1, flatString.getFields().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(flatString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'FlatString'", 1, flatString.getMethods().size());
    IMethod flatString1 = SdkAssert.assertMethodExist(flatString, "FlatString", new String[]{});
    SdkAssert.assertTrue(flatString1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(flatString1, null);

    SdkAssert.assertEquals("inner types count of 'FlatString'", 0, flatString.getTypes().size());
    // type InnerInteger
    IType innerInteger = SdkAssert.assertTypeExists(formWithGroupBoxesFormData, "InnerInteger");
    SdkAssert.assertHasFlags(innerInteger, 9);
    SdkAssert.assertHasSuperTypeSignature(innerInteger, "QAbstractValueFieldData<QInteger;>;");

    // fields of InnerInteger
    SdkAssert.assertEquals("field count of 'InnerInteger'", 1, innerInteger.getFields().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(innerInteger, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");

    SdkAssert.assertEquals("method count of 'InnerInteger'", 1, innerInteger.getMethods().size());
    IMethod innerInteger1 = SdkAssert.assertMethodExist(innerInteger, "InnerInteger", new String[]{});
    SdkAssert.assertTrue(innerInteger1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(innerInteger1, null);

    SdkAssert.assertEquals("inner types count of 'InnerInteger'", 0, innerInteger.getTypes().size());
  }

}