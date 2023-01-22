/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.model.api.query.InnerTypeQuery;
import org.eclipse.scout.sdk.core.java.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.util.SourceRange;

/**
 * <h3>{@link ICompilationUnit}</h3> Represents a compilation unit usually defined by a .java file.
 *
 * @since 5.1.0
 */
public interface ICompilationUnit extends IJavaElement {

  /**
   * Synthetic {@link ICompilationUnit}s are based on binary {@link IType}s. Such {@link ICompilationUnit}s have a
   * singleton type list, no imports, no absolute path and no source attached.
   *
   * @return {@code true} if this {@link ICompilationUnit} is synthetic based on a binary type.
   */
  boolean isSynthetic();

  /**
   * Gets the {@link IPackage} of this {@link ICompilationUnit}.
   *
   * @return The {@link IPackage} of this {@link ICompilationUnit}.
   */
  IPackage containingPackage();

  /**
   * @return The {@link IClasspathEntry} in which this {@link ICompilationUnit} was found. Returns an empty
   *         {@link Optional} if this {@link ICompilationUnit} is synthetic or does not come from a directory (but e.g.
   *         a jar instead).
   */
  Optional<IClasspathEntry> containingClasspathFolder();

  /**
   * Gets all import declarations in this {@link ICompilationUnit}.
   * <p>
   * Please note that imports are only available if this {@link ICompilationUnit} is not {@link #isSynthetic()
   * synthetic}. For synthetic {@link ICompilationUnit} instances this method always returns an empty {@link Stream}.
   *
   * @return A {@link Stream} containing all imports in the order as they appear in the source.
   */
  Stream<IImport> imports();

  /**
   * Gets a {@link InnerTypeQuery} which by default returns all direct {@link IType}s in this {@link ICompilationUnit}.
   *
   * @return A new {@link InnerTypeQuery} for the nested {@link IType}s of this {@link ICompilationUnit}.
   */
  InnerTypeQuery types();

  /**
   * Gets the main {@link IType} of this {@link ICompilationUnit}. This is the {@link IType} whose name matches the name
   * of the java file.
   *
   * @return The main {@link IType} or an empty {@link Optional} if no main type is defined in this
   *         {@link ICompilationUnit}.
   * @see #requireMainType()
   */
  Optional<IType> mainType();

  /**
   * Same as {@link #mainType()} but throws an {@link IllegalArgumentException} if this {@link ICompilationUnit} has no
   * main {@link IType}.
   *
   * @return The main {@link IType} of this {@link ICompilationUnit}. This is the type that matches the compilation unit
   *         name.
   * @throws IllegalArgumentException
   *           if this {@link ICompilationUnit} has no main type.
   * @see #mainType()
   */
  IType requireMainType();

  /**
   * Resolves the given simple type name in the context of this {@link ICompilationUnit} to an {@link IType}.
   *
   * @param simpleName
   *          The simple class name to search in the context of this {@link ICompilationUnit}.
   * @return The {@link IType} with given simpleName as it is referenced by this {@link ICompilationUnit} or an empty
   *         {@link Optional} if no such simpleName is referenced by this {@link ICompilationUnit}.
   */
  Optional<IType> resolveTypeBySimpleName(String simpleName);

  /**
   * Gets the java doc source of this {@link ICompilationUnit}. This is the java doc added on top of the java file
   * (before the imports).
   *
   * @return The {@link SourceRange} for the java doc of this {@link ICompilationUnit}.
   */
  Optional<SourceRange> javaDoc();

  /**
   * Gets the classpath relative path of this {@link ICompilationUnit}.
   * <p>
   * <b>Note</b>: {@link Path#toString()} uses platform dependent name separators! This may be wrong if the compilation
   * unit is inside an archive.
   *
   * @return The {@link Path} of this {@link ICompilationUnit} relative to the classpath root.<br>
   *         E.g. "org/eclipse/scout/myapp/MyClass.java"
   */
  Path path();

  /**
   * @return The absolute {@link Path} of this {@link ICompilationUnit} on the local file system. The {@link Optional}
   *         is empty if this is a synthetic compilation unit (see {@link #isSynthetic()}).
   * @see #isSynthetic()
   */
  Optional<Path> absolutePath();

  /**
   * @return The name of this {@link ICompilationUnit}. This is the name of the java file.<br>
   *         E.g. "MyClass.java"
   */
  @Override
  String elementName();

  @Override
  CompilationUnitSpi unwrap();

  @Override
  ICompilationUnitGenerator<?> toWorkingCopy();

  @Override
  ICompilationUnitGenerator<?> toWorkingCopy(IWorkingCopyTransformer transformer);
}
