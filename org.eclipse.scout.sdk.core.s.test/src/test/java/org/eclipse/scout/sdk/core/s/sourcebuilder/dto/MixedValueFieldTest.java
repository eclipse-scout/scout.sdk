/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.sourcebuilder.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

import formdata.client.ui.forms.mixed.MixedValueFieldForm;

/**
 * <h3>{@link MixedValueFieldTest}</h3>
 *
 * @since 6.1.0
 */
public class MixedValueFieldTest {

  @Test
  public void testMixedValueFieldWithReplace() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(MixedValueFieldForm.class.getName());
    testApiOfMixedValueFieldFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfMixedValueFieldFormData(IType mixedValueFieldFormData) {
    SdkAssert.assertHasFlags(mixedValueFieldFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(mixedValueFieldFormData, "Lorg.eclipse.scout.rt.shared.data.form.AbstractFormData;");
    Assert.assertEquals("annotation count", 1, mixedValueFieldFormData.annotations().list().size());
    SdkAssert.assertAnnotation(mixedValueFieldFormData, "javax.annotation.Generated");

    // fields of MixedValueFieldFormData
    Assert.assertEquals("field count of 'formdata.shared.mixed.MixedValueFieldFormData'", 1, mixedValueFieldFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(mixedValueFieldFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.mixed.MixedValueFieldFormData'", 1, mixedValueFieldFormData.methods().list().size());
    IMethod getFirst = SdkAssert.assertMethodExist(mixedValueFieldFormData, "getFirst", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirst, "Lformdata.shared.mixed.MixedValueFieldFormData$First;");
    Assert.assertEquals("annotation count", 0, getFirst.annotations().list().size());

    Assert.assertEquals("inner types count of 'MixedValueFieldFormData'", 1, mixedValueFieldFormData.innerTypes().list().size());
    // type First
    IType first = SdkAssert.assertTypeExists(mixedValueFieldFormData, "First");
    SdkAssert.assertHasFlags(first, 9);
    SdkAssert.assertHasSuperTypeSignature(first, "Lformdata.shared.mixed.AbstractMixedValueFieldData<Ljava.lang.Short;>;");
    Assert.assertEquals("annotation count", 0, first.annotations().list().size());

    // fields of First
    Assert.assertEquals("field count of 'formdata.shared.mixed.MixedValueFieldFormData$First'", 1, first.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(first, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID1.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.mixed.MixedValueFieldFormData$First'", 1, first.methods().list().size());
    IMethod getChangedAttributeNameFieldEx = SdkAssert.assertMethodExist(first, "getChangedAttributeNameFieldEx", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getChangedAttributeNameFieldEx, "Lformdata.shared.mixed.MixedValueFieldFormData$First$ChangedAttributeNameFieldEx;");
    Assert.assertEquals("annotation count", 0, getChangedAttributeNameFieldEx.annotations().list().size());

    Assert.assertEquals("inner types count of 'First'", 1, first.innerTypes().list().size());
    // type ChangedAttributeNameFieldEx
    IType changedAttributeNameFieldEx = SdkAssert.assertTypeExists(first, "ChangedAttributeNameFieldEx");
    SdkAssert.assertHasFlags(changedAttributeNameFieldEx, 9);
    SdkAssert.assertHasSuperTypeSignature(changedAttributeNameFieldEx, "Lorg.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<Ljava.lang.String;>;");
    Assert.assertEquals("annotation count", 1, changedAttributeNameFieldEx.annotations().list().size());
    SdkAssert.assertAnnotation(changedAttributeNameFieldEx, "org.eclipse.scout.rt.platform.Replace");

    // fields of ChangedAttributeNameFieldEx
    Assert.assertEquals("field count of 'formdata.shared.mixed.MixedValueFieldFormData$First$ChangedAttributeNameFieldEx'", 1, changedAttributeNameFieldEx.fields().list().size());
    IField serialVersionUID2 = SdkAssert.assertFieldExist(changedAttributeNameFieldEx, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID2, 26);
    SdkAssert.assertFieldSignature(serialVersionUID2, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID2.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.mixed.MixedValueFieldFormData$First$ChangedAttributeNameFieldEx'", 0, changedAttributeNameFieldEx.methods().list().size());

    Assert.assertEquals("inner types count of 'ChangedAttributeNameFieldEx'", 0, changedAttributeNameFieldEx.innerTypes().list().size());
  }
}
