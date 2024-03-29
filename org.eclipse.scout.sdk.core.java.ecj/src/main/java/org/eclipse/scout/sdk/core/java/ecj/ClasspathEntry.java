/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.eclipse.scout.sdk.core.java.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link ClasspathEntry}</h3>
 *
 * @since 5.2.0
 */
public class ClasspathEntry {
  private final Path m_classpath;
  private final int m_mode;
  private final String m_encoding;

  public ClasspathEntry(Path classpath, int mode, String encoding) {
    m_classpath = Ensure.notNull(classpath);
    m_mode = mode;
    m_encoding = Strings.isBlank(encoding) ? StandardCharsets.UTF_8.name() : encoding;
  }

  public Path path() {
    return m_classpath;
  }

  public String encoding() {
    return m_encoding;
  }

  public int mode() {
    return m_mode;
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append("ClasspathEntry [");
    builder.append("path=").append(m_classpath).append(", ");
    builder.append("mode=");
    var sourceAttached = false;
    if ((m_mode & ClasspathSpi.MODE_SOURCE) != 0) {
      builder.append("source");
      sourceAttached = true;
    }
    if ((m_mode & ClasspathSpi.MODE_BINARY) != 0) {
      if (sourceAttached) {
        builder.append('&');
      }
      builder.append("binary");
    }
    builder.append(", encoding=").append(m_encoding);
    builder.append(']');
    return builder.toString();
  }

  @Override
  public int hashCode() {
    var prime = 31;
    var result = prime + m_classpath.hashCode();
    return prime * result + m_mode;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    var other = (ClasspathEntry) obj;
    return m_mode == other.m_mode && m_classpath.equals(other.m_classpath);
  }
}
