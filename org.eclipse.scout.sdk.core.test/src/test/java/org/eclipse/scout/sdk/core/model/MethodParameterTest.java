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
package org.eclipse.scout.sdk.core.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class MethodParameterTest {

  @Test
  public void testDeclaringMethodParameters() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);
    Assert.assertEquals(3, childClassType.getMethods().size());
    IMethod method = childClassType.getMethods().get(1);
    List<IMethodParameter> methodInChildClassParams = method.getParameters();
    Assert.assertEquals(2, methodInChildClassParams.size());

    IMethodParameter firstParam = methodInChildClassParams.get(0);
    Assert.assertEquals("firstParam", firstParam.getName());
    Assert.assertEquals(Flags.AccFinal, firstParam.getFlags());
    Assert.assertEquals(method, firstParam.getOwnerMethod());
    Assert.assertEquals(String.class.getName(), firstParam.getType().getName());

    IMethodParameter secondParam = methodInChildClassParams.get(1);
    Assert.assertEquals("secondParam", secondParam.getName());
    Assert.assertEquals(Flags.AccFinal, secondParam.getFlags());
    Assert.assertEquals(method, secondParam.getOwnerMethod());
    Assert.assertEquals(List.class.getName(), secondParam.getType().getName());
  }

  @Test
  public void testToString() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);
    Assert.assertFalse(StringUtils.isBlank(childClassType.getMethods().get(1).getParameters().get(1).toString()));

    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);
    Assert.assertFalse(StringUtils.isBlank(baseClassType.getMethods().get(2).getParameters().get(0).toString()));
  }

  @Test
  public void testBindingMethodParameters() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);
    Assert.assertEquals(3, baseClassType.getMethods().size());
    IMethod method = baseClassType.getMethods().get(2);
    List<IMethodParameter> methodInBaseClassParams = method.getParameters();
    Assert.assertEquals(1, methodInBaseClassParams.size());

    IMethodParameter runnableParam = methodInBaseClassParams.get(0);
    Assert.assertEquals("arg0", runnableParam.getName()); // parameter names not supported for binary types
    Assert.assertEquals(Flags.AccDefault, runnableParam.getFlags()); // final not supported for binary types
    Assert.assertEquals(method, runnableParam.getOwnerMethod());
    Assert.assertEquals(Double.class.getName(), runnableParam.getType().getName());
    Assert.assertTrue(runnableParam.getType().isArray());
    Assert.assertEquals(1, runnableParam.getType().getArrayDimension());
  }
}
