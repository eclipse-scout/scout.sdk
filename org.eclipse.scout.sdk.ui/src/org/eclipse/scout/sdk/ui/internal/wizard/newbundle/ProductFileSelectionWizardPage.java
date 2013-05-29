/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.wizard.newbundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.add.ScoutProjectAddOperation;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.wizard.project.AbstractProjectNewWizardPage;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * <h3>{@link ProductFileSelectionWizardPage}</h3>
 * 
 * @author mvi
 * @since 3.9.0 29.05.2013
 */
public class ProductFileSelectionWizardPage extends AbstractProjectNewWizardPage {

  private final static String PROP_SELECTED_PRODUCTS = "selectedProductFilesToModify";

  private final IScoutBundle m_bundle;
  private final Set<IFile> m_productFiles;
  private final List<ITreeNode> m_productNodes;

  private CheckableTree m_bundleTree;
  private ITreeNode m_invisibleRootNode;

  public ProductFileSelectionWizardPage(IScoutBundle project, Set<IFile> productFiles) {
    super(ProductFileSelectionWizardPage.class.getName());
    setTitle(Texts.get("SelectProductsToModify"));
    setDescription(Texts.get("ProductFileSelectionWizardMsg"));
    m_bundle = project;
    m_productFiles = productFiles;
    m_productNodes = new ArrayList<ITreeNode>();
  }

  @Override
  public void putProperties(PropertyMap properties) {
    ITreeNode[] checkedNodes = (ITreeNode[]) getProperty(PROP_SELECTED_PRODUCTS);
    if (checkedNodes != null && checkedNodes.length > 0) {
      IFile[] prodFiles = new IFile[checkedNodes.length];
      for (int i = 0; i < checkedNodes.length; i++) {
        prodFiles[i] = (IFile) checkedNodes[i].getData();
      }
      properties.setProperty(ScoutProjectAddOperation.PROP_PRODUCT_FILES_TO_EXTEND, prodFiles);
    }
  }

  @Override
  protected void createContent(Composite parent) {
    m_invisibleRootNode = buildProductTree();

    m_bundleTree = new CheckableTree(parent, m_invisibleRootNode);
    m_bundleTree.addCheckSelectionListener(new ICheckStateListener() {
      @Override
      public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
        setProperty(PROP_SELECTED_PRODUCTS, m_bundleTree.getCheckedNodes());
        pingStateChanging();
      }
    });
    m_bundleTree.setChecked(m_productNodes.toArray(new ITreeNode[m_productNodes.size()]));

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_bundleTree.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
  }

  private ITreeNode buildProductTree() {
    m_productNodes.clear();
    ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
    rootNode.setCheckable(false);
    rootNode.setVisible(false);
    for (IFile prodFile : m_productFiles) {
      IScoutBundle b = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(prodFile.getProject());
      if (b != null) {
        ITreeNode bundleNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(b));
        if (bundleNode == null) {
          bundleNode = TreeUtility.createBundleTreeNode(rootNode, b);
          bundleNode.setCheckable(false);
        }

        IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter) prodFile.getAdapter(IWorkbenchAdapter.class);
        ImageDescriptor imageDescriptor = null;
        if (wbAdapter != null) {
          imageDescriptor = wbAdapter.getImageDescriptor(prodFile);
        }
        ITreeNode childNode = TreeUtility.createNode(bundleNode, TreeUtility.TYPE_PRODUCT_NODE, prodFile.getName(), imageDescriptor, 0, prodFile, false);
        childNode.setCheckable(true);
        bundleNode.addChild(childNode);
        m_productNodes.add(childNode);
      }
    }

    return rootNode;
  }
}
