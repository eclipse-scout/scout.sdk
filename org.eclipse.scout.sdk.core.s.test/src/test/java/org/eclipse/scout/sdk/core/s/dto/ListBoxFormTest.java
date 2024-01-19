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

public class ListBoxFormTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.ListBoxForm", ListBoxFormTest::testApiOfListBoxFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfListBoxFormData(IType listBoxFormData) {
    var scoutApi = listBoxFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(listBoxFormData, Flags.AccPublic);
    assertHasSuperClass(listBoxFormData, scoutApi.AbstractFormData());
    assertEquals(1, listBoxFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(listBoxFormData, scoutApi.Generated());

    // fields of ListBoxFormData
    assertEquals(1, listBoxFormData.fields().stream().count(), "field count of 'formdata.shared.services.process.ListBoxFormData'");
    var serialVersionUID = assertFieldExist(listBoxFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, listBoxFormData.methods().stream().count(), "method count of 'formdata.shared.services.process.ListBoxFormData'");
    var getListBox = assertMethodExist(listBoxFormData, "getListBox");
    assertMethodReturnType(getListBox, "formdata.shared.services.process.ListBoxFormData$ListBox");
    assertEquals(0, getListBox.annotations().stream().count(), "annotation count");

    assertEquals(1, listBoxFormData.innerTypes().stream().count(), "inner types count of 'ListBoxFormData'");
    // type ListBox
    var listBox = assertTypeExists(listBoxFormData, "ListBox");
    assertHasFlags(listBox, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(listBox, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.util.Set<java.lang.Long>>");
    assertEquals(0, listBox.annotations().stream().count(), "annotation count");

    // fields of ListBox
    assertEquals(1, listBox.fields().stream().count(), "field count of 'formdata.shared.services.process.ListBoxFormData$ListBox'");
    var serialVersionUID1 = assertFieldExist(listBox, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, listBox.methods().stream().count(), "method count of 'formdata.shared.services.process.ListBoxFormData$ListBox'");

    assertEquals(0, listBox.innerTypes().stream().count(), "inner types count of 'ListBox'");
  }
}
