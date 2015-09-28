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
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
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
    Assert.assertEquals(3, childClassType.methods().list().size());
    IMethod method = childClassType.methods().list().get(1);
    List<IMethodParameter> methodInChildClassParams = method.parameters().list();
    Assert.assertEquals(2, methodInChildClassParams.size());

    IMethodParameter firstParam = methodInChildClassParams.get(0);
    Assert.assertEquals("firstParam", firstParam.elementName());
    Assert.assertEquals(Flags.AccFinal, firstParam.flags());
    Assert.assertEquals(method, firstParam.declaringMethod());
    Assert.assertEquals(String.class.getName(), firstParam.dataType().name());

    IMethodParameter secondParam = methodInChildClassParams.get(1);
    Assert.assertEquals("secondParam", secondParam.elementName());
    Assert.assertEquals(Flags.AccFinal, secondParam.flags());
    Assert.assertEquals(method, secondParam.declaringMethod());
    Assert.assertEquals(List.class.getName(), secondParam.dataType().name());
  }

  @Test
  public void testToString() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);
    Assert.assertFalse(StringUtils.isBlank(childClassType.methods().list().get(1).parameters().list().get(1).toString()));

    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);
    Assert.assertFalse(StringUtils.isBlank(baseClassType.methods().first().parameters().first().toString()));
  }

  @Test
  public void testBindingMethodParameters() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);
    Assert.assertEquals(2, baseClassType.methods().list().size());
    IMethod method = baseClassType.methods().first();
    List<IMethodParameter> methodInBaseClassParams = method.parameters().list();
    Assert.assertEquals(1, methodInBaseClassParams.size());

    IMethodParameter runnableParam = methodInBaseClassParams.get(0);
    Assert.assertEquals("arg0", runnableParam.elementName()); // parameter names not supported for binary types
    Assert.assertEquals(Flags.AccDefault, runnableParam.flags()); // final not supported for binary types
    Assert.assertEquals(method, runnableParam.declaringMethod());
    Assert.assertEquals(IJavaRuntimeTypes.java_lang_Double, runnableParam.dataType().leafComponentType().name());
    Assert.assertTrue(runnableParam.dataType().isArray());
    Assert.assertEquals(1, runnableParam.dataType().arrayDimension());
  }
}
