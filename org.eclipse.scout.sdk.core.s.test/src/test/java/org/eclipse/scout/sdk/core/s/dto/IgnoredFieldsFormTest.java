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

import formdata.client.ui.forms.IgnoredFieldsForm;

public class IgnoredFieldsFormTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(IgnoredFieldsForm.class.getName(), IgnoredFieldsFormTest::testApiOfIgnoredFieldsFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfIgnoredFieldsFormData(IType ignoredFieldsFormData) {
    // type IgnoredFieldsFormData
    assertHasFlags(ignoredFieldsFormData, 1);
    assertHasSuperClass(ignoredFieldsFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");

    // fields of IgnoredFieldsFormData
    assertEquals(1, ignoredFieldsFormData.fields().stream().count(), "field count of 'IgnoredFieldsFormData'");
    var serialVersionUID = assertFieldExist(ignoredFieldsFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, ignoredFieldsFormData.methods().stream().count(), "method count of 'IgnoredFieldsFormData'");
    var getNotIgnored = assertMethodExist(ignoredFieldsFormData, "getNotIgnored", new String[]{});
    assertMethodReturnType(getNotIgnored, "formdata.shared.services.process.IgnoredFieldsFormData$NotIgnored");

    assertEquals(1, ignoredFieldsFormData.innerTypes().stream().count(), "inner types count of 'IgnoredFieldsFormData'");
    // type NotIgnored
    var notIgnored = assertTypeExists(ignoredFieldsFormData, "NotIgnored");
    assertHasFlags(notIgnored, 9);
    assertHasSuperClass(notIgnored, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of NotIgnored
    assertEquals(1, notIgnored.fields().stream().count(), "field count of 'NotIgnored'");
    var serialVersionUID1 = assertFieldExist(notIgnored, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, notIgnored.methods().stream().count(), "method count of 'NotIgnored'");

    assertEquals(0, notIgnored.innerTypes().stream().count(), "inner types count of 'NotIgnored'");
  }
}
