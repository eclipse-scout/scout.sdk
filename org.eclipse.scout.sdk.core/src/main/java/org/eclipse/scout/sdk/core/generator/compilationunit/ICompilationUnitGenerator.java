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
package org.eclipse.scout.sdk.core.generator.compilationunit;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.comment.ICommentBuilder;
import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.PackageGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link ICompilationUnitGenerator}</h3>
 * <p>
 * {@link IJavaElementGenerator} that creates compilation units.
 *
 * @since 6.1.0
 */
public interface ICompilationUnitGenerator<TYPE extends ICompilationUnitGenerator<TYPE>> extends IJavaElementGenerator<TYPE> {

  /**
   * Gets the name of this {@link ICompilationUnitGenerator}. This is the {@link #fileName()} without the
   * {@link JavaTypes#JAVA_FILE_SUFFIX}.
   *
   * @return The name of this {@link ICompilationUnitGenerator}.
   */
  @Override
  Optional<String> elementName();

  /**
   * @return The filename of this {@link ICompilationUnitGenerator}. This will be the {@link #elementName()} with the
   *         {@link JavaTypes#JAVA_FILE_SUFFIX}.
   */
  Optional<String> fileName();

  /**
   * Sets the name of this {@link ICompilationUnitGenerator}. The name may include the ".java" file suffix or not.
   *
   * @param newName
   *          The new name (optionally including the ".java" file suffix) or {@code null}.
   * @return This generator
   */
  @Override
  TYPE withElementName(String newName);

  /**
   * @return The {@link PackageGenerator} or an empty {@link Optional} if it is the default package.
   */
  Optional<PackageGenerator> getPackage();

  /**
   * @return The package name or an empty {@link Optional} if it is the default package.
   */
  Optional<String> packageName();

  /**
   * Sets the {@link PackageGenerator} of this {@link ICompilationUnitGenerator}.
   *
   * @param generator
   *          The new {@link PackageGenerator}.
   * @return this generator.
   */
  TYPE withPackage(PackageGenerator generator);

  /**
   * Sets the package of this {@link ICompilationUnitGenerator}.
   *
   * @param name
   *          The new package name.
   * @return this generator.
   */
  TYPE withPackageName(String name);

  /**
   * @return The main {@link ITypeGenerator} in this {@link ICompilationUnitGenerator}. The main type is the one having
   *         the {@code public} modifier.
   */
  Optional<ITypeGenerator<?>> mainType();

  /**
   * Adds the specified import to this {@link ICompilationUnitGenerator}.
   *
   * @param name
   *          The import to add without leading import keyword or trailing semicolon. Must not be blank (see
   *          {@link Strings#isBlank(CharSequence)}.
   * @return This generator.
   */
  TYPE withImport(CharSequence name);

  /**
   * Removes the specified import from this {@link ICompilationUnitGenerator}.
   *
   * @param name
   *          The import to remove without leading import keyword or trailing semicolon.
   * @return This generator.
   */
  TYPE withoutImport(CharSequence name);

  /**
   * @return A {@link Stream} returning all imports of this {@link ICompilationUnitGenerator}.
   */
  Stream<CharSequence> imports();

  /**
   * Adds the specified static import to this {@link ICompilationUnitGenerator}.
   *
   * @param name
   *          The static import to add without leading import keyword or trailing semicolon. Must not be blank (see
   *          {@link Strings#isBlank(CharSequence)}.
   * @return This generator.
   */
  TYPE withStaticImport(CharSequence name);

  /**
   * Removes the specified static import from this {@link ICompilationUnitGenerator}.
   *
   * @param name
   *          The static import to remove without leading import keyword or trailing semicolon.
   * @return This generator.
   */
  TYPE withoutStaticImport(CharSequence name);

  /**
   * @return A {@link Stream} returning all static imports of this {@link ICompilationUnitGenerator}.
   */
  Stream<CharSequence> staticImports();

  /**
   * Removes all imports from this {@link ICompilationUnitGenerator}.
   *
   * @return This generator.
   */
  TYPE withoutAllImports();

  /**
   * @return A {@link Stream} returning all top level {@link ITypeGenerator}s in this {@link ICompilationUnitGenerator}.
   */
  Stream<ITypeGenerator<?>> types();

  /**
   * Adds the specified {@link ITypeGenerator} to this {@link ICompilationUnitGenerator}.
   *
   * @param generator
   *          The {@link ITypeGenerator} to add. Must not be {@code null}.
   * @param sortObjects
   *          Optional elements used to define the position of the {@link ITypeGenerator} within this
   *          {@link ICompilationUnitGenerator}. May be {@code null} or omitted (in that case a default position is
   *          calculated). The generators are sorted according to the natural order of the elements specified.
   * @return This generator.
   * @see TypeGenerator#create()
   */
  TYPE withType(ITypeGenerator<?> generator, Object... sortObjects);

  /**
   * Removes the {@link ITypeGenerator} with given {@link #elementName()} from this {@link ICompilationUnitGenerator}.
   *
   * @param elementName
   *          The simple name of the {@link ITypeGenerator} to remove. Must not be {@code null}.
   * @return This generator.
   */
  TYPE withoutType(Predicate<ITypeGenerator<?>> removalFilter);

  /**
   * Adds the specified footer generator to this {@link ICompilationUnitGenerator}.
   * <p>
   * A footer is appended after the last {@link ITypeGenerator} of this {@link ICompilationUnitGenerator}.
   *
   * @param footerGenerator
   *          The {@link ISourceGenerator} to add. May be {@code null}. In that case this method does nothing.
   * @return This generator.
   */
  TYPE withFooter(ISourceGenerator<ICommentBuilder<?>> footerGenerator);

  /**
   * @return A {@link Stream} with all footers of this {@link ICompilationUnitGenerator}.
   */
  Stream<ISourceGenerator<ICommentBuilder<?>>> footers();
}
