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

public class ExternalGroupboxTest {

  @Test
  public void testCreateFormData() {
    String formName = "AbstractExternalGroupBox";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield." + formName);
    testApiOfAbstractExternalGroupBoxData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractExternalGroupBoxData(IType abstractExternalGroupBoxData) {
    // type AbstractExternalGroupBoxData
    SdkAssert.assertHasFlags(abstractExternalGroupBoxData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractExternalGroupBoxData, "QAbstractFormFieldData;");

    // fields of AbstractExternalGroupBoxData
    Assert.assertEquals("field count of 'AbstractExternalGroupBoxData'", 1, abstractExternalGroupBoxData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractExternalGroupBoxData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'AbstractExternalGroupBoxData'", 1, abstractExternalGroupBoxData.methods().list().size());
    IMethod getExternalString = SdkAssert.assertMethodExist(abstractExternalGroupBoxData, "getExternalString", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getExternalString, "QExternalString;");

    Assert.assertEquals("inner types count of 'AbstractExternalGroupBoxData'", 1, abstractExternalGroupBoxData.innerTypes().list().size());
    // type ExternalString
    IType externalString = SdkAssert.assertTypeExists(abstractExternalGroupBoxData, "ExternalString");
    SdkAssert.assertHasFlags(externalString, 9);
    SdkAssert.assertHasSuperTypeSignature(externalString, "QAbstractValueFieldData<QString;>;");

    // fields of ExternalString
    Assert.assertEquals("field count of 'ExternalString'", 1, externalString.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(externalString, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    Assert.assertEquals("method count of 'ExternalString'", 0, externalString.methods().list().size());

    Assert.assertEquals("inner types count of 'ExternalString'", 0, externalString.innerTypes().list().size());
  }
}
