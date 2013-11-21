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
package org.eclipse.scout.sdk.workspace;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.bundles.RuntimeBundles;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.util.NamingUtility;

/**
 * <h3>{@link IScoutBundle}</h3><br>
 * Represents a plug-in having a dependency on scout runtime bundles.<br>
 * <br>
 * Do not hold references to IScoutBundle instances without handling the corresponding scout workspace events: If the
 * bundle model (e.g. a manifest file or the target platform) changes in the workspace, this may trigger a complete
 * re-build of the scout bundle graph. On a re-build all existing scout bundle instances are discarded and replaced by
 * new instances matching the new workspace situation. Ensure to listen for the corresponding scout workspace events
 * (see {@link IScoutWorkspace} and {@link IScoutWorkspaceListener} acquired from {@link ScoutSdkCore}) to replace your
 * instances when necessary.
 * 
 * @author Matthias Villiger
 * @since 3.9.0 27.02.2013
 * @see IScoutBundleGraph
 */
public interface IScoutBundle extends IAdaptable {

  final String TYPE_CLIENT = "CLIENT";
  final String TYPE_SHARED = "SHARED";
  final String TYPE_SERVER = "SERVER";
  final String TYPE_UI_SWING = "UI_SWING";
  final String TYPE_UI_SWT = "UI_SWT";

  /**
   * Gets the type of the scout bundle. <br>
   * This string is always one of the types contributed by the
   * 'org.eclipse.scout.sdk.runtimeBundles' extension point.
   * 
   * @return the
   * @see RuntimeBundles
   */
  String getType();

  /**
   * gets a live-list of the direct parent scout bundles (non-recursive). This is equal to the most specific
   * dependencies of this bundle to other scout bundles.
   * 
   * @return a live-set containing all direct parents.
   */
  Set<? extends IScoutBundle> getDirectParentBundles();

  /**
   * gets a live-list of the direct child scout bundles (non-recursive). this is equal to the scout bundles having most
   * specific dependencies to this bundle (my dependents).
   * 
   * @return a live-set containing all direct children.
   */
  Set<? extends IScoutBundle> getDirectChildBundles();

  /**
   * Performs a breadth first (aka level order) traversal going up the tree visiting all parents (=dependencies) and
   * maybe myself recursively. It returns all scout bundles matching the given filter (including maybe myself) ordered
   * by level (the closest level first). Within a level the order of the bundles is undefined.
   * 
   * @param filter
   *          The filter to decide which of the parent bundles will be returned.
   * @param includeThis
   *          specifies if the current instance should be visited as well. if true, this may be part of the resulting
   *          array.
   * @return an array holding all parent bundles (recursive) matching the given filter ordered by level (closest level
   *         first).
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Breadth-first_search">http://en.wikipedia.org/wiki/Breadth-first_search</a>
   */
  IScoutBundle[] getParentBundles(IScoutBundleFilter filter, boolean includeThis);

  /**
   * Performs a breadth first (aka level order) traversal going up the tree visiting all parents (=dependencies) and
   * maybe myself recursively. It returns the first parent bundle (or maybe myself) according to the given filter.<br>
   * If multiple bundles matching the filter are found on the nearest level, the one having the most similar name to
   * the symbolic name of this instance is returned according to the levenshtein distance.
   * 
   * @param filter
   *          The filter to decide which of the parent bundles will be considered as candidates to be returned.
   * @param includeThis
   *          Specifies if the current instance should be visited as well. If true, this may be the result (if it
   *          matches the filter).
   * @return The scout bundle on the nearest level matching the given filter.
   * @see IScoutBundleFilter
   * @see ScoutBundleFilters
   * @see IScoutBundleComparator
   * @see ScoutBundleComparators
   * @see NamingUtility#stringDistance(String, String)
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Breadth-first_search">http://en.wikipedia.org/wiki/Breadth-first_search</a>
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Levenshtein_distance">http://en.wikipedia.org/wiki/Levenshtein_distance</a>
   */
  IScoutBundle getParentBundle(IScoutBundleFilter filter, boolean includeThis);

  /**
   * Performs a breadth first (aka level order) traversal going up the tree visiting all parents (=dependencies) and
   * maybe myself recursively. It returns the first parent bundle (or maybe myself) according to the given filter.<br>
   * If multiple bundles matching the filter are found on the nearest level, the one having the most similar name to
   * the symbolic name of the given reference bundle is returned according to the levenshtein distance.
   * 
   * @param filter
   *          The filter to decide which of the parent bundles will be considered as candidates to be returned.
   * @param reference
   *          The reference bundle to which the match with the most similar name should be returned.
   * @param includeThis
   *          Specifies if the current instance should be visited as well. If true, this may be the result (if it
   *          matches the filter).
   * @return The scout bundle on the nearest level matching the given filter.
   * @see IScoutBundleFilter
   * @see ScoutBundleFilters
   * @see IScoutBundleComparator
   * @see ScoutBundleComparators
   * @see NamingUtility#stringDistance(String, String)
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Breadth-first_search">http://en.wikipedia.org/wiki/Breadth-first_search</a>
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Levenshtein_distance">http://en.wikipedia.org/wiki/Levenshtein_distance</a>
   */
  IScoutBundle getParentBundle(IScoutBundleFilter filter, IScoutBundle reference, boolean includeThis);

  /**
   * Performs a breadth first (aka level order) traversal going up the tree visiting all parents (=dependencies) and
   * maybe myself recursively. It returns the first parent bundle (or maybe myself) according to the given filter and
   * comparator.
   * 
   * @param filter
   *          The filter to decide which of the parent bundles will be considered as candidates to be returned.
   * @param comparator
   *          If multiple bundles matching the filter are found on the nearest level, the first (least) bundle as
   *          defined by this comparator is chosen.
   * @param includeThis
   *          Specifies if the current instance should be visited as well. If true, this may be the result (if it
   *          matches the filter).
   * @return The scout bundle on the nearest level matching the given filter. If multiple bundles on the same level
   *         fulfill the filter, the first (least) as defined by the given comparator is returned.
   * @see IScoutBundleFilter
   * @see ScoutBundleFilters
   * @see IScoutBundleComparator
   * @see ScoutBundleComparators
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Breadth-first_search">http://en.wikipedia.org/wiki/Breadth-first_search</a>
   */
  IScoutBundle getParentBundle(IScoutBundleFilter filter, IScoutBundleComparator comparator, boolean includeThis);

  /**
   * Performs a breadth first (aka level order) traversal going down the tree visiting all children (=dependents) and
   * maybe myself recursively. It returns all scout bundles matching the given filter (including maybe myself) ordered
   * by level (closest level first). Within a level the order of the bundles is undefined.
   * 
   * @param filter
   *          The filter to decide which of the child bundles will be returned.
   * @param includeThis
   *          specifies if the current instance should be visited as well. if true, this may be part of the resulting
   *          array.
   * @return an array holding all child bundles (recursive) matching the given filter ordered by level (closest level
   *         first).
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Breadth-first_search">http://en.wikipedia.org/wiki/Breadth-first_search</a>
   */
  IScoutBundle[] getChildBundles(IScoutBundleFilter filter, boolean includeThis);

  /**
   * Performs a breadth first (aka level order) traversal going down the tree visiting all children (=dependents) and
   * maybe myself recursively. It returns the first child bundle (or maybe myself) according to the given filter.<br>
   * If multiple bundles matching the filter are found on the nearest level, the one having the most similar name to
   * the symbolic name of this instance is returned according to the levenshtein distance.
   * 
   * @param filter
   *          The filter to decide which of the child bundles will be considered as candidates to be returned.
   * @param includeThis
   *          Specifies if the current instance should be visited as well. If true, this may be the result (if it
   *          matches the filter).
   * @return The scout bundle on the nearest level matching the given filter.
   * @see IScoutBundleFilter
   * @see ScoutBundleFilters
   * @see IScoutBundleComparator
   * @see ScoutBundleComparators
   * @see NamingUtility#stringDistance(String, String)
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Breadth-first_search">http://en.wikipedia.org/wiki/Breadth-first_search</a>
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Levenshtein_distance">http://en.wikipedia.org/wiki/Levenshtein_distance</a>
   */
  IScoutBundle getChildBundle(IScoutBundleFilter filter, boolean includeThis);

  /**
   * Performs a breadth first (aka level order) traversal going down the tree visiting all children (=dependents) and
   * maybe myself recursively. It returns the first child bundle (or maybe myself) according to the given filter.<br>
   * If multiple bundles matching the filter are found on the nearest level, the one having the most similar name to
   * the symbolic name of the given reference bundle is returned according to the levenshtein distance.
   * 
   * @param filter
   *          The filter to decide which of the child bundles will be considered as candidates to be returned.
   * @param reference
   *          The reference bundle to which the match with the most similar name should be returned.
   * @param includeThis
   *          Specifies if the current instance should be visited as well. If true, this may be the result (if it
   *          matches the filter).
   * @return The scout bundle on the nearest level matching the given filter.
   * @see IScoutBundleFilter
   * @see ScoutBundleFilters
   * @see IScoutBundleComparator
   * @see ScoutBundleComparators
   * @see NamingUtility#stringDistance(String, String)
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Breadth-first_search">http://en.wikipedia.org/wiki/Breadth-first_search</a>
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Levenshtein_distance">http://en.wikipedia.org/wiki/Levenshtein_distance</a>
   */
  IScoutBundle getChildBundle(IScoutBundleFilter filter, IScoutBundle reference, boolean includeThis);

  /**
   * Performs a breadth first (aka level order) traversal going down the tree visiting all children (=dependents) and
   * maybe myself recursively. It returns the first child bundle (or maybe myself) according to the given filter and
   * comparator.
   * 
   * @param filter
   *          The filter to decide which of the child bundles will be considered as candidates to be returned.
   * @param comparator
   *          If multiple bundles matching the filter are found on the nearest level, the first (least) bundle as
   *          defined by this comparator is chosen.
   * @param includeThis
   *          Specifies if the current instance should be visited as well. If true, this may be the result (if it
   *          matches the filter).
   * @return The scout bundle on the nearest level matching the given filter. If multiple bundles on the same level
   *         fulfill the filter, the first (least) as defined by the given comparator is returned.
   * @see IScoutBundleFilter
   * @see ScoutBundleFilters
   * @see IScoutBundleComparator
   * @see ScoutBundleComparators
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Breadth-first_search">http://en.wikipedia.org/wiki/Breadth-first_search</a>
   */
  IScoutBundle getChildBundle(IScoutBundleFilter filter, IScoutBundleComparator comparator, boolean includeThis);

  /**
   * gets the symbolic name of this bundle.
   * 
   * @return the symbolic name
   */
  String getSymbolicName();

  /**
   * gets the eclipse preferences to store settings that only belong to this bundle. this method returns null if this
   * bundle is a binary bundle.
   * 
   * @return the preferences or null
   * @see IScoutBundle#isBinary()
   */
  IEclipsePreferences getPreferences();

  /**
   * checks whether this bundle contains the given java element.
   * 
   * @param e
   *          the java element that will be searched in this bundle
   * @return true if the element is in this bundle, false otherwise.
   */
  boolean contains(IJavaElement e);

  /**
   * gets the java project that belongs to this bundle or null if it is a binary bundle.
   * 
   * @return the corresponding java project or null.
   * @see IScoutBundle#isBinary()
   */
  IJavaProject getJavaProject();

  /**
   * gets the project that belongs to this bundle or null if this is a binary bundle.
   * 
   * @return the corresponding bundle or null.
   * @see IScoutBundle#isBinary()
   */
  IProject getProject();

  /**
   * gets the NLS project tree for this bundle
   * 
   * @return the NLS project for this bundle.
   * @see INlsProject
   */
  INlsProject getNlsProject();

  /**
   * gets the documentation NLS project tree for this bundle.
   * 
   * @return the documentation NLS project
   * @see INlsProject
   */
  INlsProject getDocsNlsProject();

  /**
   * gets the icon provider for this bundle
   * 
   * @return the icon provider for this bundle
   * @see IIconProvider
   */
  IIconProvider getIconProvider();

  /**
   * gets the fully qualified package name inside this bundle extended by the given appendix.
   * 
   * @param appendix
   *          the suffix that should be added to the base package of this bundle.
   * @return the complete package name.
   */
  String getPackageName(String appendix);

  /**
   * gets the package fragment inside this bundle matching the given fully qualified name
   * 
   * @param packageFqn
   *          the fully qualified name of the package
   * @return the package fragment (it may exist or not)
   * @throws JavaModelException
   * @see IPackageFragmentRoot#getPackageFragment(String)
   */
  IPackageFragment getPackageFragment(String packageFqn) throws JavaModelException;

  /**
   * Gets the fully qualified default package inside this bundle.<br>
   * Any bundle configurations are considered when computing the default package for the given Id.
   * 
   * @param packageId
   *          one of the constants defined in IDefaultTargetPackage
   * @return the fully qualified package name (symbolic name of this bundle extended by the suffix as configured for
   *         this bundle).
   * @see IDefaultTargetPackage
   */
  String getDefaultPackage(String packageId);

  /**
   * Gets if this bundle is in the workspace or on the target platform.
   * 
   * @return true if it is on the target platform. false otherwise.
   */
  boolean isBinary();

  /**
   * Gets if this is a fragment bundle or not.
   * 
   * @return true if it is a fragment
   */
  boolean isFragment();

  /**
   * Visits all parent or child bundles of the receiver optionally including the receiver itself in the visit.<br>
   * <br>
   * The visit performs a breadth first (aka level order) traversal first visiting the nearest neighbors of the receiver
   * and then continuing with the next levels.
   * 
   * @param visitor
   *          The visitor.
   * @param includeThis
   *          true if the receiver should be visited as well. false otherwise.
   * @param up
   *          true if the parents (=dependencies) of the receiver should be visited. false if the children (=dependents)
   *          should be visited.
   * @see IScoutBundleGraphVisitor
   * @see IScoutBundleGraphVisitor#visit(IScoutBundle, int)
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Breadth-first_search">http://en.wikipedia.org/wiki/Breadth-first_search</a>
   */
  void visit(IScoutBundleGraphVisitor visitor, boolean includeThis, boolean up);

}
