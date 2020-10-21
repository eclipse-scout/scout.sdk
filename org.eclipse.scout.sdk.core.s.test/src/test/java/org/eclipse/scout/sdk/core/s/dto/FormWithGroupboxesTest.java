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
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.FormWithGroupBoxesForm;

public class FormWithGroupboxesTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(FormWithGroupBoxesForm.class.getName(), FormWithGroupboxesTest::testApiOfFormWithGroupBoxesFormData);
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
    var getFlatString = assertMethodExist(formWithGroupBoxesFormData, "getFlatString", new String[]{});
    assertMethodReturnType(getFlatString, "formdata.shared.services.process.FormWithGroupBoxesFormData$FlatString");
    var getInnerInteger = assertMethodExist(formWithGroupBoxesFormData, "getInnerInteger", new String[]{});
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
