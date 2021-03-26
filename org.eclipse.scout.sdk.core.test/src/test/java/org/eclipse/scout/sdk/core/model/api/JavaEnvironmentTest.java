/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
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
    var javaUtilSetWithArgs = childClass.methods().withName("firstCase").first().get().requireReturnType().leafComponentType().get();
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
