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
package org.eclipse.scout.sdk.core.model.spi;

import java.nio.file.Path;

import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;

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
