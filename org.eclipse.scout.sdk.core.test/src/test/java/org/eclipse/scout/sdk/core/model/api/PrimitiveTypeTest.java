/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link PrimitiveTypeTest}</h3>
 * <p>
 *
 * @since 5.1.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
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
