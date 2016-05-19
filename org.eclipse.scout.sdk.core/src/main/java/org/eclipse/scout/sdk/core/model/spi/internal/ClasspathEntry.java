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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;

/**
 * <h3>{@link ClasspathEntry}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ClasspathEntry {
  private final Classpath m_classpath;
  private final String m_encoding;

  public ClasspathEntry(Classpath classpath, String encoding) {
    super();
    m_classpath = classpath;
    m_encoding = encoding;
  }

  public Classpath getClasspath() {
    return m_classpath;
  }

  public String getEncoding() {
    return m_encoding;
  }

  public static Set<Classpath> toClassPaths(Set<ClasspathEntry> entries) {
    if (entries == null || entries.isEmpty()) {
      return Collections.emptySet();
    }

    Set<Classpath> result = new LinkedHashSet<>(entries.size());
    for (ClasspathEntry entry : entries) {
      result.add(entry.getClasspath());
    }
    return result;
  }

  @Override
  public String toString() {
    return m_classpath == null ? "null" : m_classpath.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_classpath == null) ? 0 : m_classpath.hashCode());
    result = prime * result + ((m_encoding == null) ? 0 : m_encoding.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ClasspathEntry other = (ClasspathEntry) obj;
    if (m_classpath == null) {
      if (other.m_classpath != null) {
        return false;
      }
    }
    else if (!m_classpath.equals(other.m_classpath)) {
      return false;
    }
    if (m_encoding == null) {
      if (other.m_encoding != null) {
        return false;
      }
    }
    else if (!m_encoding.equals(other.m_encoding)) {
      return false;
    }
    return true;
  }
}
