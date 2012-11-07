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
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.create.CreateTemplateAction;
import org.eclipse.scout.sdk.ui.action.create.RadioButtonNewAction;
import org.eclipse.scout.sdk.ui.action.delete.BoxDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.FormFieldRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

/**
 * <h3>{@link RadioButtonGroupNodePage}</h3> ...
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
    // TODO check only subtypes of IRadioButton
    super.loadChildrenImpl();
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof RadioButtonNewAction) {
      ((RadioButtonNewAction) menu).setType(getType());
    }
    if (menu instanceof BoxDeleteAction) {
      menu.setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.RadiobuttonGroupRemove));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{FormFieldRenameAction.class, ShowJavaReferencesAction.class, RadioButtonNewAction.class,
        BoxDeleteAction.class, CreateTemplateAction.class};
  }
}
