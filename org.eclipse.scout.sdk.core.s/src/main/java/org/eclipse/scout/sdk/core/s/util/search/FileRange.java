/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
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

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * Represents a finding of a {@link IFileQueryResult}. It contains file path and the range within the file that
 * represents the match.
 */
public class FileRange {
  private final Path m_file;
  private final int m_start;
  private final int m_end;

  public FileRange(Path file, int start, int end) {
    m_file = Ensure.notNull(file);
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
  public String toString() {
    return new StringBuilder(FileRange.class.getSimpleName())
        .append(" [file=").append(m_file)
        .append(", start=").append(m_start)
        .append(", end=").append(m_end)
        .append(']')
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FileRange fileRange = (FileRange) o;
    return m_start == fileRange.m_start
        && m_end == fileRange.m_end
        && m_file.equals(fileRange.m_file);
  }

  @Override
  public int hashCode() {
    return (((m_file.hashCode() * 31) + m_start) * 31) + m_end;
  }
}