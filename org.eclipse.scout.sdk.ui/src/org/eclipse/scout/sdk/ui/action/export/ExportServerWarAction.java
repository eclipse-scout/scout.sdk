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
package org.eclipse.scout.sdk.ui.action.export;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.export.ExportServerWarWizard;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class ExportServerWarAction extends AbstractWizardAction {

  private IScoutProject m_res;

  public ExportServerWarAction() {
    super(Texts.get("ExportAsWarFile"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServerBundleExport), null, false, Category.IMPORT);
  }

  public void setScoutProject(IScoutProject res) {
    m_res = res;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    return new ExportServerWarWizard(m_res);
  }
}
