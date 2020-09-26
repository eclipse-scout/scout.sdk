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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link DepthFirstTraversalTest}</h3>
 *
 * @since 8.0.0
 */
public class DepthFirstTraversalTest {

  private final Object m_root = new Object();
  private final Object m_child = new Object();

  @Test
  public void testWithNullChildren() {
    IDepthFirstVisitor<Object> visitor = new DefaultDepthFirstVisitor<>() {
      @Override
      public boolean postVisit(Object element, int level, int index) {
        return element == m_root;
      }
    };
    TreeVisitResult result = TreeTraversals.create(visitor, this::children).traverse(m_root);
    assertEquals(TreeVisitResult.TERMINATE, result);
  }

  private Stream<Object> children(Object parent) {
    if (parent == m_root) {
      return Stream.of(null, m_child, null);
    }
    return null;
  }
}
