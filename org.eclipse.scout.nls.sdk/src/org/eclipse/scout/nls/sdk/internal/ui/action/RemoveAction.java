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
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.util.concurrent.AbstractJob;

public class RemoveAction extends Action {
  private INlsProject m_nlsProject;
  private IStatus m_status;
  private final INlsEntry[] m_entries;

  public RemoveAction(String name, INlsProject project, INlsEntry entry) {
    this(name, project, new INlsEntry[]{entry});
  }

  public RemoveAction(String name, INlsProject project, INlsEntry[] entries) {
    super(name);
    m_nlsProject = project;
    m_entries = entries;
    setImageDescriptor(NlsCore.getImageDescriptor(NlsCore.TextRemove));
  }

  @Override
  public void run() {
    AbstractJob job = new AbstractJob("update translations") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        return m_nlsProject.removeEntries(m_entries);
      }
    };
    job.setUser(false);
    job.schedule();

    try {
      job.join();
      m_status = job.getResult();
    }
    catch (InterruptedException e) {
      NlsCore.logError("cold not remove the row key: " + getVerbose(m_entries) + " in translation resources", e);
    }
  }

  private static String getVerbose(INlsEntry[] entries) {
    if (entries == null) {
      return "[no entries]";
    }
    StringBuilder builder = new StringBuilder("[");
    for (int i = 0; i < entries.length; i++) {
      builder.append("'" + entries[i].getKey() + "'");
      if (i + 1 != entries.length) {
        builder.append(", ");
      }
    }
    builder.append("]");
    return builder.toString();
  }

  public IStatus getStatus() {
    return m_status;
  }
}
