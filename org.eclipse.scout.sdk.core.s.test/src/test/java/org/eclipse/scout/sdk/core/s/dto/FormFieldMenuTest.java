/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertTypeExists;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.model.api.IType;
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
    var serialVersionUID = assertFieldExist(formFieldMenuTestFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, JavaTypes._long);
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, formFieldMenuTestFormData.methods().stream().count(), "method count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData'");
    var getTestBoolean = assertMethodExist(formFieldMenuTestFormData, "getTestBoolean");
    assertMethodReturnType(getTestBoolean, "formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean");
    assertEquals(0, getTestBoolean.annotations().stream().count(), "annotation count");

    assertEquals(1, formFieldMenuTestFormData.innerTypes().stream().count(), "inner types count of 'FormFieldMenuTestFormData'");
    // type TestBoolean
    var testBoolean = assertTypeExists(formFieldMenuTestFormData, "TestBoolean");
    assertHasFlags(testBoolean, 9);
    assertHasSuperClass(testBoolean, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Boolean>");
    assertEquals(0, testBoolean.annotations().stream().count(), "annotation count");

    // fields of TestBoolean
    assertEquals(1, testBoolean.fields().stream().count(), "field count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean'");
    var serialVersionUID1 = assertFieldExist(testBoolean, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, JavaTypes._long);
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, testBoolean.methods().stream().count(), "method count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean'");

    assertEquals(0, testBoolean.innerTypes().stream().count(), "inner types count of 'TestBoolean'");
  }
}
