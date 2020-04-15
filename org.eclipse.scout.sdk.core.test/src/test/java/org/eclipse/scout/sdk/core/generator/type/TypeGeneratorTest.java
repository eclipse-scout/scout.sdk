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
package org.eclipse.scout.sdk.core.generator.type;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.fixture.InterfaceWithTypeParam;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.ITypeParameterGenerator;
import org.eclipse.scout.sdk.core.generator.typeparam.TypeParameterGenerator;
import org.eclipse.scout.sdk.core.model.annotation.GeneratedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.DefaultCommentGeneratorExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.UsernameExtension;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link TypeGeneratorTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWith(UsernameExtension.class)
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWith(DefaultCommentGeneratorExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class TypeGeneratorTest {

  @SuppressWarnings("HardcodedFileSeparator")
  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/generator/type/";
  private static final AtomicLong INSERTION_ORDER = new AtomicLong();

  @Test
  public void testTypeWithSingleFieldAndAllImplementedMethods(IJavaEnvironment env) {
    String superIfc = InterfaceWithTypeParam.class.getName() + JavaTypes.C_GENERIC_START + String.class.getName() + JavaTypes.C_GENERIC_END;
    ITypeGenerator<?> generator = TypeGenerator.create()
        .asPublic()
        .withElementName("TestConsumer")
        .withInterface(superIfc)
        .withField(FieldGenerator.create()
            .asPublic()
            .asStatic()
            .asPackagePrivate()
            .asTransient()
            .asVolatile()
            .withDataType(JavaTypes._double)
            .withElementName("VALUE")
            .withValue(b -> b.append(44.3)))
        .withAllMethodsImplemented()
        .setDeclaringFullyQualifiedName("a.b.c");
    assertEqualsRefFile(env, REF_FILE_FOLDER + "TypeGeneratorTest6.txt", generator);
  }

  @Test
  public void testTypeGenerator(IJavaEnvironment env) {
    Stream<String> interfaces = Stream.of(CharSequence.class.getName(), Comparable.class.getName());

    ITypeGenerator<?> generator = TypeGenerator.create()
        .asPublic()
        .asFinal()
        .withAnnotation(AnnotationGenerator.createGenerated("GeneratorType", "Comments"))
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withElementName("TestClass")
        .withInterfaces(interfaces)
        .withInterface(Serializable.class.getName())
        .withoutInterface(Serializable.class.getName())
        .withSuperClass(AbstractMap.class.getName())
        .withField(
            FieldGenerator.create()
                .asPrivate()
                .withElementName("m_simpleMember")
                .withDataType(JavaTypes._float))
        .withField(FieldGenerator.createSerialVersionUid(), 0, -100, "whatever")
        .withField(FieldGenerator.create()
            .withElementName("willBeRemoved"))
        .withoutField("willBeRemoved")
        .withMethod(MethodGenerator.create()
            .asPublic()
            .withElementName("TestClass"))
        .withMethod(MethodGenerator.create()
            .asPublic()
            .withElementName("testMethod")
            .withReturnType(JavaTypes._void), 0, -200, "whatever")
        .withMethod(MethodGenerator.create()
            .withElementName("toRemove"))
        .withoutMethod("toRemove")
        .withType(TypeGenerator.create()
            .asPublic()
            .withElementName("InnerType"))
        .withType(TypeGenerator.create()
            .asPrivate()
            .asFinal()
            .withElementName("FirstInnerType"), 0, -300, "whatever")
        .withType(TypeGenerator.create()
            .withElementName("RemovedType"))
        .withoutType("RemovedType")
        .withTypeParameter(TypeParameterGenerator.create()
            .withBound(Comparable.class.getName())
            .withElementName("T"))
        .withTypeParameter(TypeParameterGenerator.create()
            .withElementName("willBeRemoved"))
        .withoutTypeParameter("willBeRemoved")
        .withoutTypeParameter("notExisting")
        .setDeclaringFullyQualifiedName("a.b.c");

    assertEquals("a.b.c.TestClass", generator.fullyQualifiedName());
    assertEquals(AbstractMap.class.getName(), generator.superClass().get());
    assertEquals(2, generator.fields().count());
    assertEquals(2, generator.methods().count());
    assertTrue(generator.method("testMethod()").isPresent());
    assertEquals(2, generator.types().count());
    assertEquals(1, generator.typeParameters().count());

    assertEqualsRefFile(env, REF_FILE_FOLDER + "TypeGeneratorTest1.txt", generator);
  }

  @Test
  public void testTypeParameterWithoutName() {
    ITypeParameterGenerator<?> typeParamGenerator = TypeParameterGenerator.create()
        .withBound(CharSequence.class.getName())
        .withBound(Iterable.class.getName())
        .withBound(Comparable.class.getName());
    assertEquals(3, typeParamGenerator.bounds().count());

    String src = TypeGenerator.create()
        .asPublic()
        .asInterface()
        .withElementName("GenericIfc")
        .withTypeParameter(typeParamGenerator)
        .setDeclaringFullyQualifiedName("a.b.c")
        .toJavaSource()
        .toString();

    //noinspection HardcodedLineSeparator
    assertEquals("public interface GenericIfc<? extends CharSequence & Iterable & Comparable> {\n\n}", src);
  }

  @Test
  public void testEnum(IJavaEnvironment env) {
    ITypeGenerator<?> generator = TypeGenerator.create()
        .asPublic()
        .asEnum()
        .withElementName("TestEnum")
        .setDeclaringFullyQualifiedName("a.b.c");

    assertEqualsRefFile(env, REF_FILE_FOLDER + "TypeGeneratorTest4.txt", generator);
  }

  @Test
  public void testAnnotationType(IJavaEnvironment env) {
    ITypeGenerator<?> generator = TypeGenerator.create()
        .asPublic()
        .asAnnotationType()
        .withElementName("TestAnnotation")
        .setDeclaringFullyQualifiedName("a.b.c");

    assertEqualsRefFile(env, REF_FILE_FOLDER + "TypeGeneratorTest3.txt", generator);
  }

  @Test
  public void testInterface(IJavaEnvironment env) {
    ITypeGenerator<?> generator = TypeGenerator.create()
        .asPublic()
        .asInterface()
        .withElementName("ITest")
        .withInterface(Comparable.class.getName())
        .withInterface(List.class.getName())
        .setDeclaringFullyQualifiedName("a.b.c");

    assertEqualsRefFile(env, REF_FILE_FOLDER + "TypeGeneratorTest2.txt", generator);
  }

  @Test
  public void testClassWithObjectSuperClass(IJavaEnvironment env) {
    ITypeGenerator<?> generator = TypeGenerator.create()
        .asPublic()
        .withElementName("ClassWithObjectSuperClass")
        .withSuperClass(Object.class.getName())
        .setDeclaringFullyQualifiedName("a.b.c");

    assertEqualsRefFile(env, REF_FILE_FOLDER + "TypeGeneratorTest5.txt", generator);
  }

  @Test
  public void testDefaultMethodSortOrder() {
    assertMethodOrder(1000, MethodGenerator.create().withElementName("Ctor"));
    assertMethodOrder(2000, MethodGenerator.create().withReturnType(JavaTypes._int).withElementName("getMyValue"));
    assertMethodOrder(2000, MethodGenerator.create().withReturnType(JavaTypes._void).withElementName("setMyValue"));
    assertMethodOrder(2000, MethodGenerator.create().withReturnType(JavaTypes._boolean).withElementName("isMyValue"));
    assertMethodOrder(3000, MethodGenerator.create().asStatic().withReturnType(JavaTypes._int).withElementName("getMyStaticValue"));
    assertMethodOrder(4000, MethodGenerator.create().withReturnType(JavaTypes._int).withElementName("otherOperation"));
  }

  @Test
  public void testDefaultTypeSortOrder() {
    assertTypeOrder(1000, TypeGenerator.create().asPublic().withElementName("type01"));
    assertTypeOrder(3000, TypeGenerator.create().asStatic().withElementName("type02"));
    assertTypeOrder(2000, TypeGenerator.create().withElementName("type03"));
  }

  @Test
  public void testDefaultFieldSortOrder() {
    assertFieldOrder(1000, FieldGenerator.createSerialVersionUid());
    assertFieldOrder(2000, FieldGenerator.create().asStatic().asFinal().withElementName("field01"));
    assertFieldOrder(3000, FieldGenerator.create().asFinal().withElementName("field02"));
    assertFieldOrder(4000, FieldGenerator.create().asPrivate().withElementName("field03"));
  }

  @Test
  public void testUnableToAddPrimaryTypeToType() {
    PrimaryTypeGenerator<?> primary = PrimaryTypeGenerator.create();
    PrimaryTypeGenerator<?> nested = PrimaryTypeGenerator.create();
    assertEquals("A PrimaryTypeGenerator cannot be added as nested type. Use a TypeGenerator instead.", assertThrows(IllegalArgumentException.class, () -> primary.withType(nested)).getMessage());
  }

  @Test
  public void testAnnotationReferringNestedType(IJavaEnvironment env) {
    String idFieldName = "ID";
    PrimaryTypeGenerator<?> primary = PrimaryTypeGenerator.create()
        .withElementName("Outer")
        .withPackageName("a.b.c");
    ITypeGenerator<?> inner = TypeGenerator.create()
        .withElementName("Inner")
        .withField(FieldGenerator.create()
            .asPublic()
            .asStatic()
            .asFinal()
            .withDataType(String.class.getName())
            .withElementName(idFieldName)
            .withValue(b -> b.stringLiteral("value")));
    primary.withType(inner)
        .withAnnotation(AnnotationGenerator.createGenerated("whatever")
            .withElement(GeneratedAnnotation.VALUE_ELEMENT_NAME, b -> b.ref(inner.fullyQualifiedName()).dot().append(idFieldName)))
        .withMethod(MethodGenerator.create()
            .withElementName("setter")
            .withReturnType(JavaTypes._void)
            .withParameter(MethodParameterGenerator.create()
                .withDataType(inner.fullyQualifiedName())
                .withElementName("param")));
    IType result = assertNoCompileErrors(env, primary);

    // import to the nested type must be present for the annotation
    assertEquals(2, result.requireCompilationUnit().imports().count());
  }

  protected static void assertMethodOrder(int expectedOrder, IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> generator) {
    long insertionOrder = INSERTION_ORDER.getAndIncrement();
    assertMemberOrder(expectedOrder, SortedMemberEntry.METHOD_ORDER, insertionOrder, SortedMemberEntry.defaultMethodOrder(generator, insertionOrder));
  }

  protected static void assertTypeOrder(int expectedOrder, ITypeGenerator<?> generator) {
    long insertionOrder = INSERTION_ORDER.getAndIncrement();
    assertMemberOrder(expectedOrder, SortedMemberEntry.TYPE_ORDER, insertionOrder, SortedMemberEntry.defaultTypeOrder(generator, insertionOrder));
  }

  protected static void assertFieldOrder(int expectedOrder, IFieldGenerator<?> generator) {
    long insertionOrder = INSERTION_ORDER.getAndIncrement();
    assertMemberOrder(expectedOrder, SortedMemberEntry.FIELD_ORDER, insertionOrder, SortedMemberEntry.defaultFieldOrder(generator, insertionOrder));
  }

  protected static void assertMemberOrder(int expectedOrder, int memberKind, long insertionOrder, CompositeObject result) {
    assertEquals(new CompositeObject(SortedMemberEntry.DEFAULT_ORDER, memberKind, expectedOrder, insertionOrder), result);
  }
}
