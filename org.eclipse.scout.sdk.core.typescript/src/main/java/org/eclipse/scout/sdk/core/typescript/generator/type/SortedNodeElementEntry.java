/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator.type;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.sdk.core.typescript.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.INodeElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 * <h3>{@link SortedNodeElementEntry}</h3>
 *
 * @since 13.0
 */
public class SortedNodeElementEntry implements Comparable<SortedNodeElementEntry> {

  public static final int DEFAULT_ORDER = 20;
  public static final int FIELD_ORDER = 100000;
  private static final AtomicLong INPUT_ORDER = new AtomicLong();

  private final INodeElementGenerator<?> m_generator;
  private final FinalValue<CompositeObject> m_sortOrder;
  private final long m_index;

  public SortedNodeElementEntry(INodeElementGenerator<?> generator) {
    this(generator, (Object[]) null);
  }

  public SortedNodeElementEntry(INodeElementGenerator<?> generator, Object... sortOrder) {
    m_generator = Ensure.notNull(generator);
    m_sortOrder = new FinalValue<>();
    if (sortOrder != null && sortOrder.length > 0) {
      m_sortOrder.set(new CompositeObject(sortOrder));
      m_index = 0;
    }
    else {
      m_index = INPUT_ORDER.getAndIncrement();
    }
  }

  public INodeElementGenerator<?> generator() {
    return m_generator;
  }

  public CompositeObject sortOrder() {
    return m_sortOrder.computeIfAbsentAndGet(this::calculateDefaultOrder);
  }

  protected CompositeObject calculateDefaultOrder() {
    var generator = generator();
    if (isField()) {
      return defaultFieldOrder((IFieldGenerator<?>) generator, m_index);
    }
    return new CompositeObject();
  }

  public boolean isField() {
    return hasType(IFieldGenerator.class);
  }

  public boolean hasType(Class<?> type) {
    return type.isAssignableFrom(generator().getClass());
  }

  @Override
  public int compareTo(SortedNodeElementEntry o) {
    return sortOrder().compareTo(o.sortOrder());
  }

  @Override
  public int hashCode() {
    return sortOrder().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    var other = (SortedNodeElementEntry) obj;
    return sortOrder().equals(other.sortOrder());
  }

  protected static CompositeObject defaultFieldOrder(@SuppressWarnings("TypeMayBeWeakened") IFieldGenerator<?> generator, long insertionOrder) {
    var modifiers = generator.modifiers();
    var isDeclare = modifiers.contains(Modifier.DECLARE);

    int pos;
    if (isDeclare) {
      pos = 1000;
    }
    else {
      pos = 2000;
    }

    return new CompositeObject(DEFAULT_ORDER, FIELD_ORDER, pos, insertionOrder);
  }

  public static Object[] createDefaultFieldPos(Object... order) {
    return combine(FIELD_ORDER, order);
  }

  static Object[] combine(int objectOrder, Object... orders) {
    if (orders == null || orders.length < 1) {
      return new Object[]{DEFAULT_ORDER, objectOrder};
    }

    var result = new Object[orders.length + 2];
    result[0] = DEFAULT_ORDER;
    result[1] = objectOrder;
    System.arraycopy(orders, 0, result, 2, orders.length);
    return result;
  }
}
