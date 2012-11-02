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
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.resources.IProject;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;
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

  public BundleGraph() {
    m_nodeLinks = new HashMap<String, BundleGraphNode>();
    m_scoutBundles = new HashMap<String, ScoutBundle>();
  }

  public synchronized ScoutBundle addWorkspaceProject(IProject project, ScoutWorkspaceEventList eventCollector) {
    ScoutBundle scoutBundle = m_scoutBundles.get(project.getName());
    if (scoutBundle == null) {
      scoutBundle = new ScoutBundle(project, eventCollector.getSource());
      m_scoutBundles.put(project.getName(), scoutBundle);
      eventCollector.setEvent(ScoutWorkspaceEvent.TYPE_BUNDLE_ADDED, scoutBundle, true);
      return scoutBundle;
    }
    else {
      // return null if already registered
      return null;
    }
  }

  public synchronized ScoutBundle removeWorkspaceProject(String bundleId, ScoutWorkspaceEventList eventCollector) {
    ScoutBundle scoutBundle = m_scoutBundles.remove(bundleId);
    if (scoutBundle != null) {
      m_scoutBundles.remove(bundleId);
      eventCollector.setEvent(ScoutWorkspaceEvent.TYPE_BUNDLE_REMOVED, scoutBundle, true);
      return scoutBundle;
    }
    return null;
  }

  public synchronized ScoutBundle[] getAllBundles() {
    return m_scoutBundles.values().toArray(new ScoutBundle[m_scoutBundles.size()]);
  }

  public ScoutBundle getScoutBundle(IProject p) {
    return m_scoutBundles.get(p.getName());
  }

  public synchronized void buildTree() {
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
    // XXX RAP replace by better code
    IPluginModelBase rapModel = PluginRegistry.findModel("org.eclipse.scout.rt.ui.rap");
    if (rapModel != null) {
      BundleGraphNode rapNode = new BundleGraphNode(rapModel, 8);
      freeNodes.add(rapNode);
      m_nodeLinks.put(rapNode.getIdentifier(), rapNode);
    }
    // XXX
    HashSet<BundleGraphNode> parentNodes = new HashSet<BundleGraphNode>();
    m_invisibleRootNode = new P_InvisibleRootNode();
    parentNodes.add(m_invisibleRootNode);
    for (ScoutBundle b : m_scoutBundles.values()) {
      if (b.getProject() != null && b.getProject().exists()) {
        BundleGraphNode node = createTreeNode(b);
        if (node != null) {
          m_nodeLinks.put(b.getBundleName(), node);
          freeNodes.add(node);
        }
      }
    }
    buildTree(parentNodes, freeNodes);
  }

  public synchronized BundleGraphNode getRootNode() {
    return m_invisibleRootNode;
  }

  public synchronized BundleGraphNode getNode(IScoutBundle bundle) {
    return m_nodeLinks.get(bundle.getBundleName());
  }

  /**
   * @return
   */
  public synchronized BundleGraphNode[] getWorkspaceNodes() {
    return m_nodeLinks.values().toArray(new BundleGraphNode[m_nodeLinks.size()]);
  }

  public synchronized void printGraph(PrintStream out) {
    printNodeRect(out, m_invisibleRootNode, 0);
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
    BundleGraphNode node = null;
    int pluginType = getBundleIdRec(pluginModel.getBundleDescription(), new Stack<String>());
    if (pluginType > 0) {
      node = new BundleGraphNode(scoutBundle, pluginType);
    }
    else {
      ScoutSdk.logWarning("could not determ plugin type of '" + pluginModel.getBundleDescription().getName() + "'.");
    }
    return node;
  }

  private int getBundleIdRec(BundleDescription bd, Stack<String> dependencyStack) {
    int id = -1;
    String name = bd.getName();
    try {
      dependencyStack.push(name);
      if (RuntimeClasses.ScoutUiSwingBundleId.equals(name)) {
        id = IScoutElement.BUNDLE_UI_SWING;
      }
      else if (RuntimeClasses.ScoutUiSwtBundleId.equals(name)) {
        id = IScoutElement.BUNDLE_UI_SWT;
      }
      // XXX to code in a better way
      else if ("org.eclipse.scout.rt.ui.rap".equals(name)) {
        id = 8;
      }
      // XXX end do
      else if (RuntimeClasses.ScoutClientBundleId.equals(name)) {
        id = IScoutElement.BUNDLE_CLIENT;
      }
      else if (RuntimeClasses.ScoutServerBundleId.equals(name)) {
        id = IScoutElement.BUNDLE_SERVER;
      }
      else if (RuntimeClasses.ScoutSharedBundleId.equals(name)) {
        id = IScoutElement.BUNDLE_SHARED;
      }
      else {
        for (BundleDescription reqiredDesc : getDirectDependencies(bd)) {
          if (dependencyStack.contains(reqiredDesc.getName())) {
            // a dependency loop was detected: log the loop and stop processing of this part of the dependency graph
            StringBuilder loopMsg = new StringBuilder(Texts.get("DependencyLoopDetected"));
            loopMsg.append(":\n");
            for (String s : dependencyStack) {
              loopMsg.append(s);
              loopMsg.append("\n");
            }
            loopMsg.append(reqiredDesc.getName());
            ScoutSdk.logError(loopMsg.toString());
          }
          else {
            int ret = getBundleIdRec(reqiredDesc, dependencyStack);
            if (getBundleIdPrio(ret) > getBundleIdPrio(id)) {
              // if prio of the plugin-type is higher: remember
              id = ret;
            }
          }
        }
      }
      return id;
    }
    finally {
      dependencyStack.pop();
    }
  }

  /**
   * gets the prio of a bundle id. this prio describes how specific or determinant a bundle id is.<br>
   * higher prio means more specific plugin.<br>
   * example: a plugin with shared & client dependency: is of type client -> client has higher prio!
   * 
   * @param a
   *          the bundle id to check. must be one of the IScoutElement.BUNDLE_* constants.
   * @return the priority of the given id.
   * @see IScoutElement
   */
  private int getBundleIdPrio(int a) {
    switch (a) {
      case IScoutElement.BUNDLE_SHARED: {
        return 10;
      }
      case IScoutElement.BUNDLE_CLIENT: {
        return 20;
      }
      case IScoutElement.BUNDLE_SERVER: {
        return 30;
      }
      case IScoutElement.BUNDLE_UI_SWING: {
        return 40;
      }
      case IScoutElement.BUNDLE_UI_SWT: {
        return 50;
      }
      case 8 /* TODO 8=RAP Bundle, see org.eclipse.scout.sdk.rap.ui.internal.extensions.UiRapBundleNodeFactory */: {
        return 60;
      }
      default: {
        return -10;
      }
    }
  }

  /**
   * gets all resolved direct dependencies of the given bundle.<br>
   * if the given bundle is host for fragments, the dependencies added by these
   * fragments are not part of the return list!
   * 
   * @param bundle
   *          the bundle for which the dependencies should be returned.
   * @return all resolved direct dependencies of the given bundle.
   */
  private List<BundleDescription> getDirectDependencies(BundleDescription bundle) {
    BundleSpecification[] reqSpec = bundle.getRequiredBundles();
    HashSet<String> directDependencies = new HashSet<String>(reqSpec.length);
    for (BundleSpecification bs : reqSpec) {
      directDependencies.add(bs.getName());
    }

    ArrayList<BundleDescription> bd = new ArrayList<BundleDescription>(directDependencies.size());
    for (BundleDescription d : bundle.getResolvedRequires()) {
      if (directDependencies.contains(d.getName())) {
        bd.add(d);
      }
    }
    return bd;
  }

  BundleGraphNode findNode(String identifier) {
    return m_nodeLinks.get(identifier);
  }

  private void printNodeRect(PrintStream out, BundleGraphNode node, int level) {
    String indent = "";
    for (int i = 0; i < level; i++) {
      indent = indent + "\t";
    }
    String scoutProjectName = "NA";
    if (node.getScoutBundle() != null && node.getScoutBundle().getScoutProject() != null) {
      scoutProjectName = node.getScoutBundle().getScoutProject().getProjectName();
    }
    int bundleType = node.getNodeType();
    out.println(indent + "-- BUNDLE '" + node.getIdentifier() + " " + scoutProjectName + "'-" + bundleType + "-");
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
