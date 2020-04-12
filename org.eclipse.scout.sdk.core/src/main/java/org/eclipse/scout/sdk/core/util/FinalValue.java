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
package org.eclipse.scout.sdk.core.util;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * An effectively final value that can be lazily set.
 *
 * @since 6.1.0
 */
public final class FinalValue<VALUE> {

  /**
   * Marker for initial null-value. It prevents an additional boolean member that would track if the final value has
   * already been set.
   */
  private static final Object NULL_VALUE = new Object();

  private final AtomicReference<Object> m_value = new AtomicReference<>(NULL_VALUE);

  /**
   * @return the value or {@code null}, if not set.
   */
  @SuppressWarnings("unchecked")
  public VALUE get() {
    Object value = m_value.get();
    if (value == NULL_VALUE) {
      return null;
    }
    return (VALUE) value;
  }

  /**
   * @return An {@link Optional} with the current value. The returned {@link Optional} is empty if this
   *         {@link FinalValue} has not yet been set or if the set value is {@code null}.
   */
  public Optional<VALUE> opt() {
    return Optional.ofNullable(get());
  }

  /**
   * Sets the specified value as final value, or throws {@link IllegalArgumentException} if already set.
   *
   * @param value
   *          value to set
   * @throws IllegalArgumentException
   *           if a final value is already set.
   */
  public void set(VALUE value) {
    Ensure.same(m_value.get(), NULL_VALUE, "Value already set.");
    setIfAbsent(value);
  }

  /**
   * Sets the specified value as final value, but only if not set yet.
   *
   * @return the final value.
   */
  public VALUE setIfAbsentAndGet(VALUE value) {
    return computeIfAbsentAndGet(() -> value);
  }

  /**
   * Computes the final value with the specified {@link Supplier}, but only if not set yet. It makes the same promises
   * as other concurrent structures (e.g. like {@link ConcurrentMap}): the {@link Supplier} could be executed
   * concurrently by multiple threads but only first available value is used. The {@link Supplier} is responsible for
   * dealing with this fact.
   *
   * @param producer
   *          to produce the final value if no final value is set yet.
   * @return the final value.
   * @throws RuntimeException
   *           if the {@link Supplier} throws an exception
   */
  @SuppressWarnings("unchecked")
  public VALUE computeIfAbsentAndGet(Supplier<VALUE> producer) {
    computeIfAbsent(producer);
    return (VALUE) m_value.get();
  }

  /**
   * Sets the specified value as final value, but only if not set yet.
   *
   * @return {@code true}, if the value was set, {@code false}, if a value already existed.
   */
  public boolean setIfAbsent(VALUE value) {
    return computeIfAbsent(() -> value);
  }

  /**
   * Computes the final value with the specified {@link Supplier}, but only if not set yet. It makes the same promises
   * as other concurrent structures (e.g. like {@link ConcurrentMap}): the {@link Supplier} could be executed
   * concurrently by multiple threads but only first available value is used. The {@link Supplier} is responsible for
   * dealing with this fact.
   *
   * @param producer
   *          to produce the final value if no final value is set yet. Must not be {@code null}.
   * @return {@code true} if the value was set with the given producer. {@code false} if a value already existed.
   * @throws RuntimeException
   *           if the {@link Supplier} throws an exception
   */
  public boolean computeIfAbsent(Supplier<VALUE> producer) {
    Object value = m_value.get();
    return value == NULL_VALUE && m_value.compareAndSet(NULL_VALUE, Ensure.notNull(producer).get());
  }

  /**
   * @return {@code true}, if a final value was set, or else {@code false}.
   */
  public boolean isSet() {
    return m_value.get() != NULL_VALUE;
  }

  @Override
  public String toString() {
    String className = FinalValue.class.getSimpleName();
    if (isSet()) {
      String content = Objects.toString(get());
      return new StringBuilder(className.length() + content.length() + 2).append(className).append('[').append(content).append(']').toString();
    }
    String notSet = "[<not set>]";
    return new StringBuilder(className.length() + notSet.length()).append(className).append(notSet).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(get());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    FinalValue<?> other = (FinalValue<?>) obj;
    return Objects.equals(get(), other.get());
  }
}
