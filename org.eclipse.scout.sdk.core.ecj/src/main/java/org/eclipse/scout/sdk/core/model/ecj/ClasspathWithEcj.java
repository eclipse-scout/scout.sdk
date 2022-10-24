/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.internal.ClasspathEntryImplementor;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.AbstractSpiElement;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.util.SdkException;

public class ClasspathWithEcj extends AbstractSpiElement<IClasspathEntry> implements ClasspathSpi {
  private final Path m_path;
  private final int m_mode;
  private final boolean m_isDirectory;
  private final String m_encoding;
  private final int m_hashCode;

  protected ClasspathWithEcj(ClasspathEntry cp, AbstractJavaEnvironment env) {
    super(env);
    m_path = cp.path();
    m_isDirectory = Files.isDirectory(m_path);
    m_encoding = cp.encoding();
    m_mode = cp.mode();
    m_hashCode = env.nextHashCode();
  }

  @Override
  public Path getPath() {
    return m_path;
  }

  @Override
  public String getEncoding() {
    return m_encoding;
  }

  @Override
  public int getMode() {
    return m_mode;
  }

  @Override
  public boolean isDirectory() {
    return m_isDirectory;
  }

  @Override
  protected IClasspathEntry internalCreateApi() {
    return new ClasspathEntryImplementor(this);
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    return null; // not supported
  }

  @Override
  public boolean isSourceFolder() {
    return getMode() == MODE_SOURCE && isDirectory();
  }

  @Override
  public final int hashCode() {
    return m_hashCode;
  }

  @Override
  public final boolean equals(Object obj) {
    return obj == this;
  }

  private String modeName() {
    return switch (getMode()) {
      case MODE_SOURCE -> "SOURCE";
      case MODE_BINARY -> "BINARY";
      case MODE_SOURCE | MODE_BINARY -> "SOURCE & BINARY";
      default -> throw new SdkException("Unknown mode: {}", getMode());
    };
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    builder.append(ClasspathWithEcj.class.getSimpleName()).append(" [")
        .append("path=").append(getPath())
        .append(", mode=").append(modeName())
        .append(']');
    return builder.toString();
  }
}
