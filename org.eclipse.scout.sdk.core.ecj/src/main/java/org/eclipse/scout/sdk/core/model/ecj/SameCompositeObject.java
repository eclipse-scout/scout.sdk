/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import java.util.Arrays;

/**
 * <h3>{@link SameCompositeObject}</h3> Composite object used to compare compositions of objects by sameness a1==b1 AND
 * a2==b2 AND ...
 *
 * @since 5.1.0
 */
final class SameCompositeObject {

  private final Object[] m_value;
  private final int m_hash;

  SameCompositeObject(Object... a) {
    m_value = a;
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
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    var other = (SameCompositeObject) obj;
    return sameElements(m_value, other.m_value);
  }

  private static boolean sameElements(Object[] a1, Object[] a2) {
    //noinspection ArrayEquality
    if (a1 == a2) {
      return true;
    }
    if (a1 == null || a2 == null) {
      return false;
    }
    var length = a1.length;
    if (a2.length != length) {
      return false;
    }

    for (var i = 0; i < length; i++) {
      var e1 = a1[i];
      var e2 = a2[i];

      if (e1 != e2) {
        return false;
      }
    }
    return true;
  }
}
