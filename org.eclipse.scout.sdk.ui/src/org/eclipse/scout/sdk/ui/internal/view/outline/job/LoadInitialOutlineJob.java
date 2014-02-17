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
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.AbstractWorkspaceBlockingJob;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerPart;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class LoadInitialOutlineJob extends AbstractWorkspaceBlockingJob {
  private ScoutExplorerPart m_view;

  public LoadInitialOutlineJob(ScoutExplorerPart view) {
    super(Texts.get("LoadingScoutExplorer") + "...");
    m_view = view;
  }

  @Override
  protected void run(final IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    Control c = m_view.getTreeViewer().getControl();
    if (c.isDisposed()) return;
    //
    Display display = c.getDisplay();
    final IPage rootPage = m_view.getRootPage();
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
      if (rootPage != null) {
        loadNodeRec(rootPage, monitor);
      }
    }
    finally {
      // gui thread
      display.asyncExec(new Runnable() {
        @Override
        public void run() {
          if (!m_view.getTreeViewer().getTree().isDisposed()) {
            if (!monitor.isCanceled()) {
              m_view.expandAndSelectProjectLevel();
              m_view.getTreeViewer().refresh();
              m_view.getViewContentProvider().setAutoLoadChildren(true);
            }
            m_view.getTreeViewer().getControl().setCursor(null);
          }

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
      if (monitor.isCanceled()) {
        return;
      }
      monitor.subTask("Loading " + page.getName() + "...");
      if (!page.isChildrenLoaded()) {
        page.loadChildren();
      }
      // load children
      for (IPage childPage : page.getChildArray()) {
        loadNodeRec(childPage, monitor);
        if (monitor.isCanceled()) {
          return;
        }
      }
    }
  }
}
