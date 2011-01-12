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
package org.eclipse.scout.nls.sdk.internal.ui.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.workspace.project.NlsProject;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.NlsStatusDialog;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.ITranslationFile;
import org.eclipse.scout.nls.sdk.model.workspace.util.NlsUtil;
import org.eclipse.scout.nls.sdk.util.concurrent.AbstractJob;
import org.eclipse.swt.widgets.Display;


public class RemoveAction extends Action {
  private NlsProject m_nlsProject;
  private IStatus m_status;
  private final INlsEntry[] m_entries;
  public RemoveAction(String name, NlsProject project, INlsEntry entry) {
    this(name, project, new INlsEntry[]{entry});
  }
  public RemoveAction(String name, NlsProject project, INlsEntry[] entries) {
    super(name);
    m_nlsProject = project;
    m_entries = entries;
  }

  @Override
  public void run() {
    AbstractJob job = new AbstractJob("update translation files") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        IStatus status = null;
        for (ITranslationFile file : m_nlsProject.getAllTranslationFiles()) {
          for(INlsEntry e : m_entries){
            status = file.remove(e.getKey(), monitor);
            if (!status.isOK()) {
              new NlsStatusDialog(Display.getDefault().getActiveShell(), status).open();
              break;
            }
          }
        }
        // m_nlsProject.commitChanges(monitor);
        return status;
      }
    };
    job.setUser(false);
    job.schedule();

    try {
      job.join();
      m_status = job.getResult();
    }
    catch (InterruptedException e) {
      NlsCore.logError("cold not remove the row key: " + NlsUtil.getVerbose(m_entries) + " in translation files", e);
    }
  }

  public IStatus getStatus() {
    return m_status;
  }

}
