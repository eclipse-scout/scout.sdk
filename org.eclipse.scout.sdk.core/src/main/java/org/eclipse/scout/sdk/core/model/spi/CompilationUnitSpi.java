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
package org.eclipse.scout.sdk.core.model.spi;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.core.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link CompilationUnitSpi}</h3> Represents a compilation unit usually defined by a .java file.
 *
 * @since 5.1.0
 */
public interface CompilationUnitSpi extends JavaElementSpi {

  /**
   * @return true if this {@link CompilationUnitSpi} is synthetic based on a binary type.
   *         <p>
   *         Synthetic {@link CompilationUnitSpi}s have a singleton type list, no imports and no source attached
   */
  boolean isSynthetic();

  /**
   * Gets the {@link PackageSpi} of this {@link CompilationUnitSpi}.
   *
   * @return The {@link PackageSpi} of this {@link CompilationUnitSpi} or the default package
   *         ({@link PackageSpi#getElementName()} is {@code null}).
   */
  PackageSpi getPackage();

  /**
   * Gets a {@link Map} that contains all imports. The {@link Map} iterates over the imports in the order as they appear
   * in the source.
   *
   * @return A {@link Map} with the import simple name as key and the corresponding {@link ImportSpi} as value.
   */
  List<? extends ImportSpi> getImports();

  /**
   * Gets all {@link TypeSpi}s that are defined in this {@link CompilationUnitSpi} in the order as they are defined in
   * the java source file.
   *
   * @return A {@link List} with all {@link TypeSpi}s of this {@link CompilationUnitSpi}.
   */
  List<? extends TypeSpi> getTypes();

  /**
   * Gets the main {@link TypeSpi} of this {@link CompilationUnitSpi}. This is the {@link TypeSpi} whose name matches
   * the name of the java file.
   *
   * @return The main {@link TypeSpi} or {@code null} if no main type is defined in this {@link CompilationUnitSpi} .
   */
  TypeSpi getMainType();

  /**
   * Resolves the given simple type name in the context of this {@link CompilationUnitSpi} to an {@link TypeSpi}.
   *
   * @param simpleName
   *          The simple class name to search in the context of this {@link CompilationUnitSpi}.
   * @return The {@link TypeSpi} with given simpleName as it is referenced by this {@link CompilationUnitSpi} or
   *         {@code null} if no such simpleName is referenced by this {@link CompilationUnitSpi}.
   */
  TypeSpi findTypeBySimpleName(String simpleName);

  ISourceRange getJavaDoc();

  @Override
  default TreeVisitResult acceptPreOrder(IDepthFirstJavaElementVisitor visitor, int level, int index) {
    return visitor.preVisit(wrap());
  }

  @Override
  default boolean acceptPostOrder(IDepthFirstJavaElementVisitor visitor, int level, int index) {
    return visitor.postVisit(wrap());
  }

  @Override
  default TreeVisitResult acceptLevelOrder(IBreadthFirstJavaElementVisitor visitor, int level, int index) {
    return visitor.visit(wrap());
  }

  @Override
  ICompilationUnit wrap();
}
