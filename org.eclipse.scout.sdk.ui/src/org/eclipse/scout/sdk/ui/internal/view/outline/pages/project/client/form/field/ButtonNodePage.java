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

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.KeyStrokeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.MenuTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.client.ui.form.field.AbstractFormFieldNodePage;

/**
 * <h3>ButtonNodePage</h3> ...
 */
public class ButtonNodePage extends AbstractFormFieldNodePage {

  public ButtonNodePage() {

  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.BUTTON_NODE_PAGE;
  }

  @Override
  public void loadChildrenImpl() {
    new KeyStrokeTablePage(this, getType());
    new MenuTablePage(this, getType());
  }

  @Override
  public Action createRenameAction() {
    return new FormFieldRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), ScoutIdeProperties.SUFFIX_BUTTON);
  }

}
