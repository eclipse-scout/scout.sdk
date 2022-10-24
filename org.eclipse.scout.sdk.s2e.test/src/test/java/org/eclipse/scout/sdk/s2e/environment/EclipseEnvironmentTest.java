/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.environment;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.util.CharSequenceInputStream;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.model.JavaEnvironmentWithJdt;
import org.eclipse.scout.sdk.s2e.testing.MockFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link EclipseEnvironmentTest}</h3>
 *
 * @since 7.0.0
 */
@SuppressWarnings({"resource"})
public class EclipseEnvironmentTest {
  @Test
  public void testWriteResourceWithGeneratorSync() {
    runInEnvironment((env, fs) -> {
      var a = Paths.get("a");
      var content = "examplecontent";
      var content2 = "otherfileContent";

      var j = new AbstractJob("") {
        @Override
        protected void execute(IProgressMonitor monitor) {
          env.writeResource(b -> b.append(content), a, null);
          env.writeResource(content2, a, null);
          env.writeResource(content2, a, null);
        }
      };
      var rule = mock(ISchedulingRule.class);
      doAnswer(invocation -> mockingDetails(invocation.getArgument(0)).isMock()).when(rule).contains(any());
      doAnswer(invocation -> mockingDetails(invocation.getArgument(0)).isMock()).when(rule).isConflicting(any());
      j.setRule(rule);
      j.scheduleWithFuture()
          .awaitDoneThrowingOnErrorOrCancel();

      verify(env, atLeastOnce()).doRunSync(any(), any(), any());
      assertEquals(content2, fs.get(a).toString());
    });
  }

  @Test
  public void testWriteCompilationUnitAsync() {
    runInEnvironment((env, fs) -> {
      var je = env.findJavaEnvironment(null).orElseThrow();
      var className = "TestClass";
      var result = env.writeCompilationUnit(PrimaryTypeGenerator.create().withElementName(className), je.primarySourceFolder().orElseThrow());
      assertEquals(className, result.elementName());

      env.executeGenerator(PrimaryTypeGenerator.create()
          .withElementName(className)
          .withField(FieldGenerator.createSerialVersionUid()), je.primarySourceFolder().orElseThrow());

      result = env.writeCompilationUnitAsync(
          PrimaryTypeGenerator.create()
              .withElementName(className)
              .withField(FieldGenerator.createSerialVersionUid()),
          je.primarySourceFolder().orElseThrow(), null)
          .result();
      assertTrue(result.fields().first().isPresent());
    });
  }

  /**
   * Tests that the type is written and returned even if a reload is executed as part of the type generation.
   */
  @Test
  public void testCreateCompilationUnitWithReload() {
    runInEnvironment((env, fs) -> {
      var je = env.findJavaEnvironment(null).orElseThrow();
      var result = env.writeCompilationUnit(
          PrimaryTypeGenerator.create()
              .withElementName("Test")
              .withField(FieldGenerator.create()
                  .withDataType(Object.class.getName())
                  .withElementName("m_field")
                  .withValue(b -> {
                    b.context().environment().orElseThrow().reload();
                    b.nullLiteral();
                  })),
          je.primarySourceFolder().orElseThrow());
      assertNotNull(result);
    });
  }

  @Test
  public void testWriteResourceAsync() {
    runInEnvironment((env, fs) -> {
      var a = Paths.get("a");
      var content = "examplecontent";
      env.writeResourceAsync(content, a, null).awaitDoneThrowingOnErrorOrCancel();
      assertEquals(content, fs.get(a).toString());
    });
  }

  @Test
  public void testEnvironment() {
    try (var jdtEnv = mock(EclipseEnvironment.class)) {
      assertNotNull(EclipseEnvironment.narrow(jdtEnv));
    }

    var env = mock(IEnvironment.class);
    assertFalse(env instanceof EclipseEnvironment);
    assertThrows(IllegalArgumentException.class, () -> EclipseEnvironment.narrow(env));
  }

  @Test
  public void testToScoutProgress() {
    IProgress ep = mock(EclipseProgress.class);
    assertNotNull(EclipseEnvironment.toScoutProgress(ep));

    assertNotNull(EclipseEnvironment.toScoutProgress((IProgress) null));
    assertNotNull(EclipseEnvironment.toScoutProgress((IProgressMonitor) null));

    var p = mock(IProgress.class);
    assertFalse(p instanceof EclipseProgress);
    assertThrows(IllegalArgumentException.class, () -> EclipseEnvironment.toScoutProgress(p));
  }

  @Test
  public void testJavaEnvironmentWithJdt() throws JavaModelException {
    try (var e = new EclipseEnvironment(); var adapter = spy(e)) {
      doAnswer(invocation -> new JavaEnvironmentWithJdt(invocation.getArgument(0), null)).when(adapter).createNewJavaEnvironmentFor(any());
      var javaProject = MockFactory.createJavaProjectMock();
      //noinspection NestedTryStatement
      try (var env = (JavaEnvironmentWithJdt) adapter.toScoutJavaEnvironment(javaProject).unwrap()) {
        assertNotNull(env);
        assertSame(javaProject, env.javaProject());
        assertNotNull(adapter.toScoutType(MockFactory.createJdtTypeMock(Long.class.getName(), javaProject)));
        assertNull(adapter.toScoutType(null));
        assertNull(adapter.toScoutType(MockFactory.createJdtTypeMock("not.existing", javaProject)));
        assertNotNull(adapter.toScoutSourceFolder(javaProject.getAllPackageFragmentRoots()[0]));
        assertNull(adapter.toScoutSourceFolder(null));
        var notExistingPackageRoot = mock(IPackageFragmentRoot.class);
        when(notExistingPackageRoot.getJavaProject()).thenReturn(javaProject);
        assertNull(adapter.toScoutSourceFolder(notExistingPackageRoot));
      }
    }
  }

  @Test
  public void testWriteResource() {
    runInEnvironment((env, fs) -> {
      var a = Paths.get("a");
      var content = "examplecontent";
      env.writeResource(content, a, null);
      assertEquals(content, fs.get(a).toString());
    });
  }

  protected static void runInEnvironment(BiConsumer<EclipseEnvironment, Map<Path, StringBuilder>> task) {
    runInEclipseEnvironment((e, progress) -> {
      Map<Path, StringBuilder> memoryFileSystem = new HashMap<>();
      try (var env = spy(e)) {
        var javaProject = MockFactory.createJavaProjectMock();
        doAnswer(invocation -> Optional.of(javaProject)).when(env).findJavaProject(any());

        doAnswer(invocation -> {
          Path pathOfFile = invocation.getArgument(0);

          var folder = mock(IContainer.class);
          when(folder.exists()).thenReturn(true);
          when(folder.contains(folder)).thenReturn(true);
          when(folder.isConflicting(folder)).thenReturn(true);

          var fileContent = memoryFileSystem.computeIfAbsent(pathOfFile, k -> new StringBuilder());
          var file = mock(IFile.class);
          try {
            when(file.exists()).thenAnswer(i -> memoryFileSystem.get(pathOfFile).length() > 0);
            when(file.getContents()).thenAnswer(i -> new CharSequenceInputStream(memoryFileSystem.get(pathOfFile), StandardCharsets.UTF_8));
            when(file.getType()).thenReturn(IResource.FILE);
            when(file.getParent()).thenReturn(folder);
            when(file.getCharset()).thenReturn(StandardCharsets.UTF_8.name());
            doAnswer(i2 -> {
              fileContent.delete(0, fileContent.length());
              var newContent = Strings.fromInputStream(i2.getArgument(0), StandardCharsets.UTF_8);
              fileContent.append(newContent);
              return null;
            }).when(file).create(any(), anyBoolean(), any());
            doAnswer(i2 -> {
              file.create(i2.getArgument(0), i2.<Boolean> getArgument(1), i2.getArgument(3));
              return null;
            }).when(file).setContents(any(InputStream.class), anyBoolean(), anyBoolean(), any());
          }
          catch (CoreException ex) {
            throw new SdkException(ex);
          }
          return file;
        }).when(env).pathToWorkspaceFile(any());

        doAnswer(invocation -> new JavaEnvironmentWithJdt(invocation.getArgument(0), null)).when(env).createNewJavaEnvironmentFor(any());

        task.accept(env, memoryFileSystem);
      }
    }).awaitDoneThrowingOnErrorOrCancel();
  }
}
