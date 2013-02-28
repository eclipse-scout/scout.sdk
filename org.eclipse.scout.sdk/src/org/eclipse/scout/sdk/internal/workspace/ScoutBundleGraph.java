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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.extensions.runtime.bundles.RuntimeBundles;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleComparator;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

/**
 * Scout Bundle graph implementation
 */
public class ScoutBundleGraph implements IScoutBundleGraph {
  private final Set<String> m_dependencyIssues;
  private final ReentrantReadWriteLock m_lock;

  private Map<String /*symbolic name*/, ScoutBundle> m_bundleGraph;

  public ScoutBundleGraph() {
    m_dependencyIssues = new HashSet<String>();
    m_lock = new ReentrantReadWriteLock(true);
  }

  public void build() {
    build(null);
  }

  public void build(ScoutWorkspaceEventList eventCollector) {
    final Map<String, ScoutBundle> oldGraph = m_bundleGraph;
    try {
      m_lock.writeLock().lock();

      Map<String, ScoutBundle> newGraph = getAllScoutBundles();
      for (ScoutBundle b : newGraph.values()) {
        for (IPluginModelBase dependency : b.getAllDependencies()) {
          ScoutBundle parent = newGraph.get(dependency.getBundleDescription().getSymbolicName());
          if (parent != null && !b.containsBundleRec(parent)) { // do not create circles
            parent.addChildProject(b);
          }
        }
      }

      for (ScoutBundle p : newGraph.values()) {
        p.removeImplicitChildren();
      }
      // printAsTree(scoutBundles);

      m_bundleGraph = newGraph;

      if (eventCollector != null) {
        collectDeltas(eventCollector, m_bundleGraph, oldGraph);
      }
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  public String[] getDependencyIssues() {
    return m_dependencyIssues.toArray(new String[m_dependencyIssues.size()]);
  }

  @Override
  public IScoutBundle[] getBundles(IScoutBundleFilter filter) {
    return getBundles(filter, null);
  }

  @Override
  public IScoutBundle[] getBundles(IScoutBundleFilter filter, IScoutBundleComparator comparator) {
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
      return ret.toArray(new IScoutBundle[ret.size()]);
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Override
  public IScoutBundle getBundle(IJavaElement je) {
    if (je instanceof IJavaProject) {
      return getBundle(((IJavaProject) je).getElementName());
    }
    if (je == null) {
      return null;
    }
    return getBundle(je.getJavaProject().getElementName());
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
    try {
      m_lock.readLock().lock();
      return m_bundleGraph.get(symbolicName);
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

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
  private Map<String, ScoutBundle> getAllScoutBundles() {
    Map<String, ScoutBundle> allScoutBundles = new HashMap<String, ScoutBundle>();
    HashSet<String> messageCollector = new HashSet<String>();
    for (IPluginModelBase p : PluginRegistry.getWorkspaceModels()) {
      collectScoutBundlesRec(allScoutBundles, p, messageCollector);
    }
    m_dependencyIssues.clear();
    m_dependencyIssues.addAll(messageCollector);
    return allScoutBundles;
  }

  /**
   * Recursively iterates over the dependency tree to find all bundles.<br>
   * The dependency tree is canceled when a Scout RT dependency is found.
   */
  private static void collectScoutBundlesRec(Map<String, ScoutBundle> collector, IPluginModelBase bundle, Set<String> messageCollector) {
    if (bundle != null && bundle.getBundleDescription() != null && !RuntimeBundles.contains(bundle.getBundleDescription())) {
      ScoutBundle b = new ScoutBundle(bundle);
      messageCollector.addAll(b.getDependencyIssues());
      if (b.getType() != null) {
        collector.put(b.getSymbolicName(), b);
        for (IPluginModelBase dependency : b.getAllDependencies()) {
          collectScoutBundlesRec(collector, dependency, messageCollector);
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

  /**
   * checks if 'value' is one of the items in 'list'. to check equality the equals method is used.
   */
  private static <T> boolean contains(T[] list, T value) {
    if (list != null) {
      for (T t : list) {
        if (CompareUtility.equals(t, value)) {
          return true;
        }
      }
    }
    return false;
  }

}
