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
package org.eclipse.scout.sdk.core.s.derived;

import static org.eclipse.scout.sdk.core.s.derived.DtoUpdateHandler.findDataAnnotationForFormData;
import static org.eclipse.scout.sdk.core.s.derived.DtoUpdateHandler.findDataAnnotationForPageData;
import static org.eclipse.scout.sdk.core.s.derived.DtoUpdateHandler.findDataAnnotationForRowData;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutClientJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import formdata.client.extensions.MultiColumnExtension;
import formdata.client.extensions.ThirdIntegerColumn;
import formdata.client.ui.desktop.outline.pages.BaseTablePage;
import formdata.client.ui.desktop.outline.pages.PageWithTableExtension;
import formdata.client.ui.desktop.outline.pages.PageWithoutTableExtension;
import formdata.client.ui.forms.ListBoxForm;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutClientJavaEnvironmentFactory.class)
public class DtoUpdateHandlerTest {

  @Test
  public void testFindDataAnnotationForFormData(IJavaEnvironment env) {
    // no table page
    assertFalse(findDataAnnotationForFormData(env.requireType(ThirdIntegerColumn.class.getName())).isPresent());

    // IPageWithTable
    assertTrue(findDataAnnotationForFormData(env.requireType(ListBoxForm.class.getName())).isPresent());
  }

  @Test
  public void testFindDataAnnotationForPageData(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);

    // no table page
    assertFalse(findDataAnnotationForPageData(env.requireType(ThirdIntegerColumn.class.getName()), scoutApi).isPresent());

    // IPageWithTable
    assertTrue(findDataAnnotationForPageData(env.requireType(BaseTablePage.class.getName()), scoutApi).isPresent());
  }

  @Test
  public void testFindDataAnnotationForRowData(IJavaEnvironment env) {
    var scoutApi = env.requireApi(IScoutApi.class);

    // IColumn
    assertTrue(findDataAnnotationForRowData(env.requireType(ThirdIntegerColumn.class.getName()), scoutApi).isPresent());

    // ITableExtension
    assertTrue(findDataAnnotationForRowData(env.requireType(MultiColumnExtension.class.getName()), scoutApi).isPresent());

    // IPageWithTableExtension with nested ITableExtensions (may contain columns)
    assertTrue(findDataAnnotationForRowData(env.requireType(PageWithTableExtension.class.getName()), scoutApi).isPresent());

    // IPageWithTableExtension without nested ITableExtensions (may not contain columns)
    assertFalse(findDataAnnotationForRowData(env.requireType(PageWithoutTableExtension.class.getName()), scoutApi).isPresent());
  }

}
