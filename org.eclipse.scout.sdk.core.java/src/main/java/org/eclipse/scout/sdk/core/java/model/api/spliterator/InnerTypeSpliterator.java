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
import java.util.Spliterator;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SuperHierarchySpliterator;

/**
 * <h3>{@link InnerTypeSpliterator}</h3>
 *
 * @since 6.1.0
 */
public class InnerTypeSpliterator implements Spliterator<IType> {

  private final boolean m_includeInnerTypesRecursive;
  private final int m_characteristics;
  private final Deque<IType> m_dek;

  private Spliterator<IType> m_innerTypes;

  public InnerTypeSpliterator(IType innerTypeHolder, boolean includeInnerTypesRecursive) {
    this(innerTypesOf(Ensure.notNull(innerTypeHolder)), includeInnerTypesRecursive);
  }

  public InnerTypeSpliterator(Spliterator<IType> innerTypesFirstLevel, boolean includeInnerTypesRecursive) {
    m_includeInnerTypesRecursive = includeInnerTypesRecursive;
    m_characteristics = SuperHierarchySpliterator.getCharacteristics(!includeInnerTypesRecursive);
    if (includeInnerTypesRecursive) {
      m_dek = new ArrayDeque<>();
    }
    else {
      m_dek = null;
    }
    m_innerTypes = innerTypesFirstLevel;
  }

  public static Spliterator<IType> innerTypesOf(@SuppressWarnings("TypeMayBeWeakened") IType focusType) {
    return new WrappingSpliterator<>(focusType.unwrap().getTypes());
  }

  @Override
  public boolean tryAdvance(Consumer<? super IType> action) {
    var consumed = consume(action);
    if (isIncludeInnerTypesRecursive()) {
      while (!consumed && tryMoveToNextInnerType()) {
        consumed = consume(action);
      }
    }
    return consumed;
  }

  protected boolean tryMoveToNextInnerType() {
    if (m_dek.isEmpty()) {
      return false;
    }
    moveToInnerTypesOf(m_dek.removeFirst());
    return true;
  }

  protected void moveToInnerTypesOf(IType newFocusType) {
    m_innerTypes = innerTypesOf(newFocusType);
  }

  protected boolean consume(Consumer<? super IType> action) {
    return m_innerTypes.tryAdvance(t -> {
      if (isIncludeInnerTypesRecursive()) {
        m_dek.addLast(t);
      }
      action.accept(t);
    });
  }

  @Override
  public Spliterator<IType> trySplit() {
    return null; // no split support needed
  }

  @Override
  public long estimateSize() {
    if (hasCharacteristics(SIZED)) {
      // we have no recursion -> we know the exact number of remaining elements.
      return m_innerTypes.estimateSize();
    }
    return Long.MAX_VALUE; // we should not get here as this method will not be called on unsized spliterators
  }

  @Override
  public int characteristics() {
    return m_characteristics;
  }

  public boolean isIncludeInnerTypesRecursive() {
    return m_includeInnerTypesRecursive;
  }
}
