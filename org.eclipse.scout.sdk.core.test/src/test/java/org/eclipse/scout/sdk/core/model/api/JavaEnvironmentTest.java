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
package org.eclipse.scout.sdk.core.model.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link JavaEnvironmentTest}</h3>
 *
 * @since 7.0.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class JavaEnvironmentTest {

  @Test
  public void testReloadOfIcuMultipleTimes(IJavaEnvironment env) {
    var packageName = "org.test";
    var fileName = "TestClass.java";

    var firstSrc = "package org.test;\n\n"
        + "public class TestClass {\n"
        + "}\n";

    var secondSrc = "package org.test;\n\n"
        + "public class TestClass {\n"
        + "int a = 0;"
        + "}\n";

    var reload = env.registerCompilationUnitOverride(packageName, fileName, firstSrc);
    assertFalse(reload);
    var testClass = env.requireType(packageName + ".TestClass");
    assertFalse(testClass.fields().existsAny());

    reload = env.registerCompilationUnitOverride(packageName, fileName, firstSrc);
    assertFalse(reload);

    reload = env.registerCompilationUnitOverride(packageName, fileName, secondSrc);
    assertTrue(reload);
    assertFalse(testClass.fields().existsAny());
    env.reload();
    assertTrue(testClass.fields().existsAny());

    reload = env.registerCompilationUnitOverride(packageName, fileName, secondSrc);
    assertFalse(reload);
  }

  @Test
  public void testReloadOfSourceType(IJavaEnvironment env) {
    testReloadOfType(env);
  }

  @Test
  public void testReloadOfBinaryType() {
    new CoreJavaEnvironmentBinaryOnlyFactory().accept(JavaEnvironmentTest::testReloadOfType);
  }

  protected static void testReloadOfType(IJavaEnvironment env) {
    var packageName = "java.lang";
    var fileName = "Long.java";

    var firstSrc = "package java.lang;\n\n"
        + "public class Long {\n"
        + "long a = 0;"
        + "}\n";

    var longType = env.requireType(Long.class.getName());
    assertFalse(longType.fields().withName("a").existsAny());

    assertTrue(env.registerCompilationUnitOverride(packageName, fileName, firstSrc));
    env.reload();
    assertTrue(longType.fields().withName("a").existsAny());
  }
}
