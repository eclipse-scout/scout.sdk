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
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.NlsEntryModifyDialog;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.widgets.Display;

/** <h4>NlsEntryNewAction</h4> */
public class NlsEntryModifyAction extends AbstractWorkspaceAction {

  private NlsEntry m_entry;
  private final INlsProject m_project;

  public NlsEntryModifyAction(NlsEntry entry, INlsProject project) {
    super("Modify Entry...", true);
    m_entry = entry;
    if (m_entry == null) {
      m_entry = new NlsEntry("", project);
    }
    m_project = project;
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
    NlsEntryModifyDialog dialog = new NlsEntryModifyDialog(Display.getDefault().getActiveShell(), "Modify Entry", m_entry, m_project);
    m_entry = dialog.show();
    return m_entry != null;
  }

  public NlsEntry getEntry() {
    return m_entry;
  }
}
