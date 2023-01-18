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
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertTypeExists;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.template.formfield.AbstractExternalGroupBox;

public class ExternalGroupboxTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(AbstractExternalGroupBox.class.getName(), ExternalGroupboxTest::testApiOfAbstractExternalGroupBoxData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractExternalGroupBoxData(IType abstractExternalGroupBoxData) {
    // type AbstractExternalGroupBoxData
    assertHasFlags(abstractExternalGroupBoxData, 1025);
    assertHasSuperClass(abstractExternalGroupBoxData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData");

    // fields of AbstractExternalGroupBoxData
    assertEquals(1, abstractExternalGroupBoxData.fields().stream().count(), "field count of 'AbstractExternalGroupBoxData'");
    var serialVersionUID = assertFieldExist(abstractExternalGroupBoxData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, abstractExternalGroupBoxData.methods().stream().count(), "method count of 'AbstractExternalGroupBoxData'");
    var getExternalString = assertMethodExist(abstractExternalGroupBoxData, "getExternalString");
    assertMethodReturnType(getExternalString, "formdata.shared.services.process.AbstractExternalGroupBoxData$ExternalString");

    assertEquals(1, abstractExternalGroupBoxData.innerTypes().stream().count(), "inner types count of 'AbstractExternalGroupBoxData'");
    // type ExternalString
    var externalString = assertTypeExists(abstractExternalGroupBoxData, "ExternalString");
    assertHasFlags(externalString, 9);
    assertHasSuperClass(externalString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of ExternalString
    assertEquals(1, externalString.fields().stream().count(), "field count of 'ExternalString'");
    var serialVersionUID1 = assertFieldExist(externalString, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, externalString.methods().stream().count(), "method count of 'ExternalString'");

    assertEquals(0, externalString.innerTypes().stream().count(), "inner types count of 'ExternalString'");
  }
}
