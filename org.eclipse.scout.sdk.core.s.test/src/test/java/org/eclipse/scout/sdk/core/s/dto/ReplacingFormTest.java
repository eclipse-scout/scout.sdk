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
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ReplacingFormTest}</h3> Tests that a FormData has an @Replace annotation if the corresponding form as
 * an @Replace annotation.
 *
 * @since 5.1.0
 */
public class ReplacingFormTest {
  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.ReplacingForm", ReplacingFormTest::testApiOfReplacingFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfReplacingFormData(IType replacingFormData) {
    var scoutApi = replacingFormData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(replacingFormData, Flags.AccPublic);
    assertHasSuperClass(replacingFormData, "formdata.shared.ui.forms.AnnotationCopyTestFormData");
    assertEquals(2, replacingFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(replacingFormData, scoutApi.Replace());
    assertAnnotation(replacingFormData, scoutApi.Generated());

    // fields of ReplacingFormData
    assertEquals(1, replacingFormData.fields().stream().count(), "field count of 'formdata.shared.ui.forms.ReplacingFormData'");
    var serialVersionUID = assertFieldExist(replacingFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(0, replacingFormData.methods().stream().count(), "method count of 'formdata.shared.ui.forms.ReplacingFormData'");

    assertEquals(0, replacingFormData.innerTypes().stream().count(), "inner types count of 'ReplacingFormData'");
  }
}
