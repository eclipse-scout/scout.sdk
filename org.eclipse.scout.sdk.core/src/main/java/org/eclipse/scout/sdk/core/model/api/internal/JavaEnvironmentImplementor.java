/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api.internal;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.IUnresolvedType;
import org.eclipse.scout.sdk.core.model.api.MissingTypeException;
import org.eclipse.scout.sdk.core.model.api.internal.UnresolvedTypeImplementor.UnresolvedTypeSpi;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link JavaEnvironmentImplementor}</h3>
 *
 * @since 5.1.0
 */
public class JavaEnvironmentImplementor implements IJavaEnvironment {
  private final JavaEnvironmentSpi m_spi;
  private FinalValue<List<IClasspathEntry>> m_sourceFoldersSorted;

  public JavaEnvironmentImplementor(JavaEnvironmentSpi spi) {
    m_spi = spi;
    m_sourceFoldersSorted = new FinalValue<>();
  }

  @Override
  public Optional<IType> findType(String fqn) {
    return Optional.ofNullable(m_spi.findType(fqn))
        .map(TypeSpi::wrap);
  }

  @Override
  public IType requireType(String fqn) {
    return findType(fqn)
        .orElseThrow(() -> newFail("Type '{}' cannot be found.", fqn));
  }

  @Override
  public boolean exists(IType t) {
    return t != null && (t.javaEnvironment() == this || exists(t.name()));
  }

  @Override
  public boolean exists(String fqn) {
    return Strings.hasText(fqn) && m_spi.findType(fqn) != null;
  }

  @Override
  @SuppressWarnings("squid:S1166")
  public IUnresolvedType findUnresolvedType(String fqn) {
    try {
      Optional<IType> t = findType(fqn);
      if (t.isPresent()) {
        return new UnresolvedTypeImplementor(new UnresolvedTypeSpi(unwrap(), t.get()));
      }
    }
    catch (MissingTypeException ex) {
      SdkLog.debug("Missing type during unresolved-type creation.", ex);
    }
    return new UnresolvedTypeImplementor(new UnresolvedTypeSpi(unwrap(), fqn));
  }

  @Override
  public void reload() {
    m_spi.reload();
  }

  @Override
  public boolean registerCompilationUnitOverride(String packageName, String fileName, CharSequence buf) {
    char[] arr = new char[buf.length()];
    for (int i = 0; i < buf.length(); i++) {
      arr[i] = buf.charAt(i);
    }
    return m_spi.registerCompilationUnitOverride(packageName, fileName, arr);
  }

  @Override
  public List<String> compileErrors(IType type) {
    return m_spi.getCompileErrors(Ensure.notNull(type).unwrap());
  }

  @Override
  public List<String> compileErrors(String fqn) {
    return m_spi.getCompileErrors(fqn);
  }

  @Override
  public Stream<IClasspathEntry> classpath() {
    return m_spi.getClasspath().stream()
        .map(ClasspathSpi::wrap);
  }

  @Override
  public Stream<IClasspathEntry> sourceFolders() {
    return sourceFoldersSorted().stream();
  }

  @Override
  public Optional<IClasspathEntry> primarySourceFolder() {
    List<IClasspathEntry> sourceFoldersSorted = sourceFoldersSorted();
    if (sourceFoldersSorted.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(sourceFoldersSorted.get(0));
  }

  protected List<IClasspathEntry> sourceFoldersSorted() {
    return m_sourceFoldersSorted.computeIfAbsentAndGet(() -> {
      List<ClasspathSpi> src = m_spi.getClasspath();
      Collection<P_ClasspathComposite> sorter = new TreeSet<>(comparingInt(P_ClasspathComposite::order).thenComparingInt(P_ClasspathComposite::pos));
      for (int i = 0; i < src.size(); i++) {
        ClasspathSpi classpathSpi = src.get(i);
        if (classpathSpi.isSourceFolder()) {
          sorter.add(new P_ClasspathComposite(classpathSpi.wrap(), i));
        }
      }
      return sorter.stream()
          .map(P_ClasspathComposite::entry)
          .collect(toList());
    });
  }

  @Override
  public JavaEnvironmentSpi unwrap() {
    return m_spi;
  }

  public void spiChanged() {
    m_sourceFoldersSorted = new FinalValue<>();
  }

  protected static int priorityOfSourceFolder(IClasspathEntry sf) {
    return priorityOfSourceFolder(sf.path());
  }

  public static int priorityOfSourceFolder(Path absolutePath) {
    if (absolutePath.endsWith(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)) {
      return 1;
    }
    if (absolutePath.endsWith("src")) {
      return 11;
    }
    if (absolutePath.endsWith(ISourceFolders.TEST_JAVA_SOURCE_FOLDER)) {
      return 20;
    }

    int totalSegments = absolutePath.getNameCount();
    int numEndSegments = Math.min(totalSegments, 3); // last 3 segments or less if the path has less segments
    Path rel = absolutePath.subpath(totalSegments - numEndSegments, totalSegments);
    if (rel.startsWith(Paths.get(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER).subpath(0, 2))) {
      return 12;
    }
    if (rel.startsWith(Paths.get(ISourceFolders.TEST_JAVA_SOURCE_FOLDER).subpath(0, 2))) {
      return 22;
    }

    return 30;
  }

  private static final class P_ClasspathComposite {
    private final IClasspathEntry m_entry;
    private final int m_order;
    private final int m_pos;

    private P_ClasspathComposite(IClasspathEntry entry, int pos) {
      m_entry = entry;
      m_pos = pos;
      m_order = priorityOfSourceFolder(entry);
    }

    private IClasspathEntry entry() {
      return m_entry;
    }

    @SuppressWarnings("squid:UnusedPrivateMethod")
    private int order() {
      return m_order;
    }

    public int pos() {
      return m_pos;
    }
  }
}
