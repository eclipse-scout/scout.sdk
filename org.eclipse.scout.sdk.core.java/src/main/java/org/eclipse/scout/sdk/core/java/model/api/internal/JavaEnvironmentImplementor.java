/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api.internal;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.core.util.Strings.toCharArray;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.ISourceFolders;
import org.eclipse.scout.sdk.core.java.apidef.Api;
import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.model.CompilationUnitInfo;
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.IUnresolvedType;
import org.eclipse.scout.sdk.core.java.model.api.MissingTypeException;
import org.eclipse.scout.sdk.core.java.model.api.internal.UnresolvedTypeImplementor.UnresolvedTypeSpi;
import org.eclipse.scout.sdk.core.java.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.java.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.java.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.log.SdkLog;
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
  private final Map<Class<? extends IApiSpecification>, Optional<? extends IApiSpecification>> m_apiCache;

  public JavaEnvironmentImplementor(JavaEnvironmentSpi spi) {
    m_spi = spi;
    m_sourceFoldersSorted = new FinalValue<>();
    m_apiCache = new ConcurrentHashMap<>();
  }

  @Override
  public Optional<IType> findType(ITypeNameSupplier nameSupplier) {
    return findType(nameSupplier.fqn());
  }

  @Override
  public Optional<IType> findType(String fqn) {
    return Optional.ofNullable(m_spi.findType(fqn))
        .map(TypeSpi::wrap);
  }

  @Override
  public <A extends IApiSpecification> Optional<IType> findTypeFrom(Class<A> apiDefinition, Function<A, ITypeNameSupplier> nameSupplier) {
    return new ApiFunction<>(apiDefinition, nameSupplier)
        .apply(this)
        .flatMap(this::findType);
  }

  @Override
  public IType requireType(ITypeNameSupplier nameSupplier) {
    return requireType(nameSupplier.fqn());
  }

  @Override
  public IType requireType(String fqn) {
    return findType(fqn)
        .orElseThrow(() -> newFail("Type '{}' cannot be found.", fqn));
  }

  @Override
  public <A extends IApiSpecification> IType requireTypeFrom(Class<A> apiDefinition, Function<A, ITypeNameSupplier> nameSupplier) {
    return new ApiFunction<>(apiDefinition, nameSupplier)
        .apply(this)
        .map(this::requireType)
        .orElseThrow(() -> newFail("Cannot find API '{}' in this context.", apiDefinition.getSimpleName()));
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
      var t = findType(fqn);
      if (t.isPresent()) {
        return new UnresolvedTypeImplementor(new UnresolvedTypeSpi(unwrap(), t.orElseThrow()));
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
  public boolean registerCompilationUnitOverride(CharSequence source, String packageName, String fileName) {
    return registerCompilationUnitOverride(source, null, packageName, fileName);
  }

  @Override
  public boolean registerCompilationUnitOverride(CharSequence source, Path sourceFolder, String packageName, String fileName) {
    return registerCompilationUnitOverride(source, new CompilationUnitInfo(sourceFolder, packageName, fileName));
  }

  @Override
  public boolean registerCompilationUnitOverride(CharSequence source, CompilationUnitInfo cuInfo) {
    return m_spi.registerCompilationUnitOverride(toCharArray(source), cuInfo);
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
  public boolean classpathContains(Path path) {
    if (path == null) {
      return false;
    }
    return m_spi.getClasspath().stream()
        .map(ClasspathSpi::getPath)
        .anyMatch(path::startsWith);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <A extends IApiSpecification> Optional<A> api(Class<A> apiDefinition) {
    var key = apiDefinition == null ? (Class<A>) IApiSpecification.class : apiDefinition;
    var api = m_apiCache.computeIfAbsent(key, this::createApi);
    return (Optional<A>) api; // is empty in case the apiDefinition class is null or the API could not be found in this environment.
  }

  @Override
  public <A extends IApiSpecification> A requireApi(Class<A> apiDefinition) {
    return api(apiDefinition).orElseThrow(() -> newFail("API '{}' could not be found.", apiDefinition.getSimpleName()));
  }

  protected <A extends IApiSpecification> Optional<A> createApi(Class<A> apiDefinition) {
    if (apiDefinition == IApiSpecification.class) {
      return Optional.empty();
    }
    return Api.create(apiDefinition, this);
  }

  @Override
  public Stream<IClasspathEntry> sourceFolders() {
    return sourceFoldersSorted().stream();
  }

  @Override
  public Optional<IClasspathEntry> primarySourceFolder() {
    var sourceFoldersSorted = sourceFoldersSorted();
    if (sourceFoldersSorted.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(sourceFoldersSorted.get(0));
  }

  protected List<IClasspathEntry> sourceFoldersSorted() {
    return m_sourceFoldersSorted.computeIfAbsentAndGet(() -> {
      var src = m_spi.getClasspath();
      Collection<P_ClasspathComposite> sorter = new TreeSet<>(comparingInt(P_ClasspathComposite::order).thenComparingInt(P_ClasspathComposite::pos));
      for (var i = 0; i < src.size(); i++) {
        var classpathSpi = src.get(i);
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
    m_apiCache.clear();
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

    var totalSegments = absolutePath.getNameCount();
    var numEndSegments = Math.min(totalSegments, 3); // last 3 segments or fewer if the path has fewer segments
    var rel = absolutePath.subpath(totalSegments - numEndSegments, totalSegments);
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
