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
package org.eclipse.scout.sdk.core.generator.compilationunit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;

public class CompilationUnitInfoTest {
  @Test
  public void testWithPackage() {
    var packageName = "test.pck";
    var classSimpleName = "Test";
    var sourceFolder = createMockClasspathEntry("dev", "src", "main", "java");
    var expectedFileName = classSimpleName + JavaTypes.JAVA_FILE_SUFFIX;
    var expectedAbsoluteDirectory = Paths.get("dev", "src", "main", "java", "test", "pck");
    var expectedToStringValue = "CompilationUnitInfo [dev/src/main/java/test/pck/Test.java]";

    var info1 = new CompilationUnitInfo(sourceFolder, packageName, classSimpleName);
    assertEquals(expectedFileName, info1.fileName());
    assertEquals(expectedAbsoluteDirectory, info1.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "test", "pck", expectedFileName), info1.targetFile());
    assertSame(sourceFolder, info1.sourceFolder());
    assertEquals(classSimpleName, info1.mainTypeSimpleName());
    assertEquals("test.pck.Test", info1.mainTypeFullyQualifiedName());
    assertEquals(packageName, info1.packageName());
    assertEquals(expectedToStringValue, info1.toString());

    var info2 = new CompilationUnitInfo(sourceFolder, Paths.get("test", "pck", expectedFileName));
    assertEquals(expectedFileName, info2.fileName());
    assertEquals(expectedAbsoluteDirectory, info2.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "test", "pck", expectedFileName), info2.targetFile());
    assertSame(sourceFolder, info2.sourceFolder());
    assertEquals(classSimpleName, info2.mainTypeSimpleName());
    assertEquals(packageName + JavaTypes.C_DOT + classSimpleName, info2.mainTypeFullyQualifiedName());
    assertEquals(packageName, info2.packageName());
    assertEquals(expectedToStringValue, info2.toString());
  }

  @Test
  public void testDefaultPackage() {
    var sourceFolder = createMockClasspathEntry("dev", "src", "main", "java");
    var classSimpleName = "Test";
    var expectedFileName = classSimpleName + JavaTypes.JAVA_FILE_SUFFIX;
    var expectedToStringValue = "CompilationUnitInfo [dev/src/main/java/Test.java]";
    var expectedAbsoluteDirectory = Paths.get("dev", "src", "main", "java");

    var info1 = new CompilationUnitInfo(sourceFolder, null, classSimpleName);
    assertEquals(expectedFileName, info1.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java"), info1.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", expectedFileName), info1.targetFile());
    assertSame(sourceFolder, info1.sourceFolder());
    assertEquals(classSimpleName, info1.mainTypeSimpleName());
    assertEquals(classSimpleName, info1.mainTypeFullyQualifiedName());
    assertNull(info1.packageName());
    assertEquals(expectedToStringValue, info1.toString());

    var info2 = new CompilationUnitInfo(sourceFolder, Paths.get(expectedFileName));
    assertEquals(expectedFileName, info2.fileName());
    assertEquals(expectedAbsoluteDirectory, info2.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", expectedFileName), info2.targetFile());
    assertSame(sourceFolder, info2.sourceFolder());
    assertEquals(classSimpleName, info2.mainTypeSimpleName());
    assertEquals(classSimpleName, info2.mainTypeFullyQualifiedName());
    assertNull(info2.packageName());
    assertEquals(expectedToStringValue, info2.toString());
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsBetweenInconvertibleTypes", "ConstantConditions", "EqualsWithItself"})
  public void testEqualsAndHashCode() {
    var sourceFolder = createMockClasspathEntry("dev");
    var a = new CompilationUnitInfo(sourceFolder, null, "Test");
    var b = new CompilationUnitInfo(sourceFolder, null, "Test");
    var c = new CompilationUnitInfo(sourceFolder, "pck", "Test");

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

    var a = new CompilationUnitInfo(generator, sourceFolder);
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

    var a = new CompilationUnitInfo(generator, sourceFolder);
    assertEquals("Test.java", a.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java"), a.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "Test.java"), a.targetFile());
  }

  protected static IClasspathEntry createMockClasspathEntry(String first, String... more) {
    return createMockClasspathEntry(Paths.get(first, more));
  }

  protected static IClasspathEntry createMockClasspathEntry(Path p) {
    var cpEntry = mock(IClasspathEntry.class);
    when(cpEntry.path()).thenReturn(p);
    return cpEntry;
  }
}
