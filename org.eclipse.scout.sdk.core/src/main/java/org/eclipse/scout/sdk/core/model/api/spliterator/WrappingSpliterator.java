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
package org.eclipse.scout.sdk.core.model.api.spliterator;

import java.util.List;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class WrappingSpliterator<T extends IJavaElement> implements Spliterator<T> {

  private final List<? extends JavaElementSpi> m_spiList;
  private int m_end; // exclusive
  private int m_pos;

  public WrappingSpliterator(List<? extends JavaElementSpi> spiList) {
    this(spiList, 0, spiList.size());
  }

  public WrappingSpliterator(List<? extends JavaElementSpi> spiList, int offset, int end) {
    Ensure.instanceOf(spiList, RandomAccess.class);
    m_spiList = spiList;
    m_pos = offset;
    m_end = end;
  }

  public static <X extends IJavaElement> Stream<X> stream(List<? extends JavaElementSpi> spiList) {
    return StreamSupport.stream(new WrappingSpliterator<>(spiList), false);
  }

  @Override
  public boolean tryAdvance(Consumer<? super T> action) {
    if (m_pos >= m_end) {
      return false;
    }
    action.accept(currentElement());
    moveToNext();
    return true;
  }

  protected T currentElement() {
    return element(m_pos);
  }

  @SuppressWarnings("unchecked")
  protected T element(int index) {
    return (T) m_spiList.get(index).wrap();
  }

  protected void moveToNext() {
    m_pos++;
  }

  @Override
  public long getExactSizeIfKnown() {
    return estimateSize();
  }

  @Override
  public void forEachRemaining(Consumer<? super T> action) {
    for (var i = m_pos; i < m_end; i++) {
      action.accept(element(i));
    }
  }

  @Override
  public Spliterator<T> trySplit() {
    var split = (m_end - m_pos) / 2;
    if (split < 1) {
      return null;
    }
    var firstForSplit = m_pos + split;
    Spliterator<T> result = new WrappingSpliterator<>(m_spiList, firstForSplit, m_end);
    m_end = firstForSplit;
    return result;
  }

  @Override
  public long estimateSize() {
    return m_end - m_pos;
  }

  @Override
  public int characteristics() {
    return SuperTypeHierarchySpliterator.DEFAULT_CHARACTERISTICS | SIZED | SUBSIZED;
  }
}
