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
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerPart;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class LoadInitialOutlineProcess extends AbstractWorkspaceBlockingJob {
  private ScoutExplorerPart m_view;

  public LoadInitialOutlineProcess(ScoutExplorerPart view) {
    super("Loading outline");
    m_view = view;
  }

  public String getProcessName() {
    return "Loading outline";
  }

  @Override
  protected void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    Control c = m_view.getTreeViewer().getControl();
    if (c.isDisposed()) return;
    //
    Display display = c.getDisplay();
    final IPage rootPage = m_view.getViewContentProvider().getRoot();
    final Cursor waitCursor = new Cursor(display, SWT.CURSOR_WAIT);
    try {
      // gui thread
      display.syncExec(new Runnable() {
        @Override
        public void run() {
          m_view.getTreeViewer().getControl().setCursor(waitCursor);
          m_view.getViewContentProvider().setAutoLoadChildren(false);
        }
      });
      // model
      loadNodeRec(rootPage, monitor);
    }
    finally {
      // gui thread
      display.asyncExec(new Runnable() {
        @Override
        public void run() {
          m_view.getTreeViewer().refresh();
          expandLoadedNodeRec(rootPage);
          m_view.getViewContentProvider().setAutoLoadChildren(true);
          m_view.getTreeViewer().getControl().setCursor(null);
          waitCursor.dispose();
        }
      });
    }
  }

  /**
   * must be running in java thread
   */
  private void loadNodeRec(IPage page, IProgressMonitor monitor) {
    if (page.isInitiallyLoaded()) {
      monitor.subTask("Loading " + page.getName() + "...");
      if (!page.isChildrenLoaded()) {
        page.loadChildren();
      }
      // load children
      for (IPage childPage : page.getChildArray()) {
        loadNodeRec(childPage, monitor);
      }
      monitor.subTask("Loading complete");
    }
  }

  /**
   * must be running in gui thread
   */
  private void expandLoadedNodeRec(IPage page) {
    if (page.isChildrenLoaded()) {
      m_view.getTreeViewer().setExpandedState(page, true);
      for (IPage ch : page.getChildArray()) {
        expandLoadedNodeRec(ch);
      }
    }
  }

}
