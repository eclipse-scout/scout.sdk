/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
 * <h3>{@link ReplacingFormTest}</h3> Tests that a FormData has an @Replace annotation if the corresponding form as
 * an @Replace annotation.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class ReplacingFormTest {
  @Test
  public void testCreateFormData() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors("formdata.client.ui.forms.ReplacingForm");
    testApiOfReplacingFormData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfReplacingFormData(IType replacingFormData) {
    SdkAssert.assertHasFlags(replacingFormData, 1);
    SdkAssert.assertHasSuperTypeSignature(replacingFormData, "Lformdata.shared.ui.forms.AnnotationCopyTestFormData;");
    Assert.assertEquals("annotation count", 2, replacingFormData.getAnnotations().size());
    SdkAssert.assertAnnotation(replacingFormData, "org.eclipse.scout.commons.annotations.Replace");
    SdkAssert.assertAnnotation(replacingFormData, "javax.annotation.Generated");

    // fields of ReplacingFormData
    Assert.assertEquals("field count of 'formdata.shared.ui.forms.ReplacingFormData'", 1, replacingFormData.getFields().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(replacingFormData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID.getAnnotations().size());

    Assert.assertEquals("method count of 'formdata.shared.ui.forms.ReplacingFormData'", 1, replacingFormData.getMethods().size());
    IMethod replacingFormData1 = SdkAssert.assertMethodExist(replacingFormData, "ReplacingFormData", new String[]{});
    Assert.assertTrue(replacingFormData1.isConstructor());
    Assert.assertEquals("annotation count", 0, replacingFormData1.getAnnotations().size());

    Assert.assertEquals("inner types count of 'ReplacingFormData'", 0, replacingFormData.getTypes().size());
  }
}
