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

/**
 * Utility class to determine the state of a certain position in Java or JavaScript sources.<br/>
 * It is possible to query if a certain index is within a comment (line or block) or within a string or char literal.
 */
@SuppressWarnings("HardcodedLineSeparator")
public final class SourceState {

  private SourceState() {
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
    InLineComment {
      @Override
      State next(char c) {
        if (c == '\n') {
          return Default;
        }
        return InLineComment;
      }
    },
    /**
     * Describes positions within a multi or single line block comment. Such comments start with /* and end with
     * &#42;/.<br/>
     * The InBlockComment state is valid from star to star (excluded are stars in between, which might already be an
     * ending indicator and therefore belong to AboutToExitBlockComment). While the starting slash is
     * AboutToEnterComment state the ending slash is already Default state.
     */
    InBlockComment {
      @Override
      State next(char c) {
        if (c == '*') {
          return AboutToExitBlockComment;
        }
        return InBlockComment;
      }
    },
    /**
     * Indicates a * character within a block comment: /* ** &#42;/. The two stars in between belong to the state
     * AboutToExitBlockComment. But as no slash follows it stays in the AboutToExitBlockComment state or turns into
     * InBlockComment again.<br>
     * So state AboutToExitBlockComment also indicates to be within a block comment (like InBlockComment does).
     */
    AboutToExitBlockComment {
      @Override
      State next(char c) {
        switch (c) {
          case '*':
            return AboutToExitBlockComment;
          case '/':
            return Default;
          default:
            return InBlockComment;
        }
      }
    },
    /**
     * Indicates a / character which is not within a string or char literal and ist not within a comment. This state
     * might indicate that a comment is about to begin or it could also belong to e.g. a division.
     */
    AboutToEnterComment {
      @Override
      State next(char c) {
        switch (c) {
          case '*':
            return InBlockComment;
          case '/':
            return InLineComment;
          default:
            return Default;
        }
      }
    },
    /**
     * Indicates a backslash character within a String or char literal. This state (together with InString) also means
     * the position is within a String or char literal
     */
    EscapeString {
      @Override
      State next(char c) {
        return InString;
      }
    },
    /**
     * Describes positions within a String or char literal. While the starting " or ' is already in state InString, the
     * ending character is not.
     */
    InString {
      @Override
      State next(char c) {
        switch (c) {
          case '"':
          case '\'':
            return Default;
          case '\\':
            return EscapeString;
          default:
            return InString;
        }
      }
    },
    /**
     * Describes positions that do not belong to any of the other states.
     */
    Default {
      @Override
      State next(char c) {
        switch (c) {
          case '"':
          case '\'':
            return InString;
          case '/':
            return AboutToEnterComment;
          default:
            return Default;
        }
      }
    };

    abstract State next(char c);
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
    int limit = limit(src, pos);
    return parseImpl(src, limit);
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
    int limit = limit(src, pos);
    State state = parseImpl(src, limit);
    if (isStringEnd(state, src, limit)) {
      return true;
    }
    return state == State.InString || state == State.EscapeString;
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
    int limit = limit(src, pos);
    State state = parseImpl(src, limit);
    if (isStringEnd(state, src, limit)) {
      return false;
    }
    if (isCommentBoundary(state, src, limit)) {
      return false;
    }
    return state == State.Default || state == State.AboutToEnterComment;
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
    int limit = limit(src, pos);
    State state = parseImpl(src, limit);
    if (isCommentBoundary(state, src, limit)) {
      return true;
    }
    return state == State.InBlockComment || state == State.InLineComment || state == State.AboutToExitBlockComment;
  }

  static boolean isCommentBoundary(State calculatedState, char[] src, int limit) {
    if (calculatedState == State.Default) {
      return previousState(State.Default, src, limit) == State.AboutToEnterComment;
    }
    if (calculatedState != State.AboutToEnterComment) {
      return false;
    }
    State next = nextState(calculatedState, src, limit);
    return next == State.InBlockComment || next == State.InLineComment;
  }

  static boolean isStringEnd(State calculatedState, char[] src, int limit) {
    return calculatedState == State.Default && previousState(State.Default, src, limit) == State.InString;
  }

  static State parseImpl(char[] chars, int end) {
    State state = State.Default;
    for (int i = 0; i < end; i++) {
      state = state.next(chars[i]);
    }
    return state;
  }

  static int limit(char[] src, int pos) {
    return Math.min(Ensure.notNull(src.length), pos + 1);
  }

  static State previousState(State calculatedState, char[] src, int limit) {
    if (limit <= 0) {
      return State.Default;
    }
    return calculatedState.next(src[limit - 1]);
  }

  static State nextState(State calculatedState, char[] src, int limit) {
    if (src.length <= limit) {
      return calculatedState;
    }
    return calculatedState.next(src[limit]);
  }
}
