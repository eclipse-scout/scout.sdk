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
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.WellformScoutTypeAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyTablePage;
import org.eclipse.scout.sdk.util.SdkProperties;

/**
 * <h3>WizardNodePage</h3>
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
  protected void loadChildrenImpl() {
    new BeanPropertyTablePage(this, getType());
    new WizardStepTablePage(this, getType());
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(TypeRenameAction.class, ShowJavaReferencesAction.class, WellformScoutTypeAction.class, DeleteAction.class);
  }
}
