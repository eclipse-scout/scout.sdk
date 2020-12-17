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

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.eclipse.scout.sdk.core.model.ecj.ClasspathEntry;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link ClasspathEntryTest}</h3>
 *
 * @since 7.0.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class ClasspathEntryTest {
  @Test
  public void testApiAndSpi(IJavaEnvironment env) {
    var path = env.classpath().collect(toList());
    var first = path.get(0);
    var second = path.get(1);
    var last = path.get(path.size() - 1);
    assertEquals(StandardCharsets.UTF_8.name(), first.encoding());
    assertTrue(first.isDirectory());
    assertTrue(first.isSourceFolder());
    assertEquals(ClasspathContentKind.SOURCE, first.kind());
    assertEquals(ClasspathContentKind.BINARY, last.kind());
    assertSame(env, first.javaEnvironment());

    assertTrue(first.toString().replace('\\', '/').endsWith("/src/test/fixture]"));
    assertNotEquals(second, first);
    assertNotEquals(second.hashCode(), first.hashCode());

    assertTrue(first.unwrap().toString().replace('\\', '/').endsWith("/src/test/fixture, mode=SOURCE]"));
    assertNotEquals(second.unwrap().hashCode(), first.unwrap().hashCode());
    new CoreJavaEnvironmentWithSourceFactory().accept(newEnv -> assertNotEquals(newEnv.classpath().findAny().get(), first));
    assertEquals(first, first);
    assertEquals(first.unwrap(), first.unwrap());
    assertNotEquals(second.unwrap(), first.unwrap());

    assertEquals(2, env.sourceFolders().count());
    assertTrue(env.primarySourceFolder().isPresent());
  }

  @Test
  @SuppressWarnings({"unlikely-arg-type", "SimplifiableJUnitAssertion", "ConstantConditions", "EqualsBetweenInconvertibleTypes", "EqualsWithItself"})
  public void testEntry() {
    var a = new ClasspathEntry(Paths.get("a", "a.jar"), ClasspathSpi.MODE_BINARY, StandardCharsets.UTF_8.name());
    var b = new ClasspathEntry(Paths.get("b", "b.jar"), ClasspathSpi.MODE_BINARY, StandardCharsets.UTF_8.name());
    var aa = new ClasspathEntry(Paths.get("a", "a.jar"), ClasspathSpi.MODE_BINARY, StandardCharsets.UTF_8.name());
    var bb = new ClasspathEntry(Paths.get("b", "b.jar"), ClasspathSpi.MODE_SOURCE, StandardCharsets.UTF_8.name());
    var cc = new ClasspathEntry(Paths.get("b", "b.jar"), ClasspathSpi.MODE_SOURCE | ClasspathSpi.MODE_BINARY, StandardCharsets.UTF_8.name());
    assertNotEquals(a, b);
    assertNotEquals(bb, b);
    assertEquals(a, aa);
    assertEquals("ClasspathEntry [path=b/b.jar, mode=binary, encoding=UTF-8]", b.toString().replace('\\', '/'));
    assertEquals("ClasspathEntry [path=b/b.jar, mode=source, encoding=UTF-8]", bb.toString().replace('\\', '/'));
    assertEquals("ClasspathEntry [path=b/b.jar, mode=source&binary, encoding=UTF-8]", cc.toString().replace('\\', '/'));
    assertNotEquals(a.hashCode(), b.hashCode());
    assertNotEquals(bb.hashCode(), b.hashCode());
    assertEquals(a.hashCode(), aa.hashCode());
    assertFalse(a.equals(null));
    assertFalse(a.equals(""));
    assertTrue(a.equals(a));
  }
}
