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
import org.eclipse.scout.nls.sdk.internal.ui.dialog.NlsEntryNewDialog;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.widgets.Shell;

/** <h4>NlsEntryNewAction</h4> */
public class NlsEntryNewAction extends AbstractWorkspaceAction {

  private NlsEntry m_entry;

  private final INlsProject m_project;

  private NlsEntry m_initialEntry;

  private final Shell m_parentShell;

  public NlsEntryNewAction(Shell parentShell, INlsProject project) {
    this(parentShell, project, null);
  }

  public NlsEntryNewAction(Shell parentShell, INlsProject project, NlsEntry entry) {
    super("New Entry...", true);
    m_parentShell = parentShell;
    m_initialEntry = entry;
    m_project = project;
    setImageDescriptor(NlsCore.getImageDescriptor(NlsCore.TextAdd));
    setEnabled(project != null);
  }

  @Override
  protected boolean interactWithUi() {
    if (m_initialEntry == null) {
      m_entry = new NlsEntry("", m_project);
    }
    else {
      m_entry = m_initialEntry;
    }
    NlsEntryNewDialog dialog = new NlsEntryNewDialog(m_parentShell, m_entry, m_project);
    m_entry = dialog.show();
    return m_entry != null;
  }

  @Override
  protected void execute(IProgressMonitor monitor) {
    if (getEntry() != null) {
      m_project.updateRow(getEntry(), monitor);
    }
  }

  public NlsEntry getEntry() {
    return m_entry;
  }
}
