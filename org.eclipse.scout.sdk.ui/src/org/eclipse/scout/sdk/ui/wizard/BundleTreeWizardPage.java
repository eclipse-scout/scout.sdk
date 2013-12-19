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
package org.eclipse.scout.sdk.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.DndEvent;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeDndListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>BundleTreeWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 12.02.2010
 */
public class BundleTreeWizardPage extends AbstractWorkspaceWizardPage {

  private CheckableTree m_tree;
  private boolean m_treeListenersAttached;

  private final ITreeNode m_rootNode;
  private final List<ITreeDndListener> m_tempDndListeners;
  private final List<ICheckStateListener> m_tempCheckListeners;
  private final ITreeNodeFilter m_initialCheckedFilter;

  public BundleTreeWizardPage(String pageTitle, String message, ITreeNode rootNode, ITreeNodeFilter initialCheckedFilter) {
    super(BundleTreeWizardPage.class.getName());
    m_treeListenersAttached = false;
    m_rootNode = rootNode;
    m_tempDndListeners = new ArrayList<ITreeDndListener>();
    m_tempCheckListeners = new ArrayList<ICheckStateListener>();

    setDescription(message);
    setTitle(pageTitle);
    if (initialCheckedFilter == null) {
      initialCheckedFilter = NodeFilters.getAcceptNone();
    }
    m_initialCheckedFilter = initialCheckedFilter;
  }

  public synchronized void addCheckSelectionListener(ICheckStateListener listener) {
    if (m_treeListenersAttached) {
      m_tree.addCheckSelectionListener(listener);
    }
    else {
      m_tempCheckListeners.add(listener);
    }
  }

  public synchronized void removeCheckSelectionListener(ICheckStateListener listener) {
    if (m_treeListenersAttached) {
      m_tree.removeCheckSelectionListener(listener);
    }
    else {
      m_tempCheckListeners.remove(listener);
    }
  }

  public synchronized void addDndListener(ITreeDndListener listener) {
    if (m_treeListenersAttached) {
      m_tree.addDndListener(listener);
    }
    else {
      m_tempDndListeners.add(listener);
    }
  }

  public synchronized void removeDndListener(ITreeDndListener listener) {
    if (m_treeListenersAttached) {
      m_tree.removeDndListener(listener);
    }
    else {
      m_tempDndListeners.remove(listener);
    }
  }

  @Override
  protected void createContent(Composite parent) {
    m_tree = new CheckableTree(parent, getRootNode());
    m_tree.setChecked(TreeUtility.findNodes(getRootNode(), m_initialCheckedFilter));
    synchronized (this) {
      for (ITreeDndListener l : m_tempDndListeners) {
        m_tree.addDndListener(l);
      }
      m_tempDndListeners.clear();
      m_tree.addDndListener(new ITreeDndListener() {
        @Override
        public void validateTarget(DndEvent dndEvent) {
        }

        @Override
        public boolean isDragableNode(ITreeNode node) {
          return true;
        }

        @Override
        public void dndPerformed(DndEvent dndEvent) {
          pingStateChanging();
        }
      });

      for (ICheckStateListener l : m_tempCheckListeners) {
        m_tree.addCheckSelectionListener(l);
      }
      m_tempCheckListeners.clear();
      m_tree.addCheckSelectionListener(new ICheckStateListener() {
        @Override
        public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
          pingStateChanging();
        }
      });
      m_treeListenersAttached = true;
    }
    // layout
    parent.setLayout(new GridLayout(1, true));

    GridData interfaceLocationData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL);
    interfaceLocationData.heightHint = 200;
    m_tree.setLayoutData(interfaceLocationData);
  }

  /**
   * @return the rootNode
   */
  public ITreeNode getRootNode() {
    return m_rootNode;
  }

  private CheckableTree getTree() {
    return m_tree;
  }

  public void refreshTree() {
    if (isControlCreated()) {
      m_tree.getTreeViewer().refresh();
    }
  }

  public IScoutBundle[] getLocationBundles(String type, boolean visibleOnly, boolean checkedOnly) {
    ITreeNode[] nodes = getTreeNodes(type, visibleOnly, checkedOnly);
    ArrayList<IScoutBundle> result = new ArrayList<IScoutBundle>();
    for (ITreeNode node : nodes) {
      if (node != null && node.getParent() != null && node.getParent().getData() instanceof IScoutBundle) {
        result.add((IScoutBundle) node.getParent().getData());
      }
    }
    return result.toArray(new IScoutBundle[result.size()]);
  }

  public ITreeNode[] getTreeNodes(String type, boolean visibleOnly, boolean checkedOnly) {
    ArrayList<ITreeNode> result = new ArrayList<ITreeNode>();
    if (isControlCreated()) {
      ITreeNode[] nodes = TreeUtility.findNodes(m_rootNode, NodeFilters.getByType(type));
      for (ITreeNode node : nodes) {
        if (node != null && checkedOnly && node.isCheckable()) {
          if (!getTree().isChecked(node)) {
            continue;
          }
        }
        if (node != null && visibleOnly) {
          if (!node.isVisible()) {
            continue;
          }
        }
        result.add(node);
      }
    }
    return result.toArray(new ITreeNode[result.size()]);
  }

  public static IScoutBundle getLocationBundle(ITreeNode node) {
    ITreeNode searchNode = node;
    IScoutBundle bundle = null;
    while (bundle == null && searchNode != null) {
      Object o = searchNode.getData();
      if (o instanceof IScoutBundle) {
        return (IScoutBundle) o;
      }
      searchNode = searchNode.getParent();
    }
    return null;
  }

  public IScoutBundle getLocationBundle(String type, boolean visibleOnly, boolean checkedOnly) {
    ITreeNode node = getTreeNode(type, visibleOnly, checkedOnly);
    return getLocationBundle(node);
  }

  public ITreeNode getTreeNode(String type, boolean visibleOnly, boolean checkedOnly) {
    ITreeNode node = null;
    if (isControlCreated()) {
      node = TreeUtility.findNode(m_rootNode, NodeFilters.getByType(type));
      if (node != null) {
        if (node.isCheckable() && checkedOnly && !getTree().isChecked(node)) {
          return null;
        }
        if (visibleOnly && !node.isVisible()) {
          return null;
        }
      }
    }
    return node;
  }

  /**
   * @param nodeType
   * @return
   */
  public String getTextOfNode(String nodeType) {
    return getTextOfNode(nodeType, true, true);
  }

  public String getTextOfNode(String type, boolean visibleOnly, boolean checkedOnly) {
    ITreeNode treeNode = getTreeNode(type, visibleOnly, checkedOnly);
    String text = null;
    if (treeNode != null) {
      text = treeNode.getText();
    }
    return text;
  }

  public boolean isNodeChecked(ITreeNode node) {
    return m_tree.isChecked(node);
  }

  public void setNodeChecked(ITreeNode node, boolean checked) {
    m_tree.setChecked(node, checked);
  }
}
