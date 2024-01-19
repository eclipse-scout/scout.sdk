/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
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

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.junit.jupiter.api.Test;

/**
 * @since 7.1.0
 */
public class FormFieldMenuTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.formfieldmenu.FormFieldMenuTestForm", FormFieldMenuTest::testApiOfFormFieldMenuTestFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfFormFieldMenuTestFormData(IType formFieldMenuTestFormData) {
    var scoutApi = formFieldMenuTestFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(formFieldMenuTestFormData, Flags.AccPublic);
    assertHasSuperClass(formFieldMenuTestFormData, scoutApi.AbstractFormData());
    assertEquals(1, formFieldMenuTestFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(formFieldMenuTestFormData, scoutApi.Generated());

    // fields of FormFieldMenuTestFormData
    assertEquals(1, formFieldMenuTestFormData.fields().stream().count(), "field count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData'");
    var serialVersionUID = assertFieldExist(formFieldMenuTestFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, formFieldMenuTestFormData.methods().stream().count(), "method count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData'");
    var getTestBoolean = assertMethodExist(formFieldMenuTestFormData, "getTestBoolean");
    assertMethodReturnType(getTestBoolean, "formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean");
    assertEquals(0, getTestBoolean.annotations().stream().count(), "annotation count");

    assertEquals(1, formFieldMenuTestFormData.innerTypes().stream().count(), "inner types count of 'FormFieldMenuTestFormData'");
    // type TestBoolean
    var testBoolean = assertTypeExists(formFieldMenuTestFormData, "TestBoolean");
    assertHasFlags(testBoolean, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(testBoolean, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Boolean>");
    assertEquals(0, testBoolean.annotations().stream().count(), "annotation count");

    // fields of TestBoolean
    assertEquals(1, testBoolean.fields().stream().count(), "field count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean'");
    var serialVersionUID1 = assertFieldExist(testBoolean, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, testBoolean.methods().stream().count(), "method count of 'formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData$TestBoolean'");

    assertEquals(0, testBoolean.innerTypes().stream().count(), "inner types count of 'TestBoolean'");
  }
}
