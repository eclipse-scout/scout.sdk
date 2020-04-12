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

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.DefaultCommentGeneratorExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link CompilationUnitGeneratorTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWith(DefaultCommentGeneratorExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class CompilationUnitGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/generator/compilationunit/";

  @Test
  public void testCompilationUnit(IJavaEnvironment env) {
    ICompilationUnitGenerator<?> generator = CompilationUnitGenerator.create()
        .withPackageName("pck.test")
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withElementName("TestClass.java")
        .withFooter(b -> b.append("// whatever").nl())
        .withImport(Test.class.getName())
        .withoutImport(Test.class.getName())
        .withImport(StreamSupport.class.getName())
        .withoutAllImports()
        .withImport(StreamSupport.class.getName())
        .withStaticImport(Arrays.class.getName() + ".asList")
        .withoutStaticImport("a.b.c.Test.testMethod")
        .withStaticImport("a.b.c.Test.otherMethod")
        .withoutStaticImport("a.b.c.Test.otherMethod")
        .withType(TypeGenerator.create()
            .withElementName("SecondClassInFile"))
        .withType(TypeGenerator.create()
            .withElementName("ThirdClassInFile")
            .withInterface(Serializable.class.getName()))
        .withType(TypeGenerator.create()
            .asPublic()
            .withElementName("TestClass"))
        .withoutType("SecondClassInFile")
        .withoutType("NotExisting");
    assertEqualsRefFile(env, REF_FILE_FOLDER + "CompilationUnitGeneratorTest1.txt", generator);
    assertNoCompileErrors(env, generator);
  }

  @Test
  public void testMainType() {
    ICompilationUnitGenerator<?> generator = CompilationUnitGenerator.create()
        .withElementName("Test.java")
        .withType(TypeGenerator.create()
            .withElementName("OtherName"));

    assertFalse(generator.mainType().isPresent());

    generator = CompilationUnitGenerator.create()
        .withElementName("Test.java")
        .withType(TypeGenerator.create()
            .asPublic()
            .withElementName("Test"));

    assertTrue(generator.mainType().isPresent());
  }
}
