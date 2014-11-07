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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.bundles.RuntimeBundles;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleComparator;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraphVisitor;
import org.eclipse.scout.sdk.workspace.ScoutBundleComparators;

/**
 * <h3>{@link ScoutBundle}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 30.01.2013
 */
public class ScoutBundle implements IScoutBundle {

  private static final Pattern REGEX_LEADING_DOTS = Pattern.compile("^\\.*");

  private final Map<String, IPluginModelBase> m_allDependencies;
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

  private ITypeHierarchyChangedListener m_textProvidersChangedListener;
  private IEclipsePreferences m_projectPreferences;
  private volatile Holder<INlsProject> m_nlsProjectHolder;
  private volatile Holder<INlsProject> m_docsNlsProjectHolder;
  private volatile IIconProvider m_iconProvider;
  private volatile IPackageFragmentRoot m_rootPackage;

  public ScoutBundle(IPluginModelBase bundle, IProgressMonitor monitor) {
    m_pluginModelBase = bundle;
    m_parentBundles = new HashSet<ScoutBundle>();
    m_childBundles = new HashSet<ScoutBundle>();
    m_dependencyIssues = new HashSet<String>();
    m_allDependencies = getAllDependenciesImpl(bundle, monitor);
    m_type = RuntimeBundles.getBundleType(this);
    m_javaProject = getJavaProject(bundle);
    m_isBinary = m_javaProject == null;
    m_project = m_isBinary ? null : getJavaProject().getProject();
    m_symbolicName = bundle.getBundleDescription().getSymbolicName();
    m_defaultComparator = ScoutBundleComparators.getSymbolicNameLevenshteinDistanceComparator(m_symbolicName);
    m_isFragment = bundle.getBundleDescription().getHost() != null;
    m_id = "{" + m_symbolicName + "@type=" + m_type + "@fragment=" + m_isFragment + "@binary=" + m_isBinary + "}";
    m_hash = m_id.hashCode();

    m_nlsProjectHolder = null;
    m_docsNlsProjectHolder = null;
    m_projectPreferences = null;
    m_iconProvider = null;
    m_rootPackage = null;
  }

  /**
   * returns a valid java project that can be edited or null if no editable java project can be found.<br>
   * An editable java project is a java project that exists, is not read-only and has at least one writable package
   * fragment root.
   *
   * @param bundle
   * @return
   */
  private IJavaProject getJavaProject(IPluginModelBase bundle) {
    if (bundle.getUnderlyingResource() != null) {
      IProject project = bundle.getUnderlyingResource().getProject();
      if (project != null) {
        IJavaProject jp = JavaCore.create(project);
        if (jp != null && jp.exists() && !jp.isReadOnly()) {
          try {
            IPackageFragmentRoot[] packageFragmentRoots = jp.getPackageFragmentRoots();
            if (packageFragmentRoots != null) {
              for (IPackageFragmentRoot root : packageFragmentRoots) {
                if (root != null && !root.isArchive() && !root.isReadOnly() && !root.isExternal()) {
                  return jp;
                }
              }
            }
          }
          catch (JavaModelException e) {
            BundleDescription bundleDescription = bundle.getBundleDescription();
            if (bundleDescription != null) {
              ScoutSdk.logError("Unable to evaluate package fragment roots of bundle '" + bundleDescription.getSymbolicName() + "'. The bundle will be handled as binary.", e);
            }
            else {
              ScoutSdk.logError("Unable to evaluate package fragment roots. The bundle will be handled as binary.", e);
            }
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
  public boolean hasType(String type) {
    if (CompareUtility.equals(m_type, type)) {
      return true;
    }
    return RuntimeBundles.hasReferencedType(m_type, type);
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
    if (!(obj instanceof ScoutBundle)) {
      return false;
    }
    return toString().equals(obj.toString());
  }

  @Override
  public Set<? extends IScoutBundle> getDirectParentBundles() {
    return CollectionUtility.hashSet(m_parentBundles);
  }

  @Override
  public Set<ScoutBundle> getDirectChildBundles() {
    return CollectionUtility.hashSet(m_childBundles);
  }

  @Override
  public Set<? extends IScoutBundle> getChildBundles(IScoutBundleFilter filter, boolean includeThis) {
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
  public Set<? extends IScoutBundle> getParentBundles(IScoutBundleFilter filter, boolean includeThis) {
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
    if (m_projectPreferences == null && getProject() != null) {
      IScopeContext prefScope = new ProjectScope(getProject());
      m_projectPreferences = prefScope.getNode(ScoutSdk.getDefault().getBundle().getSymbolicName());
    }
    return m_projectPreferences;
  }

  @Override
  public boolean contains(IJavaElement e) {
    if (!TypeUtility.exists(e)) {
      return false;
    }
    String contributingBundle = ScoutWorkspace.getInstance().getBundleGraphInternal().getContributingBundleSymbolicName(e);
    return m_symbolicName.equals(contributingBundle);
  }

  @Override
  public INlsProject getNlsProject() {
    Holder<INlsProject> result = m_nlsProjectHolder;
    if (result == null) {
      synchronized (this) {
        result = m_nlsProjectHolder;
        if (result == null) {
          try {
            registerNlsServiceListener();
            result = new Holder<INlsProject>(INlsProject.class, null);
            INlsProject nlsProject = NlsCore.getNlsWorkspace().getNlsProject(new Object[]{TypeUtility.getType(IRuntimeClasses.TEXTS), this});
            result.setValue(nlsProject);
            m_nlsProjectHolder = result;
          }
          catch (CoreException e) {
            ScoutSdk.logError("error loading NLS project for: '" + getSymbolicName() + "'.", e);
          }
        }
      }
    }
    return result.getValue();
  }

  @Override
  public INlsProject getDocsNlsProject() {
    Holder<INlsProject> result = m_docsNlsProjectHolder;
    if (result == null) {
      synchronized (this) {
        result = m_docsNlsProjectHolder;
        if (result == null) {
          try {
            registerNlsServiceListener();
            result = new Holder<INlsProject>(INlsProject.class, null);
            INlsProject nlsProject = NlsCore.getNlsWorkspace().getNlsProject(new Object[]{TypeUtility.getType(IRuntimeClasses.IDocumentationTextProviderService), this});
            result.setValue(nlsProject);
            m_docsNlsProjectHolder = result;
          }
          catch (CoreException e) {
            ScoutSdk.logError("error loading NLS project for: '" + getSymbolicName() + "'.", e);
          }
        }
      }
    }
    return result.getValue();
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
    IPackageFragmentRoot result = m_rootPackage;
    if (result == null) {
      synchronized (this) {
        result = m_rootPackage;
        if (result == null) {
          Path src = new Path(IPath.SEPARATOR + getSymbolicName() + IPath.SEPARATOR + TypeUtility.DEFAULT_SOURCE_FOLDER_NAME);
          result = getJavaProject().findPackageFragmentRoot(src);
          m_rootPackage = result;
        }
      }
    }
    return result.getPackageFragment(packageFqn);
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
    IIconProvider result = m_iconProvider;
    if (result == null) {
      synchronized (this) {
        result = m_iconProvider;
        if (result == null) {
          m_iconProvider = result = new ScoutProjectIcons(this);
        }
      }
    }
    return result;
  }

  @Override
  public boolean isBinary() {
    return m_isBinary;
  }

  @Override
  public boolean isFragment() {
    return m_isFragment;
  }

  @Override
  public Object getAdapter(Class adapter) {
    if (IScoutBundle.class == adapter || IAdaptable.class == adapter || ScoutBundle.class == adapter) {
      return this;
    }
    if (!isBinary()) {
      if (IResource.class == adapter || ISchedulingRule.class == adapter) {
        return getProject();
      }
      if (IJavaProject.class == adapter || IParent.class == adapter || IJavaElement.class == adapter || IOpenable.class == adapter) {
        return getJavaProject();
      }
    }
    if (IPluginModelBase.class == adapter) {
      return getPluginModelBase();
    }

    return Platform.getAdapterManager().getAdapter(this, adapter);
  }

  public IPluginModelBase getPluginModelBase() {
    return m_pluginModelBase;
  }

  @Override
  public void visit(IScoutBundleGraphVisitor visitor, boolean includeThis, boolean up) {
    if (includeThis) {
      breadthFirstTraverseFromThis(visitor, up);
    }
    else {
      breadthFirstTraverseNeighbors(visitor, up, up ? m_parentBundles : m_childBundles);
    }
  }

  @Override
  public Map<String, IPluginModelBase> getAllDependencies() {
    return CollectionUtility.copyMap(m_allDependencies);
  }

  private void registerNlsServiceListener() {
    if (m_textProvidersChangedListener == null) {
      IType abstractDynamicNlsTextProviderService = TypeUtility.getType(IRuntimeClasses.AbstractDynamicNlsTextProviderService);
      if (TypeUtility.exists(abstractDynamicNlsTextProviderService)) {
        ICachedTypeHierarchy pth = TypeUtility.getPrimaryTypeHierarchy(abstractDynamicNlsTextProviderService);
        m_textProvidersChangedListener = new P_TextProviderServiceHierarchyChangedListener(this);
        pth.addHierarchyListener(m_textProvidersChangedListener);
      }
    }
  }

  private void breadthFirstTraverseNeighbors(IScoutBundleGraphVisitor visitor, boolean up, Set<ScoutBundle> directNeighbors) {
    Deque<P_TraverseComposite> deck = new LinkedList<P_TraverseComposite>();
    for (ScoutBundle start : directNeighbors) {
      deck.addLast(new P_TraverseComposite(start, 1));
    }
    breadthFirstTraverse(visitor, up, deck);
  }

  private void breadthFirstTraverseFromThis(IScoutBundleGraphVisitor visitor, boolean up) {
    Deque<P_TraverseComposite> deck = new LinkedList<P_TraverseComposite>();
    deck.addLast(new P_TraverseComposite(this, 0));
    breadthFirstTraverse(visitor, up, deck);
  }

  /**
   * level order traversal
   *
   * @param start
   * @param visitor
   * @param up
   */
  private static void breadthFirstTraverse(IScoutBundleGraphVisitor visitor, boolean up, Deque<P_TraverseComposite> deck) {
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

  void removeImplicitChildren() {
    Iterator<ScoutBundle> bundleIt = m_childBundles.iterator();
    while (bundleIt.hasNext()) {
      ScoutBundle bundle = bundleIt.next();
      bundle.removeImplicitChildren();

      ScoutBundle[] otherChildren = m_childBundles.toArray(new ScoutBundle[m_childBundles.size()]);
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
    for (ScoutBundle b : m_childBundles) {
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

  private Map<String, IPluginModelBase> getAllDependenciesImpl(IPluginModelBase bundle, IProgressMonitor monitor) {
    Map<String, IPluginModelBase> collector = new HashMap<String, IPluginModelBase>();
    Stack<IPluginModelBase> dependencyStack = new Stack<IPluginModelBase>();
    Set<String> messageCollector = new HashSet<String>();
    collectDependencies(bundle, collector, dependencyStack, messageCollector, true, monitor);
    getDependencyIssues().addAll(messageCollector);
    return collector;
  }

  private synchronized void clearNlsCache() {
    m_nlsProjectHolder = null;
    m_docsNlsProjectHolder = null;
    if (m_textProvidersChangedListener != null) {
      IType abstractDynamicNlsTextProviderService = TypeUtility.getType(IRuntimeClasses.AbstractDynamicNlsTextProviderService);
      if (TypeUtility.exists(abstractDynamicNlsTextProviderService)) {
        ICachedTypeHierarchy pth = TypeUtility.getPrimaryTypeHierarchy(abstractDynamicNlsTextProviderService);
        pth.removeHierarchyListener(m_textProvidersChangedListener);
      }
      m_textProvidersChangedListener = null; // weak listener. will be collected when this instance holds no reference anymore.
    }
  }

  private static void collectDependencies(IPluginModelBase bundle, Map<String, IPluginModelBase> collector, Stack<IPluginModelBase> dependencyStack, Set<String> messageCollector, boolean rec, IProgressMonitor monitor) {
    if (bundle != null && bundle.getBundleDescription() != null) {
      for (BundleSpecification dependency : bundle.getBundleDescription().getRequiredBundles()) {
        if (monitor != null && monitor.isCanceled()) {
          return;
        }
        if (!bundle.getBundleDescription().getSymbolicName().equals(dependency.getName())) { // ignore dependencies on the bundle itself
          IPluginModelBase model = PluginRegistry.findModel(dependency.getName());
          if (model != null) {
            addDependency(model, collector, dependencyStack, messageCollector, rec, monitor);
          }
        }
      }

      if (bundle.getBundleDescription().getHost() != null) {
        for (BundleDescription host : bundle.getBundleDescription().getHost().getHosts()) {
          // it is a fragment: the dependencies of the host are also present.
          IPluginModelBase model = PluginRegistry.findModel(host.getSymbolicName());
          if (model != null) {
            addDependency(model, collector, dependencyStack, messageCollector, rec, monitor);
          }
        }
      }
    }
  }

  private static boolean handleDependencyCycle(IPluginModelBase bundle, Map<String, IPluginModelBase> collector, Stack<IPluginModelBase> dependencyStack, Set<String> messageCollector) {
    if (dependencyStack.contains(bundle)) {
      // a dependency loop was detected: log the loop
      StringBuilder loopMsg = new StringBuilder(Texts.get("DependencyLoopDetected"));
      loopMsg.append(":\n");
      boolean loopBeginFound = false;
      for (IPluginModelBase s : dependencyStack) {
        if (!loopBeginFound && s.equals(bundle)) {
          loopBeginFound = true;
        }
        if (loopBeginFound) {
          String symbolicName = s.getBundleDescription().getSymbolicName();
          loopMsg.append(symbolicName);
          loopMsg.append('\n');
          collector.remove(symbolicName); // correction: remove all dependencies that build up the cycle
        }
      }
      loopMsg.append(bundle.getBundleDescription().getSymbolicName());
      messageCollector.add(loopMsg.toString());
      return true; // cycle found
    }
    return false; // no cycle found
  }

  private static void addDependency(IPluginModelBase bundle, Map<String, IPluginModelBase> collector, Stack<IPluginModelBase> dependencyStack, Set<String> messageCollector, boolean rec, IProgressMonitor monitor) {
    // dependency loop detection & prevention
    if (handleDependencyCycle(bundle, collector, dependencyStack, messageCollector)) {
      // cycle found and corrected: stop processing of this part of the dependency graph
      return;
    }

    IPluginModelBase existingBundle = collector.put(bundle.getBundleDescription().getSymbolicName(), bundle);
    if (existingBundle != null) {
      // we have already processed this bundle and its children. cancel here.
      return;
    }

    try {
      dependencyStack.push(bundle);
      if (rec && !RuntimeBundles.containsTypeDefiningBundle(bundle.getBundleDescription())) {
        collectDependencies(bundle, collector, dependencyStack, messageCollector, rec, monitor);
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

  private static final class P_BundleCollector implements IScoutBundleGraphVisitor {
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

    public Set<IScoutBundle> getElements() {
      return m_collector;
    }
  }

  private static final class P_SingleBundleByLevelCollector implements IScoutBundleGraphVisitor {
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

  private static final class P_TextProviderServiceHierarchyChangedListener implements ITypeHierarchyChangedListener, WeakEventListener {
    private ScoutBundle m_observer;

    private P_TextProviderServiceHierarchyChangedListener(ScoutBundle observer) {
      m_observer = observer;
    }

    @Override
    public void hierarchyInvalidated() {
      m_observer.clearNlsCache();
    }
  }

  private static final class P_TraverseComposite {
    private final int m_level;
    private final ScoutBundle m_bundle;

    private P_TraverseComposite(ScoutBundle bundle, int level) {
      m_level = level;
      m_bundle = bundle;
    }
  }
}
