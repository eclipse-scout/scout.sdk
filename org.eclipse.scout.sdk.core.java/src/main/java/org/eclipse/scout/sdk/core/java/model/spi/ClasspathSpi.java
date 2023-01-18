/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.spi;

import java.nio.file.Path;

import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;

/**
 * <h3>{@link ClasspathSpi}</h3>
 *
 * @since 5.1.0
 */
public interface ClasspathSpi {

  /**
   * Describes a {@link ClasspathSpi} containing *.java source files (can be a directory or an archive).
   */
  int MODE_SOURCE = 1;
  /**
   * Describes a {@link ClasspathSpi} containing *.class files (can be a directory or an archive).
   */
  int MODE_BINARY = 2;

  /**
   * @return The content mode of this {@link ClasspathSpi}. A bit mask consisting of {@link #MODE_SOURCE} and/or
   *         {@link #MODE_BINARY}.
   */
  int getMode();

  boolean isDirectory();

  boolean isSourceFolder();

  JavaEnvironmentSpi getJavaEnvironment();

  Path getPath();

  String getEncoding();

  IClasspathEntry wrap();
}
