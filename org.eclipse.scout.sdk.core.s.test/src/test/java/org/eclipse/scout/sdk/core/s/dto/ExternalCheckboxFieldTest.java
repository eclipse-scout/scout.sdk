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

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.template.formfield.AbstractTestCheckboxField;

public class ExternalCheckboxFieldTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(AbstractTestCheckboxField.class.getName(), ExternalCheckboxFieldTest::testApiOfAbstractTestCheckboxFieldData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractTestCheckboxFieldData(IType abstractTestCheckboxFieldData) {
    // type AbstractTestCheckboxFieldData
    assertHasFlags(abstractTestCheckboxFieldData, 1025);
    assertHasSuperClass(abstractTestCheckboxFieldData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Boolean>");

    // fields of AbstractTestCheckboxFieldData
    assertEquals(1, abstractTestCheckboxFieldData.fields().stream().count(), "field count of 'AbstractTestCheckboxFieldData'");
    var serialVersionUID = assertFieldExist(abstractTestCheckboxFieldData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(0, abstractTestCheckboxFieldData.methods().stream().count(), "method count of 'AbstractTestCheckboxFieldData'");

    assertEquals(0, abstractTestCheckboxFieldData.innerTypes().stream().count(), "inner types count of 'AbstractTestCheckboxFieldData'");
  }

}
