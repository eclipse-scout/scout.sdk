/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.api;

import static org.eclipse.scout.sdk.core.model.api.Flags.isDefaultMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.util.Set;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.ClassWithConstructors;
import org.eclipse.scout.sdk.core.fixture.ClassWithDefaultMethods;
import org.eclipse.scout.sdk.core.fixture.MarkerAnnotation;
import org.eclipse.scout.sdk.core.model.ecj.DeclarationTypeWithEcj;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class MethodTest {

  @Test
  public void testChildClassMethods(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    assertEquals(3, childClassType.methods().stream().count());

    // constructor
    var constr = childClassType.methods().first().orElseThrow();
    assertEquals(childClassType, constr.requireDeclaringType());
    assertEquals(0, constr.exceptionTypes().count());
    assertEquals(Flags.AccPublic, constr.flags());
    assertEquals(childClassType.elementName(), constr.elementName());
    assertEquals(0, constr.parameters().stream().count());
    assertFalse(constr.returnType().isPresent());
    assertTrue(constr.isConstructor());
    assertEquals(0, constr.annotations().stream().count());
    assertFalse(constr.toWorkingCopy().returnType().isPresent());

    // methodInChildClass
    var methodInChildClass = childClassType.methods().item(1).orElseThrow();
    assertEquals(childClassType, methodInChildClass.requireDeclaringType());
    assertEquals(1, methodInChildClass.exceptionTypes().count());
    assertEquals(IOException.class.getName(), methodInChildClass.exceptionTypes().findAny().orElseThrow().name());
    assertEquals(Flags.AccProtected | Flags.AccSynchronized, methodInChildClass.flags());
    assertEquals("methodInChildClass", methodInChildClass.elementName());
    assertEquals(2, methodInChildClass.parameters().stream().count());
    assertEquals(JavaTypes._boolean, methodInChildClass.requireReturnType().leafComponentType().orElseThrow().name());
    assertTrue(methodInChildClass.requireReturnType().isArray());
    assertFalse(methodInChildClass.isConstructor());
    assertEquals(1, methodInChildClass.annotations().stream().count());

    // firstCase
    var firstCase = childClassType.methods().item(2).orElseThrow();
    assertEquals(childClassType, firstCase.requireDeclaringType());
    assertEquals(0, firstCase.exceptionTypes().count());
    assertEquals(Flags.AccPrivate, firstCase.flags());
    assertEquals("firstCase", firstCase.elementName());
    assertEquals(0, firstCase.parameters().stream().count());
    assertEquals(Set.class.getName(), firstCase.requireReturnType().leafComponentType().orElseThrow().name());
    assertFalse(firstCase.isConstructor());
    assertEquals(1, firstCase.annotations().stream().count());
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    assertFalse(Strings.isBlank(childClassType.methods().item(1).orElseThrow().toString()));
  }

  @Test
  public void testDefaultMethods(IJavaEnvironment env) {
    var type = env.requireType(ClassWithDefaultMethods.class.getName());
    assertEquals(7, type.methods().withSuperInterfaces(true).stream().count());
    assertEquals(4, type.superInterfaces().findAny().orElseThrow().methods().stream().count());
    var defaultMethod = type.superInterfaces().findAny().orElseThrow().methods().withName("defMethod2").first().orElseThrow();
    assertTrue(isDefaultMethod(defaultMethod.flags()));
  }

  @Test
  public void testBaseClassMethods(IJavaEnvironment env) {
    var baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();

    assertEquals(2, baseClassType.methods().stream().count());

    // methodInBaseClass
    var methodInBaseClass = baseClassType.methods().first().orElseThrow();
    assertEquals(baseClassType, methodInBaseClass.requireDeclaringType());
    assertEquals(2, methodInBaseClass.exceptionTypes().count());
    assertEquals(IOError.class.getName(), methodInBaseClass.exceptionTypes().findAny().orElseThrow().name());
    assertEquals(FileNotFoundException.class.getName(), methodInBaseClass.exceptionTypes().skip(1).findAny().orElseThrow().name());
    assertEquals(Flags.AccProtected, methodInBaseClass.flags());
    assertEquals("methodInBaseClass", methodInBaseClass.elementName());
    assertEquals(1, methodInBaseClass.parameters().stream().count());
    assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), methodInBaseClass.requireReturnType().leafComponentType().orElseThrow().name());
    assertEquals(2, methodInBaseClass.requireReturnType().arrayDimension());
    assertTrue(methodInBaseClass.requireReturnType().isArray());
    assertFalse(methodInBaseClass.isConstructor());
    assertEquals(2, methodInBaseClass.annotations().stream().count());

    // method2InBaseClass
    var method2InBaseClass = baseClassType.methods().item(1).orElseThrow();
    assertEquals(baseClassType, method2InBaseClass.requireDeclaringType());
    assertEquals(0, method2InBaseClass.exceptionTypes().count());
    assertEquals(Flags.AccPublic | Flags.AccSynchronized | Flags.AccFinal, method2InBaseClass.flags());
    assertEquals("method2InBaseClass", method2InBaseClass.elementName());
    assertEquals(0, method2InBaseClass.parameters().stream().count());
    assertTrue(method2InBaseClass.requireReturnType().isVoid());
    assertEquals(JavaTypes._void, method2InBaseClass.requireReturnType().name());
    assertFalse(method2InBaseClass.isConstructor());
    assertEquals(0, method2InBaseClass.annotations().stream().count());
  }

  @Test
  public void testFindMethodInSuperHierarchy(IJavaEnvironment env) {
    var methodInBaseClass = env.requireType(ChildClass.class.getName()).methods().withSuperTypes(true).withAnnotation(MarkerAnnotation.class.getName()).first().orElseThrow();
    assertEquals("methodInBaseClass", methodInBaseClass.elementName());
  }

  @Test
  public void testConstructor(IJavaEnvironment env) {
    var secondConstr = env.requireType(ClassWithConstructors.class.getName()).methods().item(1).orElseThrow();
    assertFalse(secondConstr.returnType().isPresent());
    assertTrue(secondConstr.isConstructor());
    assertEquals("ClassWithConstructors(String other) throws IOException", secondConstr.sourceOfDeclaration().orElseThrow().asCharSequence().toString());

    var declarationType = env.requireType(ClassWithConstructors.class.getName()).requireCompilationUnit().types().first().orElseThrow();
    assertTrue(declarationType.unwrap() instanceof DeclarationTypeWithEcj);
    assertEquals(2, declarationType.methods().stream().count());

    var declarationMethod = declarationType.methods().first().orElseThrow();
    assertTrue(declarationMethod.isConstructor());
    assertFalse(declarationMethod.returnType().isPresent());
    assertEquals("ClassWithConstructors() throws IOException", declarationMethod.sourceOfDeclaration().orElseThrow().asCharSequence().toString());

    new CoreJavaEnvironmentBinaryOnlyFactory().accept(binEnv -> {
      binEnv.requireType(ClassWithConstructors.class.getName()).methods().item(1).orElseThrow();
      assertFalse(secondConstr.returnType().isPresent());
    });
  }

  @Test
  public void testGetMethod(IJavaEnvironment env) {
    assertTrue(env.requireType(ChildClass.class.getName()).requireSuperClass().methods().withName("method2InBaseClass").first().isPresent());
    assertFalse(env.requireType(ChildClass.class.getName()).requireSuperClass().methods().withName("method2InBaseclass").first().isPresent());
  }

  @Test
  public void testGetMethods(IJavaEnvironment env) {
    assertEquals(1, env.requireType(ChildClass.class.getName()).requireSuperClass().methods().withFlags(Flags.AccSynchronized).stream().count());
    assertEquals(0, env.requireType(ChildClass.class.getName()).requireSuperClass().methods().withFlags(Flags.AccPrivate).stream().count());
  }
}
