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

import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
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
   * Tries to find the {@link IType} with the given name.
   * <p>
   * Primitive types such as int, float, void, etc. are supported (see {@link IJavaRuntimeTypes}).<br>
   * Array types are supported with suffix <code>[]</code>.
   *
   * @param fqn
   *          The fully qualified name of the {@link IType} to find. For inner {@link IType}s the inner part must be
   *          separated using '$'. Array types must have the array suffix for each dimension.<br>
   *          Examples:
   *          <ul>
   *          <li><code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass</code></li>
   *          <li><code>int[][]</code></li>
   *          <li><code>java.lang.Long</code></li>
   *          <li><code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass[]</code></li>
   *          </ul>
   * @return The {@link IType} matching the given fully qualified name or <code>null</code> if it could not be found.
   */
  IType findType(String fqn);

  /**
   * Returns an {@link IUnresolvedType} for the given fully qualified name.<br>
   * If the given fully qualified name can be found on the classpath the returned {@link IUnresolvedType} will exist
   * (see {@link IUnresolvedType#exists()}). Otherwise a non-existing {@link IUnresolvedType} will be returned.<br>
   * <br>
   * <b>Note:</b><br>
   * {@link IUnresolvedType}s are never cached in the {@link IJavaEnvironment}. Calls to this methdo will always create
   * new instances. This allows to get a resolved version later on.
   *
   * @param fqn
   *          The fully qualified name of the type. See {@link #findType(String)} for details.
   * @return A new {@link IUnresolvedType}. Never returns <code>null</code>.
   */
  IUnresolvedType findUnresolvedType(String fqn);

  /**
   * @return the resource file locator used to read files from the workspace module.
   *         <p>
   *         This method returns null if this {@link IJavaEnvironment} is not a workspace module.
   */
  IFileLocator getFileLocator();

  /**
   * When file system changes occurred and the current {@link IType}, {@link ICompilationUnit} should not be lost, this
   * method can be called in order to reload the SPI core of the {@link IJavaEnvironment} and replace all SPI cores of
   * the wrapped classes with the updated version.
   * <p>
   * All {@link IType}, {@link ICompilationUnit} etc. remain valid and are updated with the new state of the file system
   * including optional overrides that were registered using
   * {@link #registerCompilationUnitOverride(String, String, StringBuilder)}
   */
  void reload();

  /**
   * Register an override for a (possibly) existing compilation unit.
   * <p>
   * When the type was NEVER loaded before using {@link #findType(String)}, {@link #findUnresolvedType(String)} and is
   * not implicitly referenced by any of the currently loaded types, THEN a call to {@link #findType(String)} will
   * immediately parse and resolve this new type.
   * <p>
   * In all other cases it is recommended to call {@link #reload()}
   *
   * @param packageName
   *          The package name of the compilation unit. Use <code>null</code> for the default package.
   * @param fileName
   *          The filename of the compilation unit (e.g. MyClass.java).
   * @param buf
   *          A {@link StringBuilder} holding the content of the compilation unit.
   */
  void registerCompilationUnitOverride(String packageName, String fileName, StringBuilder buf);

  /**
   * Unwraps the {@link IJavaEnvironment} into its underlying SPI class.
   *
   * @return The service provider interface that belongs to this {@link IJavaEnvironment}.
   */
  JavaEnvironmentSpi unwrap();

  /**
   * Returns a {@link String} describing all compile errors of the compilation unit containing the type with the given
   * fully qualified name. <br>
   *
   * @param fqn
   *          The fully qualified name of the type. See {@link #findType(String)} for details.
   * @return A {@link String} with the compile errors of the compilation unit that contains the type with the given name
   *         or <code>null</code> if there are no compilation errors in the compilation unit.
   * @throws IllegalArgumentException
   *           if the given fully qualified name cannot be found in this {@link IJavaEnvironment} or it is a binary
   *           type.
   */
  String compileErrors(String fqn);
}
