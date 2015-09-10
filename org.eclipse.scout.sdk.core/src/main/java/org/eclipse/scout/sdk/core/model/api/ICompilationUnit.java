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

import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.sugar.TypeQuery;

/**
 * <h3>{@link ICompilationUnit}</h3> Represents a compilation unit usually defined by a .java file.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface ICompilationUnit extends IJavaElement {

  /**
   * @return true if this {@link ICompilationUnit} is synthetic based on a binary type {@link BindingType}
   *         <p>
   *         Synthetic {@link ICompilationUnit}s have a singleton type list, no imports and no source attached
   */
  boolean isSynthetic();

  /**
   * Gets the {@link IPackage} of this {@link ICompilationUnit}.
   *
   * @return The {@link IPackage} of this {@link ICompilationUnit} or {@link IPackage#DEFAULT_PACKAGE} for the default
   *         package.
   */
  IPackage getPackage();

  /**
   * Gets a {@link Map} that contains all imports. The {@link Map} iterates over the imports in the order as they appear
   * in the source.
   */
  List<IImport> getImports();

  /**
   * Gets all {@link IType}s that are defined in this {@link ICompilationUnit} in the order as they are defined in the
   * java source file.
   *
   * @return A {@link List} with all {@link IType}s of this {@link ICompilationUnit}.
   */
  List<IType> getTypes();

  /**
   * Gets the main {@link IType} of this {@link ICompilationUnit}. This is the {@link IType} whose name matches the name
   * of the java file.
   *
   * @return The main {@link IType} or <code>null</code> if no main type is defined in this {@link ICompilationUnit}.
   */
  IType getMainType();

  /**
   * Resolves the given simple type name in the context of this {@link ICompilationUnit} to an {@link IType}.
   *
   * @param simpleName
   *          The simple class name to search in the context of this {@link ICompilationUnit}.
   * @return The {@link IType} with given simpleName as it is referenced by this {@link ICompilationUnit} or
   *         <code>null</code> if no such simpleName is referenced by this {@link ICompilationUnit}.
   */
  IType findTypeBySimpleName(String simpleName);

  ISourceRange getJavaDoc();

  @Override
  CompilationUnitSpi unwrap();

  //additional convenience methods

  TypeQuery types();
}
