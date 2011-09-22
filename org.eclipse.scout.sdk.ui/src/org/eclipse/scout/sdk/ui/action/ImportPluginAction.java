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
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.bundle.BundleImportWizard;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class ImportPluginAction extends AbstractWizardAction {

  private IScoutProject m_resource;

  public ImportPluginAction() {
    super(Texts.get("ImportPlugin"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd), null, false, Category.IMPORT);
  }

  public void setScoutProject(IScoutProject resource) {
    m_resource = resource;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    return new BundleImportWizard(m_resource);
  }
}
