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
 * <h3>{@link AnnotationCopyTest}</h3>
 *
 * @since 4.0.0 2014-05-08
 */
public class AnnotationCopyTest {
  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.AnnotationCopyTestForm", AnnotationCopyTest::testApiOfAnnotationCopyTestFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfAnnotationCopyTestFormData(IType annotationCopyTestFormData) {
    var scoutApi = annotationCopyTestFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(annotationCopyTestFormData, Flags.AccPublic);
    assertHasSuperClass(annotationCopyTestFormData, scoutApi.AbstractFormData());
    assertEquals(1, annotationCopyTestFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(annotationCopyTestFormData, scoutApi.Generated());

    // fields of AnnotationCopyTestFormData
    assertEquals(1, annotationCopyTestFormData.fields().stream().count(), "field count of 'formdata.shared.ui.forms.AnnotationCopyTestFormData'");
    var serialVersionUID = assertFieldExist(annotationCopyTestFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(1, annotationCopyTestFormData.methods().stream().count(), "method count of 'formdata.shared.ui.forms.AnnotationCopyTestFormData'");
    var getFirst = assertMethodExist(annotationCopyTestFormData, "getFirst");
    assertMethodReturnType(getFirst, "formdata.shared.ui.forms.AnnotationCopyTestFormData$First");
    assertEquals(0, getFirst.annotations().stream().count(), "annotation count");

    assertEquals(1, annotationCopyTestFormData.innerTypes().stream().count(), "inner types count of 'AnnotationCopyTestFormData'");
    // type First
    var first = assertTypeExists(annotationCopyTestFormData, "First");
    assertHasFlags(first, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(first, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");
    assertEquals(1, first.annotations().stream().count(), "annotation count");
    assertAnnotation(first, "formdata.shared.SharedAnnotation");

    // fields of First
    assertEquals(1, first.fields().stream().count(), "field count of 'formdata.shared.ui.forms.AnnotationCopyTestFormData$First'");
    var serialVersionUID1 = assertFieldExist(first, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, first.methods().stream().count(), "method count of 'formdata.shared.ui.forms.AnnotationCopyTestFormData$First'");

    assertEquals(0, first.innerTypes().stream().count(), "inner types count of 'First'");
  }

}
