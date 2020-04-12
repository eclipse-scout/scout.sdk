/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.formfieldmenu.FormFieldMenuTestForm;

/**
 * @since 7.1.0
 */
public class FormFieldMenuTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(FormFieldMenuTestForm.class.getName(), FormFieldMenuTest::testApiOfFormFieldMenuTestFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfFormFieldMenuTestFormData(IType formFieldMenuTestFormData) {
    assertHasFlags(formFieldMenuTestFormData, 1);
    assertHasSuperClass(formFieldMenuTestFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");
    assertEquals(1, formFieldMenuTestFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(formFieldMenuTestFormData, "javax.annotation.Generated");

    // fields of FormFieldMenuTestFormData
    assertEquals(1, formFieldMenuTestFormData.fields().stream().count(), "field count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData'");
    IField serialVersionUID = assertFieldExist(formFieldMenuTestFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, JavaTypes._long);
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, formFieldMenuTestFormData.methods().stream().count(), "method count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData'");
    IMethod getTestBoolean = assertMethodExist(formFieldMenuTestFormData, "getTestBoolean", new String[]{});
    assertMethodReturnType(getTestBoolean, "formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean");
    assertEquals(0, getTestBoolean.annotations().stream().count(), "annotation count");

    assertEquals(1, formFieldMenuTestFormData.innerTypes().stream().count(), "inner types count of 'FormFieldMenuTestFormData'");
    // type TestBoolean
    IType testBoolean = assertTypeExists(formFieldMenuTestFormData, "TestBoolean");
    assertHasFlags(testBoolean, 9);
    assertHasSuperClass(testBoolean, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Boolean>");
    assertEquals(0, testBoolean.annotations().stream().count(), "annotation count");

    // fields of TestBoolean
    assertEquals(1, testBoolean.fields().stream().count(), "field count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean'");
    IField serialVersionUID1 = assertFieldExist(testBoolean, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, JavaTypes._long);
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, testBoolean.methods().stream().count(), "method count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean'");

    assertEquals(0, testBoolean.innerTypes().stream().count(), "inner types count of 'TestBoolean'");
  }
}
