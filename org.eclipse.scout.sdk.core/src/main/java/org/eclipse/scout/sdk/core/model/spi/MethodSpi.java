/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
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
   * Gets if this method is a constructor.
   *
   * @return {@code true} if this method is a constructor, {@code false} otherwise.
   */
  boolean isConstructor();

  ISourceRange getSourceOfBody();

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
