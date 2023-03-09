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

import static java.util.stream.Collectors.toCollection;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;

public class ScoutJsObjectSpliterator implements Spliterator<IScoutJsObject> {

  private final Deque<INodeModule> m_dependencyDek;
  private final int m_characteristics;

  private ScoutJsModel m_currentModel;
  private Iterator<IScoutJsObject> m_currentModelObjectsIterator; // points to the objects of the currently active ScoutJsModel

  public ScoutJsObjectSpliterator(ScoutJsModel model, boolean includeDependencies) {
    m_currentModel = model;
    if (includeDependencies) {
      m_dependencyDek = model
          .scoutJsDependenciesRecursively()
          .collect(toCollection(ArrayDeque::new));
    }
    else {
      m_dependencyDek = null;
    }
    m_currentModelObjectsIterator = model.exportedScoutObjects().values().iterator();

    var characteristics = Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED;
    if (!includeDependencies) {
      characteristics |= (Spliterator.SIZED | Spliterator.SUBSIZED);
    }
    m_characteristics = characteristics;
  }

  @Override
  public boolean tryAdvance(Consumer<? super IScoutJsObject> consumer) {
    if (!m_currentModelObjectsIterator.hasNext()) {
      if (m_dependencyDek != null) {
        boolean movedToNextModel;
        do {
          movedToNextModel = moveToNextModel();
        }
        while (movedToNextModel && !m_currentModelObjectsIterator.hasNext());

        if (!movedToNextModel) {
          return false;
        }
      }
      else {
        return false; // do not dive deep into dependencies: therefore abort here
      }
    }

    consumer.accept(m_currentModelObjectsIterator.next());
    return true;
  }

  protected boolean moveToNextModel() {
    if (m_dependencyDek.isEmpty()) {
      m_currentModel = null;
      m_currentModelObjectsIterator = null;
      return false;
    }
    var widgetClass = m_currentModel.widgetClass();
    m_currentModel = ScoutJsModels.create(m_dependencyDek.removeLast(), widgetClass).orElseThrow();
    m_currentModelObjectsIterator = m_currentModel.exportedScoutObjects().values().iterator();

    return true;
  }

  @Override
  public Spliterator<IScoutJsObject> trySplit() {
    return null; // no split support needed
  }

  @Override
  public long estimateSize() {
    if (hasCharacteristics(SIZED)) {
      // no recursion -> exact number of elements known.
      return m_currentModel.exportedScoutObjects().size();
    }
    return Long.MAX_VALUE; // having recursion the size is unknown as we don't know the number of grand-children yet
  }

  @Override
  public int characteristics() {
    return m_characteristics;
  }
}
