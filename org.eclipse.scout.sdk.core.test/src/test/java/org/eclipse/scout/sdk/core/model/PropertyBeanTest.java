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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.fixture.PropertyTestClass;
import org.eclipse.scout.sdk.core.model.api.IPropertyBean;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class PropertyBeanTest {
  @Test
  public void testPropertyBean() {
    IType propTestClass = CoreTestingUtils.createJavaEnvironment().findType(PropertyTestClass.class.getName());
    Assert.assertNotNull(propTestClass);

    List<IPropertyBean> propertyBeans = new ArrayList<>(CoreUtils.getPropertyBeans(propTestClass, null, new Comparator<IPropertyBean>() {
      @Override
      public int compare(IPropertyBean o1, IPropertyBean o2) {
        return o1.name().compareTo(o2.name());
      }
    }));
    Assert.assertEquals(5, propertyBeans.size());

    IPropertyBean aloneProp = propertyBeans.get(0);
    Assert.assertEquals("Alone", aloneProp.name());
    Assert.assertEquals(String.class.getName(), aloneProp.type().name());
    Assert.assertEquals(propTestClass, aloneProp.declaringType());
    Assert.assertNull(aloneProp.readMethod());
    Assert.assertNotNull(aloneProp.writeMethod());

    IPropertyBean falseProp = propertyBeans.get(1);
    Assert.assertEquals("False", falseProp.name());
    Assert.assertEquals(IJavaRuntimeTypes.Boolean, falseProp.type().name());
    Assert.assertEquals(propTestClass, falseProp.declaringType());
    Assert.assertNotNull(falseProp.readMethod());
    Assert.assertNotNull(falseProp.writeMethod());

    IPropertyBean onlyProp = propertyBeans.get(2);
    Assert.assertEquals("Only", onlyProp.name());
    Assert.assertEquals(IJavaRuntimeTypes.Integer, onlyProp.type().name());
    Assert.assertEquals(propTestClass, onlyProp.declaringType());
    Assert.assertNotNull(onlyProp.readMethod());
    Assert.assertNull(onlyProp.writeMethod());

    IPropertyBean stringProp = propertyBeans.get(3);
    Assert.assertEquals("String", stringProp.name());
    Assert.assertEquals(String.class.getName(), stringProp.type().name());
    Assert.assertEquals(propTestClass, stringProp.declaringType());
    Assert.assertNotNull(stringProp.readMethod());
    Assert.assertNotNull(stringProp.writeMethod());

    IPropertyBean trueProp = propertyBeans.get(4);
    Assert.assertEquals("True", trueProp.name());
    Assert.assertEquals("boolean", trueProp.type().name());
    Assert.assertEquals(propTestClass, trueProp.declaringType());
    Assert.assertNotNull(trueProp.readMethod());
    Assert.assertNotNull(trueProp.writeMethod());
  }
}
