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

import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.ListBoxForm;

public class ListBoxFormTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(ListBoxForm.class.getName(), ListBoxFormTest::testApiOfListBoxFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfListBoxFormData(IType listBoxFormData) {
    // type ListBoxFormData
    assertHasFlags(listBoxFormData, 1);
    assertHasSuperClass(listBoxFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");
    assertAnnotation(listBoxFormData, "javax.annotation.Generated");

    // fields of ListBoxFormData
    assertEquals(1, listBoxFormData.fields().stream().count(), "field count of 'ListBoxFormData'");
    var serialVersionUID = assertFieldExist(listBoxFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, listBoxFormData.methods().stream().count(), "method count of 'ListBoxFormData'");
    var getListBox = assertMethodExist(listBoxFormData, "getListBox");
    assertMethodReturnType(getListBox, "formdata.shared.services.process.ListBoxFormData$ListBox");

    assertEquals(1, listBoxFormData.innerTypes().stream().count(), "inner types count of 'ListBoxFormData'");
    // type ListBox
    var listBox = assertTypeExists(listBoxFormData, "ListBox");
    assertHasFlags(listBox, 9);
    assertHasSuperClass(listBox, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.util.Set<java.lang.Long>>");

    // fields of ListBox
    assertEquals(1, listBox.fields().stream().count(), "field count of 'ListBox'");
    var serialVersionUID1 = assertFieldExist(listBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, listBox.methods().stream().count(), "method count of 'ListBox'");

    assertEquals(0, listBox.innerTypes().stream().count(), "inner types count of 'ListBox'");
  }
}
