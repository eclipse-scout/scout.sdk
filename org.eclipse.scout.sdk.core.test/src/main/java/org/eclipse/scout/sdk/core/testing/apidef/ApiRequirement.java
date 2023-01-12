/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.testing.apidef;

import java.util.function.IntPredicate;

/**
 * Specifies how an API version requirement should be interpreted.
 */
public enum ApiRequirement {
  /**
   * Specifies that the version must be equal or higher (newer) than a reference.
   */
  MIN(d -> d >= 0),

  /**
   * Specifies that the version must be equal or lower (older) than a reference.
   */
  MAX(d -> d <= 0),

  /**
   * Specifies that the version must be higher (newer) than a reference.
   */
  AFTER(d -> d > 0),

  /**
   * Specifies that the version must be lower (older) than a reference.
   */
  BEFORE(d -> d < 0);

  private final IntPredicate m_deltaComparator;

  ApiRequirement(IntPredicate predicate) {
    m_deltaComparator = predicate;
  }

  /**
   * @param actual
   *          The actual version. Must not be {@code null}.
   * @param required
   *          The required version. Must not be {@code null}.
   * @return {@code true} if the required version fulfills this requirement compared to the actual one.
   */
  public boolean isFulfilled(int[] actual, int[] required) {
    var delta = compare(actual, required);
    return m_deltaComparator.test(delta);
  }

  static int compare(int[] a, int[] b) {
    var minSize = Math.max(a.length, b.length);
    for (var i = 0; i < minSize; i++) {
      var first = i >= a.length ? 0 : a[i];
      var second = i >= b.length ? 0 : b[i];
      var c = Integer.compare(first, second);
      if (c != 0) {
        return c;
      }
    }
    return 0;
  }
}
