/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.generator.type;

import static org.eclipse.scout.sdk.core.model.api.Flags.isFinal;
import static org.eclipse.scout.sdk.core.model.api.Flags.isPrivate;
import static org.eclipse.scout.sdk.core.model.api.Flags.isPublic;
import static org.eclipse.scout.sdk.core.model.api.Flags.isStatic;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.member.IMemberGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 * <h3>{@link SortedMemberEntry}</h3>
 *
 * @since 6.1.0
 */
public class SortedMemberEntry implements Comparable<SortedMemberEntry> {

  public static final int PARSED_ORDER = 10;
  public static final int DEFAULT_ORDER = 20;
  public static final int FIELD_ORDER = 100000;
  public static final int METHOD_ORDER = 200000;
  public static final int TYPE_ORDER = 300000;
  private static final AtomicLong INPUT_ORDER = new AtomicLong();

  private final IMemberGenerator<?> m_generator;
  private final FinalValue<CompositeObject> m_sortOrder;
  private final long m_index;

  public SortedMemberEntry(IMemberGenerator<?> generator, IJavaElement origin) {
    this(generator,
        PARSED_ORDER, origin.source()
            .map(range -> (long) range.start())
            .orElseGet(INPUT_ORDER::getAndIncrement));
  }

  public SortedMemberEntry(IMemberGenerator<?> generator) {
    this(generator, (Object[]) null);
  }

  public SortedMemberEntry(IMemberGenerator<?> generator, Object... sortOrder) {
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

  public IMemberGenerator<?> generator() {
    return m_generator;
  }

  public CompositeObject sortOrder() {
    return m_sortOrder.computeIfAbsentAndGet(this::calculateDefaultOrder);
  }

  protected CompositeObject calculateDefaultOrder() {
    var generator = generator();
    if (isType()) {
      return defaultTypeOrder((ITypeGenerator<?>) generator, m_index);
    }
    if (isMethod()) {
      return defaultMethodOrder((IMethodGenerator<?, ?>) generator, m_index);
    }
    return defaultFieldOrder((IFieldGenerator<?>) generator, m_index);
  }

  public boolean isMethod() {
    return hasType(IMethodGenerator.class);
  }

  public boolean isType() {
    return hasType(ITypeGenerator.class);
  }

  public boolean isField() {
    return hasType(IFieldGenerator.class);
  }

  public boolean hasType(Class<?> type) {
    return type.isAssignableFrom(generator().getClass());
  }

  @Override
  public int compareTo(SortedMemberEntry o) {
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
    var other = (SortedMemberEntry) obj;
    return sortOrder().equals(other.sortOrder());
  }

  protected static CompositeObject defaultFieldOrder(@SuppressWarnings("TypeMayBeWeakened") IFieldGenerator<?> generator, long insertionOrder) {
    var flags = generator.flags();
    var isFinal = isFinal(flags);
    var isConstant = isStatic(flags) && isFinal;
    var isSerialVersionUid = isConstant && isPrivate(flags) && FieldGenerator.SERIAL_VERSION_UID.equals(generator.elementName().orElse(null));

    int pos;
    if (isSerialVersionUid) {
      pos = 1000;
    }
    else if (isConstant) {
      pos = 2000;
    }
    else if (isFinal) {
      pos = 3000;
    }
    else {
      pos = 4000;
    }

    return new CompositeObject(DEFAULT_ORDER, FIELD_ORDER, pos, insertionOrder);
  }

  protected static CompositeObject defaultTypeOrder(@SuppressWarnings("TypeMayBeWeakened") ITypeGenerator<?> generator, long insertionOrder) {
    int pos;
    var flags = generator.flags();
    if (isPublic(flags)) {
      pos = 1000;
    }
    else if (isStatic(flags)) {
      pos = 3000;
    }
    else {
      pos = 2000;
    }

    return new CompositeObject(DEFAULT_ORDER, TYPE_ORDER, pos, insertionOrder);
  }

  protected static CompositeObject defaultMethodOrder(IMethodGenerator<?, ?> generator, long insertionOrder) {
    int pos;
    if (generator.isConstructor()) {
      pos = 1000;
    }
    else if (isStatic(generator.flags())) {
      pos = 3000;
    }
    else {
      pos = 4000;
    }
    return new CompositeObject(DEFAULT_ORDER, METHOD_ORDER, pos, insertionOrder);
  }

  public static Object[] createDefaultMethodPos(Object... order) {
    return combine(METHOD_ORDER, order);
  }

  public static Object[] createDefaultTypePos(Object... order) {
    return combine(TYPE_ORDER, order);
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
