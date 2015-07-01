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

import org.eclipse.scout.sdk.core.fixture.PropertyTestClass;
import org.eclipse.scout.sdk.core.testing.TestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class PropertyBeanTest {
  @Test
  public void testPropertyBean() {
    IType propTestClass = TestingUtils.getType(PropertyTestClass.class.getName());
    Assert.assertNotNull(propTestClass);

    List<IPropertyBean> propertyBeans = new ArrayList<>(CoreUtils.getPropertyBeans(propTestClass, null, new Comparator<IPropertyBean>() {
      @Override
      public int compare(IPropertyBean o1, IPropertyBean o2) {
        return o1.getBeanName().compareTo(o2.getBeanName());
      }
    }));
    Assert.assertEquals(5, propertyBeans.size());

    IPropertyBean aloneProp = propertyBeans.get(0);
    Assert.assertEquals("Alone", aloneProp.getBeanName());
    Assert.assertEquals(String.class.getName(), aloneProp.getBeanType().getName());
    Assert.assertEquals(propTestClass, aloneProp.getDeclaringType());
    Assert.assertNull(aloneProp.getReadMethod());
    Assert.assertNotNull(aloneProp.getWriteMethod());

    IPropertyBean falseProp = propertyBeans.get(1);
    Assert.assertEquals("False", falseProp.getBeanName());
    Assert.assertEquals(Boolean.class.getName(), falseProp.getBeanType().getName());
    Assert.assertEquals(propTestClass, falseProp.getDeclaringType());
    Assert.assertNotNull(falseProp.getReadMethod());
    Assert.assertNotNull(falseProp.getWriteMethod());

    IPropertyBean onlyProp = propertyBeans.get(2);
    Assert.assertEquals("Only", onlyProp.getBeanName());
    Assert.assertEquals(Integer.class.getName(), onlyProp.getBeanType().getName());
    Assert.assertEquals(propTestClass, onlyProp.getDeclaringType());
    Assert.assertNotNull(onlyProp.getReadMethod());
    Assert.assertNull(onlyProp.getWriteMethod());

    IPropertyBean stringProp = propertyBeans.get(3);
    Assert.assertEquals("String", stringProp.getBeanName());
    Assert.assertEquals(String.class.getName(), stringProp.getBeanType().getName());
    Assert.assertEquals(propTestClass, stringProp.getDeclaringType());
    Assert.assertNotNull(stringProp.getReadMethod());
    Assert.assertNotNull(stringProp.getWriteMethod());

    IPropertyBean trueProp = propertyBeans.get(4);
    Assert.assertEquals("True", trueProp.getBeanName());
    Assert.assertEquals("boolean", trueProp.getBeanType().getName());
    Assert.assertEquals(propTestClass, trueProp.getDeclaringType());
    Assert.assertNotNull(trueProp.getReadMethod());
    Assert.assertNotNull(trueProp.getWriteMethod());
  }
}
