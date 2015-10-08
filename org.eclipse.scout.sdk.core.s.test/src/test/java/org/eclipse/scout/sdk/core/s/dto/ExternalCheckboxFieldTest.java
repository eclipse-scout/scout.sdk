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

public class ExternalCheckboxFieldTest {

  @Test
  public void testCreateFormData() {
    String templateName = "AbstractTestCheckboxField";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield." + templateName);
    testApiOfAbstractTestCheckboxFieldData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractTestCheckboxFieldData(IType abstractTestCheckboxFieldData) {
    // type AbstractTestCheckboxFieldData
    SdkAssert.assertHasFlags(abstractTestCheckboxFieldData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractTestCheckboxFieldData, "QAbstractValueFieldData<QBoolean;>;");

    // fields of AbstractTestCheckboxFieldData
    Assert.assertEquals("field count of 'AbstractTestCheckboxFieldData'", 1, abstractTestCheckboxFieldData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractTestCheckboxFieldData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    Assert.assertEquals("method count of 'AbstractTestCheckboxFieldData'", 1, abstractTestCheckboxFieldData.methods().list().size());
    IMethod abstractTestCheckboxFieldData1 = SdkAssert.assertMethodExist(abstractTestCheckboxFieldData, "AbstractTestCheckboxFieldData", new String[]{});
    Assert.assertTrue(abstractTestCheckboxFieldData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractTestCheckboxFieldData1, null);

    Assert.assertEquals("inner types count of 'AbstractTestCheckboxFieldData'", 0, abstractTestCheckboxFieldData.innerTypes().list().size());
  }

}
