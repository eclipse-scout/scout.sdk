/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.spi;

import org.eclipse.scout.sdk.core.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link ImportSpi}</h3> Represents an import declaration in an {@link CompilationUnitSpi}
 *
 * @since 5.1.0
 */
public interface ImportSpi extends JavaElementSpi {

  /**
   * Gets the {@link CompilationUnitSpi} this import belongs to.
   *
   * @return the {@link CompilationUnitSpi} this import belongs to.
   */
  CompilationUnitSpi getCompilationUnit();

  /**
   * @return the fully qualified name of the type imported.
   */
  String getName();

  /**
   * @return the simple name of the imported type.
   */
  @Override
  String getElementName();

  /**
   * @return the qualifier of the imported type.
   */
  String getQualifier();

  boolean isStatic();

  @Override
  default TreeVisitResult acceptPreOrder(IDepthFirstJavaElementVisitor visitor, int level, int index) {
    return visitor.preVisit(wrap(), level, index);
  }

  @Override
  default boolean acceptPostOrder(IDepthFirstJavaElementVisitor visitor, int level, int index) {
    return visitor.postVisit(wrap(), level, index);
  }

  @Override
  default TreeVisitResult acceptLevelOrder(IBreadthFirstJavaElementVisitor visitor, int level, int index) {
    return visitor.visit(wrap(), level, index);
  }

  @Override
  IImport wrap();
}
