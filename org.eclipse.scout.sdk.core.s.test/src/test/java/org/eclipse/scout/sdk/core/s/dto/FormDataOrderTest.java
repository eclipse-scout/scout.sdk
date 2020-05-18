/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.forms.SimpleForm;

public class FormDataOrderTest {
  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/s/dto/";

  @Test
  public void testSortOrderStabilityOfFormData() {
    createFormDataAssertNoCompileErrors(SimpleForm.class.getName(), FormDataOrderTest::compareWithRefFile);
  }

  protected static void compareWithRefFile(IType createdFormData) {
    assertEqualsRefFile(REF_FILE_FOLDER + "FormDataOrder.txt", createdFormData.requireCompilationUnit().source().get().asCharSequence());
  }
}
