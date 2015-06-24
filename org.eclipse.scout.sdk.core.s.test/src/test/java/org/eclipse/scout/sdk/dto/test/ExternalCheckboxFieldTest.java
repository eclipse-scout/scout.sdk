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

public class ExternalCheckboxFieldTest {

  @Test
  public void testCreateFormData() throws Exception {
    String templateName = "AbstractTestCheckboxField";
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.template.formfield." + templateName);
    testApiOfAbstractTestCheckboxFieldData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfAbstractTestCheckboxFieldData(IType abstractTestCheckboxFieldData) throws Exception {
    // type AbstractTestCheckboxFieldData
    SdkAssert.assertHasFlags(abstractTestCheckboxFieldData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractTestCheckboxFieldData, "QAbstractValueFieldData<QBoolean;>;");

    // fields of AbstractTestCheckboxFieldData
    SdkAssert.assertEquals("field count of 'AbstractTestCheckboxFieldData'", 1, abstractTestCheckboxFieldData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractTestCheckboxFieldData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'AbstractTestCheckboxFieldData'", 1, abstractTestCheckboxFieldData.getMethods().size());
    IMethod abstractTestCheckboxFieldData1 = SdkAssert.assertMethodExist(abstractTestCheckboxFieldData, "AbstractTestCheckboxFieldData", new String[]{});
    SdkAssert.assertTrue(abstractTestCheckboxFieldData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractTestCheckboxFieldData1, null);

    SdkAssert.assertEquals("inner types count of 'AbstractTestCheckboxFieldData'", 0, abstractTestCheckboxFieldData.getTypes().size());
  }

}
