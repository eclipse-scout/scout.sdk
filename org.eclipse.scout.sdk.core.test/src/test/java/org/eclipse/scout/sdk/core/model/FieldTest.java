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
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
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

    IField myStringField = childClassType.fields().first();
    Assert.assertNotNull(myStringField);

    Assert.assertEquals("myStringValue", myStringField.constantValue().get(String.class));
    Assert.assertEquals(String.class.getName(), myStringField.dataType().name());
    Assert.assertEquals(childClassType, myStringField.declaringType());
    Assert.assertEquals(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal, myStringField.flags());
    Assert.assertEquals("myString", myStringField.elementName());
  }

  @Test
  public void testToString() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    IField myStringField = childClassType.fields().first();
    Assert.assertNotNull(myStringField);

    Assert.assertFalse(StringUtils.isBlank(myStringField.toString()));
  }

  @Test
  public void testNullArrayField() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    IField mTestField = childClassType.fields().list().get(1);
    Assert.assertNotNull(mTestField);

    Assert.assertNull(mTestField.constantValue());
    Assert.assertEquals(int.class.getName(), mTestField.dataType().leafComponentType().name());
    Assert.assertEquals(2, mTestField.dataType().arrayDimension());
    Assert.assertEquals(childClassType, mTestField.declaringType());
    Assert.assertEquals(Flags.AccProtected | Flags.AccFinal, mTestField.flags());
    Assert.assertEquals("m_test", mTestField.elementName());
  }

  @Test
  public void testAnnotationOnFieldChild() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    IField mTestField = childClassType.fields().list().get(1);
    Assert.assertNotNull(mTestField);

    Assert.assertEquals(1, mTestField.annotations().list().size());
    Assert.assertEquals(mTestField, mTestField.annotations().first().owner());
    Assert.assertEquals(TestAnnotation.class.getName(), mTestField.annotations().first().type().name());
  }

  @Test
  public void testAnnotationOnFieldBase() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    IField myLongField = baseClassType.fields().first();
    Assert.assertEquals(1, myLongField.annotations().list().size());
    Assert.assertEquals(myLongField, myLongField.annotations().first().owner());
    Assert.assertEquals(TestAnnotation.class.getName(), myLongField.annotations().first().type().name());
  }

  @Test
  public void testLongConstantField() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    IField myLongField = baseClassType.fields().first();
    Assert.assertNotNull(myLongField);

    Assert.assertEquals(IJavaRuntimeTypes.java_lang_Long, myLongField.dataType().name());
    Assert.assertEquals(baseClassType, myLongField.declaringType());
    Assert.assertEquals(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal, myLongField.flags());
    Assert.assertEquals("myLong", myLongField.elementName());
  }

  @Test
  public void testAnonymousTypeField() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    IField anonymousClassField = baseClassType.fields().list().get(1);
    Assert.assertNotNull(anonymousClassField);

    Assert.assertNull(anonymousClassField.constantValue());
    Assert.assertEquals(Runnable.class.getName(), anonymousClassField.dataType().name());
    Assert.assertEquals(0, anonymousClassField.dataType().arrayDimension());
    Assert.assertEquals(baseClassType, anonymousClassField.declaringType());
    Assert.assertEquals(Flags.AccPublic | Flags.AccStatic | Flags.AccFinal, anonymousClassField.flags());
    Assert.assertEquals("ANONYMOUS_CLASS", anonymousClassField.elementName());
  }
}
