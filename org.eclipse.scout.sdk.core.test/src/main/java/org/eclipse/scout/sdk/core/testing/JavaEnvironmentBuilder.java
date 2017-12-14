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
package org.eclipse.scout.sdk.core.testing;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.model.spi.internal.ClasspathEntry;
import org.eclipse.scout.sdk.core.model.spi.internal.JavaEnvironmentWithJdt;

/**
 * <h3>{@link JavaEnvironmentBuilder}</h3> Contains helpers used in order to create a {@link IJavaEnvironment}.
 *
 * @author Ivan Motsch
 * @since 5.2.0
 */
public class JavaEnvironmentBuilder {
  private final Path m_curDir = Paths.get("").toAbsolutePath();
  private final Collection<Pattern> m_sourceExcludes = new ArrayList<>();
  private final Collection<Pattern> m_binaryExcludes = new ArrayList<>();
  private final List<ClasspathEntry> m_paths = new ArrayList<>();

  private Path m_javaHome;
  private boolean m_includeRunningClasspath = true;
  private boolean m_includeSources = true;

  /**
   * Include current running classpath, default is true
   *
   * @return this
   */
  public JavaEnvironmentBuilder withRunningClasspath(final boolean b) {
    m_includeRunningClasspath = b;
    return this;
  }

  /**
   * Exclude classes paths containing {@code .scout.sdk.}
   *
   * @return this
   */
  public JavaEnvironmentBuilder withoutScoutSdk() {
    without(".*" + Pattern.quote("wsdl4j") + ".*");
    return without(".*" + Pattern.quote(".scout.sdk.") + ".*target/classes");
  }

  /**
   * @return The Java home to use. If it is {@code null}, the running Java home will be used.
   */
  public Path javaHome() {
    return m_javaHome;
  }

  /**
   * Specifies the Java home to use.
   *
   * @param javaHome
   *          The JRE (not JDK!) home to use or {@code null} if the running Java home should be used.
   * @return this
   */
  public JavaEnvironmentBuilder withJavaHome(final Path javaHome) {
    m_javaHome = javaHome;
    return this;
  }

  /**
   * Exclude some paths
   *
   * @param regex
   *          file path pattern with '/' as delimiter
   * @return this
   */
  public JavaEnvironmentBuilder without(final String regex) {
    m_sourceExcludes.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    m_binaryExcludes.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    return this;
  }

  /**
   * Exclude all source paths
   *
   * @return this
   */
  public JavaEnvironmentBuilder withoutAllSources() {
    m_includeSources = false;
    return this;
  }

  /**
   * Exclude specific source paths
   *
   * @return this
   */
  public JavaEnvironmentBuilder withoutSources(final String regex) {
    m_sourceExcludes.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    return this;
  }

  /**
   * @param sourceFolder
   *          is a project relative path such as src/main/java
   * @return this
   */
  public JavaEnvironmentBuilder withSourceFolder(final String sourceFolder) {
    if (sourceFolder != null) {
      appendSourcePath(m_curDir.resolve(sourceFolder), m_paths);
    }
    return this;
  }

  /**
   * @param classesFolder
   *          is a project relative path such as target/classes
   * @return this
   */
  public JavaEnvironmentBuilder withClassesFolder(final String classesFolder) {
    if (classesFolder != null) {
      appendBinaryPath(m_curDir.resolve(classesFolder), m_paths);
    }
    return this;
  }

  /**
   * @param sourcePath
   *          is an absolute source path
   * @return this
   */
  public JavaEnvironmentBuilder withAbsoluteSourcePath(final String sourcePath) {
    if (sourcePath != null) {
      appendSourcePath(Paths.get(sourcePath), m_paths);
    }
    return this;
  }

  /**
   * @param binaryPath
   *          is an absolute binary path
   * @return this
   */
  public JavaEnvironmentBuilder withAbsoluteBinaryPath(final String binaryPath) {
    if (binaryPath != null) {
      appendBinaryPath(Paths.get(binaryPath), m_paths);
    }
    return this;
  }

  public Path currentDirectory() {
    return m_curDir;
  }

  protected void collectRunningClassPath(final Collection<ClasspathEntry> collector, final Collection<Path> sourceAttachmentFor) {
    final String javaClassPathRaw = System.getProperty("java.class.path");
    if (StringUtils.isEmpty(javaClassPathRaw)) {
      return;
    }

    final StringTokenizer tokenizer = new StringTokenizer(javaClassPathRaw, File.pathSeparator);
    while (tokenizer.hasMoreTokens()) {
      filterAndAppendBinaryPath(new File(tokenizer.nextToken()).toPath(), sourceAttachmentFor, collector);
    }
  }

  /**
   * Check exclude filters and append path to collector using {@link #appendBinaryPath(Path, Collection)}
   */
  protected void filterAndAppendBinaryPath(final Path f, final Collection<Path> sourceAttachmentForCollector, final Collection<ClasspathEntry> collector) {
    if (isExcluded(f, m_binaryExcludes)) {
      return;
    }
    sourceAttachmentForCollector.add(f);
    appendBinaryPath(f, collector);
  }

  /**
   * Check exclude filters and append path to collector using {@link #appendSourcePath(Path, Collection)}
   */
  protected void filterAndAppendSourcePath(final Path f, final Collection<ClasspathEntry> collector) {
    if (isExcluded(f, m_sourceExcludes)) {
      return;
    }
    appendSourcePath(f, collector);
  }

  protected static boolean isExcluded(final Path f, final Collection<Pattern> exclusions) {
    if (exclusions.isEmpty()) {
      return false;
    }

    final String s = StringUtils.replace(f.toString(), f.getFileSystem().getSeparator(), "/");
    for (final Pattern p : exclusions) {
      if (p.matcher(s).matches()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Append path to binary path collector. Only append if the path exists
   */
  protected static void appendBinaryPath(final Path f, final Collection<ClasspathEntry> collector) {
    appendPath(f, false, collector);
  }

  /**
   * Append path to source path collector. Only append if the path exists
   */
  protected static void appendSourcePath(final Path f, final Collection<ClasspathEntry> collector) {
    appendPath(f, true, collector);
  }

  protected static void appendPath(final Path f, final boolean isSource, final Collection<ClasspathEntry> collector) {
    if (f == null) {
      return;
    }
    collector.add(new ClasspathEntry(f, isSource ? ClasspathSpi.MODE_SOURCE : ClasspathSpi.MODE_BINARY));
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected void appendSourceAttachments(final Iterable<Path> sourceAttachmentsFor, final Collection<ClasspathEntry> collector) {
    if (!m_includeSources) {
      return;
    }
    for (final Path path : sourceAttachmentsFor) {
      if (path.endsWith("target/classes")) {
        filterAndAppendSourcePath(path.getParent().getParent().resolve("src/main/java"), collector);
        filterAndAppendSourcePath(path.getParent().getParent().resolve("target/generated-sources/annotations"), collector);
      }
      else {
        String fileName = path.getFileName().toString();
        if (fileName.endsWith(".zip") || fileName.endsWith(".jar")) {
          fileName = fileName.substring(0, fileName.length() - 4) + "-sources" + fileName.substring(fileName.length() - 4);
          filterAndAppendSourcePath(path.getParent().resolve(fileName), collector);
        }
      }
    }
  }

  protected static Collection<ClasspathEntry> sort(final Collection<ClasspathEntry> allEntries) {
    final int numBuckets = 4;
    final Map<Integer, List<ClasspathEntry>> buckets = new HashMap<>(numBuckets);
    for (final ClasspathEntry entry : allEntries) {
      buckets.computeIfAbsent(bucketOf(entry), ArrayList::new).add(entry);
    }
    final Collection<ClasspathEntry> grouped = new ArrayList<>(allEntries.size());
    for (int i = 0; i < numBuckets; i++) {
      addBucket(i, grouped, buckets);
    }
    return grouped;
  }

  protected static void addBucket(final int index, final Collection<ClasspathEntry> grouped, final Map<Integer, List<ClasspathEntry>> buckets) {
    final List<ClasspathEntry> bucketContent = buckets.get(index);
    if (bucketContent == null) {
      return;
    }
    grouped.addAll(bucketContent);
  }

  protected static Integer bucketOf(final ClasspathEntry entry) {
    int result = 0;
    if (!Files.isDirectory(entry.path())) {
      result++;
    }
    if (entry.mode() == ClasspathSpi.MODE_BINARY) {
      result += 2;
    }
    return result;
  }

  public IJavaEnvironment build() {
    final Collection<ClasspathEntry> allEntries = new ArrayList<>(m_paths);
    if (m_includeRunningClasspath) {
      final Collection<Path> sourceAttachmentFor = new LinkedHashSet<>();
      collectRunningClassPath(allEntries, sourceAttachmentFor); // current classpath
      appendSourceAttachments(sourceAttachmentFor, allEntries); // find source attachments for the running classpath entries
    }

    return new JavaEnvironmentWithJdt(path -> m_curDir.resolve(path).toFile(), javaHome(), sort(allEntries)).wrap();
  }
}
