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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.sdk.internal.workspace.ScoutBundle;

/**
 *
 */
public class BundleGraphNode {

  private Set<BundleGraphNode> m_children;
  private Set<BundleGraphNode> m_parentNodes;
  private final int m_nodeType;
  IPluginModelBase m_pluginModel;
  private ScoutBundle m_scoutBundle;

  BundleGraphNode(ScoutBundle scoutBundle, int nodeType) {
    this(PluginRegistry.findModel(scoutBundle.getProject()), nodeType);
    m_scoutBundle = scoutBundle;
  }

  BundleGraphNode(IPluginModelBase pluginModel, int nodeType) {
    m_pluginModel = pluginModel;
    m_nodeType = nodeType;
    m_children = new HashSet<BundleGraphNode>();
    m_parentNodes = new HashSet<BundleGraphNode>();
  }

  public IPluginModelBase getPluginModel() {
    return m_pluginModel;
  }

  public ScoutBundle getScoutBundle() {
    return m_scoutBundle;
  }

  public String getIdentifier() {
    return getPluginModel().getBundleDescription().getName();
  }

  public int getNodeType() {
    return m_nodeType;
  }

  boolean hasChildNode(BundleGraphNode node) {
    return m_children.contains(node);
  }

  public Set<BundleGraphNode> getChildren() {
    return new HashSet<BundleGraphNode>(m_children);
  }

  void addChildNode(BundleGraphNode child) {
    m_children.add(child);
  }

  boolean removeChildNode(BundleGraphNode child) {
    boolean parentRemoved = child.removeParentNode(this);
    return m_children.remove(child) && parentRemoved;
  }

  void clearChildren() {
    m_children.clear();
  }

  public boolean hasParent(BundleGraphNode node) {
    return m_parentNodes.contains(node);
  }

  public Set<BundleGraphNode> getParents() {
    return new HashSet<BundleGraphNode>(m_parentNodes);
  }

  void addParentNode(BundleGraphNode parent) {
    m_parentNodes.add(parent);
  }

  boolean removeParentNode(BundleGraphNode parent) {
    return m_parentNodes.remove(parent);
  }

  boolean isSubnodeOf(BundleGraphNode parent) {
    for (BundleGraphNode b : m_parentNodes) {
      if (b.equals(parent)) {
        return true;
      }
      else {
        return b.isSubnodeOf(parent);
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return getIdentifier();
  }

}
