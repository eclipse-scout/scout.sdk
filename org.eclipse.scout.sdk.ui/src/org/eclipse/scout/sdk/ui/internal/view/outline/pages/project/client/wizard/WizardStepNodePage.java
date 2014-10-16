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

import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.delete.WizardStepDeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.WizardStepRenameAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;
import org.eclipse.scout.sdk.util.SdkProperties;

/**
 * <h3>WizardStepNodePage</h3>
 */
public class WizardStepNodePage extends AbstractScoutTypePage {

  public WizardStepNodePage() {
    super(SdkProperties.SUFFIX_WIZARD_STEP);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.WizardStep));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.WIZARD_STEP_NODE_PAGE;
  }

  @Override
  protected String getMethodNameForTranslatedText() {
    return "getConfiguredTitle";
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    return newSet(WizardStepRenameAction.class, WizardStepDeleteAction.class, ShowJavaReferencesAction.class);
  }
}
