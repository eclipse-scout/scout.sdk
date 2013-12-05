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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.wizard;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.util.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;
import org.eclipse.scout.sdk.util.SdkProperties;

/**
 * <h3>WizardNodePage</h3> ...
 */
public class WizardNodePage extends AbstractScoutTypePage {

  public WizardNodePage(IPage parent, IType wizardType) {
    super(SdkProperties.SUFFIX_WIZARD);
    setParent(parent);
    setType(wizardType);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Wizard));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.WIZARD_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredTitle";
  }

  @Override
  public void loadChildrenImpl() {
    new BeanPropertyTablePage(this, getType());
    new WizardStepTablePage(this, getType());
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{TypeRenameAction.class, ShowJavaReferencesAction.class, WellformAction.class, DeleteAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.setLabel(Texts.get("WellformWizard"));
      action.init(getScoutBundle(), getType());
      action.setOperation(new WellformScoutTypeOperation(getType(), true));
    }
    else if (menu instanceof DeleteAction) {
      DeleteAction action = (DeleteAction) menu;
      action.addType(getType());
      action.setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.WizardRemove));
    }
  }
}
