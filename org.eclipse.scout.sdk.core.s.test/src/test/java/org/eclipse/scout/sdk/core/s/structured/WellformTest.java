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
package org.eclipse.scout.sdk.core.s.structured;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

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
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WellformTest {

  @Test
  public void testEmptyCommentRegex() {
    Assert.assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n   *\n   */").matches());
    Assert.assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n\t*\n\t*/").matches());

    Assert.assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n   * \n   */").matches());
    Assert.assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n\t* \n\t*/").matches());

    Assert.assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n   *\n   *\n   *\n   */").matches());
    Assert.assertTrue(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n\t*\n\t*\n\t*/").matches());

    Assert.assertFalse(Wellformer.EMPTY_COMMENT_REGEX.matcher("/**\n   * whatever \n   */").matches());
  }

  @Test
  public void testWellform() {
    Wellformer wf = new Wellformer("\n", true);

    IJavaEnvironment clientJavaEnvironment = CoreScoutTestingUtils.createClientJavaEnvironment();

    Class<?>[] testingClasses = new Class[]{
        AnnotationCopyTestForm.class,
        BaseWithExtendedTableForm.class,
        FormWithGroupBoxesForm.class,
        ListBoxForm.class,
        SimpleForm.class,
        TableFieldForm.class,
        ExtendedTablePage.class
    };

    for (Class<?> c : testingClasses) {
      StringBuilder out = new StringBuilder();
      String name = c.getName();
      IType type = clientJavaEnvironment.findType(name);

      String cuSource = type.compilationUnit().source().toString();

      wf.buildSource(type, out);
      String newCuSource = cuSource.substring(0, type.source().start()) + out.toString();
      CoreTestingUtils.assertNoCompileErrors(clientJavaEnvironment, name, newCuSource);
    }
  }
}
