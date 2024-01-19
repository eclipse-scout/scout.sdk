/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.structured;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutClientJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link WellformTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWithJavaEnvironmentFactory(ScoutClientJavaEnvironmentFactory.class)
public class WellformTest {

  @Test
  public void testEmptyCommentRegex() {
    assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n   *\n   */").matches());
    assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n\t*\n\t*/").matches());

    assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n   * \n   */").matches());
    assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n\t* \n\t*/").matches());

    assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n   *\n   *\n   *\n   */").matches());
    assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n\t*\n\t*\n\t*/").matches());

    assertFalse(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n   * whatever \n   */").matches());
  }

  @Test
  public void testWellform(IJavaEnvironment env) {
    var wf = new Wellformer("\n", true);

    var names = new String[]{
        "formdata.client.ui.forms.AnnotationCopyTestForm",
        "formdata.client.ui.forms.BaseWithExtendedTableForm",
        "formdata.client.ui.forms.FormWithGroupBoxesForm",
        "formdata.client.ui.forms.ListBoxForm",
        "formdata.client.ui.forms.SimpleForm",
        "formdata.client.ui.forms.TableFieldForm",
        "formdata.client.ui.desktop.outline.pages.ExtendedTablePage"
    };

    for (var name : names) {
      var out = new StringBuilder();
      var type = env.requireType(name);

      var cuSource = type.requireCompilationUnit().source().orElseThrow().asCharSequence();

      wf.buildSource(type, out);
      var newCuSource = cuSource.toString().substring(0, type.source().orElseThrow().start()) + out;
      assertNoCompileErrors(env, newCuSource, type.qualifier(), type.elementName());
    }
  }
}
