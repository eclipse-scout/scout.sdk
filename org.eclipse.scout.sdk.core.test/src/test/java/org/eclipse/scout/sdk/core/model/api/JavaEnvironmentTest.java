/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link JavaEnvironmentTest}</h3>
 *
 * @since 7.0.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class JavaEnvironmentTest {

  @Test
  public void testReloadOfIcuMultipleTimes(IJavaEnvironment env) {
    var packageName = "org.test";
    var fileName = "TestClass.java";

    var firstSrc = """
        package org.test;

        public class TestClass {
        }
        """;

    var secondSrc = """
        package org.test;

        public class TestClass {
        int a = 0;}
        """;

    var reload = env.registerCompilationUnitOverride(firstSrc, packageName, fileName);
    assertFalse(reload);
    var testClass = env.requireType(packageName + ".TestClass");
    assertFalse(testClass.fields().existsAny());

    reload = env.registerCompilationUnitOverride(firstSrc, packageName, fileName);
    assertFalse(reload);

    reload = env.registerCompilationUnitOverride(secondSrc, packageName, fileName);
    assertTrue(reload);
    assertFalse(testClass.fields().existsAny());
    env.reload();
    assertTrue(testClass.fields().existsAny());

    reload = env.registerCompilationUnitOverride(secondSrc, packageName, fileName);
    assertFalse(reload);
  }

  @Test
  public void testReloadWithParameterizedTypeReferences(IJavaEnvironment env) {
    var javaUtilSet = env.requireType(Set.class.getName());
    var childClass = env.requireType(ChildClass.class.getName());
    var javaUtilSetWithArgs = childClass.methods().withName("firstCase").first().orElseThrow().requireReturnType().leafComponentType().orElseThrow();
    assertNotSame(javaUtilSet, javaUtilSetWithArgs);
    assertNotSame(javaUtilSet.unwrap(), javaUtilSetWithArgs.unwrap());
    assertEquals(Set.class.getName(), javaUtilSet.name());
    assertEquals(Set.class.getName(), javaUtilSetWithArgs.name());

    env.reload();
    assertNotSame(javaUtilSet, javaUtilSetWithArgs);
    assertNotSame(javaUtilSet.unwrap(), javaUtilSetWithArgs.unwrap());
    assertEquals(Set.class.getName(), javaUtilSet.name());
    assertEquals(Set.class.getName(), javaUtilSetWithArgs.name());
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
    var packageName = "org.eclipse.scout.test";
    var className = "ReloadTest";

    var firstSrc = "package " + packageName + ";\n\n"
        + "public class " + className + " {\n"
        + "}\n";

    var secondSrc = "package " + packageName + ";\n\n"
        + "public class " + className + " {\n"
        + "  long a = 0;"
        + "}\n";

    assertFalse(env.registerCompilationUnitOverride(firstSrc, packageName, className + JavaTypes.JAVA_FILE_SUFFIX));
    var longType = env.requireType(packageName + '.' + className);
    assertFalse(longType.fields().withName("a").existsAny());

    assertTrue(env.registerCompilationUnitOverride(secondSrc, packageName, className + JavaTypes.JAVA_FILE_SUFFIX));
    env.reload();
    assertTrue(longType.fields().withName("a").existsAny());
  }
}
