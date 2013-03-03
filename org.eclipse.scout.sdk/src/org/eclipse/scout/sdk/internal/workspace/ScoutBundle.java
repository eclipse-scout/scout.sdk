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

import java.io.PrintStream;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.bundles.RuntimeBundles;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleComparator;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.IScoutWorkspaceListener;
import org.eclipse.scout.sdk.workspace.ScoutBundleComparators;
import org.eclipse.scout.sdk.workspace.ScoutWorkspaceEvent;

/**
 * <h3>{@link ScoutBundle}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 30.01.2013
 */
public class ScoutBundle implements IScoutBundle {

  private final static Pattern REGEX_LEADING_DOTS = Pattern.compile("^\\.*");

  private final Set<IPluginModelBase> m_allDependencies;
  private final Set<IPluginModelBase> m_directDependencies;
  private final Set<ScoutBundle> m_parentBundles;
  private final Set<ScoutBundle> m_childBundles;
  private final Set<String> m_dependencyIssues;
  private final IScoutBundleComparator m_defaultComparator;
  private final String m_type;
  private final IJavaProject m_javaProject;
  private final IProject m_project;
  private final IPluginModelBase m_pluginModelBase;
  private final String m_symbolicName;
  private final boolean m_isFragment;
  private final boolean m_isBinary;
  private final String m_id;
  private final int m_hash;

  private IEclipsePreferences m_projectPreferences;
  private Holder<INlsProject> m_nlsProjectHolder;
  private Holder<INlsProject> m_docsNlsProjectHolder;
  private IIconProvider m_iconProvider;
  private IPackageFragmentRoot m_rootPackage;
  private ITypeHierarchyChangedListener m_textProvidersChangedListener;
  private IScoutWorkspaceListener m_bundleRemovedListener;

  public ScoutBundle(IPluginModelBase bundle) {
    m_pluginModelBase = bundle;
    m_parentBundles = new HashSet<ScoutBundle>();
    m_childBundles = new HashSet<ScoutBundle>();
    m_dependencyIssues = new HashSet<String>();
    m_allDependencies = getAllDependenciesImpl(bundle);
    m_directDependencies = getDirectDependenciesImpl(bundle);
    m_type = RuntimeBundles.getBundleType(this);
    m_javaProject = getJavaProject(bundle);
    m_isBinary = getJavaProject() == null;
    m_project = isBinary() ? null : getJavaProject().getProject();
    m_symbolicName = bundle.getBundleDescription().getSymbolicName();
    m_defaultComparator = ScoutBundleComparators.getSymbolicNameLevenshteinDistanceComparator(m_symbolicName);
    m_isFragment = bundle.getBundleDescription().getHost() != null;
    m_id = "{" + m_symbolicName + "@type=" + m_type + "@fragment=" + m_isFragment + "@binary=" + m_isBinary + "}";
    m_hash = m_id.hashCode();

    m_nlsProjectHolder = null;
    m_docsNlsProjectHolder = null;
    m_projectPreferences = null;
    m_iconProvider = null;
    m_textProvidersChangedListener = null;
    m_bundleRemovedListener = null;
    m_rootPackage = null;
  }

  private IJavaProject getJavaProject(IPluginModelBase bundle) {
    if (bundle.getUnderlyingResource() != null) {
      IProject project = bundle.getUnderlyingResource().getProject();
      if (project != null) {
        IJavaProject jp = JavaCore.create(project);
        if (jp != null) {
          if (jp.exists() && !jp.isReadOnly()) {
            return jp;
          }
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return m_id;
  }

  @Override
  public String getType() {
    return m_type;
  }

  @Override
  public String getSymbolicName() {
    return m_symbolicName;
  }

  @Override
  public int hashCode() {
    return m_hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return toString().equals(obj.toString());
  }

  @Override
  public Set<? extends IScoutBundle> getDirectParentBundles() {
    return m_parentBundles;
  }

  @Override
  public Set<ScoutBundle> getDirectChildBundles() {
    return m_childBundles;
  }

  @Override
  public IScoutBundle[] getChildBundles(IScoutBundleFilter filter, boolean includeThis) {
    P_BundleCollector bundleCollector = new P_BundleCollector(filter);
    visit(bundleCollector, includeThis, false);
    return bundleCollector.getElements();
  }

  @Override
  public IScoutBundle getChildBundle(IScoutBundleFilter filter, boolean includeThis) {
    return getChildBundle(filter, m_defaultComparator, includeThis);
  }

  @Override
  public IScoutBundle getChildBundle(IScoutBundleFilter filter, IScoutBundle reference, boolean includeThis) {
    IScoutBundleComparator c = ScoutBundleComparators.getSymbolicNameLevenshteinDistanceComparator(reference.getSymbolicName());
    return getChildBundle(filter, c, includeThis);
  }

  @Override
  public IScoutBundle getChildBundle(IScoutBundleFilter filter, IScoutBundleComparator comparator, boolean includeThis) {
    P_SingleBundleByLevelCollector bundleCollector = new P_SingleBundleByLevelCollector(filter, comparator);
    visit(bundleCollector, includeThis, false);
    return bundleCollector.getElement();
  }

  @Override
  public IScoutBundle[] getParentBundles(IScoutBundleFilter filter, boolean includeThis) {
    P_BundleCollector bundleCollector = new P_BundleCollector(filter);
    visit(bundleCollector, includeThis, true);
    return bundleCollector.getElements();
  }

  @Override
  public IScoutBundle getParentBundle(IScoutBundleFilter filter, boolean includeThis) {
    return getParentBundle(filter, m_defaultComparator, includeThis);
  }

  @Override
  public IScoutBundle getParentBundle(IScoutBundleFilter filter, IScoutBundle reference, boolean includeThis) {
    IScoutBundleComparator c = ScoutBundleComparators.getSymbolicNameLevenshteinDistanceComparator(reference.getSymbolicName());
    return getParentBundle(filter, c, includeThis);
  }

  @Override
  public IScoutBundle getParentBundle(IScoutBundleFilter filter, IScoutBundleComparator comparator, boolean includeThis) {
    P_SingleBundleByLevelCollector bundleCollector = new P_SingleBundleByLevelCollector(filter, comparator);
    visit(bundleCollector, includeThis, true);
    return bundleCollector.getElement();
  }

  @Override
  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  @Override
  public IProject getProject() {
    return m_project;
  }

  @Override
  public synchronized IEclipsePreferences getPreferences() {
    if (m_projectPreferences == null) {
      if (getProject() != null) {
        IScopeContext prefScope = new ProjectScope(getProject());
        m_projectPreferences = prefScope.getNode(ScoutSdk.getDefault().getBundle().getSymbolicName());
      }
    }
    return m_projectPreferences;
  }

  @Override
  public boolean contains(IJavaElement e) {
    if (!TypeUtility.exists(e)) return false;
    String contributingBundle = ScoutWorkspace.getInstance().getBundleGraphInternal().getContributingBundleSymbolicName(e);
    return m_symbolicName.equals(contributingBundle);
  }

  @Override
  public INlsProject getNlsProject() {
    if (m_nlsProjectHolder == null) {
      synchronized (this) {
        if (m_nlsProjectHolder == null) {
          try {
            m_nlsProjectHolder = new Holder<INlsProject>(INlsProject.class, null);
            INlsProject nlsProject = NlsCore.getNlsWorkspace().getNlsProject(new Object[]{TypeUtility.getType(RuntimeClasses.TEXTS), this});
            m_nlsProjectHolder.setValue(nlsProject);
          }
          catch (CoreException e) {
            ScoutSdk.logError("error loading NLS project for: '" + getSymbolicName() + "'.", e);
          }
        }
      }
    }
    return m_nlsProjectHolder.getValue();
  }

  @Override
  public INlsProject getDocsNlsProject() {
    if (m_docsNlsProjectHolder == null) {
      synchronized (this) {
        if (m_docsNlsProjectHolder == null) {
          try {
            m_docsNlsProjectHolder = new Holder<INlsProject>(INlsProject.class, null);
            INlsProject nlsProject = NlsCore.getNlsWorkspace().getNlsProject(new Object[]{TypeUtility.getType(RuntimeClasses.IDocumentationTextProviderService), this});
            m_docsNlsProjectHolder.setValue(nlsProject);
          }
          catch (CoreException e) {
            ScoutSdk.logError("error loading NLS project for: '" + getSymbolicName() + "'.", e);
          }
        }
      }
    }
    return m_docsNlsProjectHolder.getValue();
  }

  private void registerTextProviderServiceHierarchyListener() {
    if (m_textProvidersChangedListener == null) {
      IPrimaryTypeTypeHierarchy pth = TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(RuntimeClasses.AbstractDynamicNlsTextProviderService));
      m_textProvidersChangedListener = new P_TextProviderServiceHierarchyChangedListener(this);
      pth.addHierarchyListener(m_textProvidersChangedListener);
      m_bundleRemovedListener = new P_BundleRemovedWorkspaceListener(this);
      ScoutSdkCore.getScoutWorkspace().addWorkspaceListener(m_bundleRemovedListener);
    }
  }

  @Override
  public String getPackageName(String appendix) {
    if (appendix == null) {
      return getSymbolicName();
    }

    appendix = REGEX_LEADING_DOTS.matcher(appendix).replaceAll("").trim();
    if (appendix.length() > 0) {
      appendix = "." + appendix;
    }
    return getSymbolicName() + appendix;
  }

  @Override
  public IPackageFragment getPackageFragment(String packageFqn) throws JavaModelException {
    if (m_rootPackage == null) {
      synchronized (this) {
        if (m_rootPackage == null) {
          Path src = new Path(IPath.SEPARATOR + getSymbolicName() + IPath.SEPARATOR + SdkProperties.DEFAULT_SOURCE_FOLDER_NAME);
          m_rootPackage = getJavaProject().findPackageFragmentRoot(src);
        }
      }
    }
    return m_rootPackage.getPackageFragment(packageFqn);
  }

  @Override
  public String getDefaultPackage(String packageId) {
    String pck = DefaultTargetPackage.get(this, packageId);
    if (pck == null) {
      throw new IllegalArgumentException("invalid package id");
    }
    return getPackageName(pck);
  }

  @Override
  public IIconProvider getIconProvider() {
    if (m_iconProvider == null) {
      synchronized (this) {
        if (m_iconProvider == null) {
          m_iconProvider = new ScoutProjectIcons(this);
        }
      }
    }
    return m_iconProvider;
  }

  @Override
  public boolean isBinary() {
    return m_isBinary;
  }

  public IPluginModelBase getPluginModelBase() {
    return m_pluginModelBase;
  }

  private void visit(IBundleVisitor visitor, boolean includeThis, boolean up) {
    if (includeThis) {
      breadthFirstTraverse(visitor, up, this);
    }
    else {
      breadthFirstTraverse(visitor, up, up ? m_parentBundles : m_childBundles);
    }
  }

  private void breadthFirstTraverse(IBundleVisitor visitor, boolean up, Set<ScoutBundle> startBundles) {
    Deque<P_TraverseComposite> deck = new LinkedList<P_TraverseComposite>();
    for (ScoutBundle start : startBundles) {
      deck.addLast(new P_TraverseComposite(start, 0));
    }
    breadthFirstTraverse(visitor, up, deck);
  }

  private void breadthFirstTraverse(IBundleVisitor visitor, boolean up, ScoutBundle b) {
    Deque<P_TraverseComposite> deck = new LinkedList<P_TraverseComposite>();
    deck.addLast(new P_TraverseComposite(b, 0));
    breadthFirstTraverse(visitor, up, deck);
  }

  /**
   * level order traversal
   * 
   * @param start
   * @param visitor
   * @param up
   */
  private static void breadthFirstTraverse(IBundleVisitor visitor, boolean up, Deque<P_TraverseComposite> deck) {
    while (!deck.isEmpty()) {
      P_TraverseComposite el = deck.removeFirst();
      Set<ScoutBundle> nextLevelBundles;
      if (up) {
        nextLevelBundles = el.m_bundle.m_parentBundles;
      }
      else {
        nextLevelBundles = el.m_bundle.m_childBundles;
      }

      int nextLevel = el.m_level + 1;
      for (ScoutBundle child : nextLevelBundles) {
        deck.addLast(new P_TraverseComposite(child, nextLevel));
      }

      if (!visitor.visit(el.m_bundle, el.m_level)) {
        return;
      }
    }
  }

  void print(PrintStream out) {
    print(out, this, "");
  }

  void addChildProject(ScoutBundle child) {
    m_childBundles.add(child);
    child.m_parentBundles.add(this);
  }

  public void removeChildProject(ScoutBundle child) {
    m_childBundles.remove(child);
    child.m_parentBundles.remove(this);
  }

  public Set<IPluginModelBase> getAllDependencies() {
    return m_allDependencies;
  }

  public Set<IPluginModelBase> getDirectDependencies() {
    return m_directDependencies;
  }

  void removeImplicitChildren() {
    Iterator<ScoutBundle> bundleIt = getDirectChildBundles().iterator();
    while (bundleIt.hasNext()) {
      ScoutBundle bundle = bundleIt.next();
      bundle.removeImplicitChildren();

      ScoutBundle[] otherChildren = getDirectChildBundles().toArray(new ScoutBundle[getDirectChildBundles().size()]);
      for (ScoutBundle otherChild : otherChildren) {
        if (otherChild != bundle && otherChild.containsBundleRec(bundle)) {
          bundleIt.remove(); // remove bundle from my children
          bundle.m_parentBundles.remove(this); //remove me from bundle's parents
          break;
        }
      }
    }
  }

  boolean containsBundleRec(ScoutBundle search) {
    for (ScoutBundle b : getDirectChildBundles()) {
      if (b == search) {
        return true;
      }
      if (b.containsBundleRec(search)) {
        return true;
      }
    }
    return false;
  }

  Set<String> getDependencyIssues() {
    return m_dependencyIssues;
  }

  private Set<IPluginModelBase> getAllDependenciesImpl(IPluginModelBase bundle) {
    Set<IPluginModelBase> collector = new HashSet<IPluginModelBase>();
    Stack<IPluginModelBase> dependencyStack = new Stack<IPluginModelBase>();
    Set<String> messageCollector = new HashSet<String>();
    collectDependencies(bundle, collector, dependencyStack, messageCollector, true);
    getDependencyIssues().addAll(messageCollector);
    return collector;
  }

  private static Set<IPluginModelBase> getDirectDependenciesImpl(IPluginModelBase bundle) {
    Set<IPluginModelBase> bd = new HashSet<IPluginModelBase>();
    Stack<IPluginModelBase> dependencyStack = new Stack<IPluginModelBase>();
    collectDependencies(bundle, bd, dependencyStack, null, false);
    return bd;
  }

  private static void collectDependencies(IPluginModelBase bundle, Set<IPluginModelBase> collector, Stack<IPluginModelBase> dependencyStack, Set<String> messageCollector, boolean rec) {
    if (bundle != null && bundle.getBundleDescription() != null) {
      for (BundleSpecification dependency : bundle.getBundleDescription().getRequiredBundles()) {
        if (!bundle.getBundleDescription().getSymbolicName().equals(dependency.getName())) { // ignore dependencies on the bundle itself
          IPluginModelBase model = PluginRegistry.findModel(dependency.getName());
          if (model != null) {
            addDependency(model, collector, dependencyStack, messageCollector, rec);
          }
        }
      }

      if (bundle.getBundleDescription().getHost() != null) {
        for (BundleDescription host : bundle.getBundleDescription().getHost().getHosts()) {
          // it is a fragment: the dependencies of the host are also present.
          IPluginModelBase model = PluginRegistry.findModel(host.getSymbolicName());
          if (model != null) {
            addDependency(model, collector, dependencyStack, messageCollector, rec);
          }
        }
      }
    }
  }

  private static void addDependency(IPluginModelBase bundle, Set<IPluginModelBase> collector, Stack<IPluginModelBase> dependencyStack, Set<String> messageCollector, boolean rec) {
    // dependency loop detection & prevention
    if (dependencyStack.contains(bundle)) {
      // a dependency loop was detected: log the loop and stop processing of this part of the dependency graph
      StringBuilder loopMsg = new StringBuilder(Texts.get("DependencyLoopDetected"));
      loopMsg.append(":\n");
      boolean loopBeginFound = false;
      for (IPluginModelBase s : dependencyStack) {
        if (!loopBeginFound && s.equals(bundle)) {
          loopBeginFound = true;
        }
        if (loopBeginFound) {
          loopMsg.append(s.getBundleDescription().getSymbolicName());
          loopMsg.append('\n');
          collector.remove(s); // correction: remove all dependencies that build up the cycle
        }
      }
      loopMsg.append(bundle.getBundleDescription().getSymbolicName());
      messageCollector.add(loopMsg.toString());
      return;
    }

    collector.add(bundle);

    try {
      dependencyStack.push(bundle);
      if (rec && !RuntimeBundles.containsTypeDefiningBundle(bundle.getBundleDescription())) {
        collectDependencies(bundle, collector, dependencyStack, messageCollector, rec);
      }
    }
    finally {
      dependencyStack.pop();
    }
  }

  private static void print(PrintStream out, ScoutBundle b, String prefix) {
    out.println(prefix + b);
    for (ScoutBundle child : b.getDirectChildBundles()) {
      print(out, child, prefix + "  | ");
    }
  }

  private interface IBundleVisitor {
    boolean visit(IScoutBundle bundle, int traversalLevel);
  }

  private static class P_BundleCollector implements IBundleVisitor {
    private final LinkedHashSet<IScoutBundle> m_collector;
    private final IScoutBundleFilter m_filter;

    private P_BundleCollector(IScoutBundleFilter filter) {
      m_collector = new LinkedHashSet<IScoutBundle>();
      m_filter = filter;
    }

    @Override
    public boolean visit(IScoutBundle bundle, int traversalLevel) {
      if (m_filter == null || m_filter.accept(bundle)) {
        m_collector.add(bundle);
      }
      return true;
    }

    public IScoutBundle[] getElements() {
      return m_collector.toArray(new IScoutBundle[m_collector.size()]);
    }
  }

  private static class P_SingleBundleByLevelCollector implements IBundleVisitor {
    private final IScoutBundleFilter m_filter;
    private final TreeSet<IScoutBundle> m_collector;
    private int m_lastLevel;

    private P_SingleBundleByLevelCollector(IScoutBundleFilter filter, IScoutBundleComparator comparator) {
      m_filter = filter;
      if (comparator == null) {
        // if no comparator is given, the client does not care about which bundle inside a traversal level is chosen.
        // but to ensure there is no random behavior we assign one
        comparator = ScoutBundleComparators.getSymbolicNameAscComparator();
      }
      m_collector = new TreeSet<IScoutBundle>(comparator);
      m_lastLevel = 0; // first traversal level is 0
    }

    @Override
    public boolean visit(IScoutBundle bundle, int traversalLevel) {
      if (traversalLevel > m_lastLevel) {
        // the next level is coming: check if we have a candidate
        IScoutBundle candidate = getElement();
        if (candidate != null) {
          // we have found an item: cancel the traversal
          return false;
        }
        m_lastLevel = traversalLevel;
      }

      if (m_filter == null || m_filter.accept(bundle)) {
        m_collector.add(bundle);
      }

      return true; // continue
    }

    public IScoutBundle getElement() {
      if (m_collector.isEmpty()) {
        return null;
      }
      return m_collector.first();
    }
  }

  private static class P_TextProviderServiceHierarchyChangedListener implements ITypeHierarchyChangedListener {
    private ScoutBundle m_observer;

    private P_TextProviderServiceHierarchyChangedListener(ScoutBundle observer) {
      m_observer = observer;
    }

    @Override
    public void handleEvent(int eventType, IType type) {
      synchronized (m_observer) {
        m_observer.m_nlsProjectHolder = null;
        m_observer.m_docsNlsProjectHolder = null;
      }
    }
  }

  private static class P_TraverseComposite {
    private final int m_level;
    private final ScoutBundle m_bundle;

    private P_TraverseComposite(ScoutBundle bundle, int level) {
      m_level = level;
      m_bundle = bundle;
    }
  }

  private static class P_BundleRemovedWorkspaceListener implements IScoutWorkspaceListener {
    private ScoutBundle m_observer;

    private P_BundleRemovedWorkspaceListener(ScoutBundle observer) {
      m_observer = observer;
    }

    @Override
    public void workspaceChanged(ScoutWorkspaceEvent event) {
      if (event.getScoutElement() == m_observer && event.getType() == ScoutWorkspaceEvent.TYPE_BUNDLE_REMOVED) {
        synchronized (m_observer) {
          m_observer.m_iconProvider = null;
          IPrimaryTypeTypeHierarchy pth = TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(RuntimeClasses.AbstractDynamicNlsTextProviderService));
          pth.removeHierarchyListener(m_observer.m_textProvidersChangedListener);
          m_observer.m_textProvidersChangedListener = null;
          ScoutSdkCore.getScoutWorkspace().removeWorkspaceListener(this);
          m_observer.m_bundleRemovedListener = null;
        }
      }
    }
  }
}
