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
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.jobs.OptionalWorkspaceBlockingRule;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ProjectsTablePage;
import org.eclipse.scout.sdk.ui.view.outline.DirtyUpdateManager;
import org.eclipse.scout.sdk.ui.view.outline.IDirtyManageable;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public class RefreshOutlineSubTreeJob extends Job {
  public static final String SELECTION_PREVENTER = "selectionPreventer";
  private final IDirtyManageable m_view;
  private final DirtyUpdateManager m_manager;
  private P_BackupNode[] m_backupTree;
  private ITreeSelection m_backupedSelection;

  public RefreshOutlineSubTreeJob(IDirtyManageable view, DirtyUpdateManager manager, String name) {
    super(name);
    m_manager = manager;
    m_view = view;
    setRule(new OptionalWorkspaceBlockingRule(false));
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    if (monitor.isCanceled()) {
      return Status.CANCEL_STATUS;
    }
    final IPage[] dirtyStructureRoots = m_manager.fetchDirtyStructurePages();
    if (dirtyStructureRoots.length == 0) {
      return Status.OK_STATUS;
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
          return Status.OK_STATUS;
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
        if (treeControl != null && !treeControl.isDisposed()) {
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
                if (treeControl != null && !treeControl.isDisposed()) {
                  treeControl.setCursor(null);
                }
              }
            }
          });
        }
      }
      finally {
        waitCursor.dispose();
        m_backupTree = null;
        m_backupedSelection = null;
      }
    }
    return Status.OK_STATUS;
  }

  private void restoreSelectionInUiThread() {
    TreePath[] paths = m_backupedSelection.getPaths();
    List<TreePath> newPaths = new ArrayList<TreePath>(paths.length);

    for (TreePath p : paths) {
      TreePath newPath = getNewPath(p);
      if (newPath.getSegmentCount() > 0) {
        newPaths.add(newPath);
      }
    }
    TreeSelection newTreeSelection = new TreeSelection(newPaths.toArray(new TreePath[newPaths.size()]));
    TreeViewer treeViewer = m_view.getTreeViewer();
    if (!treeViewer.getControl().isDisposed() && !treeViewer.getTree().isDisposed()) {
      treeViewer.setSelection(newTreeSelection);
    }
  }

  /**
   * Creates a new {@link TreePath} containing the new {@link IPage}s created after the reload of the children. The new
   * path points to the same location (where possible) as the old one but containing the new {@link IPage} instances.
   *
   * @param oldPath
   *          The old path to convert
   * @return The new path
   */
  private TreePath getNewPath(TreePath oldPath) {
    TreeItem item = null;
    ArrayList<Object> newSegments = new ArrayList<Object>(oldPath.getSegmentCount());
    for (int i = 0; i < oldPath.getSegmentCount(); i++) {
      TreeItem[] curItems = null;
      if (item == null) {
        curItems = m_view.getTreeViewer().getTree().getItems();
      }
      else {
        curItems = item.getItems();
      }

      item = findItemWithData(curItems, oldPath.getSegment(i));
      if (item == null) {
        break;
      }

      Object page = item.getData();
      if (page instanceof ITypePage) {
        if (((ITypePage) page).getType().exists()) {
          newSegments.add(page);
        }
        else {
          break;
        }
      }
      else {
        newSegments.add(page);
      }
    }

    return new TreePath(newSegments.toArray());
  }

  private TreeItem findItemWithData(TreeItem[] candidates, Object data) {
    if (data instanceof IPage) {
      IPage search = (IPage) data;
      for (TreeItem ti : candidates) {
        Object data2 = ti.getData();
        if (data2 instanceof IPage) {
          IPage p = (IPage) data2;

          // don't use equals() here because this includes the parent page which is already removed for the old page instances
          if (isSame(search, p)) {
            return ti;
          }
        }
      }
    }
    return null;
  }

  private boolean isSame(IPage a, IPage b) {
    if (a == b) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }

    // parents are available -> we can use the equals() method
    if (a.getParent() != null && b.getParent() != null) {
      return CompareUtility.equals(a, b);
    }

    // parents are not both available -> one has already been detached from the tree
    if (!a.getClass().equals(b.getClass())) {
      return false;
    }
    if (!CompareUtility.equals(a.getPageId(), b.getPageId())) {
      return false;
    }
    if (!CompareUtility.equals(a.getName(), b.getName())) {
      return false;
    }

    if (a instanceof ITypePage && b instanceof ITypePage) {
      return CompareUtility.equals(((ITypePage) a).getType(), (((ITypePage) b).getType()));
    }
    return true;
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
