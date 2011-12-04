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
package org.eclipse.scout.sdk.internal.workspace;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.IPluginModelListener;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelDelta;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.BundleGraph;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.BundleGraphNode;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.ProjectGraph;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.ProjectGraphNode;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.ScoutWorkspaceEventList;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.IScoutWorkspace;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

/**
 *
 */
@SuppressWarnings("restriction")
public final class ScoutWorkspace implements IScoutWorkspace {
  private EventListenerList m_eventListeners = new EventListenerList();

  private final static ScoutWorkspace INSTANCE = new ScoutWorkspace();

  private BundleGraph m_bundleGraph;
  private ProjectGraph m_projectGraph;

  private ScoutWorkspace() {
    m_bundleGraph = new BundleGraph();
    m_projectGraph = new ProjectGraph();
    PDECore.getDefault().getModelManager().addPluginModelListener(new P_PluginModelListener());
    // init
    ScoutWorkspaceEventList eventCollector = new ScoutWorkspaceEventList(this);
    for (IPluginModelBase m : PluginRegistry.getWorkspaceModels()) {
      IProject project = m.getUnderlyingResource().getProject();
      try {
        addProjectNoFire(project, eventCollector);
      }
      catch (CoreException e) {
        ScoutSdk.logError("error during initializing workspace. could not load scout proejct '" + project.getName() + "'", e);
      }
    }
    rebuildGraphNoFire(eventCollector);
    eventCollector.addEvent(ScoutWorkspaceEvent.TYPE_WORKSPACE_INITIALIZED, null);
    fireWorkspaceEvnets(eventCollector.getAllEvents());
  }

  public static ScoutWorkspace getInstance() {
    return INSTANCE;
  }

  @Override
  public void addWorkspaceListener(IScoutWorkspaceListener listener) {
    m_eventListeners.add(IScoutWorkspaceListener.class, listener);
  }

  @Override
  public void removeWorkspaceListener(IScoutWorkspaceListener listener) {
    m_eventListeners.remove(IScoutWorkspaceListener.class, listener);
  }

  private void fireWorkspaceEvnets(ScoutWorkspaceEvent[] events) {
    for (ScoutWorkspaceEvent e : events) {
      fireWorkspaceEvent(e);
    }
  }

  private void fireWorkspaceEvent(ScoutWorkspaceEvent e) {
    for (IScoutWorkspaceListener l : m_eventListeners.getListeners(IScoutWorkspaceListener.class)) {
      try {
        l.worspaceChanged(e);
      }
      catch (Throwable t) {
        ScoutSdk.logError("error during listener notification.", t);
      }
    }
  }

  @Override
  public ScoutProject[] getRootProjects() {
    return m_projectGraph.getRootProjects();
  }

  @Override
  public IScoutProject getScoutProject(IScoutBundle bundle) {
    ProjectGraphNode node = m_projectGraph.getProjectNode(bundle);
    if (node != null) {
      return node.getScoutProject();
    }
    return null;
  }

  @Override
  public IScoutProject findScoutProject(String projectName) {
    return findScoutProjectRec(m_projectGraph.getRootNode(), projectName);
  }

  private IScoutProject findScoutProjectRec(ProjectGraphNode node, String projectName) {
    if (node.getScoutProject() != null && projectName.equals(node.getScoutProject().getProjectName())) {
      return node.getScoutProject();
    }
    for (ProjectGraphNode childNode : node.getSubProjects()) {
      IScoutProject scoutProject = findScoutProjectRec(childNode, projectName);
      if (scoutProject != null) {
        return scoutProject;
      }
    }
    return null;
  }

  @Override
  public ScoutProject getParentProject(ScoutProject scoutProject) {
    return m_projectGraph.getParentProject(scoutProject);
  }

  @Override
  public ScoutProject[] getSubProjects(IScoutProject scoutProject) {
    return m_projectGraph.getSubProjects(scoutProject);
  }

  public int getBundleType(ScoutBundle scoutBundle) {
    BundleGraphNode node = m_bundleGraph.getNode(scoutBundle);
    if (node != null) {
      return node.getNodeType();
    }
    else {
      ScoutSdk.logError("could not find bundle type of '" + scoutBundle.getBundleName() + "'.");
      return -1;
    }
  }

  @Override
  public IScoutBundle[] getAllBundles() {
    ArrayList<IScoutBundle> result = new ArrayList<IScoutBundle>();
    for (BundleGraphNode desc : m_bundleGraph.getWorkspaceNodes()) {
      result.add(desc.getScoutBundle());
    }
    return result.toArray(new IScoutBundle[result.size()]);
  }

  @Override
  public IScoutBundle getScoutBundle(IProject project) {
    return m_bundleGraph.getScoutBundle(project);
  }

  public void printBundleGraph() {
    m_bundleGraph.printGraph(System.out);
  }

  public IScoutBundle[] getDirectDependents(IScoutBundle bundle, IScoutBundleFilter filter) {
    ArrayList<IScoutBundle> result = new ArrayList<IScoutBundle>();
    BundleGraphNode node = m_bundleGraph.getNode(bundle);
    for (BundleGraphNode candidate : node.getChildren()) {
      if (candidate.getScoutBundle() != null && filter.accept(candidate.getScoutBundle())) {
        result.add(candidate.getScoutBundle());
      }
    }
    return result.toArray(new IScoutBundle[result.size()]);
  }

  public IScoutBundle[] getDependentBundles(IScoutBundle bundle, IScoutBundleFilter filter, boolean includeThis) {
    List<ScoutBundle> collector = new ArrayList<ScoutBundle>();
    BundleGraphNode node = m_bundleGraph.getNode(bundle);
    if (node != null) {
      ArrayList<BundleGraphNode> next = new ArrayList<BundleGraphNode>();
      if (includeThis) {
        next.add(node);
      }
      else {
        next.addAll(node.getChildren());
      }
      ArrayList<BundleGraphNode> current = new ArrayList<BundleGraphNode>();
      while (!next.isEmpty()) {
        current = next;
        next = new ArrayList<BundleGraphNode>();
        for (BundleGraphNode n : current) {
          next.addAll(n.getChildren());
          if (n.getScoutBundle() != null && filter.accept(n.getScoutBundle())) {
            collector.add(n.getScoutBundle());
          }
        }
      }
    }
    return collector.toArray(new ScoutBundle[collector.size()]);
  }

  public IScoutBundle[] getDirectRequiredBundles(IScoutBundle bundle, IScoutBundleFilter filter) {
    List<ScoutBundle> collector = new ArrayList<ScoutBundle>();
    BundleGraphNode node = m_bundleGraph.getNode(bundle);
    if (node != null) {
      for (BundleGraphNode candidate : node.getParents()) {
        if (candidate.getScoutBundle() != null && filter.accept(candidate.getScoutBundle())) {
          collector.add(candidate.getScoutBundle());
        }
      }
    }
    return collector.toArray(new ScoutBundle[collector.size()]);
  }

  public IScoutBundle[] getRequiredBundles(IScoutBundle bundle, IScoutBundleFilter filter, boolean includeThis) {
    List<ScoutBundle> collector = new ArrayList<ScoutBundle>();
    BundleGraphNode node = m_bundleGraph.getNode(bundle);
    if (node != null) {
      ArrayList<BundleGraphNode> next = new ArrayList<BundleGraphNode>();
      if (includeThis) {
        next.add(node);
      }
      else {
        next.addAll(node.getParents());
      }
      ArrayList<BundleGraphNode> current = new ArrayList<BundleGraphNode>();
      while (!next.isEmpty()) {
        current = next;
        next = new ArrayList<BundleGraphNode>();
        for (BundleGraphNode n : current) {
          next.addAll(n.getParents());
          if (n.getScoutBundle() != null && filter.accept(n.getScoutBundle())) {
            collector.add(n.getScoutBundle());
          }
        }
      }
    }
    return collector.toArray(new ScoutBundle[collector.size()]);
  }

  // workspace internal

  private void rebuildGraph() {
    ScoutWorkspaceEventList eventCollector = new ScoutWorkspaceEventList(this);
    rebuildGraphNoFire(eventCollector);
    fireWorkspaceEvnets(eventCollector.getAllEvents());
  }

  private void rebuildGraphNoFire(ScoutWorkspaceEventList eventCollector) {
    m_bundleGraph.buildTree();
    m_projectGraph.buildGraph(m_bundleGraph, eventCollector);
  }

  private boolean handleProjectAdded(IProject project) throws CoreException {
    try {
      if (project.isOpen() && project.hasNature(ScoutSdk.NATURE_ID) && project.hasNature("org.eclipse.pde.PluginNature")) {
        if (project.isOpen() && project.exists()) {
          ScoutWorkspaceEventList eventCollector = new ScoutWorkspaceEventList(this);
          boolean result = addProjectNoFire(project, eventCollector);
          if (result) {
            rebuildGraphNoFire(eventCollector);
            fireWorkspaceEvnets(eventCollector.getAllEvents());
          }
          return result;
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return false;

  }

  private boolean addProjectNoFire(IProject project, ScoutWorkspaceEventList eventCollector) throws CoreException {
    if (project.isOpen() && project.hasNature(ScoutSdk.NATURE_ID) && project.hasNature("org.eclipse.pde.PluginNature")) {
      if (project.isOpen() && project.exists()) {
        ScoutBundle bundle = m_bundleGraph.addWorkspaceProject(project, eventCollector);
        if (bundle != null) {
          return true;
        }
      }
    }
    return false;
  }

  private void handleProjectChanged(IProject project) throws CoreException {
    if (project.exists() && project.isOpen() && project.hasNature(ScoutSdk.NATURE_ID) && project.hasNature("org.eclipse.pde.PluginNature")) {
      ScoutBundle scoutBundle = m_bundleGraph.getScoutBundle(project);
      if (scoutBundle == null) {
        handleProjectAdded(project);
      }
      else if (scoutBundle.hasDependencyChanges()) {
        rebuildGraph();
      }
    }
  }

  private boolean handleProjectRemoved(String bundleId) {
    ScoutWorkspaceEventList eventCollector = new ScoutWorkspaceEventList(this);
    ScoutBundle bundle = m_bundleGraph.removeWorkspaceProject(bundleId, eventCollector);
    if (bundle != null) {
      rebuildGraphNoFire(eventCollector);
      fireWorkspaceEvnets(eventCollector.getAllEvents());
      return true;
    }
    return false;
  }

  /**
   *
   */
  private final class P_PluginModelListener implements IPluginModelListener {
    @Override
    public void modelsChanged(PluginModelDelta delta) {
      try {
        // process add events
        if ((delta.getKind() & PluginModelDelta.ADDED) != 0) {
          for (ModelEntry e : delta.getAddedEntries()) {
            IPluginModelBase modelBase = e.getModel();
            if (modelBase.getUnderlyingResource() != null) {
              handleProjectAdded(modelBase.getUnderlyingResource().getProject());
            }
          }
        }
        // process change events
        if ((delta.getKind() & PluginModelDelta.CHANGED) != 0) {
          for (ModelEntry e : delta.getChangedEntries()) {
            IPluginModelBase modelBase = e.getModel();
            if (modelBase.getUnderlyingResource() != null) {
              handleProjectChanged(modelBase.getUnderlyingResource().getProject());
            }
          }
        }
        // removed
        if ((delta.getKind() & PluginModelDelta.REMOVED) != 0) {
          for (ModelEntry e : delta.getRemovedEntries()) {
            handleProjectRemoved(e.getId());
          }
        }

      }
      catch (CoreException e) {
        ScoutSdk.logError("error updating scout workspace.", e);
      }
    }
  }
}
