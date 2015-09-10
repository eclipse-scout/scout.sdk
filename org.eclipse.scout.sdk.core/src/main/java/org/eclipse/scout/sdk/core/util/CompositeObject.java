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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 * <h3>{@link CompositeObject}</h3> Composite object used to sort and compare compositions of objects.
 *
 * @since 5.1.0
 */
public class CompositeObject implements Comparable<CompositeObject>, Serializable {
  private static final long serialVersionUID = 0L;
  private final Object[] m_value;
  private final int m_hash;

  public CompositeObject(Object... a) {
    if (a != null && a.length == 1 && a[0] instanceof Collection<?>) {
      m_value = ((Collection<?>) a[0]).toArray();
    }
    else {
      m_value = a;
    }
    m_hash = Arrays.deepHashCode(m_value);
  }

  public static CompositeObject concat(CompositeObject... objects) {
    int n = 0;
    for (CompositeObject o : objects) {
      n += (o.m_value != null ? o.m_value.length : 0);
    }
    Object[] a = new Object[n];
    int pos = 0;
    for (CompositeObject o : objects) {
      int i = (o.m_value != null ? o.m_value.length : 0);
      if (i > 0) {
        System.arraycopy(o.m_value, 0, a, pos, i);
        pos += i;
      }
    }
    return new CompositeObject(a);
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
    if (!(obj instanceof CompositeObject)) {
      return false;
    }
    CompositeObject other = (CompositeObject) obj;
    if (!Arrays.deepEquals(m_value, other.m_value)) {
      return false;
    }
    return true;
  }

  public int getComponentCount() {
    if (m_value == null) {
      return 0;
    }
    return m_value.length;
  }

  public Object getComponent(int index) {
    return m_value[index];
  }

  public Object[] getComponents() {
    return m_value;
  }

  @Override
  public int compareTo(CompositeObject o) {
    Object[] me = this.m_value;
    Object[] other = o.m_value;
    if (me == null) {
      return -1;
    }
    else if (other == null) {
      return 1;
    }
    for (int i = 0; i < me.length && i < other.length; i++) {
      int c = compareImpl(me[i], other[i]);
      if (c != 0) {
        return c;
      }
    }
    if (me.length < other.length) {
      return -1;
    }
    if (me.length > other.length) {
      return 1;
    }
    return 0;
  }

  @SuppressWarnings("unchecked")
  private static int compareImpl(Object a, Object b) {
    if (a == null && b == null) {
      return 0;
    }
    if (a == null) {
      return -1;
    }
    if (b == null) {
      return 1;
    }
    if ((a instanceof Comparable) && (b instanceof Comparable)) {
      return ((Comparable<Object>) a).compareTo(b);
    }
    return a.toString().compareTo(b.toString());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    if (m_value != null && m_value.length > 0) {
      sb.append(String.valueOf(m_value[0]));
      for (int i = 1; i < m_value.length; i++) {
        sb.append(',');
        sb.append(String.valueOf(m_value[i]));
      }
    }
    sb.append(']');
    return sb.toString();
  }
}
