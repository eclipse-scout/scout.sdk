/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js;

import static java.util.Collections.emptyIterator;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.util.Ensure;

public abstract class AbstractScoutJsElementSpliterator<E extends IScoutJsElement> implements Spliterator<E> {

  private final Function<ScoutJsModel, Collection<E>> m_scoutElementsProvider;
  private final Deque<INodeModule> m_dependencyDek;
  private final int m_characteristics;
  private final IES6Class m_widgetClass;
  private Iterator<E> m_currentModelElementsIterator; // points to the elements of the currently active ScoutJsModel

  protected AbstractScoutJsElementSpliterator(ScoutJsModel start, boolean includeStart, boolean includeDependencies, Function<ScoutJsModel, Collection<E>> scoutElementsProvider) {
    m_scoutElementsProvider = Ensure.notNull(scoutElementsProvider);
    m_widgetClass = start.widgetClass();
    if (includeDependencies) {
      m_dependencyDek = start
          .scoutJsDependenciesRecursively()
          .collect(toCollection(ArrayDeque::new));
    }
    else {
      m_dependencyDek = new ArrayDeque<>();
    }
    if (includeStart) {
      m_dependencyDek.addLast(start.nodeModule());
    }
    moveToNextModel();
    if (m_currentModelElementsIterator == null) {
      // when no elements are available: assign empty one so that no null check is necessary in tryAdvance
      m_currentModelElementsIterator = emptyIterator();
    }
    m_characteristics = Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED;
  }

  @Override
  public boolean tryAdvance(Consumer<? super E> consumer) {
    if (!m_currentModelElementsIterator.hasNext()) {
      boolean movedToNextModel;
      do {
        movedToNextModel = moveToNextModel();
      }
      while (movedToNextModel && !m_currentModelElementsIterator.hasNext());

      if (!movedToNextModel) {
        return false;
      }
    }

    consumer.accept(m_currentModelElementsIterator.next());
    return true;
  }

  protected boolean moveToNextModel() {
    if (m_dependencyDek.isEmpty()) {
      m_currentModelElementsIterator = null;
      return false;
    }
    var currentModel = ScoutJsModels.create(m_dependencyDek.removeLast(), m_widgetClass).orElseThrow();
    m_currentModelElementsIterator = m_scoutElementsProvider.apply(currentModel).iterator();

    return true;
  }

  @Override
  public Spliterator<E> trySplit() {
    return null; // no split support needed
  }

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return m_characteristics;
  }
}
