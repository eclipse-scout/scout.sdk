/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.Serializable;
import java.util.AbstractList;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class TypeParameterTest {
  @Test
  public void testChildClassTypeParams(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    var typeParameters = childClassType.typeParameters().collect(toList());
    assertEquals(1, typeParameters.size());

    var param = typeParameters.get(0);
    assertEquals("X", param.elementName());
    assertEquals(childClassType, param.declaringMember());

    var bounds = param.bounds().collect(toList());
    assertEquals(3, bounds.size());
    assertEquals(AbstractList.class.getName(), bounds.get(0).name());
    assertEquals(Runnable.class.getName(), bounds.get(1).name());
    assertEquals(Serializable.class.getName(), bounds.get(2).name());
    var expectedParmSrc = "X extends AbstractList<String> & Runnable & Serializable";
    assertEquals(expectedParmSrc, param.toWorkingCopy().toJavaSource().toString());
    assertEquals(expectedParmSrc, param.source().orElseThrow().asCharSequence().toString());

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
    var typeParameters = baseClassType.typeParameters().collect(toList());
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
