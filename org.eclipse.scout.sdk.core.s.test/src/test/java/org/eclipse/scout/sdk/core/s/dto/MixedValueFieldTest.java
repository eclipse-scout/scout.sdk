/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
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
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
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

import formdata.client.ui.forms.mixed.MixedValueFieldForm;

/**
 * <h3>{@link MixedValueFieldTest}</h3>
 *
 * @since 6.1.0
 */
public class MixedValueFieldTest {

  @Test
  public void testMixedValueFieldWithReplace() {
    createFormDataAssertNoCompileErrors(MixedValueFieldForm.class.getName(), MixedValueFieldTest::testApiOfMixedValueFieldFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfMixedValueFieldFormData(IType mixedValueFieldFormData) {
    assertHasFlags(mixedValueFieldFormData, 1);
    assertHasSuperClass(mixedValueFieldFormData, "org.eclipse.scout.rt.shared.data.form.AbstractFormData");
    assertEquals(1, mixedValueFieldFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(mixedValueFieldFormData, "javax.annotation.Generated");

    // fields of MixedValueFieldFormData
    assertEquals(1, mixedValueFieldFormData.fields().stream().count(), "field count of 'formdata.shared.mixed.MixedValueFieldFormData'");
    var serialVersionUID = assertFieldExist(mixedValueFieldFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, mixedValueFieldFormData.methods().stream().count(), "method count of 'formdata.shared.mixed.MixedValueFieldFormData'");
    var getFirst = assertMethodExist(mixedValueFieldFormData, "getFirst");
    assertMethodReturnType(getFirst, "formdata.shared.mixed.MixedValueFieldFormData$First");
    assertEquals(0, getFirst.annotations().stream().count(), "annotation count");

    assertEquals(1, mixedValueFieldFormData.innerTypes().stream().count(), "inner types count of 'MixedValueFieldFormData'");
    // type First
    var first = assertTypeExists(mixedValueFieldFormData, "First");
    assertHasFlags(first, 9);
    assertHasSuperClass(first, "formdata.shared.mixed.AbstractMixedValueFieldData<java.lang.Short>");
    assertEquals(0, first.annotations().stream().count(), "annotation count");

    // fields of First
    assertEquals(1, first.fields().stream().count(), "field count of 'formdata.shared.mixed.MixedValueFieldFormData$First'");
    var serialVersionUID1 = assertFieldExist(first, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(1, first.methods().stream().count(), "method count of 'formdata.shared.mixed.MixedValueFieldFormData$First'");
    var getChangedAttributeNameFieldEx = assertMethodExist(first, "getChangedAttributeNameFieldEx");
    assertMethodReturnType(getChangedAttributeNameFieldEx, "formdata.shared.mixed.MixedValueFieldFormData$First$ChangedAttributeNameFieldEx");
    assertEquals(0, getChangedAttributeNameFieldEx.annotations().stream().count(), "annotation count");

    assertEquals(1, first.innerTypes().stream().count(), "inner types count of 'First'");
    // type ChangedAttributeNameFieldEx
    var changedAttributeNameFieldEx = assertTypeExists(first, "ChangedAttributeNameFieldEx");
    assertHasFlags(changedAttributeNameFieldEx, 9);
    assertHasSuperClass(changedAttributeNameFieldEx, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(1, changedAttributeNameFieldEx.annotations().stream().count(), "annotation count");
    assertAnnotation(changedAttributeNameFieldEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of ChangedAttributeNameFieldEx
    assertEquals(1, changedAttributeNameFieldEx.fields().stream().count(), "field count of 'formdata.shared.mixed.MixedValueFieldFormData$First$ChangedAttributeNameFieldEx'");
    var serialVersionUID2 = assertFieldExist(changedAttributeNameFieldEx, "serialVersionUID");
    assertHasFlags(serialVersionUID2, 26);
    assertFieldType(serialVersionUID2, "long");
    assertEquals(0, serialVersionUID2.annotations().stream().count(), "annotation count");

    assertEquals(0, changedAttributeNameFieldEx.methods().stream().count(), "method count of 'formdata.shared.mixed.MixedValueFieldFormData$First$ChangedAttributeNameFieldEx'");

    assertEquals(0, changedAttributeNameFieldEx.innerTypes().stream().count(), "inner types count of 'ChangedAttributeNameFieldEx'");
  }
}
