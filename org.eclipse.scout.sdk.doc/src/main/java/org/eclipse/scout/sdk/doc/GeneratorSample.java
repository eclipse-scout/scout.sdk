/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.doc;

import static java.lang.System.lineSeparator;

import java.io.Serializable;
import java.util.AbstractList;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.builder.JavaBuilderContext;
import org.eclipse.scout.sdk.core.java.builder.JavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.builder.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.java.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.java.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.log.SdkLog;

@SuppressWarnings({"MethodMayBeStatic"})
public class GeneratorSample {

  public static void main(String[] args) {
    new GeneratorSample().buildSource();
  }

  protected void buildSource() {
    new EmptyJavaEnvironmentFactory().accept(this::buildSource);
  }

  // tag::generatorSample[]
  protected void buildSource(IJavaEnvironment javaEnvironment) {
    var generator = newCharSequenceListGenerator(); // <1>
    executeGeneratorWithBuilder(generator, javaEnvironment);
    executeGeneratorWithContext(generator, javaEnvironment);
  }

  protected ITypeGenerator<?> newCharSequenceListGenerator() {
    var charSequenceListRef = AbstractList.class.getName() + // <2>
        JavaTypes.C_GENERIC_START + CharSequence.class.getName() + JavaTypes.C_GENERIC_END;
    return PrimaryTypeGenerator.create() // <3>
        .asPublic()
        .withElementName("CharSequenceList")
        .withInterface(Serializable.class.getName())
        .withSuperClass(charSequenceListRef)
        .withField(FieldGenerator.createSerialVersionUid()) // <4>
        .withMethod(MethodGenerator.create() // <5>
            .asPublic()
            .withReturnType(CharSequence.class.getName())
            .withElementName("get")
            .withAnnotation(AnnotationGenerator.createOverride()) // <6>
            .withParameter(MethodParameterGenerator.create() // <7>
                .withDataType(JavaTypes._int) // <8>
                .withElementName("i"))
            .withBody(this::buildGetMethodBody)) // <9>
        .withMethod(MethodOverrideGenerator.createOverride() // <10>
            .withElementName("iterator"))
        .withMethod(MethodOverrideGenerator.createOverride()
            .withElementName("size"));
  }

  protected void buildGetMethodBody(IMethodBodyBuilder<?> builder) {
    builder.returnClause().nullLiteral().semicolon(); // <11>
  }

  protected void executeGeneratorWithBuilder(ISourceGenerator<ISourceBuilder<?>> generator,
      IJavaEnvironment javaEnvironment) {
    var builder = JavaSourceBuilder.create(MemorySourceBuilder.create(), javaEnvironment); // <12>
    generator.generate(builder); // <13>
    SdkLog.warning(builder.toString());
  }

  protected void executeGeneratorWithContext(IJavaElementGenerator<?> generator,
      IJavaEnvironment javaEnvironment) {
    var context = new JavaBuilderContext(new BuilderContext(lineSeparator()), javaEnvironment); // <14>
    var source = generator.toJavaSource(context); // <15>
    SdkLog.warning(source);
  }
  // end::generatorSample[]
}
