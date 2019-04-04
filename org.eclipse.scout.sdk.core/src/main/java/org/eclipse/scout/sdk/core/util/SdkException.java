/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util;

import org.eclipse.scout.sdk.core.log.FormattingTuple;
import org.eclipse.scout.sdk.core.log.MessageFormatter;

/**
 * <h3>{@link SdkException}</h3>
 *
 * @since 5.1.0
 */
public class SdkException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates a {@link SdkException} from the given message.
   * <p>
   * Formatting anchors in the form of {@link MessageFormatter#ARG_REPLACE_PATTERN} can be used in the message, which
   * will be replaced by the respective argument.
   * <p>
   * If there are more arguments of the type {@link Throwable} and not referenced as formatting anchor in the message,
   * that {@link Throwable} is used as the exception's cause.
   * <p>
   * Internally, {@link MessageFormatter} is used to provide substitution functionality.
   *
   * @param message
   *          the message with support for formatting anchors in the form of
   *          {@link MessageFormatter#ARG_REPLACE_PATTERN} pairs.
   * @param args
   *          optional arguments to substitute formatting anchors in the message, with the last argument used as the
   *          exception's cause if of type {@link Throwable} and not referenced in the message.
   */
  public SdkException(String message, Object... args) {
    this(MessageFormatter.arrayFormat(message, args));
  }

  public SdkException(Throwable cause) {
    this("", cause);
  }

  protected SdkException(FormattingTuple format) {
    super(format.message(), format.firstThrowable().orElse(null));
  }
}
