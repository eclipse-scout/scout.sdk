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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.util.wellform.WellformWizardsOperation;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.WellformAction;
import org.eclipse.scout.sdk.ui.action.create.WizardNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>WizardTablePage</h3>
 */
public class WizardTablePage extends AbstractPage {

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

  @Override
  protected void loadChildrenImpl() {
    IType iWizard = TypeUtility.getType(IRuntimeClasses.IWizard);

    if (m_wizardHierarchy == null) {
      m_wizardHierarchy = TypeUtility.getPrimaryTypeHierarchy(iWizard);
      m_wizardHierarchy.addHierarchyListener(getPageDirtyListener());
    }
    Set<IType> searchForms = m_wizardHierarchy.getAllSubtypes(iWizard, ScoutTypeFilters.getClassesInScoutBundles(getScoutBundle()), TypeComparators.getTypeNameComparator());
    for (IType searchForm : searchForms) {
      new WizardNodePage(this, searchForm);
    }
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof WellformAction) {
      WellformAction action = (WellformAction) menu;
      action.init(getScoutBundle());
      action.setLabel(Texts.get("WellformAllWizards"));
      action.setOperation(new WellformWizardsOperation(getScoutBundle()));
    }
    else if (menu instanceof WizardNewAction) {
      ((WizardNewAction) menu).setScoutResource(getScoutBundle());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WellformAction.class, WizardNewAction.class};
  }
}
