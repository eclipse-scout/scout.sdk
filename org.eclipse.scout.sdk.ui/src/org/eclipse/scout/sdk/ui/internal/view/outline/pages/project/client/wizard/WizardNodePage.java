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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.operation.util.TypeDeleteOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;

/**
 * <h3>WizardNodePage</h3> ...
 */
public class WizardNodePage extends AbstractScoutTypePage {

  public WizardNodePage(IPage parent, IType wizardType) {
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

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new Separator());
    manager.add(new WellformAction(getOutlineView().getSite().getShell(), "Wellform wizard...", new WellformScoutTypeOperation(getType(), true)));
  }

  @Override
  public Action createRenameAction() {
    return new TypeRenameAction(getOutlineView().getSite().getShell(), "Rename...", getType(), ScoutIdeProperties.SUFFIX_WIZARD);
  }

  @Override
  public Action createDeleteAction() {
    TypeDeleteOperation delOp = new TypeDeleteOperation(getType());
    DeleteAction deleteAction = new DeleteAction("Delete...", getOutlineView().getSite().getShell(), delOp);
    deleteAction.setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.WizardRemove));
    return deleteAction;
  }

}
