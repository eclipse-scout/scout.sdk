/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.spi;

import org.eclipse.scout.sdk.core.model.api.IBreadthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.model.api.IDepthFirstJavaElementVisitor;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * <h3>{@link JavaElementSpi}</h3>Represents a Java element.
 *
 * @since 5.1.0
 */
public interface JavaElementSpi {

  JavaEnvironmentSpi getJavaEnvironment();

  /**
   * @return the element name
   *         <p>
   *         this is the relative or simple name of the element inside its containing scope
   */
  String getElementName();

  ISourceRange getSource();

  IJavaElement wrap();

  /**
   * Pre-order visitor call. Must not delegate to any children.
   */
  TreeVisitResult acceptPreOrder(IDepthFirstJavaElementVisitor visitor, int level, int index);

  /**
   * Post-order visitor call. Must not delegate to any children.
   */
  boolean acceptPostOrder(IDepthFirstJavaElementVisitor visitor, int level, int index);

  /**
   * Level-order visitor call. Must not delegate to any children.
   */
  TreeVisitResult acceptLevelOrder(IBreadthFirstJavaElementVisitor visitor, int level, int index);
}
