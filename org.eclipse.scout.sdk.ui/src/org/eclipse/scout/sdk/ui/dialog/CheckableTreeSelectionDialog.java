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
package org.eclipse.scout.sdk.ui.dialog;

import java.beans.PropertyChangeListener;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link CheckableTreeSelectionDialog}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 09.09.2010
 */
public class CheckableTreeSelectionDialog extends TitleAreaDialog {

  public static final String PROP_CHECKED_NODES = "checkedNodes"; /*Set<ITreeNode> */
  public static final String PROP_SELECTED_NODE = "selectedNodes"; /*ITreeNode */

  private final BasicPropertySupport m_propertySupport;
  private final OptimisticLock m_uiLock;
  private final ITreeNode m_rootNode;
  private final String m_message;
  private final String m_title;

  private CheckableTree m_tree;
  private boolean m_complete;

  public CheckableTreeSelectionDialog(Shell parentShell, ITreeNode rootNode) {
    this(parentShell, rootNode, null, null);
  }

  public CheckableTreeSelectionDialog(Shell parentShell, ITreeNode rootNode, String dialogTitle, String message) {
    super(parentShell);

    m_propertySupport = new BasicPropertySupport(this);
    m_uiLock = new OptimisticLock();

    m_rootNode = rootNode;
    m_title = dialogTitle;
    m_message = message;

    setShellStyle(getShellStyle() | SWT.RESIZE);
    setHelpAvailable(false);
  }

  @Override
  protected Control createContents(Composite parent) {
    Control c = super.createContents(parent);
    setMessage(m_message);
    setTitle(m_title);
    m_tree.setChecked(getCheckedNodes());
    return c;
  }

  @Override
  protected Control createButtonBar(Composite parent) {
    Control buttonbar = super.createButtonBar(parent);
    getButton(IDialogConstants.OK_ID).setEnabled(isComplete());
    return buttonbar;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite rootPane = new Composite(parent, SWT.NONE);

    m_tree = new CheckableTree(rootPane, getRootNode());
    m_tree.addCheckSelectionListener(new ICheckStateListener() {
      @Override
      public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
        try {
          if (m_uiLock.acquire()) {
            setCheckedNodes(m_tree.getCheckedNodes());
          }
        }
        finally {
          m_uiLock.release();
        }

      }
    });
    m_tree.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        try {
          if (m_uiLock.acquire()) {
            ITreeNode selectedNode = null;
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            if (!selection.isEmpty()) {
              selectedNode = (ITreeNode) selection.getFirstElement();
            }
            if (selectedNode != null) {
              setSelectedNode(selectedNode);
            }
          }
        }
        finally {
          m_uiLock.release();
        }
      }
    });

    if (parent.getLayout() instanceof GridLayout) {
      rootPane.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
    }
    rootPane.setLayout(new GridLayout(1, true));
    m_tree.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL));

    return rootPane;
  }

  public void setComplete(boolean complete) {
    m_complete = complete;
    if (getShell() != null && !getShell().isDisposed()) {
      getButton(IDialogConstants.OK_ID).setEnabled(complete);
    }
  }

  /**
   * @return the complete
   */
  public boolean isComplete() {
    return m_complete;
  }

  /**
   * @return the rootNode
   */
  public ITreeNode getRootNode() {
    return m_rootNode;
  }

  @SuppressWarnings("unchecked")
  public Set<ITreeNode> getCheckedNodes() {
    Set<ITreeNode> checkedNodes = (Set<ITreeNode>) m_propertySupport.getProperty(PROP_CHECKED_NODES);
    if (checkedNodes == null) {
      return CollectionUtility.emptyHashSet();
    }
    return CollectionUtility.hashSet(checkedNodes);
  }

  public void setCheckedNodes(Set<ITreeNode> checkedNodes) {
    Set<ITreeNode> newCheckedNodes = CollectionUtility.hashSet(checkedNodes);
    m_propertySupport.setProperty(PROP_CHECKED_NODES, newCheckedNodes);
    if (m_tree != null && !m_tree.isDisposed()) {
      try {
        m_uiLock.acquire();
        m_tree.setChecked(newCheckedNodes);
      }
      finally {
        m_uiLock.release();
      }
    }
  }

  public ITreeNode getSelectedNode() {
    return (ITreeNode) m_propertySupport.getProperty(PROP_SELECTED_NODE);
  }

  public void setSelectedNode(ITreeNode node) {
    m_propertySupport.setProperty(PROP_SELECTED_NODE, node);
    if (m_tree != null && !m_tree.isDisposed()) {
      try {
        m_uiLock.acquire();
        m_tree.getTreeViewer().setSelection(new StructuredSelection(node), true);
      }
      finally {
        m_uiLock.release();
      }
    }
  }

  /**
   * @param listener
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  /**
   * @param listener
   * @see org.eclipse.scout.commons.beans.BasicPropertySupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

}
