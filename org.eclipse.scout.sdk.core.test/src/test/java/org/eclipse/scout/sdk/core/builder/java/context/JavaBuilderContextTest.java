/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.builder.java.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.util.PropertySupport;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link JavaBuilderContextTest}</h3>
 *
 * @since 6.1.0
 */
public class JavaBuilderContextTest {
  @Test
  @SuppressWarnings({"unlikely-arg-type", "SimplifiableJUnitAssertion", "EqualsWithItself", "ConstantConditions", "EqualsBetweenInconvertibleTypes"})
  public void testValues() {
    assertNotNull(new JavaBuilderContext().properties());
    assertNotNull(new JavaBuilderContext().lineDelimiter());
    assertNotNull(new JavaBuilderContext().builderContext());
    assertNotNull(new JavaBuilderContext().validator());
    assertFalse(new JavaBuilderContext().environment().isPresent());

    var c = new JavaBuilderContext();
    assertTrue(c.equals(c));
    assertTrue(new JavaBuilderContext().equals(new JavaBuilderContext()));
    assertFalse(new JavaBuilderContext().equals(null));
    assertFalse(new JavaBuilderContext().equals(""));
    new CoreJavaEnvironmentWithSourceFactory().accept(env -> assertNotEquals(new JavaBuilderContext(), new JavaBuilderContext(env)));

    assertEquals(new JavaBuilderContext().hashCode(), new JavaBuilderContext().hashCode());
    new CoreJavaEnvironmentWithSourceFactory().accept(env -> assertNotEquals(new JavaBuilderContext().hashCode(), new JavaBuilderContext(env).hashCode()));

    var c2 = new JavaBuilderContext(new BuilderContext("a"));
    assertNotEquals(c2, c);

    var a = new BuilderContext();
    var b = new BuilderContext("c");
    var d = new BuilderContext("nl", new PropertySupport());
    var e = new BuilderContext("nl", new PropertySupport());
    assertFalse(a.equals(b));
    assertTrue(a.equals(a));
    assertFalse(a.equals(null));
    assertFalse(a.equals(""));
    assertTrue(e.equals(d));
  }
}
