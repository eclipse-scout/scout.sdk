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
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;

/**
 * <h3>{@link ClasspathEntry}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ClasspathEntry {

  private final Path m_classpath;
  private final int m_mode;
  private final String m_encoding;

  public ClasspathEntry(final Path classpath, final int mode) {
    this(classpath, mode, null);
  }

  public ClasspathEntry(final Path classpath, final int mode, final String encoding) {
    m_classpath = Validate.notNull(classpath);
    m_mode = mode;
    m_encoding = StringUtils.isBlank(encoding) ? StandardCharsets.UTF_8.name() : encoding;
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
    final StringBuilder builder = new StringBuilder();
    builder.append("ClasspathEntry [");
    builder.append("path=").append(m_classpath).append(", ");
    builder.append("mode=");
    boolean sourceAttached = false;
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
    builder.append(", ");
    builder.append("encoding=").append(m_encoding);
    builder.append(']');
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    final int result = prime + m_classpath.hashCode();
    return prime * result + m_mode;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final ClasspathEntry other = (ClasspathEntry) obj;
    return m_mode == other.m_mode && m_classpath.equals(other.m_classpath);
  }
}
