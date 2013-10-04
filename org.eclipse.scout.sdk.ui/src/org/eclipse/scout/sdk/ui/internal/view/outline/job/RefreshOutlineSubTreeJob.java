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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scout.sdk.jobs.AbstractWorkspaceBlockingJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerPart;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ProjectsTablePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class RefreshOutlineSubTreeJob extends AbstractWorkspaceBlockingJob {
  public static final String SELECTION_PREVENTER = "selectionPreventer";
  private ScoutExplorerPart m_view;
  private P_BackupNode[] m_backupTree;
  private ITreeSelection m_backupedSelection;

  public RefreshOutlineSubTreeJob(ScoutExplorerPart view, String name) {
    super(name);
    m_view = view;
    setRule(ResourcesPlugin.getWorkspace().getRoot());
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (monitor.isCanceled()) {
      return;
    }
    final IPage[] dirtyStructureRoots = m_view.fetchDirtyStructurePages();
    if (dirtyStructureRoots.length == 0) {
      return;
    }
    //
    Display display = ScoutSdkUi.getDisplay();
    final TreeViewer treeViewer = m_view.getTreeViewer();
    final Control treeControl = treeViewer.getControl();
    final Cursor waitCursor = new Cursor(display, SWT.CURSOR_WAIT);
    try {
      m_backupTree = new P_BackupNode[dirtyStructureRoots.length];
      if (dirtyStructureRoots.length > 0) {
        if (treeControl == null || treeControl.isDisposed()) {
          return;
        }
        // gui thread
        display.syncExec(new Runnable() {
          @Override
          public void run() {
            if (treeControl == null || treeControl.isDisposed()) {
              return;
            }
            treeControl.setCursor(waitCursor);
            m_view.getViewContentProvider().setAutoLoadChildren(false);
            m_backupedSelection = (ITreeSelection) treeViewer.getSelection();
            for (int i = 0; i < m_backupTree.length; i++) {
              m_backupTree[i] = new P_BackupNode(null, dirtyStructureRoots[i]);
            }
            try {
              treeViewer.setData(SELECTION_PREVENTER, this);
              treeViewer.setSelection(null, false); // remove the selection. we will restore it later. prevents 'widget disposed' errors during refresh later on.
            }
            finally {
              treeViewer.setData(SELECTION_PREVENTER, null);
            }
          }
        });
        // model thread
        for (int i = 0; i < m_backupTree.length; i++) {
          if (m_backupTree[i] != null) {
            m_backupTree[i].refreshStructure(dirtyStructureRoots[i]);
          }
        }
      }
    }
    finally {
      try {
        // gui thread
        if (treeControl == null || treeControl.isDisposed()) {
          return;
        }
        display.syncExec(new Runnable() {
          @Override
          public void run() {
            try {
              if (treeControl == null || treeControl.isDisposed()) {
                return;
              }
              if (dirtyStructureRoots.length > 0) {
                try {
                  treeViewer.setData(SELECTION_PREVENTER, this);

                  for (IPage p : dirtyStructureRoots) {
                    treeViewer.refresh(p, true);
                  }
                  for (int i = 0; i < m_backupTree.length; i++) {
                    m_backupTree[i].restoreGui(dirtyStructureRoots[i]);
                  }
                }
                finally {
                  treeViewer.setData(SELECTION_PREVENTER, null);
                }
                // restore selection
                restoreSelectionInUiThread();
              }
              m_view.getViewContentProvider().setAutoLoadChildren(true);
            }
            finally {
              treeControl.setCursor(null);
            }
          }
        });
      }
      finally {
        waitCursor.dispose();
      }
    }
  }

  private void restoreSelectionInUiThread() {
    TreePath[] paths = m_backupedSelection.getPaths();
    ArrayList<TreePath> newPaths = new ArrayList<TreePath>(paths.length);
    for (TreePath p : paths) {
      ArrayList<Object> newSegments = new ArrayList<Object>(p.getSegmentCount());
      for (int i = 0; i < p.getSegmentCount(); i++) {
        Object segment = p.getSegment(i);
        if (segment instanceof ITypePage) {
          if (((ITypePage) segment).getType().exists()) {
            newSegments.add(segment);
          }
          else {
            break;
          }
        }
        else {
          newSegments.add(segment);
        }
      }
      newPaths.add(new TreePath(newSegments.toArray()));
    }

    TreeViewer treeViewer = m_view.getTreeViewer();
    if (!treeViewer.getControl().isDisposed() && !treeViewer.getTree().isDisposed()) {
      treeViewer.setSelection(new TreeSelection(newPaths.toArray(new TreePath[newPaths.size()])));
    }
  }

  private class P_BackupNode {
    private String m_name;
    private boolean m_expanded;
    private HashMap<String, P_BackupNode> m_children = new HashMap<String, P_BackupNode>();

    public P_BackupNode(P_BackupNode parent, IPage p) {
      m_name = p.getName();
      m_expanded = m_view.getTreeViewer().getExpandedState(p);
      if (parent != null) {
        parent.m_children.put(m_name, this);
      }
      if (m_expanded) {
        for (IPage childPage : p.getChildren()) {
          new P_BackupNode(this, childPage);
        }
      }
    }

    /**
     * must be running in java thread
     */
    public void refreshStructure(IPage p) {
      if (p.getParent() != null || (p instanceof ProjectsTablePage)) {
        p.unloadChildren();
        if (m_expanded) {
          p.loadChildren();
          for (IPage childPage : p.getChildren()) {
            P_BackupNode node = m_children.get(childPage.getName());
            if (node != null) {
              node.refreshStructure(childPage);
            }
          }
        }
      }
    }

    /**
     * must be running in gui thread
     */
    public void restoreGui(IPage p) {
      if (m_expanded) {
        m_view.getTreeViewer().setExpandedState(p, true);
        for (IPage childPage : p.getChildren()) {
          P_BackupNode node = m_children.get(childPage.getName());
          if (node != null) {
            node.restoreGui(childPage);
          }
        }
      }
    }

  } // end class P_BackupNode

}
