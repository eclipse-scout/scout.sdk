/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
    var result = TreeTraversals.create(visitor, this::children).traverse(m_root);
    assertEquals(TreeVisitResult.TERMINATE, result);
  }

  private Stream<Object> children(Object parent) {
    if (parent == m_root) {
      return Stream.of(null, m_child, null);
    }
    return null;
  }
}
