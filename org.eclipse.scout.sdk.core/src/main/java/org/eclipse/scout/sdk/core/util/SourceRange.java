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

/**
 * <h3>{@link SourceRange}</h3>
 *
 * @since 5.1.0
 */
public class SourceRange {

  private final CharSequence m_src;
  private final int m_start;
  private final int m_end;

  public SourceRange(CharSequence src, int start) {
    m_src = Ensure.notNull(src);
    m_start = start;
    m_end = start + src.length() - 1;
  }

  /**
   * Gets the zero based index of the first character of this element relative to the source of the entire file
   * (inclusive).
   *
   * @return The start index of the source of the element this range belongs to or a negative value if no source is
   *         available.
   */
  public int start() {
    return m_start;
  }

  /**
   * Gets the number of characters of the source code for this element
   *
   * @return the number of characters or a negative value if no source is available.
   */
  public int length() {
    return m_src.length();
  }

  /**
   * Gets the zero based index of the last character of this element relative to the source of the entire file
   * (inclusive).
   *
   * @return The end index of the source of the element this range belongs to or a negative value if no source is
   *         available.
   */
  public int end() {
    return m_end;
  }

  /**
   * Gets the source of the element this {@link SourceRange} belongs to.
   *
   * @return the source as {@link CharSequence}.
   */
  public CharSequence asCharSequence() {
    return m_src;
  }

  @Override
  public String toString() {
    return asCharSequence().toString();
  }
}
