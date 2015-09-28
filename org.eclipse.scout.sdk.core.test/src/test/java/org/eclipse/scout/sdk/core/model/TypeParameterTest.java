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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class TypeParameterTest {
  @Test
  public void testChildClassTypeParams() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    List<ITypeParameter> typeParameters = childClassType.typeParameters();
    Assert.assertEquals(1, typeParameters.size());

    ITypeParameter param = typeParameters.get(0);
    Assert.assertEquals("X", param.elementName());
    Assert.assertEquals(childClassType, param.declaringMember());

    List<IType> bounds = param.bounds();
    Assert.assertEquals(3, bounds.size());
    Assert.assertEquals(AbstractList.class.getName(), bounds.get(0).name());
    Assert.assertEquals(Runnable.class.getName(), bounds.get(1).name());
    Assert.assertEquals(Serializable.class.getName(), bounds.get(2).name());

    IType abstractListBound = bounds.get(0);
    Assert.assertEquals(String.class.getName(), abstractListBound.typeArguments().get(0).name());
  }

  @Test
  public void testToString() {
    ITypeParameter childTypeParam = CoreTestingUtils.getChildClassType().typeParameters().get(0);
    Assert.assertFalse(StringUtils.isBlank(childTypeParam.toString()));

    ITypeParameter baseTypeParam = CoreTestingUtils.getBaseClassType().typeParameters().get(1);
    Assert.assertFalse(StringUtils.isBlank(baseTypeParam.toString()));
  }

  @Test
  public void testBaseClassTypeParams() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    List<ITypeParameter> typeParameters = baseClassType.typeParameters();
    Assert.assertEquals(2, typeParameters.size());

    ITypeParameter param = typeParameters.get(0);
    Assert.assertEquals("T", param.elementName());
    Assert.assertEquals(baseClassType, param.declaringMember());
    Assert.assertEquals(0, param.bounds().size());

    param = typeParameters.get(1);
    Assert.assertEquals("Z", param.elementName());
    Assert.assertEquals(baseClassType, param.declaringMember());
    Assert.assertEquals(0, param.bounds().size());
  }

}
