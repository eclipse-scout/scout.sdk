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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.create.WizardStepNewAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.InnerTypePageDirtyListener;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;

/**
 * <h3>WizardStepTablePage</h3> ...
 */
public class WizardStepTablePage extends AbstractPage {

  private final IType m_wizardType;
  final IType iWizardStep = TypeUtility.getType(RuntimeClasses.IWizardStep);
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
      TypeCacheAccessor.getJavaResourceChangedEmitter().removeInnerTypeChangedListener(getWizardType(), m_innerTypeListener);
      m_innerTypeListener = null;
    }
  }

  @Override
  public void loadChildrenImpl() {
    if (m_innerTypeListener == null) {
      m_innerTypeListener = new InnerTypePageDirtyListener(this, iWizardStep);
      TypeCacheAccessor.getJavaResourceChangedEmitter().addInnerTypeChangedListener(getWizardType(), m_innerTypeListener);
    }
    IType[] wizardSteps = TypeUtility.getInnerTypes(getWizardType(), TypeFilters.getSubtypeFilter(iWizardStep), ScoutTypeComparators.getOrderAnnotationComparator());
    for (IType wizardStep : wizardSteps) {
      WizardStepNodePage childPage = new WizardStepNodePage();
      childPage.setParent(this);
      childPage.setType(wizardStep);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{WizardStepNewAction.class};
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    ((WizardStepNewAction) menu).init(getWizardType());
  }

  public IType getWizardType() {
    return m_wizardType;
  }
}
