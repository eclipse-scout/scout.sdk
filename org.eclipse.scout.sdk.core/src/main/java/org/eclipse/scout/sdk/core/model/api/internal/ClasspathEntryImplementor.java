/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api.internal;

import java.nio.file.Path;

import org.eclipse.scout.sdk.core.model.api.ClasspathContentKind;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;

/**
 * <h3>{@link ClasspathEntryImplementor}</h3>
 *
 * @since 7.0.0
 */
public class ClasspathEntryImplementor implements IClasspathEntry {

  private final ClasspathSpi m_spi;

  public ClasspathEntryImplementor(ClasspathSpi classpathWithEcj) {
    m_spi = classpathWithEcj;
  }

  @Override
  public IJavaEnvironment javaEnvironment() {
    return m_spi.getJavaEnvironment().wrap();
  }

  @Override
  public ClasspathContentKind kind() {
    return switch (m_spi.getMode()) {
      case ClasspathSpi.MODE_SOURCE -> ClasspathContentKind.SOURCE;
      case ClasspathSpi.MODE_BINARY -> ClasspathContentKind.BINARY;
      default -> ClasspathContentKind.MIXED;
    };
  }

  @Override
  public boolean isDirectory() {
    return m_spi.isDirectory();
  }

  @Override
  public boolean isSourceFolder() {
    return m_spi.isSourceFolder();
  }

  @Override
  public Path path() {
    return m_spi.getPath();
  }

  @Override
  public String encoding() {
    return m_spi.getEncoding();
  }

  @Override
  public ClasspathSpi unwrap() {
    return m_spi;
  }

  @Override
  public final int hashCode() {
    return m_spi.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    return obj == this;
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append("Classpath [path=");
    builder.append(path());
    builder.append(']');
    return builder.toString();
  }
}
