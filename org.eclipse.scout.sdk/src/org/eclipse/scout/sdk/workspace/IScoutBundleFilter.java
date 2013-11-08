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

/**
 * <h3>{@link IScoutBundleFilter}</h3> A scout bundle filter
 * 
 * @author Matthias Villiger
 * @since 3.9.0 27.02.2013
 * @see IScoutBundle
 * @see ScoutBundleFilters
 */
public interface IScoutBundleFilter {
  /**
   * is called for every item in a list of scout bundles
   * 
   * @param bundle
   *          the current bundle
   * @return true if the given bundle fulfills the current filter, false otherwise.
   */
  boolean accept(IScoutBundle bundle);
}
