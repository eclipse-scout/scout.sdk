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
 * <h3>{@link MixedValueFieldTest}</h3>
 *
 * @since 6.1.0
 */
public class MixedValueFieldTest {

  @Test
  public void testMixedValueFieldWithReplace() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.mixed.MixedValueFieldForm", MixedValueFieldTest::testApiOfMixedValueFieldFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfMixedValueFieldFormData(IType mixedValueFieldFormData) {
    var scoutApi = mixedValueFieldFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(mixedValueFieldFormData, Flags.AccPublic);
    assertHasSuperClass(mixedValueFieldFormData, scoutApi.AbstractFormData());
    assertEquals(1, mixedValueFieldFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(mixedValueFieldFormData, scoutApi.Generated());

    // fields of MixedValueFieldFormData
    assertEquals(1, mixedValueFieldFormData.fields().stream().count(), "field count of 'formdata.shared.mixed.MixedValueFieldFormData'");
    var serialVersionUID = assertFieldExist(mixedValueFieldFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, mixedValueFieldFormData.methods().stream().count(), "method count of 'formdata.shared.mixed.MixedValueFieldFormData'");
    var getFirst = assertMethodExist(mixedValueFieldFormData, "getFirst");
    assertMethodReturnType(getFirst, "formdata.shared.mixed.MixedValueFieldFormData$First");
    assertEquals(0, getFirst.annotations().stream().count(), "annotation count");

    assertEquals(1, mixedValueFieldFormData.innerTypes().stream().count(), "inner types count of 'MixedValueFieldFormData'");
    // type First
    var first = assertTypeExists(mixedValueFieldFormData, "First");
    assertHasFlags(first, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(first, "formdata.shared.mixed.AbstractMixedValueFieldData<java.lang.Short>");
    assertEquals(0, first.annotations().stream().count(), "annotation count");

    // fields of First
    assertEquals(1, first.fields().stream().count(), "field count of 'formdata.shared.mixed.MixedValueFieldFormData$First'");
    var serialVersionUID1 = assertFieldExist(first, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(1, first.methods().stream().count(), "method count of 'formdata.shared.mixed.MixedValueFieldFormData$First'");
    var getChangedAttributeNameFieldEx = assertMethodExist(first, "getChangedAttributeNameFieldEx");
    assertMethodReturnType(getChangedAttributeNameFieldEx, "formdata.shared.mixed.MixedValueFieldFormData$First$ChangedAttributeNameFieldEx");
    assertEquals(0, getChangedAttributeNameFieldEx.annotations().stream().count(), "annotation count");

    assertEquals(1, first.innerTypes().stream().count(), "inner types count of 'First'");
    // type ChangedAttributeNameFieldEx
    var changedAttributeNameFieldEx = assertTypeExists(first, "ChangedAttributeNameFieldEx");
    assertHasFlags(changedAttributeNameFieldEx, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(changedAttributeNameFieldEx, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(1, changedAttributeNameFieldEx.annotations().stream().count(), "annotation count");
    assertAnnotation(changedAttributeNameFieldEx, scoutApi.Replace());

    // fields of ChangedAttributeNameFieldEx
    assertEquals(1, changedAttributeNameFieldEx.fields().stream().count(), "field count of 'formdata.shared.mixed.MixedValueFieldFormData$First$ChangedAttributeNameFieldEx'");
    var serialVersionUID2 = assertFieldExist(changedAttributeNameFieldEx, "serialVersionUID");
    assertHasFlags(serialVersionUID2, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, changedAttributeNameFieldEx.methods().stream().count(), "method count of 'formdata.shared.mixed.MixedValueFieldFormData$First$ChangedAttributeNameFieldEx'");

    assertEquals(0, changedAttributeNameFieldEx.innerTypes().stream().count(), "inner types count of 'ChangedAttributeNameFieldEx'");
  }
}
