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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.project.add.ScoutProjectAddOperation;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.project.AbstractProjectNewWizardPage;
import org.eclipse.scout.sdk.util.PropertyMap;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link ProductFileSelectionWizardPage}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.9.0 29.05.2013
 */
public class ProductFileSelectionWizardPage extends AbstractProjectNewWizardPage {

  private final static String PROP_SELECTED_PRODUCTS = "selectedProductFilesToModify";
  private final IScoutBundle m_scoutBundle;
  private CheckableTree m_bundleTree;

  public ProductFileSelectionWizardPage(IScoutBundle scoutBundle) {
    super(ProductFileSelectionWizardPage.class.getName());
    setTitle(Texts.get("SelectProductsToModify"));
    setDescription(Texts.get("ProductFileSelectionWizardMsg"));
    m_scoutBundle = scoutBundle;
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
    setExcludePage(true);

    try {
      ITreeNode root = TreeUtility.createProductTree(m_scoutBundle, null, true);

      ITreeNode[] productFileNodes = TreeUtility.findNodes(root, NodeFilters.getByType(TreeUtility.TYPE_PRODUCT_NODE));

      m_bundleTree = new CheckableTree(parent, root);
      m_bundleTree.addCheckSelectionListener(new ICheckStateListener() {
        @Override
        public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
          setProperty(PROP_SELECTED_PRODUCTS, m_bundleTree.getCheckedNodes());
          pingStateChanging();
        }
      });
      m_bundleTree.setChecked(productFileNodes);

      // layout
      parent.setLayout(new GridLayout(1, true));
      m_bundleTree.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));

      setExcludePage(productFileNodes.length < 1);
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("Unable to create product file tree", e);
    }
  }
}
