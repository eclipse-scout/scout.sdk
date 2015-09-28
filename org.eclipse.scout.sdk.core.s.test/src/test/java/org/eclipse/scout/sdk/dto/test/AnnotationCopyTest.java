/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.dto.test;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.dto.test.util.CoreScoutTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link AnnotationCopyTest}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 08.05.2014
 */
public class AnnotationCopyTest {
  @Test
  public void testCreateFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms.AnnotationCopyTestForm");
    testApiOfAnnotationCopyTestFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfAnnotationCopyTestFormData(IType annotationCopyTestFormData) {
    SdkAssert.assertHasFlags(annotationCopyTestFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(annotationCopyTestFormData, "Lorg.eclipse.scout.rt.shared.data.form.AbstractFormData;");
    Assert.assertEquals("annotation count", 1, annotationCopyTestFormData.annotations().list().size());
    SdkAssert.assertAnnotation(annotationCopyTestFormData, "javax.annotation.Generated");

    // fields of AnnotationCopyTestFormData
    Assert.assertEquals("field count of 'formdata.shared.ui.forms.AnnotationCopyTestFormData'", 1, annotationCopyTestFormData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(annotationCopyTestFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.ui.forms.AnnotationCopyTestFormData'", 2, annotationCopyTestFormData.methods().list().size());
    IMethod annotationCopyTestFormData1 = SdkAssert.assertMethodExist(annotationCopyTestFormData, "AnnotationCopyTestFormData", new String[]{});
    Assert.assertTrue(annotationCopyTestFormData1.isConstructor());
    Assert.assertEquals("annotation count", 0, annotationCopyTestFormData1.annotations().list().size());
    IMethod getFirst = SdkAssert.assertMethodExist(annotationCopyTestFormData, "getFirst", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirst, "Lformdata.shared.ui.forms.AnnotationCopyTestFormData$First;");
    Assert.assertEquals("annotation count", 0, getFirst.annotations().list().size());

    Assert.assertEquals("inner types count of 'AnnotationCopyTestFormData'", 1, annotationCopyTestFormData.innerTypes().list().size());
    // type First
    IType first = SdkAssert.assertTypeExists(annotationCopyTestFormData, "First");
    SdkAssert.assertHasFlags(first, 9);
    SdkAssert.assertHasSuperTypeSignature(first, "Lorg.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<Ljava.lang.String;>;");
    Assert.assertEquals("annotation count", 1, first.annotations().list().size());
    SdkAssert.assertAnnotation(first, "formdata.shared.SharedAnnotation");

    // fields of First
    Assert.assertEquals("field count of 'formdata.shared.ui.forms.AnnotationCopyTestFormData$First'", 1, first.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(first, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID1.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.ui.forms.AnnotationCopyTestFormData$First'", 1, first.methods().list().size());
    IMethod first1 = SdkAssert.assertMethodExist(first, "First", new String[]{});
    Assert.assertTrue(first1.isConstructor());
    Assert.assertEquals("annotation count", 0, first1.annotations().list().size());

    Assert.assertEquals("inner types count of 'First'", 0, first.innerTypes().list().size());
  }

}
