/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class MethodParameterTest {

  @Test
  public void testDeclaringMethodParameters(IJavaEnvironment env) {
    IType childClassType = env.requireType(ChildClass.class.getName());
    assertEquals(3, childClassType.methods().stream().count());
    IMethod method = childClassType.methods().item(1).get();
    List<IMethodParameter> methodInChildClassParams = method.parameters().stream().collect(toList());
    assertEquals(2, methodInChildClassParams.size());

    IMethodParameter firstParam = methodInChildClassParams.get(0);
    assertEquals("firstParam", firstParam.elementName());
    assertEquals(Flags.AccFinal, firstParam.flags());
    assertEquals(method, firstParam.declaringMethod());
    assertEquals(String.class.getName(), firstParam.dataType().name());

    IMethodParameter secondParam = methodInChildClassParams.get(1);
    assertEquals("secondParam", secondParam.elementName());
    assertEquals(Flags.AccFinal, secondParam.flags());
    assertEquals(method, secondParam.declaringMethod());
    assertEquals(List.class.getName(), secondParam.dataType().name());
    assertEquals("final List<Runnable> secondParam", secondParam.toWorkingCopy().toJavaSource().toString());
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    IType childClassType = env.requireType(ChildClass.class.getName());
    assertFalse(Strings.isBlank(childClassType.methods().item(1).get().parameters().item(1).get().toString()));

    IType baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    assertFalse(Strings.isBlank(baseClassType.methods().first().get().parameters().first().toString()));
  }

  @Test
  public void testBindingMethodParameters(IJavaEnvironment env) {
    IType baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    assertEquals(2, baseClassType.methods().stream().count());
    IMethod method = baseClassType.methods().first().get();
    List<IMethodParameter> methodInBaseClassParams = method.parameters().stream().collect(toList());
    assertEquals(1, methodInBaseClassParams.size());

    IMethodParameter runnableParam = methodInBaseClassParams.get(0);
    assertEquals("runnableParam", runnableParam.elementName());
    assertEquals(Flags.AccFinal, runnableParam.flags());
    assertEquals(method, runnableParam.declaringMethod());
    assertEquals(JavaTypes.Double, runnableParam.dataType().leafComponentType().get().name());
    assertTrue(runnableParam.dataType().isArray());
    assertEquals(1, runnableParam.dataType().arrayDimension());
  }
}
