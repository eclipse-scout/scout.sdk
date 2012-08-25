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
package org.eclipse.scout.sdk.internal.workspace.bundlegraph.helper;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.BundleGraphNode;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.BundleGraphNodeFilters;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.IBundleGraphNodeFilter;
import org.eclipse.scout.sdk.workspace.IScoutElement;

/**
 *
 */
public class ScoutProjectDescription {

  private ArrayList<BundleGraphNode> m_nodes = new ArrayList<BundleGraphNode>();
  private ArrayList<ScoutProjectDescription> m_subDescriptions = new ArrayList<ScoutProjectDescription>();
  private ScoutProjectDescription m_parentDescription;

  public ScoutProjectDescription(ScoutProjectDescription parentDesc) {
    m_parentDescription = parentDesc;
  }

  public boolean addSubProjectDescription(ScoutProjectDescription desc) {
    return m_subDescriptions.add(desc);
  }

  public boolean removeSubProjectDescription(ScoutProjectDescription desc) {
    return m_subDescriptions.remove(desc);
  }

  public String determProjectName() {
    String regex = "^(.*)\\.(ui\\.|client|shared|server)(\\.(.*))?$";
    String prefix = null;
    String postfix = null;
    for (BundleGraphNode node : m_nodes) {
      Matcher m = Pattern.compile(regex).matcher(node.getIdentifier());
      if (m.find()) {
        if (prefix != null && !prefix.equals(m.group(1))) {
          ScoutSdk.logWarning("bundlenames not consistent. Expected scout project do have the same prefix like 'org.eclipse.'");
        }
        else {
          prefix = m.group(1);
        }
        if (!StringUtility.isNullOrEmpty(m.group(4))) {
          if (postfix != null && !postfix.equals(m.group(4))) {
            ScoutSdk.logWarning("bundlenames not consistent. Expected scout project do have the same postfix like 'core' or 'marketing'");
          }
          else {
            postfix = m.group(4);
          }
        }
      }
    }
    StringBuilder builder = new StringBuilder();
    if (!StringUtility.isNullOrEmpty(prefix)) {
      builder.append(prefix);
    }
    if (!StringUtility.isNullOrEmpty(postfix)) {
      builder.append(" (" + postfix + ")");
    }
    return builder.toString();
  }

  public ScoutProjectDescription getParentDescription() {
    return m_parentDescription;
  }

  public ScoutProjectDescription[] getSubProjectDescriptions() {
    return m_subDescriptions.toArray(new ScoutProjectDescription[m_subDescriptions.size()]);
  }

  public void addNode(BundleGraphNode node) {
    if (node != null) {
      m_nodes.add(node);
    }
  }

  public void removeNode(BundleGraphNode node) {
    m_nodes.remove(node);
  }

  public BundleGraphNode[] getAllNodes() {
    return m_nodes.toArray(new BundleGraphNode[m_nodes.size()]);
  }

  public BundleGraphNode getNode(IBundleGraphNodeFilter filter) {
    ArrayList<BundleGraphNode> result = new ArrayList<BundleGraphNode>();
    for (BundleGraphNode node : m_nodes) {
      if (filter.accept(node)) {
        result.add(node);
      }
    }
    if (result.size() > 1) {
      throw new IllegalStateException("more than 1 node found.");
    }
    else if (result.size() == 1) {
      return result.get(0);
    }
    else {
      return null;
    }
  }

  public BundleGraphNode getUiSwingNode() {
    return getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_UI_SWING));
  }

  public IPluginModelBase getSwingPlugin() {
    BundleGraphNode node = getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_UI_SWING));
    if (node != null) {
      return node.getPluginModel();
    }
    return null;
  }

  public BundleGraphNode getUiSwtNode() {
    return getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_UI_SWT));
  }

  public IPluginModelBase getSwtPlugin() {
    BundleGraphNode node = getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_UI_SWT));
    if (node != null) {
      return node.getPluginModel();
    }
    return null;
  }

  public BundleGraphNode getClientNode() {
    return getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_CLIENT));
  }

  public IPluginModelBase getClientPlugin() {
    BundleGraphNode node = getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_CLIENT));
    if (node != null) {
      return node.getPluginModel();
    }
    return null;
  }

  public BundleGraphNode getUiShared() {
    return getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_SHARED));
  }

  public IPluginModelBase getSharedPlugin() {
    BundleGraphNode node = getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_SHARED));
    if (node != null) {
      return node.getPluginModel();
    }
    return null;
  }

  public BundleGraphNode getUiServer() {
    return getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_SERVER));
  }

  public IPluginModelBase getServerPlugin() {
    BundleGraphNode node = getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_SERVER));
    if (node != null) {
      return node.getPluginModel();
    }
    return null;
  }
}
