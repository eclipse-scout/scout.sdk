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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.BundleGraph;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.BundleGraphNode;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.ProjectGraph;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.ProjectGraphNode;
import org.eclipse.scout.sdk.internal.workspace.bundlegraph.ScoutWorkspaceEventList;
import org.eclipse.scout.sdk.pde.listener.ResourceChangeListenerWithVisitor;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.IScoutWorkspace;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

/**
 *
 */
public class ScoutWorkspace implements IScoutWorkspace {
  private EventListenerList m_eventListeners = new EventListenerList();

  private BundleGraph m_bundleGraph;
  private ProjectGraph m_projectGraph;

  public ScoutWorkspace() {
    m_bundleGraph = new BundleGraph();
    m_projectGraph = new ProjectGraph();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(new P_ProjectDiscoveryListener());
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

  public void addWorkspaceListener(IScoutWorkspaceListener listener) {
    m_eventListeners.add(IScoutWorkspaceListener.class, listener);
  }

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
      l.worspaceChanged(e);
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
    if (project.isOpen() && project.hasNature(ScoutSdk.NATURE_ID) && project.hasNature("org.eclipse.pde.PluginNature")) {
      if (project.isOpen() && project.exists()) {
        ScoutBundle scoutBundle = m_bundleGraph.getScoutBundle(project);
        if (scoutBundle == null) {
          handleProjectAdded(project);
        }
        else if (scoutBundle.hasDependencyChanges()) {
          rebuildGraph();
        }
      }
    }
  }

  private boolean handleProjectRemoved(IProject project) {
    ScoutWorkspaceEventList eventCollector = new ScoutWorkspaceEventList(this);
    ScoutBundle bundle = m_bundleGraph.removeWorkspaceProject(project, eventCollector);
    if (bundle != null) {
      rebuildGraphNoFire(eventCollector);
      fireWorkspaceEvnets(eventCollector.getAllEvents());
      return true;
    }
    return false;
  }

  private class P_ProjectDiscoveryListener extends ResourceChangeListenerWithVisitor {
    @Override
    public void beforeChange(IResource r, int type) {
      try {
        if (r instanceof IProject) {
          final IProject p = (IProject) r;
          switch (type) {
            case IResourceChangeEvent.PRE_CLOSE:
            case IResourceChangeEvent.PRE_DELETE:
              // do not queue later, project is then gone
              handleProjectRemoved(p);
              break;

          }
        }
      }
      catch (Exception e) {
        ScoutSdk.logWarning("could not process resource change", e);
      }
    }

    @Override
    public void afterChange(IResource r, int type) {
    }

    @Override
    public boolean visitDelta(IResourceDelta delta) {
      // System.out.println(delta.getResource().getName()+" "+delta.getAffectedChildren().length);
      try {
        if (delta.getResource().getType() == IResource.PROJECT) {
          final IProject p = (IProject) delta.getResource();
          switch (delta.getKind()) {
            case IResourceDelta.ADDED:
            case IResourceDelta.OPEN:
              handleProjectAdded(p);
              break;
            case IResourceDelta.CHANGED:
              final BooleanHolder holder = new BooleanHolder(false);
              delta.accept(new IResourceDeltaVisitor() {
                @Override
                public boolean visit(IResourceDelta innerDelta) throws CoreException {
                  if (innerDelta.getResource().getName().equals("MANIFEST.MF")) {
                    holder.setValue(true);
                    return false;
                  }
                  return true;
                }
              });
              if (holder.getValue()) {
                handleProjectChanged(p);
              }
              break;
          }
        }
      }
      catch (Exception e) {
        ScoutSdk.logWarning("could not process resource change", e);
      }
      return true;
    }

  } // end class P_ProjectDiscoveryListener

}
