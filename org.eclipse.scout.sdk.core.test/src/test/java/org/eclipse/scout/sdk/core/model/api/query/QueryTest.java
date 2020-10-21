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
package org.eclipse.scout.sdk.core.model.api.query;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.fixture.AbstractBaseClass;
import org.eclipse.scout.sdk.core.fixture.AbstractChildClass;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel1;
import org.eclipse.scout.sdk.core.fixture.MarkerAnnotation;
import org.eclipse.scout.sdk.core.fixture.MethodParamHierarchicAnnotation;
import org.eclipse.scout.sdk.core.fixture.MethodParamHierarchicAnnotation.ParamAnnotationChildClass;
import org.eclipse.scout.sdk.core.fixture.MethodParamHierarchicAnnotation.ParamMarkerAnnotation;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation;
import org.eclipse.scout.sdk.core.fixture.WildcardBaseClass;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link QueryTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class QueryTest {

  @Test
  public void testSuperTypeQuery(IJavaEnvironment env) {
    var childClass = env.requireType(ChildClass.class.getName());
    assertEquals(3, childClass.superTypes().stream().filter(element -> !element.isInterface()).count());
    assertEquals(3, childClass.superTypes().withFlags(Flags.AccInterface).stream().count());
    assertEquals(2, childClass.superTypes().stream().limit(2).count());
    assertEquals(1, childClass.superTypes().withName(InterfaceLevel1.class.getName()).stream().count());
    assertEquals(1, childClass.superTypes().withSimpleName(InterfaceLevel1.class.getSimpleName()).stream().count());
    assertEquals(5, childClass.superTypes().withSelf(false).stream().count());
    assertEquals(3, childClass.superTypes().withSelf(false).withSuperClasses(false).stream().count());
    assertEquals(4, childClass.superTypes().withSuperClasses(false).stream().count());
    assertEquals(3, childClass.superTypes().withSuperInterfaces(false).stream().count());

    // start with an interface but don't request super interfaces:
    var firstSuperIfc = childClass.superTypes().withSelf(false).withSuperClasses(false).first().get();
    assertEquals(1, firstSuperIfc.superTypes().withSuperInterfaces(false).stream().count());
  }

  @Test
  public void testSuperMethodQuery(IJavaEnvironment env) {
    var acc = env.requireType(AbstractChildClass.class.getName());
    assertEquals(2, acc.methods().withName("blub").first().get().superMethods().stream().count());
    assertEquals(1, acc.methods().withName("blub").first().get().superMethods().stream().limit(1).count());
    assertEquals(1, acc.methods().withName("blub").first().get().superMethods().withSelf(false).stream().count());
    assertEquals(1, acc.methods().withName("blub").first().get().superMethods().withSuperClasses(false).stream().count());
    assertEquals(1, acc.methods().withName("blub").first().get().superMethods().stream().filter(element -> AbstractBaseClass.class.getName().equals(element.requireDeclaringType().name())).count());
  }

  @Test
  public void testTypeQuery(IJavaEnvironment env) {
    var base = env.requireType(ChildClass.class.getName()).requireSuperClass();
    assertEquals(1, base.innerTypes().stream().filter(element -> "InnerClass2".equals(element.elementName())).count());
    assertEquals(1, base.innerTypes().withFlags(Flags.AccStatic).stream().count());
    assertEquals(2, base.innerTypes().withInstanceOf(Collection.class.getName()).stream().count());
    assertEquals(1, base.innerTypes().stream().limit(1).count());
    assertEquals(1, base.innerTypes().withName("org.eclipse.scout.sdk.core.fixture.BaseClass$InnerClass2").stream().count());
    assertEquals(1, base.innerTypes().withSimpleName("InnerClass2").stream().count());

    var abc = env.requireType(AbstractBaseClass.class.getName());
    assertEquals(1, abc.innerTypes().withRecursiveInnerTypes(true).withSimpleName("Leaf3").stream().count());
    assertEquals(1, abc.innerTypes().withRecursiveInnerTypes(true).withSimpleName("Leaf").stream().count());
    assertEquals(1, abc.innerTypes().withRecursiveInnerTypes(true).withInstanceOf("org.eclipse.scout.sdk.core.fixture.AbstractBaseClass$Leaf").stream().count());
    assertEquals(0, env.requireType(ChildClass.class.getName()).innerTypes().withRecursiveInnerTypes(true).withSimpleName("Leaf3").stream().count());
    assertEquals(0, abc.innerTypes().withRecursiveInnerTypes(false).withSimpleName("InnerThree").stream().count());

    assertEquals(2, env.requireType(ChildClass.class.getName()).innerTypes().withSuperClasses(true).stream().count());
    assertEquals("InnerClass2", env.requireType(ChildClass.class.getName()).innerTypes().withSuperClasses(true).item(1).get().elementName());
    assertFalse(env.requireType(Object.class.getName()).innerTypes().withSuperClasses(true).withName("notExisting").existsAny());
    assertEquals(1, env.requireType(ChildClass.class.getName()).requireCompilationUnit().types().stream().count());

    var innerTypeInSuperClass = env.requireType(ChildClass.class.getName()).innerTypes().withSuperClasses(true).stream().filter(element -> "InnerClass2".equals(element.elementName())).findFirst();
    assertTrue(innerTypeInSuperClass.isPresent());
    assertEquals("org.eclipse.scout.sdk.core.fixture.BaseClass$InnerClass2", innerTypeInSuperClass.get().name());
  }

  @Test
  public void testMethodParameterQuery(IJavaEnvironment env) {
    var methodWithParams = env.requireType(AbstractBaseClass.class.getName()).methods().withName("methodWithParams").first().get();
    assertEquals(1, methodWithParams.parameters().withDataType(String.class.getName()).stream().count());
    assertEquals(TestAnnotation.class.getName(), methodWithParams.parameters().withName("firstParam").first().get().annotations().first().get().type().name());
    assertEquals(1, methodWithParams.parameters().stream().limit(1).count());
    assertEquals(1, methodWithParams.parameters().stream().filter(element -> element.annotations().existsAny()).count());
  }

  @Test
  public void testFieldQuery(IJavaEnvironment env) {
    var baseClass = env.requireType(ChildClass.class.getName()).requireSuperClass();
    var childClass = env.requireType(ChildClass.class.getName());

    assertEquals(1, baseClass.fields().stream().filter(element -> element.dataType().name().equals(JavaTypes.Long)).count());
    assertEquals(1, childClass.fields().withFlags(Flags.AccProtected).stream().count());
    assertEquals(1, childClass.fields().stream().limit(1).count());
    assertEquals(1, childClass.fields().withName("m_test").stream().count());
    assertEquals(3, childClass.fields().withSuperTypes(true).withFlags(Flags.AccPublic).stream().count());
  }

  @Test
  public void testMethodQuery(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    assertEquals(1, childClassType.methods().withAnnotation(TestAnnotation.class.getName()).stream().count());

    var list = childClassType.methods().stream().filter((element) -> !element.parameters().existsAny()).collect(toList());
    assertEquals(2, list.size());
    assertTrue(list.get(0).isConstructor());

    assertEquals(1, childClassType.methods().withFlags(Flags.AccProtected | Flags.AccSynchronized).stream().count());
    assertEquals(1, childClassType.methods().withFlags(Flags.AccPrivate).stream().count());
    assertEquals(2, childClassType.methods().stream().limit(2).count());
    assertEquals(2, childClassType.methods().withName(Pattern.compile("[a-z0-9_]+class", Pattern.CASE_INSENSITIVE)).stream().count());
    assertEquals(1, childClassType.methods().withName("firstCase").stream().count());
    assertEquals(1, childClassType.methods().withSuperClasses(true).withName("method2InBaseClass").stream().count());
    assertEquals(1, childClassType.methods().withSuperClasses(true).withMethodIdentifier("methodInChildClass(java.lang.String,java.util.List<java.lang.Runnable>)").stream().count());

    var abstractBaseClass = env.requireType(AbstractBaseClass.class.getName());
    assertEquals(1, abstractBaseClass.methods().withSuperInterfaces(true).withName("close").stream().count());
    assertEquals(1, abstractBaseClass.methods().withSuperTypes(true).withName("close").stream().count());
  }

  @Test
  public void testAnnotationQuery(IJavaEnvironment env) {
    var acc = env.requireType(AbstractChildClass.class.getName());
    var childClass = env.requireType(ChildClass.class.getName());
    var wbc = env.requireType(WildcardBaseClass.class.getName());
    assertEquals(2, acc.methods().withName("blub").first().get().annotations().withSuperTypes(true).withName(MarkerAnnotation.class.getName()).stream().count());
    assertEquals(2, childClass.annotations().withSuperClasses(true).withName(TestAnnotation.class.getName()).stream().count());

    assertEquals(1, wbc.annotations().stream().filter(element -> element.element("inner").get().value() instanceof IArrayMetaValue).count());
    assertEquals(1, acc.methods().withName("blub").first().get().annotations().stream().limit(1).count());

    var methodInChildClass = childClass.methods().withName("methodInChildClass").first().get();
    assertEquals(1, methodInChildClass.annotations().stream().filter(element -> element.elements().size() == 3).count());

    // annotations on hierarchical method params
    var rootType = env.requireType(MethodParamHierarchicAnnotation.class.getName());
    var paramAnnotationChildClass = rootType.innerTypes().withSimpleName(ParamAnnotationChildClass.class.getSimpleName()).first().get();
    var firstParam = paramAnnotationChildClass.methods().first().get().parameters().first().get();
    var secondParam = paramAnnotationChildClass.methods().first().get().parameters().item(1).get();

    var firstParamAnnotations = firstParam.annotations().withSuperTypes(true).stream().collect(toList());
    assertEquals(2, firstParamAnnotations.size());
    assertEquals("test", firstParamAnnotations.get(1).element("msg").get().value().as(String.class));

    var secondParamAnnotations = secondParam.annotations().withSuperTypes(true).stream().collect(toList());
    assertEquals(1, secondParamAnnotations.size());
    assertEquals(ParamMarkerAnnotation.class.getName(), secondParamAnnotations.get(0).type().name());

  }
}
