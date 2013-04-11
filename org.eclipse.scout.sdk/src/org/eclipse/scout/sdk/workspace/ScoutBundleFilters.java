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

import java.util.HashSet;
import java.util.Set;

/**
 * <h3>{@link ScoutBundleFilters}</h3> Contains pre-defined scout bundle filters
 * 
 * @author mvi
 * @since 3.9.0 23.02.2013
 * @see IScoutBundleFilter
 * @see IScoutBundle
 * @see IScoutBundleGraph
 */
public final class ScoutBundleFilters {

  private final static IScoutBundleFilter ROOT_BUNDLES = new IScoutBundleFilter() {
    @Override
    public boolean accept(IScoutBundle bundle) {
      return bundle.getDirectParentBundles().size() == 0;
    }
  };

  private final static IScoutBundleFilter ALL_BUNDLES = null;

  private final static IScoutBundleFilter WORKSPACE_BUNDLES = new IScoutBundleFilter() {
    @Override
    public boolean accept(IScoutBundle bundle) {
      return !bundle.isBinary();
    }
  };

  private final static IScoutBundleFilter NO_FRAGMENTS = new IScoutBundleFilter() {
    @Override
    public boolean accept(IScoutBundle bundle) {
      return !bundle.isFragment();
    }
  };

  private ScoutBundleFilters() {
  }

  /**
   * @return a filter that only returns bundles that have no parent (root bundles)
   * @see IScoutBundle#getDirectParentBundles()
   */
  public static IScoutBundleFilter getRootBundlesFilter() {
    return ROOT_BUNDLES;
  }

  /**
   * @return a filter that returns all bundles. equal to use no filter (null).
   */
  public static IScoutBundleFilter getAllBundlesFilter() {
    return ALL_BUNDLES;
  }

  /**
   * @return a filter that returns all bundles that are in the current workspace (bundles from the target platform as
   *         discarded)
   * @see IScoutBundle#isBinary()
   */
  public static IScoutBundleFilter getWorkspaceBundlesFilter() {
    return WORKSPACE_BUNDLES;
  }

  /**
   * @return a filter that returns all bundles that are no fragments.
   * @see IScoutBundle#isFragment()
   */
  public static IScoutBundleFilter getNoFragmentsFilter() {
    return NO_FRAGMENTS;
  }

  /**
   * creates and returns a filter that only returns bundles matching certain types.<br>
   * If no types are passed as filter, this filter returns no bundles.
   * 
   * @param acceptedTypes
   *          the list of types that are accepted.
   * @return a filter that only returns bundles that match the given types
   * @see IScoutBundle#getType()
   */
  public static IScoutBundleFilter getBundlesOfTypeFilter(final String... acceptedTypes) {
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        if (acceptedTypes == null || acceptedTypes.length < 1) {
          return false;
        }
        for (String type : acceptedTypes) {
          if (bundle.getType().equals(type)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  /**
   * Creates and returns a filter that only returns bundles that match all of the given filters.<br>
   * The order of the filters matters: the filter stops evaluating subsequent filters as soon as the first filter does
   * not accept a bundle. Therefore use strong and fast filters first!<br>
   * If no subsequent filters are passed, this filter returns all bundles (no filtering).
   * 
   * @param filters
   *          the subsequent filter to evaluate
   * @return the created filter
   * @see IScoutBundleFilter
   * @see ScoutBundleFilters
   */
  public static IScoutBundleFilter getMultiFilterAnd(final IScoutBundleFilter... filters) {
    return getMultiFilter(false, filters);
  }

  /**
   * Creates and returns a filter that returns bundles that match at least one of the given filters.<br>
   * The order of the filters matters: the filter stops evaluating subsequent filters as soon as the first filter
   * accepts a bundle. Therefore use strong and fast filters first!<br>
   * If no subsequent filters are passed, this filter returns all bundles (no filtering).
   * 
   * @param filters
   *          the subsequent filter to evaluate
   * @return the created filter
   * @see IScoutBundleFilter
   * @see ScoutBundleFilters
   */
  public static IScoutBundleFilter getMultiFilterOr(final IScoutBundleFilter... filters) {
    return getMultiFilter(true, filters);
  }

  private static IScoutBundleFilter getMultiFilter(final boolean or, final IScoutBundleFilter... filters) {
    if (filters == null || filters.length < 1) {
      return getAllBundlesFilter();
    }

    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        for (IScoutBundleFilter f : filters) {
          boolean accepted = f.accept(bundle);
          if (or == accepted) {
            return accepted;
          }
        }
        return !or;
      }
    };
  }

  /**
   * Creates and returns a filter that returns all bundles except the ones provided in the list.
   * 
   * @param list
   *          The list of excluded bundles.
   * @return the created filter
   * @see IScoutBundleFilter
   */
  public static IScoutBundleFilter getNotInListFilter(IScoutBundle... list) {
    if (list == null || list.length < 1) {
      return ALL_BUNDLES;
    }
    HashSet<IScoutBundle> set = new HashSet<IScoutBundle>(list.length);
    for (IScoutBundle b : list) {
      set.add(b);
    }
    return getNotInListFilter(set);
  }

  /**
   * Creates and returns a filter that returns all bundles except the ones provided in the list.
   * 
   * @param list
   *          The list of excluded bundles.
   * @return the created filter
   * @see IScoutBundleFilter
   */
  public static IScoutBundleFilter getNotInListFilter(final Set<IScoutBundle> list) {
    if (list == null || list.size() < 1) {
      return ALL_BUNDLES;
    }
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        return !list.contains(bundle);
      }
    };
  }

  /**
   * Creates and returns a filter that returns all bundles except the ones with given symbolic names.
   * 
   * @param symbolicNames
   *          The bundle symbolic names to exclude from the result
   * @return the created filter
   * @see IScoutBundleFilter
   */
  public static IScoutBundleFilter getNotInSymbolicNameListFilter(String... symbolicNames) {
    if (symbolicNames == null || symbolicNames.length < 1) {
      return ALL_BUNDLES;
    }
    HashSet<String> set = new HashSet<String>(symbolicNames.length);
    for (String name : symbolicNames) {
      set.add(name);
    }
    return getNotInSymbolicNameListFilter(set);
  }

  /**
   * Creates and returns a filter that returns all bundles except the ones with given symbolic names.
   * 
   * @param symbolicNames
   *          The bundle symbolic names to exclude from the result
   * @return the created filter
   * @see IScoutBundleFilter
   */
  public static IScoutBundleFilter getNotInSymbolicNameListFilter(final Set<String> symbolicNames) {
    if (symbolicNames == null || symbolicNames.size() < 1) {
      return ALL_BUNDLES;
    }
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        return !symbolicNames.contains(bundle.getSymbolicName());
      }
    };
  }

  /**
   * Creates and returns a filter that returns all bundles that
   * <ol>
   * <li>fulfill the given filter and</li>
   * <li>have no direct parent that also fulfills the filter</li>
   * </ol>
   * 
   * @param filter
   *          the filter to use as criteria
   * @return the created filter
   * @see IScoutBundleFilter
   */
  public static IScoutBundleFilter getFilteredRootBundlesFilter(final IScoutBundleFilter filter) {
    return new IScoutBundleFilter() {
      @Override
      public boolean accept(IScoutBundle bundle) {
        if (filter.accept(bundle)) {
          return !hasParent(bundle, filter);
        }
        return false;
      }
    };
  }

  private static boolean hasParent(IScoutBundle b, IScoutBundleFilter filter) {
    for (IScoutBundle parent : b.getDirectParentBundles()) {
      if (filter.accept(parent)) {
        return true;
      }
    }
    return false;
  }
}
