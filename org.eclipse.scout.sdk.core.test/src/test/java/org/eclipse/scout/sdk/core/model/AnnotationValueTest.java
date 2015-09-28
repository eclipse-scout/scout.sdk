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
    IAnnotationValue testAnnotValues = childClassType.annotations().first().value("values");
    Assert.assertEquals("values", testAnnotValues.elementName());
    Assert.assertEquals(MetaValueType.Array, testAnnotValues.metaValue().type());
    Assert.assertEquals(childClassType.annotations().first(), testAnnotValues.declaringAnnotation());

    IMetaValue[] arr = ((IArrayMetaValue) testAnnotValues.metaValue()).metaValueArray();
    Assert.assertEquals(2, arr.length);

    Assert.assertEquals(MetaValueType.Type, arr[0].type());
    Assert.assertEquals(Serializable.class.getName(), arr[0].get(IType.class).name());

    Assert.assertEquals(MetaValueType.Type, arr[1].type());
    Assert.assertEquals(Runnable.class.getName(), arr[1].get(IType.class).name());

    // methodInChildClass annotation values
    Map<String, IAnnotationValue> methodInChildClassValueMap = new HashMap<>(childClassType.methods().list().get(1).annotations().first().values());
    IAnnotationValue methodInChildClassValue1 = methodInChildClassValueMap.get("values");
    Assert.assertEquals("values", methodInChildClassValue1.elementName());
    Assert.assertEquals(MetaValueType.Type, methodInChildClassValue1.metaValue().type());
    Assert.assertEquals(childClassType.methods().list().get(1).annotations().first(), methodInChildClassValue1.declaringAnnotation());
    Assert.assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), methodInChildClassValue1.metaValue().get(IType.class).name());

    IAnnotationValue methodInChildClassValue2 = methodInChildClassValueMap.get("en");
    Assert.assertEquals("en", methodInChildClassValue2.elementName());
    Assert.assertEquals(MetaValueType.Enum, methodInChildClassValue2.metaValue().type());
    Assert.assertEquals(childClassType.methods().list().get(1).annotations().first(), methodInChildClassValue2.declaringAnnotation());
    Assert.assertEquals("A", methodInChildClassValue2.metaValue().get(IField.class).elementName());

    // firstCase annotation value
    IAnnotationValue suppressWarningValue = childClassType.methods().list().get(2).annotations().first().value("value");
    Assert.assertEquals("value", suppressWarningValue.elementName());
    Assert.assertEquals(MetaValueType.String, suppressWarningValue.metaValue().type());
    Assert.assertEquals(childClassType.methods().list().get(2).annotations().first(), suppressWarningValue.declaringAnnotation());
    Assert.assertEquals("unused", suppressWarningValue.metaValue().get(String.class));
  }

  @Test
  public void testToString() {
    List<IAnnotationValue> values = new ArrayList<>(CoreTestingUtils.getChildClassType().methods().list().get(1).annotations().first().values().values());
    IAnnotationValue methodInChildClassValue1 = values.get(0);
    Assert.assertFalse(StringUtils.isBlank(methodInChildClassValue1.toString()));

    IAnnotationValue testAnnotValues = CoreTestingUtils.getBaseClassType().annotations().first().value("values");
    Assert.assertFalse(StringUtils.isBlank(testAnnotValues.toString()));
  }

  @Test
  public void testBaseClassAnnotationValues() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    // BaseClass annotation
    IAnnotationValue testAnnotValues = baseClassType.annotations().first().value("values");
    Assert.assertEquals("values", testAnnotValues.elementName());
    Assert.assertEquals(MetaValueType.Array, testAnnotValues.metaValue().type());
    Assert.assertEquals(baseClassType.annotations().first(), testAnnotValues.declaringAnnotation());

    IMetaValue[] arr = ((IArrayMetaValue) testAnnotValues.metaValue()).metaValueArray();
    Assert.assertEquals(2, arr.length);

    Assert.assertEquals(MetaValueType.Type, arr[0].type());
    Assert.assertEquals(Serializable.class.getName(), arr[0].get(IType.class).name());

    Assert.assertEquals(MetaValueType.Type, arr[1].type());
    Assert.assertEquals(Runnable.class.getName(), arr[1].get(IType.class).name());

    // methodInBaseClass annotation
    IAnnotatable methodInBaseClass = baseClassType.methods().list().get(0);
    testAnnotValues = methodInBaseClass.annotations().first().value("values");
    Assert.assertEquals("values", testAnnotValues.elementName());
    Assert.assertEquals(MetaValueType.Array, testAnnotValues.metaValue().type());
    Assert.assertEquals(methodInBaseClass.annotations().first(), testAnnotValues.declaringAnnotation());

    arr = ((IArrayMetaValue) testAnnotValues.metaValue()).metaValueArray();
    Assert.assertEquals(2, arr.length);

    Assert.assertEquals(MetaValueType.Type, arr[0].type());
    Assert.assertEquals(Serializable.class.getName(), arr[0].get(IType.class).name());

    Assert.assertEquals(MetaValueType.Type, arr[1].type());
    Assert.assertEquals(Runnable.class.getName(), arr[1].get(IType.class).name());
  }
}
