/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.api;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentBinaryOnlyFactory;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class TypeParameterTest {
  @Test
  public void testChildClassTypeParams(IJavaEnvironment env) {
    IType childClassType = env.requireType(ChildClass.class.getName());
    List<ITypeParameter> typeParameters = childClassType.typeParameters().collect(toList());
    assertEquals(1, typeParameters.size());

    ITypeParameter param = typeParameters.get(0);
    assertEquals("X", param.elementName());
    assertEquals(childClassType, param.declaringMember());

    List<IType> bounds = param.bounds().collect(toList());
    assertEquals(3, bounds.size());
    assertEquals(AbstractList.class.getName(), bounds.get(0).name());
    assertEquals(Runnable.class.getName(), bounds.get(1).name());
    assertEquals(Serializable.class.getName(), bounds.get(2).name());
    String expectedParmSrc = "X extends AbstractList<String> & Runnable & Serializable";
    assertEquals(expectedParmSrc, param.toWorkingCopy().toJavaSource().toString());
    assertEquals(expectedParmSrc, param.source().get().asCharSequence().toString());

    IType abstractListBound = bounds.get(0);
    assertEquals(String.class.getName(), abstractListBound.typeArguments().findAny().get().name());

    new CoreJavaEnvironmentBinaryOnlyFactory().accept(binEnv -> {
      ITypeParameter paramBin = binEnv.requireType(ChildClass.class.getName()).typeParameters().findAny().get();
      assertEquals(3, paramBin.bounds().count());
      binEnv.reload();
      assertEquals(3, paramBin.bounds().count());
    });
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    ITypeParameter childTypeParam = env.requireType(ChildClass.class.getName()).typeParameters().findAny().get();
    assertFalse(Strings.isBlank(childTypeParam.toString()));

    ITypeParameter baseTypeParam = env.requireType(ChildClass.class.getName()).requireSuperClass().typeParameters().skip(1).findAny().get();
    assertFalse(Strings.isBlank(baseTypeParam.toString()));
  }

  @Test
  public void testBaseClassTypeParams(IJavaEnvironment env) {
    IType baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    List<ITypeParameter> typeParameters = baseClassType.typeParameters().collect(toList());
    assertEquals(2, typeParameters.size());

    ITypeParameter param = typeParameters.get(0);
    assertEquals("T", param.elementName());
    assertEquals(baseClassType, param.declaringMember());
    assertEquals(0, param.bounds().count());

    param = typeParameters.get(1);
    assertEquals("Z", param.elementName());
    assertEquals(baseClassType, param.declaringMember());
    assertEquals(0, param.bounds().count());
  }

}
