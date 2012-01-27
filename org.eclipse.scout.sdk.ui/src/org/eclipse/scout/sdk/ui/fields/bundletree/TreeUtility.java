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
package org.eclipse.scout.sdk.ui.fields.bundletree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree.P_DependentFilter;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * <h3>TreeUtility</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 04.02.2010
 */
public class TreeUtility {
  private static TreeUtility instance = new TreeUtility();
  public static final int TYPE_PRODUCT_NODE = 2999;

  private TreeUtility() {
  }

  public static ITreeNode[] findNodes(ITreeNode startNode, ITreeNodeFilter filter) {
    return instance.findNodesImpl(startNode, filter);
  }

  private ITreeNode[] findNodesImpl(ITreeNode startNode, ITreeNodeFilter filter) {
    ArrayList<ITreeNode> collector = new ArrayList<ITreeNode>(3);
    collectNodes(startNode, filter, collector);
    return collector.toArray(new ITreeNode[collector.size()]);
  }

  public static ITreeNode findNode(ITreeNode startNode, ITreeNodeFilter filter) {
    return instance.findNodeImpl(startNode, filter);
  }

  private ITreeNode findNodeImpl(ITreeNode startNode, ITreeNodeFilter filter) {
    ArrayList<ITreeNode> collector = new ArrayList<ITreeNode>(3);
    collectNodes(startNode, filter, collector);
    if (collector.size() > 0) {
      if (collector.size() > 1) {
        ScoutSdkUi.logWarning("more than 1 node found.");
      }
      return collector.get(0);
    }
    return null;
  }

  private void collectNodes(ITreeNode node, ITreeNodeFilter filter, List<ITreeNode> collector) {
    if (filter.accept(node)) {
      collector.add(node);
    }
    for (ITreeNode childNode : node.getChildren()) {
      collectNodes(childNode, filter, collector);
    }
  }

  public static ITreeNode createBundleTree(IScoutProject scoutProject) {
    return createBundleTree(scoutProject, NodeFilters.getAcceptAll());
  }

  public static ITreeNode createBundleTree(IScoutProject scoutProject, ITreeNodeFilter filter) {
    ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
    rootNode.setVisible(false);
    while (scoutProject.getParentProject() != null) {
      scoutProject = scoutProject.getParentProject();
    }
    if (scoutProject.getClientBundle() != null) {
      recAddChildNodes(rootNode, new IScoutBundle[]{scoutProject.getClientBundle()}, IScoutBundle.BUNDLE_CLIENT, ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ClientBundle), 1000, filter);
    }
    if (scoutProject.getSharedBundle() != null) {
      recAddChildNodes(rootNode, new IScoutBundle[]{scoutProject.getSharedBundle()}, IScoutBundle.BUNDLE_SHARED, ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SharedBundle), 2000, filter);
    }
    if (scoutProject.getServerBundle() != null) {
      recAddChildNodes(rootNode, new IScoutBundle[]{scoutProject.getServerBundle()}, IScoutBundle.BUNDLE_SERVER, ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServerBundle), 3000, filter);
    }
    return rootNode;
  }

  private static void recAddChildNodes(ITreeNode node, IScoutBundle[] bundles, int type, ImageDescriptor img, long orderNr, ITreeNodeFilter filter) {
    for (IScoutBundle b : bundles) {
      TreeNode childNode = new TreeNode(b.getType(), b.getBundleName(), b);
      childNode.setOrderNr(orderNr);
      childNode.setBold(true);
      childNode.setImage(img);
      if (filter.accept(childNode)) {
        node.addChild(childNode);
        childNode.setParent(node);
        recAddChildNodes(childNode, b.getDependentBundles(new P_DependentFilter(type), false), type, img, orderNr, filter);
      }
    }
  }

  public static ITreeNode createNode(ITreeNode parentNode, int type, String name, ImageDescriptor img) {
    return createNode(parentNode, type, name, img, 0);
  }

  /**
   * @return the rootNode
   */
  public static ITreeNode createNode(ITreeNode parentNode, int type, String name, ImageDescriptor img, long orderNr) {
    return createNode(parentNode, type, name, img, orderNr, null);

  }

  public static ITreeNode createNode(ITreeNode parentNode, int type, String name, ImageDescriptor img, long orderNr, Object data) {
    TreeNode node = new TreeNode(type, name);
    node.setData(data);
    node.setOrderNr(orderNr);
    node.setImage(img);
    node.setCheckable(true);
    node.setParent(parentNode);
    parentNode.addChild(node);
    return node;

  }

  public static ITreeNode createProductTree(IScoutProject project, ITreeNodeFilter visibleFilter, boolean checkMode) {
    return instance.createProductTreeImpl(project, visibleFilter, checkMode);
  }

  private ITreeNode createProductTreeImpl(IScoutProject project, ITreeNodeFilter visibleFilter, boolean checkMode) {
    if (project == null) {
      return null;
    }
    else {
      if (visibleFilter == null) {
        visibleFilter = NodeFilters.getAcceptAll();
      }
      ArrayList<P_ProductFile> productFiles = new ArrayList<P_ProductFile>();
      visitScoutProject(productFiles, project);
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
              ((TreeNode) bundleNode).setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SwingBundle));
              ((TreeNode) bundleNode).setOrderNr(1000);
              break;
            case IScoutBundle.BUNDLE_UI_SWT:
              ((TreeNode) bundleNode).setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SwtBundle));
              ((TreeNode) bundleNode).setOrderNr(2000);
              break;
            case IScoutBundle.BUNDLE_SERVER:
              ((TreeNode) bundleNode).setImage(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServerBundle));
              ((TreeNode) bundleNode).setOrderNr(3000);
              break;
          }
          bundleNode.setCheckable(false);
          if (visibleFilter.accept(bundleNode)) {
            rootNode.addChild(bundleNode);
          }
          else {
            bundleNode = null;
          }
        }
        if (bundleNode != null) {
          TreeNode productNode = new TreeNode(TYPE_PRODUCT_NODE, file.getName() + " (" + file.getParent().getName() + ")", file);
          productNode.setCheckable(checkMode);
          productNode.setBold(true);
          //Image img = ScoutSdkUi.getImage(ScoutSdkUi.File);
          IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter) file.getAdapter(IWorkbenchAdapter.class);
          if (wbAdapter != null) {
            ImageDescriptor imageDescriptor = wbAdapter.getImageDescriptor(file);
            (productNode).setImage(imageDescriptor);
          }

          if (visibleFilter.accept(productNode)) {
            bundleNode.addChild(productNode);
          }
        }
      }
      return rootNode;
    }
  }

  public static IFile[] getAllProductFiles(IScoutProject project) {
    ArrayList<P_ProductFile> productFiles = new ArrayList<P_ProductFile>();
    instance.visitScoutProject(productFiles, project);
    IFile[] ret = new IFile[productFiles.size()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = productFiles.get(i).getProductFile();
    }
    return ret;
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
