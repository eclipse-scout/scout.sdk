/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.spi;

import java.util.List;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.model.CompilationUnitInfo;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;

/**
 * <h3>{@link JavaEnvironmentSpi}</h3> Represents a Java lookup environment (classpath) capable to resolve
 * {@link TypeSpi}s by name.
 *
 * @since 5.1.0
 */
public interface JavaEnvironmentSpi {

  /**
   * @param name
   *          The name of the package (e.g. {@code org.eclipse.scout}). Use {@code null} for the default package.
   * @return The {@link PackageSpi} for the given package name.
   */
  PackageSpi getPackage(String name);

  /**
   * Tries to find the {@link TypeSpi} with the given name in the receiver {@link JavaEnvironmentSpi} (classpath).
   * <p>
   * Primitive types such as int, float, void etc. are supported (see {@link JavaTypes}).
   *
   * @param fqn
   *          The fully qualified name of the {@link TypeSpi} to find. For inner {@link TypeSpi}s the inner part must be
   *          separated using '$': {@code org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass}.
   * @return The {@link TypeSpi} matching the given fully qualified name or {@code null} if it could not be found.
   */
  TypeSpi findType(String fqn);

  void reload();

  /**
   * Register an override for a (possibly) existing compilation unit.
   */
  boolean registerCompilationUnitOverride(char[] src, CompilationUnitInfo cuInfo);

  /**
   * @param fqn
   *          type name
   * @return A {@link List} with all errors of the compilation unit with given fully qualified type name.
   * @throws IllegalArgumentException
   *           if no compilation unit with given fully qualified name could be found.
   */
  List<String> getCompileErrors(String fqn);

  List<String> getCompileErrors(TypeSpi typeSpi);

  List<ClasspathSpi> getClasspath();

  IJavaEnvironment wrap();
}
