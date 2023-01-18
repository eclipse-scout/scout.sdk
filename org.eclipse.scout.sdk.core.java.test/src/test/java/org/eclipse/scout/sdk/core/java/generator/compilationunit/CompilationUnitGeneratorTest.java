/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.compilationunit;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.eclipse.scout.sdk.core.java.builder.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.java.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.DefaultCommentGeneratorExtension;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link CompilationUnitGeneratorTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWith(DefaultCommentGeneratorExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class CompilationUnitGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/java/generator/compilationunit/";

  @Test
  public void testCompilationUnit(IJavaEnvironment env) {
    var generator = CompilationUnitGenerator.create()
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
        .withoutType(t -> "SecondClassInFile".equals(t.elementName().orElseThrow()))
        .withoutType(t -> "NotExisting".equals(t.elementName().orElseThrow()));
    assertEqualsRefFile(env, REF_FILE_FOLDER + "CompilationUnitGeneratorTest1.txt", generator);
    assertNoCompileErrors(env, generator);
  }

  @Test
  public void testMainType() {
    var generator = CompilationUnitGenerator.create()
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
