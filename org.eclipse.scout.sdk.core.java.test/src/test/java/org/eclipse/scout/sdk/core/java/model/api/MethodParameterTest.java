/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.fixture.ChildClass;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class MethodParameterTest {

  @Test
  public void testDeclaringMethodParameters(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    assertEquals(3, childClassType.methods().stream().count());
    var method = childClassType.methods().item(1).orElseThrow();
    var methodInChildClassParams = method.parameters().stream().toList();
    assertEquals(2, methodInChildClassParams.size());

    var firstParam = methodInChildClassParams.get(0);
    assertEquals("firstParam", firstParam.elementName());
    assertEquals(Flags.AccFinal, firstParam.flags());
    assertEquals(method, firstParam.declaringMethod());
    assertEquals(String.class.getName(), firstParam.dataType().name());

    var secondParam = methodInChildClassParams.get(1);
    assertEquals("secondParam", secondParam.elementName());
    assertEquals(Flags.AccFinal, secondParam.flags());
    assertEquals(method, secondParam.declaringMethod());
    assertEquals(List.class.getName(), secondParam.dataType().name());
    assertEquals("final List<Runnable> secondParam", secondParam.toWorkingCopy().toJavaSource().toString());
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    assertFalse(Strings.isBlank(childClassType.methods().item(1).orElseThrow().parameters().item(1).orElseThrow().toString()));

    var baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    assertFalse(Strings.isBlank(baseClassType.methods().first().orElseThrow().parameters().first().toString()));
  }

  @Test
  public void testBindingMethodParameters(IJavaEnvironment env) {
    var baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    assertEquals(2, baseClassType.methods().stream().count());
    var method = baseClassType.methods().first().orElseThrow();
    var methodInBaseClassParams = method.parameters().stream().toList();
    assertEquals(1, methodInBaseClassParams.size());

    var runnableParam = methodInBaseClassParams.get(0);
    assertEquals("runnableParam", runnableParam.elementName());
    assertEquals(Flags.AccFinal, runnableParam.flags());
    assertEquals(method, runnableParam.declaringMethod());
    assertEquals(JavaTypes.Double, runnableParam.dataType().leafComponentType().orElseThrow().name());
    assertTrue(runnableParam.dataType().isArray());
    assertEquals(1, runnableParam.dataType().arrayDimension());
  }
}
