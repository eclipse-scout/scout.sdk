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
package org.eclipse.scout.sdk.ui.internal.view.outline.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerPart;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class RefreshOutlineLabelsJob extends Job {
  private ScoutExplorerPart m_view;

  public RefreshOutlineLabelsJob(ScoutExplorerPart view, String name) {
    super(name);
    m_view = view;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    Display display = ScoutSdkUi.getDisplay();
    final TreeViewer m_treeViewer = m_view.getTreeViewer();
    final Control m_treeControl = m_treeViewer.getControl();
    // gui thread
    if (m_treeControl == null || m_treeControl.isDisposed()) {
      return Status.CANCEL_STATUS;
    }
    display.asyncExec(new Runnable() {
      @Override
      public void run() {
        if (m_treeControl == null || m_treeControl.isDisposed()) {
          return;
        }
        m_treeViewer.refresh(true);
      }
    });
    return Status.OK_STATUS;
  }

}
