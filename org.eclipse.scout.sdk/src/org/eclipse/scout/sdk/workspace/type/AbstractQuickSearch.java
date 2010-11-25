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
package org.eclipse.scout.sdk.workspace.type;

import java.util.Arrays;

/**
 *
 */
public abstract class AbstractQuickSearch<T> {

  private final T[] m_elements;

  public AbstractQuickSearch(T[] elements) {
    m_elements = elements;
  }

  public T findRightSibling(T element) {
    if (getElements().length == 0) {
      return null;
    }
    int[] cachedComparators = new int[m_elements.length];
    Arrays.fill(cachedComparators, Integer.MIN_VALUE);
    int left = 0;
    int right = getElements().length;
    int index = right / 2;
    while (left != right) {
//      int index = left + ((right - left) / 2);
      if (cachedComparators[index] == Integer.MIN_VALUE) {
        cachedComparators[index] = compare(element, m_elements[index]);
      }
      if (cachedComparators[index] < 0) {
        right = index;
        index = right - (Math.max(1, (right - left) / 2));
      }
      else {
        left = index;
        index = left + (Math.max(1, (right - left) / 2));
      }
    }

    return m_elements[index];
  }

  abstract int compare(T a, T b);

  public T[] getElements() {
    return m_elements;
  }
}
