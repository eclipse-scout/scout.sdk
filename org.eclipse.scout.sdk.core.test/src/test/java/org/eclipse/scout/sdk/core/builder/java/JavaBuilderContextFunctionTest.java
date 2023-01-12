/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.builder.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class JavaBuilderContextFunctionTest {

  @Test
  public void testApply() {
    Function<IJavaBuilderContext, Integer> l = c -> 1;
    var func = JavaBuilderContextFunction.create(l);
    assertFalse(func.apply().isPresent()); // cannot execute without context
    assertEquals(1, func.apply(null));
    assertTrue(func.isContextRequired());
    assertFalse(JavaBuilderContextFunction.create(1).isContextRequired());
    assertFalse(JavaBuilderContextFunction.create(JavaBuilderContextFunction.create(1)).isContextRequired());
    assertSame(l, func.contextFunction());
  }

  @Test
  public void testEqualsHashCode() {
    assertNotEquals(JavaBuilderContextFunction.create(1), JavaBuilderContextFunction.create(1));
    assertNotEquals(JavaBuilderContextFunction.create(c -> 1), JavaBuilderContextFunction.create(c -> 1));
    assertNotEquals(JavaBuilderContextFunction.create(c -> 1), "");
    assertNotEquals(JavaBuilderContextFunction.create(c -> 1).hashCode(), JavaBuilderContextFunction.create(c -> 1).hashCode());
    var func = JavaBuilderContextFunction.create(1);
    assertEquals(func, func);
  }

  @Test
  public void testOrNull() {
    assertNull(JavaBuilderContextFunction.orNull((Integer) null));
    assertEquals(1, JavaBuilderContextFunction.orNull(1).apply().orElseThrow());

    assertNull(JavaBuilderContextFunction.orNull(null));
    assertEquals(1, JavaBuilderContextFunction.orNull(c -> 1).apply(null));
    assertEquals(2, JavaBuilderContextFunction.orNull(new JavaBuilderContextFunction<>(2)).apply().orElseThrow());

    var func = JavaBuilderContextFunction.create(1);
    assertSame(func, JavaBuilderContextFunction.create(func));
    assertSame(func, JavaBuilderContextFunction.orNull(func));
  }
}
