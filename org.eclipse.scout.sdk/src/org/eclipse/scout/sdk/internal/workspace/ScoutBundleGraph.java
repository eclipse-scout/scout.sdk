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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.bundles.RuntimeBundles;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleComparator;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

/**
 * Scout Bundle graph implementation
 */
@SuppressWarnings("restriction")
public class ScoutBundleGraph implements IScoutBundleGraph {
  private final Set<String> m_dependencyIssues;
  private final ReentrantReadWriteLock m_lock;

  private volatile Map<String /*symbolic name*/, ScoutBundle> m_bundleGraph;
  private volatile Map<IPath, IPluginModelBase> m_targetPlatformBundles;

  ScoutBundleGraph() {
    m_dependencyIssues = new HashSet<String>();
    m_lock = new ReentrantReadWriteLock(true);
  }

  /**
   * clears the bundle graph and removes all elements.
   */
  void dispose() {
    try {
      m_lock.writeLock().lock();
      if (m_bundleGraph != null) {
        m_bundleGraph.clear();
        m_bundleGraph = null;
      }
      m_targetPlatformBundles = null;
      m_dependencyIssues.clear();
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  /**
   * builds the scout bundle graph. The graph contains the new bundles after this method has finished successfully.<br>
   * This method is thread safe.
   *
   * @param eventCollector
   *          the collector that contains all change events that makes up the delta between the old and the new graph.
   * @param m
   *          the monitor for progress indication and cancellation.
   * @return true if the build was successful, false if the build has been cancelled.
   */
  boolean build(ScoutWorkspaceEventList eventCollector, IProgressMonitor m) {
    try {
      m_lock.writeLock().lock();

      IProgressMonitor monitor = m;
      if (m_bundleGraph == null) {
        // the very first bundle graph creation should not be cancelled.
        // create a progress monitor that answers accordingly.
        monitor = new ProgressMonitorWrapper(m) {
          @Override
          public boolean isCanceled() {
            return false;
          }
        };
      }

      Set<String> issueCollector = new HashSet<String>();
      Map<String, ScoutBundle> newGraph = getAllScoutBundles(issueCollector, monitor);
      if (monitor.isCanceled()) {
        return false;
      }

      for (ScoutBundle b : newGraph.values()) {
        for (IPluginModelBase dependency : b.getAllDependencies()) {
          if (monitor.isCanceled()) {
            return false;
          }
          ScoutBundle parent = newGraph.get(dependency.getBundleDescription().getSymbolicName());
          if (parent != null && !b.containsBundleRec(parent)) { // do not create circles
            parent.addChildProject(b);
          }
        }
      }
      if (monitor.isCanceled()) {
        return false;
      }

      for (ScoutBundle p : newGraph.values()) {
        p.removeImplicitChildren();
      }
      if (monitor.isCanceled()) {
        return false;
      }

      m_targetPlatformBundles = getTargetPlatformBundles();

      Map<String, ScoutBundle> oldGraph = m_bundleGraph;
      m_bundleGraph = newGraph;

      m_dependencyIssues.clear();
      m_dependencyIssues.addAll(issueCollector);

      if (eventCollector != null) {
        collectDeltas(eventCollector, m_bundleGraph, oldGraph);
      }
      monitor.done();
      return true;
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  String getContributingBundleSymbolicName(IJavaElement element) {
    if (element.getResource() == null) {
      // external
      IPluginModelBase externalPlugin = m_targetPlatformBundles.get(element.getPath());
      if (externalPlugin == null) {
        return null; // java elements not contributed by a bundle (e.g. from the java RT or another jar).
      }
      return externalPlugin.getBundleDescription().getSymbolicName();
    }
    else {
      // the element is in the workspace. there exists a java project.
      return element.getJavaProject().getElementName();
    }
  }

  /**
   * gets all dependency issues (e.g. cycles) found in the last graph build.<br>
   * This method is not thread safe and only public for testing purposes.
   *
   * @return the messages for all issues.
   */
  public String[] getDependencyIssues() {
    return m_dependencyIssues.toArray(new String[m_dependencyIssues.size()]);
  }

  @Override
  public Set<IScoutBundle> getBundles(IScoutBundleFilter filter) {
    return getBundles(filter, null);
  }

  @Override
  public Set<IScoutBundle> getBundles(IScoutBundleFilter filter, IScoutBundleComparator comparator) {
    ensureGraphCreated();

    try {
      m_lock.readLock().lock();

      Set<IScoutBundle> ret = null;
      if (comparator == null) {
        ret = new HashSet<IScoutBundle>(m_bundleGraph.size());
      }
      else {
        ret = new TreeSet<IScoutBundle>(comparator);
      }

      if (filter == null) {
        ret.addAll(m_bundleGraph.values());
      }
      else {
        for (ScoutBundle bundle : m_bundleGraph.values()) {
          if (filter.accept(bundle)) {
            ret.add(bundle);
          }
        }
      }
      return ret;
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Override
  public IScoutBundle getBundle(IJavaElement je) {
    if (!TypeUtility.exists(je)) {
      return null;
    }

    ensureGraphCreated();

    try {
      m_lock.readLock().lock();
      return getBundleNoLock(getContributingBundleSymbolicName(je));
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Override
  public IScoutBundle getBundle(IProject p) {
    if (p == null) {
      return null;
    }
    return getBundle(p.getName());
  }

  @Override
  public IScoutBundle getBundle(String symbolicName) {
    ensureGraphCreated();

    try {
      m_lock.readLock().lock();
      return getBundleNoLock(symbolicName);
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Override
  public void waitFor() {
    do {
      JdtUtility.waitForJobFamily(ScoutWorkspace.BUNDLE_GRAPH_REBUILD_JOB_FAMILY);
    }
    while (ScoutWorkspace.getInstance().getNumBundleGraphRebuildJobs() > 0);
  }

  private void ensureGraphCreated() {
    if (m_bundleGraph == null) {
      // When first accessing the bundle graph (using the scout workspace) it is initialized asynchronously.
      // The build of the graph is executed in an own job which may lead to a not initialized graph when first using it (when the job has not completed yet).
      // This method blocks until the job has finished and the graph is initialized to ensure clients get a result when accessing the graph.
      // Because the first calculation of the bundle graph cannot be cancelled there is no need to wait for subsequent builds (no need to call waitFor()).
      JdtUtility.waitForJobFamily(ScoutWorkspace.BUNDLE_GRAPH_REBUILD_JOB_FAMILY);
    }
  }

  private IScoutBundle getBundleNoLock(String symbolicName) {
    return m_bundleGraph.get(symbolicName);
  }

  private static Map<IPath, IPluginModelBase> getTargetPlatformBundles() {
    IPluginModelBase[] models = PDECore.getDefault().getModelManager().getExternalModels();
    Map<IPath, IPluginModelBase> result = new HashMap<IPath, IPluginModelBase>(models.length);
    for (IPluginModelBase p : models) {
      result.put(new Path(p.getInstallLocation()), p);
    }
    return result;
  }

  @SuppressWarnings("unused")
  private static void printAsTree(Map<String, ScoutBundle> graph) {
    for (ScoutBundle p : graph.values()) {
      if (p.getDirectParentBundles().size() == 0) { // only root bundles
        p.print(System.out);
      }
    }

    System.out.println();
    System.out.println("multiple parents:");
    HashSet<ScoutBundle> multipleParents = new HashSet<ScoutBundle>();
    for (ScoutBundle b : graph.values()) {
      if (b.getDirectParentBundles().size() > 1) {
        multipleParents.add(b);
      }
    }
    for (ScoutBundle b : multipleParents) {
      System.out.println(b);
    }
  }

  /**
   * Gets all available bundles & fragments (workspace & target platform) that have scout RT bundles in its full
   * dependency tree.<br>
   * The scout RT bundles itself are not part of the list.<br>
   * When the dependencies of a host-bundle are enhanced by fragments, the dependencies contributed by these fragments
   * are NOT considered.
   *
   * @return the bundle set
   */
  private static Map<String, ScoutBundle> getAllScoutBundles(Set<String> dependencyCollector, IProgressMonitor monitor) {
    Map<String, ScoutBundle> allScoutBundles = new HashMap<String, ScoutBundle>();
    HashSet<String> messageCollector = new HashSet<String>();

    monitor.beginTask(Texts.get("WaitingForEclipsePDE") + "...", 1);
    PDECore pdeCore = PDECore.getDefault();
    if (pdeCore == null) {
      return allScoutBundles; // PDE is shutting down
    }
    PluginModelManager pmm = pdeCore.getModelManager();
    if (pmm == null) {
      return allScoutBundles; // PDE is shutting down
    }
    IPluginModelBase[] workspaceModels = pmm.getWorkspaceModels();
    monitor.worked(1);

    monitor.beginTask(Texts.get("CalculatingScoutBundleGraph"), workspaceModels.length);
    for (IPluginModelBase p : workspaceModels) {
      if (p != null && p.getBundleDescription() != null) {
        monitor.subTask(Texts.get("CalculatingGraphForBundle", p.getBundleDescription().getSymbolicName()));
      }
      collectScoutBundlesRec(allScoutBundles, p, messageCollector, monitor);
      if (monitor.isCanceled()) {
        return null;
      }
      monitor.worked(1);
    }
    dependencyCollector.addAll(messageCollector);
    return allScoutBundles;
  }

  /**
   * Recursively iterates over the dependency tree to find all bundles.<br>
   * The dependency tree is canceled when a Scout RT dependency is found.
   */
  private static void collectScoutBundlesRec(Map<String, ScoutBundle> collector, IPluginModelBase bundle, Set<String> messageCollector, IProgressMonitor monitor) {
    if (bundle != null && bundle.getBundleDescription() != null && !RuntimeBundles.contains(bundle.getBundleDescription())) {
      ScoutBundle b = new ScoutBundle(bundle, monitor);
      if (monitor.isCanceled()) {
        return;
      }
      messageCollector.addAll(b.getDependencyIssues());
      if (b.getType() != null) {
        collector.put(b.getSymbolicName(), b);
        for (IPluginModelBase dependency : b.getAllDependencies()) {
          if (monitor.isCanceled()) {
            return;
          }
          collectScoutBundlesRec(collector, dependency, messageCollector, monitor);
        }
      }
    }
  }

  private static void collectDeltas(ScoutWorkspaceEventList eventCollector, Map<String, ScoutBundle> newGraph, Map<String, ScoutBundle> oldGraph) {
    for (Entry<String, ScoutBundle> entry : newGraph.entrySet()) {
      ScoutBundle existing = null;
      if (oldGraph != null) {
        existing = oldGraph.get(entry.getKey());
      }
      if (existing == null) {
        // bundle is in the new but not in the old graph -> added
        eventCollector.addEvent(ScoutWorkspaceEvent.TYPE_BUNDLE_ADDED, entry.getValue());
      }
      else if (CompareUtility.notEquals(existing, entry.getValue()) || !existing.getDirectParentBundles().equals(entry.getValue().getDirectParentBundles())) {
        // bundle name is in the old and the new graph AND they are different -> changed
        eventCollector.addEvent(ScoutWorkspaceEvent.TYPE_BUNDLE_CHANGED, entry.getValue());
      }
    }
    if (oldGraph != null) {
      for (Entry<String, ScoutBundle> entry : oldGraph.entrySet()) {
        if (!newGraph.containsKey(entry.getKey())) {
          // bundle is in the old but not in the new graph -> removed
          eventCollector.addEvent(ScoutWorkspaceEvent.TYPE_BUNDLE_REMOVED, entry.getValue());
        }
      }
    }
  }
}
