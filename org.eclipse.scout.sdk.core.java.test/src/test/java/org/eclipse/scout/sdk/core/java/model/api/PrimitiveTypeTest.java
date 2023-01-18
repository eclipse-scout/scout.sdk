/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link PrimitiveTypeTest}</h3>
 * <p>
 *
 * @since 5.1.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class PrimitiveTypeTest {

  @Test
  public void testExistence(IJavaEnvironment env) {
    assertTrue(env.findType(JavaTypes._short).isPresent());
    assertTrue(env.findType(JavaTypes._int).isPresent());
    assertTrue(env.findType(JavaTypes._float).isPresent());
    assertTrue(env.findType(JavaTypes._long).isPresent());
    assertTrue(env.findType(JavaTypes._double).isPresent());
    assertTrue(env.findType(JavaTypes._boolean).isPresent());
    assertTrue(env.findType(JavaTypes._char).isPresent());
    assertTrue(env.findType(JavaTypes._byte).isPresent());
    assertTrue(env.findType(JavaTypes._void).isPresent());

    assertFalse(env.findType("null").isPresent());
  }
}
