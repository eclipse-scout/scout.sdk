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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.util.wellform.WellformWizardsOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.wizard.wizard.WizardNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

/**
 * <h3>WizardTablePage</h3> ...
 */
public class WizardTablePage extends AbstractPage {

  final IType iWizard = ScoutSdk.getType(RuntimeClasses.IWizard);
  private ICachedTypeHierarchy m_wizardHierarchy;

  public WizardTablePage(IPage parent) {
    setName(Texts.get("WizardTablePage"));
    setParent(parent);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Wizards));
  }

  @Override
  public void unloadPage() {
    if (m_wizardHierarchy != null) {
      m_wizardHierarchy.removeHierarchyListener(getPageDirtyListener());
    }
    super.unloadPage();
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache && m_wizardHierarchy != null) {
      m_wizardHierarchy.invalidate();
    }
    super.refresh(clearCache);
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.WIZARD_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  /**
   * client bundle
   */
  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void loadChildrenImpl() {
    if (m_wizardHierarchy == null) {
      m_wizardHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iWizard);
      m_wizardHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    IType[] searchForms = m_wizardHierarchy.getAllSubtypes(iWizard, TypeFilters.getClassesInProject(getScoutResource().getJavaProject()), TypeComparators.getTypeNameComparator());
    for (IType searchForm : searchForms) {
      new WizardNodePage(this, searchForm);
    }
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new Separator());
    manager.add(new WellformAction(getOutlineView().getSite().getShell(), "Wellform all wizards...", new WellformWizardsOperation(getScoutResource())));
  }

  @Override
  public Action createNewAction() {
    return new WizardAction(Texts.get("Action_newTypeX", "Wizard"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.WizardAdd), new WizardNewWizard(getScoutResource()));
  }

}
