/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;

/**
 * <h3>{@link ClasspathBuilder}</h3>
 * <p>
 * Helper class to create {@link Classpath} instances for a specified JRE and user {@link ClasspathEntry
 * ClasspathEntries}.
 *
 * @since 7.0.100
 */
public class ClasspathBuilder {

  private static final Map<Path, JreInfo> JRE_INFOS = new ConcurrentHashMap<>();

  private final List<Classpath> m_full;
  private final List<Classpath> m_bootClasspath;
  private final Collection<Classpath> m_classpath;
  private final Set<ClasspathEntry> m_entries;
  private final JreInfo m_jreInfo;

  /**
   * @param jreHome
   *          The absolute path to the JRE (not JDK!) root. Based on this path the bootstrap classpath will be
   *          constructed. May be {@code null}. Then the running JRE is used.
   * @param paths
   *          The runtime classpath entries.
   */
  protected ClasspathBuilder(Path jreHome, Collection<? extends ClasspathEntry> paths) {
    var javaHome = Optional.ofNullable(jreHome).orElseGet(JreInfo::runningJavaHome).normalize();
    m_jreInfo = JRE_INFOS.computeIfAbsent(javaHome, JreInfo::new);

    var classpath = toClasspath(paths);
    m_entries = classpath.keySet();
    m_classpath = classpath.values();

    m_bootClasspath = createBootClasspathFor(m_jreInfo);

    List<Classpath> fullCp = new ArrayList<>(m_classpath.size() + m_bootClasspath.size());
    fullCp.addAll(m_classpath);
    fullCp.addAll(m_bootClasspath);
    m_full = fullCp;
  }

  /**
   * @return All {@link Classpath} entries (boot classpath and user classpath combined).
   */
  public List<Classpath> fullClasspath() {
    return m_full;
  }

  /**
   * @return The boot classpath for the JRE associated with this builder.
   */
  public List<Classpath> bootClasspath() {
    return m_bootClasspath;
  }

  /**
   * @return The user classpath associated with this builder.
   */
  public Collection<Classpath> userClasspath() {
    return m_classpath;
  }

  /**
   * @return The user classpath as {@link ClasspathEntry} list.
   */
  public Set<ClasspathEntry> userClasspathEntries() {
    return m_entries;
  }

  /**
   * @return Detail information about the JRE associated with this builder.
   */
  public JreInfo jreInfo() {
    return m_jreInfo;
  }

  private static List<Classpath> createBootClasspathFor(JreInfo jre) {
    List<Classpath> result = new ArrayList<>();
    var jreHome = jre.jreHome();
    var rtSrcZip = jre.rtSrcZip();
    if (jre.supportsJrtModules()) {
      SdkLog.debug("Boot Classpath uses binary JRT modules of Java home: {}.", jreHome);
      result.add(FileSystem.getJrtClasspath(jreHome.toString(), null, null, null));
    }
    else {
      SdkLog.debug("Using Boot Classpath based on the jars in the lib folder of Java home: {}.", jreHome);
      // append RT src.zip first
      appendSrcClasspathToEnd(result, rtSrcZip, null /* use default encoding */);

      for (var cpEntry : jre.bootClasspath()) {
        appendBinClasspathToEnd(result, cpEntry);
      }
    }

    return result;
  }

  private Map<ClasspathEntry, Classpath> toClasspath(Collection<? extends ClasspathEntry> paths) {
    Map<ClasspathEntry, Classpath> result = new LinkedHashMap<>(paths.size());
    var version = jreInfo().version();
    if (JreInfo.VERSION_1_8.equals(version)) {
      version = null; // multi release JARs are not supported before Java 9.
    }

    for (ClasspathEntry cpe : paths) {
      if (result.containsKey(cpe)) {
        continue; // skip duplicates. using the first of all duplicates on the cp
      }

      var classpath = toClasspath(cpe.path(), cpe.mode() == ClasspathSpi.MODE_SOURCE, cpe.encoding(), version);
      if (classpath != null) {
        result.put(cpe, classpath);
      }
    }
    return result;
  }

  private static void appendBinClasspathToEnd(Collection<Classpath> collector, Path f) {
    appendClasspathToEnd(collector, f, false, null);
  }

  private static void appendSrcClasspathToEnd(Collection<Classpath> collector, Path f, String encoding) {
    appendClasspathToEnd(collector, f, true, encoding);
  }

  private static void appendClasspathToEnd(Collection<Classpath> collector, Path f, boolean isSourceOnly, String encoding) {
    var cp = toClasspath(f, isSourceOnly, encoding, null);
    if (cp == null) {
      return;
    }
    collector.add(cp);
  }

  private static Classpath toClasspath(Path f, boolean isSourceOnly, String encoding, String release) {
    if (f == null || !Files.isReadable(f)) {
      return null;
    }
    return FileSystem.getClasspath(f.toString(), encoding, isSourceOnly, null, null, null, release);
  }
}
