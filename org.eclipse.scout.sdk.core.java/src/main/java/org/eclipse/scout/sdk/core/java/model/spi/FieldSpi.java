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

import org.eclipse.scout.sdk.core.java.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IField;
import org.eclipse.scout.sdk.core.java.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link FieldSpi}</h3> Represents a field in a java type.
 *
 * @since 5.1.0
 */
public interface FieldSpi extends MemberSpi {

  /**
   * Gets the constant value of this {@link FieldSpi}.<br>
   * Please note: The field must be initialized with a constant value so that it can be retrieved using this method.
   *
   * @return The constant value of this {@link FieldSpi} if it can be computed or {@code null} otherwise.
   */
  IMetaValue getConstantValue();

  /**
   * Gets the data type of this {@link FieldSpi}.
   *
   * @return The {@link TypeSpi} describing the data type of this {@link FieldSpi}. Never returns {@code null}.
   */
  TypeSpi getDataType();

  SourceRange getSourceOfInitializer();

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
  IField wrap();
}
