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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.CoreTestingUtils;
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
    Assert.assertEquals("values", testAnnotValues.getName());
    Assert.assertEquals(ExpressionValueType.Array, testAnnotValues.getValueType());
    Assert.assertEquals(childClassType.getAnnotations().get(0), testAnnotValues.getOwnerAnnotation());

    IAnnotationValue[] arr = (IAnnotationValue[]) testAnnotValues.getValue();
    Assert.assertEquals(2, arr.length);

    Assert.assertEquals("values", arr[0].getName());
    Assert.assertEquals(ExpressionValueType.Type, arr[0].getValueType());
    Assert.assertEquals(childClassType.getAnnotations().get(0), arr[0].getOwnerAnnotation());
    Assert.assertEquals(Serializable.class.getName(), ((IType) arr[0].getValue()).getName());

    Assert.assertEquals("values", arr[1].getName());
    Assert.assertEquals(ExpressionValueType.Type, arr[1].getValueType());
    Assert.assertEquals(childClassType.getAnnotations().get(0), arr[1].getOwnerAnnotation());
    Assert.assertEquals(Runnable.class.getName(), ((IType) arr[1].getValue()).getName());

    // methodInChildClass annotation values
    List<IAnnotationValue> methodInChildClassValues = childClassType.getMethods().get(1).getAnnotations().get(0).getValues().valueList();
    IAnnotationValue methodInChildClassValue1 = methodInChildClassValues.get(1);
    Assert.assertEquals("values", methodInChildClassValue1.getName());
    Assert.assertEquals(ExpressionValueType.Type, methodInChildClassValue1.getValueType());
    Assert.assertEquals(childClassType.getMethods().get(1).getAnnotations().get(0), methodInChildClassValue1.getOwnerAnnotation());
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), ((IType) methodInChildClassValue1.getValue()).getName());

    IAnnotationValue methodInChildClassValue2 = methodInChildClassValues.get(0);
    Assert.assertEquals("en", methodInChildClassValue2.getName());
    Assert.assertEquals(ExpressionValueType.String, methodInChildClassValue2.getValueType());
    Assert.assertEquals(childClassType.getMethods().get(1).getAnnotations().get(0), methodInChildClassValue2.getOwnerAnnotation());
    Assert.assertEquals("TestEnum.A", methodInChildClassValue2.getValue().toString());

    // firstCase annotation value
    IAnnotationValue suppressWarningValue = childClassType.getMethods().get(2).getAnnotations().get(0).getValue("value");
    Assert.assertEquals("value", suppressWarningValue.getName());
    Assert.assertEquals(ExpressionValueType.String, suppressWarningValue.getValueType());
    Assert.assertEquals(childClassType.getMethods().get(2).getAnnotations().get(0), suppressWarningValue.getOwnerAnnotation());
    Assert.assertEquals("unused", suppressWarningValue.getValue().toString());
  }

  @Test
  public void testToString() {
    IAnnotationValue methodInChildClassValue1 = CoreTestingUtils.getChildClassType().getMethods().get(1).getAnnotations().get(0).getValues().valueList().get(0);
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
    Assert.assertEquals("values", testAnnotValues.getName());
    Assert.assertEquals(ExpressionValueType.Array, testAnnotValues.getValueType());
    Assert.assertEquals(baseClassType.getAnnotations().get(0), testAnnotValues.getOwnerAnnotation());

    IAnnotationValue[] arr = (IAnnotationValue[]) testAnnotValues.getValue();
    Assert.assertEquals(2, arr.length);

    Assert.assertEquals("values", arr[0].getName());
    Assert.assertEquals(ExpressionValueType.Type, arr[0].getValueType());
    Assert.assertEquals(baseClassType.getAnnotations().get(0), arr[0].getOwnerAnnotation());
    Assert.assertEquals(Serializable.class.getName(), ((IType) arr[0].getValue()).getName());

    Assert.assertEquals("values", arr[1].getName());
    Assert.assertEquals(ExpressionValueType.Type, arr[1].getValueType());
    Assert.assertEquals(baseClassType.getAnnotations().get(0), arr[1].getOwnerAnnotation());
    Assert.assertEquals(Runnable.class.getName(), ((IType) arr[1].getValue()).getName());

    // methodInBaseClass annotation
    IAnnotatable methodInBaseClass = baseClassType.getMethods().get(2);
    testAnnotValues = methodInBaseClass.getAnnotations().get(0).getValue("values");
    Assert.assertEquals("values", testAnnotValues.getName());
    Assert.assertEquals(ExpressionValueType.Array, testAnnotValues.getValueType());
    Assert.assertEquals(methodInBaseClass.getAnnotations().get(0), testAnnotValues.getOwnerAnnotation());

    arr = (IAnnotationValue[]) testAnnotValues.getValue();
    Assert.assertEquals(2, arr.length);

    Assert.assertEquals("values", arr[0].getName());
    Assert.assertEquals(ExpressionValueType.Type, arr[0].getValueType());
    Assert.assertEquals(methodInBaseClass.getAnnotations().get(0), arr[0].getOwnerAnnotation());
    Assert.assertEquals(Serializable.class.getName(), ((IType) arr[0].getValue()).getName());

    Assert.assertEquals("values", arr[1].getName());
    Assert.assertEquals(ExpressionValueType.Type, arr[1].getValueType());
    Assert.assertEquals(methodInBaseClass.getAnnotations().get(0), arr[1].getOwnerAnnotation());
    Assert.assertEquals(Runnable.class.getName(), ((IType) arr[1].getValue()).getName());
  }
}
