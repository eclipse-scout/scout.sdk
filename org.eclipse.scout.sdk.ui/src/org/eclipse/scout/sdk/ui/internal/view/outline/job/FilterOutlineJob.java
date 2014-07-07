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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.jobs.AbstractWorkspaceBlockingJob;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerPart;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.swt.widgets.Display;

public class FilterOutlineJob extends AbstractWorkspaceBlockingJob {
  private final ScoutExplorerPart m_view;
  private final IPage m_page;

  public FilterOutlineJob(ScoutExplorerPart view, IPage page) {
    super("Filtering " + page.getName());
    m_view = view;
    m_page = page;
  }

  @Override
  protected void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    Display display = m_view.getTreeViewer().getControl().getDisplay();
    if (!m_page.isChildrenLoaded()) {
      m_page.loadChildren();
    }
    // gui thread
    display.syncExec(
        new Runnable() {
          @Override
          public void run() {
            m_view.getTreeViewer().refresh(m_page, true);
            m_view.getTreeViewer().setExpandedState(m_page, true);
          }
        }
        );
  }
}
