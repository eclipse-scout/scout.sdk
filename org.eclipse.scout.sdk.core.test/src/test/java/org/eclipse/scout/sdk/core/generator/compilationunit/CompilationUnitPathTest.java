/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.generator.compilationunit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.junit.jupiter.api.Test;

public class CompilationUnitPathTest {
  @Test
  public void testNormal() {
    var a = new CompilationUnitPath("test.pck", "Test", Paths.get("dev", "src", "main", "java"));
    assertEquals("Test.java", a.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java", "test", "pck"), a.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "test", "pck", "Test.java"), a.targetFile());
  }

  @Test
  public void testDefaultPackage() {
    var a = new CompilationUnitPath(null, "Test", Paths.get("dev", "src", "main", "java"));
    assertEquals("Test.java", a.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java"), a.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "Test.java"), a.targetFile());
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsBetweenInconvertibleTypes", "ConstantConditions", "EqualsWithItself"})
  public void testEqualsAndHashCode() {
    var a = new CompilationUnitPath(null, "Test", Paths.get("dev"));
    var b = new CompilationUnitPath(null, "Test", Paths.get("dev"));
    var c = new CompilationUnitPath("pck", "Test", Paths.get("dev"));

    assertEquals(a, b);
    assertNotEquals(a, c);
    assertFalse(a.equals(""));
    assertFalse(a.equals(null));
    assertTrue(a.equals(a));
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a.hashCode(), c.hashCode());
  }

  @Test
  public void testWithGenerator() {
    ICompilationUnitGenerator<?> generator = mock(ICompilationUnitGenerator.class);
    when(generator.packageName()).thenReturn(Optional.of("a.b.c"));
    when(generator.elementName()).thenReturn(Optional.of("Test"));
    var sourceFolder = mock(IClasspathEntry.class);
    when(sourceFolder.path()).thenReturn(Paths.get("dev", "src", "main", "java"));

    var a = new CompilationUnitPath(generator, sourceFolder);
    assertEquals("Test.java", a.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java", "a", "b", "c"), a.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "a", "b", "c", "Test.java"), a.targetFile());
  }

  @Test
  public void testWithGeneratorAndDefaultPackage() {
    ICompilationUnitGenerator<?> generator = mock(ICompilationUnitGenerator.class);
    when(generator.packageName()).thenReturn(Optional.empty());
    when(generator.elementName()).thenReturn(Optional.of("Test"));
    var sourceFolder = mock(IClasspathEntry.class);
    when(sourceFolder.path()).thenReturn(Paths.get("dev", "src", "main", "java"));

    var a = new CompilationUnitPath(generator, sourceFolder);
    assertEquals("Test.java", a.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java"), a.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "Test.java"), a.targetFile());
  }
}
