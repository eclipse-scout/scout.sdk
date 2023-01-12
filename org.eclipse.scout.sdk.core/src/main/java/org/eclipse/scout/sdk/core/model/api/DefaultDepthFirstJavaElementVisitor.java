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
 * <h3>{@link DefaultDepthFirstJavaElementVisitor}</h3>
 * <p>
 * Default Adapter implementation for an {@link IDepthFirstJavaElementVisitor}.
 * <p>
 * This class is intended to be subclassed by clients.
 *
 * @since 8.0.0
 */
public class DefaultDepthFirstJavaElementVisitor implements IDepthFirstJavaElementVisitor {

  @Override
  public TreeVisitResult preVisit(ICompilationUnit icu) {
    return preVisitElement(icu, 0, 0);
  }

  @Override
  public TreeVisitResult preVisit(IType type, int level, int index) {
    return preVisitElement(type, level, index);
  }

  @Override
  public TreeVisitResult preVisit(IField field, int level, int index) {
    return preVisitElement(field, level, index);
  }

  @Override
  public TreeVisitResult preVisit(IMethod method, int level, int index) {
    return preVisitElement(method, level, index);
  }

  @Override
  public TreeVisitResult preVisit(IMethodParameter methodParameter, int level, int index) {
    return preVisitElement(methodParameter, level, index);
  }

  @Override
  public TreeVisitResult preVisit(IAnnotation annotation, int level, int index) {
    return preVisitElement(annotation, level, index);
  }

  @Override
  public TreeVisitResult preVisit(IAnnotationElement annotationElement, int level, int index) {
    return preVisitElement(annotationElement, level, index);
  }

  @Override
  public TreeVisitResult preVisit(IImport imp, int level, int index) {
    return preVisitElement(imp, level, index);
  }

  @Override
  public TreeVisitResult preVisit(IPackage pck, int level, int index) {
    return preVisitElement(pck, level, index);
  }

  @Override
  public TreeVisitResult preVisit(ITypeParameter typeParameter, int level, int index) {
    return preVisitElement(typeParameter, level, index);
  }

  @Override
  public TreeVisitResult preVisit(IUnresolvedType unresolvedType, int level, int index) {
    return preVisitElement(unresolvedType, level, index);
  }

  /**
   * Callback to implement an untyped pre-order traversal (top-down).
   *
   * @param element
   *          The visited element. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          Their children {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return a {@link TreeVisitResult} indicating how to continue visiting. Must not be {@code null}. <br>
   *         If {@link TreeVisitResult#TERMINATE} is returned, the {@link #postVisitElement(IJavaElement, int, int)}
   *         will not be called for the current element. The visit aborts immediately.
   * @see TreeVisitResult#CONTINUE
   * @see TreeVisitResult#SKIP_SIBLINGS
   * @see TreeVisitResult#SKIP_SUBTREE
   * @see TreeVisitResult#TERMINATE
   */
  protected TreeVisitResult preVisitElement(IJavaElement element, int level, int index) {
    return TreeVisitResult.CONTINUE;
  }

  @Override
  public boolean postVisit(ICompilationUnit icu) {
    return postVisitElement(icu, 0, 0);
  }

  @Override
  public boolean postVisit(IType type, int level, int index) {
    return postVisitElement(type, level, index);
  }

  @Override
  public boolean postVisit(IField field, int level, int index) {
    return postVisitElement(field, level, index);
  }

  @Override
  public boolean postVisit(IMethod method, int level, int index) {
    return postVisitElement(method, level, index);
  }

  @Override
  public boolean postVisit(IMethodParameter param, int level, int index) {
    return postVisitElement(param, level, index);
  }

  @Override
  public boolean postVisit(IAnnotation annotation, int level, int index) {
    return postVisitElement(annotation, level, index);
  }

  @Override
  public boolean postVisit(IAnnotationElement annotationElement, int level, int index) {
    return postVisitElement(annotationElement, level, index);
  }

  @Override
  public boolean postVisit(IImport imp, int level, int index) {
    return postVisitElement(imp, level, index);
  }

  @Override
  public boolean postVisit(IPackage pck, int level, int index) {
    return postVisitElement(pck, level, index);
  }

  @Override
  public boolean postVisit(ITypeParameter typeParameter, int level, int index) {
    return postVisitElement(typeParameter, level, index);
  }

  @Override
  public boolean postVisit(IUnresolvedType unresolvedType, int level, int index) {
    return postVisitElement(unresolvedType, level, index);
  }

  /**
   * Callback to implement a post-order traversal (bottom-up).
   * <p>
   * If the previous {@link #preVisitElement(IJavaElement, int, int)} call returned {@link TreeVisitResult#TERMINATE},
   * this post visit call will not be executed.
   *
   * @param element
   *          The visited element. Is never {@code null}.
   * @param level
   *          The level depth of the traversal. The root element on which the visit was started has the level {@code 0}.
   *          The children of the root {@code 1} and so on.
   * @param index
   *          The index of the current element in the list of children of the parent. The root element on which the
   *          visit was started has always the index {@code 0} even if it is not the first in the list of its parent!
   * @return {@code true} if the visit should continue (same as {@link TreeVisitResult#CONTINUE}. {@code false} if the
   *         visit should be canceled (same as {@link TreeVisitResult#TERMINATE}.
   */
  protected boolean postVisitElement(IJavaElement element, int level, int index) {
    return true;
  }
}
