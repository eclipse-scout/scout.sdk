/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.spliterator;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.typescript.model.spi.NodeModuleSpi;

public class PackageJsonDependencySpliterator implements Spliterator<NodeModuleSpi> {

  private final boolean m_recursive;
  private final int m_characteristics;
  private final Deque<NodeModuleSpi> m_dek;
  private final Set<NodeModuleSpi> m_dependenciesAddedToDek;
  private boolean m_withSelf;

  public PackageJsonDependencySpliterator(Collection<NodeModuleSpi> firstLevelDependencies, boolean recursive, boolean withSelf) {
    m_recursive = recursive;
    m_withSelf = withSelf;
    m_dek = new ArrayDeque<>(firstLevelDependencies);
    m_dependenciesAddedToDek = new HashSet<>(firstLevelDependencies);

    var characteristics = Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED;
    if (!recursive && !withSelf) {
      characteristics |= (Spliterator.SIZED | Spliterator.SUBSIZED);
    }
    m_characteristics = characteristics;
  }

  @Override
  public boolean tryAdvance(Consumer<? super NodeModuleSpi> consumer) {
    if (m_dek.isEmpty()) {
      return false;
    }
    var current = m_dek.removeFirst();
    consumer.accept(current);
    if (isRecursive() || isWithSelf()) {
      current.packageJson()
          .dependencies().stream()
          .filter(m_dependenciesAddedToDek::add)
          .forEach(m_dek::addLast);
      if (isWithSelf()) {
        // self-dependencies have been processed: remove
        m_withSelf = false;
      }
    }
    return true;
  }

  public boolean isRecursive() {
    return m_recursive;
  }

  public boolean isWithSelf() {
    return m_withSelf;
  }

  @Override
  public Spliterator<NodeModuleSpi> trySplit() {
    return null; // no split support needed
  }

  @Override
  public long estimateSize() {
    if (hasCharacteristics(SIZED)) {
      // no recursion -> exact number of elements known.
      return m_dek.size();
    }
    return Long.MAX_VALUE; // having recursion the size is unknown as we don't know the number of grand-children yet
  }

  @Override
  public int characteristics() {
    return m_characteristics;
  }
}
