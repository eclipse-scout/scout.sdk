/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.MarkerAnnotation;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation;
import org.eclipse.scout.sdk.core.fixture.ValueAnnot;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;

@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class AnnotationTest {
  @Test
  public void testChildClassAnnotations(IJavaEnvironment env) {
    var childClassType = env.requireType(ChildClass.class.getName());

    // type annotation
    var annotations = childClassType.annotations().stream().toList();
    assertEquals(1, annotations.size());
    var annotation = annotations.get(0);
    var nreal = 0;
    var nsynth = 0;
    for (var v : annotation.elements().values()) {
      nreal += (v.isDefault() ? 0 : 1);
      nsynth += (v.isDefault() ? 1 : 0);
    }
    assertEquals(1, nreal);
    assertEquals(2, nsynth);
    assertTrue(annotation.element("values").isPresent());
    assertEquals(childClassType, annotation.owner());
    assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    // methodInChildClass annotation
    var methodInChildClass = childClassType.methods().item(1).orElseThrow();
    assertEquals(1, methodInChildClass.annotations().stream().count());
    annotation = methodInChildClass.annotations().first().orElseThrow();
    nreal = 0;
    nsynth = 0;
    for (var v : annotation.elements().values()) {
      nreal += (v.isDefault() ? 0 : 1);
      nsynth += (v.isDefault() ? 1 : 0);
    }
    assertEquals(2, nreal);
    assertEquals(1, nsynth);
    assertTrue(annotation.element("values").isPresent());
    assertTrue(annotation.element("en").isPresent());
    assertEquals(methodInChildClass, annotation.owner());
    assertEquals(TestAnnotation.class.getName(), annotation.type().name());
    assertEquals("@TestAnnotation(values = Long.class, en = TestEnum.A)", annotation.toWorkingCopy().toJavaSource().toString());

    // firstCase annotation
    var firstCase = childClassType.methods().item(2).orElseThrow();
    assertEquals(1, firstCase.annotations().stream().count());
    annotation = firstCase.annotations().first().orElseThrow();
    assertEquals(1, annotation.elements().size());
    assertTrue(annotation.element("value").isPresent());
    assertEquals(firstCase, annotation.owner());
    assertEquals(SuppressWarnings.class.getName(), annotation.type().name());
  }

  @Test
  public void testAnnotationsWithAnnotationValues(IJavaEnvironment env) {
    var wildcardBaseClass = env.requireType("org.eclipse.scout.sdk.core.fixture.WildcardBaseClass");
    var testAnnot = wildcardBaseClass.annotations().first().orElseThrow();
    var value = testAnnot.element("inner").orElseThrow();
    assertNotNull(value);
    assertSame(value.value().type(), MetaValueType.Array);

    var arr = ((IArrayMetaValue) value.value()).metaValueArray();
    assertEquals(2, arr.length);

    var annot0 = arr[0].as(IAnnotation.class);
    assertEquals(wildcardBaseClass, annot0.owner());
    assertEquals(ValueAnnot.class.getName(), annot0.type().name());
    assertEquals("a", annot0.element("value").orElseThrow().value().as(String.class));

    var annot1 = arr[1].as(IAnnotation.class);
    assertEquals(wildcardBaseClass, annot1.owner());
    assertEquals(ValueAnnot.class.getName(), annot1.type().name());
    assertEquals("b", annot1.element("value").orElseThrow().value().as(String.class));
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    var annotation = env.requireType(ChildClass.class.getName()).methods().item(2).orElseThrow().annotations().first().orElseThrow();
    assertFalse(Strings.isBlank(annotation.toString()));

    annotation = env.requireType(ChildClass.class.getName()).requireSuperClass().annotations().first().orElseThrow();
    assertFalse(Strings.isBlank(annotation.toString()));
  }

  @Test
  public void testBaseClassAnnotations(IJavaEnvironment env) {
    var baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();

    // type annotation
    var annotations = baseClassType.annotations().stream().toList();
    assertEquals(1, annotations.size());
    var annotation = annotations.get(0);
    var nreal = 0;
    var nsynth = 0;
    for (var v : annotation.elements().values()) {
      nreal += (v.isDefault() ? 0 : 1);
      nsynth += (v.isDefault() ? 1 : 0);
    }
    assertEquals(1, nreal);
    assertEquals(2, nsynth);
    assertTrue(annotation.element("values").isPresent());
    assertEquals(baseClassType, annotation.owner());
    assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    // methodInBaseClass annotation
    var methodInBaseClass = baseClassType.methods().first().orElseThrow();
    assertEquals(2, methodInBaseClass.annotations().stream().count());

    annotation = methodInBaseClass.annotations().first().orElseThrow();
    nreal = 0;
    nsynth = 0;
    for (var v : annotation.elements().values()) {
      nreal += (v.isDefault() ? 0 : 1);
      nsynth += (v.isDefault() ? 1 : 0);
    }
    assertEquals(1, nreal);
    assertEquals(2, nsynth);
    assertTrue(annotation.element("values").isPresent());
    assertTrue(annotation.element("en").isPresent());//default value TestEnum.A
    assertEquals(methodInBaseClass, annotation.owner());
    assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    annotation = methodInBaseClass.annotations().item(1).orElseThrow();
    assertEquals(0, annotation.elements().size());
    assertEquals(methodInBaseClass, annotation.owner());
    assertEquals(MarkerAnnotation.class.getName(), annotation.type().name());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testDeprecatedAnnotations(IJavaEnvironment env) {
    var deprChildType = env.requireType(org.eclipse.scout.sdk.core.fixture.DeprecatedChildClass.class.getName());
    var deprBaseType = deprChildType.requireSuperClass();

    assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprChildType.flags());
    assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprBaseType.flags());

    assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprChildType.methods().first().orElseThrow().flags());
    assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprBaseType.methods().first().orElseThrow().flags());
  }

  @Test
  public void testGetAnnotation(IJavaEnvironment env) {
    assertNotNull(env.requireType(ChildClass.class.getName()).requireSuperClass().methods().first().orElseThrow().annotations().withName(MarkerAnnotation.class.getName()).first());
  }
}
