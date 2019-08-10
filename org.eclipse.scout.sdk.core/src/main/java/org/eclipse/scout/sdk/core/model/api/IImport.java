/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import org.eclipse.scout.sdk.core.model.spi.ImportSpi;

/**
 * <h3>{@link IImport}</h3> Represents an import declaration in an {@link ICompilationUnit}
 *
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
   * Gets the last segment of the import (without wildcards).<br>
   * If the import is a type or method import, the simple type name or method name is returned.<br>
   * If the import is a wildcard package import (e.g. java.util.*), the last package segment is returned.
   *
   * @return The last non-wildcard segment of the import.
   */
  @Override
  String elementName();

  /**
   * Gets the full import name.<br>
   * If the import is a type or method import, the fully qualified name of the type or method is returned.<br>
   * If the import contains a wildcard (e.g. java.util.*), the wildcard will be part of the full import name which then
   * denotes a package.
   *
   * @return The full import name.
   */
  String name();

  /**
   * Gets all but the last segments of the import (without wildcards).<br>
   * If the import is a type import, the qualifier of the type is returned.<br>
   * If the import is a method import, the fully qualified name of the declaring type of the method is returned.<br>
   * If the import is a wildcard package import (e.g. java.util.*), all but the last segments are returned.
   *
   * @return All but the last non-wildcard segments of the import.
   */
  String qualifier();

  /**
   * Specifies if it is a static method import.
   *
   * @return {@code true} if it is a static method import, {@code false} otherwise.
   */
  boolean isStatic();

  @Override
  ImportSpi unwrap();
}
