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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field;

import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.delete.FormFieldDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.MenuTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;
import org.eclipse.scout.sdk.util.SdkProperties;

/**
 * <h3>ButtonNodePage</h3>
 */
public class ButtonNodePage extends AbstractFormFieldNodePage {

  public ButtonNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Button));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.BUTTON_NODE_PAGE;
  }

  @Override
  protected void loadChildrenImpl() {
    new MenuTablePage(this, getType());
    new KeyStrokeTablePage(this, getType());
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof FormFieldRenameAction) {
      FormFieldRenameAction a = (FormFieldRenameAction) menu;
      a.setReadOnlySuffix(SdkProperties.SUFFIX_BUTTON);
    }
    else if (menu instanceof FormFieldDeleteAction) {
      menu.setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ButtonRemove));
    }
  }
}
