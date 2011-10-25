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
package org.eclipse.scout.nls.sdk.internal.ui.wizard.importExport;

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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.widgets.Shell;

/**
 * <h4>NlsImportAction</h4>
 */
public class NlsImportAction extends Action {

  private final Shell m_parentShell;
  private final INlsProject m_nlsProject;

  /**
   * @param name
   * @param interactWithUi
   */
  public NlsImportAction(INlsProject nlsProject, Shell parentShell) {
    super("Import...");
    m_nlsProject = nlsProject;
    m_parentShell = parentShell;
    setImageDescriptor(NlsCore.getImageDescriptor("import"));
    setEnabled(nlsProject != null);
  }

  @Override
  public void run() {
    NlsImportWizard wizard = new NlsImportWizard(m_nlsProject);
    WizardDialog dialog = new WizardDialog(m_parentShell, wizard);
    dialog.setBlockOnOpen(true);
    dialog.open();
  }

}
