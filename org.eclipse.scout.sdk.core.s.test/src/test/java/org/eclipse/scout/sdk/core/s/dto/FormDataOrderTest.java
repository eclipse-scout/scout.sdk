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

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.s.testing.ScoutSdkAssertions.assertEqualsVersionedRefFile;

import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.junit.jupiter.api.Test;

public class FormDataOrderTest {
  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/s/dto/";

  @Test
  public void testSortOrderStabilityOfFormData() {
    createFormDataAssertNoCompileErrors("formdata.client.ui.forms.SimpleForm", FormDataOrderTest::compareWithRefFile);
  }

  protected static void compareWithRefFile(IType createdFormData) {
    assertEqualsVersionedRefFile(createdFormData.javaEnvironment(), REF_FILE_FOLDER, "FormDataOrder.txt", createdFormData.requireCompilationUnit().source().orElseThrow().asCharSequence());
  }
}
