/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util;

import java.util.Arrays;
import java.util.Collection;

/**
 * <h3>{@link SameCompositeObject}</h3> Composite object used to compare compositions of objects by sameness a1==b1 AND
 * a2==b2 AND ...
 *
 * @since 5.1.0
 */
public class SameCompositeObject {

  private final Object[] m_value;
  private final int m_hash;

  public SameCompositeObject(Object... a) {
    if (a != null && a.length == 1 && a[0] instanceof Collection<?>) {
      m_value = ((Collection<?>) a[0]).toArray();
    }
    else {
      m_value = a;
    }
    m_hash = Arrays.hashCode(m_value);
  }

  @Override
  public int hashCode() {
    return m_hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SameCompositeObject other = (SameCompositeObject) obj;
    if (!deepSame(m_value, other.m_value)) {
      return false;
    }
    return true;
  }

  private static boolean deepSame(Object[] a1, Object[] a2) {
    if (a1 == a2) {
      return true;
    }
    if (a1 == null || a2 == null) {
      return false;
    }
    int length = a1.length;
    if (a2.length != length) {
      return false;
    }

    for (int i = 0; i < length; i++) {
      Object e1 = a1[i];
      Object e2 = a2[i];

      if (e1 == e2) {
        continue;
      }
      if (e1 == null) {
        return false;
      }

      // Figure out whether the two elements are equal
      if (e1 instanceof Object[] && e2 instanceof Object[]) {
        return deepSame((Object[]) e1, (Object[]) e2);
      }
      return e1 == e2;
    }
    return true;
  }
}
