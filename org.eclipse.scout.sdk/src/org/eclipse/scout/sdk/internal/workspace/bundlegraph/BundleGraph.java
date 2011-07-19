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
package org.eclipse.scout.sdk.internal.workspace.bundlegraph;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.ScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutElement;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

/**
 *
 */
public class BundleGraph {
  static final String NODE_ID_ROOT = "invisibleRootNode";
  static final int TYPE_ROOT = -1;

  private BundleGraphNode m_invisibleRootNode;

  private final HashMap<String, BundleGraphNode> m_nodeLinks;
  private final HashMap<String, ScoutBundle> m_scoutBundles;
  // private final Set<ScoutBundle> m_scoutBundles;
  // private final HashMap<String, ScoutBundle> m_projectLinks;

  private Object lock = new Object();

  public BundleGraph() {
    m_nodeLinks = new HashMap<String, BundleGraphNode>();
    m_scoutBundles = new HashMap<String, ScoutBundle>();
    // m_projectLinks=new HashMap<String, ScoutBundle>();
  }

  public ScoutBundle addWorkspaceProject(IProject project, ScoutWorkspaceEventList eventCollector) {
    synchronized (lock) {
      ScoutBundle scoutBundle = m_scoutBundles.get(project.getName());
      if (scoutBundle == null) {
        scoutBundle = new ScoutBundle(project, eventCollector.getSource());
        // m_projectLinks.put(project.getName(), scoutBundle);
        m_scoutBundles.put(project.getName(), scoutBundle);
        eventCollector.setEvent(ScoutWorkspaceEvent.TYPE_BUNDLE_ADDED, scoutBundle, true);
        return scoutBundle;
      }
      else {
        // return null if already registered
        return null;
      }
    }
  }

  public ScoutBundle removeWorkspaceProject(IProject project, ScoutWorkspaceEventList eventCollector) {
    synchronized (lock) {
      ScoutBundle scoutBundle = m_scoutBundles.remove(project.getName());
      if (scoutBundle != null) {
        m_scoutBundles.remove(scoutBundle);
        eventCollector.setEvent(ScoutWorkspaceEvent.TYPE_BUNDLE_REMOVED, scoutBundle, true);
      }
      return scoutBundle;
    }
  }

  public ScoutBundle[] getAllBundles() {
    synchronized (lock) {
      return m_scoutBundles.values().toArray(new ScoutBundle[m_scoutBundles.size()]);
    }
  }

  public ScoutBundle getScoutBundle(IProject p) {
    return m_scoutBundles.get(p.getName());
  }

  public void buildTree() {
    m_invisibleRootNode = null;
    m_nodeLinks.clear();
    Set<BundleGraphNode> freeNodes = new HashSet<BundleGraphNode>();
    // add scout bundles
    IPluginModelBase uiSwtModel = PluginRegistry.findModel(RuntimeClasses.ScoutUiSwtBundleId);
    if (uiSwtModel != null) {
      BundleGraphNode swtNode = new BundleGraphNode(uiSwtModel, IScoutElement.BUNDLE_UI_SWT);
      freeNodes.add(swtNode);
      m_nodeLinks.put(swtNode.getIdentifier(), swtNode);
    }
    IPluginModelBase uiSwingModel = PluginRegistry.findModel(RuntimeClasses.ScoutUiSwingBundleId);
    if (uiSwingModel != null) {
      BundleGraphNode swingNode = new BundleGraphNode(uiSwingModel, IScoutElement.BUNDLE_UI_SWING);
      freeNodes.add(swingNode);
      m_nodeLinks.put(swingNode.getIdentifier(), swingNode);
    }
    IPluginModelBase clientModel = PluginRegistry.findModel(RuntimeClasses.ScoutClientBundleId);
    if (clientModel != null) {
      BundleGraphNode clientNode = new BundleGraphNode(clientModel, IScoutElement.BUNDLE_CLIENT);
      freeNodes.add(clientNode);
      m_nodeLinks.put(clientNode.getIdentifier(), clientNode);
    }
    IPluginModelBase sharedModel = PluginRegistry.findModel(RuntimeClasses.ScoutSharedBundleId);
    if (sharedModel != null) {
      BundleGraphNode sharedNode = new BundleGraphNode(sharedModel, IScoutElement.BUNDLE_SHARED);
      freeNodes.add(sharedNode);
      m_nodeLinks.put(sharedNode.getIdentifier(), sharedNode);
    }
    IPluginModelBase serverModel = PluginRegistry.findModel(RuntimeClasses.ScoutServerBundleId);
    if (serverModel != null) {
      BundleGraphNode serverNode = new BundleGraphNode(serverModel, IScoutElement.BUNDLE_SERVER);
      freeNodes.add(serverNode);
      m_nodeLinks.put(serverNode.getIdentifier(), serverNode);
    }
    HashSet<BundleGraphNode> parentNodes = new HashSet<BundleGraphNode>();
    m_invisibleRootNode = new P_InvisibleRootNode();
    parentNodes.add(m_invisibleRootNode);
    for (ScoutBundle b : m_scoutBundles.values()) {
      BundleGraphNode node = createTreeNode(b);
      if (node != null) {
        m_nodeLinks.put(b.getBundleName(), node);
        freeNodes.add(node);
      }
    }
    buildTree(parentNodes, freeNodes);
  }

  public BundleGraphNode getRootNode() {
    return m_invisibleRootNode;
  }

  public BundleGraphNode getNode(IScoutBundle bundle) {
    return m_nodeLinks.get(bundle.getBundleName());
  }

  /**
   * @return
   */
  public BundleGraphNode[] getWorkspaceNodes() {
    return m_nodeLinks.values().toArray(new BundleGraphNode[m_nodeLinks.size()]);
  }

  private void buildTree(Set<BundleGraphNode> parentNodes, Set<BundleGraphNode> freeNodes) {
    HashSet<BundleGraphNode> roots = new HashSet<BundleGraphNode>(freeNodes);
    for (Iterator<BundleGraphNode> rootIt = roots.iterator(); rootIt.hasNext();) {
      BundleGraphNode currentNode = rootIt.next();
      Set<BundleGraphNode> visitees = new HashSet<BundleGraphNode>(freeNodes);
      visitees.remove(currentNode);
      for (BundleGraphNode visitee : visitees) {
        if (isSubnode(visitee, currentNode)) {
          rootIt.remove();
          break;
        }
      }
    }
    if (!roots.isEmpty()) {
      for (BundleGraphNode toInsert : roots) {
        freeNodes.remove(toInsert);
        for (BundleGraphNode potParent : parentNodes) {
          if (isSubnode(potParent, toInsert)) {
            // if(toInsert.isOnClasspath(potParent)){
            potParent.addChildNode(toInsert);
            toInsert.addParentNode(potParent);
          }
        }
      }
      buildTree(roots, freeNodes);
    }
    else if (!freeNodes.isEmpty()) {
      ScoutSdk.logWarning("the bundle dependency tree is not correct.");
    }
  }

  BundleGraphNode[] getDirectChildren(BundleGraphNode node, IBundleGraphNodeFilter filter) {
    if (node == null) {
      return new BundleGraphNode[0];
    }
    ArrayList<BundleGraphNode> acceptedChildren = new ArrayList<BundleGraphNode>();
    for (BundleGraphNode cn : node.getChildren()) {
      if (filter.accept(cn)) {
        acceptedChildren.add(cn);
      }
    }
    return acceptedChildren.toArray(new BundleGraphNode[acceptedChildren.size()]);
  }

  private boolean isSubnode(BundleGraphNode potParent, BundleGraphNode potChild) {
    if (potParent == m_invisibleRootNode) {
      return true;
    }
    else {
      String nodeBundleName = potParent.getPluginModel().getBundleDescription().getName();
      for (BundleSpecification spec : potChild.getPluginModel().getBundleDescription().getRequiredBundles()) {
        if (nodeBundleName.equals(spec.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  private BundleGraphNode createTreeNode(ScoutBundle scoutBundle) {
    IPluginModelBase pluginModel = PluginRegistry.findModel(scoutBundle.getProject());
    HashMap<String, BundleDescription> requiredBundles = new HashMap<String, BundleDescription>();
    requiredBundles.put(pluginModel.getBundleDescription().getName(), pluginModel.getBundleDescription());
    BundleGraphNode node = null;
    int pluginType = getBundleIdRec(requiredBundles);
    if (pluginType > 0) {
      node = new BundleGraphNode(scoutBundle, pluginType);
    }
    else {
      ScoutSdk.logWarning("could not determ plugin type of '" + pluginModel.getBundleDescription().getName() + "'.");
    }
    return node;
  }

  private int getBundleIdRec(HashMap<String, BundleDescription> requiredDescs) {
    int id = -1;
    if (requiredDescs.containsKey(RuntimeClasses.ScoutUiSwingBundleId)) {
      id = IScoutElement.BUNDLE_UI_SWING;
    }
    else if (requiredDescs.containsKey(RuntimeClasses.ScoutUiSwtBundleId)) {
      id = IScoutElement.BUNDLE_UI_SWT;
    }
    else if (requiredDescs.containsKey(RuntimeClasses.ScoutClientBundleId)) {
      id = IScoutElement.BUNDLE_CLIENT;
    }
    else if (requiredDescs.containsKey(RuntimeClasses.ScoutServerBundleId)) {
      id = IScoutElement.BUNDLE_SERVER;
    }
    else if (requiredDescs.containsKey(RuntimeClasses.ScoutSharedBundleId)) {
      id = IScoutElement.BUNDLE_SHARED;
    }
    else {
      HashMap<String, BundleDescription> requiredBundles = new HashMap<String, BundleDescription>();
      for (BundleDescription reqiredDesc : requiredDescs.values()) {
        for (BundleDescription d : reqiredDesc.getResolvedRequires()) {
          requiredBundles.put(d.getName(), d);
        }
      }
      if (!requiredBundles.isEmpty()) {
        return getBundleIdRec(requiredBundles);
      }
    }
    return id;
  }

  BundleGraphNode findNode(String identifier) {
    return m_nodeLinks.get(identifier);
  }

  public void printGraph(PrintStream out) {
    printNodeRect(out, m_invisibleRootNode, 0);
  }

  private void printNodeRect(PrintStream out, BundleGraphNode node, int level) {
    String indent = "";
    for (int i = 0; i < level; i++) {
      indent = indent + "\t";
    }
    out.println(indent + "-- BUNDLE '" + node.getIdentifier() + " " + node.getScoutBundle().getProject().exists() + "'--");
    for (BundleGraphNode c : node.getChildren()) {
      printNodeRect(out, c, level + 1);
    }
  }

  private class P_InvisibleRootNode extends BundleGraphNode {
    public P_InvisibleRootNode() {
      super((IPluginModelBase) null, TYPE_ROOT);
    }

    @Override
    public String getIdentifier() {
      return NODE_ID_ROOT;
    }
  } // end class P_InvisibleRootNode

}
