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

import org.eclipse.scout.sdk.core.java.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.java.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.java.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.util.SourceRange;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link AnnotationElementSpi}</h3>
 *
 * @since 5.1.0
 */
public interface AnnotationElementSpi extends JavaElementSpi {

  /**
   * Gets the value of the annotation attribute.
   *
   * @return never null
   */
  IMetaValue getMetaValue();

  /**
   * Gets the {@link AnnotationSpi} this {@link AnnotationElementSpi} belongs to.
   *
   * @return The declaring {@link AnnotationSpi}
   */
  AnnotationSpi getDeclaringAnnotation();

  /**
   * @return true if this {@link AnnotationElementSpi} was not declared in source code but is the default value of the
   *         annotation
   */
  boolean isDefaultValue();

  SourceRange getSourceOfExpression();

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
  IAnnotationElement wrap();
}
