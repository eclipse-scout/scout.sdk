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
package org.eclipse.scout.sdk.core.model;

/**
 * <h3>{@link IImportDeclaration}</h3>
 * Represents an import declaration in an {@link ICompilationUnit}
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IImportDeclaration {

  /**
   * Gets the {@link ICompilationUnit} this import belongs to.
   * 
   * @return the {@link ICompilationUnit} this import belongs to.
   */
  ICompilationUnit getCompilationUnit();

  /**
   * @return the fully qualified name of the type imported.
   */
  String getName();

  /**
   * @return the simple name of the imported type.
   */
  String getSimpleName();

  /**
   * @return the qualifier of the imported type.
   */
  String getQualifier();

}
