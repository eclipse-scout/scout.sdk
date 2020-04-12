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
package org.eclipse.scout.sdk.core.model.spi;

import java.util.List;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

/**
 * <h3>{@link JavaEnvironmentSpi}</h3> Represents a Java lookup environment (classpath) capable to resolve
 * {@link TypeSpi}s by name.
 *
 * @since 5.1.0
 */
public interface JavaEnvironmentSpi {

  PackageSpi getPackage(String name);

  /**
   * Tries to find the {@link TypeSpi} with the given name in the receiver {@link JavaEnvironmentSpi} (classpath).
   * <p>
   * Also primitive types such as int, float, void etc. are supported
   *
   * @param fqn
   *          The fully qualified name of the {@link TypeSpi} to find. For inner {@link TypeSpi}s the inner part must be
   *          separated using '$': {@code org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass}.
   * @return The {@link TypeSpi} matching the given fully qualified name or {@code null} if it could not be found.
   */
  TypeSpi findType(String fqn);

  void reload();

  /**
   * Register an override for a (possibly) existing compilation unit. This only has an effect after a call to
   * {@link #reload()}
   */
  boolean registerCompilationUnitOverride(String packageName, String fileName, char[] src);

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

  /**
   * Calls the specified {@link Function} passing a new copy of this {@link JavaEnvironmentSpi} having the same setup
   * (classpath and configuration).
   * <p>
   * This can be useful if a something should be executed without having any impact on the current
   * {@link JavaEnvironmentSpi}.
   * <p>
   * This operation is quite resource intense because a complete new environment is created to execute the specified
   * {@link Function}.
   *
   * @param function
   *          The {@link Function} to call. Must not be {@code null}.
   * @return The result of the {@link Function} specified.
   */
  <T> T callInEmptyCopy(Function<JavaEnvironmentSpi, T> function);

}
