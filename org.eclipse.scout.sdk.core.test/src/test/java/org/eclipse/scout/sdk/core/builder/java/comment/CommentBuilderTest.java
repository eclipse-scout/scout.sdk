/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.builder.java.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link CommentBuilderTest}</h3>
 *
 * @since 7.0.0
 */
public class CommentBuilderTest {
  @Test
  public void test() {
    assertCommentEquals("/**\n* a\n* b\n*/\n", "a\nb");
    assertCommentEquals("/**\n* a\n* b\n*/\n", "a\n* b");
    assertCommentEquals("/**\n* abc\n*/\n", "/**\nabc\n**/");
    assertCommentEquals("/**\n* abc\n*/\n", "/*\nabc\n*/");
  }

  protected static void assertCommentEquals(String expected, String input) {
    MemorySourceBuilder inner = new MemorySourceBuilder();
    new CommentBuilder<>(inner).appendBlockComment(input);
    assertEquals(expected, inner.source().toString());
  }
}
