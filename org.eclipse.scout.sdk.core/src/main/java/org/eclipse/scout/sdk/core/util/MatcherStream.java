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

import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Helper class to create a {@link Stream} of a Regex {@link Pattern} execution.
 */
public final class MatcherStream {

  private MatcherStream() {
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
  public static Stream<String> all(Pattern pattern, CharSequence input) {
    return allMatches(pattern, input).map(MatchResult::group);
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
  public static Stream<MatchResult> allMatches(Pattern pattern, CharSequence input) {
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
    };
    return StreamSupport.stream(spliterator, false);
  }
}
