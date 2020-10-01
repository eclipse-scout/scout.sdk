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
package org.eclipse.scout.sdk.core.generator.method;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.fixture.AbstractClass;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.TypeGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.TypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.DefaultCommentGeneratorExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.UsernameExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link MethodGeneratorTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWith(UsernameExtension.class)
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWith(DefaultCommentGeneratorExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class MethodGeneratorTest {

  private static final String TESTING_TARGET_PACKAGE = "org.eclipse.scout.sdk.test";
  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/generator/method/";

  @Test
  public void testVisibility() {
    assertEquals(Flags.AccPublic, MethodGenerator.create().asPrivate().asPublic().flags());
    assertEquals(Flags.AccProtected, MethodGenerator.create().asPrivate().asProtected().flags());
    assertEquals(Flags.AccPrivate, MethodGenerator.create().asPublic().asPrivate().flags());
    assertEquals(Flags.AccDefault, MethodGenerator.create().asPrivate().asPackagePrivate().flags());

    assertEquals(Flags.AccFinal | Flags.AccStatic | Flags.AccPublic, MethodGenerator.create().asStatic().asFinal().asPrivate().asPublic().flags());
    assertEquals(Flags.AccFinal | Flags.AccStatic | Flags.AccProtected, MethodGenerator.create().asStatic().asFinal().asPrivate().asProtected().flags());
    assertEquals(Flags.AccFinal | Flags.AccStatic | Flags.AccPrivate, MethodGenerator.create().asStatic().asFinal().asPublic().asPrivate().flags());
    assertEquals(Flags.AccFinal | Flags.AccStatic, MethodGenerator.create().asStatic().asFinal().asPrivate().asPackagePrivate().flags());
  }

  @Test
  public void testMethod(IJavaEnvironment env) {
    IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> generator = MethodGenerator.create()
        .asPublic()
        .asAbstract()
        .withAnnotation(AnnotationGenerator.createOverride())
        .withAnnotation(AnnotationGenerator.createSuppressWarnings("checked"))
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withElementName("testMethod")
        .withException(IOException.class.getName())
        .withException(SecurityException.class.getName())
        .withException(RuntimeException.class.getName())
        .withoutException(filter -> RuntimeException.class.getName().equals(filter.apply().get()))
        .withReturnType(JavaTypes._byte)
        .withParameter(
            MethodParameterGenerator.create()
                .asFinal()
                .withElementName("firstParameter")
                .withComment(b -> b.appendJavaDocLine("do not use anymore"))
                .withAnnotation(AnnotationGenerator.createDeprecated())
                .withDataType(JavaTypes.Integer))
        .withParameter(
            MethodParameterGenerator.create()
                .withElementName("second"))
        .withParameter(
            MethodParameterGenerator.create()
                .withElementName("generic")
                .withDataType("T"))
        .withParameter(
            MethodParameterGenerator.create()
                .withElementName("last")
                .notFinal()
                .notVarargs()
                .asVarargs()
                .withElementName("static")
                .withDataType(JavaTypes._int))
        .withoutParameter("second")
        .withTypeParameter(
            TypeParameterGenerator.create()
                .withElementName("T")
                .withBinding(Iterable.class.getName())
                .withComment(b -> b.appendJavaDocLine("type param"))
                .withBinding(Serializable.class.getName()));

    assertEquals("testMethod(java.lang.Integer,T,int)", generator.identifier(env));

    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest1.txt", generator);
  }

  @Test
  public void testSetter(IJavaEnvironment env) {
    IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> generator = MethodGenerator.createSetter("m_testField", JavaTypes._int)
        .asSynchronized()
        .withoutParameter("notexisting")
        .withTypeParameter(
            TypeParameterGenerator.create()
                .withElementName("test"))
        .withoutTypeParameter("notexisting")
        .withoutTypeParameter("test");

    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest2.txt", generator);
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest2.txt", MethodGenerator.createSetter("m_testField", JavaTypes._int, Flags.AccSynchronized | Flags.AccPublic));
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest2.txt", MethodGenerator.createSetter("m_testField", JavaTypes._int, Flags.AccSynchronized | Flags.AccPublic, ""));
  }

  @Test
  public void testGetter(IJavaEnvironment env) {
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest3.txt", MethodGenerator.createGetter("m_testField", JavaTypes._long));
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest3.txt", MethodGenerator.createGetter("m_testField", JavaTypes._long, Flags.AccPublic));
  }

  @Test
  public void testCreateOverride(IJavaEnvironment env) {
    ITypeGenerator<?> typeGenerator = TypeGenerator.create()
        .setDeclaringFullyQualifiedName(TESTING_TARGET_PACKAGE)
        .withSuperClass(AbstractClass.class.getName());

    // override normal unique method with parameters from interface (auto method stub)
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest4.txt",
        new MethodOverrideGenerator<>(null)
            .withDeclaringGenerator(typeGenerator)
            .withElementName("get"));

    // override method with primitive return type
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest5.txt",
        new MethodOverrideGenerator<>(null)
            .withDeclaringGenerator(typeGenerator)
            .withElementName("methodWithArgs"));

    // override method with overloads and filter
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest6.txt",
        new MethodOverrideGenerator<>(null)
            .withDeclaringGenerator(typeGenerator)
            .withElementName("methodWithOverload")
            .withParameter(MethodParameterGenerator.create().withDataType(JavaTypes._int))
            .withParameter(MethodParameterGenerator.create().withDataType(String.class.getName())));

    // override default method in interface
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest7.txt",
        new MethodOverrideGenerator<>(null)
            .withDeclaringGenerator(typeGenerator)
            .withElementName("defMethod"));

    // override void method
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest8.txt",
        new MethodOverrideGenerator<>(null)
            .withDeclaringGenerator(typeGenerator)
            .withElementName("voidMethod"));

    // override constructor
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest10.txt",
        new MethodOverrideGenerator<>(null)
            .withDeclaringGenerator(typeGenerator)
            .withElementName("AbstractClass")
            .withParameter(MethodParameterGenerator.create().withDataType(Float.class.getName())));
  }

  @Test
  public void testCreateUnimplemented(IJavaEnvironment env) {
    PrimaryTypeGenerator<?> typeGenerator = PrimaryTypeGenerator.create()
        .withPackageName("a.b.c")
        .withElementName("TestClass")
        .withSuperClass(AbstractClass.class.getName())
        .withAllMethodsImplemented();
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest11.txt", typeGenerator);
    assertNoCompileErrors(env, typeGenerator);
  }

  @Test
  public void testGetterFromFieldGenerator(IJavaEnvironment env) {
    IFieldGenerator<?> generator = FieldGenerator.create()
        .withElementName("m_testField")
        .withDataType(JavaTypes._long);
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest3.txt", MethodGenerator.createGetter(generator));
  }

  @Test
  public void testSetterFromFieldGenerator(IJavaEnvironment env) {
    IFieldGenerator<?> generator = FieldGenerator.create()
        .withElementName("m_testField")
        .withDataType(JavaTypes._int);
    assertEqualsRefFile(env, REF_FILE_FOLDER + "MethodGeneratorTest2.txt", MethodGenerator.createSetter(generator).asSynchronized());
  }

  @Test
  public void testVarargsInterfaceMethod() {
    String src = MethodGenerator.create()
        .withFlags(Flags.AccVarargs)
        .withFlags(Flags.AccInterface)
        .withElementName("testMethod")
        .withReturnType(JavaTypes._void)
        .withParameter(
            MethodParameterGenerator.create()
                .withElementName("param01")
                .withDataType(JavaTypes._int))
        .toJavaSource()
        .toString();
    assertEquals("void testMethod(int... param01);", src);
  }

}
