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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.WizardAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.ui.wizard.wizard.step.WizardStepNewWizard;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>WizardStepTablePage</h3> ...
 */
public class WizardStepTablePage extends AbstractPage {

  private final IType m_wizardType;
  final IType iWizardStep = ScoutSdk.getType(RuntimeClasses.IWizardStep);
  private InnerTypePageDirtyListener m_innerTypeListener;

  public WizardStepTablePage(IPage parent, IType wizardType) {
    m_wizardType = wizardType;
    setParent(parent);
    setName(Texts.get("WizardStepTablePage"));
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.WizardSteps));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.WIZARD_STEP_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void unloadPage() {
    super.unloadPage();
    if (m_innerTypeListener != null) {
      ScoutSdk.removeInnerTypeChangedListener(getWizardType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
  }

  @Override
  public void loadChildrenImpl() {
    if (m_innerTypeListener == null) {
      m_innerTypeListener = new InnerTypePageDirtyListener(this, iWizardStep);
      ScoutSdk.addInnerTypeChangedListener(getWizardType(), m_innerTypeListener);
    }
    IType[] wizardSteps = TypeUtility.getInnerTypes(getWizardType(), TypeFilters.getSubtypeFilter(iWizardStep), TypeComparators.getOrderAnnotationComparator());
    for (IType wizardStep : wizardSteps) {
      WizardStepNodePage childPage = new WizardStepNodePage();
      childPage.setParent(this);
      childPage.setType(wizardStep);
    }

  }

  @Override
  public Action createNewAction() {
    WizardStepNewWizard wizard = new WizardStepNewWizard();
    wizard.initWizard(getWizardType());
    return new WizardAction(Texts.get("Action_newTypeX", "Wizard step"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.WizardStepAdd), wizard);
  }

  public IType getWizardType() {
    return m_wizardType;
  }

}
