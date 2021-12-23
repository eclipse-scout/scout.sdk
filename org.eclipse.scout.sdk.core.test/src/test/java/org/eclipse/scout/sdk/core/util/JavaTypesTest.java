/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
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
import static org.eclipse.scout.sdk.core.util.JavaTypes.arrayMarker;
import static org.eclipse.scout.sdk.core.util.JavaTypes.boxPrimitive;
import static org.eclipse.scout.sdk.core.util.JavaTypes.createMethodIdentifier;
import static org.eclipse.scout.sdk.core.util.JavaTypes.defaultValueOf;
import static org.eclipse.scout.sdk.core.util.JavaTypes.erasure;
import static org.eclipse.scout.sdk.core.util.JavaTypes.isArray;
import static org.eclipse.scout.sdk.core.util.JavaTypes.isPrimitive;
import static org.eclipse.scout.sdk.core.util.JavaTypes.isWildcard;
import static org.eclipse.scout.sdk.core.util.JavaTypes.qualifier;
import static org.eclipse.scout.sdk.core.util.JavaTypes.simpleName;
import static org.eclipse.scout.sdk.core.util.JavaTypes.typeArguments;
import static org.eclipse.scout.sdk.core.util.JavaTypes.unboxToPrimitive;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.sdk.core.builder.java.JavaBuilderContext;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.ClassWithArrayMethodParams;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link JavaTypesTest}</h3>
 *
 * @since 6.1.0
 */
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
    assertEquals(Boolean, boxPrimitive(_boolean));
    assertEquals(Byte, boxPrimitive(_byte));
    assertEquals(Character, boxPrimitive(_char));
    assertEquals(Double, boxPrimitive(_double));
    assertEquals(Float, boxPrimitive(_float));
    assertEquals(Integer, boxPrimitive(_int));
    assertEquals(Long, boxPrimitive(_long));
    assertEquals(Short, boxPrimitive(_short));
    assertEquals(Void, boxPrimitive(_void));

    assertEquals(Boolean, boxPrimitive(Boolean));
    assertEquals(Byte, boxPrimitive(Byte));
    assertEquals(Character, boxPrimitive(Character));
    assertEquals(Double, boxPrimitive(Double));
    assertEquals(Float, boxPrimitive(Float));
    assertEquals(Integer, boxPrimitive(Integer));
    assertEquals(Long, boxPrimitive(Long));
    assertEquals(Short, boxPrimitive(Short));
    assertEquals(Void, boxPrimitive(Void));

    assertNull(boxPrimitive(null));
    assertEquals("whatever", boxPrimitive("whatever"));
  }

  @Test
  public void testUnboxToPrimitive() {
    assertEquals(_boolean, unboxToPrimitive(_boolean));
    assertEquals(_byte, unboxToPrimitive(_byte));
    assertEquals(_char, unboxToPrimitive(_char));
    assertEquals(_double, unboxToPrimitive(_double));
    assertEquals(_float, unboxToPrimitive(_float));
    assertEquals(_int, unboxToPrimitive(_int));
    assertEquals(_long, unboxToPrimitive(_long));
    assertEquals(_short, unboxToPrimitive(_short));
    assertEquals(_void, unboxToPrimitive(_void));

    assertEquals(_boolean, unboxToPrimitive(Boolean));
    assertEquals(_byte, unboxToPrimitive(Byte));
    assertEquals(_char, unboxToPrimitive(Character));
    assertEquals(_double, unboxToPrimitive(Double));
    assertEquals(_float, unboxToPrimitive(Float));
    assertEquals(_int, unboxToPrimitive(Integer));
    assertEquals(_long, unboxToPrimitive(Long));
    assertEquals(_short, unboxToPrimitive(Short));
    assertEquals(_void, unboxToPrimitive(Void));

    assertNull(unboxToPrimitive(null));
    assertEquals("whatever", unboxToPrimitive("whatever"));
  }

  @Test
  public void testDefaultValueOf() {
    assertNull(defaultValueOf(null));

    // primitives
    assertEquals("false", defaultValueOf(_boolean));
    assertEquals("0", defaultValueOf(_byte));
    assertEquals("0", defaultValueOf(_char));
    assertEquals("0.0", defaultValueOf(_double));
    assertEquals("0.0f", defaultValueOf(_float));
    assertEquals("0", defaultValueOf(_int));
    assertEquals("0L", defaultValueOf(_long));
    assertEquals("0", defaultValueOf(_short));
    assertNull(defaultValueOf(_void));
    assertEquals("null", defaultValueOf(Object.class.getName()));

    // complex
    assertEquals("false", defaultValueOf(Boolean));
    assertEquals("0", defaultValueOf(Byte));
    assertEquals("0", defaultValueOf(Character));
    assertEquals("0.0", defaultValueOf(Double));
    assertEquals("0.0f", defaultValueOf(Float));
    assertEquals("0", defaultValueOf(Integer));
    assertEquals("0L", defaultValueOf(Long));
    assertEquals("0", defaultValueOf(Short));
    assertEquals("null", defaultValueOf(String.class.getName()));
    assertEquals("null", defaultValueOf(Void));
  }

  @Test
  public void testIsPrimitive() {
    assertFalse(isPrimitive(null));
    assertTrue(isPrimitive(JavaTypes._boolean));
    assertTrue(isPrimitive(JavaTypes._byte));
    assertTrue(isPrimitive(JavaTypes._char));
    assertTrue(isPrimitive(JavaTypes._double));
    assertTrue(isPrimitive(JavaTypes._float));
    assertTrue(isPrimitive(JavaTypes._int));
    assertTrue(isPrimitive(JavaTypes._long));
    assertTrue(isPrimitive(JavaTypes._short));
    assertTrue(isPrimitive(JavaTypes._void));
    assertFalse(isPrimitive(JavaTypes.Character));
    assertFalse(isPrimitive(JavaTypes.Short));
  }

  @Test
  public void testIsArray() {
    assertFalse(isArray(null));
    assertFalse(isArray(""));
    assertFalse(isArray(" "));
    assertFalse(isArray(JavaTypes._boolean));
    assertFalse(isArray(List.class.getName()));
    assertTrue(isArray(List.class.getName() + arrayMarker(3)));
    assertTrue(isArray(JavaTypes._boolean + arrayMarker()));
  }

  @Test
  public void testIsWildcard() {
    assertFalse(isWildcard(null));
    assertFalse(isWildcard(""));
    assertFalse(isWildcard(" "));
    assertFalse(isWildcard(JavaTypes._boolean));
    assertFalse(isWildcard(List.class.getName()));
    assertTrue(isWildcard(String.valueOf(JavaTypes.C_QUESTION_MARK)));
  }

  @Test
  public void testSubElement() {
    assertEquals("def", JavaTypes.subElement("abc   def   ghi", 3, 11));
    assertEquals("abc   def   ghi", JavaTypes.subElement("abc   def   ghi", 0, 15));
    assertEquals("def", JavaTypes.subElement("abc   def   ghi", 6, 9));
  }

  @Test
  @ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
  public void testCreateMethodIdentifier(IJavaEnvironment env) throws NoSuchMethodException {
    var context = new JavaBuilderContext(env);
    var type = env.requireType(ChildClass.class.getName());
    var method = type.methods().item(1).orElseThrow();
    assertEquals("methodInChildClass(java.lang.String,java.util.List<java.lang.Runnable>)", method.identifier(true));
    assertEquals("methodInChildClass(java.lang.String,java.util.List)", method.identifier());
    assertEquals("methodInChildClass(java.lang.String,java.util.List<java.lang.Runnable>)", method.toWorkingCopy().identifier(context, true));
    assertEquals("methodInChildClass(java.lang.String,java.util.List)", method.toWorkingCopy().identifier(context));
    assertEquals("methodInChildClass()", createMethodIdentifier("methodInChildClass", null));

    var arrayType = env.requireType(ClassWithArrayMethodParams.class.getName());
    var expectedForMethod1 = "method1(java.lang.String[])";
    assertEquals(expectedForMethod1, createMethodIdentifier(ClassWithArrayMethodParams.class.getMethod("method1", String[].class)));
    assertEquals(expectedForMethod1, arrayType.methods().withName("method1").first().orElseThrow().identifier());

    var method2 = arrayType.methods().withName("method2").first().orElseThrow();
    var expectedForMethod2 = "method2(java.lang.String[][][])";
    assertEquals(expectedForMethod2, method2.identifier());
    assertEquals(expectedForMethod2, method2.toWorkingCopy().identifier(context));
    assertEquals(expectedForMethod2, createMethodIdentifier(ClassWithArrayMethodParams.class.getMethod("method2", String[][][].class)));

    var method3 = arrayType.methods().withName("method3").first().orElseThrow();
    var expectedForMethod3 = "method3(java.lang.String[])";
    assertEquals(expectedForMethod3, method3.identifier());
    assertEquals(expectedForMethod3, method3.toWorkingCopy().identifier(context));
    assertEquals(expectedForMethod3, createMethodIdentifier(ClassWithArrayMethodParams.class.getMethod("method3", String[].class)));

    var method4 = arrayType.methods().withName("method4").first().orElseThrow();
    var expectedForMethod4 = "method4(java.util.List<java.lang.String[]>)";
    var expectedForMethod4Erasure = "method4(java.util.List)";
    assertEquals(expectedForMethod4, method4.identifier(true));
    assertEquals(expectedForMethod4, method4.toWorkingCopy().identifier(context, true));
    assertEquals(expectedForMethod4, createMethodIdentifier(ClassWithArrayMethodParams.class.getMethod("method4", List.class), true));
    assertEquals(expectedForMethod4Erasure, method4.identifier());
    assertEquals(expectedForMethod4Erasure, method4.toWorkingCopy().identifier(context));
    assertEquals(expectedForMethod4Erasure, createMethodIdentifier(ClassWithArrayMethodParams.class.getMethod("method4", List.class)));

    var expectedForMethod5 = "method5(boolean[])";
    assertEquals(expectedForMethod5, arrayType.methods().withName("method5").first().orElseThrow().identifier());
    assertEquals(expectedForMethod5, createMethodIdentifier(ClassWithArrayMethodParams.class.getMethod("method5", boolean[].class)));

    var method6 = arrayType.methods().withName("method6").first().orElseThrow();
    var method6_reflect = Arrays.stream(ClassWithArrayMethodParams.class.getMethods())
        .filter(m -> "method6".equals(m.getName()))
        .findFirst().orElseThrow();
    var expectedForMethod6 = "method6(X[])";
    assertEquals(expectedForMethod6, method6.identifier());
    assertEquals(expectedForMethod6, method6.toWorkingCopy().identifier(context));
    assertEquals(expectedForMethod6, createMethodIdentifier(method6_reflect, true));
    assertEquals("method6(java.lang.Object[])", createMethodIdentifier(method6_reflect));

    var method7 = arrayType.methods().withName("method7").first().orElseThrow();
    var expectedForMethod7 = "method7(java.util.List<java.util.Map<java.lang.String,java.lang.String[]>>)";
    var expectedForMethod7Erasure = "method7(java.util.List)";
    assertEquals(expectedForMethod7, method7.identifier(true));
    assertEquals(expectedForMethod7, method7.toWorkingCopy().identifier(context, true));
    assertEquals(expectedForMethod7, createMethodIdentifier(ClassWithArrayMethodParams.class.getMethod("method7", List.class), true));
    assertEquals(expectedForMethod7Erasure, method7.identifier());
    assertEquals(expectedForMethod7Erasure, method7.toWorkingCopy().identifier(context));
    assertEquals(expectedForMethod7Erasure, createMethodIdentifier(ClassWithArrayMethodParams.class.getMethod("method7", List.class)));
  }

  @Test
  public void testArrayMarker() {
    assertEquals("[]", arrayMarker());
    assertEquals("[][][][]", arrayMarker(4));
    assertEquals("", arrayMarker(0));
    assertEquals("", arrayMarker(-1));
  }

  @Test
  public void testErasure() {
    assertEquals("QList;", erasure("QList<QT;>;"));
    assertEquals("QList;", erasure("QList;"));
    assertEquals("QX;", erasure("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>;"));
    assertEquals("QX.Member;", erasure("QX<QObject;>.Member;"));
    assertEquals("QX.Member;", erasure("QX<QObject;>.Member<QObject;>;"));
    assertEquals("QX.Member;", erasure("QX.Member<QObject;>;"));
    assertEquals("QX.Member;", erasure("QX<QObject;>.Member<QList<QT;>;QMap<QU;QABC<QT;>;>;>;"));
    assertEquals("QX.Member;", erasure("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>.Member<QObject;>;"));
    assertEquals("X.Member", erasure("X<List<T>,Map<U,ABC<T>>>.Member<Object>"));
    var sign = "Ljava.util.List;";
    assertSame(sign, erasure(sign));
    assertEquals("java.util.List", erasure("java.util.List<-[java.lang.Number>"));
    assertEquals("java.util.List", erasure("java.util.List<? extends java.lang.Number>"));
    assertEquals("java.util.List", erasure("java.util.List<? super java.lang.Number>"));
    assertEquals("java.util.List", erasure("java.util.List<java.lang.String>"));

    assertEquals("java.util.List#myMethod(java.util.Set)", erasure("java.util.List<java.lang.Integer>#myMethod(java.util.Set<java.lang.String>)"));
  }

  @Test
  public void testQualifier() {
    assertEquals("java.lang", qualifier("java.lang.Object"));
    assertEquals("", qualifier(""));
    assertEquals("java.util", qualifier("java.util.List<java.lang.Object>"));
    assertEquals("org.eclipse.scout", qualifier("org.eclipse.scout.Blub$Inner$Inner"));
    assertEquals("Outer", qualifier("Outer.Inner"));
  }

  @Test
  public void testSimpleName() {
    assertEquals("Object", simpleName("java.lang.Object"));
    assertEquals("", simpleName(""));
    assertEquals("Entry", simpleName("java.util.Map$Entry"));
    assertEquals("List", simpleName("java.util.List<java.lang.String>"));
    assertEquals("Map", simpleName("java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"));
  }

  @Test
  public void testTypeArguments() {
    assertEquals(singletonList("T"), typeArguments("List<T>"));
    assertEquals(Arrays.asList("T", "U"), typeArguments("X<T,U>"));
    assertEquals(singletonList("*"), typeArguments("X<*>"));
    assertEquals(Arrays.asList("? extends E", "S"), typeArguments("X<? extends E,S>"));
    assertEquals(Arrays.asList("List<T>", "Map<U,ABC<T>>"), typeArguments("X<List<T>,   Map<U,ABC<T>>   >"));
    assertEquals(emptyList(), typeArguments("List"));
    assertEquals(emptyList(), typeArguments("X<Object>.Member"));
    assertEquals(singletonList("Object"), typeArguments("X<Object>.Member<Object>"));
    assertEquals(singletonList("Object"), typeArguments("X.Member<Object>"));
    assertEquals(Arrays.asList("List<T>", "Map<U,ABC<T>>"), typeArguments("X<Object>.Member<List<T>,Map<U,ABC<T>>>"));
    assertEquals(singletonList("Object"), typeArguments("X<List<T>,Map<U,ABC<T>>>.Member<Object>"));
  }
}
