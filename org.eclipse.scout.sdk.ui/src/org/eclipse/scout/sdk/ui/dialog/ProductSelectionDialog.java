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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * <h3>{@link ProductSelectionDialog}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.09.2010
 */
public class ProductSelectionDialog extends TitleAreaDialog {
  private int TYPE_PRODUCT_NODE = 1;
  private final IScoutProject m_project;
  private ITreeNode m_rootNode;
  private CheckableTree m_tree;
  private ArrayList<Resource> m_allocatedResources = new ArrayList<Resource>();
  private IFile[] m_checkedFiles;

  /**
   * @param parentShell
   */
  public ProductSelectionDialog(Shell parentShell, IScoutProject project) {
    super(parentShell);
    m_project = project;
    setTitle("Select Product");
    m_checkedFiles = new IFile[0];
    setShellStyle(getShellStyle() | SWT.RESIZE);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite rootPane = new Composite(parent, SWT.NONE);
    parent.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        for (Resource r : m_allocatedResources) {
          r.dispose();
        }
      }
    });
    m_rootNode = createProductTree();
    m_tree = new CheckableTree(rootPane, m_rootNode);
    m_tree.addCheckSelectionListener(new ICheckStateListener() {
      @Override
      public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
        ArrayList<IFile> checkedFiles = new ArrayList<IFile>();
        for (ITreeNode n : m_tree.getCheckedNodes()) {
          if (n.getType() == TYPE_PRODUCT_NODE) {
            checkedFiles.add((IFile) n.getData());
          }
        }
        m_checkedFiles = checkedFiles.toArray(new IFile[checkedFiles.size()]);
      }
    });
    m_tree.setChecked(TreeUtility.findNodes(m_rootNode, NodeFilters.getByData(m_checkedFiles)));
    if (parent.getLayout() instanceof GridLayout) {
      rootPane.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
    }
    rootPane.setLayout(new GridLayout(1, true));
    m_tree.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL));
    return rootPane;
  }

  private ITreeNode createProductTree() {
    ArrayList<P_ProductFile> productFiles = new ArrayList<P_ProductFile>();
    visitScoutProject(productFiles, m_project);
    ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
    rootNode.setVisible(false);
    for (P_ProductFile productFile : productFiles) {
      IScoutBundle bundle = productFile.getScoutBundle();
      IFile file = productFile.getProductFile();
      ITreeNode bundleNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(bundle));
      if (bundleNode == null) {
        bundleNode = new TreeNode(bundle.getType(), bundle.getBundleName(), bundle);
        switch (bundle.getType()) {
          case IScoutBundle.BUNDLE_UI_SWING:
            ((TreeNode) bundleNode).setImage(ScoutSdkUi.getImage(ScoutSdkUi.IMG_UI_BUNDLE));
            ((TreeNode) bundleNode).setOrderNr(1000);
            break;
          case IScoutBundle.BUNDLE_UI_SWT:
            ((TreeNode) bundleNode).setImage(ScoutSdkUi.getImage(ScoutSdkUi.IMG_UI_BUNDLE));
            ((TreeNode) bundleNode).setOrderNr(2000);
            break;
          case IScoutBundle.BUNDLE_SERVER:
            ((TreeNode) bundleNode).setImage(ScoutSdkUi.getImage(ScoutSdkUi.IMG_SERVER));
            ((TreeNode) bundleNode).setOrderNr(3000);
            break;
        }
        bundleNode.setCheckable(false);
        rootNode.addChild(bundleNode);
      }
      TreeNode productNode = new TreeNode(TYPE_PRODUCT_NODE, file.getName() + " (" + file.getParent().getName() + ")", file);
      productNode.setCheckable(true);
      productNode.setBold(true);
      Image img = ScoutSdkUi.getImage(ScoutSdkUi.IMG_FILE);
      IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter) file.getAdapter(IWorkbenchAdapter.class);
      if (wbAdapter != null) {
        ImageDescriptor imageDescriptor = wbAdapter.getImageDescriptor(file);
        if (imageDescriptor != null) {
          img = imageDescriptor.createImage();
          m_allocatedResources.add(img);
        }
      }
      ((TreeNode) productNode).setImage(img);
      bundleNode.addChild(productNode);
    }
    return rootNode;
  }

  private void visitScoutProject(List<P_ProductFile> productFileCollector, IScoutProject project) {
    for (IScoutBundle b : project.getAllScoutBundles()) {
      try {
        b.getProject().accept(new P_ProductResourceVisitor(b, productFileCollector));
      }
      catch (CoreException e) {
        ScoutSdkUi.logWarning("error during searching *.product in '" + b.getProject().getName() + "'.", e);
      }
    }
    for (IScoutProject childProject : project.getSubProjects()) {
      visitScoutProject(productFileCollector, childProject);
    }
  }

  public IFile[] getSelectedProducts() {
    return m_checkedFiles;
  }

  /**
   * @param array
   */
  public void setSelectedProducts(IFile[] array) {
    m_checkedFiles = array;
    if (m_tree != null && !m_tree.isDisposed()) {
      m_tree.setChecked(TreeUtility.findNodes(m_rootNode, NodeFilters.getByData(array)));
    }

  }

  private class P_ProductResourceVisitor implements IResourceVisitor {
    private final List<P_ProductFile> m_productFileCollector;
    private final IScoutBundle m_bundle;

    private P_ProductResourceVisitor(IScoutBundle bundle, List<P_ProductFile> productFileCollector) {
      m_bundle = bundle;
      m_productFileCollector = productFileCollector;

    }

    @Override
    public boolean visit(IResource resource) throws CoreException {
      if (resource.getType() == IResource.FILE && resource.getName().matches(".*\\.product")) {
        m_productFileCollector.add(new P_ProductFile(m_bundle, (IFile) resource));
      }
      else if (resource.getType() == IResource.FOLDER) {
        return true;
      }
      return true;
    }
  }

  private class P_ProductFile {
    private IScoutBundle m_scoutBundle;
    private IFile m_productFile;

    public P_ProductFile(IScoutBundle bundle, IFile file) {
      m_scoutBundle = bundle;
      m_productFile = file;
    }

    /**
     * @return the scoutBundle
     */
    public IScoutBundle getScoutBundle() {
      return m_scoutBundle;
    }

    /**
     * @return the productFile
     */
    public IFile getProductFile() {
      return m_productFile;
    }
  }

}
