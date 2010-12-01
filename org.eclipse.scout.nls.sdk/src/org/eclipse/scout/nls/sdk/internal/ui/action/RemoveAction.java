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
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.ITranslationFile;
import org.eclipse.scout.nls.sdk.util.concurrent.AbstractJob;
import org.eclipse.swt.widgets.Display;

public class RemoveAction extends Action {
  private NlsProject m_nlsProject;
  private String m_key;
  private IStatus m_status;

  public RemoveAction(String name, NlsProject project, String key) {
    super(name);
    m_nlsProject = project;
    m_key = key;
    setImageDescriptor(NlsCore.getImageDescriptor(NlsCore.TextRemove));
  }

  @Override
  public void run() {
    AbstractJob job = new AbstractJob("update translation files", new Object[]{m_key}) {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        String key = (String) args[0];
        IStatus status = null;
        for (ITranslationFile file : m_nlsProject.getAllTranslationFiles()) {
          status = file.remove(key, monitor);
          if (!status.isOK()) {
            new NlsStatusDialog(Display.getDefault().getActiveShell(), status).open();
            break;
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
      // if (m_status.isOK()) {
      // m_nlsProject.removeRow(m_key);
      // }
    }
    catch (InterruptedException e) {
      NlsCore.logError("cold not remove the row key: " + m_key + " in translation files", e);
    }

  }

  public IStatus getStatus() {
    return m_status;
  }

}
