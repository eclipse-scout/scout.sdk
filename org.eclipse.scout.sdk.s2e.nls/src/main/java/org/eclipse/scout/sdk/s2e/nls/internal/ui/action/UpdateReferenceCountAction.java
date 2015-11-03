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

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.s2e.nls.INlsIcons;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.model.NlsReferenceProvider;
import org.eclipse.scout.sdk.s2e.nls.internal.search.NlsFindKeysJob;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.editor.NlsTable;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.editor.NlsTableModel;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;

/**
 * <h4>UpdateReferenceCountAction</h4>
 */
public class UpdateReferenceCountAction extends Action {
  private final INlsProject m_project;
  private final NlsTable m_table;
  private final NlsTableModel m_tableModel;
  private NlsFindKeysJob m_job;

  public UpdateReferenceCountAction(INlsProject project, NlsTable table, NlsTableModel model) {
    m_project = project;
    m_table = table;
    m_tableModel = model;
    setEnabled(project != null);
    setText("Show NLS Entry usage");
    setImageDescriptor(NlsCore.getImageDescriptor(INlsIcons.FIND_OBJECT));
  }

  @Override
  public void run() {
    m_job = new NlsFindKeysJob(m_project, getText());
    m_job.addJobChangeListener(new P_JobListener());
    m_job.schedule();
  }

  private void handleBeginSearch() {
    m_table.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        UpdateReferenceCountAction.this.setEnabled(false);
      }
    });
  }

  private void handleEndSearch() {
    m_table.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        if (m_table.isDisposed()) {
          return;
        }
        UpdateReferenceCountAction.this.setEnabled(true);
        m_tableModel.setReferenceProvider(new NlsReferenceProvider(m_job.getAllMatches()));
        m_table.refreshAll(false);
      }
    });
  }

  private class P_JobListener extends JobChangeAdapter {
    @Override
    public void scheduled(IJobChangeEvent event) {
      handleBeginSearch();
    }

    @Override
    public void done(IJobChangeEvent event) {
      handleEndSearch();
    }
  }
}
