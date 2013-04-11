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

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.scout.sdk.ScoutSdkCore;

/**
 * <h3>{@link IScoutBundleGraph}</h3> The bundle graph responsible for holding and building the scout bundles and the
 * connections between them.
 * 
 * @author mvi
 * @since 3.9.0 31.01.2013
 * @see IScoutWorkspace#getBundleGraph()
 * @see ScoutSdkCore#getScoutWorkspace()
 */
public interface IScoutBundleGraph {

  /**
   * Gets all scout bundles of the graph matching the given filter.<br>
   * If the bundle graph is currently building, this method blocks until the build has been completed.
   * 
   * @param filter
   *          The filter to decide which bundle will be returned or null when no filter should be applied.
   * @param comparator
   *          the comparator defining the order in which the bundles are returned or null if the order is not relevant.
   * @return all scout bundles matching the given filter sorted using the given comparator.
   * @see IScoutBundle
   * @see IScoutBundleFilter
   * @see IScoutBundleComparator
   * @see ScoutBundleFilters
   * @see ScoutBundleComparators
   */
  IScoutBundle[] getBundles(IScoutBundleFilter filter, IScoutBundleComparator comparator);

  /**
   * Gets all scout bundles of the graph matching the given filter.<br>
   * The order of the returned bundles is undefined.<br>
   * If the bundle graph is currently building, this method blocks until the build has been completed.
   * 
   * @param filter
   *          The filter to decide which bundle will be returned or null when no filter should be applied.
   * @return all scout bundles matching the given filter.
   * @see IScoutBundle
   * @see IScoutBundleFilter
   * @see ScoutBundleFilters
   */
  IScoutBundle[] getBundles(IScoutBundleFilter filter);

  /**
   * Gets the scout bundle the given java element is in or null if no scout bundle exists for the given element.<br>
   * If the bundle graph is currently building, this method blocks until the build has been completed.
   * 
   * @param je
   *          the java element
   * @return the scout bundle containing the given element or null.
   * @see IScoutBundle
   */
  IScoutBundle getBundle(IJavaElement je);

  /**
   * Gets the scout bundle for the given eclipse project or null if no scout bundle is associated with the given
   * project.<br>
   * If the bundle graph is currently building, this method blocks until the build has been completed.
   * 
   * @param p
   *          the project that belongs to the returned scout bundle
   * @return the scout bundle that belongs to the given project or null.
   * @see IScoutBundle
   */
  IScoutBundle getBundle(IProject p);

  /**
   * Gets the scout bundle with the given symbolic name or null if no such bundle could be found.<br>
   * If the bundle graph is currently building, this method blocks until the build has been completed.
   * 
   * @param symbolicName
   *          the symbolic name of the scout bundle to search.
   * @return the scout bundle matching the given symbolic name or null.
   * @see IScoutBundle
   */
  IScoutBundle getBundle(String symbolicName);

}
