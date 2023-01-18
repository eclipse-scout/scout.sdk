/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model;

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

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;
import org.junit.jupiter.api.Test;

public class CompilationUnitInfoWithClasspathTest {
  @Test
  public void testWithPackage() {
    var packageName = "test.pck";
    var classSimpleName = "Test";
    var sourceFolder = createMockClasspathEntry("dev", "src", "main", "java");
    var expectedFileName = classSimpleName + JavaTypes.JAVA_FILE_SUFFIX;
    var expectedAbsoluteDirectory = Paths.get("dev", "src", "main", "java", "test", "pck");
    var expectedToStringValue = "CompilationUnitInfo [dev/src/main/java/test/pck/Test.java]";

    var info1 = new CompilationUnitInfoWithClasspath(sourceFolder, packageName, classSimpleName + JavaTypes.JAVA_FILE_SUFFIX);
    assertEquals(expectedFileName, info1.fileName());
    assertEquals(expectedAbsoluteDirectory, info1.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "test", "pck", expectedFileName), info1.targetFile());
    assertEquals(Paths.get("dev/src/main/java"), info1.sourceFolder());
    assertSame(sourceFolder, info1.classpathEntry());
    assertEquals(classSimpleName, info1.mainTypeSimpleName());
    assertEquals("test.pck.Test", info1.mainTypeFullyQualifiedName());
    assertEquals(packageName, info1.packageName());
    assertEquals(expectedToStringValue, info1.toString());
    assertEquals("dev/src/main/java/test/pck/Test.java", info1.targetFileAsString());
    assertEquals("dev/src/main/java", info1.sourceDirectoryAsString());
    assertEquals("dev/src/main/java/test/pck", info1.targetDirectoryAsString());

    var info2 = new CompilationUnitInfoWithClasspath(sourceFolder, Paths.get("test", "pck", expectedFileName));
    assertEquals(expectedFileName, info2.fileName());
    assertEquals(expectedAbsoluteDirectory, info2.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "test", "pck", expectedFileName), info2.targetFile());
    assertEquals(Paths.get("dev", "src", "main", "java"), info2.sourceFolder());
    assertSame(sourceFolder, info2.classpathEntry());
    assertEquals(classSimpleName, info2.mainTypeSimpleName());
    assertEquals(packageName + JavaTypes.C_DOT + classSimpleName, info2.mainTypeFullyQualifiedName());
    assertEquals(packageName, info2.packageName());
    assertEquals(expectedToStringValue, info2.toString());
  }

  @Test
  public void testWithoutSourceFolder() {
    var relPath = "org/eclipse/scout/sdk/test/Test.java";
    var sourceFolderRelPath = Paths.get(relPath);
    var info1 = new CompilationUnitInfo(null, sourceFolderRelPath);
    assertEquals("Test.java", info1.fileName());
    assertEquals(Paths.get("org/eclipse/scout/sdk/test"), info1.targetDirectory());
    assertEquals(Paths.get("org/eclipse/scout/sdk/test/Test.java"), info1.targetFile());
    assertEquals(Paths.get(""), info1.sourceFolder());
    assertEquals("Test", info1.mainTypeSimpleName());
    assertEquals("org.eclipse.scout.sdk.test.Test", info1.mainTypeFullyQualifiedName());
    assertEquals("org.eclipse.scout.sdk.test", info1.packageName());
    assertEquals(relPath, info1.targetFileAsString());
    assertEquals("", info1.sourceDirectoryAsString());
    assertEquals("org/eclipse/scout/sdk/test", info1.targetDirectoryAsString());

    var info2 = new CompilationUnitInfo(null, "org.eclipse.scout.sdk.test", "Test.java");
    assertEquals("Test.java", info2.fileName());
    assertEquals(Paths.get("org/eclipse/scout/sdk/test"), info2.targetDirectory());
    assertEquals(Paths.get("org/eclipse/scout/sdk/test/Test.java"), info2.targetFile());
    assertEquals(Paths.get(""), info2.sourceFolder());
    assertEquals("Test", info2.mainTypeSimpleName());
    assertEquals("org.eclipse.scout.sdk.test.Test", info2.mainTypeFullyQualifiedName());
    assertEquals("org.eclipse.scout.sdk.test", info2.packageName());
    assertEquals(relPath, info2.targetFileAsString());
    assertEquals("", info2.sourceDirectoryAsString());
    assertEquals("org/eclipse/scout/sdk/test", info2.targetDirectoryAsString());
  }

  @Test
  public void testDefaultPackage() {
    var sourceFolder = createMockClasspathEntry("dev", "src", "main", "java");
    var classSimpleName = "Test";
    var expectedFileName = classSimpleName + JavaTypes.JAVA_FILE_SUFFIX;
    var expectedToStringValue = "CompilationUnitInfo [dev/src/main/java/Test.java]";
    var expectedAbsoluteDirectory = Paths.get("dev", "src", "main", "java");

    var info1 = new CompilationUnitInfoWithClasspath(sourceFolder, null, classSimpleName + JavaTypes.JAVA_FILE_SUFFIX);
    assertEquals(expectedFileName, info1.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java"), info1.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", expectedFileName), info1.targetFile());
    assertEquals(Paths.get("dev", "src", "main", "java"), info1.sourceFolder());
    assertSame(sourceFolder, info1.classpathEntry());
    assertEquals(classSimpleName, info1.mainTypeSimpleName());
    assertEquals(classSimpleName, info1.mainTypeFullyQualifiedName());
    assertNull(info1.packageName());
    assertEquals(expectedToStringValue, info1.toString());

    var info2 = new CompilationUnitInfoWithClasspath(sourceFolder, Paths.get(expectedFileName));
    assertEquals(expectedFileName, info2.fileName());
    assertEquals(expectedAbsoluteDirectory, info2.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", expectedFileName), info2.targetFile());
    assertEquals(Paths.get("dev", "src", "main", "java"), info2.sourceFolder());
    assertSame(sourceFolder, info2.classpathEntry());
    assertEquals(classSimpleName, info2.mainTypeSimpleName());
    assertEquals(classSimpleName, info2.mainTypeFullyQualifiedName());
    assertNull(info2.packageName());
    assertEquals(expectedToStringValue, info2.toString());
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsBetweenInconvertibleTypes", "ConstantConditions", "EqualsWithItself"})
  public void testEqualsAndHashCode() {
    var sourceFolder = createMockClasspathEntry("dev");
    var fileName = "Test" + JavaTypes.JAVA_FILE_SUFFIX;
    var a = new CompilationUnitInfoWithClasspath(sourceFolder, null, fileName);
    var b = new CompilationUnitInfoWithClasspath(sourceFolder, null, fileName);
    var c = new CompilationUnitInfoWithClasspath(sourceFolder, "pck", fileName);

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
    var fileName = "Test" + JavaTypes.JAVA_FILE_SUFFIX;
    when(generator.fileName()).thenReturn(Optional.of(fileName));
    var sourceFolder = mock(IClasspathEntry.class);
    when(sourceFolder.path()).thenReturn(Paths.get("dev", "src", "main", "java"));

    var a = new CompilationUnitInfoWithClasspath(sourceFolder, generator);
    assertEquals("Test.java", a.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java", "a", "b", "c"), a.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "a", "b", "c", fileName), a.targetFile());
  }

  @Test
  public void testWithGeneratorAndDefaultPackage() {
    ICompilationUnitGenerator<?> generator = mock(ICompilationUnitGenerator.class);
    when(generator.packageName()).thenReturn(Optional.empty());
    var fileName = "Test" + JavaTypes.JAVA_FILE_SUFFIX;
    when(generator.fileName()).thenReturn(Optional.of(fileName));
    var sourceFolder = mock(IClasspathEntry.class);
    when(sourceFolder.path()).thenReturn(Paths.get("dev", "src", "main", "java"));

    var a = new CompilationUnitInfoWithClasspath(sourceFolder, generator);
    assertEquals("Test.java", a.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java"), a.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", fileName), a.targetFile());
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
