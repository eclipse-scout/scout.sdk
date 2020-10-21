/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.spi.internal.metavalue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation.TestEnum;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.model.ecj.metavalue.MetaValueFactory;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class MetaValueFactoryTest {
  @Test
  public void testCreateNull() {
    var createNull = MetaValueFactory.createNull();
    assertNull(createNull.as(Object.class));
    assertEquals("null", createNull.toString());
    assertEquals(MetaValueType.Null, createNull.type());
    assertEquals(MetaValueType.Null, MetaValueFactory.createArray(null).type());
    assertEquals(MetaValueType.Null, MetaValueFactory.createFromAnnotation(null).type());
    assertEquals(MetaValueType.Null, MetaValueFactory.createFromConstant(null).type());
    assertEquals(MetaValueType.Null, MetaValueFactory.createFromEnum(null).type());
    assertEquals(MetaValueType.Null, MetaValueFactory.createFromType(null).type());
    assertEquals(MetaValueType.Null, MetaValueFactory.createUnknown(null).type());
  }

  @Test
  public void testCreateUnknown() {
    Object val = "test";
    var metaValue = MetaValueFactory.createUnknown(val);
    assertSame(val, metaValue.as(Object.class));
    assertEquals("Unknown(" + val + ')', metaValue.toString());
    assertEquals(MetaValueType.Unknown, metaValue.type());
  }

  @Test
  public void testCreateFromType(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());
    var metaValue = MetaValueFactory.createFromType(childClassType.unwrap());
    assertSame(childClassType, metaValue.as(Object.class));
    assertSame(childClassType, metaValue.as(IType.class));
    assertSame(childClassType.unwrap(), metaValue.as(List.class));
    assertEquals(childClassType.name(), metaValue.as(String.class));
    assertEquals(childClassType.elementName() + ".class", metaValue.toString());
    assertEquals(MetaValueType.Type, metaValue.type());
  }

  @Test
  public void testCreateFromEnum(IJavaEnvironment env) {
    var type = env.requireType(TestEnum.class.getName());
    var field = type.fields().first().get();

    var metaValue = MetaValueFactory.createFromEnum(field.unwrap());
    assertSame(field, metaValue.as(Object.class));
    assertSame(field, metaValue.as(IField.class));
    assertSame(field.unwrap(), metaValue.as(List.class));
    assertEquals(field.elementName(), metaValue.as(String.class));
    assertEquals(type.elementName() + JavaTypes.C_DOT + field.elementName(), metaValue.toString());
    assertEquals(MetaValueType.Enum, metaValue.type());
  }

  @Test
  public void testCreateFromAnnotation(IJavaEnvironment env) {
    var annot = env.requireType(ChildClass.class.getName()).annotations().first().get();
    var metaValue = MetaValueFactory.createFromAnnotation(annot.unwrap());
    assertSame(annot, metaValue.as(Object.class));
    assertSame(annot, metaValue.as(IAnnotation.class));
    assertEquals(annot.unwrap(), metaValue.as(String.class));
    assertEquals(annot.toString(), metaValue.toString());
    assertEquals(MetaValueType.Annotation, metaValue.type());
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testCreateFromConstantBoolean() {
    var val = true;
    var metaValue = MetaValueFactory.createFromConstant(BooleanConstant.fromValue(val));
    assertEquals(MetaValueType.Bool, metaValue.type());
    assertEquals(val, metaValue.as(Object.class));
    assertEquals(val, metaValue.as(Boolean.class));
    assertEquals(val, metaValue.as(boolean.class));
    assertEquals(Boolean.toString(val), metaValue.as(String.class));
    assertEquals(Boolean.toString(val), metaValue.toString());
  }

  @Test
  public void testCreateFromConstantByte() {
    byte val = 55;
    var metaValue = MetaValueFactory.createFromConstant(ByteConstant.fromValue(val));
    assertEquals(MetaValueType.Byte, metaValue.type());
    assertEquals(val, metaValue.as(Object.class));
    assertEquals(Byte.toString(val), metaValue.toString());
    assertEquals(Byte.toString(val), metaValue.as(String.class));

    assertEquals(Byte.valueOf(val), metaValue.as(Byte.class));
    assertEquals(Byte.valueOf(val), metaValue.as(byte.class));
    assertEquals(Integer.valueOf(val), metaValue.as(int.class));
    assertEquals(Integer.valueOf(val), metaValue.as(Integer.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(char.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(Character.class));
    assertEquals(Long.valueOf(val), metaValue.as(long.class));
    assertEquals(Long.valueOf(val), metaValue.as(Long.class));
    assertEquals(Short.valueOf(val), metaValue.as(short.class));
    assertEquals(Short.valueOf(val), metaValue.as(Short.class));
    assertEquals(Double.valueOf(val), metaValue.as(double.class));
    assertEquals(Double.valueOf(val), metaValue.as(Double.class));
    assertEquals(Float.valueOf(val), metaValue.as(float.class));
    assertEquals(Float.valueOf(val), metaValue.as(Float.class));
  }

  @Test
  public void testCreateFromConstantInt() {
    var val = 55;
    var metaValue = MetaValueFactory.createFromConstant(IntConstant.fromValue(val));
    assertEquals(MetaValueType.Int, metaValue.type());
    assertEquals(val, metaValue.as(Object.class));
    assertEquals(Integer.toString(val), metaValue.toString());
    assertEquals(Integer.toString(val), metaValue.as(String.class));

    assertEquals(Byte.valueOf((byte) val), metaValue.as(Byte.class));
    assertEquals(Byte.valueOf((byte) val), metaValue.as(byte.class));
    assertEquals(Integer.valueOf(val), metaValue.as(int.class));
    assertEquals(Integer.valueOf(val), metaValue.as(Integer.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(char.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(Character.class));
    assertEquals(Long.valueOf(val), metaValue.as(long.class));
    assertEquals(Long.valueOf(val), metaValue.as(Long.class));
    assertEquals(Short.valueOf((short) val), metaValue.as(short.class));
    assertEquals(Short.valueOf((short) val), metaValue.as(Short.class));
    assertEquals(Double.valueOf(val), metaValue.as(double.class));
    assertEquals(Double.valueOf(val), metaValue.as(Double.class));
    assertEquals(Float.valueOf(val), metaValue.as(float.class));
    assertEquals(Float.valueOf(val), metaValue.as(Float.class));
  }

  @Test
  public void testCreateFromConstantChar() {
    char val = 55;
    var metaValue = MetaValueFactory.createFromConstant(CharConstant.fromValue(val));
    assertEquals(MetaValueType.Char, metaValue.type());
    assertEquals(val, metaValue.as(Object.class));
    assertEquals(Character.toString(val), metaValue.toString());
    assertEquals(Character.toString(val), metaValue.as(String.class));

    assertEquals(Byte.valueOf((byte) val), metaValue.as(Byte.class));
    assertEquals(Byte.valueOf((byte) val), metaValue.as(byte.class));
    assertEquals(Integer.valueOf(val), metaValue.as(int.class));
    assertEquals(Integer.valueOf(val), metaValue.as(Integer.class));
    assertEquals(Character.valueOf(val), metaValue.as(char.class));
    assertEquals(Character.valueOf(val), metaValue.as(Character.class));
    assertEquals(Long.valueOf(val), metaValue.as(long.class));
    assertEquals(Long.valueOf(val), metaValue.as(Long.class));
    assertEquals(Short.valueOf((short) val), metaValue.as(short.class));
    assertEquals(Short.valueOf((short) val), metaValue.as(Short.class));
    assertEquals(Double.valueOf(val), metaValue.as(double.class));
    assertEquals(Double.valueOf(val), metaValue.as(Double.class));
    assertEquals(Float.valueOf(val), metaValue.as(float.class));
    assertEquals(Float.valueOf(val), metaValue.as(Float.class));
  }

  @Test
  public void testCreateFromConstantLong() {
    long val = 55;
    var metaValue = MetaValueFactory.createFromConstant(LongConstant.fromValue(val));
    assertEquals(MetaValueType.Long, metaValue.type());
    assertEquals(val, metaValue.as(Object.class));
    assertEquals(Long.toString(val), metaValue.toString());
    assertEquals(Long.toString(val), metaValue.as(String.class));

    assertEquals(Byte.valueOf((byte) val), metaValue.as(Byte.class));
    assertEquals(Byte.valueOf((byte) val), metaValue.as(byte.class));
    assertEquals(Integer.valueOf((int) val), metaValue.as(int.class));
    assertEquals(Integer.valueOf((int) val), metaValue.as(Integer.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(char.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(Character.class));
    assertEquals(Long.valueOf(val), metaValue.as(long.class));
    assertEquals(Long.valueOf(val), metaValue.as(Long.class));
    assertEquals(Short.valueOf((short) val), metaValue.as(short.class));
    assertEquals(Short.valueOf((short) val), metaValue.as(Short.class));
    assertEquals(Double.valueOf(val), metaValue.as(double.class));
    assertEquals(Double.valueOf(val), metaValue.as(Double.class));
    assertEquals(Float.valueOf(val), metaValue.as(float.class));
    assertEquals(Float.valueOf(val), metaValue.as(Float.class));
  }

  @Test
  public void testCreateFromConstantShort() {
    short val = 55;
    var metaValue = MetaValueFactory.createFromConstant(ShortConstant.fromValue(val));
    assertEquals(MetaValueType.Short, metaValue.type());
    assertEquals(val, metaValue.as(Object.class));
    assertEquals(Short.toString(val), metaValue.toString());
    assertEquals(Short.toString(val), metaValue.as(String.class));

    assertEquals(Byte.valueOf((byte) val), metaValue.as(Byte.class));
    assertEquals(Byte.valueOf((byte) val), metaValue.as(byte.class));
    assertEquals(Integer.valueOf(val), metaValue.as(int.class));
    assertEquals(Integer.valueOf(val), metaValue.as(Integer.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(char.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(Character.class));
    assertEquals(Long.valueOf(val), metaValue.as(long.class));
    assertEquals(Long.valueOf(val), metaValue.as(Long.class));
    assertEquals(Short.valueOf(val), metaValue.as(short.class));
    assertEquals(Short.valueOf(val), metaValue.as(Short.class));
    assertEquals(Double.valueOf(val), metaValue.as(double.class));
    assertEquals(Double.valueOf(val), metaValue.as(Double.class));
    assertEquals(Float.valueOf(val), metaValue.as(float.class));
    assertEquals(Float.valueOf(val), metaValue.as(Float.class));
  }

  @Test
  @SuppressWarnings("NumericCastThatLosesPrecision")
  public void testCreateFromConstantDouble() {
    var val = 55.3;
    var metaValue = MetaValueFactory.createFromConstant(DoubleConstant.fromValue(val));
    assertEquals(MetaValueType.Double, metaValue.type());
    assertEquals(val, metaValue.as(Object.class));
    assertEquals(Double.toString(val), metaValue.toString());
    assertEquals(Double.toString(val), metaValue.as(String.class));

    assertEquals(Byte.valueOf((byte) val), metaValue.as(Byte.class));
    assertEquals(Byte.valueOf((byte) val), metaValue.as(byte.class));
    assertEquals(Integer.valueOf((int) val), metaValue.as(int.class));
    assertEquals(Integer.valueOf((int) val), metaValue.as(Integer.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(char.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(Character.class));
    assertEquals(Long.valueOf((long) val), metaValue.as(long.class));
    assertEquals(Long.valueOf((long) val), metaValue.as(Long.class));
    assertEquals(Short.valueOf((short) val), metaValue.as(short.class));
    assertEquals(Short.valueOf((short) val), metaValue.as(Short.class));
    assertEquals(Double.valueOf(val), metaValue.as(double.class));
    assertEquals(Double.valueOf(val), metaValue.as(Double.class));
    assertEquals(Float.valueOf((float) val), metaValue.as(float.class));
    assertEquals(Float.valueOf((float) val), metaValue.as(Float.class));
  }

  @Test
  @SuppressWarnings("NumericCastThatLosesPrecision")
  public void testCreateFromConstantFloat() {
    var val = 55.3f;
    var metaValue = MetaValueFactory.createFromConstant(FloatConstant.fromValue(val));
    assertEquals(MetaValueType.Float, metaValue.type());
    assertEquals(val, metaValue.as(Object.class));
    assertEquals(Float.toString(val), metaValue.toString());
    assertEquals(Float.toString(val), metaValue.as(String.class));

    assertEquals(Byte.valueOf((byte) val), metaValue.as(Byte.class));
    assertEquals(Byte.valueOf((byte) val), metaValue.as(byte.class));
    assertEquals(Integer.valueOf((int) val), metaValue.as(int.class));
    assertEquals(Integer.valueOf((int) val), metaValue.as(Integer.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(char.class));
    assertEquals(Character.valueOf((char) val), metaValue.as(Character.class));
    assertEquals(Long.valueOf((long) val), metaValue.as(long.class));
    assertEquals(Long.valueOf((long) val), metaValue.as(Long.class));
    assertEquals(Short.valueOf((short) val), metaValue.as(short.class));
    assertEquals(Short.valueOf((short) val), metaValue.as(Short.class));
    assertEquals(Double.valueOf(val), metaValue.as(double.class));
    assertEquals(Double.valueOf(val), metaValue.as(Double.class));
    assertEquals(Float.valueOf(val), metaValue.as(float.class));
    assertEquals(Float.valueOf(val), metaValue.as(Float.class));
  }

  @Test
  public void testCreateFromConstantString() {
    var val = "teststring";
    var metaValue = MetaValueFactory.createFromConstant(StringConstant.fromValue(val));
    assertEquals(MetaValueType.String, metaValue.type());
    assertEquals(val, metaValue.as(Object.class));
    assertEquals(val, metaValue.as(String.class));
    assertArrayEquals(new String[]{val}, metaValue.as(String[].class));
    assertEquals(val, metaValue.toString());
  }

  @Test
  public void testCreateFromConstantNullString() {
    var metaValue = MetaValueFactory.createFromConstant(StringConstant.fromValue(null));
    assertEquals(MetaValueType.String, metaValue.type());
    assertNull(metaValue.as(Object.class));
    assertNull(metaValue.as(String.class));
    assertEquals("null", metaValue.toString());
  }

  @Test
  public void testCreateArray() {
    var first = "first";
    var second = "second";
    var firstMetaValue = MetaValueFactory.createFromConstant(StringConstant.fromValue(first));
    var secondMetaValue = MetaValueFactory.createFromConstant(StringConstant.fromValue(second));
    IMetaValue[] metaArray = {firstMetaValue, secondMetaValue};
    var metaValue = MetaValueFactory.createArray(metaArray);

    assertEquals(MetaValueType.Array, metaValue.type());
    assertArrayEquals(new Object[]{first, second}, (Object[]) metaValue.as(Object.class));
    assertArrayEquals(new Object[]{first, second}, metaValue.as(Object[].class));
    assertArrayEquals(new Object[]{first, second}, metaValue.as(String[].class));

    var array = (IArrayMetaValue) metaValue;
    assertSame(metaArray, array.metaValueArray());
  }

  @Test
  public void testCreateArrayOneElement() {
    var first = "first";
    var firstMetaValue = MetaValueFactory.createFromConstant(StringConstant.fromValue(first));
    IMetaValue[] metaArray = {firstMetaValue};
    var metaValue = MetaValueFactory.createArray(metaArray);

    assertEquals(MetaValueType.Array, metaValue.type());
    assertArrayEquals(new Object[]{first}, (Object[]) metaValue.as(Object.class));
    assertArrayEquals(new Object[]{first}, metaValue.as(Object[].class));
    assertArrayEquals(new Object[]{first}, metaValue.as(String[].class));
    assertEquals(first, metaValue.as(String.class));

    var array = (IArrayMetaValue) metaValue;
    assertSame(metaArray, array.metaValueArray());
  }
}
