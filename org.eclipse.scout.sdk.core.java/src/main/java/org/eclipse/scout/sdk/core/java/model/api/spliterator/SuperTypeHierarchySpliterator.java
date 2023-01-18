/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.spliterator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link SuperTypeHierarchySpliterator}</h3>
 *
 * @since 6.1.0
 */
public class SuperTypeHierarchySpliterator implements Spliterator<IType> {

  public static final int DEFAULT_CHARACTERISTICS = Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED;

  private final boolean m_includeSuperClasses;
  private final boolean m_includeSuperInterfaces;
  private final boolean m_includeStartType;
  private final int m_characteristics;

  private final Deque<IType> m_dek;
  private final Set<IType> m_visitedSuperInterfaces;
  private final IType m_startType;

  public SuperTypeHierarchySpliterator(IType startType, boolean includeSuperClasses, boolean includeSuperInterfaces, boolean includeStartType) {
    m_startType = Ensure.notNull(startType);

    m_includeStartType = includeStartType;
    m_includeSuperClasses = includeSuperClasses;
    m_includeSuperInterfaces = includeSuperInterfaces;

    var isSizeKnown = !includeSuperClasses && !includeSuperInterfaces;
    m_characteristics = getCharacteristics(isSizeKnown);
    if (isSizeKnown) {
      m_dek = new ArrayDeque<>(1);
    }
    else {
      m_dek = new ArrayDeque<>();
    }
    if (includeSuperInterfaces) {
      m_visitedSuperInterfaces = new HashSet<>();
    }
    else {
      m_visitedSuperInterfaces = null;
    }

    if (includeStartType) {
      m_dek.addLast(startType);
    }
    else {
      enqueueSuperLevelsOf(startType);
    }
  }

  protected static int getCharacteristics(boolean isSizeKnown) {
    var characteristics = DEFAULT_CHARACTERISTICS;
    if (isSizeKnown) {
      characteristics |= (Spliterator.SIZED | Spliterator.SUBSIZED);
    }
    return characteristics;
  }

  @Override
  public boolean tryAdvance(Consumer<? super IType> action) {
    moveToNextValidElement();
    if (!isDataAvailable()) {
      return false;
    }
    consumeNext(action);
    return true;
  }

  protected void consumeNext(Consumer<? super IType> action) {
    action.accept(moveToNext());
  }

  @SuppressWarnings("ConstantConditions")
  protected void moveToNextValidElement() {
    while (isDataAvailable()) {
      var next = m_dek.peekFirst();
      var ifcAccepted = isIncludeSuperInterfaces() && next.isInterface();
      var superClassAccepted = isIncludeSuperClasses() && !next.isInterface();
      var selfAccepted = isIncludeStartType() && next == m_startType;
      if (ifcAccepted || superClassAccepted || selfAccepted) {
        return;
      }
      moveToNext();
    }
  }

  protected boolean isDataAvailable() {
    return !m_dek.isEmpty();
  }

  protected IType moveToNext() {
    var curElement = m_dek.removeFirst();
    enqueueSuperLevelsOf(curElement);
    return curElement;
  }

  protected void enqueueSuperLevelsOf(IType curLevel) {
    // we must always add super-classes to find the super interfaces of them even if we are only interested in interfaces
    if (!curLevel.isInterface()) { // super-class of an interface is java.lang.Object -> ignore
      curLevel.superClass().ifPresent(m_dek::addLast);
    }

    if (!isIncludeSuperInterfaces()) {
      return;
    }
    curLevel.superInterfaces()
        .filter(m_visitedSuperInterfaces::add)
        .forEach(m_dek::addLast);
  }

  @Override
  public Spliterator<IType> trySplit() {
    return null; // no split support needed
  }

  @Override
  public long estimateSize() {
    if (hasCharacteristics(SIZED)) {
      if (isIncludeStartType()) {
        return 1;
      }
      return 0;
    }
    return Long.MAX_VALUE; // we should not get here as this method will not be called on unsized spliterators
  }

  @Override
  public int characteristics() {
    return m_characteristics;
  }

  protected boolean isIncludeSuperClasses() {
    return m_includeSuperClasses;
  }

  protected boolean isIncludeSuperInterfaces() {
    return m_includeSuperInterfaces;
  }

  protected boolean isIncludeStartType() {
    return m_includeStartType;
  }
}
