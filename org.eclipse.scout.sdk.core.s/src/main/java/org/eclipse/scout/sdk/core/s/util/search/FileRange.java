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
package org.eclipse.scout.sdk.core.s.util.search;

import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * Represents a finding of a {@link IFileQueryResult}. It contains file path and the range within the file that
 * represents the match.
 */
public class FileRange {

  private final Path m_file;
  private final Path m_module;
  private final CharSequence m_textOfRange;
  private final int m_start;
  private final int m_end;

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
  public FileRange(Path file, Path modulePath, CharSequence textOfRange, int start, int end) {
    m_file = Ensure.notNull(file);
    m_module = Ensure.notNull(modulePath);
    m_textOfRange = Ensure.notNull(textOfRange);
    m_start = start;
    m_end = end;
  }

  /**
   * @return The path of the file in which the match exists. Is never {@code null}.
   */
  public Path file() {
    return m_file;
  }

  /**
   * @return The absolute path to the java module that contains the {@link #file()}.
   */
  public Path module() {
    return m_module;
  }

  /**
   * @return The text stored in the {@link #file()} at this range.
   */
  public CharSequence text() {
    return m_textOfRange;
  }

  /**
   * @return The zero based offset of the start of the matching range (including the character at this index).
   */
  public int start() {
    return m_start;
  }

  /**
   * @return The zero based offset of the end of the matching range (excluding the character at this index).
   */
  public int end() {
    return m_end;
  }

  /**
   * @return The length of the matching range.
   */
  public int length() {
    return m_end - m_start;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var fileRange = (FileRange) o;
    return m_start == fileRange.m_start
        && m_end == fileRange.m_end
        && m_textOfRange.equals(fileRange.m_textOfRange)
        && m_file.equals(fileRange.m_file);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_file, m_textOfRange, m_start, m_end);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", FileRange.class.getSimpleName() + " [", "]")
        .add("file=" + m_file)
        .add("text=" + m_textOfRange)
        .add("start=" + m_start)
        .add("end=" + m_end)
        .toString();
  }
}
