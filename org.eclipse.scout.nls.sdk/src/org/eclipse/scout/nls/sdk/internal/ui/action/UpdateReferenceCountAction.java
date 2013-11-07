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

import org.eclipse.jface.action.Action;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.NlsReferenceProvider;
import org.eclipse.scout.nls.sdk.internal.model.NlsTableModel;
import org.eclipse.scout.nls.sdk.internal.search.INlsKeySearchListener;
import org.eclipse.scout.nls.sdk.internal.search.NlsFindKeysJob;
import org.eclipse.scout.nls.sdk.internal.ui.editor.NlsTable;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.search.ui.text.Match;

/** <h4>UpdateReferenceCountAction</h4> */
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
    setImageDescriptor(NlsCore.getImageDescriptor(NlsCore.FindObject));
  }

  @Override
  public void run() {
    m_job = new NlsFindKeysJob(m_project, getText());
    m_job.getSearchRequstor().addFindReferencesListener(new P_JobListener());
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
        UpdateReferenceCountAction.this.setEnabled(true);
        m_tableModel.setReferenceProvider(new NlsReferenceProvider(m_job.getMatches()));
        m_table.refreshAll(false);
      }
    });
  }

  private class P_JobListener implements INlsKeySearchListener {
    @Override
    public void beginReporting() {
      handleBeginSearch();
    }

    @Override
    public void foundMatch(String key, Match match) {
    }

    @Override
    public void endReporting() {
      handleEndSearch();
    }
  }
}
