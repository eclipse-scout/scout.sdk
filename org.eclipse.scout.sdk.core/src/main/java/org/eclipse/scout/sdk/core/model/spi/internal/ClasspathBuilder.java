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

import java.lang.reflect.Method;
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

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;

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

  private final Classpath[] m_full;
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
  protected ClasspathBuilder(final Path jreHome, final Collection<? extends ClasspathEntry> paths) {
    final Path javaHome = Optional.ofNullable(jreHome).orElseGet(() -> Validate.notNull(Util.getJavaHome(), "Cannot calculate the running Java home. Please specify a JRE home explicitly.").toPath());
    m_jreInfo = JRE_INFOS.computeIfAbsent(javaHome, JreInfo::new);

    final Map<ClasspathEntry, Classpath> classpath = toClasspath(paths);
    m_entries = classpath.keySet();
    m_classpath = classpath.values();

    m_bootClasspath = createBootClasspathFor(m_jreInfo);

    final List<Classpath> fullCp = new ArrayList<>(classpath.size() + m_bootClasspath.size());
    fullCp.addAll(classpath.values());
    fullCp.addAll(m_bootClasspath);
    m_full = fullCp.toArray(new Classpath[fullCp.size()]);
  }

  /**
   * @return All {@link Classpath} entries (boot classpath and user classpath combined).
   */
  public Classpath[] fullClasspath() {
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
  public Set<ClasspathEntry> entries() {
    return m_entries;
  }

  /**
   * @return Detail information about the JRE associated with this builder.
   */
  public JreInfo jreInfo() {
    return m_jreInfo;
  }

  private static List<Classpath> createBootClasspathFor(final JreInfo jre) {
    final List<Classpath> bootClasspath = new ArrayList<>();

    // append RT src.zip first
    appendSrcClasspathToEnd(bootClasspath, jre.rtSrcZip(), null /* use default */);

    final Path jreHome = jre.jreHome();
    if (jre.supportsJrtModules()) {
      SdkLog.debug("Boot Classpath uses JRT modules of Java home: {}.", jreHome);
      bootClasspath.add(resolveJrtClasspath(jreHome));
    }
    else {
      SdkLog.debug("Using Boot Classpath based on the jars in the lib folder of Java home: {}.", jreHome);
      for (final Path cpEntry : jre.bootClasspath()) {
        appendBinClasspathToEnd(bootClasspath, cpEntry);
      }
    }

    return bootClasspath;
  }

  private static Classpath resolveJrtClasspath(final Path jreHome) {
    try {
      // try JDT with Java9 support
      final Method getJrtClasspath = FileSystem.class.getDeclaredMethod("getJrtClasspath", String.class, String.class, AccessRuleSet.class, Map.class);
      return (Classpath) getJrtClasspath.invoke(null, jreHome.toString(), null, null, null);
    }
    catch (final NoSuchMethodException nsme) {
      throw new SdkException("The specified JRE (" + jreHome + ") uses a JRT FileSystem (Java 9 or newer). But the compiler used does not support JRT. Please update to a newer JDT/ECJ compiler.", nsme);
    }
    catch (final ReflectiveOperationException e) {
      throw new SdkException(e);
    }
  }

  private static Map<ClasspathEntry, Classpath> toClasspath(final Collection<? extends ClasspathEntry> paths) {
    final Map<ClasspathEntry, Classpath> result = new LinkedHashMap<>(paths.size());
    for (final ClasspathEntry cpe : paths) {
      if (result.containsKey(cpe)) {
        continue; // skip duplicates. using the first of all duplicates on the cp
      }

      final Classpath classpath = toClasspath(cpe.path(), cpe.mode() == ClasspathSpi.MODE_SOURCE, cpe.encoding());
      if (classpath != null) {
        result.put(cpe, classpath);
      }
    }
    return result;
  }

  private static void appendBinClasspathToEnd(final Collection<Classpath> collector, final Path f) {
    appendClasspathToEnd(collector, f, false, null);
  }

  private static void appendSrcClasspathToEnd(final Collection<Classpath> collector, final Path f, final String encoding) {
    appendClasspathToEnd(collector, f, true, encoding);
  }

  private static void appendClasspathToEnd(final Collection<Classpath> collector, final Path f, final boolean isSourceOnly, final String encoding) {
    final Classpath cp = toClasspath(f, isSourceOnly, encoding);
    if (cp == null) {
      return;
    }
    collector.add(cp);
  }

  private static Classpath toClasspath(final Path f, final boolean isSourceOnly, final String encoding) {
    if (f == null || !Files.isReadable(f)) {
      return null;
    }
    return FileSystem.getClasspath(f.toString(), encoding, isSourceOnly, null, null, null);
  }
}
