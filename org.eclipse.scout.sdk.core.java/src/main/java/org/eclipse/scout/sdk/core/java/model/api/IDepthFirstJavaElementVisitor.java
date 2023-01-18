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
 * Visitor to traverse an {@link IJavaElement} tree in a <i>Depth-First</i> (DFS) strategy.<br>
 * Using this visitor a pre-order (top-down) or post-order (bottom-up) traversal can be implemented.
 *
 * @since 8.0
 * @see IJavaElement#visit(IDepthFirstJavaElementVisitor)
 * @see TreeVisitResult
 */
public interface IDepthFirstJavaElementVisitor {

  /**
   * Callback to implement a pre-order traversal for {@link ICompilationUnit}s.
   *
   * @param icu
   *          The visited {@link ICompilationUnit}. Is never {@code null}.
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(ICompilationUnit)} will not be
   *         called for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(ICompilationUnit icu);

  /**
   * Callback to implement a pre-order traversal for {@link IType}s.
   *
   * @param type
   *          The visited {@link IType}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(IType, int, int)} will not be
   *         called for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(IType type, int level, int index);

  /**
   * Callback to implement a pre-order traversal for {@link IField}s.
   *
   * @param field
   *          The visited {@link IField}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(IField, int, int)} will not be
   *         called for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(IField field, int level, int index);

  /**
   * Callback to implement a pre-order traversal for {@link IMethod}s.
   *
   * @param method
   *          The visited {@link IMethod}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(IMethod, int, int)} will not be
   *         called for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(IMethod method, int level, int index);

  /**
   * Callback to implement a pre-order traversal for {@link IMethodParameter}s.
   *
   * @param methodParameter
   *          The visited {@link IMethodParameter}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(IMethodParameter, int, int)} will
   *         not be called for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(IMethodParameter methodParameter, int level, int index);

  /**
   * Callback to implement a pre-order traversal for {@link IAnnotation}s.
   *
   * @param annotation
   *          The visited {@link IAnnotation}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(IAnnotation, int, int)} will not be
   *         called for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(IAnnotation annotation, int level, int index);

  /**
   * Callback to implement a pre-order traversal for {@link IAnnotationElement}s.
   *
   * @param annotationElement
   *          The visited {@link IAnnotationElement}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(IAnnotationElement, int, int)} will
   *         not be called for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(IAnnotationElement annotationElement, int level, int index);

  /**
   * Callback to implement a pre-order traversal for {@link IImport}s.
   *
   * @param imp
   *          The visited {@link IImport}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(IImport, int, int)} will not be
   *         called for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(IImport imp, int level, int index);

  /**
   * Callback to implement a pre-order traversal for {@link IPackage}s.
   *
   * @param pck
   *          The visited {@link IPackage}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(IPackage, int, int)} will not be
   *         called for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(IPackage pck, int level, int index);

  /**
   * Callback to implement a pre-order traversal for {@link ITypeParameter}s.
   *
   * @param typeParameter
   *          The visited {@link ITypeParameter}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(ITypeParameter, int, int)} will not
   *         be called for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(ITypeParameter typeParameter, int level, int index);

  /**
   * Callback to implement a pre-order traversal for {@link IUnresolvedType}s.
   *
   * @param unresolvedType
   *          The visited {@link IUnresolvedType}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisit(IUnresolvedType, int, int)} will
   *         not be called for the current element.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  TreeVisitResult preVisit(IUnresolvedType unresolvedType, int level, int index);

  /**
   * Callback to implement a post-order traversal for {@link ICompilationUnit}s.
   * <p>
   * If the previous {@link #preVisit(ICompilationUnit)} call returned {@link TreeVisitResult#TERMINATE}, this post
   * visit call will not be executed.
   *
   * @param icu
   *          The visited {@link ICompilationUnit}. Is never {@code null}.
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}). {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}).
   */
  boolean postVisit(ICompilationUnit icu);

  /**
   * Callback to implement a post-order traversal for {@link IType}s.
   * <p>
   * If the previous {@link #preVisit(IType, int, int)} call returned {@link TreeVisitResult#TERMINATE}, this post visit
   * call will not be executed.
   *
   * @param type
   *          The visited {@link IType}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}). {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}).
   */
  boolean postVisit(IType type, int level, int index);

  /**
   * Callback to implement a post-order traversal for {@link IField}s.
   * <p>
   * If the previous {@link #preVisit(IField, int, int)} call returned {@link TreeVisitResult#TERMINATE}, this post
   * visit call will not be executed.
   *
   * @param field
   *          The visited {@link IField}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}). {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}).
   */
  boolean postVisit(IField field, int level, int index);

  /**
   * Callback to implement a post-order traversal for {@link IMethod}s.
   * <p>
   * If the previous {@link #preVisit(IMethod, int, int)} call returned {@link TreeVisitResult#TERMINATE}, this post
   * visit call will not be executed.
   *
   * @param method
   *          The visited {@link IMethod}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}). {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}).
   */
  boolean postVisit(IMethod method, int level, int index);

  /**
   * Callback to implement a post-order traversal for {@link IMethodParameter}s.
   * <p>
   * If the previous {@link #preVisit(IMethodParameter, int, int)} call returned {@link TreeVisitResult#TERMINATE}, this
   * post visit call will not be executed.
   *
   * @param param
   *          The visited {@link IMethodParameter}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}). {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}).
   */
  boolean postVisit(IMethodParameter param, int level, int index);

  /**
   * Callback to implement a post-order traversal for {@link IAnnotation}s.
   * <p>
   * If the previous {@link #preVisit(IAnnotation, int, int)} call returned {@link TreeVisitResult#TERMINATE}, this post
   * visit call will not be executed.
   *
   * @param annotation
   *          The visited {@link IAnnotation}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}). {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}).
   */
  boolean postVisit(IAnnotation annotation, int level, int index);

  /**
   * Callback to implement a post-order traversal for {@link IAnnotationElement}s.
   * <p>
   * If the previous {@link #preVisit(IAnnotationElement, int, int)} call returned {@link TreeVisitResult#TERMINATE},
   * this post visit call will not be executed.
   *
   * @param annotationElement
   *          The visited {@link IAnnotationElement}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}). {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}).
   */
  boolean postVisit(IAnnotationElement annotationElement, int level, int index);

  /**
   * Callback to implement a post-order traversal for {@link IImport}s.
   * <p>
   * If the previous {@link #preVisit(IImport, int, int)} call returned {@link TreeVisitResult#TERMINATE}, this post
   * visit call will not be executed.
   *
   * @param imp
   *          The visited {@link IImport}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}). {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}).
   */
  boolean postVisit(IImport imp, int level, int index);

  /**
   * Callback to implement a post-order traversal for {@link IPackage}s.
   * <p>
   * If the previous {@link #preVisit(IPackage, int, int)} call returned {@link TreeVisitResult#TERMINATE}, this post
   * visit call will not be executed.
   *
   * @param pck
   *          The visited {@link IPackage}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}). {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}).
   */
  boolean postVisit(IPackage pck, int level, int index);

  /**
   * Callback to implement a post-order traversal for {@link ITypeParameter}s.
   * <p>
   * If the previous {@link #preVisit(ITypeParameter, int, int)} call returned {@link TreeVisitResult#TERMINATE}, this
   * post visit call will not be executed.
   *
   * @param typeParameter
   *          The visited {@link ITypeParameter}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}). {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}).
   */
  boolean postVisit(ITypeParameter typeParameter, int level, int index);

  /**
   * Callback to implement a post-order traversal for {@link IUnresolvedType}s.
   * <p>
   * If the previous {@link #preVisit(IUnresolvedType, int, int)} call returned {@link TreeVisitResult#TERMINATE}, this
   * post visit call will not be executed.
   *
   * @param unresolvedType
   *          The visited {@link IUnresolvedType}. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}). {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}).
   */
  boolean postVisit(IUnresolvedType unresolvedType, int level, int index);
}
