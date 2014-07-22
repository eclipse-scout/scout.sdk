/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.FormFieldTemplateTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field.FormTemplateTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageTemplateTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

/**
 * <h3>{@link TemplateTablePage}</h3> ...
 *
 * @author Andreas Hoegger
 * @since 1.0.8 11.09.2010
 */
public class TemplateTablePage extends AbstractPage {

  public TemplateTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("Templates"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Templates));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.TEMPLATE_TABLE_PAGE;
  }

  @Override
  protected void loadChildrenImpl() {
    new FormTemplateTablePage(this);
    new PageTemplateTablePage(this);
    new FormFieldTemplateTablePage(this);
  }

  @Override
  public boolean isFolder() {
    return true;
  }
}
