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

/** <h4> NlsExportAction </h4>
 *
 * @author Andreas Hoegger
 * @since 1.1.0 (12.11.2010)
 *
 */
public class NlsExportAction extends Action {

  private final Shell m_parentShell;
  private final INlsProject m_nlsProject;

  public NlsExportAction(INlsProject nlsProject, Shell shell) {
    super("Export...");
    m_nlsProject = nlsProject;
    m_parentShell = shell;
    setImageDescriptor(NlsCore.getImageDescriptor(NlsCore.ICON_TOOL_EXPORT));
  }


  @Override
  public void run() {
    NlsExportWizard wizard = new NlsExportWizard(m_nlsProject);
    WizardDialog dialog = new WizardDialog(m_parentShell, wizard);
    dialog.setBlockOnOpen(true);
    dialog.open();
  }

}
