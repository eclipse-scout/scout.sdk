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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.s2e.nls.INlsIcons;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;

public class RemoveAction extends Action {
  private final INlsProject m_nlsProject;
  private IStatus m_status;
  private final List<INlsEntry> m_entries;

  public RemoveAction(String name, INlsProject project, INlsEntry entry) {
    this(name, project, Arrays.asList(entry));
  }

  public RemoveAction(String name, INlsProject project, List<INlsEntry> entries) {
    super(name);
    m_nlsProject = project;
    m_entries = new ArrayList<>(entries);
    setImageDescriptor(NlsCore.getImageDescriptor(INlsIcons.TEXT_REMOVE));
  }

  @Override
  public void run() {
    Job job = new Job("update translations") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        return m_nlsProject.removeEntries(m_entries, monitor);
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

  private static String getVerbose(List<INlsEntry> entries) {
    if (entries == null) {
      return "[no entries]";
    }
    StringBuilder builder = new StringBuilder('[');
    Iterator<INlsEntry> iterator = entries.iterator();
    if (iterator.hasNext()) {
      builder.append('\'').append(iterator.next()).append('\'');
      while (iterator.hasNext()) {
        builder.append(", ").append('\'').append(iterator.next()).append('\'');
      }
    }
    builder.append(']');
    return builder.toString();
  }

  public IStatus getStatus() {
    return m_status;
  }
}
