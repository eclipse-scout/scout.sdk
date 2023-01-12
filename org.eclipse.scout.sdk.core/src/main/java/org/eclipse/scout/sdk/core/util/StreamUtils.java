/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.util;

import static java.util.stream.StreamSupport.stream;

import java.util.Enumeration;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class StreamUtils {

  private StreamUtils() {
  }

  /**
   * Returns a {@link Predicate} only accepting the first element as returned by the given selector function.
   * 
   * @param selector
   *          The {@link Function} to apply to elements to compute the attribute to compare. Must not be {@code null}.
   * @param <T>
   *          The element type
   * @return A {@link Predicate} only accepting the first element according to the attribute of the element returned by
   *         the selector.
   * @throws IllegalArgumentException
   *           if the selector is {@code null}.
   */
  public static <T> Predicate<T> firstBy(Function<? super T, ?> selector) {
    Ensure.notNull(selector);
    Map<Object, Boolean> seen = new ConcurrentHashMap<>();
    return t -> seen.putIfAbsent(selector.apply(t), Boolean.TRUE) == null;
  }

  /**
   * Converts the given {@link Enumeration} into a {@link Stream}. The {@link Enumeration} is evaluated lazy by the
   * {@link Stream}.<br>
   * Because {@link Enumeration Enumerations} cannot be reset a fresh instance should be passed to this method.
   * Otherwise the resulting {@link Stream} only processes the remaining elements of the {@link Enumeration}.
   * 
   * @param e
   *          The {@link Enumeration} to convert. Must not be {@code null}.
   * @return A non-parallel, ordered {@link Stream} backed by the {@link Enumeration} given.
   */
  public static <T> Stream<T> toStream(Enumeration<T> e) {
    Ensure.notNull(e);
    Spliterator<T> spliterator = new AbstractSpliterator<>(Long.MAX_VALUE, Spliterator.ORDERED) {
      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
        if (!e.hasMoreElements()) {
          return false;
        }
        action.accept(e.nextElement());
        return true;
      }

      @Override
      public void forEachRemaining(Consumer<? super T> action) {
        while (e.hasMoreElements()) {
          action.accept(e.nextElement());
        }
      }
    };
    return stream(spliterator, false);
  }
}
