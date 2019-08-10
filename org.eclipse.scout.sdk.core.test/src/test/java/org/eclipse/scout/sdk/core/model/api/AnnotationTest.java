/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.fixture.MarkerAnnotation;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation;
import org.eclipse.scout.sdk.core.fixture.ValueAnnot;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class AnnotationTest {
  @Test
  public void testChildClassAnnotations(IJavaEnvironment env) {
    IType childClassType = env.requireType(ChildClass.class.getName());

    // type annotation
    List<IAnnotation> annotations = childClassType.annotations().stream().collect(toList());
    assertEquals(1, annotations.size());
    IAnnotation annotation = annotations.get(0);
    int nreal = 0;
    int nsynth = 0;
    for (IAnnotationElement v : annotation.elements().values()) {
      nreal += (v.isDefault() ? 0 : 1);
      nsynth += (v.isDefault() ? 1 : 0);
    }
    assertEquals(1, nreal);
    assertEquals(2, nsynth);
    assertTrue(annotation.element("values").isPresent());
    assertEquals(childClassType, annotation.owner());
    assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    // methodInChildClass annotation
    IMethod methodInChildClass = childClassType.methods().item(1).get();
    assertEquals(1, methodInChildClass.annotations().stream().count());
    annotation = methodInChildClass.annotations().first().get();
    nreal = 0;
    nsynth = 0;
    for (IAnnotationElement v : annotation.elements().values()) {
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
    IMethod firstCase = childClassType.methods().item(2).get();
    assertEquals(1, firstCase.annotations().stream().count());
    annotation = firstCase.annotations().first().get();
    assertEquals(1, annotation.elements().size());
    assertTrue(annotation.element("value").isPresent());
    assertEquals(firstCase, annotation.owner());
    assertEquals(SuppressWarnings.class.getName(), annotation.type().name());
  }

  @Test
  public void testAnnotationsWithAnnotationValues(IJavaEnvironment env) {
    IType wildcardBaseClass = env.requireType("org.eclipse.scout.sdk.core.fixture.WildcardBaseClass");
    IAnnotation testAnnot = wildcardBaseClass.annotations().first().get();
    IAnnotationElement value = testAnnot.element("inner").get();
    assertNotNull(value);
    assertSame(value.value().type(), MetaValueType.Array);

    IMetaValue[] arr = ((IArrayMetaValue) value.value()).metaValueArray();
    assertEquals(2, arr.length);

    IAnnotation annot0 = arr[0].as(IAnnotation.class);
    assertEquals(wildcardBaseClass, annot0.owner());
    assertEquals(ValueAnnot.class.getName(), annot0.type().name());
    assertEquals("a", annot0.element("value").get().value().as(String.class));

    IAnnotation annot1 = arr[1].as(IAnnotation.class);
    assertEquals(wildcardBaseClass, annot1.owner());
    assertEquals(ValueAnnot.class.getName(), annot1.type().name());
    assertEquals("b", annot1.element("value").get().value().as(String.class));
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    IAnnotation annotation = env.requireType(ChildClass.class.getName()).methods().item(2).get().annotations().first().get();
    assertFalse(Strings.isBlank(annotation.toString()));

    annotation = env.requireType(ChildClass.class.getName()).requireSuperClass().annotations().first().get();
    assertFalse(Strings.isBlank(annotation.toString()));
  }

  @Test
  public void testBaseClassAnnotations(IJavaEnvironment env) {
    IType baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();

    // type annotation
    List<IAnnotation> annotations = baseClassType.annotations().stream().collect(toList());
    assertEquals(1, annotations.size());
    IAnnotation annotation = annotations.get(0);
    int nreal = 0;
    int nsynth = 0;
    for (IAnnotationElement v : annotation.elements().values()) {
      nreal += (v.isDefault() ? 0 : 1);
      nsynth += (v.isDefault() ? 1 : 0);
    }
    assertEquals(1, nreal);
    assertEquals(2, nsynth);
    assertTrue(annotation.element("values").isPresent());
    assertEquals(baseClassType, annotation.owner());
    assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    // methodInBaseClass annotation
    IMethod methodInBaseClass = baseClassType.methods().first().get();
    assertEquals(2, methodInBaseClass.annotations().stream().count());

    annotation = methodInBaseClass.annotations().first().get();
    nreal = 0;
    nsynth = 0;
    for (IAnnotationElement v : annotation.elements().values()) {
      nreal += (v.isDefault() ? 0 : 1);
      nsynth += (v.isDefault() ? 1 : 0);
    }
    assertEquals(1, nreal);
    assertEquals(2, nsynth);
    assertTrue(annotation.element("values").isPresent());
    assertTrue(annotation.element("en").isPresent());//default value TestEnum.A
    assertEquals(methodInBaseClass, annotation.owner());
    assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    annotation = methodInBaseClass.annotations().item(1).get();
    assertEquals(0, annotation.elements().size());
    assertEquals(methodInBaseClass, annotation.owner());
    assertEquals(MarkerAnnotation.class.getName(), annotation.type().name());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testDeprecatedAnnotations(IJavaEnvironment env) {
    IType deprChildType = env.requireType(org.eclipse.scout.sdk.core.fixture.DeprecatedChildClass.class.getName());
    IType deprBaseType = deprChildType.requireSuperClass();

    assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprChildType.flags());
    assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprBaseType.flags());

    assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprChildType.methods().first().get().flags());
    assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprBaseType.methods().first().get().flags());
  }

  @Test
  public void testGetAnnotation(IJavaEnvironment env) {
    assertNotNull(env.requireType(ChildClass.class.getName()).requireSuperClass().methods().first().get().annotations().withName(MarkerAnnotation.class.getName()).first());
  }
}
