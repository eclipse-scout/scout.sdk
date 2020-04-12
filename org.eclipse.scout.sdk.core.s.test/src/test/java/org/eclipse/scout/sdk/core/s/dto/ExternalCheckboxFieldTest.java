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
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IType;
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
    IField serialVersionUID = assertFieldExist(abstractTestCheckboxFieldData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(0, abstractTestCheckboxFieldData.methods().stream().count(), "method count of 'AbstractTestCheckboxFieldData'");

    assertEquals(0, abstractTestCheckboxFieldData.innerTypes().stream().count(), "inner types count of 'AbstractTestCheckboxFieldData'");
  }

}
