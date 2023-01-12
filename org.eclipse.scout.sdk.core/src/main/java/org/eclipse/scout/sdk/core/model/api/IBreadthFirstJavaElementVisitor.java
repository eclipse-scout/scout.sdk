/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api;

import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link IBreadthFirstJavaElementVisitor}</h3>
 * <p>
 * Visitor to traverse an {@link IJavaElement} tree in a <i>Breadth-First</i> (BFS) strategy (level-order).
 *
 * @since 8.0.0
 * @see IJavaElement#visit(IBreadthFirstJavaElementVisitor)
 * @see TreeVisitResult
 */
public interface IBreadthFirstJavaElementVisitor {

  /**
   * Visit callback for {@link ICompilationUnit}s.
   *
   * @param icu
   *          The visited {@link ICompilationUnit}. Is never {@code null}.
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult visit(ICompilationUnit icu);

  /**
   * Visit callback for {@link IType}s.
   *
   * @param type
   *          The visited {@link IType}. Is never {@code null}.
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
  TreeVisitResult visit(IType type, int level, int index);

  /**
   * Visit callback for {@link IField}s.
   *
   * @param field
   *          The visited {@link IField}. Is never {@code null}.
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
  TreeVisitResult visit(IField field, int level, int index);

  /**
   * Visit callback for {@link IMethod}s.
   *
   * @param method
   *          The visited {@link IMethod}. Is never {@code null}.
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
  TreeVisitResult visit(IMethod method, int level, int index);

  /**
   * Visit callback for {@link IMethodParameter}s.
   *
   * @param methodParameter
   *          The visited {@link IMethodParameter}. Is never {@code null}.
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
  TreeVisitResult visit(IMethodParameter methodParameter, int level, int index);

  /**
   * Visit callback for {@link IAnnotation}s.
   *
   * @param annotation
   *          The visited {@link IAnnotation}. Is never {@code null}.
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
  TreeVisitResult visit(IAnnotation annotation, int level, int index);

  /**
   * Visit callback for {@link IAnnotationElement}s.
   *
   * @param annotationElement
   *          The visited {@link IAnnotationElement}. Is never {@code null}.
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
  TreeVisitResult visit(IAnnotationElement annotationElement, int level, int index);

  /**
   * Visit callback for {@link IImport}s.
   *
   * @param imp
   *          The visited {@link IImport}. Is never {@code null}.
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
  TreeVisitResult visit(IImport imp, int level, int index);

  /**
   * Visit callback for {@link IPackage}.
   *
   * @param pck
   *          The visited {@link IPackage}. Is never {@code null}.
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
  TreeVisitResult visit(IPackage pck, int level, int index);

  /**
   * Visit callback for {@link ITypeParameter}s.
   *
   * @param typeParameter
   *          The visited {@link ITypeParameter}. Is never {@code null}.
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
  TreeVisitResult visit(ITypeParameter typeParameter, int level, int index);

  /**
   * Visit callback for {@link IUnresolvedType}s.
   *
   * @param unresolvedType
   *          The visited {@link IUnresolvedType}. Is never {@code null}.
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
  TreeVisitResult visit(IUnresolvedType unresolvedType, int level, int index);
}
