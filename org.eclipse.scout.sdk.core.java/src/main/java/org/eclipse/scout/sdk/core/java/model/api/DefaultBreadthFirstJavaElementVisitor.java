/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link DefaultBreadthFirstJavaElementVisitor}</h3>
 * <p>
 * Default Adapter implementation for an {@link IBreadthFirstJavaElementVisitor}.
 * <p>
 * This class is intended to be subclassed by clients.
 *
 * @since 8.0.0
 */
public class DefaultBreadthFirstJavaElementVisitor implements IBreadthFirstJavaElementVisitor {

  @Override
  public TreeVisitResult visit(ICompilationUnit icu) {
    return visitElement(icu, 0, 0);
  }

  @Override
  public TreeVisitResult visit(IType type, int level, int index) {
    return visitElement(type, level, index);
  }

  @Override
  public TreeVisitResult visit(IField field, int level, int index) {
    return visitElement(field, level, index);
  }

  @Override
  public TreeVisitResult visit(IMethod method, int level, int index) {
    return visitElement(method, level, index);
  }

  @Override
  public TreeVisitResult visit(IMethodParameter methodParameter, int level, int index) {
    return visitElement(methodParameter, level, index);
  }

  @Override
  public TreeVisitResult visit(IAnnotation annotation, int level, int index) {
    return visitElement(annotation, level, index);
  }

  @Override
  public TreeVisitResult visit(IAnnotationElement annotationElement, int level, int index) {
    return visitElement(annotationElement, level, index);
  }

  @Override
  public TreeVisitResult visit(IImport imp, int level, int index) {
    return visitElement(imp, level, index);
  }

  @Override
  public TreeVisitResult visit(IPackage pck, int level, int index) {
    return visitElement(pck, level, index);
  }

  @Override
  public TreeVisitResult visit(ITypeParameter typeParameter, int level, int index) {
    return visitElement(typeParameter, level, index);
  }

  @Override
  public TreeVisitResult visit(IUnresolvedType unresolvedType, int level, int index) {
    return visitElement(unresolvedType, level, index);
  }

  /**
   * Untyped visit callback.
   *
   * @param element
   *          The visited element. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  protected TreeVisitResult visitElement(IJavaElement element, int level, int index) {
    return TreeVisitResult.CONTINUE;
  }
}
