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
package org.eclipse.scout.sdk.core.s.structured;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutClientJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import formdata.client.ui.desktop.outline.pages.ExtendedTablePage;
import formdata.client.ui.forms.AnnotationCopyTestForm;
import formdata.client.ui.forms.BaseWithExtendedTableForm;
import formdata.client.ui.forms.FormWithGroupBoxesForm;
import formdata.client.ui.forms.ListBoxForm;
import formdata.client.ui.forms.SimpleForm;
import formdata.client.ui.forms.TableFieldForm;

/**
 * <h3>{@link WellformTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
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

    Class<?>[] testingClasses = new Class[]{
        AnnotationCopyTestForm.class,
        BaseWithExtendedTableForm.class,
        FormWithGroupBoxesForm.class,
        ListBoxForm.class,
        SimpleForm.class,
        TableFieldForm.class,
        ExtendedTablePage.class
    };

    for (var c : testingClasses) {
      var out = new StringBuilder();
      var name = c.getName();
      var type = env.requireType(name);

      var cuSource = type.requireCompilationUnit().source().get().asCharSequence();

      wf.buildSource(type, out);
      var newCuSource = cuSource.toString().substring(0, type.source().get().start()) + out;
      assertNoCompileErrors(env, type.qualifier(), type.elementName(), newCuSource);
    }
  }
}
