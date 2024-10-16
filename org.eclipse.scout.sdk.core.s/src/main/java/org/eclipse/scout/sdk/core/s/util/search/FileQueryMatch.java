/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.util.search;

import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;

import org.eclipse.scout.sdk.core.util.Ensure;

public class FileQueryMatch extends FileRange {

  private final int m_severity;

  /**
   * @param file
   *          The file {@link Path}
   * @param textOfRange
   *          The text in the file at the range specified
   * @param start
   *          The zero based start index.
   * @param end
   *          The zero based end index.
   */
  public FileQueryMatch(Path file, Path module, CharSequence textOfRange, int start, int end) {
    this(file, module, textOfRange, start, end, Level.OFF.intValue());
  }

  /**
   * @param file
   *          The file {@link Path}
   * @param textOfRange
   *          The text in the file at the range specified
   * @param start
   *          The zero based start index.
   * @param end
   *          The zero based end index.
   * @param severity
   *          The severity of the {@link FileQueryMatch}. One of the {@link Level} constants.
   */
  public FileQueryMatch(Path file, Path module, CharSequence textOfRange, int start, int end, int severity) {
    super(file, module, textOfRange, start, end);
    m_severity = severity;
  }

  /**
   * Creates a new {@link FileQueryMatch} with severity {@link Level#OFF}. The attributes from the {@link FileRange} are
   * copied to the result.
   *
   * @param range
   *          The {@link FileRange} to copy from.
   * @return The new {@link FileQueryMatch}
   */
  public static FileQueryMatch fromFileRange(FileRange range) {
    return fromFileRange(range, Level.OFF.intValue());
  }

  /**
   * Creates a new {@link FileQueryMatch} with the given severity. The attributes from the {@link FileRange} are copied
   * to the result.
   *
   * @param range
   *          The {@link FileRange} to copy from.
   * @param severity
   *          The severity of the match.
   * @return The new {@link FileQueryMatch}
   */
  public static FileQueryMatch fromFileRange(FileRange range, int severity) {
    Ensure.notNull(range);
    return new FileQueryMatch(range.file(), range.module(), range.text(), range.start(), range.end(), severity);
  }

  /**
   * @return The severity of the {@link FileRange}. One of the {@link Level} constants.
   */
  public int severity() {
    return m_severity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    var that = (FileQueryMatch) o;
    return m_severity == that.m_severity;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), m_severity);
  }
}
