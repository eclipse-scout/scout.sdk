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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.wizard.form.fields.radiobutton.RadioButtonNewWizard;

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
  public void fillContextMenu(IMenuManager manager) {
    RadioButtonNewWizard wizard = new RadioButtonNewWizard();
    wizard.initWizard(getType());
    manager.add(new WizardAction("New Radio Button...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.RadiobuttonAdd), wizard));
    super.fillContextMenu(manager);
  }

  @Override
  public Action createDeleteAction() {
    Action deleteAction = super.createDeleteAction();
    if (deleteAction != null) {
      deleteAction.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.RadiobuttonGroupRemove));
    }
    return deleteAction;
  }
}
