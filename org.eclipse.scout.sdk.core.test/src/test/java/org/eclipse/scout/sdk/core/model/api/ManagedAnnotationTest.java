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
package org.eclipse.scout.sdk.core.model.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.RoundingMode;

import javax.annotation.Generated;

import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithArrayValues;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithDefaultValues;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithShortValueForIntField;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithSingleValues;
import org.eclipse.scout.sdk.core.fixture.ClassWithScalarGeneratedAnnotation;
import org.eclipse.scout.sdk.core.fixture.ValueAnnot;
import org.eclipse.scout.sdk.core.fixture.managed.AnnotationWithArrayValues;
import org.eclipse.scout.sdk.core.fixture.managed.AnnotationWithDefaultValues;
import org.eclipse.scout.sdk.core.fixture.managed.AnnotationWithSingleValues;
import org.eclipse.scout.sdk.core.model.annotation.GeneratedAnnotation;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;

/**
 * Test managed annotation wrapper {@link AbstractManagedAnnotation}
 */
@SuppressWarnings("HtmlTagCanBeJavadocTag")
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class ManagedAnnotationTest {

  @Test
  public void testDefaultValuesWhenUsed(IJavaEnvironment env) {
    var t = env.requireType(ClassWithAnnotationWithDefaultValues.class.getName());
    var a = t.annotations().withManagedWrapper(AnnotationWithDefaultValues.class).first().orElseThrow();
    assertEquals(1, a.num());
    assertEquals("one", a.string());
    assertEquals(env.requireType(RoundingMode.class.getName()).fields().withName("HALF_UP").first().orElseThrow(), a.enumValue());
    assertEquals(env.requireType(String.class.getName()), a.type());
    assertEquals(env.requireType(Generated.class.getName()), a.anno().type());
    assertEquals("g", a.anno().element("value").orElseThrow().value().as(String.class));
  }

  @Test
  public void testDefaultValuesWhenNotUsed(IJavaEnvironment env) {
    var t = env.requireType(ClassWithAnnotationWithSingleValues.class.getName());
    var a = t.annotations().withManagedWrapper(AnnotationWithSingleValues.class).first().orElseThrow();
    assertEquals(Integer.MIN_VALUE, a.num());
    assertEquals("alpha", a.string());
    assertEquals(env.requireType(RoundingMode.class.getName()).fields().withName("HALF_UP").first().orElseThrow(), a.enumValue());
    assertEquals(env.requireType(String.class.getName()), a.type());
    assertEquals(env.requireType(ValueAnnot.class.getName()), a.anno().type());
    assertEquals("g1", a.anno().element("value").orElseThrow().value().as(String.class));
  }

  @Test
  public void testWithAnnotationArrayValue(IJavaEnvironment env) {
    var t = env.requireType(ClassWithAnnotationWithArrayValues.class.getName());
    var annot = t.annotations().withManagedWrapper(AnnotationWithArrayValues.class).first().orElseThrow().annos();
    assertEquals(2, annot.length);

    assertEquals(JavaTypes.Integer, annot[1].type().name());
    assertEquals(12, annot[1].num());
    assertArrayEquals(new String[]{"g2"}, annot[1].anno().wrap(GeneratedAnnotation.class).value());
  }

  @Test
  public void testDefaultValueOverrideWithNullWhenUsed(IJavaEnvironment env) {
    var t = env.requireType(ClassWithAnnotationWithDefaultValues.class.getName());
    var a = t.annotations().withManagedWrapper(AnnotationWithDefaultValues.class).first().orElseThrow();
    assertEquals(0, a.num(0));
    assertNull(a.string((String) null));
    assertNull(a.enumValue((IField) null));
    assertNull(a.type((IType) null));
    assertNull(a.anno((IAnnotation) null));
  }

  @Test
  public void testDefaultValueOverrideWithNullWhenNotUsed(IJavaEnvironment env) {
    var t = env.requireType(ClassWithAnnotationWithSingleValues.class.getName());
    var a = t.annotations().withManagedWrapper(AnnotationWithSingleValues.class).first().orElseThrow();
    assertEquals(Integer.MIN_VALUE, a.num(0));
    assertEquals("alpha", a.string((String) null));
    assertEquals(env.requireType(RoundingMode.class.getName()).fields().withName("HALF_UP").first().orElseThrow(), a.enumValue((IField) null));
    assertEquals(env.requireType(String.class.getName()), a.type((IType) null));
    assertEquals(env.requireType(ValueAnnot.class.getName()), a.anno((IAnnotation) null).type());
    assertEquals("g1", a.anno((IAnnotation) null).element("value").orElseThrow().value().as(String.class));
    assertArrayEquals(new String[]{"g1"}, a.generated().value());
  }

  @Test
  public void testDefaultValueOverrideWithNonNullWhenUsed(IJavaEnvironment env) {
    var t = env.requireType(ClassWithAnnotationWithDefaultValues.class.getName());
    var a = t.annotations().withManagedWrapper(AnnotationWithDefaultValues.class).first().orElseThrow();
    assertEquals(5, a.num(5));
    assertEquals("two", a.string("two"));
    assertEquals(env.requireType(RoundingMode.class.getName()).fields().withName("HALF_EVEN").first().orElseThrow(),
        a.enumValue(env.requireType(RoundingMode.class.getName()).fields().withName("HALF_EVEN").first().orElseThrow()));
    assertEquals(env.requireType(JavaTypes.Integer), a.type(env.requireType(JavaTypes.Integer)));
  }

  @Test
  public void testDefaultValueOverrideWithNonNullWhenNotUsed(IJavaEnvironment env) {
    var t = env.requireType(ClassWithAnnotationWithSingleValues.class.getName());
    var a = t.annotations().withManagedWrapper(AnnotationWithSingleValues.class).first().orElseThrow();
    assertEquals(Integer.MIN_VALUE, a.num(5));
    assertEquals("alpha", a.string("two"));
    assertEquals(env.requireType(RoundingMode.class.getName()).fields().withName("HALF_UP").first().orElseThrow(),
        a.enumValue(env.requireType(RoundingMode.class.getName()).fields().withName("HALF_UP").first().orElseThrow()));
    assertEquals(env.requireType(String.class.getName()), a.type(env.requireType(JavaTypes.Integer)));
  }

  /**
   * enum as string instead of IField
   * <p>
   * type string instead of IType
   * <p>
   * int read from Integer.class instead of int
   */
  @Test
  public void testValueCoercion(IJavaEnvironment env) {
    var t = env.requireType(ClassWithAnnotationWithDefaultValues.class.getName());
    var a = t.annotations().withManagedWrapper(AnnotationWithDefaultValues.class).first().orElseThrow();
    assertEquals(1, a.numFromBoxedType());
    assertEquals("HALF_UP", a.enumValueCoercedToString());
    assertEquals(String.class.getName(), a.typeCoercedToString());
  }

  /**
   * annotation declares array, but usage only sets a scalar value, which is allowed by java
   * <p>
   * {@code @Generated("g")} and <code>@Generated({"a","b","c"})</code>
   */
  @Test
  public void testArrayCoercion(IJavaEnvironment env) {
    var t = env.requireType(ClassWithScalarGeneratedAnnotation.class.getName());
    var a = t.annotations().withManagedWrapper(GeneratedAnnotation.class).first().orElseThrow();
    assertArrayEquals(new String[]{"g"}, a.value());
  }

  /**
   * annotation declares int, but usage is short
   * <p>
   * {@code @Generated("g")} and <code>@Generated({"a","b","c"})</code>
   */
  @Test
  public void testNumberCoercion(IJavaEnvironment env) {
    var t = env.requireType(ClassWithAnnotationWithShortValueForIntField.class.getName());
    var a = t.annotations().withManagedWrapper(AnnotationWithDefaultValues.class).first().orElseThrow();
    assertEquals(Integer.valueOf(4), a.unwrap().element("num").orElseThrow().value().as(int.class));
    assertEquals(4, a.num());
  }
}
