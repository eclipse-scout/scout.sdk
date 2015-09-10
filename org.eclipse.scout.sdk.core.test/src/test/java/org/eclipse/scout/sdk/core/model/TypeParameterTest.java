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

    List<ITypeParameter> typeParameters = childClassType.getTypeParameters();
    Assert.assertEquals(1, typeParameters.size());

    ITypeParameter param = typeParameters.get(0);
    Assert.assertEquals("X", param.getElementName());
    Assert.assertEquals(childClassType, param.getDeclaringMember());

    List<IType> bounds = param.getBounds();
    Assert.assertEquals(3, bounds.size());
    Assert.assertEquals(AbstractList.class.getName(), bounds.get(0).getName());
    Assert.assertEquals(Runnable.class.getName(), bounds.get(1).getName());
    Assert.assertEquals(Serializable.class.getName(), bounds.get(2).getName());

    IType abstractListBound = bounds.get(0);
    Assert.assertEquals(String.class.getName(), abstractListBound.getTypeArguments().get(0).getName());
  }

  @Test
  public void testToString() {
    ITypeParameter childTypeParam = CoreTestingUtils.getChildClassType().getTypeParameters().get(0);
    Assert.assertFalse(StringUtils.isBlank(childTypeParam.toString()));

    ITypeParameter baseTypeParam = CoreTestingUtils.getBaseClassType().getTypeParameters().get(1);
    Assert.assertFalse(StringUtils.isBlank(baseTypeParam.toString()));
  }

  @Test
  public void testBaseClassTypeParams() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    List<ITypeParameter> typeParameters = baseClassType.getTypeParameters();
    Assert.assertEquals(2, typeParameters.size());

    ITypeParameter param = typeParameters.get(0);
    Assert.assertEquals("T", param.getElementName());
    Assert.assertEquals(baseClassType, param.getDeclaringMember());
    Assert.assertEquals(0, param.getBounds().size());

    param = typeParameters.get(1);
    Assert.assertEquals("Z", param.getElementName());
    Assert.assertEquals(baseClassType, param.getDeclaringMember());
    Assert.assertEquals(0, param.getBounds().size());
  }

}
