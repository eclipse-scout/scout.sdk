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

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.model.api.IType;

/**
 * <h3>{@link HierarchicalSpliterator}</h3>
 *
 * @since 6.1.0
 */
public class HierarchicalSpliterator<ELEMENT> implements Spliterator<ELEMENT> {

  private final Spliterator<IType> m_hierarchySpliterator;
  private final Consumer<? super IType> m_toNextLevelType;
  private final boolean m_oneLevel;

  private Spliterator<ELEMENT> m_levelSpliterator;

  HierarchicalSpliterator(IType startType, boolean includeSuperClasses, boolean includeSuperInterfaces, boolean includeStartType, Function<IType, Spliterator<ELEMENT>> levelSpliteratorProvider) {
    m_hierarchySpliterator = new SuperTypeHierarchySpliterator(startType, includeSuperClasses, includeSuperInterfaces, includeStartType);
    m_toNextLevelType = nextType -> m_levelSpliterator = levelSpliteratorProvider.apply(nextType);
    m_oneLevel = !includeSuperClasses && !includeSuperInterfaces && includeStartType;
    moveToNextType();
  }

  @Override
  public boolean tryAdvance(Consumer<? super ELEMENT> action) {
    boolean dataAvailable;
    do {
      dataAvailable = m_levelSpliterator != null && m_levelSpliterator.tryAdvance(action);
      if (!dataAvailable) {
        boolean classFound = moveToNextType();
        if (!classFound) {
          return false;
        }
      }
    }
    while (!dataAvailable);
    return true;
  }

  protected boolean moveToNextType() {
    return m_hierarchySpliterator.tryAdvance(m_toNextLevelType);
  }

  @Override
  public Spliterator<ELEMENT> trySplit() {
    return null; // no split support needed
  }

  @Override
  public long estimateSize() {
    if (isOneLevel()) {
      return m_levelSpliterator.estimateSize();
    }
    return m_hierarchySpliterator.estimateSize();
  }

  @Override
  public int characteristics() {
    if (isOneLevel()) {
      // we have one level only -> use the characteristics of the level spliterator
      return m_levelSpliterator.characteristics();
    }
    return m_hierarchySpliterator.characteristics();
  }

  protected boolean isOneLevel() {
    return m_oneLevel;
  }
}
