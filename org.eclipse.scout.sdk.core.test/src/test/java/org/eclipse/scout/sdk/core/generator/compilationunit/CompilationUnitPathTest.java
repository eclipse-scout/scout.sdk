package org.eclipse.scout.sdk.core.generator.compilationunit;

import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CompilationUnitPathTest {
  @Test
  public void testNormal() {
    CompilationUnitPath a = new CompilationUnitPath("test.pck", "Test", Paths.get("dev", "src", "main", "java"));
    assertEquals("Test.java", a.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java", "test", "pck"), a.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "test", "pck", "Test.java"), a.targetFile());
  }

  @Test
  public void testDefaultPackage() {
    CompilationUnitPath a = new CompilationUnitPath(null, "Test", Paths.get("dev", "src", "main", "java"));
    assertEquals("Test.java", a.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java"), a.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "Test.java"), a.targetFile());
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsBetweenInconvertibleTypes", "ConstantConditions", "EqualsWithItself"})
  public void testEqualsAndHashCode() {
    CompilationUnitPath a = new CompilationUnitPath(null, "Test", Paths.get("dev"));
    CompilationUnitPath b = new CompilationUnitPath(null, "Test", Paths.get("dev"));
    CompilationUnitPath c = new CompilationUnitPath("pck", "Test", Paths.get("dev"));

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
    IClasspathEntry sourceFolder = mock(IClasspathEntry.class);
    when(sourceFolder.path()).thenReturn(Paths.get("dev", "src", "main", "java"));

    CompilationUnitPath a = new CompilationUnitPath(generator, sourceFolder);
    assertEquals("Test.java", a.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java", "a", "b", "c"), a.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "a", "b", "c", "Test.java"), a.targetFile());
  }

  @Test
  public void testWithGeneratorAndDefaultPackage() {
    ICompilationUnitGenerator<?> generator = mock(ICompilationUnitGenerator.class);
    when(generator.packageName()).thenReturn(Optional.empty());
    when(generator.elementName()).thenReturn(Optional.of("Test"));
    IClasspathEntry sourceFolder = mock(IClasspathEntry.class);
    when(sourceFolder.path()).thenReturn(Paths.get("dev", "src", "main", "java"));

    CompilationUnitPath a = new CompilationUnitPath(generator, sourceFolder);
    assertEquals("Test.java", a.fileName());
    assertEquals(Paths.get("dev", "src", "main", "java"), a.targetDirectory());
    assertEquals(Paths.get("dev", "src", "main", "java", "Test.java"), a.targetFile());
  }
}
