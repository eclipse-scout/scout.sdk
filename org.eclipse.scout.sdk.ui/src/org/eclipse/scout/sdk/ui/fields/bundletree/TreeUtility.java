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

import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.bundletree.CheckableTree.P_DependentFilter;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>TreeUtility</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 04.02.2010
 */
public class TreeUtility {
  private static TreeUtility instance = new TreeUtility();

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
    if (collector.size() == 1) {
      return collector.get(0);
    }
    else if (collector.size() > 1) {
      throw new IllegalStateException("more than 1 node found.");
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
      recAddChildNodes(rootNode, new IScoutBundle[]{scoutProject.getClientBundle()}, IScoutBundle.BUNDLE_CLIENT, ScoutSdkUi.getImage(ScoutSdkUi.IMG_CLIENT), 1000, filter);
    }
    if (scoutProject.getSharedBundle() != null) {
      recAddChildNodes(rootNode, new IScoutBundle[]{scoutProject.getSharedBundle()}, IScoutBundle.BUNDLE_SHARED, ScoutSdkUi.getImage(ScoutSdkUi.IMG_SHARED), 2000, filter);
    }
    if (scoutProject.getServerBundle() != null) {
      recAddChildNodes(rootNode, new IScoutBundle[]{scoutProject.getServerBundle()}, IScoutBundle.BUNDLE_SERVER, ScoutSdkUi.getImage(ScoutSdkUi.IMG_SERVER), 3000, filter);
    }
    return rootNode;
  }

  private static void recAddChildNodes(ITreeNode node, IScoutBundle[] bundles, int type, Image img, long orderNr, ITreeNodeFilter filter) {
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

  public static ITreeNode createNode(ITreeNode parentNode, int type, String name, Image img) {
    return createNode(parentNode, type, name, img, 0);
  }

  /**
   * @return the rootNode
   */
  public static ITreeNode createNode(ITreeNode parentNode, int type, String name, Image img, long orderNr) {
    return createNode(parentNode, type, name, img, orderNr, null);

  }

  public static ITreeNode createNode(ITreeNode parentNode, int type, String name, Image img, long orderNr, Object data) {
    TreeNode node = new TreeNode(type, name);
    node.setData(data);
    node.setOrderNr(orderNr);
    node.setImage(img);
    node.setCheckable(true);
    node.setParent(parentNode);
    parentNode.addChild(node);
    return node;

  }

}
