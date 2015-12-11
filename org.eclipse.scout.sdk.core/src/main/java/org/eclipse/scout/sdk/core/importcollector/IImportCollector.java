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
package org.eclipse.scout.sdk.core.importcollector;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.signature.SignatureDescriptor;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;

/**
 * <h3>{@link IImportCollector}</h3>
 *
 * @author Ivan Motsch, Matthias Villiger
 * @since 5.2
 */
public interface IImportCollector {

  IJavaEnvironment getJavaEnvironment();

  /**
   * @return the qualifier of the scope this {@link IImportCollector} represents.
   *         <p>
   *         {@link ICompilationUnitSourceBuilder} wraps this {@link IImportCollector} and uses the packageName
   *         <p>
   *         Inner {@link ITypeSourceBuilder} use a wrapped {@link IImportCollector} that uses the outer type qualified
   *         name
   */
  String getQualifier();

  /**
   * Adds the given fully qualified name to the list of static imports to be created.<br>
   * Any already existing mapping for the same simple name of the given qualified name will be replaced.
   *
   * @param fqn
   *          The fully qualified name to add.
   */
  void addStaticImport(String fqn);

  /**
   * Adds the given fully qualified name to the list of imports to be created.<br>
   * Any already existing mapping for the same simple name of the given qualified name will be replaced.
   *
   * @param fqn
   *          The fully qualified name to add.
   */
  void addImport(String fqn);

  /**
   * Reserve the name for potential usages. This does not already add an import declaration but reserves the potential
   * import declaration in case a different package name is asked for its import resolved name.
   *
   * @param signature
   *          a signature e.g. 'Ljava.lang.String;'
   */
  void reserveElement(SignatureDescriptor cand);

  /**
   * Do not use this method directly, use {@link SignatureUtils#useSignature(String, IImportCollector)} instead
   *
   * @return the simple name for this signature and registers the name. Use {@link #checkElement(SignatureDescriptor)}
   *         before calling this method.
   */
  String registerElement(SignatureDescriptor cand);

  /**
   * @param cand
   * @return the simple name if there is already an import for this type
   *         <p>
   *         return the qualified name if there is already an import for another type with same simple name
   *         <p>
   *         return null if there is no import yet, call {@link #checkCurrentScope(SignatureDescriptor)} for further
   *         analysis
   */
  String checkExistingImports(SignatureDescriptor cand);

  /**
   * Call {@link #checkExistingImports(SignatureDescriptor)} first.
   *
   * @param cand
   * @return the simple name if the element is visible in the current scope without adding an import.
   *         <p>
   *         return the qualified name if the element would hide another simple name in the current scope.
   *         <p>
   *         return null if the current scope doesn't know such an element. A call to
   *         {@link #registerElement(SignatureDescriptor)} may be performed.
   */
  String checkCurrentScope(SignatureDescriptor cand);

  /**
   * Gets the list of imports to be created (sorted and grouped, including empty lines).<br>
   * If the {@link IImportCollector} is based on an existing {@link ICompilationUnit}, the already existing imports are
   * part of the result as well!
   *
   * @return A {@link Collection} containing all fully qualified <code>import</code> declarations (including import
   *         keyword and flags) that needs to be created (e.g. <code>import (static)? $qualifiedName;</code>) as well as
   *         empty string group separators.
   */
  List<String> createImportDeclarations();

  /**
   * Gets the list of imports to be created (sorted and grouped, including empty lines).
   * 
   * @param includeExisting
   *          Specifies if imports that already exist on the underlying {@link ICompilationUnit} should be returned as
   *          well.
   * @return A {@link Collection} containing all fully qualified <code>import</code> declarations (including import
   *         keyword and flags) that needs to be created (e.g. <code>import (static)? $qualifiedName;</code>) as well as
   *         empty string group separators.
   */
  List<String> createImportDeclarations(boolean includeExisting);

  /**
   * Gets all fully qualified static imports of this collector.
   *
   * @return A {@link Collection} with all fully qualified static imports.
   */
  Collection<String> getStaticImports();

  /**
   * Gets all fully qualified non-static imports of this collector.
   *
   * @return A {@link Collection} with all fully qualified non-static imports.
   */
  Collection<String> getImports();
}
