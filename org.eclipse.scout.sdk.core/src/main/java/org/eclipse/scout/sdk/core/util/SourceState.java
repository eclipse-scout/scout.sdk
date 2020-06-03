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

import static java.nio.CharBuffer.wrap;

/**
 * Utility class to determine the state of a certain position in Java or JavaScript sources.<br/>
 * It is possible to query if a certain index is within a comment (line or block) or within a string or char literal.
 */
@SuppressWarnings("HardcodedLineSeparator")
public final class SourceState {

  private SourceState() {
  }

  private interface IState {
    IState next(char c);

    State toState();
  }

  private static final class InLineCommentState implements IState {
    private static final IState INSTANCE = new InLineCommentState();

    @Override
    public IState next(char c) {
      if (c == '\n') {
        return DefaultState.INSTANCE;
      }
      return this;
    }

    @Override
    public State toState() {
      return State.IN_LINE_COMMENT;
    }
  }

  private static final class InBlockCommentState implements IState {
    private static final IState INSTANCE = new InBlockCommentState();

    @Override
    public IState next(char c) {
      if (c == '*') {
        return AboutToExitBlockCommentState.INSTANCE;
      }
      return this;
    }

    @Override
    public State toState() {
      return State.IN_BLOCK_COMMENT;
    }
  }

  private static final class AboutToExitBlockCommentState implements IState {
    private static final IState INSTANCE = new AboutToExitBlockCommentState();

    @Override
    public IState next(char c) {
      switch (c) {
        case '*':
          return this;
        case '/':
          return DefaultState.INSTANCE;
        default:
          return InBlockCommentState.INSTANCE;
      }
    }

    @Override
    public State toState() {
      return State.ABOUT_TO_EXIT_BLOCK_COMMENT;
    }
  }

  private static final class AboutToEnterCommentState implements IState {
    private static final IState INSTANCE = new AboutToEnterCommentState();

    @Override
    public IState next(char c) {
      switch (c) {
        case '*':
          return InBlockCommentState.INSTANCE;
        case '/':
          return InLineCommentState.INSTANCE;
        default:
          return DefaultState.INSTANCE;
      }
    }

    @Override
    public State toState() {
      return State.ABOUT_TO_ENTER_COMMENT;
    }
  }

  private static final class EscapeStringState implements IState {

    private final char m_delimiter;

    private EscapeStringState(char delimiter) {
      m_delimiter = delimiter;
    }

    @Override
    public IState next(char c) {
      return new InStringState(m_delimiter);
    }

    @Override
    public State toState() {
      return State.ESCAPE_STRING;
    }
  }

  private static final class InStringState implements IState {

    private final char m_delimiter;

    private InStringState(char delimiter) {
      m_delimiter = delimiter;
    }

    @Override
    public IState next(char c) {
      if (c == m_delimiter) {
        return DefaultState.INSTANCE;
      }
      if (c == '\\') {
        return new EscapeStringState(m_delimiter);
      }
      return this;
    }

    @Override
    public State toState() {
      return State.IN_STRING;
    }
  }

  private static final class DefaultState implements IState {
    private static final IState INSTANCE = new DefaultState();

    @Override
    public IState next(char c) {
      switch (c) {
        case '"':
        case '`':
        case '\'':
          return new InStringState(c);
        case '/':
          return AboutToEnterCommentState.INSTANCE;
        default:
          return this;
      }
    }

    @Override
    public State toState() {
      return State.DEFAULT;
    }
  }

  /**
   * Describes the state of a character position within Java or JavaScript source.
   */
  public enum State {
    /**
     * Describes positions within a single line comment. The single line comment in the source starts with // and ends
     * with a newline.<br/>
     * The InLineComment source state is valid starting at the second slash and ends at the last char before the
     * terminating \n.<br/>
     * So the terminating \n is no longer part of the InLineComment state (it is Default state instead) but a \r just
     * before still is.<br/>
     * The first slash is NOT part of InLineComment state but it is the AboutToEnterComment state instead. Please note
     * that this state may also be a simple division within the code and is therefore no comment state.
     */
    IN_LINE_COMMENT,
    /**
     * Describes positions within a multi or single line block comment. Such comments start with /* and end with
     * &#42;/.<br/>
     * The InBlockComment state is valid from star to star (excluded are stars in between, which might already be an
     * ending indicator and therefore belong to AboutToExitBlockComment). While the starting slash is
     * AboutToEnterComment state the ending slash is already Default state.
     */
    IN_BLOCK_COMMENT,
    /**
     * Indicates a * character within a block comment: /* ** &#42;/. The two stars in between belong to the state
     * AboutToExitBlockComment. But as no slash follows it stays in the AboutToExitBlockComment state or turns into
     * InBlockComment again.<br>
     * So state AboutToExitBlockComment also indicates to be within a block comment (like InBlockComment does).
     */
    ABOUT_TO_EXIT_BLOCK_COMMENT,
    /**
     * Indicates a / character which is not within a string or char literal and ist not within a comment. This state
     * might indicate that a comment is about to begin or it could also belong to e.g. a division.
     */
    ABOUT_TO_ENTER_COMMENT,
    /**
     * Indicates a backslash character within a String or char literal. This state (together with InString) also means
     * the position is within a String or char literal
     */
    ESCAPE_STRING,
    /**
     * Describes positions within a String or char literal. While the starting " or ' is already in state InString, the
     * ending character is not.
     */
    IN_STRING,
    /**
     * Describes positions that do not belong to any of the other states.
     */
    DEFAULT
  }

  /**
   * Gets the {@link State} of the given position within the source specified.
   * 
   * @param src
   *          The Java or JavaScript source. Must not be {@code null}.
   * @param pos
   *          The position (zero based index) within the source.
   * @return The source {@link State} at the index given.
   * @see State
   */
  public static State parse(char[] src, int pos) {
    return parse(wrap(src), pos);
  }

  public static State parse(CharSequence src, int pos) {
    int limit = limit(src, pos);
    return parseImpl(src, limit).toState();
  }

  /**
   * Checks if the given position within the source specified is in a {@link String} or char literal.<br/>
   * The starting and ending quotes are considered to be in the string.<br/>
   * This is different than the {@link State} enums itself where the ending character never belongs to the just ending
   * state.
   * 
   * @param src
   *          The Java or JavaScript source. Must not be {@code null}.
   * @param pos
   *          The position (zero based index) within the source.
   * @return {@code true} if the specified index is within a {@link String} or {@link Character} literal. Also returns
   *         {@code true} if the position points to the starting or ending delimiters (" or ').
   */
  public static boolean isInString(char[] src, int pos) {
    return isInString(wrap(src), pos);
  }

  /**
   * @see #isInString(char[], int)
   */
  public static boolean isInString(CharSequence src, int pos) {
    int limit = limit(src, pos);
    IState state = parseImpl(src, limit);
    if (isStringEnd(state, src, limit)) {
      return true;
    }
    State result = state.toState();
    return result == State.IN_STRING || result == State.ESCAPE_STRING;
  }

  /**
   * Checks if the given position is neither within a string or char literal nor within a comment (block or single
   * line).<br/>
   * The starting and ending quotes of string and chars are considered to be in the string and are therefore no code
   * (return {@code false}). <br/>
   * For comments the starting slashes, ending slashes and stars are considered to be in the comment and are therefore
   * no code (return {@code false}).<br/>
   * This is different than the {@link State} enums itself where the ending character never belongs to the just ending
   * state.
   *
   * @param src
   *          The Java or JavaScript source. Must not be {@code null}.
   * @param pos
   *          The position (zero based index) within the source.
   * @return {@code true} if the specified index is not within a {@link String} or {@link Character} literal and not
   *         within a comment. Returns {@code false} otherwise which includes the starting and ending " or ' for strings
   *         and the starting and ending / for comments.
   */
  public static boolean isInCode(char[] src, int pos) {
    return isInCode(wrap(src), pos);
  }

  /**
   * @see #isInCode(char[], int)
   */
  public static boolean isInCode(CharSequence src, int pos) {
    int limit = limit(src, pos);
    IState state = parseImpl(src, limit);
    if (isStringEnd(state, src, limit)) {
      return false;
    }
    if (isCommentBoundary(state, src, limit)) {
      return false;
    }
    return state == DefaultState.INSTANCE
        || state == AboutToEnterCommentState.INSTANCE;
  }

  /**
   * Checks if the given position within the source specified is in a comment.<br/>
   * The starting slashes, ending slashes and stars are considered to be part of the comment.<br/>
   * This is different than the {@link State} enums itself where the ending character never belongs to the just ending
   * state.
   *
   * @param src
   *          The Java or JavaScript source. Must not be {@code null}.
   * @param pos
   *          The position (zero based index) within the source.
   * @return {@code true} if the specified index is within a comment. Also returns {@code true} if the position points
   *         to the starting or ending slashes or stars.
   */
  public static boolean isInComment(char[] src, int pos) {
    return isInComment(wrap(src), pos);
  }

  /**
   * @see #isInString(char[], int)
   */
  public static boolean isInComment(CharSequence src, int pos) {
    int limit = limit(src, pos);
    IState state = parseImpl(src, limit);
    if (isCommentBoundary(state, src, limit)) {
      return true;
    }
    return state == InBlockCommentState.INSTANCE
        || state == InLineCommentState.INSTANCE
        || state == AboutToExitBlockCommentState.INSTANCE;
  }

  static boolean isCommentBoundary(IState calculatedState, CharSequence src, int limit) {
    if (calculatedState == DefaultState.INSTANCE) {
      return previousState(DefaultState.INSTANCE, src, limit) == AboutToEnterCommentState.INSTANCE;
    }
    if (calculatedState != AboutToEnterCommentState.INSTANCE) {
      return false;
    }
    IState next = nextState(calculatedState, src, limit);
    return next == InBlockCommentState.INSTANCE || next == InLineCommentState.INSTANCE;
  }

  static boolean isStringEnd(IState calculatedState, CharSequence src, int limit) {
    return calculatedState == DefaultState.INSTANCE && previousState(DefaultState.INSTANCE, src, limit).toState() == State.IN_STRING;
  }

  static IState parseImpl(CharSequence chars, int end) {
    IState state = DefaultState.INSTANCE;
    for (int i = 0; i < end; i++) {
      state = state.next(chars.charAt(i));
    }
    return state;
  }

  static int limit(CharSequence src, int pos) {
    return Math.min(Ensure.notNull(src.length()), pos + 1);
  }

  static IState previousState(IState calculatedState, CharSequence src, int limit) {
    if (limit <= 0) {
      return DefaultState.INSTANCE;
    }
    return calculatedState.next(src.charAt(limit - 1));
  }

  static IState nextState(IState calculatedState, CharSequence src, int limit) {
    if (src.length() <= limit) {
      return calculatedState;
    }
    return calculatedState.next(src.charAt(limit));
  }
}
