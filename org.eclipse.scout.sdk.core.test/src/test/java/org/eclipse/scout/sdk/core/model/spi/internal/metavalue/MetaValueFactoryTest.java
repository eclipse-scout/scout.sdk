/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi.internal.metavalue;

import java.util.List;

import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation.TestEnum;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

public class MetaValueFactoryTest {
  @Test
  public void testCreateNull() {
    IMetaValue createNull = MetaValueFactory.createNull();
    Assert.assertNull(createNull.get(Object.class));
    Assert.assertEquals("null", createNull.toString());
    Assert.assertEquals(MetaValueType.Null, createNull.type());
    Assert.assertEquals(MetaValueType.Null, MetaValueFactory.createArray(null).type());
    Assert.assertEquals(MetaValueType.Null, MetaValueFactory.createFromAnnotation(null).type());
    Assert.assertEquals(MetaValueType.Null, MetaValueFactory.createFromConstant(null).type());
    Assert.assertEquals(MetaValueType.Null, MetaValueFactory.createFromEnum(null).type());
    Assert.assertEquals(MetaValueType.Null, MetaValueFactory.createFromType(null).type());
    Assert.assertEquals(MetaValueType.Null, MetaValueFactory.createUnknown(null).type());
  }

  @Test
  public void testCreateUnknown() {
    Object val = "test";
    IMetaValue metaValue = MetaValueFactory.createUnknown(val);
    Assert.assertSame(val, metaValue.get(Object.class));
    Assert.assertEquals("Unknown(" + val + ")", metaValue.toString());
    Assert.assertEquals(MetaValueType.Unknown, metaValue.type());
  }

  @Test
  public void testCreateFromType() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    IMetaValue metaValue = MetaValueFactory.createFromType(childClassType.unwrap());
    Assert.assertSame(childClassType, metaValue.get(Object.class));
    Assert.assertSame(childClassType, metaValue.get(IType.class));
    Assert.assertSame(childClassType.unwrap(), metaValue.get(List.class));
    Assert.assertEquals(childClassType.name(), metaValue.get(String.class));
    Assert.assertEquals(childClassType.elementName() + ".class", metaValue.toString());
    Assert.assertEquals(MetaValueType.Type, metaValue.type());
  }

  @Test
  public void testCreateFromEnum() {
    IType type = CoreTestingUtils.createJavaEnvironment().findType(TestEnum.class.getName());
    IField field = type.fields().first();

    IMetaValue metaValue = MetaValueFactory.createFromEnum(field.unwrap());
    Assert.assertSame(field, metaValue.get(Object.class));
    Assert.assertSame(field, metaValue.get(IField.class));
    Assert.assertSame(field.unwrap(), metaValue.get(List.class));
    Assert.assertEquals(field.elementName(), metaValue.get(String.class));
    Assert.assertEquals(type.elementName() + '.' + field.elementName(), metaValue.toString());
    Assert.assertEquals(MetaValueType.Enum, metaValue.type());
  }

  @Test
  public void testCreateFromAnnotation() {
    IAnnotation annot = CoreTestingUtils.getChildClassType().annotations().first();

    IMetaValue metaValue = MetaValueFactory.createFromAnnotation(annot.unwrap());
    Assert.assertSame(annot, metaValue.get(Object.class));
    Assert.assertSame(annot, metaValue.get(IAnnotation.class));
    Assert.assertEquals(annot.unwrap(), metaValue.get(String.class));
    Assert.assertEquals(annot.toString(), metaValue.toString());
    Assert.assertEquals(MetaValueType.Annotation, metaValue.type());
  }

  @Test
  public void testCreateFromConstantBoolean() {
    boolean val = true;
    IMetaValue metaValue = MetaValueFactory.createFromConstant(BooleanConstant.fromValue(val));
    Assert.assertEquals(MetaValueType.Bool, metaValue.type());
    Assert.assertEquals(val, metaValue.get(Object.class));
    Assert.assertEquals(val, metaValue.get(Boolean.class));
    Assert.assertEquals(val, metaValue.get(boolean.class));
    Assert.assertEquals(Boolean.toString(val), metaValue.get(String.class));
    Assert.assertEquals(Boolean.toString(val), metaValue.toString());
  }

  @Test
  public void testCreateFromConstantByte() {
    byte val = 55;
    IMetaValue metaValue = MetaValueFactory.createFromConstant(ByteConstant.fromValue(val));
    Assert.assertEquals(MetaValueType.Byte, metaValue.type());
    Assert.assertEquals(Byte.valueOf(val), metaValue.get(Object.class));
    Assert.assertEquals(Byte.toString(val), metaValue.toString());
    Assert.assertEquals(Byte.toString(val), metaValue.get(String.class));

    Assert.assertEquals(Byte.valueOf(val), metaValue.get(Byte.class));
    Assert.assertEquals(Byte.valueOf(val), metaValue.get(byte.class));
    Assert.assertEquals(Integer.valueOf(val), metaValue.get(int.class));
    Assert.assertEquals(Integer.valueOf(val), metaValue.get(Integer.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(char.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(Character.class));
    Assert.assertEquals(Long.valueOf(val), metaValue.get(long.class));
    Assert.assertEquals(Long.valueOf(val), metaValue.get(Long.class));
    Assert.assertEquals(Short.valueOf(val), metaValue.get(short.class));
    Assert.assertEquals(Short.valueOf(val), metaValue.get(Short.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(double.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(Double.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(float.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(Float.class));
  }

  @Test
  public void testCreateFromConstantInt() {
    int val = 55;
    IMetaValue metaValue = MetaValueFactory.createFromConstant(IntConstant.fromValue(val));
    Assert.assertEquals(MetaValueType.Int, metaValue.type());
    Assert.assertEquals(Integer.valueOf(val), metaValue.get(Object.class));
    Assert.assertEquals(Integer.toString(val), metaValue.toString());
    Assert.assertEquals(Integer.toString(val), metaValue.get(String.class));

    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(Byte.class));
    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(byte.class));
    Assert.assertEquals(Integer.valueOf(val), metaValue.get(int.class));
    Assert.assertEquals(Integer.valueOf(val), metaValue.get(Integer.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(char.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(Character.class));
    Assert.assertEquals(Long.valueOf(val), metaValue.get(long.class));
    Assert.assertEquals(Long.valueOf(val), metaValue.get(Long.class));
    Assert.assertEquals(Short.valueOf((short) val), metaValue.get(short.class));
    Assert.assertEquals(Short.valueOf((short) val), metaValue.get(Short.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(double.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(Double.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(float.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(Float.class));
  }

  @Test
  public void testCreateFromConstantChar() {
    char val = 55;
    IMetaValue metaValue = MetaValueFactory.createFromConstant(CharConstant.fromValue(val));
    Assert.assertEquals(MetaValueType.Char, metaValue.type());
    Assert.assertEquals(Character.valueOf(val), metaValue.get(Object.class));
    Assert.assertEquals(Character.toString(val), metaValue.toString());
    Assert.assertEquals(Character.toString(val), metaValue.get(String.class));

    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(Byte.class));
    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(byte.class));
    Assert.assertEquals(Integer.valueOf(val), metaValue.get(int.class));
    Assert.assertEquals(Integer.valueOf(val), metaValue.get(Integer.class));
    Assert.assertEquals(Character.valueOf(val), metaValue.get(char.class));
    Assert.assertEquals(Character.valueOf(val), metaValue.get(Character.class));
    Assert.assertEquals(Long.valueOf(val), metaValue.get(long.class));
    Assert.assertEquals(Long.valueOf(val), metaValue.get(Long.class));
    Assert.assertEquals(Short.valueOf((short) val), metaValue.get(short.class));
    Assert.assertEquals(Short.valueOf((short) val), metaValue.get(Short.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(double.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(Double.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(float.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(Float.class));
  }

  @Test
  public void testCreateFromConstantLong() {
    long val = 55;
    IMetaValue metaValue = MetaValueFactory.createFromConstant(LongConstant.fromValue(val));
    Assert.assertEquals(MetaValueType.Long, metaValue.type());
    Assert.assertEquals(Long.valueOf(val), metaValue.get(Object.class));
    Assert.assertEquals(Long.toString(val), metaValue.toString());
    Assert.assertEquals(Long.toString(val), metaValue.get(String.class));

    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(Byte.class));
    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(byte.class));
    Assert.assertEquals(Integer.valueOf((int) val), metaValue.get(int.class));
    Assert.assertEquals(Integer.valueOf((int) val), metaValue.get(Integer.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(char.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(Character.class));
    Assert.assertEquals(Long.valueOf(val), metaValue.get(long.class));
    Assert.assertEquals(Long.valueOf(val), metaValue.get(Long.class));
    Assert.assertEquals(Short.valueOf((short) val), metaValue.get(short.class));
    Assert.assertEquals(Short.valueOf((short) val), metaValue.get(Short.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(double.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(Double.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(float.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(Float.class));
  }

  @Test
  public void testCreateFromConstantShort() {
    short val = 55;
    IMetaValue metaValue = MetaValueFactory.createFromConstant(ShortConstant.fromValue(val));
    Assert.assertEquals(MetaValueType.Short, metaValue.type());
    Assert.assertEquals(Short.valueOf(val), metaValue.get(Object.class));
    Assert.assertEquals(Short.toString(val), metaValue.toString());
    Assert.assertEquals(Short.toString(val), metaValue.get(String.class));

    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(Byte.class));
    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(byte.class));
    Assert.assertEquals(Integer.valueOf(val), metaValue.get(int.class));
    Assert.assertEquals(Integer.valueOf(val), metaValue.get(Integer.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(char.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(Character.class));
    Assert.assertEquals(Long.valueOf(val), metaValue.get(long.class));
    Assert.assertEquals(Long.valueOf(val), metaValue.get(Long.class));
    Assert.assertEquals(Short.valueOf(val), metaValue.get(short.class));
    Assert.assertEquals(Short.valueOf(val), metaValue.get(Short.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(double.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(Double.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(float.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(Float.class));
  }

  @Test
  public void testCreateFromConstantDouble() {
    double val = 55.3;
    IMetaValue metaValue = MetaValueFactory.createFromConstant(DoubleConstant.fromValue(val));
    Assert.assertEquals(MetaValueType.Double, metaValue.type());
    Assert.assertEquals(Double.valueOf(val), metaValue.get(Object.class));
    Assert.assertEquals(Double.toString(val), metaValue.toString());
    Assert.assertEquals(Double.toString(val), metaValue.get(String.class));

    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(Byte.class));
    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(byte.class));
    Assert.assertEquals(Integer.valueOf((int) val), metaValue.get(int.class));
    Assert.assertEquals(Integer.valueOf((int) val), metaValue.get(Integer.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(char.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(Character.class));
    Assert.assertEquals(Long.valueOf((long) val), metaValue.get(long.class));
    Assert.assertEquals(Long.valueOf((long) val), metaValue.get(Long.class));
    Assert.assertEquals(Short.valueOf((short) val), metaValue.get(short.class));
    Assert.assertEquals(Short.valueOf((short) val), metaValue.get(Short.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(double.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(Double.class));
    Assert.assertEquals(Float.valueOf((float) val), metaValue.get(float.class));
    Assert.assertEquals(Float.valueOf((float) val), metaValue.get(Float.class));
  }

  @Test
  public void testCreateFromConstantFloat() {
    float val = 55.3f;
    IMetaValue metaValue = MetaValueFactory.createFromConstant(FloatConstant.fromValue(val));
    Assert.assertEquals(MetaValueType.Float, metaValue.type());
    Assert.assertEquals(Float.valueOf(val), metaValue.get(Object.class));
    Assert.assertEquals(Float.toString(val), metaValue.toString());
    Assert.assertEquals(Float.toString(val), metaValue.get(String.class));

    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(Byte.class));
    Assert.assertEquals(Byte.valueOf((byte) val), metaValue.get(byte.class));
    Assert.assertEquals(Integer.valueOf((int) val), metaValue.get(int.class));
    Assert.assertEquals(Integer.valueOf((int) val), metaValue.get(Integer.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(char.class));
    Assert.assertEquals(Character.valueOf((char) val), metaValue.get(Character.class));
    Assert.assertEquals(Long.valueOf((long) val), metaValue.get(long.class));
    Assert.assertEquals(Long.valueOf((long) val), metaValue.get(Long.class));
    Assert.assertEquals(Short.valueOf((short) val), metaValue.get(short.class));
    Assert.assertEquals(Short.valueOf((short) val), metaValue.get(Short.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(double.class));
    Assert.assertEquals(Double.valueOf(val), metaValue.get(Double.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(float.class));
    Assert.assertEquals(Float.valueOf(val), metaValue.get(Float.class));
  }

  @Test
  public void testCreateFromConstantString() {
    String val = "teststring";
    IMetaValue metaValue = MetaValueFactory.createFromConstant(StringConstant.fromValue(val));
    Assert.assertEquals(MetaValueType.String, metaValue.type());
    Assert.assertEquals(val, metaValue.get(Object.class));
    Assert.assertEquals(val, metaValue.get(String.class));
    Assert.assertArrayEquals(new String[]{val}, metaValue.get(String[].class));
    Assert.assertEquals(val, metaValue.toString());
  }

  @Test
  public void testCreateFromConstantNullString() {
    IMetaValue metaValue = MetaValueFactory.createFromConstant(StringConstant.fromValue(null));
    Assert.assertEquals(MetaValueType.String, metaValue.type());
    Assert.assertNull(metaValue.get(Object.class));
    Assert.assertNull(metaValue.get(String.class));
    Assert.assertEquals("null", metaValue.toString());
  }

  @Test
  public void testCreateArray() {
    String first = "first";
    String second = "second";
    IMetaValue firstMetaValue = MetaValueFactory.createFromConstant(StringConstant.fromValue(first));
    IMetaValue secondMetaValue = MetaValueFactory.createFromConstant(StringConstant.fromValue(second));
    IMetaValue[] metaArray = new IMetaValue[]{firstMetaValue, secondMetaValue};
    IMetaValue metaValue = MetaValueFactory.createArray(metaArray);

    Assert.assertEquals(MetaValueType.Array, metaValue.type());
    Assert.assertArrayEquals(new Object[]{first, second}, (Object[]) metaValue.get(Object.class));
    Assert.assertArrayEquals(new Object[]{first, second}, metaValue.get(Object[].class));
    Assert.assertArrayEquals(new Object[]{first, second}, metaValue.get(String[].class));

    IArrayMetaValue array = (IArrayMetaValue) metaValue;
    Assert.assertSame(metaArray, array.metaValueArray());
  }

  @Test
  public void testCreateArrayOneElement() {
    String first = "first";
    IMetaValue firstMetaValue = MetaValueFactory.createFromConstant(StringConstant.fromValue(first));
    IMetaValue[] metaArray = new IMetaValue[]{firstMetaValue};
    IMetaValue metaValue = MetaValueFactory.createArray(metaArray);

    Assert.assertEquals(MetaValueType.Array, metaValue.type());
    Assert.assertArrayEquals(new Object[]{first}, (Object[]) metaValue.get(Object.class));
    Assert.assertArrayEquals(new Object[]{first}, metaValue.get(Object[].class));
    Assert.assertArrayEquals(new Object[]{first}, metaValue.get(String[].class));
    Assert.assertEquals(first, metaValue.get(String.class));

    IArrayMetaValue array = (IArrayMetaValue) metaValue;
    Assert.assertSame(metaArray, array.metaValueArray());
  }
}
