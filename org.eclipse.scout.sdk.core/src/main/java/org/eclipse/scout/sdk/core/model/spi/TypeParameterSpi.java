/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.spi;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link TypeParameterSpi}</h3> Represents a type parameter.
 *
 * @since 4.1.0 2014-11-09
 */
public interface TypeParameterSpi extends JavaElementSpi {

  /**
   * Gets all bounds of this {@link TypeParameterSpi}. The first bound will be the class parameter (if existing)
   * followed by all interface bounds in the order as it is defined in the source or class file.<br>
   * <br>
   * <b>Example: </b> {@code ChildClass<X extends AbstractList<String> & Runnable & Serializable>: .getBounds() =
   * {AbstractList<String>, Runnable, Serializable}}
   *
   * @return A {@link List} containing all bounds of this {@link TypeParameterSpi}.
   */
  List<TypeSpi> getBounds();

  /**
   * Gets the {@link MemberSpi} this {@link TypeParameterSpi} belongs to.
   *
   * @return The {@link MemberSpi} this {@link TypeParameterSpi} belongs to.
   */
  MemberSpi getDeclaringMember();

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
  ITypeParameter wrap();
}
