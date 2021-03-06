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
package org.eclipse.scout.sdk.core.generator;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;

import org.eclipse.scout.sdk.core.builder.java.expression.IExpressionBuilder;
import org.eclipse.scout.sdk.core.fixture.sub.PackageAnnotation;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.CompilationUnitGenerator;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.DefaultCommentGeneratorExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.transformer.SimpleWorkingCopyTransformerBuilder;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DefaultCommentGeneratorExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class PackageGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/generator/";

  @Test
  public void testPackageGenerator(IJavaEnvironment env) {
    var generator = CompilationUnitGenerator.create()
        .withElementName(JavaTypes.PackageInfo)
        .withPackage(PackageGenerator.create()
            .withElementName("test.pck")
            .withComment(b -> b.appendBlockComment("pck block comment"))
            .withAnnotation(AnnotationGenerator.create()
                .withElementName(PackageAnnotation.class.getName())
                .withElement("testAttrib", Strings.toStringLiteral("testValue"))));
    assertEqualsRefFile(env, REF_FILE_FOLDER + "PackageGeneratorTest1.txt", generator);
    assertNoCompileErrors(env, generator);
  }

  @Test
  public void testPackageTransformationSimple(IJavaEnvironment env) {
    var testClass = env.requireType("org.eclipse.scout.sdk.core.fixture.sub.package-info");
    var generator = testClass.requireCompilationUnit().toWorkingCopy();
    assertEqualsRefFile(env, REF_FILE_FOLDER + "PackageGeneratorTest2.txt", generator);
    assertNoCompileErrors(env, generator);
  }

  @Test
  public void testPackageTransformationWithTransformation(IJavaEnvironment env) {
    var testClass = env.requireType("org.eclipse.scout.sdk.core.fixture.sub.package-info");
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
