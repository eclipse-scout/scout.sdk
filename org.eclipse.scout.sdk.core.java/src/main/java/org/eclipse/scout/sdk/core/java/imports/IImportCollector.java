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

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;

/**
 * <h3>{@link IImportCollector}</h3>
 *
 * @since 5.2
 */
public interface IImportCollector {

  /**
   * @return The {@link IJavaBuilderContext} this {@link IImportCollector} belongs to.
   */
  Optional<IJavaBuilderContext> getContext();

  /**
   * @return The {@link IJavaEnvironment} of the {@link #getContext() context} (see
   *         {@link IJavaBuilderContext#environment()}).
   */
  Optional<IJavaEnvironment> getJavaEnvironment();

  /**
   * @return the qualifier of the scope this {@link IImportCollector} represents.
   *         <p>
   *         {@link ICompilationUnitGenerator} wraps an {@link IImportCollector} and uses the packageName.
   *         <p>
   *         Inner {@link ITypeGenerator} wraps an {@link IImportCollector} and uses the declaring type fully qualified
   *         name.
   */
  String getQualifier();

  /**
   * Adds the given fully qualified name to the list of static imports to be created.<br>
   * Any already existing mapping for the same simple name of the given qualified name will be replaced.
   *
   * @param fqn
   *          The fully qualified name to add.
   */
  void addStaticImport(CharSequence fqn);

  /**
   * Adds the given fully qualified name to the list of imports to be created.<br>
   * Any already existing mapping for the same simple name of the given qualified name will be replaced.
   *
   * @param fqn
   *          The fully qualified name to add.
   */
  void addImport(CharSequence fqn);

  /**
   * Reserve the name for potential usages. This does not already add an import declaration but reserves the potential
   * import declaration in case a different package name is asked for its import resolved name.
   *
   * @param candidate
   *          The {@link TypeReferenceDescriptor} to use
   */
  void reserveElement(TypeReferenceDescriptor candidate);

  /**
   * Do not use this method directly, use {@link IImportValidator#useReference(CharSequence)} instead
   *
   * @return the simple name for this signature and registers the name. Use
   *         {@link #checkCurrentScope(TypeReferenceDescriptor)} and
   *         {@link #checkExistingImports(TypeReferenceDescriptor)} before calling this method.
   */
  String registerElement(TypeReferenceDescriptor candidate);

  /**
   * @param candidate
   *          The {@link TypeReferenceDescriptor} to check.
   * @return the simple name if there is already an import for this type
   *         <p>
   *         return the qualified name if there is already an import for another type with same simple name
   *         <p>
   *         return null if there is no import yet, call {@link #checkCurrentScope(TypeReferenceDescriptor)} for further
   *         analysis
   */
  String checkExistingImports(TypeReferenceDescriptor candidate);

  /**
   * Call {@link #checkExistingImports(TypeReferenceDescriptor)} first.
   *
   * @param candidate
   *          The {@link TypeReferenceDescriptor} to check.
   * @return the simple name if the element is visible in the current scope without adding an import.
   *         <p>
   *         return the qualified name if the element would hide another simple name in the current scope.
   *         <p>
   *         return null if the current scope doesn't know such an element. A call to
   *         {@link #registerElement(TypeReferenceDescriptor)} may be performed.
   */
  String checkCurrentScope(TypeReferenceDescriptor candidate);

  /**
   * Gets the list of imports to be created (sorted and grouped, including empty lines).<br>
   * If the {@link IImportCollector} is based on an existing {@link ICompilationUnit}, the already existing imports are
   * part of the result as well!
   *
   * @return A {@link Collection} containing all fully qualified {@code import} declarations (including import keyword
   *         and flags) that needs to be created (e.g. {@code import (static)? $qualifiedName;}) as well as empty string
   *         group separators.
   */
  Stream<StringBuilder> createImportDeclarations();

  /**
   * Gets the list of imports to be created (sorted and grouped, including empty lines).
   *
   * @param includeExisting
   *          Specifies if imports that already exist on the underlying {@link ICompilationUnit} should be returned as
   *          well.
   * @return A {@link Collection} containing all fully qualified {@code import} declarations (including import keyword
   *         and flags) that needs to be created (e.g. {@code import (static)? $qualifiedName;}) as well as empty string
   *         group separators.
   */
  Stream<StringBuilder> createImportDeclarations(boolean includeExisting);

  /**
   * Gets all fully qualified static imports of this collector.
   *
   * @return A {@link Collection} with all fully qualified static imports.
   */
  Stream<StringBuilder> getStaticImports();

  /**
   * Gets all fully qualified non-static imports of this collector.
   *
   * @return A {@link Collection} with all fully qualified non-static imports.
   */
  Stream<StringBuilder> getImports();
}
