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
package org.eclipse.scout.sdk.core.imports;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.JavaTypes;
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
  public String checkCurrentScope(TypeReferenceDescriptor cand) {
    CharSequence q = getQualifier();

    // same qualifier
    if (Objects.equals(q, cand.getQualifier()) || (Strings.isBlank(q) && Strings.isBlank(cand.getQualifier()))) {
      return cand.getSimpleName();
    }

    // check if simpleName (with other packageName) exists in same package
    IJavaEnvironment env = getJavaEnvironment();
    if (env != null) {
      Boolean existsInSamePackage = m_existsInSamePackageCache.get(cand.getSimpleName());
      if (existsInSamePackage == null) {
        // load to cache
        String name;
        if (Strings.isEmpty(q)) {
          name = cand.getSimpleName();
        }
        else {
          name = new StringBuilder(q).append(JavaTypes.C_DOT).append(cand.getSimpleName()).toString();
        }
        existsInSamePackage = env.exists(name);
        m_existsInSamePackageCache.put(cand.getSimpleName(), existsInSamePackage);
      }
      if (existsInSamePackage) {
        //must qualify
        return cand.getQualifiedName();
      }
    }
    return super.checkCurrentScope(cand);
  }
}
