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

import java.io.Serializable;
import java.util.AbstractList;

import org.eclipse.scout.sdk.core.java.fixture.ChildClass;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class TypeParameterTest {
  @Test
  public void testChildClassTypeParams(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    var typeParameters = childClassType.typeParameters().toList();
    assertEquals(1, typeParameters.size());

    var param = typeParameters.get(0);
    assertEquals("X", param.elementName());
    assertEquals(childClassType, param.declaringMember());

    var bounds = param.bounds().toList();
    assertEquals(3, bounds.size());
    assertEquals(AbstractList.class.getName(), bounds.get(0).name());
    assertEquals(Runnable.class.getName(), bounds.get(1).name());
    assertEquals(Serializable.class.getName(), bounds.get(2).name());
    var expectedParamSrc = "X extends AbstractList<String> & Runnable & Serializable";
    assertEquals(expectedParamSrc, param.toWorkingCopy().toJavaSource().toString());
    assertEquals(expectedParamSrc, param.source().orElseThrow().asCharSequence().toString());

    var abstractListBound = bounds.get(0);
    assertEquals(String.class.getName(), abstractListBound.typeArguments().findAny().orElseThrow().name());

    new CoreJavaEnvironmentBinaryOnlyFactory().accept(binEnv -> {
      var paramBin = binEnv.requireType(ChildClass.class.getName()).typeParameters().findAny().orElseThrow();
      assertEquals(3, paramBin.bounds().count());
      binEnv.reload();
      assertEquals(3, paramBin.bounds().count());
    });
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    var childTypeParam = env.requireType(ChildClass.class.getName()).typeParameters().findAny().orElseThrow();
    assertFalse(Strings.isBlank(childTypeParam.toString()));

    var baseTypeParam = env.requireType(ChildClass.class.getName()).requireSuperClass().typeParameters().skip(1).findAny().orElseThrow();
    assertFalse(Strings.isBlank(baseTypeParam.toString()));
  }

  @Test
  public void testBaseClassTypeParams(IJavaEnvironment env) {
    var baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    var typeParameters = baseClassType.typeParameters().toList();
    assertEquals(2, typeParameters.size());

    var param = typeParameters.get(0);
    assertEquals("T", param.elementName());
    assertEquals(baseClassType, param.declaringMember());
    assertEquals(0, param.bounds().count());

    param = typeParameters.get(1);
    assertEquals("Z", param.elementName());
    assertEquals(baseClassType, param.declaringMember());
    assertEquals(0, param.bounds().count());
  }

}
