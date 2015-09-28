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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.fixture.MarkerAnnotation;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation;
import org.eclipse.scout.sdk.core.fixture.ValueAnnot;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.api.IArrayMetaValue;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class AnnotationTest {
  @Test
  public void testChildClassAnnotations() {
    IType childClassType = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClassType);

    // type annotation
    List<IAnnotation> annotations = childClassType.annotations().list();
    Assert.assertEquals(1, annotations.size());
    IAnnotation annotation = annotations.get(0);
    int nreal = 0, nsynth = 0;
    for (IAnnotationValue v : annotation.values().values()) {
      nreal += (v.isDefaultValue() ? 0 : 1);
      nsynth += (v.isDefaultValue() ? 1 : 0);
    }
    Assert.assertEquals(1, nreal);
    Assert.assertEquals(2, nsynth);
    Assert.assertNotNull(annotation.value("values"));
    Assert.assertEquals(childClassType, annotation.owner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    // methodInChildClass annotation
    IMethod methodInChildClass = childClassType.methods().list().get(1);
    Assert.assertEquals(1, methodInChildClass.annotations().list().size());
    annotation = methodInChildClass.annotations().first();
    nreal = 0;
    nsynth = 0;
    for (IAnnotationValue v : annotation.values().values()) {
      nreal += (v.isDefaultValue() ? 0 : 1);
      nsynth += (v.isDefaultValue() ? 1 : 0);
    }
    Assert.assertEquals(2, nreal);
    Assert.assertEquals(1, nsynth);
    Assert.assertNotNull(annotation.value("values"));
    Assert.assertNotNull(annotation.value("en"));
    Assert.assertEquals(methodInChildClass, annotation.owner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    // firstCase annotation
    IMethod firstCase = childClassType.methods().list().get(2);
    Assert.assertEquals(1, firstCase.annotations().list().size());
    annotation = firstCase.annotations().first();
    Assert.assertEquals(1, annotation.values().size());
    Assert.assertNotNull(annotation.value("value"));
    Assert.assertEquals(firstCase, annotation.owner());
    Assert.assertEquals(SuppressWarnings.class.getName(), annotation.type().name());
  }

  @Test
  public void testAnnotationsWithAnnotationValues() {
    IType wildcardBaseClass = CoreTestingUtils.createJavaEnvironment().findType("org.eclipse.scout.sdk.core.fixture.WildcardBaseClass");
    IAnnotation testAnnot = wildcardBaseClass.annotations().first();
    IAnnotationValue value = testAnnot.value("inner");
    Assert.assertNotNull(value);
    Assert.assertTrue(value.metaValue().type() == MetaValueType.Array);

    IMetaValue[] arr = ((IArrayMetaValue) value.metaValue()).metaValueArray();
    Assert.assertEquals(2, arr.length);

    IAnnotation annot0 = arr[0].get(IAnnotation.class);
    Assert.assertEquals(wildcardBaseClass, annot0.owner());
    Assert.assertEquals(ValueAnnot.class.getName(), annot0.type().name());
    Assert.assertEquals("a", annot0.value("value").metaValue().get(String.class));

    IAnnotation annot1 = arr[1].get(IAnnotation.class);
    Assert.assertEquals(wildcardBaseClass, annot1.owner());
    Assert.assertEquals(ValueAnnot.class.getName(), annot1.type().name());
    Assert.assertEquals("b", annot1.value("value").metaValue().get(String.class));
  }

  @Test
  public void testToString() {
    IAnnotation annotation = CoreTestingUtils.getChildClassType().methods().list().get(2).annotations().first();
    Assert.assertFalse(StringUtils.isBlank(annotation.toString()));

    annotation = CoreTestingUtils.getBaseClassType().annotations().first();
    Assert.assertFalse(StringUtils.isBlank(annotation.toString()));
  }

  @Test
  public void testBaseClassAnnotations() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    // type annotation
    List<IAnnotation> annotations = baseClassType.annotations().list();
    Assert.assertEquals(1, annotations.size());
    IAnnotation annotation = annotations.get(0);
    int nreal = 0, nsynth = 0;
    for (IAnnotationValue v : annotation.values().values()) {
      nreal += (v.isDefaultValue() ? 0 : 1);
      nsynth += (v.isDefaultValue() ? 1 : 0);
    }
    Assert.assertEquals(1, nreal);
    Assert.assertEquals(2, nsynth);
    Assert.assertNotNull(annotation.value("values"));
    Assert.assertEquals(baseClassType, annotation.owner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    // methodInBaseClass annotation
    IMethod methodInBaseClass = baseClassType.methods().list().get(0);
    Assert.assertEquals(2, methodInBaseClass.annotations().list().size());

    annotation = methodInBaseClass.annotations().first();
    nreal = 0;
    nsynth = 0;
    for (IAnnotationValue v : annotation.values().values()) {
      nreal += (v.isDefaultValue() ? 0 : 1);
      nsynth += (v.isDefaultValue() ? 1 : 0);
    }
    Assert.assertEquals(1, nreal);
    Assert.assertEquals(2, nsynth);
    Assert.assertNotNull(annotation.value("values"));
    Assert.assertNotNull(annotation.value("en"));//default value TestEnum.A
    Assert.assertEquals(methodInBaseClass, annotation.owner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    annotation = methodInBaseClass.annotations().list().get(1);
    Assert.assertEquals(0, annotation.values().size());
    Assert.assertEquals(methodInBaseClass, annotation.owner());
    Assert.assertEquals(MarkerAnnotation.class.getName(), annotation.type().name());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testDeprecatedAnnotations() {
    IType deprChildType = CoreTestingUtils.createJavaEnvironment().findType(org.eclipse.scout.sdk.core.fixture.DeprecatedChildClass.class.getName());
    Assert.assertNotNull(deprChildType);

    IType deprBaseType = deprChildType.superClass();

    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprChildType.flags());
    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprBaseType.flags());

    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprChildType.methods().list().get(0).flags());
    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprBaseType.methods().list().get(0).flags());
  }
}
