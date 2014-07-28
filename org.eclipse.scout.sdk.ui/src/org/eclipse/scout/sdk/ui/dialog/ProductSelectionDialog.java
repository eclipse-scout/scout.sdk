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

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link ProductSelectionDialog}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 09.09.2010
 */
public class ProductSelectionDialog extends TitleAreaDialog {
  private IScoutBundle m_project;
  private ITreeNode m_rootNode;
  private CheckableTree m_tree;
  private IFile[] m_checkedFiles;
  private boolean m_multiSelectionMode = true;
  private boolean m_productSelectionRequired;
  private IFile m_selectedProductFile;
  private ITreeNodeFilter m_visibleNodeFilter;

  /**
   * @param parentShell
   */
  public ProductSelectionDialog(Shell parentShell, IScoutBundle project) {
    this(parentShell, project, NodeFilters.getAcceptAll());
  }

  public ProductSelectionDialog(Shell parentShell, IScoutBundle project, ITreeNodeFilter visibleFilter) {
    super(parentShell);
    m_project = project;
    m_visibleNodeFilter = visibleFilter;
    m_checkedFiles = new IFile[0];
    setShellStyle(getShellStyle() | SWT.RESIZE);
    setHelpAvailable(false);
  }

  public ProductSelectionDialog(Shell parentShell, ITreeNode rootNode) {
    super(parentShell);
    m_rootNode = rootNode;
    m_checkedFiles = new IFile[0];
    setShellStyle(getShellStyle() | SWT.RESIZE);
    setHelpAvailable(false);
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Texts.get("ProductSelectionDialogTitle"));
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    setTitle(Texts.get("ProductSelectionDialogTitle"));
    setMessage(Texts.get("ChooseProducts"));

    Composite rootPane = new Composite(parent, SWT.NONE);

    ITreeNode root = null;
    try {
      root = getRootNode();
    }
    catch (CoreException e1) {
      ScoutSdkUi.logError("unable to create product file list", e1);
    }
    m_tree = new CheckableTree(rootPane, root);
    m_tree.addCheckSelectionListener(new ICheckStateListener() {
      @Override
      public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
        ArrayList<IFile> checkedFiles = new ArrayList<IFile>();
        for (ITreeNode n : m_tree.getCheckedNodes()) {
          if (n.getType() == TreeUtility.TYPE_PRODUCT_NODE) {
            checkedFiles.add((IFile) n.getData());
          }
        }
        m_checkedFiles = checkedFiles.toArray(new IFile[checkedFiles.size()]);
      }
    });
    m_tree.getTreeViewer().addSelectionChangedListener(new P_TreeSelectionListener());
    m_tree.setChecked(TreeUtility.findNodes(m_rootNode, NodeFilters.getByData((Object[]) m_checkedFiles)));
    if (parent.getLayout() instanceof GridLayout) {
      rootPane.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
    }
    rootPane.setLayout(new GridLayout(1, true));
    m_tree.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL));
    return rootPane;
  }

  /**
   * @return the rootNode
   * @throws CoreException
   */
  public ITreeNode getRootNode() throws CoreException {
    if (m_rootNode == null) {
      m_rootNode = TreeUtility.createProductTree(m_project, getVisibleNodeFilter(), isMultiSelectionMode());
    }
    return m_rootNode;
  }

  public IFile[] getCheckedProductFiles() {
    if (isMultiSelectionMode()) {
      return m_checkedFiles;
    }
    else {
      return new IFile[]{m_selectedProductFile};
    }
  }

  /**
   * @return the selectedProductFile
   */
  public IFile getSelectedProductFile() {
    return m_selectedProductFile;
  }

  /**
   * @param array
   */
  public void setCheckedProductFiles(IFile[] array) {
    m_checkedFiles = array;
    if (m_tree != null && !m_tree.isDisposed()) {
      ITreeNode[] treeNodes = TreeUtility.findNodes(m_rootNode, NodeFilters.getByData((Object[]) array));
      if (isMultiSelectionMode()) {
        m_tree.setChecked(treeNodes);
      }
      else {
        if (treeNodes.length > 0) {
          ArrayList<Object> pathElements = new ArrayList<Object>();
          ITreeNode n = treeNodes[0];
          while (n != null) {
            pathElements.add(pathElements.size(), n);
            n = n.getParent();
          }
          m_tree.getTreeViewer().setSelection(new TreeSelection(new TreePath(pathElements.toArray())));
        }
      }
    }
  }

  /**
   * @return the multiSelectionMode
   */
  public boolean isMultiSelectionMode() {
    return m_multiSelectionMode;
  }

  /**
   * @param multiSelectionMode
   *          the multiSelectionMode to set
   */
  public void setMultiSelectionMode(boolean multiSelectionMode) {
    m_multiSelectionMode = multiSelectionMode;
  }

  /**
   * @return the productSelectionRequired
   */
  public boolean isProductSelectionRequired() {
    return m_productSelectionRequired;
  }

  /**
   * @param productSelectionRequired
   *          the productSelectionRequired to set
   */
  public void setProductSelectionRequired(boolean productSelectionRequired) {
    m_productSelectionRequired = productSelectionRequired;
  }

  /**
   * @return the visibleNodeFilter
   */
  public ITreeNodeFilter getVisibleNodeFilter() {
    return m_visibleNodeFilter;
  }

  private class P_TreeSelectionListener implements ISelectionChangedListener {
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
      m_selectedProductFile = null;
      boolean pageComplete = !isProductSelectionRequired();
      if (!pageComplete) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (!selection.isEmpty()) {
          Object selectedElement = selection.getFirstElement();
          if (selectedElement instanceof TreeNode) {
            ITreeNode treeNode = (ITreeNode) selectedElement;
            if (treeNode.getType() == TreeUtility.TYPE_PRODUCT_NODE) {
              m_selectedProductFile = (IFile) treeNode.getData();
              pageComplete = true;
            }
          }
        }
      }
      getButton(IDialogConstants.OK_ID).setEnabled(pageComplete);

    }
  }

}
