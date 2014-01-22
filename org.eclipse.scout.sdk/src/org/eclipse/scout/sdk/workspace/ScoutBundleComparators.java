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

import org.eclipse.scout.sdk.util.NamingUtility;

/**
 * <h3>{@link ScoutBundleComparators}</h3> Contains pre-defined scout bundle comparators
 * 
 * @author Matthias Villiger
 * @since 3.9.0 09.02.2013
 * @see IScoutBundleComparator
 * @see IScoutBundle
 * @see IScoutBundleGraph
 */
public final class ScoutBundleComparators {
  private static final IScoutBundleComparator SYMBOLIC_NAME_COMPARATOR = new IScoutBundleComparator() {
    @Override
    public int compare(IScoutBundle o1, IScoutBundle o2) {
      return o1.getSymbolicName().compareTo(o2.getSymbolicName());
    }
  };

  private ScoutBundleComparators() {
  }

  /**
   * @return a comparator that returns the scout bundles sorted ascending by their symbolic names
   */
  public static IScoutBundleComparator getSymbolicNameAscComparator() {
    return SYMBOLIC_NAME_COMPARATOR;
  }

  /**
   * Creates and returns a comparator that sorts the bundles by their levenshtein distance to the given reference
   * bundle.<br>
   * For calculating the levenshtein distance the symbolic names of the bundles are used.
   * The bundle with the symbolic name most similar to the symbolic name of the given reference bundle is the first in
   * the list.<br>
   * If two bundles have the same distance to the reference, they are sorted ascending by their symbolic name as a
   * second sorting criteria.
   * 
   * @param ref
   *          the reference bundle. the symbolic name of this bundle is used as reference
   * @return the created comparator
   * @see NamingUtility#stringDistance(String, String)
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Levenshtein_distance">http://en.wikipedia.org/wiki/Levenshtein_distance</a>
   */
  public static IScoutBundleComparator getSymbolicNameLevenshteinDistanceComparator(IScoutBundle ref) {
    return getSymbolicNameLevenshteinDistanceComparator(ref.getSymbolicName());
  }

  /**
   * Creates and returns a comparator that sorts the bundles by their levenshtein distance to the given reference
   * name.<br>
   * For calculating the levenshtein distance the symbolic names of the bundles are used.
   * The bundle with the symbolic name most similar to the given reference is the first in the list.<br>
   * If two bundles have the same distance to the reference, they are sorted ascending by their symbolic name as a
   * second sorting criteria.
   * 
   * @param symbolicName
   *          the name to use as reference
   * @return the created comparator
   * @see NamingUtility#stringDistance(String, String)
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Levenshtein_distance">http://en.wikipedia.org/wiki/Levenshtein_distance</a>
   */
  public static IScoutBundleComparator getSymbolicNameLevenshteinDistanceComparator(final String symbolicName) {
    return new IScoutBundleComparator() {
      @Override
      public int compare(IScoutBundle o1, IScoutBundle o2) {
        // fast pre-check
        if (o1.equals(o2)) {
          return 0;
        }

        int distance1 = NamingUtility.stringDistance(o1.getSymbolicName(), symbolicName);
        int distance2 = NamingUtility.stringDistance(o2.getSymbolicName(), symbolicName);

        int ret = distance1 - distance2;
        if (ret != 0) {
          return ret;
        }

        // the two items have the same difference to the reference name
        // to ensure there is no random behavior in this case: order the bundles by name as second criteria
        return SYMBOLIC_NAME_COMPARATOR.compare(o1, o2);
      }
    };
  }
}
