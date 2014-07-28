/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.workspace;

/**
 * <h3>{@link IScoutBundleGraphVisitor}</h3> A visitor used to traverse the scout bundle graph.
 *
 * @author Matthias Villiger
 * @since 3.10.0 20.11.2013
 * @see IScoutBundle#visit(IScoutBundleGraphVisitor, boolean, boolean)
 */
public interface IScoutBundleGraphVisitor {
  /**
   * Callback for every visited bundle.<br>
   * The visit performs a breadth first (aka level order) traversal which means the traversalLevel will always increase
   * during the visits.
   *
   * @param bundle
   *          The current bundle visited.
   * @param traversalLevel
   *          The distance in the bundle graph to the bundle on which the visit has been started.<br>
   *          A distance of 0 means the start bundle itself. A distance of 1 means the direct parents (=dependencies) or
   *          children (=dependents).
   * @return true if the visit should continue, false if the visit should be aborted.
   * @see IScoutBundle#visit(IScoutBundleGraphVisitor, boolean, boolean)
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Breadth-first_search">http://en.wikipedia.org/wiki/Breadth-first_search</a>
   */
  boolean visit(IScoutBundle bundle, int traversalLevel);
}
