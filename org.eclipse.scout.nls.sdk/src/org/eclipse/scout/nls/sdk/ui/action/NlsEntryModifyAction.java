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
package org.eclipse.scout.nls.sdk.ui.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.NlsEntryModifyDialog;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.widgets.Shell;

/** <h4>NlsEntryNewAction</h4> */
public class NlsEntryModifyAction extends AbstractWorkspaceAction {

  private NlsEntry m_entry;
  private final INlsProject m_project;
  private final Shell m_parentShell;

  public NlsEntryModifyAction(Shell parentShell, INlsEntry entry, INlsProject project) {
    super("Modify Entry...", true);
    m_entry = new NlsEntry(entry);
    m_project = project;
    m_parentShell = parentShell;
    setImageDescriptor(NlsCore.getImageDescriptor(NlsCore.Text));
  }

  @Override
  protected void execute(IProgressMonitor monitor) {
    if (getEntry() != null) {
      m_project.updateRow(getEntry(), monitor);
    }
  }

  @Override
  protected boolean interactWithUi() {
    NlsEntryModifyDialog dialog = new NlsEntryModifyDialog(m_parentShell, m_entry, m_project);
    m_entry = dialog.show();
    return m_entry != null;
  }

  public NlsEntry getEntry() {
    return m_entry;
  }
}
