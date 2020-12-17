/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link JavaTypesTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
public class JavaTypesTest {

  // primitive types
  private static final String _boolean = "boolean";
  private static final String _byte = "byte";
  private static final String _char = "char";
  private static final String _double = "double";
  private static final String _float = "float";
  private static final String _int = "int";
  private static final String _long = "long";
  private static final String _short = "short";
  private static final String _void = "void";

  // wrapper types
  private static final String Boolean = "java.lang.Boolean";
  private static final String Byte = "java.lang.Byte";
  private static final String Character = "java.lang.Character";
  private static final String Double = "java.lang.Double";
  private static final String Float = "java.lang.Float";
  private static final String Integer = "java.lang.Integer";
  private static final String Long = "java.lang.Long";
  private static final String Short = "java.lang.Short";
  private static final String Void = "java.lang.Void";

  @Test
  public void testBoxPrimitive() {
    assertEquals(Boolean, JavaTypes.boxPrimitive(_boolean));
    assertEquals(Byte, JavaTypes.boxPrimitive(_byte));
    assertEquals(Character, JavaTypes.boxPrimitive(_char));
    assertEquals(Double, JavaTypes.boxPrimitive(_double));
    assertEquals(Float, JavaTypes.boxPrimitive(_float));
    assertEquals(Integer, JavaTypes.boxPrimitive(_int));
    assertEquals(Long, JavaTypes.boxPrimitive(_long));
    assertEquals(Short, JavaTypes.boxPrimitive(_short));
    assertEquals(Void, JavaTypes.boxPrimitive(_void));

    assertEquals(Boolean, JavaTypes.boxPrimitive(Boolean));
    assertEquals(Byte, JavaTypes.boxPrimitive(Byte));
    assertEquals(Character, JavaTypes.boxPrimitive(Character));
    assertEquals(Double, JavaTypes.boxPrimitive(Double));
    assertEquals(Float, JavaTypes.boxPrimitive(Float));
    assertEquals(Integer, JavaTypes.boxPrimitive(Integer));
    assertEquals(Long, JavaTypes.boxPrimitive(Long));
    assertEquals(Short, JavaTypes.boxPrimitive(Short));
    assertEquals(Void, JavaTypes.boxPrimitive(Void));

    assertNull(JavaTypes.boxPrimitive(null));
    assertEquals("whatever", JavaTypes.boxPrimitive("whatever"));
  }

  @Test
  public void testUnboxToPrimitive() {
    assertEquals(_boolean, JavaTypes.unboxToPrimitive(_boolean));
    assertEquals(_byte, JavaTypes.unboxToPrimitive(_byte));
    assertEquals(_char, JavaTypes.unboxToPrimitive(_char));
    assertEquals(_double, JavaTypes.unboxToPrimitive(_double));
    assertEquals(_float, JavaTypes.unboxToPrimitive(_float));
    assertEquals(_int, JavaTypes.unboxToPrimitive(_int));
    assertEquals(_long, JavaTypes.unboxToPrimitive(_long));
    assertEquals(_short, JavaTypes.unboxToPrimitive(_short));
    assertEquals(_void, JavaTypes.unboxToPrimitive(_void));

    assertEquals(_boolean, JavaTypes.unboxToPrimitive(Boolean));
    assertEquals(_byte, JavaTypes.unboxToPrimitive(Byte));
    assertEquals(_char, JavaTypes.unboxToPrimitive(Character));
    assertEquals(_double, JavaTypes.unboxToPrimitive(Double));
    assertEquals(_float, JavaTypes.unboxToPrimitive(Float));
    assertEquals(_int, JavaTypes.unboxToPrimitive(Integer));
    assertEquals(_long, JavaTypes.unboxToPrimitive(Long));
    assertEquals(_short, JavaTypes.unboxToPrimitive(Short));
    assertEquals(_void, JavaTypes.unboxToPrimitive(Void));

    assertNull(JavaTypes.unboxToPrimitive(null));
    assertEquals("whatever", JavaTypes.unboxToPrimitive("whatever"));
  }

  @Test
  public void testDefaultValueOf() {
    assertNull(JavaTypes.defaultValueOf(null));

    // primitives
    assertEquals("false", JavaTypes.defaultValueOf(_boolean));
    assertEquals("0", JavaTypes.defaultValueOf(_byte));
    assertEquals("0", JavaTypes.defaultValueOf(_char));
    assertEquals("0.0", JavaTypes.defaultValueOf(_double));
    assertEquals("0.0f", JavaTypes.defaultValueOf(_float));
    assertEquals("0", JavaTypes.defaultValueOf(_int));
    assertEquals("0L", JavaTypes.defaultValueOf(_long));
    assertEquals("0", JavaTypes.defaultValueOf(_short));
    assertNull(JavaTypes.defaultValueOf(_void));
    assertEquals("null", JavaTypes.defaultValueOf(Object.class.getName()));

    // complex
    assertEquals("false", JavaTypes.defaultValueOf(Boolean));
    assertEquals("0", JavaTypes.defaultValueOf(Byte));
    assertEquals("0", JavaTypes.defaultValueOf(Character));
    assertEquals("0.0", JavaTypes.defaultValueOf(Double));
    assertEquals("0.0f", JavaTypes.defaultValueOf(Float));
    assertEquals("0", JavaTypes.defaultValueOf(Integer));
    assertEquals("0L", JavaTypes.defaultValueOf(Long));
    assertEquals("0", JavaTypes.defaultValueOf(Short));
    assertEquals("null", JavaTypes.defaultValueOf(String.class.getName()));
    assertEquals("null", JavaTypes.defaultValueOf(Void));
  }

  @Test
  public void testIsPrimitive() {
    assertFalse(JavaTypes.isPrimitive(null));
    assertTrue(JavaTypes.isPrimitive(JavaTypes._boolean));
    assertTrue(JavaTypes.isPrimitive(JavaTypes._byte));
    assertTrue(JavaTypes.isPrimitive(JavaTypes._char));
    assertTrue(JavaTypes.isPrimitive(JavaTypes._double));
    assertTrue(JavaTypes.isPrimitive(JavaTypes._float));
    assertTrue(JavaTypes.isPrimitive(JavaTypes._int));
    assertTrue(JavaTypes.isPrimitive(JavaTypes._long));
    assertTrue(JavaTypes.isPrimitive(JavaTypes._short));
    assertTrue(JavaTypes.isPrimitive(JavaTypes._void));
    assertFalse(JavaTypes.isPrimitive(JavaTypes.Character));
    assertFalse(JavaTypes.isPrimitive(JavaTypes.Short));
  }

  @Test
  public void testSubElement() {
    assertEquals("def", JavaTypes.subElement("abc   def   ghi", 3, 11));
    assertEquals("abc   def   ghi", JavaTypes.subElement("abc   def   ghi", 0, 15));
    assertEquals("def", JavaTypes.subElement("abc   def   ghi", 6, 9));
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
  public void testCreateMethodIdentifier(IJavaEnvironment env) {
    var type = env.requireType(ChildClass.class.getName());
    var method = type.methods().item(1).get();
    assertEquals("methodInChildClass(java.lang.String,java.util.List<java.lang.Runnable>)", method.identifier());
    assertEquals("methodInChildClass(java.lang.String,java.util.List)", method.identifier(true));
    assertEquals("methodInChildClass(java.lang.String,java.util.List<java.lang.Runnable>)", method.toWorkingCopy().identifier(env));
    assertEquals("methodInChildClass()", JavaTypes.createMethodIdentifier("methodInChildClass", null));
  }

  @Test
  public void testErasure() {
    assertEquals("QList;", JavaTypes.erasure("QList<QT;>;"));
    assertEquals("QList;", JavaTypes.erasure("QList;"));
    assertEquals("QX;", JavaTypes.erasure("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>;"));
    assertEquals("QX.Member;", JavaTypes.erasure("QX<QObject;>.Member;"));
    assertEquals("QX.Member;", JavaTypes.erasure("QX<QObject;>.Member<QObject;>;"));
    assertEquals("QX.Member;", JavaTypes.erasure("QX.Member<QObject;>;"));
    assertEquals("QX.Member;", JavaTypes.erasure("QX<QObject;>.Member<QList<QT;>;QMap<QU;QABC<QT;>;>;>;"));
    assertEquals("QX.Member;", JavaTypes.erasure("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>.Member<QObject;>;"));
    assertEquals("X.Member", JavaTypes.erasure("X<List<T>,Map<U,ABC<T>>>.Member<Object>"));
    var sign = "Ljava.util.List;";
    assertSame(sign, JavaTypes.erasure(sign));
    assertEquals("java.util.List", JavaTypes.erasure("java.util.List<-[java.lang.Number>"));
    assertEquals("java.util.List", JavaTypes.erasure("java.util.List<? extends java.lang.Number>"));
    assertEquals("java.util.List", JavaTypes.erasure("java.util.List<? super java.lang.Number>"));
    assertEquals("java.util.List", JavaTypes.erasure("java.util.List<java.lang.String>"));
  }

  @Test
  public void testQualifier() {
    assertEquals("java.lang", JavaTypes.qualifier("java.lang.Object"));
    assertEquals("", JavaTypes.qualifier(""));
    assertEquals("java.util", JavaTypes.qualifier("java.util.List<java.lang.Object>"));
    assertEquals("org.eclipse.scout", JavaTypes.qualifier("org.eclipse.scout.Blub$Inner$Inner"));
    assertEquals("Outer", JavaTypes.qualifier("Outer.Inner"));
  }

  @Test
  public void testSimpleName() {
    assertEquals("Object", JavaTypes.simpleName("java.lang.Object"));
    assertEquals("", JavaTypes.simpleName(""));
    assertEquals("Entry", JavaTypes.simpleName("java.util.Map$Entry"));
    assertEquals("List", JavaTypes.simpleName("java.util.List<java.lang.String>"));
    assertEquals("Map", JavaTypes.simpleName("java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"));
  }

  @Test
  public void testTypeArguments() {
    assertEquals(singletonList("T"), JavaTypes.typeArguments("List<T>"));
    assertEquals(Arrays.asList("T", "U"), JavaTypes.typeArguments("X<T,U>"));
    assertEquals(singletonList("*"), JavaTypes.typeArguments("X<*>"));
    assertEquals(Arrays.asList("? extends E", "S"), JavaTypes.typeArguments("X<? extends E,S>"));
    assertEquals(Arrays.asList("List<T>", "Map<U,ABC<T>>"), JavaTypes.typeArguments("X<List<T>,   Map<U,ABC<T>>   >"));
    assertEquals(emptyList(), JavaTypes.typeArguments("List"));
    assertEquals(emptyList(), JavaTypes.typeArguments("X<Object>.Member"));
    assertEquals(singletonList("Object"), JavaTypes.typeArguments("X<Object>.Member<Object>"));
    assertEquals(singletonList("Object"), JavaTypes.typeArguments("X.Member<Object>"));
    assertEquals(Arrays.asList("List<T>", "Map<U,ABC<T>>"), JavaTypes.typeArguments("X<Object>.Member<List<T>,Map<U,ABC<T>>>"));
    assertEquals(singletonList("Object"), JavaTypes.typeArguments("X<List<T>,Map<U,ABC<T>>>.Member<Object>"));
  }
}
