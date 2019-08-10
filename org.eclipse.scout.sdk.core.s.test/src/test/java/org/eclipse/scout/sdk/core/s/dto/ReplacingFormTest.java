/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.ReplacingForm;

/**
 * <h3>{@link ReplacingFormTest}</h3> Tests that a FormData has an @Replace annotation if the corresponding form as
 * an @Replace annotation.
 *
 * @since 5.1.0
 */
public class ReplacingFormTest {
  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(ReplacingForm.class.getName(), ReplacingFormTest::testApiOfReplacingFormData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfReplacingFormData(IType replacingFormData) {
    assertHasFlags(replacingFormData, 1);
    assertHasSuperClass(replacingFormData, "formdata.shared.ui.forms.AnnotationCopyTestFormData");
    assertEquals(2, replacingFormData.annotations().stream().count(), "annotation count");
    assertAnnotation(replacingFormData, "org.eclipse.scout.rt.platform.Replace");
    assertAnnotation(replacingFormData, "javax.annotation.Generated");

    // fields of ReplacingFormData
    assertEquals(1, replacingFormData.fields().stream().count(), "field count of 'formdata.shared.ui.forms.ReplacingFormData'");
    IField serialVersionUID = assertFieldExist(replacingFormData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(0, replacingFormData.methods().stream().count(), "method count of 'formdata.shared.ui.forms.ReplacingFormData'");

    assertEquals(0, replacingFormData.innerTypes().stream().count(), "inner types count of 'ReplacingFormData'");
  }
}
