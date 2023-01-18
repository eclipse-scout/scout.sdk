/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertNoCompileErrors;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.JavaUtils;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.java.fixture.sub.PackageAnnotation;
import org.eclipse.scout.sdk.core.java.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.java.generator.compilationunit.CompilationUnitGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IPackage;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.DefaultCommentGeneratorExtension;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.java.transformer.SimpleWorkingCopyTransformerBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DefaultCommentGeneratorExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class PackageGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/java/generator/";

  @Test
  public void testPackageGenerator(IJavaEnvironment env) {
    var generator = CompilationUnitGenerator.create()
        .withElementName(JavaTypes.PackageInfo)
        .withPackage(PackageGenerator.create()
            .withElementName("test.pck")
            .withComment(b -> b.appendBlockComment("pck block comment"))
            .withAnnotation(AnnotationGenerator.create()
                .withElementName(PackageAnnotation.class.getName())
                .withElement("testAttrib", JavaUtils.toStringLiteral("testValue"))));
    assertEqualsRefFile(env, REF_FILE_FOLDER + "PackageGeneratorTest1.txt", generator);
    assertNoCompileErrors(env, generator);
  }

  @Test
  public void testPackageTransformationSimple(IJavaEnvironment env) {
    var testClass = env.requireType("org.eclipse.scout.sdk.core.java.fixture.sub.package-info");
    var generator = testClass.requireCompilationUnit().toWorkingCopy();
    assertEqualsRefFile(env, REF_FILE_FOLDER + "PackageGeneratorTest2.txt", generator);
    assertNoCompileErrors(env, generator);
  }

  @Test
  public void testPackageTransformationWithTransformation(IJavaEnvironment env) {
    var testClass = env.requireType("org.eclipse.scout.sdk.core.java.fixture.sub.package-info");
    var transformer = new SimpleWorkingCopyTransformerBuilder()
        .withPackageMapper(PackageGeneratorTest::transformPackage)
        .withAnnotationElementMapper(PackageGeneratorTest::transformAnnotationElement)
        .build();
    var generator = testClass.requireCompilationUnit()
        .toWorkingCopy(transformer)
        .withComment(null);
    assertEqualsRefFile(env, REF_FILE_FOLDER + "PackageGeneratorTest3.txt", generator);
    assertNoCompileErrors(env, generator);
  }

  private static ISourceGenerator<IExpressionBuilder<?>> transformAnnotationElement(ITransformInput<IAnnotationElement, ISourceGenerator<IExpressionBuilder<?>>> input) {
    return b -> b.stringLiteral("changedValue");
  }

  private static PackageGenerator transformPackage(ITransformInput<IPackage, PackageGenerator> input) {
    return input.requestDefaultWorkingCopy()
        .withElementName("other.container")
        .withComment(b -> b.appendSingleLineComment("single line comment"));
  }
}
