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
package org.eclipse.scout.sdk.core.model.api;

import org.eclipse.scout.sdk.core.model.spi.JavaEnvironmentSpi;

/**
 * <h3>{@link IJavaEnvironment}</h3> Represents a lookup environment (classpath) capable to resolve {@link IType}s by
 * name.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IJavaEnvironment {

  /**
   * Tries to find the {@link IType} with the given name in the receiver {@link IJavaEnvironment} (classpath).
   * <p>
   * Also primitive types such as int, float, void, null etc. are supported
   *
   * @param fqn
   *          The fully qualified name of the {@link IType} to find. For inner {@link IType}s the inner part must be
   *          separated using '$': <code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass</code>.
   * @return The {@link Iype} matching the given fully qualified name or <code>null</code> if it could not be found.
   */
  IType findType(String fqn);

  IUnresolvedType findUnresolvedType(String fqn);

  /**
   * When filesystem changes occured and the current {@link IType}, {@link ICompilationUnit} should not be lost, this
   * method can be called in order to reload the spi core of the {@link IJavaEnvironment} and replace all spi cores of
   * the wrapped classes with the updated version.
   * <p>
   * All {@link IType}, {@link ICompilationUnit} etc. remain valid and are updated with the new state of the filesystem
   * including optional overrides that were registered using
   * {@link #compileAndRegisterCompilationUnit(String, String, StringBuilder, boolean)}
   */
  void reload();

  /**
   * Register an override for a (possibly) existing compilation unit.
   * <p>
   * When the type was NEVER loaded before using {@link #findType(String)}, {@link #findUnresolvedType(String)} and id
   * not implicitly referenced by any of the currently loaded types, THEN a call to {@link #findType(String)} will
   * immediately parse and resolve this new type.
   * <p>
   * In all other cases it is recommended to call {@link #reload()}
   *
   * @param packageName
   * @param fileName
   * @param buf
   */
  void registerCompilationUnitOverride(String packageName, String fileName, StringBuilder buf);

  /**
   * @param fqn
   *          type name
   * @return null if the type has no compilation errors
   */
  String getCompileErrors(String fqn);

  JavaEnvironmentSpi unwrap();

  void internalSetSpi(JavaEnvironmentSpi newSpi);
}
