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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class AnnotationValueTest {
  @Test
  public void testChildClassAnnotationValues() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    // ChildClass Annotation
    IAnnotationValue testAnnotValues = childClassType.getAnnotations().get(0).getValue("values");
    Assert.assertEquals("values", testAnnotValues.getElementName());
    Assert.assertEquals(MetaValueType.Array, testAnnotValues.getMetaValue().getType());
    Assert.assertEquals(childClassType.getAnnotations().get(0), testAnnotValues.getDeclaringAnnotation());

    IMetaValue[] arr = ((IArrayMetaValue) testAnnotValues.getMetaValue()).getMetaValueArray();
    Assert.assertEquals(2, arr.length);

    Assert.assertEquals(MetaValueType.Type, arr[0].getType());
    Assert.assertEquals(Serializable.class.getName(), arr[0].getObject(IType.class).getName());

    Assert.assertEquals(MetaValueType.Type, arr[1].getType());
    Assert.assertEquals(Runnable.class.getName(), arr[1].getObject(IType.class).getName());

    // methodInChildClass annotation values
    Map<String, IAnnotationValue> methodInChildClassValueMap = new HashMap<>(childClassType.getMethods().get(1).getAnnotations().get(0).getValues());
    IAnnotationValue methodInChildClassValue1 = methodInChildClassValueMap.get("values");
    Assert.assertEquals("values", methodInChildClassValue1.getElementName());
    Assert.assertEquals(MetaValueType.Type, methodInChildClassValue1.getMetaValue().getType());
    Assert.assertEquals(childClassType.getMethods().get(1).getAnnotations().get(0), methodInChildClassValue1.getDeclaringAnnotation());
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), methodInChildClassValue1.getMetaValue().getObject(IType.class).getName());

    IAnnotationValue methodInChildClassValue2 = methodInChildClassValueMap.get("en");
    Assert.assertEquals("en", methodInChildClassValue2.getElementName());
    Assert.assertEquals(MetaValueType.Enum, methodInChildClassValue2.getMetaValue().getType());
    Assert.assertEquals(childClassType.getMethods().get(1).getAnnotations().get(0), methodInChildClassValue2.getDeclaringAnnotation());
    Assert.assertEquals("A", methodInChildClassValue2.getMetaValue().getObject(IField.class).getElementName());

    // firstCase annotation value
    IAnnotationValue suppressWarningValue = childClassType.getMethods().get(2).getAnnotations().get(0).getValue("value");
    Assert.assertEquals("value", suppressWarningValue.getElementName());
    Assert.assertEquals(MetaValueType.String, suppressWarningValue.getMetaValue().getType());
    Assert.assertEquals(childClassType.getMethods().get(2).getAnnotations().get(0), suppressWarningValue.getDeclaringAnnotation());
    Assert.assertEquals("unused", suppressWarningValue.getMetaValue().getObject(String.class));
  }

  @Test
  public void testToString() {
    List<IAnnotationValue> values = new ArrayList<>(CoreTestingUtils.getChildClassType().getMethods().get(1).getAnnotations().get(0).getValues().values());
    IAnnotationValue methodInChildClassValue1 = values.get(0);
    Assert.assertFalse(StringUtils.isBlank(methodInChildClassValue1.toString()));

    IAnnotationValue testAnnotValues = CoreTestingUtils.getBaseClassType().getAnnotations().get(0).getValue("values");
    Assert.assertFalse(StringUtils.isBlank(testAnnotValues.toString()));
  }

  @Test
  public void testBaseClassAnnotationValues() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    // BaseClass annotation
    IAnnotationValue testAnnotValues = baseClassType.getAnnotations().get(0).getValue("values");
    Assert.assertEquals("values", testAnnotValues.getElementName());
    Assert.assertEquals(MetaValueType.Array, testAnnotValues.getMetaValue().getType());
    Assert.assertEquals(baseClassType.getAnnotations().get(0), testAnnotValues.getDeclaringAnnotation());

    IMetaValue[] arr = ((IArrayMetaValue) testAnnotValues.getMetaValue()).getMetaValueArray();
    Assert.assertEquals(2, arr.length);

    Assert.assertEquals(MetaValueType.Type, arr[0].getType());
    Assert.assertEquals(Serializable.class.getName(), arr[0].getObject(IType.class).getName());

    Assert.assertEquals(MetaValueType.Type, arr[1].getType());
    Assert.assertEquals(Runnable.class.getName(), arr[1].getObject(IType.class).getName());

    // methodInBaseClass annotation
    IAnnotatable methodInBaseClass = baseClassType.getMethods().get(0);
    testAnnotValues = methodInBaseClass.getAnnotations().get(0).getValue("values");
    Assert.assertEquals("values", testAnnotValues.getElementName());
    Assert.assertEquals(MetaValueType.Array, testAnnotValues.getMetaValue().getType());
    Assert.assertEquals(methodInBaseClass.getAnnotations().get(0), testAnnotValues.getDeclaringAnnotation());

    arr = ((IArrayMetaValue) testAnnotValues.getMetaValue()).getMetaValueArray();
    Assert.assertEquals(2, arr.length);

    Assert.assertEquals(MetaValueType.Type, arr[0].getType());
    Assert.assertEquals(Serializable.class.getName(), arr[0].getObject(IType.class).getName());

    Assert.assertEquals(MetaValueType.Type, arr[1].getType());
    Assert.assertEquals(Runnable.class.getName(), arr[1].getObject(IType.class).getName());
  }
}
