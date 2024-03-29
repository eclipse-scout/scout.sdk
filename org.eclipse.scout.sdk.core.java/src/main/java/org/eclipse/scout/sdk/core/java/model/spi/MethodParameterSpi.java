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

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link MethodParameterSpi}</h3> Represents a parameter of an {@link MethodSpi}.
 *
 * @since 5.1.0
 */
public interface MethodParameterSpi extends AnnotatableSpi {

  /**
   * Gets the data type of this parameter.
   *
   * @return The {@link TypeSpi} that represents the data type of this parameter.
   */
  TypeSpi getDataType();

  /**
   * Gets the flags of this method parameter (e.g. final). Use the class {@link Flags} to decode the resulting
   * {@link Integer}.<br>
   * <b>Note: </b>If the surrounding {@link TypeSpi} is a binary type, the underlying class file may not contain all
   * flags as defined in the original source code because of reorganizations and optimizations of the compiler.
   *
   * @return An {@link Integer} holding all the flags of this method.
   * @see Flags
   */
  int getFlags();

  int getIndex();

  /**
   * Gets the {@link MethodSpi} this parameter belongs to
   *
   * @return The {@link MethodSpi} this parameter belongs to. Never returns {@code null}.
   */
  MethodSpi getDeclaringMethod();

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
  IMethodParameter wrap();
}
