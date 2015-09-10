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

import java.math.RoundingMode;

import javax.annotation.Generated;

import org.eclipse.scout.sdk.core.TypeNames;
import org.eclipse.scout.sdk.core.annotation.javax_annotation_Generated;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithDefaultValues;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithShortValueForIntField;
import org.eclipse.scout.sdk.core.fixture.ClassWithAnnotationWithSingleValues;
import org.eclipse.scout.sdk.core.fixture.ClassWithScalarGeneratedAnnotation;
import org.eclipse.scout.sdk.core.fixture.org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues;
import org.eclipse.scout.sdk.core.fixture.org_eclipse_scout_sdk_core_fixture_AnnotationWithSingleValues;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.sugar.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test managed annotation wrapper {@link AbstractManagedAnnotation}
 */
public class ManagedAnnotationTest {

//  @AnnotationWithSingleValues(type = String.class, enumValue = RoundingMode.HALF_UP, num = Integer.MIN_VALUE, string = "alpha", anno = @Generated("g1") )

  @Test
  public void testDefaultValuesWhenUsed() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();
    IType t = env.findType(ClassWithAnnotationWithDefaultValues.class.getName());
    org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues a = t.annotations().withManagedWrapper(org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues.class).first();
    Assert.assertEquals(1, a.num());
    Assert.assertEquals("one", a.string());
    Assert.assertEquals(env.findType(RoundingMode.class.getName()).fields().withName("HALF_UP").first(), a.enumValue());
    Assert.assertEquals(env.findType(String.class.getName()), a.type());
    Assert.assertEquals(env.findType(Generated.class.getName()), a.anno().getType());
    Assert.assertEquals("g", a.anno().getValue("value").getMetaValue().getObject(String.class));
  }

  @Test
  public void testDefaultValuesWhenNotUsed() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();
    IType t = env.findType(ClassWithAnnotationWithSingleValues.class.getName());
    org_eclipse_scout_sdk_core_fixture_AnnotationWithSingleValues a = t.annotations().withManagedWrapper(org_eclipse_scout_sdk_core_fixture_AnnotationWithSingleValues.class).first();
    Assert.assertEquals(Integer.MIN_VALUE, a.num());
    Assert.assertEquals("alpha", a.string());
    Assert.assertEquals(env.findType(RoundingMode.class.getName()).fields().withName("HALF_UP").first(), a.enumValue());
    Assert.assertEquals(env.findType(String.class.getName()), a.type());
    Assert.assertEquals(env.findType(Generated.class.getName()), a.anno().getType());
    Assert.assertEquals("g1", a.anno().getValue("value").getMetaValue().getObject(String.class));
  }

  @Test
  public void testDefaultValueOverrideWithNullWhenUsed() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();
    IType t = env.findType(ClassWithAnnotationWithDefaultValues.class.getName());
    org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues a = t.annotations().withManagedWrapper(org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues.class).first();
    Assert.assertEquals(0, a.num(0));
    Assert.assertEquals(null, a.string((String) null));
    Assert.assertEquals(null, a.enumValue((IField) null));
    Assert.assertEquals(null, a.type((IType) null));
    Assert.assertEquals(null, a.anno((IAnnotation) null));
  }

  @Test
  public void testDefaultValueOverrideWithNullWhenNotUsed() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();
    IType t = env.findType(ClassWithAnnotationWithSingleValues.class.getName());
    org_eclipse_scout_sdk_core_fixture_AnnotationWithSingleValues a = t.annotations().withManagedWrapper(org_eclipse_scout_sdk_core_fixture_AnnotationWithSingleValues.class).first();
    Assert.assertEquals(Integer.MIN_VALUE, a.num(0));
    Assert.assertEquals("alpha", a.string((String) null));
    Assert.assertEquals(env.findType(RoundingMode.class.getName()).fields().withName("HALF_UP").first(), a.enumValue((IField) null));
    Assert.assertEquals(env.findType(String.class.getName()), a.type((IType) null));
    Assert.assertEquals(env.findType(Generated.class.getName()), a.anno((IAnnotation) null).getType());
    Assert.assertEquals("g1", a.anno((IAnnotation) null).getValue("value").getMetaValue().getObject(String.class));
  }

  @Test
  public void testDefaultValueOverrideWithNonNullWhenUsed() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();
    IType t = env.findType(ClassWithAnnotationWithDefaultValues.class.getName());
    org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues a = t.annotations().withManagedWrapper(org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues.class).first();
    Assert.assertEquals(5, a.num(5));
    Assert.assertEquals("two", a.string("two"));
    Assert.assertEquals(env.findType(RoundingMode.class.getName()).fields().withName("HALF_EVEN").first(), a.enumValue(env.findType(RoundingMode.class.getName()).fields().withName("HALF_EVEN").first()));
    Assert.assertEquals(env.findType(TypeNames.java_lang_Integer), a.type(env.findType(TypeNames.java_lang_Integer)));
  }

  @Test
  public void testDefaultValueOverrideWithNonNullWhenNotUsed() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();
    IType t = env.findType(ClassWithAnnotationWithSingleValues.class.getName());
    org_eclipse_scout_sdk_core_fixture_AnnotationWithSingleValues a = t.annotations().withManagedWrapper(org_eclipse_scout_sdk_core_fixture_AnnotationWithSingleValues.class).first();
    Assert.assertEquals(Integer.MIN_VALUE, a.num(5));
    Assert.assertEquals("alpha", a.string("two"));
    Assert.assertEquals(env.findType(RoundingMode.class.getName()).fields().withName("HALF_UP").first(), a.enumValue(env.findType(RoundingMode.class.getName()).fields().withName("HALF_UP").first()));
    Assert.assertEquals(env.findType(String.class.getName()), a.type(env.findType(TypeNames.java_lang_Integer)));
  }

  /**
   * enum as string instead of IField
   * <p>
   * type string instead of IType
   * <p>
   * int read from Integer.class instead of int
   */
  @Test
  public void testValueCoercion() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();
    IType t = env.findType(ClassWithAnnotationWithDefaultValues.class.getName());
    org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues a = t.annotations().withManagedWrapper(org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues.class).first();
    Assert.assertEquals(1, a.numFromBoxedType());
    Assert.assertEquals("HALF_UP", a.enumValueCoercedToString());
    Assert.assertEquals(String.class.getName(), a.typeCoercedToString());
  }

  /**
   * annotation declares array, but usage only sets a scalar value, which is allowed by java
   * <p>
   * <code>@Generated("g")</code> and <code>@Generated({"a","b","c"})</code>
   */
  @Test
  public void testArrayCoercion() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();
    IType t = env.findType(ClassWithScalarGeneratedAnnotation.class.getName());
    javax_annotation_Generated a = t.annotations().withManagedWrapper(javax_annotation_Generated.class).first();
    Assert.assertArrayEquals(new String[]{"g"}, a.value());
  }

  /**
   * annotation declares int, but usage is short
   * <p>
   * <code>@Generated("g")</code> and <code>@Generated({"a","b","c"})</code>
   */
  @Test
  public void testNumberCoercion() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();
    IType t = env.findType(ClassWithAnnotationWithShortValueForIntField.class.getName());
    org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues a = t.annotations().withManagedWrapper(org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues.class).first();
    Assert.assertEquals(new Integer(4), a.unwrap().getValue("num").getMetaValue().getObject(int.class));
    Assert.assertTrue(4 == a.num());
//    Assert.assertEquals(new String[]{"g"}, );
  }

}
