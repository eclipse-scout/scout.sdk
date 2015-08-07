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
package org.eclipse.scout.sdk.s2e.nls.internal.ui.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.s2e.nls.INlsIcons;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.dialog.NlsEntryNewDialog;
import org.eclipse.scout.sdk.s2e.nls.model.NlsEntry;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h4>NlsEntryNewAction</h4>
 */
public class NlsEntryNewAction extends AbstractWorkspaceAction {

  private NlsEntry m_entry;
  private NlsEntry m_initialEntry;
  private INlsProject m_project;
  private final Shell m_parentShell;
  private final boolean m_showProjectList;

  public NlsEntryNewAction(Shell parentShell, INlsProject project, boolean showProjectList) {
    this(parentShell, project, null, showProjectList);
  }

  public NlsEntryNewAction(Shell parentShell, INlsProject project, NlsEntry entry, boolean showProjectList) {
    super("New Entry...", true);
    m_parentShell = parentShell;
    m_initialEntry = entry;
    m_project = project;
    m_showProjectList = showProjectList;
    setImageDescriptor(NlsCore.getImageDescriptor(INlsIcons.TEXT_ADD));
    setEnabled(project != null && !project.isReadOnly());
  }

  @Override
  protected boolean interactWithUi() {
    if (m_project.isReadOnly()) {
      MessageBox box = new MessageBox(m_parentShell, SWT.ICON_INFORMATION | SWT.OK);
      box.setMessage("The NLS Project is read-only. Check that you have a valid NLS Project in your workspace.");
      box.open();
      return false;
    }

    if (m_initialEntry == null) {
      m_entry = new NlsEntry("", m_project);
    }
    else {
      m_entry = m_initialEntry;
    }
    NlsEntryNewDialog dialog = new NlsEntryNewDialog(m_parentShell, m_entry, m_project, m_showProjectList);
    m_entry = dialog.show();
    m_project = dialog.getNlsProject();
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
