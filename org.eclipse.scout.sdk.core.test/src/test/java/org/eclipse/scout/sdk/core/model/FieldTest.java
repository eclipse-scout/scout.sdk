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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.CoreTestingUtils;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class FieldTest {
  @Test
  public void testStringConstantField() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    IField myStringField = childClassType.getFields().get(0);
    Assert.assertNotNull(myStringField);

    Assert.assertEquals("myStringValue", myStringField.getValue());
    Assert.assertEquals(String.class.getName(), myStringField.getDataType().getName());
    Assert.assertEquals(childClassType, myStringField.getDeclaringType());
    Assert.assertEquals(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal, myStringField.getFlags());
    Assert.assertEquals("myString", myStringField.getName());
  }

  @Test
  public void testToString() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    IField myStringField = childClassType.getFields().get(0);
    Assert.assertNotNull(myStringField);

    Assert.assertFalse(StringUtils.isBlank(myStringField.toString()));
  }

  @Test
  public void testNullArrayField() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    IField mTestField = childClassType.getFields().get(1);
    Assert.assertNotNull(mTestField);

    Assert.assertNull(mTestField.getValue());
    Assert.assertEquals(int.class.getName(), mTestField.getDataType().getName());
    Assert.assertEquals(2, mTestField.getDataType().getArrayDimension());
    Assert.assertEquals(childClassType, mTestField.getDeclaringType());
    Assert.assertEquals(Flags.AccProtected | Flags.AccFinal, mTestField.getFlags());
    Assert.assertEquals("m_test", mTestField.getName());
  }

  @Test
  public void testAnnotationOnFieldChild() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    IField mTestField = childClassType.getFields().get(1);
    Assert.assertNotNull(mTestField);

    Assert.assertEquals(1, mTestField.getAnnotations().size());
    Assert.assertEquals(mTestField, mTestField.getAnnotations().get(0).getOwner());
    Assert.assertEquals(TestAnnotation.class.getName(), mTestField.getAnnotations().get(0).getType().getName());
  }

  @Test
  public void testAnnotationOnFieldBase() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    IField myLongField = baseClassType.getFields().get(1);
    Assert.assertEquals(1, myLongField.getAnnotations().size());
    Assert.assertEquals(myLongField, myLongField.getAnnotations().get(0).getOwner());
    Assert.assertEquals(TestAnnotation.class.getName(), myLongField.getAnnotations().get(0).getType().getName());
  }

  @Test
  public void testLongConstantField() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    IField myLongField = baseClassType.getFields().get(1);
    Assert.assertNotNull(myLongField);

    Assert.assertEquals(Long.class.getName(), myLongField.getDataType().getName());
    Assert.assertEquals(baseClassType, myLongField.getDeclaringType());
    Assert.assertEquals(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal, myLongField.getFlags());
    Assert.assertEquals("myLong", myLongField.getName());
  }

  @Test
  public void testAnonymousTypeField() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    IField anonymousClassField = baseClassType.getFields().get(0);
    Assert.assertNotNull(anonymousClassField);

    Assert.assertNull(anonymousClassField.getValue());
    Assert.assertEquals(Runnable.class.getName(), anonymousClassField.getDataType().getName());
    Assert.assertEquals(0, anonymousClassField.getDataType().getArrayDimension());
    Assert.assertEquals(baseClassType, anonymousClassField.getDeclaringType());
    Assert.assertEquals(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal, anonymousClassField.getFlags());
    Assert.assertEquals("ANONYMOUS_CLASS", anonymousClassField.getName());
  }
}
