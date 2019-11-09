/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.builder.java.comment;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;

/**
 * <h3>{@link ICommentBuilder}</h3>
 * <p>
 * An {@link ISourceBuilder} that provides methods to apply java comment expressions.
 *
 * @since 6.1.0
 */
public interface ICommentBuilder<TYPE extends ICommentBuilder<TYPE>> extends ISourceBuilder<TYPE> {

  /**
   * Appends a block comment start expression without any trailing space. The start expression is {@code /*}
   *
   * @return This builder
   */
  TYPE appendBlockCommentStart();

  /**
   * Appends a JavaDoc block comment start expression (without trailing space). The start expression is {@code /**}
   *
   * @return This builder
   */
  TYPE appendJavaDocStart();

  /**
   * Appends a block comment end expression. The end expression is <code>*&#47;</code>.
   *
   * @return This builder
   */
  TYPE appendBlockCommentEnd();

  /**
   * Appends the given comment text as block comment. Line delimiters within the specified text are preserved and
   * prefixed with a comment star.
   *
   * @param comment
   *          The raw comment text. May contain line delimiters. May not be {@code null}.
   * @return This builder
   */
  TYPE appendBlockComment(String comment);

  /**
   * Appends the given comment text as JavaDoc block comment. Line delimiters within the specified text are preserved and
   * prefixed with a comment star.
   *
   * @param comment
   *          The raw comment text. May contain line delimiters. May not be {@code null}.
   * @return This builder
   */
  TYPE appendJavaDocComment(String comment);

  /**
   * Appends a single line to-do comment with the specified text. The to-do expression contains the user name.
   *
   * @param toDoMessage
   *          The message without any line delimiters. May not be {@code null}.
   * @return This builder
   */
  TYPE appendTodo(CharSequence toDoMessage);

  /**
   * Appends a single line to-do with the text "Auto-generated method stub."
   *
   * @return This builder
   */
  TYPE appendTodoAutoGeneratedMethodStub();

  /**
   * Appends a single line comment but with block comment delimiters.<br>
   * <br>
   * <b>Example:</b> <code>/** comment-text *&#47;</code>
   *
   * @param comment
   *          The comment text without any line delimiters. May not be {@code null}.
   * @return This builder
   */
  TYPE appendJavaDocLine(CharSequence comment);

  /**
   * Appends a single line comment.<br>
   * <br>
   * <b>Example:</b> {@code // comment-text}
   *
   * @param msg
   *          The comment message without any line delimiters. May not be {@code null}.
   * @return This builder
   */
  TYPE appendSingleLineComment(CharSequence msg);

}
