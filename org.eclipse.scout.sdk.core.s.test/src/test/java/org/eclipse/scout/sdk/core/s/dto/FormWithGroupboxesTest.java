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


public class FormWithGroupboxesTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.FormWithGroupBoxesForm", FormWithGroupboxesTest::testApiOfFormWithGroupBoxesFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfFormWithGroupBoxesFormData(IType formWithGroupBoxesFormData) {
    // type FormWithGroupBoxesFormData
    assertHasFlags(formWithGroupBoxesFormData, 1);
    assertHasSuperClass(formWithGroupBoxesFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");

    // fields of FormWithGroupBoxesFormData
    assertEquals(1, formWithGroupBoxesFormData.fields().stream().count(), "field count of 'FormWithGroupBoxesFormData'");
    var serialVersionUID = assertFieldExist(formWithGroupBoxesFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(2, formWithGroupBoxesFormData.methods().stream().count(), "method count of 'FormWithGroupBoxesFormData'");
    var getFlatString = assertMethodExist(formWithGroupBoxesFormData, "getFlatString");
    assertMethodReturnType(getFlatString, "formdata.shared.services.process.FormWithGroupBoxesFormData$FlatString");
    var getInnerInteger = assertMethodExist(formWithGroupBoxesFormData, "getInnerInteger");
    assertMethodReturnType(getInnerInteger, "formdata.shared.services.process.FormWithGroupBoxesFormData$InnerInteger");

    assertEquals(2, formWithGroupBoxesFormData.innerTypes().stream().count(), "inner types count of 'FormWithGroupBoxesFormData'");
    // type FlatString
    var flatString = assertTypeExists(formWithGroupBoxesFormData, "FlatString");
    assertHasFlags(flatString, 9);
    assertHasSuperClass(flatString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of FlatString
    assertEquals(1, flatString.fields().stream().count(), "field count of 'FlatString'");
    var serialVersionUID1 = assertFieldExist(flatString, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, flatString.methods().stream().count(), "method count of 'FlatString'");

    assertEquals(0, flatString.innerTypes().stream().count(), "inner types count of 'FlatString'");
    // type InnerInteger
    var innerInteger = assertTypeExists(formWithGroupBoxesFormData, "InnerInteger");
    assertHasFlags(innerInteger, 9);
    assertHasSuperClass(innerInteger, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.Integer>");

    // fields of InnerInteger
    assertEquals(1, innerInteger.fields().stream().count(), "field count of 'InnerInteger'");
    var serialVersionUID2 = assertFieldExist(innerInteger, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");

    assertEquals(0, innerInteger.methods().stream().count(), "method count of 'InnerInteger'");

    assertEquals(0, innerInteger.innerTypes().stream().count(), "inner types count of 'InnerInteger'");
  }

}
