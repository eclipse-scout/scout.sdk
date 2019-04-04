/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
    StringBuilder aggregator = new StringBuilder();
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
