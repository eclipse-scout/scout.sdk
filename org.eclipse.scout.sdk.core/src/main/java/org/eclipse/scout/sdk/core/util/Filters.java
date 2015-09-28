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
package org.eclipse.scout.sdk.core.util;

import java.util.Collection;

/**
 * Contains {@link IFilter}s.
 */
public final class Filters {

  private Filters() {
  }

  /**
   * Collect all items and return always false.
   * <p>
   * This filter can be used in the various {@link CoreUtils} find methods when all items should be collected.
   * <p>
   * When used together with the {@link #and(IFilter...)} a collector can be built that only collects the items that are
   * accepted by the former filters.
   */
  public static <T> IFilter<T> collect(final Collection<T> collection) {
    return new IFilter<T>() {
      @Override
      public boolean evaluate(T element) {
        collection.add(element);
        return false;
      }
    };
  }

  /**
   * Gets a {@link IFilter} which evaluates to <code>true</code> if at least one of the given not-null {@link IFilter}s
   * evaluates to <code>true</code>.
   * <p>
   * This is the conditional AND. The filter list is not processed any further once a filter element accepts.
   *
   * @param filters
   *          The {@link IFilter}s to evaluate.
   * @return The created {@link IFilter}.
   */
  @SafeVarargs
  public static <T> IFilter<T> or(final IFilter<T>... filters) {
    return unionAndOr(true, filters);
  }

  /**
   * Gets a {@link IFilter} which evaluates to <code>true</code> if all the given not-null {@link IFilter}s evaluate to
   * <code>true</code>.
   * <p>
   * This is the conditional AND. The filter list is not processed any further once a filter element rejects.
   *
   * @param filters
   *          The {@link IFilter}s to evaluate.
   * @return The created {@link IFilter}.
   */
  @SafeVarargs
  public static <T> IFilter<T> and(final IFilter<T>... filters) {
    return unionAndOr(false, filters);
  }

  @SafeVarargs
  private static <T> IFilter<T> unionAndOr(final boolean or, final IFilter<T>... filters) {
    if (filters == null || filters.length < 1) {
      return null;
    }
    if (filters.length == 1) {
      return filters[0];
    }

    return new IFilter<T>() {
      @Override
      public boolean evaluate(T candidate) {
        for (IFilter<T> f : filters) {
          if (f != null) {
            boolean accepted = f.evaluate(candidate);
            if (or == accepted) {
              return accepted;
            }
          }
        }
        return !or;
      }

      @Override
      public String toString() {
        StringBuilder text = new StringBuilder();
        text.append((or ? "OR" : "AND") + "{");
        for (int i = 0; i < filters.length; i++) {
          text.append(filters[i]);
          if (i < filters.length - 1) {
            text.append(", ");
          }
        }
        return text.toString();
      }
    };
  }
}
