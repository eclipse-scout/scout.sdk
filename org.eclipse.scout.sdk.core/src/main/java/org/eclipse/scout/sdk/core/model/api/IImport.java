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

import org.eclipse.scout.sdk.core.model.spi.ImportSpi;

/**
 * <h3>{@link IImport}</h3> Represents an import declaration in an {@link ICompilationUnit}
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface IImport extends IJavaElement {

  /**
   * Gets the {@link ICompilationUnit} this import belongs to.
   *
   * @return the {@link ICompilationUnit} this import belongs to.
   */
  ICompilationUnit compilationUnit();

  /**
   * @return the fully qualified name of the type imported.
   */
  String name();

  /**
   * @return the simple name of the imported type.
   */
  String simpleName();

  /**
   * @return the qualifier of the imported type.
   */
  String qualifier();

  boolean isStatic();

  @Override
  ImportSpi unwrap();
}
