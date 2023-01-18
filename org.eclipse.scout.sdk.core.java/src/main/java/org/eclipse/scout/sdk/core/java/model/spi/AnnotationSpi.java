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

import java.util.Map;

import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link AnnotationSpi}</h3>
 *
 * @since 5.1.0
 */
public interface AnnotationSpi extends JavaElementSpi {

  /**
   * Gets all elements of this {@link AnnotationSpi}.<br>
   * The {@link Map} iterates over the elements in the order as they appear in the source or class file.
   *
   * @return A {@link Map} containing the element name ({@link AnnotationElementSpi#getElementName()}) as key and the
   *         {@link AnnotationElementSpi} as value. Never returns {@code null}.
   */
  Map<String, AnnotationElementSpi> getValues();

  /**
   * Gets the object on which this {@link AnnotationSpi} is defined.
   *
   * @return The owner {@link AnnotatableSpi} of this {@link AnnotationSpi}. Never returns {@code null}.
   */
  AnnotatableSpi getOwner();

  /**
   * Gets the annotation definition {@link TypeSpi}.<br>
   * Returns e.g. the type {@link Override}.
   *
   * @return The annotation definition type. Never returns {@code null}.
   */
  TypeSpi getType();

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
  IAnnotation wrap();
}
