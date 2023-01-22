/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.spi;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link MethodSpi}</h3> Represents a method declaration.
 *
 * @since 5.1.0
 */
public interface MethodSpi extends MemberSpi {

  /**
   * @return Gets return data {@link TypeSpi} of this {@link MethodSpi}. This can be the void type.<br>
   *         For constructors {@code null} is returned.
   * @see #isConstructor()
   */
  TypeSpi getReturnType();

  /**
   * Gets all {@link MethodParameterSpi}s in the order as they appear in the method.
   *
   * @return A {@link List} with all {@link MethodParameterSpi}s of this {@link MethodSpi}.
   */
  List<MethodParameterSpi> getParameters();

  /**
   * Gets all exception declarations of this {@link MethodSpi} in the order as they appear in the source or class file.
   *
   * @return a {@link List} containing all thrown {@link TypeSpi}s of this {@link MethodSpi}.
   */
  List<TypeSpi> getExceptionTypes();

  /**
   * @return The method identifier without type arguments (type erasure only)
   * @see JavaTypes#createMethodIdentifier(CharSequence, Collection)
   */
  String getMethodId();

  /**
   * Gets if this method is a constructor.
   *
   * @return {@code true} if this method is a constructor, {@code false} otherwise.
   */
  boolean isConstructor();

  SourceRange getSourceOfBody();

  SourceRange getSourceOfDeclaration();

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
  IMethod wrap();
}
