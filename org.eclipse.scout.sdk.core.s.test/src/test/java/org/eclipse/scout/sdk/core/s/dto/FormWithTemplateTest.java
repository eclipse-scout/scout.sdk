/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.UsingTemplateForm;

public class FormWithTemplateTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(UsingTemplateForm.class.getName(), FormWithTemplateTest::testApiOfUsingTemplateFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfUsingTemplateFormData(IType usingTemplateFormData) {
    // type UsingTemplateFormData
    assertHasFlags(usingTemplateFormData, 1);
    assertHasSuperClass(usingTemplateFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");

    // fields of UsingTemplateFormData
    assertEquals(1, usingTemplateFormData.fields().stream().count(), "field count of 'UsingTemplateFormData'");
    var serialVersionUID = assertFieldExist(usingTemplateFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(4, usingTemplateFormData.methods().stream().count(), "method count of 'UsingTemplateFormData'");
    var getExternalGroupBox = assertMethodExist(usingTemplateFormData, "getExternalGroupBox", new String[]{});
    assertMethodReturnType(getExternalGroupBox, "formdata.shared.services.process.UsingTemplateFormData$ExternalGroupBox");
    var getInternalHtml = assertMethodExist(usingTemplateFormData, "getInternalHtml", new String[]{});
    assertMethodReturnType(getInternalHtml, "formdata.shared.services.process.UsingTemplateFormData$InternalHtml");
    var getTestCheckbox = assertMethodExist(usingTemplateFormData, "getTestCheckbox", new String[]{});
    assertMethodReturnType(getTestCheckbox, "formdata.shared.services.process.UsingTemplateFormData$TestCheckbox");
    var getTestLimitedString = assertMethodExist(usingTemplateFormData, "getTestLimitedString", new String[]{});
    assertMethodReturnType(getTestLimitedString, "formdata.shared.services.process.UsingTemplateFormData$TestLimitedString");

    assertEquals(4, usingTemplateFormData.innerTypes().stream().count(), "inner types count of 'UsingTemplateFormData'");
    // type ExternalGroupBox
    var externalGroupBox = assertTypeExists(usingTemplateFormData, "ExternalGroupBox");
    assertHasFlags(externalGroupBox, 9);
    assertHasSuperClass(externalGroupBox, "formdata.shared.services.process.AbstractExternalGroupBoxData");

    // fields of ExternalGroupBox
    assertEquals(1, externalGroupBox.fields().stream().count(), "field count of 'ExternalGroupBox'");
    var serialVersionUID1 = assertFieldExist(externalGroupBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, externalGroupBox.methods().stream().count(), "method count of 'ExternalGroupBox'");

    assertEquals(0, externalGroupBox.innerTypes().stream().count(), "inner types count of 'ExternalGroupBox'");
    // type InternalHtml
    var internalHtml = assertTypeExists(usingTemplateFormData, "InternalHtml");
    assertHasFlags(internalHtml, 9);
    assertHasSuperClass(internalHtml, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of InternalHtml
    assertEquals(1, internalHtml.fields().stream().count(), "field count of 'InternalHtml'");
    var serialVersionUID2 = assertFieldExist(internalHtml, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, internalHtml.methods().stream().count(), "method count of 'InternalHtml'");

    assertEquals(0, internalHtml.innerTypes().stream().count(), "inner types count of 'InternalHtml'");
    // type TestCheckbox
    var testCheckbox = assertTypeExists(usingTemplateFormData, "TestCheckbox");
    assertHasFlags(testCheckbox, 9);
    assertHasSuperClass(testCheckbox, "formdata.shared.services.process.AbstractTestCheckboxFieldData");

    // fields of TestCheckbox
    assertEquals(1, testCheckbox.fields().stream().count(), "field count of 'TestCheckbox'");
    var serialVersionUID3 = assertFieldExist(testCheckbox, "serialVersionUID");
    assertHasFlags(serialVersionUID3, 26);
    assertFieldType(serialVersionUID3, "long");

    assertEquals(0, testCheckbox.methods().stream().count(), "method count of 'TestCheckbox'");

    assertEquals(0, testCheckbox.innerTypes().stream().count(), "inner types count of 'TestCheckbox'");
    // type TestLimitedString
    var testLimitedString = assertTypeExists(usingTemplateFormData, "TestLimitedString");
    assertHasFlags(testLimitedString, 9);
    assertHasSuperClass(testLimitedString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of TestLimitedString
    assertEquals(1, testLimitedString.fields().stream().count(), "field count of 'TestLimitedString'");
    var serialVersionUID4 = assertFieldExist(testLimitedString, "serialVersionUID");
    assertHasFlags(serialVersionUID4, 26);
    assertFieldType(serialVersionUID4, "long");

    assertEquals(0, testLimitedString.methods().stream().count(), "method count of 'TestLimitedString'");

    assertEquals(0, testLimitedString.innerTypes().stream().count(), "inner types count of 'TestLimitedString'");
  }
}
