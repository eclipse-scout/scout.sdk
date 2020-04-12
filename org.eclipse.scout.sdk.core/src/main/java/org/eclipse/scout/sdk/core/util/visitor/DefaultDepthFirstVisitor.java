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
package org.eclipse.scout.sdk.core.util.visitor;

/**
 * Empty adapter implementation for {@link IDepthFirstVisitor}.
 *
 * @since 7.1
 * @see TreeVisitResult
 * @see IDepthFirstVisitor
 * @see TreeTraversals
 */
public class DefaultDepthFirstVisitor<T> implements IDepthFirstVisitor<T> {

  @Override
  public TreeVisitResult preVisit(T element, int level, int index) {
    return TreeVisitResult.CONTINUE;
  }

  @Override
  public boolean postVisit(T element, int level, int index) {
    return true;
  }
}
