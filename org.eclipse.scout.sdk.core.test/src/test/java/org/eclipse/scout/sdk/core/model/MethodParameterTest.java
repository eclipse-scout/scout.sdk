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
import org.eclipse.scout.sdk.core.TypeNames;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
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
    Assert.assertEquals("firstParam", firstParam.getElementName());
    Assert.assertEquals(Flags.AccFinal, firstParam.getFlags());
    Assert.assertEquals(method, firstParam.getDeclaringMethod());
    Assert.assertEquals(String.class.getName(), firstParam.getDataType().getName());

    IMethodParameter secondParam = methodInChildClassParams.get(1);
    Assert.assertEquals("secondParam", secondParam.getElementName());
    Assert.assertEquals(Flags.AccFinal, secondParam.getFlags());
    Assert.assertEquals(method, secondParam.getDeclaringMethod());
    Assert.assertEquals(List.class.getName(), secondParam.getDataType().getName());
  }

  @Test
  public void testToString() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);
    Assert.assertFalse(StringUtils.isBlank(childClassType.getMethods().get(1).getParameters().get(1).toString()));

    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);
    Assert.assertFalse(StringUtils.isBlank(baseClassType.getMethods().get(0).getParameters().get(0).toString()));
  }

  @Test
  public void testBindingMethodParameters() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);
    Assert.assertEquals(2, baseClassType.getMethods().size());
    IMethod method = baseClassType.getMethods().get(0);
    List<IMethodParameter> methodInBaseClassParams = method.getParameters();
    Assert.assertEquals(1, methodInBaseClassParams.size());

    IMethodParameter runnableParam = methodInBaseClassParams.get(0);
    Assert.assertEquals("arg0", runnableParam.getElementName()); // parameter names not supported for binary types
    Assert.assertEquals(Flags.AccDefault, runnableParam.getFlags()); // final not supported for binary types
    Assert.assertEquals(method, runnableParam.getDeclaringMethod());
    Assert.assertEquals(TypeNames.java_lang_Double, runnableParam.getDataType().getLeafComponentType().getName());
    Assert.assertTrue(runnableParam.getDataType().isArray());
    Assert.assertEquals(1, runnableParam.getDataType().getArrayDimension());
  }
}
