/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.imports;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Ignore imports when in same package or types in same compilation unit
 */
public class CompilationUnitScopedImportCollector extends WrappedImportCollector {
  private final String m_packageName;
  private final Map<String/* simpleName */, Boolean /* exists in own package*/> m_existsInSamePackageCache = new HashMap<>();

  public CompilationUnitScopedImportCollector(IImportCollector inner, String packageName) {
    this(inner, packageName, null);
  }

  public CompilationUnitScopedImportCollector(IImportCollector inner, String packageName, ICompilationUnitGenerator<?> cuGenerator) {
    super(inner);
    m_packageName = packageName;
    if (cuGenerator != null) {
      consumeAllTypeNamesRec(cuGenerator.types());
      registerImports(cuGenerator);
    }
  }

  protected void registerImports(ICompilationUnitGenerator<?> cuGenerator) {
    cuGenerator.imports()
        .forEach(this::addImport);
    cuGenerator.staticImports()
        .forEach(this::addStaticImport);
  }

  protected void consumeAllTypeNamesRec(Stream<ITypeGenerator<?>> stream) {
    stream
        .peek(g -> consumeAllTypeNamesRec(g.types()))
        .map(ITypeGenerator::fullyQualifiedName)
        .map(TypeReferenceDescriptor::new)
        .forEach(this::reserveElement);
  }

  @Override
  public String getQualifier() {
    return m_packageName;
  }

  @Override
  public String checkCurrentScope(TypeReferenceDescriptor candidate) {
    CharSequence q = getQualifier();

    // same qualifier
    if (Objects.equals(q, candidate.getQualifier()) || (Strings.isBlank(q) && Strings.isBlank(candidate.getQualifier()))) {
      return candidate.getSimpleName();
    }

    var env = getJavaEnvironment().orElse(null);
    if (env == null) {
      return super.checkCurrentScope(candidate);
    }

    // check if simpleName (with other packageName) exists in same package
    boolean existsInSamePackage = m_existsInSamePackageCache.computeIfAbsent(candidate.getSimpleName(),
        simpleName -> env.exists(Strings.isEmpty(q) ? simpleName : new StringBuilder(q).append(JavaTypes.C_DOT).append(simpleName).toString()));
    if (existsInSamePackage) {
      // must qualify
      return candidate.getQualifiedName();
    }

    return super.checkCurrentScope(candidate);
  }
}
