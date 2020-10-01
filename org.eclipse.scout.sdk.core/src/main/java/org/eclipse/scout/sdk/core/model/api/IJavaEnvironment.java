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
package org.eclipse.scout.sdk.core.model.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.apidef.IApiSpecification;
import org.eclipse.scout.sdk.core.util.apidef.IClassNameSupplier;

/**
 * <h3>{@link IJavaEnvironment}</h3> Represents a lookup environment (classpath) capable to resolve {@link IType}s by
 * name.
 *
 * @since 5.1.0
 */
public interface IJavaEnvironment {

  /**
   * Tries to find the {@link IType} with the given name.
   * <p>
   * Primitive types such as {@code int}, {@code float}, {@code void}, etc. are supported ({@link JavaTypes} contains
   * constants for all primitives).<br>
   * For inner {@link IType}s the inner part must be separated using '$'.<br>
   * Array types are supported with suffix {@code []}.
   * <p>
   * <b>Examples</b>:
   * <ul>
   * <li>{@code org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass}</li>
   * <li>{@code int[][]}</li>
   * <li>{@code java.lang.Long}</li>
   * <li>{@code org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass[]}</li>
   * <li>{@code double}</li>
   * </ul>
   *
   * @param fqn
   *          The fully qualified name of the {@link IType} to find. Array types must have the array suffix for each
   *          dimension.<br>
   * @return An {@link Optional} holding the {@link IType} matching the given fully qualified name or an empty
   *         {@link Optional} if it could not be found.
   * @see #requireType(String)
   * @see JavaTypes
   */
  Optional<IType> findType(String fqn);

  Optional<IType> findType(IClassNameSupplier fqn);

  <A extends IApiSpecification> Optional<IType> findTypeFrom(Class<A> apiDefinition, Function<A, IClassNameSupplier> nameSupplier);

  /**
   * Same as {@link #findType(String)} but throws an {@link IllegalArgumentException} if the type could not be found.
   *
   * @param fqn
   *          The fully qualified name of the {@link IType} to find. For details and examples see
   *          {@link #findType(String)}.
   * @return The {@link IType} for the specified fully qualified name. Never returns {@code null}.
   * @throws IllegalArgumentException
   *           if the name specified could not be found.
   * @see #findType(String)
   */
  IType requireType(String fqn);

  IType requireType(IClassNameSupplier nameSupplier);

  <A extends IApiSpecification> IType requireTypeFrom(Class<A> apiDefinition, Function<A, IClassNameSupplier> nameSupplier);

  /**
   * Checks if an {@link IType} with given fully qualified name exists on the classpath of this
   * {@link IJavaEnvironment}.
   *
   * @param fqn
   *          The fully qualified name to search
   * @return {@code true} if it is on the classpath of this {@link IJavaEnvironment}. {@code false} otherwise.
   * @see #findType(String)
   * @see #exists(IType)
   */
  boolean exists(String fqn);

  /**
   * Checks if the given {@link IType} is also on the classpath of this {@link IJavaEnvironment}. This is always
   * {@code true} if the given {@link IType}s {@link IJavaEnvironment} is {@code this} {@link IJavaEnvironment}.
   *
   * @param t
   *          The {@link IType} to search in this {@link IJavaEnvironment}. May be {@code null}.
   * @return {@code true} if it is on the classpath of this {@link IJavaEnvironment}. {@code false} otherwise.
   * @see #findType(String)
   * @see #exists(String)
   */
  boolean exists(IType t);

  /**
   * Returns an {@link IUnresolvedType} for the given fully qualified name.<br>
   * If the given fully qualified name can be found on the classpath the returned {@link IUnresolvedType} will exist
   * (see {@link IUnresolvedType#exists()}). Otherwise a non-existing {@link IUnresolvedType} will be returned.<br>
   * <br>
   * <b>Note:</b><br>
   * {@link IUnresolvedType}s are never cached in the {@link IJavaEnvironment}. Calls to this method will always create
   * new instances. This allows to get a resolved version later on.
   *
   * @param fqn
   *          The fully qualified name of the type. See {@link #findType(String)} for details.
   * @return A new {@link IUnresolvedType}.
   */
  IUnresolvedType findUnresolvedType(String fqn);

  /**
   * When file system changes occurred and the current {@link IJavaElement}s should not be lost, this method can be
   * called in order to reload the SPI core of the {@link IJavaEnvironment} and replace all SPI cores of the wrapped
   * classes with the updated version.
   * <p>
   * All {@link IJavaElement}s remain valid (if they still exist) and are updated with the new state of the file system
   * including optional overrides that were registered using
   * {@link #registerCompilationUnitOverride(String, String, CharSequence)}
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
   *          The package name of the compilation unit. Use {@code null} for the default package.
   * @param fileName
   *          The filename of the compilation unit (e.g. MyClass.java).
   * @param src
   *          A {@link CharSequence} holding the compilation unit source.
   * @return {@code true} if a type with given fully qualified name was already loaded and a call to {@link #reload()}
   *         would therefore be necessary so that the given type becomes active. {@code false} if the given type has not
   *         been used yet.
   */
  boolean registerCompilationUnitOverride(String packageName, String fileName, CharSequence src);

  /**
   * Unwraps the {@link IJavaEnvironment} into its underlying SPI class.
   *
   * @return The service provider interface that belongs to this {@link IJavaEnvironment}.
   */
  JavaEnvironmentSpi unwrap();

  /**
   * Returns a {@link List} holding a {@link String} for each compile error of the compilation unit containing the type
   * with the given fully qualified name.
   *
   * @param fqn
   *          The fully qualified name of the type. See {@link #findType(String)} for details.
   * @return A {@link List} with an entry for each compile error of the compilation unit that contains the type with the
   *         given name.
   * @throws IllegalArgumentException
   *           If the given fully qualified name cannot be found in this {@link IJavaEnvironment} or it is a binary
   *           type.
   */
  List<String> compileErrors(String fqn);

  /**
   * Returns a {@link List} holding a {@link String} for each compile error of the compilation unit containing the
   * specified type.
   *
   * @param type
   *          The {@link IType} for which the compile errors should be calculated. Must not be {@code null}.
   * @return A {@link List} with an entry for each compile error of the compilation unit that contains the type with the
   *         given name.
   * @throws IllegalArgumentException
   *           If the given {@link IType} is a binary type.
   */
  List<String> compileErrors(IType type);

  /**
   * @return The first entry of all {@link #sourceFolders()} of this {@link IJavaEnvironment}.
   */
  Optional<IClasspathEntry> primarySourceFolder();

  /**
   * @return A {@link Stream} with all {@link IClasspathEntry}s of this {@link IJavaEnvironment}. The order of the
   *         entries returned by the stream are the same as used by the {@link IJavaEnvironment}.
   */
  Stream<IClasspathEntry> classpath();

  /**
   * A {@link Stream} with all {@link IClasspathEntry}s that are source folders (see
   * {@link IClasspathEntry#isSourceFolder()}).
   * <p>
   * <b>Important:</b><br>
   * The resulting {@link Stream} does NOT return the source folders in the order as they are used in this
   * {@link IJavaEnvironment}! Instead the source folders are sorted by importance of the source folder (e.g.
   * {@code src/main/java} comes first).<br>
   * If the source folders are needed in the order of the {@link IJavaEnvironment}, use {@link #classpath()}.
   *
   * @return A {@link Stream} with all {@link IClasspathEntry}s that are source folders sorted by importance.
   * @see IClasspathEntry#isSourceFolder()
   */
  Stream<IClasspathEntry> sourceFolders();

  <A extends IApiSpecification> Optional<A> api(Class<A> apiDefinition);

  <A extends IApiSpecification> A requireApi(Class<A> apiDefinition);
}
