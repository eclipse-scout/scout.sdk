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
package org.eclipse.scout.sdk.ui.internal.view.outline;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.view.outline.job.RefreshOutlineLabelsJob;
import org.eclipse.scout.sdk.ui.internal.view.outline.job.RefreshOutlineSubTreeJob;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.IScoutSeverityListener;
import org.eclipse.scout.sdk.util.ScoutSeverityManager;

/**
 * <h3>DirtyUpdateManager</h3> Only use this class from OutlineView to schedule dirty nodes to be refreshed.
 * The tree update job blocks the workspace to ensure synchrony execution.
 */
public class DirtyUpdateManager {
  private final ScoutExplorerPart m_view;
  private LinkedList<IPage> m_structureRoots = new LinkedList<IPage>();
  private Job m_currentRefreshSubTreeProcess;
  private Job m_currentRefreshLabelsProcess;

  public DirtyUpdateManager(ScoutExplorerPart view) {
    m_view = view;
    m_currentRefreshSubTreeProcess = new RefreshOutlineSubTreeJob(m_view, Texts.get("Refreshing"));
    // add quality listener
    ScoutSeverityManager.getInstance().addQualityManagerListener(
        new IScoutSeverityListener() {
          @Override
          public void severityChanged(IResource r) {
            enqueueLabelsJob();
          }
        }
        );
  }

  public void notifyStructureDirty(IPage page) {
    synchronized (m_structureRoots) {
      // check if an ancestor of this node is already marked
      IPage p = page;
      while (p != null) {
        if (m_structureRoots.contains(p)) {
          return;
        }
        p = p.getParent();
      }
      // remove all nodes that have this new node as ancestor
      for (Iterator<IPage> it = m_structureRoots.iterator(); it.hasNext();) {
        p = it.next();
        while (p != null) {
          if (p == page) {
            it.remove();
            break;
          }
          p = p.getParent();
        }
      }
      m_structureRoots.add(page);
    }
    enqueueStructureJob();
  }

  private synchronized void enqueueStructureJob() {
    m_currentRefreshSubTreeProcess.cancel();
    m_currentRefreshSubTreeProcess.schedule(200);
  }

  private synchronized void enqueueLabelsJob() {
    if (m_currentRefreshLabelsProcess != null) {
      m_currentRefreshLabelsProcess.cancel();
      m_currentRefreshLabelsProcess = null;
    }
    m_currentRefreshLabelsProcess = new RefreshOutlineLabelsJob(m_view, Texts.get("Refreshing"));
    m_currentRefreshLabelsProcess.schedule(200);
  }

  public IPage[] fetchDirtyStructurePages() {
    synchronized (m_structureRoots) {
      if (m_structureRoots.size() > 0) {
        IPage[] pages = m_structureRoots.toArray(new IPage[m_structureRoots.size()]);
        m_structureRoots.clear();
        return pages;
      }
      else {
        return new IPage[0];
      }
    }
  }

}
