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

import java.util.Set;

import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.create.CreateTemplateAction;
import org.eclipse.scout.sdk.ui.action.create.RadioButtonNewAction;
import org.eclipse.scout.sdk.ui.action.delete.FormFieldDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.MenuTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

/**
 * <h3>{@link RadioButtonGroupNodePage}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 28.11.2010
 */
public class RadioButtonGroupNodePage extends AbstractBoxNodePage {

  public RadioButtonGroupNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.RadiobuttonGroup));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.RADIO_BUTTON_GROUP_NODE_PAGE;
  }

  @Override
  protected void loadChildrenImpl() {
    new MenuTablePage(this, getType());
    super.loadChildrenImpl();
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(FormFieldRenameAction.class, ShowJavaReferencesAction.class, RadioButtonNewAction.class,
        FormFieldDeleteAction.class, CreateTemplateAction.class);
  }
}
