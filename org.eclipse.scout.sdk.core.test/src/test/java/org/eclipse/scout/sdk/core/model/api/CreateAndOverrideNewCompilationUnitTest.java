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
package org.eclipse.scout.sdk.core.model.api;

import static org.eclipse.scout.sdk.core.testing.CoreTestingUtils.registerCompilationUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.CompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link CreateAndOverrideNewCompilationUnitTest}</h3>
 *
 * @since 5.1.0
 */
public class CreateAndOverrideNewCompilationUnitTest {

  @Test
  @ExtendWithJavaEnvironmentFactory(EmptyJavaEnvironmentFactory.class)
  public void testCreateNewTypeWithErrors(IJavaEnvironment env) {
    // add an unresolved type error
    var cuSrc = createBaseClass();
    cuSrc.mainType().orElseThrow().methods().findAny().orElseThrow().withReturnType("FooBar");
    var cu = registerCompilationUnit(env, cuSrc).requireCompilationUnit();

    var expected =
        """
            package a.b.c;
            public class BaseClass {
              public FooBar run() {
                System.out.println("base class");
              }
            }
            """;
    assertEquals(CoreTestingUtils.normalizeWhitespace(expected), CoreTestingUtils.normalizeWhitespace(cu.source().orElseThrow().asCharSequence()));
    assertFalse(env.compileErrors("a.b.c.BaseClass").isEmpty());

    //now fix the unresolved type error
    cuSrc.mainType().orElseThrow().methods().findAny().orElseThrow().withReturnType(JavaTypes._void);
    cu = registerCompilationUnit(env, cuSrc).requireCompilationUnit();
    assertNotNull(cu);
    assertTrue(env.compileErrors("a.b.c.BaseClass").isEmpty());
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(EmptyJavaEnvironmentFactory.class)
  public void testCreateNewType(IJavaEnvironment env) {
    var cuSrc = createBaseClass();
    var cu = registerCompilationUnit(env, cuSrc).requireCompilationUnit();
    var expected =
        """
            package a.b.c;
            public class BaseClass {
              public void run() {
                System.out.println("base class");
              }
            }
            """;
    assertEquals(CoreTestingUtils.normalizeWhitespace(expected), CoreTestingUtils.normalizeWhitespace(cu.source().orElseThrow().asCharSequence()));

    //now read the type from the env
    var t2 = env.requireType("a.b.c.BaseClass");
    assertEquals(cu.requireMainType().methods().withName("run").first().orElseThrow().source().orElseThrow().asCharSequence(), t2.methods().withName("run").first().orElseThrow().source().orElseThrow().asCharSequence());
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(EmptyJavaEnvironmentFactory.class)
  public void testCreateNewSubType(IJavaEnvironment env) {
    var cuSrc = createBaseClass();
    var cu = registerCompilationUnit(env, cuSrc).requireCompilationUnit();

    // now add a subclass
    cuSrc = createSubClass();
    cu = registerCompilationUnit(env, cuSrc).requireCompilationUnit();

    var expected =
        """
            package a.b.c.d;
            import a.b.c.BaseClass;
            public class SubClass extends BaseClass {
              @Override
              public void run() {
                super.run();
                System.out.println("sub class");
              }
            }
            """;
    assertEquals(CoreTestingUtils.normalizeWhitespace(expected), CoreTestingUtils.normalizeWhitespace(cu.source().orElseThrow().asCharSequence()));
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(EmptyJavaEnvironmentFactory.class)
  public void testCreateExistingTypes(IJavaEnvironment env) {
    // create base type
    var cuSrc = createBaseClass();
    var cu = registerCompilationUnit(env, cuSrc).requireCompilationUnit();

    // create sub type
    cuSrc = createSubClass();
    cu = registerCompilationUnit(env, cuSrc).requireCompilationUnit();
    assertNotNull(cu);

    // re-create modified base type
    cuSrc = createBaseClass();
    cuSrc.mainType().orElseThrow().methods().findAny().orElseThrow().withBody(b -> b.append("System.out.println(\"modified base class\");"));
    cu = registerCompilationUnit(env, cuSrc).requireCompilationUnit();

    var expected = """
        package a.b.c;
        public class BaseClass {
          public void run() {
            System.out.println("modified base class");
          }
        }
        """;
    assertEquals(CoreTestingUtils.normalizeWhitespace(expected), CoreTestingUtils.normalizeWhitespace(cu.source().orElseThrow().asCharSequence()));

    // now read the type from the env
    var t2 = env.requireType("a.b.c.BaseClass");
    assertEquals(cu.requireMainType().methods().withName("run").first().orElseThrow().source().orElseThrow().asCharSequence(), t2.methods().withName("run").first().orElseThrow().source().orElseThrow().asCharSequence());

    // and again re-create modified base type
    cuSrc = createBaseClass();
    cuSrc.mainType().orElseThrow().methods().findAny().orElseThrow().withBody(b -> b.append("System.out.println(\"again modified base class\");"));
    cu = registerCompilationUnit(env, cuSrc).requireCompilationUnit();

    expected = """
        package a.b.c;
        public class BaseClass {
          public void run() {
            System.out.println("again modified base class");
          }
        }
        """;
    assertEquals(CoreTestingUtils.normalizeWhitespace(expected), CoreTestingUtils.normalizeWhitespace(cu.source().orElseThrow().asCharSequence()));

    // now read the type from the env
    t2 = env.requireType("a.b.c.BaseClass");
    assertEquals(cu.requireMainType().methods().withName("run").first().orElseThrow().source().orElseThrow().asCharSequence(), t2.methods().withName("run").first().orElseThrow().source().orElseThrow().asCharSequence());

  }

  private static ICompilationUnitGenerator<?> createBaseClass() {
    return CompilationUnitGenerator.create()
        .withElementName("BaseClass.java")
        .withPackageName("a.b.c")
        .withType(TypeGenerator.create()
            .asPublic()
            .withElementName("BaseClass")
            .withMethod(MethodGenerator.create()
                .asPublic()
                .withElementName("run")
                .withReturnType(JavaTypes._void)
                .withBody(b -> b.append("System.out.println(\"base class\");"))));
  }

  private static ICompilationUnitGenerator<?> createSubClass() {
    return CompilationUnitGenerator.create()
        .withElementName("SubClass.java")
        .withPackageName("a.b.c.d")
        .withType(TypeGenerator.create()
            .asPublic()
            .withElementName("SubClass")
            .withSuperClass("a.b.c.BaseClass")
            .withMethod(MethodGenerator.create()
                .withAnnotation(AnnotationGenerator.createOverride())
                .asPublic()
                .withElementName("run")
                .withReturnType(JavaTypes._void)
                .withBody(b -> b.append("super.run();\nSystem.out.println(\"sub class\");"))));
  }
}
