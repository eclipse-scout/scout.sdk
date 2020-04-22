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
package org.eclipse.scout.sdk.core.model.api;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.sdk.core.fixture.AbstractBaseClass;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.ClassWithMembers;
import org.eclipse.scout.sdk.core.fixture.ImportTestClass;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel0;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel1;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel2;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation;
import org.eclipse.scout.sdk.core.fixture.TypeWithParameterizedSuperType;
import org.eclipse.scout.sdk.core.fixture.WildcardChildClass;
import org.eclipse.scout.sdk.core.fixture.sub.ClassWithWildcardImport;
import org.eclipse.scout.sdk.core.fixture.sub.ImportTestClass2;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class TypeTest {
  @Test
  public void testPrimaryType(IJavaEnvironment env) {
    IType childClassType = env.requireType(ChildClass.class.getName());

    assertEquals(0, childClassType.arrayDimension());
    assertEquals(Flags.AccPublic, childClassType.flags());
    assertEquals(1, childClassType.annotations().stream().count());
    assertFalse(childClassType.declaringType().isPresent());
    assertEquals(2, childClassType.fields().stream().count());
    assertEquals(3, childClassType.methods().stream().count());
    assertEquals(ChildClass.class.getName(), childClassType.name());
    assertEquals(ChildClass.class.getSimpleName(), childClassType.elementName());
    assertEquals(BaseClass.class.getName(), childClassType.requireSuperClass().name());
    assertFalse(childClassType.isParameterType());
    assertFalse(childClassType.isArray());
    assertTrue(childClassType.hasTypeParameters());
    assertEquals(ChildClass.class.getName(), childClassType.toWorkingCopy().fullyQualifiedName());

    // super interfaces
    assertEquals(1, childClassType.superInterfaces().count());
    assertEquals(InterfaceLevel0.class.getName(), childClassType.superInterfaces().findAny().get().name());

    // type parameters
    assertEquals(1, childClassType.typeParameters().count());
    ITypeParameter firstTypeParam = childClassType.typeParameters().findAny().get();
    assertNotNull(firstTypeParam);
    assertEquals(childClassType, firstTypeParam.declaringMember());
    assertEquals("X", firstTypeParam.elementName());
    assertEquals(3, firstTypeParam.bounds().count());
    assertEquals(AbstractList.class.getName(), firstTypeParam.bounds().findAny().get().name());
    assertEquals(Runnable.class.getName(), firstTypeParam.bounds().skip(1).findAny().get().name());
    assertEquals(Serializable.class.getName(), firstTypeParam.bounds().skip(2).findAny().get().name());

    // member types
    assertEquals(0, childClassType.innerTypes().stream().count());

    // type arguments
    assertEquals(0, childClassType.typeArguments().count());
  }

  @Test
  @SuppressWarnings("unlikely-arg-type")
  public void testUnresolvedType(IJavaEnvironment env) {
    Class<?> existingClass = AbstractBaseClass.class;
    String notExistingFqn = "a.b.c.NotExisting";
    IUnresolvedType notExisting = env.findUnresolvedType(notExistingFqn);
    IUnresolvedType existing = env.findUnresolvedType(existingClass.getName());

    assertNotNull(existing);
    assertTrue(existing.exists());
    assertEquals(existingClass.getSimpleName(), existing.elementName());
    assertEquals(existingClass.getName(), existing.name());
    assertEquals(existingClass.getPackage().getName(), existing.containingPackage().elementName());
    assertEquals(existingClass.getName(), existing.reference());
    assertTrue(existing.type().isPresent());
    assertEquals(existingClass.getName(), existing.type().get().name());
    assertNotNull(existing.unwrap());
    assertEquals(CoreTestingUtils.removeWhitespace(existing.type().get().source().get().asCharSequence()), CoreTestingUtils.removeWhitespace(existing.toWorkingCopy().toJavaSource()));
    assertTrue(existing.source().isPresent());
    assertEquals(existingClass.getName(), existing.toWorkingCopy().fullyQualifiedName());

    assertEquals(notExistingFqn, notExisting.toWorkingCopy().fullyQualifiedName());
    assertNotNull(notExisting);
    assertFalse(notExisting.exists());
    assertEquals("NotExisting", notExisting.elementName());
    assertEquals(notExistingFqn, notExisting.name());
    assertEquals("a.b.c", notExisting.containingPackage().elementName());
    assertEquals(notExistingFqn, notExisting.reference());
    assertFalse(notExisting.type().isPresent());
    assertNull(notExisting.unwrap().getType());
    assertEquals("classNotExisting{}", CoreTestingUtils.removeWhitespace(notExisting.toWorkingCopy().toJavaSource().toString()));
    assertFalse(notExisting.source().isPresent());

    //noinspection SimplifiableJUnitAssertion,EqualsWithItself
    assertTrue(existing.equals(existing));
    //noinspection ConstantConditions,SimplifiableJUnitAssertion
    assertFalse(existing.equals(null));
    //noinspection SimplifiableJUnitAssertion,EqualsBetweenInconvertibleTypes
    assertFalse(existing.equals("other class"));
    //noinspection SimplifiableJUnitAssertion
    assertFalse(existing.equals(notExisting));
    assertNotEquals(existing.hashCode(), notExisting.hashCode());
  }

  @Test
  public void testSourceOfStaticInitializer(IJavaEnvironment env) {
    IType classWithMembers = env.requireType(ClassWithMembers.class.getName());
    assertEquals("static{System.out.println(\"staticsection\");}", CoreTestingUtils.removeWhitespace(classWithMembers.sourceOfStaticInitializer().get().asCharSequence().toString()));
  }

  @Test
  public void testResolveTypeParamValue(IJavaEnvironment env) {
    List<IType> resolvedTypeParamValueSignature = env.requireType(ChildClass.class.getName()).resolveTypeParamValue(0, InterfaceLevel1.class.getName()).get().collect(toList());
    assertEquals(1, resolvedTypeParamValueSignature.size());
    assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), resolvedTypeParamValueSignature.get(0).name());

    resolvedTypeParamValueSignature = env.requireType(ChildClass.class.getName()).resolveTypeParamValue(0, BaseClass.class.getName()).get().collect(toList());
    assertEquals(3, resolvedTypeParamValueSignature.size());
    assertEquals(AbstractList.class.getName() + JavaTypes.C_GENERIC_START + String.class.getName() + JavaTypes.C_GENERIC_END, resolvedTypeParamValueSignature.get(0).reference());
    assertEquals(AbstractList.class.getName(), resolvedTypeParamValueSignature.get(0).reference(true));
    assertEquals(Runnable.class.getName(), resolvedTypeParamValueSignature.get(1).reference());
    assertEquals(Serializable.class.getName(), resolvedTypeParamValueSignature.get(2).reference());
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    IType childClassType = env.requireType(ChildClass.class.getName());
    assertFalse(Strings.isBlank(childClassType.toString()));
  }

  @Test
  public void testWildcard(IJavaEnvironment env) {
    IType wildcardType = env.requireType(WildcardChildClass.class.getName());
    IType returnType = wildcardType.methods().first().get().requireReturnType();
    IType firstArg = returnType.typeArguments().findAny().get();
    assertTrue(firstArg.isWildcardType());
    assertEquals(BaseClass.class.getName(), firstArg.name());
    assertEquals(2, firstArg.typeArguments().count());
    assertEquals("java.lang.Class<? extends org.eclipse.scout.sdk.core.fixture.BaseClass<?,?>>", returnType.reference());
  }

  @Test
  public void testPrimaryTypeSuper(IJavaEnvironment env) {
    IType baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();

    assertEquals(0, baseClassType.arrayDimension());
    assertEquals(Flags.AccPublic, baseClassType.flags());
    assertEquals(1, baseClassType.annotations().stream().count());
    assertFalse(baseClassType.requireCompilationUnit().isSynthetic());
    assertFalse(baseClassType.declaringType().isPresent());
    assertEquals(2, baseClassType.fields().stream().count());
    assertEquals(2, baseClassType.methods().stream().count());
    assertEquals(BaseClass.class.getName(), baseClassType.name());
    assertEquals(BaseClass.class.getSimpleName(), baseClassType.elementName());
    assertEquals(Object.class.getName(), baseClassType.requireSuperClass().name());
    assertFalse(baseClassType.isParameterType());
    assertFalse(baseClassType.isArray());
    assertTrue(baseClassType.hasTypeParameters());

    // super interfaces
    assertEquals(1, baseClassType.superInterfaces().count());
    assertEquals(InterfaceLevel1.class.getName(), baseClassType.superInterfaces().findAny().get().name());

    // type parameters
    assertEquals(2, baseClassType.typeParameters().count());
    ITypeParameter firstTypeParam = baseClassType.typeParameters().findAny().get();
    assertNotNull(firstTypeParam);
    assertEquals(baseClassType, firstTypeParam.declaringMember());
    assertEquals("T", firstTypeParam.elementName());
    assertEquals(0, firstTypeParam.bounds().count());

    ITypeParameter secondTypeParam = baseClassType.typeParameters().skip(1).findAny().get();
    assertNotNull(secondTypeParam);
    assertEquals(baseClassType, secondTypeParam.declaringMember());
    assertEquals("Z", secondTypeParam.elementName());
    assertEquals(0, secondTypeParam.bounds().count());

    // member types
    assertEquals(2, baseClassType.innerTypes().stream().count());
    assertEquals(Flags.AccStatic, baseClassType.innerTypes().first().get().flags());
    assertEquals(baseClassType, baseClassType.innerTypes().first().get().declaringType().get());
    assertEquals(Flags.AccProtected, baseClassType.innerTypes().item(1).get().flags());
    assertEquals(baseClassType, baseClassType.innerTypes().item(1).get().declaringType().get());

    // type arguments
    List<IType> typeArguments = baseClassType.typeArguments().collect(toList());
    assertEquals(2, typeArguments.size());
    IType firstTypeArg = typeArguments.get(0);
    assertTrue(firstTypeArg.isParameterType());
    assertEquals(AbstractList.class.getName(), firstTypeArg.requireSuperClass().name());
    assertEquals(2, firstTypeArg.superInterfaces().count());
    assertEquals(Runnable.class.getName(), firstTypeArg.superInterfaces().findAny().get().name());
    assertEquals(Serializable.class.getName(), firstTypeArg.superInterfaces().skip(1).findAny().get().name());

    IType secondTypeArg = typeArguments.get(1);
    assertFalse(secondTypeArg.isParameterType());
    assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), secondTypeArg.name());
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentBinaryOnlyFactory.class)
  public void testInnerTypeDirectly(IJavaEnvironment env) {
    IType innerClass2 = env.requireType("org.eclipse.scout.sdk.core.fixture.BaseClass$InnerClass2");
    testInnerType(innerClass2);
  }

  @Test
  public void testGetPrimaryType(IJavaEnvironment env) {
    IType baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    assertSame(baseClassType, baseClassType.primary());
    assertSame(baseClassType, baseClassType.innerTypes().first().get().primary());
  }

  @Test
  public void testGetInnerTypes(IJavaEnvironment env) {
    assertEquals(1, env.requireType(ChildClass.class.getName()).requireSuperClass().innerTypes().withFlags(Flags.AccStatic).stream().count());
  }

  @Test
  public void testFindInnerType(IJavaEnvironment env) {
    assertEquals(env.requireType(ChildClass.class.getName()).requireSuperClass().innerTypes().first().get(), env.requireType(ChildClass.class.getName()).requireSuperClass().innerTypes().withSimpleName("InnerClass1").first().get());
  }

  @Test
  public void testGetAllSuperInterfaces(IJavaEnvironment env) {
    assertEquals(2, env.requireType(ChildClass.class.getName()).requireSuperClass().superTypes().withSelf(false).withSuperClasses(false).stream().count());
  }

  @Test
  public void testGetFields(IJavaEnvironment env) {
    assertEquals(1, env.requireType(ChildClass.class.getName()).fields().withName("m_test").stream().count());
  }

  @Test
  public void testResolveSimpleName(IJavaEnvironment env) {
    // wildcard package
    IType type = env.requireType(ClassWithWildcardImport.class.getName());

    // if this test fails the IDE might have adapted the import in this file. It must be a wildcard package import!
    assertEquals(1, type.requireCompilationUnit().imports().filter(imp -> imp.name().endsWith(".*")).count());
    assertEquals(BaseClass.class.getName(), type.resolveSimpleName(BaseClass.class.getSimpleName()).get().name());

    // explicit package
    type = env.requireType(ImportTestClass2.class.getName());
    assertEquals(ImportTestClass.class.getName(), type.resolveSimpleName(ImportTestClass.class.getSimpleName()).get().name());

    // type which is not references
    assertFalse(type.resolveSimpleName(IOException.class.getSimpleName()).isPresent());

    // in own package
    type = env.requireType(AbstractBaseClass.class.getName());
    assertEquals(TestAnnotation.class.getName(), type.resolveSimpleName(TestAnnotation.class.getSimpleName()).get().name());

    // java.lang package
    type = env.requireType(AbstractBaseClass.class.getName());
    assertEquals(AutoCloseable.class.getName(), type.resolveSimpleName(AutoCloseable.class.getSimpleName()).get().name());

    // already fully qualified
    assertEquals(IOException.class.getName(), type.resolveSimpleName(IOException.class.getName()).get().name());

    assertFalse(type.resolveSimpleName(null).isPresent());
    assertFalse(type.resolveSimpleName("").isPresent());
    assertFalse(type.resolveSimpleName(" ").isPresent());

    // resolve on a primitive type
    assertFalse(env.requireType(JavaTypes._int).resolveSimpleName(Long.class.getSimpleName()).isPresent());
  }

  @Test
  public void testInnerTypeFromDeclaringType(IJavaEnvironment env) {
    IType baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    IType innerClass2 = baseClassType.innerTypes().item(1).get();
    testInnerType(innerClass2);
  }

  private static void testInnerType(IType innerClass2) {
    assertNotNull(innerClass2);

    assertEquals(0, innerClass2.arrayDimension());
    assertEquals(Flags.AccProtected, innerClass2.flags());
    assertEquals(0, innerClass2.annotations().stream().count());
    IType baseClass = innerClass2.javaEnvironment().requireType(ChildClass.class.getName()).requireSuperClass();
    assertEquals(baseClass.name(), innerClass2.declaringType().get().name());
    assertEquals(1, innerClass2.fields().stream().count());
    assertEquals(1, innerClass2.methods().stream().count());
    assertEquals("org.eclipse.scout.sdk.core.fixture.BaseClass$InnerClass2", innerClass2.name());
    assertEquals("InnerClass2", innerClass2.elementName());
    assertEquals(ArrayList.class.getName(), innerClass2.requireSuperClass().name());
    assertFalse(innerClass2.isParameterType());
    assertFalse(innerClass2.isArray());
    assertFalse(innerClass2.hasTypeParameters());

    // super interfaces
    assertEquals(0, innerClass2.superInterfaces().count());

    // type parameters
    assertEquals(0, innerClass2.typeParameters().count());

    // member types
    assertEquals(0, innerClass2.innerTypes().stream().count());

    // type arguments
    List<IType> typeArguments = innerClass2.typeArguments().collect(toList());
    assertEquals(0, typeArguments.size());

    // super type arguments
    List<IType> superTypeArguments = innerClass2.requireSuperClass().typeArguments().collect(toList());
    assertEquals(1, superTypeArguments.size());
    IType firstTypeArg = superTypeArguments.get(0);
    assertFalse(firstTypeArg.isParameterType());
    assertTrue(firstTypeArg.isArray());
    assertEquals(1, firstTypeArg.arrayDimension());
    assertEquals(BigDecimal.class.getName(), firstTypeArg.leafComponentType().get().name());
  }

  @Test
  public void testIsOnClasspath(IJavaEnvironment env) {
    IType baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    IJavaEnvironment environment = baseClassType.javaEnvironment();
    assertTrue(environment.exists(baseClassType));
    assertFalse(environment.exists((IType) null));

    assertTrue(environment.exists(JavaTypes.Long));
    assertTrue(environment.exists(org.eclipse.scout.sdk.core.fixture.Long.class.getName()));
    assertFalse(environment.exists("not.existing.Type"));
    assertFalse(environment.exists((String) null));
  }

  @Test
  public void testWithParameterizedSuperType(IJavaEnvironment env) {
    IType t = env.requireType(TypeWithParameterizedSuperType.class.getName());
    assertEquals("AbstractList", t.requireSuperClass().elementName());
  }

  @Test
  public void testIsInstanceOf(IJavaEnvironment env) {
    assertTrue(env.requireType(ChildClass.class.getName()).isInstanceOf(BaseClass.class.getName()));
    assertTrue(env.requireType(ChildClass.class.getName()).isInstanceOf(InterfaceLevel2.class.getName()));
    assertFalse(env.requireType(ChildClass.class.getName()).isInstanceOf(org.eclipse.scout.sdk.core.fixture.Long.class.getName()));
    assertFalse(env.requireType(ChildClass.class.getName()).isInstanceOf(JavaTypes.Long));
  }
}
