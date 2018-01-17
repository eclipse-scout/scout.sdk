/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.sourcebuilder.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

import formdata.client.ui.forms.formfieldmenu.FormFieldMenuTestForm;

/**
 * @since 7.1.0
 */
public class FormFieldMenuTest {
  @Test
  public void testCreateFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(FormFieldMenuTestForm.class.getName());
    testApiOfFormFieldMenuTestFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfFormFieldMenuTestFormData(IType formFieldMenuTestFormData) {
    SdkAssert.assertHasFlags(formFieldMenuTestFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(formFieldMenuTestFormData, "Lorg.eclipse.scout.rt.shared.data.form.AbstractFormData;");
    Assert.assertEquals("annotation count", 1, formFieldMenuTestFormData.annotations().list().size());
    SdkAssert.assertAnnotation(formFieldMenuTestFormData, "javax.annotation.Generated");

    // fields of FormFieldMenuTestFormData
    Assert.assertEquals("field count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData'", 1, formFieldMenuTestFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(formFieldMenuTestFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData'", 1, formFieldMenuTestFormData.methods().list().size());
    IMethod getTestBoolean = SdkAssert.assertMethodExist(formFieldMenuTestFormData, "getTestBoolean", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getTestBoolean, "Lformdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean;");
    Assert.assertEquals("annotation count", 0, getTestBoolean.annotations().list().size());

    Assert.assertEquals("inner types count of 'FormFieldMenuTestFormData'", 1, formFieldMenuTestFormData.innerTypes().list().size());
    // type TestBoolean
    IType testBoolean = SdkAssert.assertTypeExists(formFieldMenuTestFormData, "TestBoolean");
    SdkAssert.assertHasFlags(testBoolean, 9);
    SdkAssert.assertHasSuperTypeSignature(testBoolean, "Lorg.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<Ljava.lang.Boolean;>;");
    Assert.assertEquals("annotation count", 0, testBoolean.annotations().list().size());

    // fields of TestBoolean
    Assert.assertEquals("field count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean'", 1, testBoolean.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(testBoolean, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID1.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean'", 0, testBoolean.methods().list().size());

    Assert.assertEquals("inner types count of 'TestBoolean'", 0, testBoolean.innerTypes().list().size());
  }
}
