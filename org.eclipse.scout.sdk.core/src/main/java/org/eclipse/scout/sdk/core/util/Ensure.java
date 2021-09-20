/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scout.sdk.core.log.MessageFormatter;

/**
 * <h3>{@link Ensure}</h3>
 *
 * @since 6.1.0
 */
public final class Ensure {

  private Ensure() {
  }

  /**
   * Ensures that the given expression is {@code true}. Throws an {@link IllegalArgumentException} otherwise.
   *
   * @param expression
   *          The expression to check.
   * @throws IllegalArgumentException
   *           if the given expression is {@code false}.
   */
  public static void isTrue(boolean expression) {
    isTrue(expression, "The validated expression is false");
  }

  /**
   * Ensures that the given expression is {@code true}. Throws an {@link IllegalArgumentException} otherwise.
   *
   * @param expression
   *          The expression to check.
   * @param msg
   *          The message to show if the assertion fails.
   * @param msgArgs
   *          The arguments to be placed into the formatting anchors of the given message. See {@link MessageFormatter}.
   * @throws IllegalArgumentException
   *           if the given expression is {@code false}.
   */
  public static void isTrue(boolean expression, CharSequence msg, Object... msgArgs) {
    if (!expression) {
      fail(msg, msgArgs);
    }
  }

  /**
   * Ensures that the given expression is {@code false}. Throws an {@link IllegalArgumentException} otherwise.
   *
   * @param expression
   *          The expression to check.
   * @throws IllegalArgumentException
   *           if the given expression is {@code true}.
   */
  public static void isFalse(boolean expression) {
    isFalse(expression, "The validated expression is true");
  }

  /**
   * Ensures that the given expression is {@code false}. Throws an {@link IllegalArgumentException} otherwise.
   *
   * @param expression
   *          The expression to check.
   * @param msg
   *          The message to show if the assertion fails.
   * @param msgArgs
   *          The arguments to be placed into the formatting anchors of the given message. See {@link MessageFormatter}.
   * @throws IllegalArgumentException
   *           if the given expression is {@code true}.
   */
  public static void isFalse(boolean expression, CharSequence msg, Object... msgArgs) {
    isTrue(!expression, msg, msgArgs);
  }

  /**
   * Ensures that the given {@link Path} points to an existing file.
   *
   * @param candidate
   *          The {@link Path} to check.
   * @return The given {@link Path} if it is a file.
   * @throws IllegalArgumentException
   *           if the given {@link Path} is {@code null} or not a file.
   */
  public static Path isFile(Path candidate) {
    return isFile(candidate, "'{}' is not a file.", candidate);
  }

  /**
   * Ensures that the given {@link Path} points to an existing file.
   *
   * @param candidate
   *          The {@link Path} to check.
   * @param msg
   *          The message to show if the assertion fails.
   * @param msgArgs
   *          The arguments to be placed into the formatting anchors of the given message. See {@link MessageFormatter}.
   * @return The given {@link Path} if it is a file.
   * @throws IllegalArgumentException
   *           if the given {@link Path} is {@code null} or not a file.
   */
  public static Path isFile(Path candidate, CharSequence msg, Object... msgArgs) {
    if (candidate == null || !Files.isReadable(candidate) || !Files.isRegularFile(candidate)) {
      fail(msg, msgArgs);
    }
    return candidate;
  }

  /**
   * Ensures that the given {@link Path} points to an existing directory.
   *
   * @param candidate
   *          The {@link Path} to check.
   * @return The given {@link Path} if it is a directory.
   * @throws IllegalArgumentException
   *           if the given {@link Path} is {@code null} or not a directory.
   */
  public static Path isDirectory(Path candidate) {
    return isDirectory(candidate, "'{}' is not a directory.", candidate);
  }

  /**
   * Ensures that the given {@link Path} points to an existing directory.
   *
   * @param candidate
   *          The {@link Path} to check.
   * @param msg
   *          The message to show if the assertion fails.
   * @param msgArgs
   *          The arguments to be placed into the formatting anchors of the given message. See {@link MessageFormatter}.
   * @return The given {@link Path} if it is a directory.
   * @throws IllegalArgumentException
   *           if the given {@link Path} is {@code null} or not a directory.
   */
  public static Path isDirectory(Path candidate, CharSequence msg, Object... msgArgs) {
    if (candidate == null || !Files.isReadable(candidate) || !Files.isDirectory(candidate)) {
      fail(msg, msgArgs);
    }
    return candidate;
  }

  /**
   * Ensures that the given object is not {@code null}.<br>
   * <br>
   * <b>Note:</b><br>
   * This method differs to {@link Objects#requireNonNull(Object)} in the way that it does not throw a
   * {@link NullPointerException} but an {@link IllegalArgumentException} instead.
   *
   * @param o
   *          The object to check.
   * @return The given object if it is not {@code null}.
   * @throws IllegalArgumentException
   *           if the object is {@code null}.
   */
  public static <T> T notNull(T o) {
    return notNull(o, "Object is null");
  }

  /**
   * Ensures that the given object is not {@code null}.<br>
   * <br>
   * <b>Note:</b><br>
   * This method differs to {@link Objects#requireNonNull(Object, String)} in the way that it does not throw a
   * {@link NullPointerException} but an {@link IllegalArgumentException} instead.
   *
   * @param o
   *          The object to check.
   * @param msg
   *          The message to show if the object is {@code null}.
   * @param msgArgs
   *          The arguments to be placed into the formatting anchors of the given message. See {@link MessageFormatter}.
   * @return The given object if it is not {@code null}.
   * @throws IllegalArgumentException
   *           if the object is {@code null}.
   */
  public static <T> T notNull(T o, CharSequence msg, Object... msgArgs) {
    if (o == null) {
      fail(msg, msgArgs);
    }
    return o;
  }

  /**
   * Ensures that the given {@link CharSequence} contains visible characters (is not blank) according to
   * {@link Strings#isBlank(CharSequence)}.
   *
   * @param in
   *          The {@link CharSequence} to check.
   * @return The given {@link CharSequence} if it is not blank.
   * @throws IllegalArgumentException
   *           if the given {@link CharSequence} is blank.
   */
  public static <T extends CharSequence> T notBlank(T in) {
    return notBlank(in, "CharSequence is blank but expected to contain characters.", (Object[]) null);
  }

  /**
   * Ensures that the given {@link CharSequence} contains visible characters (is not blank) according to
   * {@link Strings#isBlank(CharSequence)}.
   *
   * @param in
   *          The {@link CharSequence} to check.
   * @param msg
   *          The message to show if the assertion fails.
   * @param msgArgs
   *          The arguments to be placed into the formatting anchors of the given message. See {@link MessageFormatter}.
   * @return The given {@link CharSequence} if it is not blank.
   * @throws IllegalArgumentException
   *           if the given {@link CharSequence} is blank.
   * @see Strings#isBlank(CharSequence)
   * @see Strings#notBlank(CharSequence)
   */
  public static <T extends CharSequence> T notBlank(T in, CharSequence msg, Object... msgArgs) {
    if (Strings.isBlank(in)) {
      fail(msg, msgArgs);
    }
    return in;
  }

  /**
   * Ensures that both parameters are the same instance or are both {@code null}.
   *
   * @param a
   *          the value to be tested.
   * @param b
   *          the value to be tested against.
   * @throws IllegalArgumentException
   *           if a is not same as b.
   */
  public static void same(Object a, Object b) {
    same(a, b, "Values are not the same: [{}, {}].", a, b);
  }

  /**
   * Ensures that both parameters are the same instance or are both {@code null}.
   *
   * @param a
   *          the value to be tested.
   * @param b
   *          the value to be tested against.
   * @param msg
   *          message if the assertion fails.
   * @param msgArgs
   *          The arguments to be placed into the formatting anchors of the given message. See {@link MessageFormatter}.
   * @throws IllegalArgumentException
   *           if a is not same as b.
   */
  public static void same(Object a, Object b, CharSequence msg, Object... msgArgs) {
    if (a != b) {
      fail(msg, msgArgs);
    }
  }

  /**
   * Ensures that the given object is {@code instanceof} the given class.
   *
   * @param a
   *          The object to test.
   * @param type
   *          The type to test against.
   * @return The given object casted to the given type.
   * @throws IllegalArgumentException
   *           if the object or the type is {@code null} or if the object is not {@code instanceof} the given type.
   * @see Class#cast(Object)
   * @see MessageFormatter
   */
  public static <T> T instanceOf(Object a, Class<T> type) {
    return instanceOf(a, type, "Object of type {} is not instanceof {}.", notNull(a).getClass(), type);
  }

  /**
   * Ensures that the given object is {@code instanceof} the given class.
   *
   * @param a
   *          The object to test.
   * @param type
   *          The type to test against.
   * @param msg
   *          if the assertion fails.
   * @param msgArgs
   *          The arguments to be placed into the formatting anchors of the given message. See {@link MessageFormatter}.
   * @return The given object casted to the given type.
   * @throws IllegalArgumentException
   *           if the object or the type is {@code null} or if the object is not {@code instanceof} the given type.
   * @see Class#cast(Object)
   * @see MessageFormatter
   */
  public static <T> T instanceOf(Object a, Class<T> type, CharSequence msg, Object... msgArgs) {
    if (!notNull(type).isAssignableFrom(notNull(a).getClass())) {
      fail(msg, msgArgs);
    }
    return type.cast(a);
  }

  /**
   * To always throw an {@code IllegalArgumentException}.
   *
   * @param msg
   *          the message of the {@link IllegalArgumentException}.
   * @param msgArgs
   *          The arguments to be placed into the formatting anchors of the given message. See {@link MessageFormatter}.
   * @throws IllegalArgumentException
   *           on any call to that method.
   */
  public static void fail(CharSequence msg, Object... msgArgs) {
    throw newFail(msg, msgArgs);
  }

  /**
   * Creates a new failure {@link IllegalArgumentException} with given message and arguments.
   * <p>
   * This method may be useful to manually throw as shown in the following example:<br>
   *
   * <pre>
   * private int method() {
   *   if (condition) {
   *     return value;
   *   }
   *   throw newFail("condition not met");
   * }
   * </pre>
   *
   * @param msg
   *          the message of the {@link IllegalArgumentException}.
   * @param msgArgs
   *          The arguments to be placed into the formatting anchors of the given message. See {@link MessageFormatter}.
   * @return A new {@link IllegalArgumentException} with given message.
   */
  public static IllegalArgumentException newFail(CharSequence msg, Object... msgArgs) {
    var tuple = MessageFormatter.arrayFormat(msg, msgArgs);
    return new IllegalArgumentException(tuple.message(), tuple.firstThrowable().orElse(null));
  }

  /**
   * Represents a merge function, suitable for use in {@link Map#merge(Object, Object, BiFunction) Map.merge()} or
   * {@link Collectors#toMap(Function, Function, BinaryOperator) toMap()}, which always throws
   * {@code IllegalArgumentException}.
   * <p>
   * This can be used to enforce the assumption that the elements being collected are distinct.
   *
   * @return a merge function which always throw {@code IllegalArgumentException}
   */
  public static <T> T failOnDuplicates(T a, T b) {
    throw newFail("Unexpected duplicates found: '{}' and '{}'.", a, b);
  }
}
