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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.SdkIcons;
import org.eclipse.scout.sdk.ui.internal.extensions.bundle.ScoutBundleExtensionPoint;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceFilters;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * <h3>TreeUtility</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 04.02.2010
 */
public final class TreeUtility {

  public static final String TYPE_PRODUCT_NODE = "zz_product_node";
  public static final String TYPE_PROJECT_NODE = "zz_project_node";

  private TreeUtility() {
  }

  public static ITreeNode[] findNodes(ITreeNode startNode, ITreeNodeFilter filter) {
    ArrayList<ITreeNode> collector = new ArrayList<ITreeNode>(3);
    collectNodes(startNode, filter, collector);
    return collector.toArray(new ITreeNode[collector.size()]);
  }

  public static <T> boolean isOneOf(T toSearch, T... listToSearchIn) {
    if (listToSearchIn != null && listToSearchIn.length > 0) {
      for (T t : listToSearchIn) {
        if (CompareUtility.equals(t, toSearch)) {
          return true;
        }
      }
    }
    return false;
  }

  public static ITreeNode findNode(ITreeNode startNode, ITreeNodeFilter filter) {
    ArrayList<ITreeNode> collector = new ArrayList<ITreeNode>(3);
    collectNodes(startNode, filter, collector);
    if (collector.size() > 0) {
      return collector.get(0);
    }
    return null;
  }

  private static void collectNodes(ITreeNode node, ITreeNodeFilter filter, List<ITreeNode> collector) {
    if (node != null) {
      if (filter.accept(node)) {
        collector.add(node);
      }
      for (ITreeNode childNode : node.getChildren()) {
        collectNodes(childNode, filter, collector);
      }
    }
  }

  public static ITreeNode createBundleTree(IScoutBundle scoutProject, ITreeNodeFilter treeNodeFilter, IScoutBundleFilter scoutBundleFilter) {
    ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
    rootNode.setVisible(false);

    HashSet<IScoutBundle> rootBundles = new HashSet<IScoutBundle>();
    IScoutBundleFilter rootFilter = null;
    if (scoutBundleFilter == null) {
      rootFilter = ScoutBundleFilters.getRootBundlesFilter();
    }
    else {
      rootFilter = ScoutBundleFilters.getFilteredRootBundlesFilter(scoutBundleFilter);
    }
    for (IScoutBundle root : scoutProject.getParentBundles(rootFilter, true)) {
      rootBundles.add(root);
    }
    recAddChildNodes(rootNode, rootBundles, treeNodeFilter);
    return rootNode;
  }

  private static void recAddChildNodes(ITreeNode node, Set<? extends IScoutBundle> bundles, ITreeNodeFilter filter) {
    for (IScoutBundle b : bundles) {
      ITreeNode childNode = createBundleTreeNode(node, b, filter);
      if (childNode != null) {
        recAddChildNodes(childNode, b.getDirectChildBundles(), filter);
      }
    }
  }

  public static ITreeNode createBundleTreeNode(ITreeNode parent, IScoutBundle b) {
    return createBundleTreeNode(parent, b, null);
  }

  public static ITreeNode createBundleTreeNode(ITreeNode parent, IScoutBundle b, ITreeNodeFilter filter) {
    ScoutBundleUiExtension uiExt = ScoutBundleExtensionPoint.getExtension(b.getType());
    TreeNode childNode = null;
    if (uiExt != null) {
      childNode = new TreeNode(b.getType(), b.getSymbolicName(), b);
      childNode.setOrderNr(Integer.MAX_VALUE - Math.abs(uiExt.getOrderNumber())); // ensure the bundle nodes are at the end of all other nodes on the same level
      if (filter != null && !filter.accept(childNode)) {
        return null; // the created node does not match the filter
      }

      childNode.setBold(false);
      ImageDescriptor icon = uiExt.getIcon();
      if (b.isBinary()) {
        icon = ScoutSdkUi.getImageDescriptor(icon, SdkIcons.BinaryDecorator, IDecoration.BOTTOM_LEFT);
        childNode.setForeground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
      }
      childNode.setImage(icon);
      parent.addChild(childNode);
      childNode.setParent(parent);
    }
    return childNode;
  }

  public static ITreeNode createNode(ITreeNode parentNode, String type, String name, ImageDescriptor img) {
    return createNode(parentNode, type, name, img, 0);
  }

  /**
   * @return the rootNode
   */
  public static ITreeNode createNode(ITreeNode parentNode, String type, String name, ImageDescriptor img, int orderNr) {
    return createNode(parentNode, type, name, img, orderNr, null);
  }

  public static ITreeNode createNode(ITreeNode parentNode, String type, String name, ImageDescriptor img, int orderNr, Object data) {
    return createNode(parentNode, type, name, img, orderNr, data, true);
  }

  public static ITreeNode createNode(ITreeNode parentNode, String type, String name, ImageDescriptor img, int orderNr, Object data, boolean bold) {
    return createNode(parentNode, type, name, img, orderNr, data, true, true);
  }

  public static ITreeNode createNode(ITreeNode parentNode, String type, String name, ImageDescriptor img, int orderNr, Object data, boolean bold, boolean checkable) {
    TreeNode node = new TreeNode(type, name);
    node.setData(data);
    node.setOrderNr(orderNr);
    node.setImage(img);
    node.setBold(bold);
    node.setCheckable(checkable);
    node.setParent(parentNode);
    parentNode.addChild(node);
    return node;
  }

  private static boolean isProductFileContainingBundles(IFile productFile, IScoutBundle[] filter) {
    try {
      ProductFileModelHelper pfmh = new ProductFileModelHelper(productFile);
      for (IScoutBundle b : filter) {
        if (pfmh.ProductFile.existsDependency(b.getSymbolicName())) {
          return true;
        }
      }
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("Unable to parse product '" + productFile.getFullPath().toOSString() + "'.", e);
    }
    return false;
  }

  public static ITreeNode createProductFileTreeNode(ITreeNode parent, IFile file, ITreeNodeFilter filter, boolean checkable) {
    TreeNode productNode = new TreeNode(TYPE_PRODUCT_NODE, file.getName() + " (" + file.getParent().getName() + ")", file);
    if (filter != null && !filter.accept(productNode)) {
      return null; // the created node does not match the filter
    }

    productNode.setCheckable(checkable);
    productNode.setBold(true);

    IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter) file.getAdapter(IWorkbenchAdapter.class);
    if (wbAdapter != null) {
      ImageDescriptor imageDescriptor = wbAdapter.getImageDescriptor(file);
      productNode.setImage(imageDescriptor);
    }

    parent.addChild(productNode);
    productNode.setParent(parent);

    return productNode;
  }

  public static ITreeNode createProjectTreeNode(ITreeNode parent, IProject project, ITreeNodeFilter filter) {
    TreeNode childNode = new TreeNode(TYPE_PROJECT_NODE, project.getName(), project);
    if (filter != null && !filter.accept(childNode)) {
      return null; // the created node does not match the filter
    }

    childNode.setBold(false);

    IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter) project.getAdapter(IWorkbenchAdapter.class);
    if (wbAdapter != null) {
      ImageDescriptor imageDescriptor = wbAdapter.getImageDescriptor(project);
      childNode.setImage(imageDescriptor);
    }
    parent.addChild(childNode);
    childNode.setParent(parent);

    return childNode;
  }

  public static ITreeNode createProductTree(IScoutBundle project, ITreeNodeFilter visibleFilter, boolean checkMode) throws CoreException {
    if (project == null) {
      return null;
    }
    else {
      if (visibleFilter == null) {
        visibleFilter = NodeFilters.getAcceptAll();
      }

      IScoutBundle[] containingScoutBundles = project.getChildBundles(ScoutBundleFilters.getAllBundlesFilter(), true);

      IResource[] productFiles = ResourceUtility.getAllResources(ResourceFilters.getProductFileFilter());
      ITreeNode rootNode = new TreeNode(CheckableTree.TYPE_ROOT, "root");
      rootNode.setVisible(false);

      for (IResource res : productFiles) {
        IFile file = (IFile) res;
        if (isProductFileContainingBundles(file, containingScoutBundles)) {
          // create bundle node
          IProject fileProject = file.getProject();
          IScoutBundle bundle = ScoutTypeUtility.getScoutBundle(fileProject);
          ITreeNode bundleNode = null;
          if (bundle == null) {
            // the product is in any other project
            bundleNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(fileProject));
            if (bundleNode == null) {
              bundleNode = createProjectTreeNode(rootNode, fileProject, visibleFilter);
            }
          }
          else {
            // the product is inside a scout bundle
            bundleNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(bundle));
            if (bundleNode == null) {
              bundleNode = createBundleTreeNode(rootNode, bundle, visibleFilter);
            }
          }

          // create node for the product
          if (bundleNode != null) {
            bundleNode.setCheckable(false);

            createProductFileTreeNode(bundleNode, file, visibleFilter, checkMode);
          }
        }
      }
      return rootNode;
    }
  }
}
