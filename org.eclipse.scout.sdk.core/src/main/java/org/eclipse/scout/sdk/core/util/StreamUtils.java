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

import static java.util.stream.StreamSupport.stream;

import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Helper class to create a {@link Stream} of a Regex {@link Pattern} execution.
 */
public final class StreamUtils {

  private StreamUtils() {
  }

  /**
   * Creates a {@link Stream} of all matches of the the {@link Pattern} specified in the input {@link CharSequence}
   * specified.
   * 
   * @param pattern
   *          The {@link Pattern} to execute. Must not be {@code null}.
   * @param input
   *          The {@link CharSequence} on which the pattern should be executed. Must not be {@code null}.
   * @return A {@link Stream} with all findings (returns the main group of the pattern {@link MatchResult#group()}).
   */
  public static Stream<String> allMatches(Pattern pattern, CharSequence input) {
    return allMatchResults(pattern, input).map(MatchResult::group);
  }

  /**
   * Creates a {@link Stream} of all matches of the the {@link Pattern} specified in the input {@link CharSequence}
   * specified.
   * 
   * @param pattern
   *          The {@link Pattern} to execute. Must not be {@code null}.
   * @param input
   *          The {@link CharSequence} on which the pattern should be executed. Must not be {@code null}.
   * @return A {@link Stream} with all {@link MatchResult results} of the pattern in the input.
   */
  public static Stream<MatchResult> allMatchResults(Pattern pattern, CharSequence input) {
    Matcher matcher = pattern.matcher(input);
    Spliterator<MatchResult> spliterator = new AbstractSpliterator<MatchResult>(Long.MAX_VALUE,
        Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE) {
      @Override
      public boolean tryAdvance(Consumer<? super MatchResult> action) {
        if (!matcher.find()) {
          return false;
        }
        action.accept(matcher.toMatchResult());
        return true;
      }

      @Override
      public void forEachRemaining(Consumer<? super MatchResult> action) {
        while (matcher.find()) {
          action.accept(matcher.toMatchResult());
        }
      }
    };
    return stream(spliterator, false);
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
    Spliterator<T> spliterator = new AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {
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
