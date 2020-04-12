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
public class MethodTest {

  @Test
  public void testChildClassMethods(IJavaEnvironment env) {
    IType childClassType = env.requireType(ChildClass.class.getName());
    assertEquals(3, childClassType.methods().stream().count());

    // constructor
    IMethod constr = childClassType.methods().first().get();
    assertEquals(childClassType, constr.declaringType());
    assertEquals(0, constr.exceptionTypes().count());
    assertEquals(Flags.AccPublic, constr.flags());
    assertEquals(childClassType.elementName(), constr.elementName());
    assertEquals(0, constr.parameters().stream().count());
    assertFalse(constr.returnType().isPresent());
    assertTrue(constr.isConstructor());
    assertEquals(0, constr.annotations().stream().count());
    assertFalse(constr.toWorkingCopy().returnType().isPresent());

    // methodInChildClass
    IMethod methodInChildClass = childClassType.methods().item(1).get();
    assertEquals(childClassType, methodInChildClass.declaringType());
    assertEquals(1, methodInChildClass.exceptionTypes().count());
    assertEquals(IOException.class.getName(), methodInChildClass.exceptionTypes().findAny().get().name());
    assertEquals(Flags.AccProtected | Flags.AccSynchronized, methodInChildClass.flags());
    assertEquals("methodInChildClass", methodInChildClass.elementName());
    assertEquals(2, methodInChildClass.parameters().stream().count());
    assertEquals(JavaTypes._boolean, methodInChildClass.requireReturnType().leafComponentType().get().name());
    assertTrue(methodInChildClass.requireReturnType().isArray());
    assertFalse(methodInChildClass.isConstructor());
    assertEquals(1, methodInChildClass.annotations().stream().count());

    // firstCase
    IMethod firstCase = childClassType.methods().item(2).get();
    assertEquals(childClassType, firstCase.declaringType());
    assertEquals(0, firstCase.exceptionTypes().count());
    assertEquals(Flags.AccPrivate, firstCase.flags());
    assertEquals("firstCase", firstCase.elementName());
    assertEquals(0, firstCase.parameters().stream().count());
    assertEquals(Set.class.getName(), firstCase.requireReturnType().leafComponentType().get().name());
    assertFalse(firstCase.isConstructor());
    assertEquals(1, firstCase.annotations().stream().count());
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    IType childClassType = env.requireType(ChildClass.class.getName());
    assertFalse(Strings.isBlank(childClassType.methods().item(1).get().toString()));
  }

  @Test
  public void testDefaultMethods(IJavaEnvironment env) {
    IType type = env.requireType(ClassWithDefaultMethods.class.getName());
    assertEquals(7, type.methods().withSuperInterfaces(true).stream().count());
    assertEquals(4, type.superInterfaces().findAny().get().methods().stream().count());
    IMethod defaultMethod = type.superInterfaces().findAny().get().methods().withName("defMethod2").first().get();
    assertTrue(isDefaultMethod(defaultMethod.flags()));
  }

  @Test
  public void testBaseClassMethods(IJavaEnvironment env) {
    IType baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();

    assertEquals(2, baseClassType.methods().stream().count());

    // methodInBaseClass
    IMethod methodInBaseClass = baseClassType.methods().first().get();
    assertEquals(baseClassType, methodInBaseClass.declaringType());
    assertEquals(2, methodInBaseClass.exceptionTypes().count());
    assertEquals(IOError.class.getName(), methodInBaseClass.exceptionTypes().findAny().get().name());
    assertEquals(FileNotFoundException.class.getName(), methodInBaseClass.exceptionTypes().skip(1).findAny().get().name());
    assertEquals(Flags.AccProtected, methodInBaseClass.flags());
    assertEquals("methodInBaseClass", methodInBaseClass.elementName());
    assertEquals(1, methodInBaseClass.parameters().stream().count());
    assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), methodInBaseClass.requireReturnType().leafComponentType().get().name());
    assertEquals(2, methodInBaseClass.requireReturnType().arrayDimension());
    assertTrue(methodInBaseClass.requireReturnType().isArray());
    assertFalse(methodInBaseClass.isConstructor());
    assertEquals(2, methodInBaseClass.annotations().stream().count());

    // method2InBaseClass
    IMethod method2InBaseClass = baseClassType.methods().item(1).get();
    assertEquals(baseClassType, method2InBaseClass.declaringType());
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
    IMethod methodInBaseClass = env.requireType(ChildClass.class.getName()).methods().withSuperTypes(true).withAnnotation(MarkerAnnotation.class.getName()).first().get();
    assertEquals("methodInBaseClass", methodInBaseClass.elementName());
  }

  @Test
  public void testConstructor(IJavaEnvironment env) {
    IMethod secondConstr = env.requireType(ClassWithConstructors.class.getName()).methods().item(1).get();
    assertFalse(secondConstr.returnType().isPresent());

    new CoreJavaEnvironmentBinaryOnlyFactory().accept(binEnv -> {
      binEnv.requireType(ClassWithConstructors.class.getName()).methods().item(1).get();
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
