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
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Strings.withoutQuotes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link JreInfo}</h3> Stores meta information about a Java Runtime Environment on the disk.
 *
 * @since 7.0.100
 */
public class JreInfo {

  private final Path m_rtSrcZip;
  private final boolean m_supportsJrtModules;
  private final Path m_jreHome;
  private final String m_version;
  private final FinalValue<List<Path>> m_bootClasspath;

  /**
   * @param jreHome
   *          Must not be {@code null}. Must point to a JRE root.
   */
  @SuppressWarnings("squid:S2259")
  public JreInfo(Path jreHome) {
    m_jreHome = Ensure.notNull(jreHome);
    m_rtSrcZip = resolveRtSourceZip(jreHome);

    Path jrt = jreHome.resolve("lib").resolve(JRTUtil.JRT_FS_JAR);
    m_supportsJrtModules = Files.isReadable(jrt) && Files.isRegularFile(jrt); // supports module system (Java 9 and newer)
    m_version = computeVersion(jreHome);
    m_bootClasspath = new FinalValue<>();
  }

  /**
   * @return The absolute {@link Path} to the RT source ZIP file or {@code null} if it could not be found.
   */
  public Path rtSrcZip() {
    return m_rtSrcZip;
  }

  /**
   * Gets the version of this JRE.
   *
   * @return The version as {@link String}. The format is e.g. 1.8, 9, 10, 11. Only major and minor version is returned.
   *         Trailing '.0' are removed (no '10.0' but '10' instead).
   */
  public String version() {
    return m_version;
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
    return m_bootClasspath.computeIfAbsentAndGet(() -> {
      if (supportsJrtModules()) {
        return emptyList();
      }
      return resolvePlatformLibrariesLegacy();
    });
  }

  protected List<Path> resolvePlatformLibrariesLegacy() {
    // fall back to try to retrieve them out of the lib directory
    Path jreHome = jreHome();
    Stream<Path> libDirs = Stream.of(jreHome.resolve("lib"), jreHome.resolve("lib/ext"));

    List<Path> result = libDirs
        .flatMap(JreInfo::listFiles)
        .filter(JreInfo::isArchive)
        .collect(toList());

    Optional.of(jreHome.resolve("classes"))
        .filter(Files::isReadable)
        .filter(Files::isDirectory)
        .ifPresent(result::add);

    return unmodifiableList(result);
  }

  protected static String computeVersion(Path jreHome) {
    Path release = jreHome.resolve("release");
    if (!Files.isReadable(release) || !Files.isRegularFile(release)) {
      return "1.8";
    }

    try {
      return Ensure.notNull(parseVersion(Files.readAllLines(release, StandardCharsets.UTF_8)), "Cannot parse Java version for location '{}'.", jreHome);
    }
    catch (IOException e) {
      throw new SdkException("Error parsing Java release file: '{}'.", jreHome, e);
    }
  }

  protected static String parseVersion(Iterable<String> lines) {
    String prefix = "JAVA_VERSION=";
    for (String line : lines) {
      if (Strings.isBlank(line)) {
        continue;
      }
      if (line.toUpperCase(Locale.ENGLISH).startsWith(prefix)) {
        String value = withoutQuotes(line.substring(prefix.length()).trim()).toString();
        if (value.length() > 0) {
          return parseVersion(value);
        }
      }
    }
    return null;
  }

  protected static String parseVersion(String versionString) {
    char dot = '.';
    int firstDot = versionString.indexOf(dot);
    if (firstDot < 1) {
      return versionString;
    }
    StringBuilder majorAndMinor = new StringBuilder(5);
    majorAndMinor.append(versionString.subSequence(0, firstDot));

    int secondDot = versionString.indexOf(dot, firstDot + 1);
    if (secondDot > firstDot + 1) {
      majorAndMinor.append(dot).append(versionString.subSequence(firstDot + 1, secondDot));
    }

    // strip trailing '.0'
    while (majorAndMinor.length() > 2 && majorAndMinor.charAt(majorAndMinor.length() - 2) == dot && majorAndMinor.charAt(majorAndMinor.length() - 1) == '0') {
      majorAndMinor.delete(majorAndMinor.length() - 2, majorAndMinor.length());
    }
    return majorAndMinor.toString();
  }

  protected static Stream<Path> listFiles(Path directory) {
    if (!Files.isReadable(directory) || !Files.isDirectory(directory)) {
      return Stream.empty();
    }

    try {
      return Files.list(directory);
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  protected static boolean isArchive(Path candidate) {
    Path fileName = candidate.getFileName();
    if (fileName == null) {
      return false;
    }
    String name = fileName.toString().toLowerCase(Locale.ENGLISH);
    return name.endsWith(".jar") || name.endsWith(".zip");
  }

  /**
   * Tries to find the source ZIP file within the specified JRE.
   *
   * @param jreHome
   *          The JRE home directory (not the JDK!).
   * @return The {@link Path} pointing to the source zip or {@code null} if it could not be found.
   */
  public static Path resolveRtSourceZip(Path jreHome) {
    if (jreHome == null) {
      return null;
    }

    // in Java9 the src.zip is in the lib folder inside java-home.
    Path innerSrcZip = jreHome.resolve("lib/src.zip");
    if (Files.isReadable(innerSrcZip) && Files.isRegularFile(innerSrcZip)) {
      return innerSrcZip;
    }

    // before Java9 it was in the jdk above the java-home.
    Path parent = jreHome.getParent();
    if (parent == null) {
      return null;
    }
    return parent.resolve("src.zip");
  }

  public static Path getRunningJavaHome() {
    return Ensure.notNull(Util.getJavaHome(), "Cannot calculate the running Java home. Please specify a JRE home explicitly.").toPath();
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    JreInfo other = (JreInfo) obj;
    return m_jreHome.equals(other.m_jreHome);
  }
}
