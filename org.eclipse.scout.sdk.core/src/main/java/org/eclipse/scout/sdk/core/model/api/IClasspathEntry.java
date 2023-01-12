/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api;

import java.nio.file.Path;

import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;

/**
 * <h3>{@link IClasspathEntry}</h3>
 * <p>
 * A classpath entry. This can be a jar file or a directory. It may contain *.java and/or *.class files.
 *
 * @since 7.0.0
 */
public interface IClasspathEntry {

  /**
   * Gets the {@link IJavaEnvironment} this {@link IClasspathEntry} belongs to.
   *
   * @return The owning {@link IJavaEnvironment}.
   */
  IJavaEnvironment javaEnvironment();

  /**
   * @return The kind of content of this {@link IClasspathEntry}.
   */
  ClasspathContentKind kind();

  /**
   * @return {@code true} if this {@link IClasspathEntry} points to a directory. {@code false} if it is a file.
   */
  boolean isDirectory();

  /**
   * @return {@code true} if this {@link IClasspathEntry} contains *.java files in a directory structure that may be
   *         modified.
   */
  boolean isSourceFolder();

  /**
   * @return The {@link Path} this {@link IClasspathEntry} points to. This may be a directory or a file.
   */
  Path path();

  /**
   * @return The encoding to be used to read *.java files in this {@link IClasspathEntry}.
   */
  String encoding();

  /**
   * Unwraps the {@link IClasspathEntry} to its underlying {@link ClasspathSpi}.
   *
   * @return The service provider interface that belongs to this {@link IClasspathEntry}.
   */
  ClasspathSpi unwrap();
}
