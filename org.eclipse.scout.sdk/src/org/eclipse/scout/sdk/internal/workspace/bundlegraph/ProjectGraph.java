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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.ScoutBundle;
import org.eclipse.scout.sdk.internal.workspace.ScoutProject;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.helper.ScoutProjectDescription;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutElement;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

/**
 *
 */
public class ProjectGraph {
  private HashMap<IScoutBundle, ProjectGraphNode> m_bundleLinks;
  private HashMap<ScoutProject, ProjectGraphNode> m_projectLinks;

  private ProjectGraphNode m_invisibleRoot;

  public ProjectGraph() {
    m_bundleLinks = new HashMap<IScoutBundle, ProjectGraphNode>();
    m_projectLinks = new HashMap<ScoutProject, ProjectGraphNode>();
  }

  public synchronized ProjectGraphNode getRootNode() {
    return m_invisibleRoot;
  }

  public synchronized ScoutProject[] getRootProjects() {
    ArrayList<ScoutProject> projects = new ArrayList<ScoutProject>();
    for (ProjectGraphNode n : m_invisibleRoot.getSubProjects()) {
      projects.add(n.getScoutProject());
    }
    return projects.toArray(new ScoutProject[projects.size()]);
  }

  public synchronized ScoutProject[] getSubProjects(IScoutProject project) {
    ArrayList<ScoutProject> projects = new ArrayList<ScoutProject>();
    ProjectGraphNode node = m_projectLinks.get(project);
    for (ProjectGraphNode n : node.getSubProjects()) {
      projects.add(n.getScoutProject());
    }
    return projects.toArray(new ScoutProject[projects.size()]);
  }

  public synchronized ScoutProject getParentProject(ScoutProject project) {
    ProjectGraphNode node = m_projectLinks.get(project);
    if (node != null) {
      return node.getParentProject().getScoutProject();
    }
    return null;
  }

  public synchronized ProjectGraphNode getProjectNode(IScoutBundle bundle) {
    return m_bundleLinks.get(bundle);
  }

  // build tree

  public synchronized void buildGraph(BundleGraph bundleGraph, ScoutWorkspaceEventList eventCollector) {
    HashMap<String, ScoutProject> backupProjects = new HashMap<String, ScoutProject>();
    for (ScoutProject p : m_projectLinks.keySet()) {
      backupProjects.put(p.getProjectName(), p);
    }
    // build tree
    m_bundleLinks.clear();
    m_projectLinks.clear();
    m_invisibleRoot = null;
    HashSet<BundleGraphNode> visitedNodes = new HashSet<BundleGraphNode>();
    ScoutProjectDescription invisibleScoutProject = new ScoutProjectDescription(null);
    invisibleScoutProject.addNode(bundleGraph.findNode(RuntimeClasses.ScoutUiSwtBundleId));
    invisibleScoutProject.addNode(bundleGraph.findNode(RuntimeClasses.ScoutUiSwingBundleId));
    // XXX find a better way to include rap
    invisibleScoutProject.addNode(bundleGraph.findNode("org.eclipse.scout.rt.ui.rap"));
    invisibleScoutProject.addNode(bundleGraph.findNode(RuntimeClasses.ScoutClientBundleId));
    invisibleScoutProject.addNode(bundleGraph.findNode(RuntimeClasses.ScoutSharedBundleId));
    invisibleScoutProject.addNode(bundleGraph.findNode(RuntimeClasses.ScoutServerBundleId));
    visitedNodes.addAll(Arrays.asList(invisibleScoutProject.getAllNodes()));
    findChildProjectsRec(invisibleScoutProject, visitedNodes, bundleGraph);
    // build projects
    m_invisibleRoot = new ProjectGraphNode(null, null);
    buildProjects(invisibleScoutProject, m_invisibleRoot, backupProjects, eventCollector);
    for (ScoutProject p : backupProjects.values()) {
      eventCollector.setEvent(ScoutWorkspaceEvent.TYPE_PROJECT_REMOVED, p, true);
    }
  }

  public void printProjectGraph(PrintStream out) {
    printProjectRect(out, m_invisibleRoot, 0);
  }

  private void printProjectRect(PrintStream out, ProjectGraphNode node, int level) {
    String indent = "";
    for (int i = 0; i < level; i++) {
      indent = indent + "\t";
    }
    String projectName = "NOT DEFINED";
    if (node.getScoutProject() != null) {
      projectName = node.getScoutProject().getProjectName();
    }
    out.println(indent + "-- PROJECT '" + projectName + "' --");
    if (node.getScoutProject() != null) {
      for (IScoutBundle b : node.getScoutProject().getAllScoutBundles()) {
        out.println(indent + " - '" + b.getBundleName() + "'");
      }
    }
    out.println(indent + "-- END '" + projectName + "' --");
    for (ProjectGraphNode child : node.getSubProjects()) {
      printProjectRect(out, child, level + 1);
    }
  }

  private void buildProjects(ScoutProjectDescription desc, ProjectGraphNode parentProject, Map<String, ScoutProject> backup, ScoutWorkspaceEventList eventCollector) {
    for (ScoutProjectDescription subProject : desc.getSubProjectDescriptions()) {
      String projectName = subProject.determProjectName();
      ScoutProject project = backup.remove(projectName);
      if (project == null) {
        project = new ScoutProject(projectName, eventCollector.getSource());
        eventCollector.setEvent(ScoutWorkspaceEvent.TYPE_PROJECT_ADDED, project, true);
      }
      HashSet<ScoutBundle> backupBundles = new HashSet<ScoutBundle>(Arrays.asList(project.getAllScoutBundles()));
      ProjectGraphNode projectNode = new ProjectGraphNode(project, parentProject);
      m_projectLinks.put(project, projectNode);
      for (BundleGraphNode n : subProject.getAllNodes()) {
        if (n.getScoutBundle() != null) {
          backupBundles.remove(n.getScoutBundle());
          if (project.addScoutBundle(n.getScoutBundle())) {
            eventCollector.setEvent(ScoutWorkspaceEvent.TYPE_PROJECT_CHANGED, project, false);
          }
          m_bundleLinks.put(n.getScoutBundle(), projectNode);
        }
      }
      for (ScoutBundle b : backupBundles) {
        project.removeScoutBundle(b);
        eventCollector.setEvent(ScoutWorkspaceEvent.TYPE_PROJECT_CHANGED, project, false);
      }
      if (parentProject != null) {
        parentProject.addSubProject(projectNode);
      }
      buildProjects(subProject, projectNode, backup, eventCollector);
    }
  }

  private void findChildProjectsRec(ScoutProjectDescription parentDesc, Set<BundleGraphNode> visitedNodes, BundleGraph bundleGraph) {
    BundleGraphNode parentShared = parentDesc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_SHARED));
    if (parentShared != null) {
      IBundleGraphNodeFilter sharedFilter = BundleGraphNodeFilters.getMultiFilter(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_SHARED), BundleGraphNodeFilters.getNotInSetFilter(visitedNodes));
      for (BundleGraphNode shared : bundleGraph.getDirectChildren(parentShared, sharedFilter)) {
        ScoutProjectDescription childProject = new ScoutProjectDescription(parentDesc);
        childProject.addNode(shared);
        buildProjectDesc(childProject, parentDesc, visitedNodes, bundleGraph);
        parentDesc.addSubProjectDescription(childProject);
        visitedNodes.addAll(Arrays.asList(childProject.getAllNodes()));
        findChildProjectsRec(childProject, visitedNodes, bundleGraph);
      }
    }
    // only client
    BundleGraphNode parentClient = parentDesc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_CLIENT));
    if (parentClient != null) {
      IBundleGraphNodeFilter clientFilter = BundleGraphNodeFilters.getMultiFilter(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_CLIENT), BundleGraphNodeFilters.getNotInSetFilter(visitedNodes));
      for (BundleGraphNode client : bundleGraph.getDirectChildren(parentClient, clientFilter)) {
        ScoutProjectDescription childProject = new ScoutProjectDescription(parentDesc);
        childProject.addNode(client);
        buildProjectDesc(childProject, parentDesc, visitedNodes, bundleGraph);
        parentDesc.addSubProjectDescription(childProject);
        visitedNodes.addAll(Arrays.asList(childProject.getAllNodes()));
        findChildProjectsRec(childProject, visitedNodes, bundleGraph);
      }
    }
    // only swing
    BundleGraphNode parentSwing = parentDesc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_UI_SWING));
    if (parentSwing != null) {
      IBundleGraphNodeFilter swingFilter = BundleGraphNodeFilters.getMultiFilter(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_UI_SWING), BundleGraphNodeFilters.getNotInSetFilter(visitedNodes));
      for (BundleGraphNode swing : bundleGraph.getDirectChildren(parentSwing, swingFilter)) {
        ScoutProjectDescription childProject = new ScoutProjectDescription(parentDesc);
        childProject.addNode(swing);
        buildProjectDesc(childProject, parentDesc, visitedNodes, bundleGraph);
        parentDesc.addSubProjectDescription(childProject);
        visitedNodes.addAll(Arrays.asList(childProject.getAllNodes()));
        findChildProjectsRec(childProject, visitedNodes, bundleGraph);
      }
    }
    // only swt
    BundleGraphNode parentSwt = parentDesc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_UI_SWT));
    if (parentSwt != null) {
      IBundleGraphNodeFilter swtFilter = BundleGraphNodeFilters.getMultiFilter(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_UI_SWT), BundleGraphNodeFilters.getNotInSetFilter(visitedNodes));
      for (BundleGraphNode swt : bundleGraph.getDirectChildren(parentSwt, swtFilter)) {
        ScoutProjectDescription childProject = new ScoutProjectDescription(parentDesc);
        childProject.addNode(swt);
        buildProjectDesc(childProject, parentDesc, visitedNodes, bundleGraph);
        parentDesc.addSubProjectDescription(childProject);
        visitedNodes.addAll(Arrays.asList(childProject.getAllNodes()));
        findChildProjectsRec(childProject, visitedNodes, bundleGraph);
      }
    }
    //TODO RAP only rap Find a better way
    BundleGraphNode parentRap = parentDesc.getNode(BundleGraphNodeFilters.getFilterByType(8));
    if (parentRap != null) {
      IBundleGraphNodeFilter rapFilter = BundleGraphNodeFilters.getMultiFilter(BundleGraphNodeFilters.getFilterByType(8), BundleGraphNodeFilters.getNotInSetFilter(visitedNodes));
      for (BundleGraphNode rapNode : bundleGraph.getDirectChildren(parentRap, rapFilter)) {
        ScoutProjectDescription childProject = new ScoutProjectDescription(parentDesc);
        childProject.addNode(rapNode);
        buildProjectDesc(childProject, parentDesc, visitedNodes, bundleGraph);
        parentDesc.addSubProjectDescription(childProject);
        visitedNodes.addAll(Arrays.asList(childProject.getAllNodes()));
        findChildProjectsRec(childProject, visitedNodes, bundleGraph);
      }
    }

    // only server
    BundleGraphNode parentServer = parentDesc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_SERVER));
    if (parentServer != null) {
      IBundleGraphNodeFilter serverFilter = BundleGraphNodeFilters.getMultiFilter(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_SERVER), BundleGraphNodeFilters.getNotInSetFilter(visitedNodes));
      for (BundleGraphNode server : bundleGraph.getDirectChildren(parentServer, serverFilter)) {
        ScoutProjectDescription childProject = new ScoutProjectDescription(parentDesc);
        childProject.addNode(server);
        buildProjectDesc(childProject, parentDesc, visitedNodes, bundleGraph);
        visitedNodes.addAll(Arrays.asList(childProject.getAllNodes()));
        parentDesc.addSubProjectDescription(childProject);
        findChildProjectsRec(childProject, visitedNodes, bundleGraph);
      }
    }
  }

  private void buildProjectDesc(ScoutProjectDescription desc, ScoutProjectDescription parentDesc, Set<BundleGraphNode> visitedNodes, BundleGraph bundleGraph) {
    BundleGraphNode sharedNode = desc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_SHARED));
    if (sharedNode != null) {
      // client
      BundleGraphNode parentClientNode = parentDesc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_CLIENT));
      if (parentClientNode != null) {
        BundleGraphNode clientNode = findCommonChildNode(parentClientNode, sharedNode, IScoutElement.BUNDLE_CLIENT, visitedNodes, bundleGraph);
        if (clientNode != null) {
          desc.addNode(clientNode);

        }
      }
      // server
      BundleGraphNode parentServerNode = parentDesc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_SERVER));
      if (parentServerNode != null) {
        BundleGraphNode serverNode = findCommonChildNode(parentServerNode, sharedNode, IScoutElement.BUNDLE_SERVER, visitedNodes, bundleGraph);
        if (serverNode != null) {
          desc.addNode(serverNode);
        }
      }
    }
    BundleGraphNode clientNode = desc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_CLIENT));
    if (clientNode != null) {
      fillUiNodes(desc, parentDesc, visitedNodes, bundleGraph);
    }
  }

  private void fillUiNodes(ScoutProjectDescription desc, ScoutProjectDescription parentDesc, Set<BundleGraphNode> visitedNodes, BundleGraph bundleGraph) {
    BundleGraphNode clientNode = desc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_CLIENT));
    if (clientNode != null) {
      // ui swing
      BundleGraphNode parentSwingNode = parentDesc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_UI_SWING));
      BundleGraphNode uiSwingNode = findCommonChildNode(parentSwingNode, clientNode, IScoutElement.BUNDLE_UI_SWING, visitedNodes, bundleGraph);
      if (uiSwingNode != null) {
        desc.addNode(uiSwingNode);
      }
      // ui swt
      BundleGraphNode parentSwtNode = parentDesc.getNode(BundleGraphNodeFilters.getFilterByType(IScoutElement.BUNDLE_UI_SWT));
      BundleGraphNode uiSwtNode = findCommonChildNode(parentSwtNode, clientNode, IScoutElement.BUNDLE_UI_SWT, visitedNodes, bundleGraph);
      if (uiSwtNode != null) {
        desc.addNode(uiSwtNode);
      }
      //TODO RAP find a better way ui rap
      BundleGraphNode parentRapNode = parentDesc.getNode(BundleGraphNodeFilters.getFilterByType(8));
      BundleGraphNode uiRapNode = findCommonChildNode(parentRapNode, clientNode, 8, visitedNodes, bundleGraph);
      if (uiRapNode != null) {
        desc.addNode(uiRapNode);
      }
    }
  }

  private BundleGraphNode findCommonChildNode(BundleGraphNode parent1, BundleGraphNode parent2, int nodeType, Set<BundleGraphNode> visitedNodes, BundleGraph bundleGraph) {
    IBundleGraphNodeFilter clientFilter = BundleGraphNodeFilters.getMultiFilter(BundleGraphNodeFilters.getFilterByType(nodeType),
        BundleGraphNodeFilters.getDirectChildFilter(parent2), BundleGraphNodeFilters.getNotInSetFilter(visitedNodes));
    BundleGraphNode[] candidates = bundleGraph.getDirectChildren(parent1, clientFilter);
    if (candidates.length == 1) {
      return candidates[0];
    }
    else if (candidates.length > 1) {
      ScoutSdk.logWarning("could not find common child node of '" + parent1.getIdentifier() + "' and '" + parent2.getIdentifier() + "' bundle type '" + nodeType + "'.");
    }
    return null;
  }

}
