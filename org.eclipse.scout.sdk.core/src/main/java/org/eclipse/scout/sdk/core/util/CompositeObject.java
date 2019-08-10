/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import java.util.Arrays;

/**
 * <h3>{@link CompositeObject}</h3> Composite object used to sort and compare compositions of objects.
 *
 * @since 5.1.0
 */
public final class CompositeObject implements Comparable<CompositeObject> {

  private final Object[] m_value;
  private Integer m_hash;

  public CompositeObject(Object... a) {
    m_value = a;
    m_hash = null;
  }

  /**
   * Combines the given {@link CompositeObject}s to one.
   *
   * @param objects
   *          The {@link CompositeObject}s to combine. May be {@code null}.
   * @return A new {@link CompositeObject} holding all components of all the given {@link CompositeObject}s. Is never
   *         {@code null}.
   */
  public static CompositeObject concat(CompositeObject... objects) {
    if (objects == null || objects.length < 1) {
      return new CompositeObject();
    }

    Object[] a = new Object[getCombinedSize(objects)];
    int pos = 0;
    for (CompositeObject o : objects) {
      if (o != null && o.m_value != null) {
        int num = o.m_value.length;
        if (num > 0) {
          System.arraycopy(o.m_value, 0, a, pos, num);
          pos += num;
        }
      }
    }
    return new CompositeObject(a);
  }

  private static int getCombinedSize(CompositeObject... objects) {
    int size = 0;
    for (CompositeObject o : objects) {
      if (o != null && o.m_value != null) {
        size += o.m_value.length;
      }
    }
    return size;
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
    if (a instanceof Comparable && b instanceof Comparable) {
      return ((Comparable<Object>) a).compareTo(b);
    }
    return a.toString().compareTo(b.toString());
  }

  @Override
  public int hashCode() {
    if (m_hash == null) {
      m_hash = Arrays.hashCode(m_value);
    }
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

    CompositeObject other = (CompositeObject) obj;
    return Arrays.equals(m_value, other.m_value);
  }

  @Override
  public int compareTo(CompositeObject o) {
    Object[] me = m_value;
    Object[] other = o.m_value;
    if (me == other) {
      return 0;
    }
    if (me == null) {
      return -1;
    }
    if (other == null) {
      return 1;
    }

    int minSize = Math.min(me.length, other.length);
    for (int i = 0; i < minSize; i++) {
      int c = compareImpl(me[i], other[i]);
      if (c != 0) {
        return c;
      }
    }
    return Integer.compare(me.length, other.length);
  }

  public String toString(String delim) {
    if (m_value == null || m_value.length < 1) {
      return "";
    }

    StringBuilder b = new StringBuilder();
    b.append(m_value[0]);
    for (int i = 1; i < m_value.length; i++) {
      b.append(delim);
      b.append(m_value[i]);
    }
    return b.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    sb.append(toString(", "));
    sb.append(']');
    return sb.toString();
  }
}
