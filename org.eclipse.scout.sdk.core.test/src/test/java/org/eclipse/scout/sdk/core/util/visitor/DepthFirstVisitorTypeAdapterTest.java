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
 * <h3>{@link DepthFirstVisitorTypeAdapterTest}</h3>
 *
 * @since 8.0.0
 */
public class DepthFirstVisitorTypeAdapterTest {

  private final Object m_root = new Object();
  private final String m_child = "child";

  @Test
  public void testToString() {
    assertEquals(DepthFirstVisitorTypeAdapter.class.getSimpleName() + " for type '" + String.class.getName() + "'.",
        new DepthFirstVisitorTypeAdapter<>(new DefaultDepthFirstVisitor<>(), String.class).toString());
  }

  @Test
  public void testFunctionToVisitor() {
    var aggregator = new StringBuilder();
    IDepthFirstVisitor<Object> visitor = new DepthFirstVisitorTypeAdapter<>(s -> {
      aggregator.append(s);
      return TreeVisitResult.CONTINUE;
    }, String.class);

    TreeTraversals.create(visitor, this::children).traverse(m_root);
    assertEquals(m_child, aggregator.toString());
  }

  private Stream<Object> children(Object parent) {
    if (parent == m_root) {
      return Stream.of(null, m_child, null);
    }
    return null;
  }
}
