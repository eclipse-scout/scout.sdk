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

import org.eclipse.scout.sdk.core.model.IField;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.eclipse.scout.sdk.dto.test.util.CoreScoutTestingUtils;
import org.junit.Test;

/**
 * <h3>{@link AnnotationCopyTest}</h3>
 *
 * @author Matthias Villiger
 * @since 4.0.0 08.05.2014
 */
public class AnnotationCopyTest {
  @Test
  public void testCreateFormData() throws Exception {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms.AnnotationCopyTestForm");
    testApiOfAnnotationCopyTestFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfAnnotationCopyTestFormData(IType annotationCopyTestFormData) throws Exception {
    // type AnnotationCopyTestFormData
    SdkAssert.assertHasFlags(annotationCopyTestFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(annotationCopyTestFormData, "QAbstractFormData;");
    SdkAssert.assertAnnotation(annotationCopyTestFormData, "javax.annotation.Generated");

    // fields of AnnotationCopyTestFormData
    SdkAssert.assertEquals("field count of 'AnnotationCopyTestFormData'", 1, annotationCopyTestFormData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(annotationCopyTestFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'AnnotationCopyTestFormData'", 2, annotationCopyTestFormData.getMethods().size());
    IMethod annotationCopyTestFormData1 = SdkAssert.assertMethodExist(annotationCopyTestFormData, "AnnotationCopyTestFormData", new String[]{});
    SdkAssert.assertTrue(annotationCopyTestFormData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(annotationCopyTestFormData1, null);
    IMethod getFirst = SdkAssert.assertMethodExist(annotationCopyTestFormData, "getFirst", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getFirst, "QFirst;");

    SdkAssert.assertEquals("inner types count of 'AnnotationCopyTestFormData'", 1, annotationCopyTestFormData.getTypes().size());
    // type First
    IType first = SdkAssert.assertTypeExists(annotationCopyTestFormData, "First");
    SdkAssert.assertHasFlags(first, 9);
    SdkAssert.assertHasSuperTypeSignature(first, "QAbstractValueFieldData<QString;>;");
    SdkAssert.assertAnnotation(first, "formdata.shared.SharedAnnotation");

    // fields of First
    SdkAssert.assertEquals("field count of 'First'", 1, first.getFields().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(first, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");

    SdkAssert.assertEquals("method count of 'First'", 1, first.getMethods().size());
    IMethod first1 = SdkAssert.assertMethodExist(first, "First", new String[]{});
    SdkAssert.assertTrue(first1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(first1, null);

    SdkAssert.assertEquals("inner types count of 'First'", 0, first.getTypes().size());
  }

}
