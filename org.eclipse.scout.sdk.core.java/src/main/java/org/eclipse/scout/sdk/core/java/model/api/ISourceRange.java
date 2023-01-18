/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

/**
 * <h3>{@link ISourceRange}</h3>
 *
 * @since 5.1.0
 */
public interface ISourceRange {

  /**
   * Gets the source of the element this {@link ISourceRange} belongs to.
   *
   * @return the source as {@link CharSequence}.
   */
  CharSequence asCharSequence();

  /**
   * Gets the zero based index of the first character of this element relative to the source of the entire compilation
   * unit.
   *
   * @return The start index of the source of the element this range belongs to or a negative value if no source is
   *         available.
   */
  int start();

  /**
   * Gets the number of characters of the source code for this element
   *
   * @return the number of characters or a negative value if no source is available.
   */
  int length();

  /**
   * Gets the zero based index of the last character of this element relative to the source of the entire compilation
   * unit.
   *
   * @return The end index of the source of the element this range belongs to or a negative value if no source is
   *         available.
   */
  int end();
}
