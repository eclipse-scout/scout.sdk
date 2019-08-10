/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.NlsReferenceProvider;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.NlsTableController;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.search.NlsFindKeysJob;
import org.eclipse.swt.widgets.Display;

/**
 * <h4>UpdateReferenceCountAction</h4>
 */
public class UpdateReferenceCountAction extends Action {
  private final NlsTableController m_controller;
  private final Display m_display;

  public UpdateReferenceCountAction(NlsTableController controller, Display display) {
    m_controller = controller;
    m_display = display;
    setText("Show Translation Key usage");
    setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.Find));
  }

  @Override
  public void run() {
    NlsFindKeysJob job = new NlsFindKeysJob(m_controller.stack(), getText());
    job.addJobChangeListener(new P_JobListener());
    job.schedule();
  }

  private final class P_JobListener extends JobChangeAdapter {
    @Override
    public void scheduled(IJobChangeEvent event) {
      m_display.asyncExec(this::handleBeginSearch);
    }

    @Override
    public void done(IJobChangeEvent event) {
      m_display.asyncExec(() -> handleEndSearch((NlsFindKeysJob) event.getJob()));
    }

    private void handleBeginSearch() {
      setEnabled(false);
    }

    private void handleEndSearch(NlsFindKeysJob job) {
      setEnabled(true);
      IStatus status = job.getResult();
      if (status != null && status.isOK()) {
        m_controller.setReferenceProvider(new NlsReferenceProvider(job.getAllMatches()));
      }
    }
  }
}
