/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import static java.nio.file.Files.readAllLines;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Strings.indexOf;
import static org.eclipse.scout.sdk.core.util.Strings.startsWith;
import static org.eclipse.scout.sdk.core.util.Strings.trim;
import static org.eclipse.scout.sdk.core.util.Strings.withoutQuotes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.eclipse.jdt.internal.compiler.util.JRTUtil;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.StreamUtils;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link JreInfo}</h3> Stores meta information about a Java Runtime Environment on the disk.
 *
 * @since 7.0.100
 */
public class JreInfo {

  public static final String VERSION_1_8 = "1.8";

  private final Path m_rtSrcZip;
  private final boolean m_supportsJrtModules;
  private final Path m_jreHome;
  private final String m_version;
  private final FinalValue<List<Path>> m_bootClasspath;
  private final int m_feature;

  /**
   * @param jreHome
   *          Must not be {@code null}. Must point to a JRE root.
   */
  @SuppressWarnings("squid:S2259")
  public JreInfo(Path jreHome) {
    m_jreHome = Ensure.notNull(jreHome);
    m_rtSrcZip = resolveRtSourceZip(jreHome);

    var jrt = jreHome.resolve("lib").resolve(JRTUtil.JRT_FS_JAR);
    m_supportsJrtModules = Files.isReadable(jrt) && Files.isRegularFile(jrt); // supports module system (Java 9 and newer)
    m_version = computeVersion(jreHome);
    m_feature = computeFeatureLevel(m_version);
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
   * @return The major Java feature level. Corresponds to the first number of 9+ format (<b>9</b>.0.1) or the second
   *         number of 1.x format (1.<b>8</b>.0_60).
   */
  public int feature() {
    return m_feature;
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
   * @return The boot classpath if the RT is based on jar files (Java 8 and older). Otherwise, an empty {@link List} is
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
    var jreHome = jreHome();
    var libDirs = Stream.of(jreHome.resolve("lib"), jreHome.resolve("lib/ext"));

    var result = libDirs
        .flatMap(JreInfo::listFiles)
        .filter(JreInfo::isArchive)
        .collect(toList());

    Optional.of(jreHome.resolve("classes"))
        .filter(Files::isReadable)
        .filter(Files::isDirectory)
        .ifPresent(result::add);

    return unmodifiableList(result);
  }

  protected static int computeFeatureLevel(String version) {
    if (version.startsWith("1.") && version.length() > 2) {
      // format 1.8: use part after the dot
      return Integer.parseInt(version, 2, version.length(), 10);
    }
    var dotPos = version.indexOf('.');
    if (dotPos > 0) {
      // format 17.4: use part before the dot
      return Integer.parseInt(version, 0, dotPos, 10);
    }
    // format 11
    return Integer.parseInt(version);
  }

  protected static String computeVersion(Path jreHome) {
    var release = jreHome.resolve("release");
    if (!Files.isReadable(release) || !Files.isRegularFile(release)) {
      return VERSION_1_8;
    }

    try {
      var parsedVersion = parseVersion(readAllLines(release, StandardCharsets.UTF_8));
      return Ensure.notNull(parsedVersion, "Cannot parse Java version for location '{}'.", jreHome);
    }
    catch (IOException e) {
      throw new SdkException("Error parsing Java release file: '{}'.", jreHome, e);
    }
  }

  protected static String parseVersion(Iterable<String> lines) {
    var prefix = "JAVA_VERSION=";
    for (var line : lines) {
      if (Strings.isBlank(line)) {
        continue;
      }
      if (startsWith(line, prefix, false)) {
        var value = withoutQuotes(trim(line.substring(prefix.length())));
        if (!value.isEmpty()) {
          return parseVersion(value);
        }
      }
    }
    return null;
  }

  protected static String parseVersion(CharSequence versionString) {
    var dot = '.';
    var firstDot = indexOf(dot, versionString);
    if (firstDot < 1) {
      return versionString.toString();
    }
    var majorAndMinor = new StringBuilder(5);
    majorAndMinor.append(versionString.subSequence(0, firstDot));

    var secondDot = indexOf(dot, versionString, firstDot + 1);
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
    var fileName = candidate.getFileName();
    if (fileName == null) {
      return false;
    }
    var name = fileName.toString().toLowerCase(Locale.US);
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
    var innerSrcZip = jreHome.resolve("lib/src.zip");
    if (Files.isReadable(innerSrcZip) && Files.isRegularFile(innerSrcZip)) {
      return innerSrcZip;
    }

    // before Java9 it was in the jdk above the java-home.
    var parent = jreHome.getParent();
    if (parent == null) {
      return null;
    }
    return parent.resolve("src.zip");
  }

  /**
   * @return The {@link Path} to the Java home directory running the current application. Is never {@code null}.
   * @throws IllegalArgumentException
   *           if the {@link Path} cannot be calculated.
   */
  public static Path runningJavaHome() {
    return Ensure.notNull(Util.getJavaHome(), "Cannot calculate the running Java home. Please specify a JRE home explicitly.").toPath();
  }

  /**
   * @return The {@link JreInfo} describing the Java runtime of the running application. Is never {@code null}.
   */
  public static JreInfo runningJreInfo() {
    return new JreInfo(runningJavaHome());
  }

  /**
   * Gets all entries in the system property "java.class.path".<br>
   * Entries pointing to the given Java directory are not part of the result.
   *
   * @param javaHome
   *          The path to the Java home directory or {@code null} if the running Java home should be used. Classpath
   *          entries below this home directory are not part of the result.
   * @return A {@link Stream} holding all user classpath elements as specified in the "java.class.path" system property.
   */
  public static Stream<Path> runningUserClassPath(Path javaHome) {
    //noinspection AccessOfSystemProperties
    var javaClassPathRaw = System.getProperty("java.class.path");
    if (Strings.isBlank(javaClassPathRaw)) {
      return Stream.empty();
    }

    var jreHome = Optional.ofNullable(javaHome).orElseGet(JreInfo::runningJavaHome);
    return StreamUtils.toStream(new StringTokenizer(javaClassPathRaw, File.pathSeparator))
        .map(Object::toString)
        .map(Paths::get)
        .filter(classpathItem -> !isJreLib(classpathItem, jreHome));
  }

  protected static boolean isJreLib(Path candidate, Path javaHome) {
    return candidate != null && candidate.startsWith(javaHome);
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

    var other = (JreInfo) obj;
    return m_jreHome.equals(other.m_jreHome);
  }
}
