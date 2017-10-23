/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi.internal;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link JreInfo}</h3> Stores meta information about a Java Runtime Environment on the disk.
 *
 * @since 7.0.100
 */
public class JreInfo {

  private final Path m_rtSrcZip;
  private final boolean m_supportsJrtModules;
  private final Path m_jreHome;

  private List<Path> m_bootClasspath; // lazily computed

  /**
   * @param jreHome
   *          Must not be {@code null}. Must point to a JRE root.
   */
  public JreInfo(final Path jreHome) {
    m_jreHome = Validate.notNull(jreHome);
    m_rtSrcZip = resolveRtSourceZip(jreHome);

    final Path jrt = jreHome.resolve("lib/jrt-fs.jar");
    m_supportsJrtModules = Files.isReadable(jrt) && Files.isRegularFile(jrt); // supports module system (Java 9 and newer)
  }

  /**
   * @return The absolute {@link Path} to the RT source ZIP file or {@code null} if it could not be found.
   */
  public Path rtSrcZip() {
    return m_rtSrcZip;
  }

  /**
   * @return The JRE home path. Never returns {@code null}.
   */
  public Path jreHome() {
    return m_jreHome;
  }

  /**
   * @return {@code true} if the JRE supports the JRT modules filesystem (Java 9 and newer). {@code false} otherwise. If
   *         this is {@code true} the {@link #bootClasspath()} will always be empty!
   */
  public boolean supportsJrtModules() {
    return m_supportsJrtModules;
  }

  /**
   * @return The boot classpath if the RT is based on jar files (Java 8 and older). Otherwise an empty {@link List} is
   *         returned.
   */
  public List<Path> bootClasspath() {
    if (m_bootClasspath == null) {
      if (supportsJrtModules()) {
        m_bootClasspath = Collections.emptyList();
      }
      else {
        m_bootClasspath = resolvePlatformLibrariesLegacy();
      }
    }
    return m_bootClasspath;
  }

  private List<Path> resolvePlatformLibrariesLegacy() {
    // fall back to try to retrieve them out of the lib directory
    final Path jreHome = jreHome();
    final Stream<Path> libDirs = Stream.of(jreHome.resolve("lib"), jreHome.resolve("lib/ext"));

    final List<Path> result = libDirs
        .flatMap(JreInfo::listFiles)
        .filter(JreInfo::isArchive)
        .collect(toList());

    Optional.of(jreHome.resolve("classes"))
        .filter(Files::isReadable)
        .filter(Files::isDirectory)
        .ifPresent(result::add);

    return result;
  }

  private static Stream<Path> listFiles(final Path directory) {
    if (!Files.isReadable(directory) || !Files.isDirectory(directory)) {
      return Stream.empty();
    }
    try {
      return Files.list(directory);
    }
    catch (final IOException e) {
      throw new SdkException(e);
    }
  }

  private static boolean isArchive(final Path candidate) {
    final String name = candidate.getFileName().toString().toLowerCase();
    return name.endsWith(".jar") || name.endsWith(".zip");
  }

  private static Path resolveRtSourceZip(final Path jreHome) {
    // in Java9 the src.zip is in the lib folder inside java-home.
    final Path innerSrcZip = jreHome.resolve("lib/src.zip");
    if (Files.isReadable(innerSrcZip) && Files.isRegularFile(innerSrcZip)) {
      return innerSrcZip;
    }

    // before Java9 it was in the jdk above the java-home.
    final Path parent = jreHome.getParent();
    if (parent == null) {
      return null;
    }
    return parent.resolve("src.zip");
  }

  @Override
  public String toString() {
    return JreInfo.class.getSimpleName() + " for " + jreHome();
  }

  @Override
  public int hashCode() {
    return m_jreHome.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final JreInfo other = (JreInfo) obj;
    return m_jreHome.equals(other.m_jreHome);
  }
}
