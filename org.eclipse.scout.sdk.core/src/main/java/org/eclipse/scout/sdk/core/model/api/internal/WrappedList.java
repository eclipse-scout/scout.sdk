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
package org.eclipse.scout.sdk.core.model.api.internal;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;

/**
 * <h3>{@link WrappedList}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class WrappedList<T> extends AbstractList<T> {
  private final List<? extends JavaElementSpi> m_spiList;

  public WrappedList(List<? extends JavaElementSpi> spiList) {
    m_spiList = spiList;
  }

  @Override
  public Iterator<T> iterator() {
    final Iterator<? extends JavaElementSpi> it = m_spiList.iterator();
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @SuppressWarnings("unchecked")
      @Override
      public T next() {
        JavaElementSpi s = it.next();
        return s != null ? (T) s.wrap() : null;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public int size() {
    return m_spiList.size();
  }

  @SuppressWarnings("unchecked")
  @Override
  public T get(int index) {
    JavaElementSpi s = m_spiList.get(index);
    return s != null ? (T) s.wrap() : null;
  }

}
