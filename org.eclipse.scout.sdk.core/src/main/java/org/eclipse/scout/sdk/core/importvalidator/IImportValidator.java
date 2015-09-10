/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.importvalidator;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;

/**
 * <h3>{@link IImportValidator}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.7.0
 */
public interface IImportValidator {
  /**
   * a dummy {@link IImportValidator} that can be used for {@link Object#toString()} sort of operations
   */
  IImportValidator DUMMY = new IImportValidator() {

    @Override
    public IJavaEnvironment getJavaEnvironment() {
      return null;
    }

    @Override
    public String getQualifier() {
      return null;
    }

    @Override
    public void addStaticImport(String fqn) {
    }

    @Override
    public void addImport(String fqn) {
    }

    @Override
    public void reserveElement(ImportElementCandidate cand) {
    }

    @Override
    public String registerElement(ImportElementCandidate cand) {
      return cand.getSimpleName();
    }

    @Override
    public String checkExistingImports(ImportElementCandidate cand) {
      return cand.getSimpleName();
    }

    @Override
    public String checkCurrentScope(ImportElementCandidate cand) {
      return null;
    }

    @Override
    public Collection<String> createImportDeclarations() {
      return Collections.emptySet();
    }
  };

  IJavaEnvironment getJavaEnvironment();

  /**
   * @return the qualifier of the scope this {@link IImportValidator} represents.
   *         <p>
   *         {@link ICompilationUnitSourceBuilder} wraps this {@link IImportValidator} and uses the packageName
   *         <p>
   *         Inner {@link ITypeSourceBuilder} use a wrapped {@link IImportValidator} that uses the outer type qualified
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
  void reserveElement(ImportElementCandidate cand);

  /**
   * Do not use this method directly, use {@link SignatureUtils#useSignature(String, IImportValidator)} instead
   *
   * @return the simple name for this signature and registers the name. Use
   *         {@link #checkElement(ImportElementCandidate)} before calling this method.
   */
  String registerElement(ImportElementCandidate cand);

  /**
   * @param cand
   * @return the simple name if there is already an import for this type
   *         <p>
   *         return the qualified name if there is already an import for another type with same simple name
   *         <p>
   *         return null if there is no import yet, call {@link #checkCurrentScope(ImportElementCandidate)} for further
   *         analysis
   */
  String checkExistingImports(ImportElementCandidate cand);

  /**
   * Call {@link #checkExistingImports(ImportElementCandidate)} first.
   *
   * @param cand
   * @return the simple name if the element is visible in the current scope without adding an import.
   *         <p>
   *         return the qualified name if the element would hide another simple name in the current scope.
   *         <p>
   *         return null if the current scope doesn't know such an element. A call to
   *         {@link #registerElement(ImportElementCandidate)} may be performed.
   */
  String checkCurrentScope(ImportElementCandidate cand);

  /**
   * Gets the list of imports to be created.<br>
   *
   * @return An array containing all fully qualified import declarations that needs to be created
   *         <code>import (static)? $qualifiedName;</code> as well as empty string group separators
   */
  Collection<String> createImportDeclarations();
}
