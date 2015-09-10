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
    List<? extends IAnnotation> annotations = childClassType.getAnnotations();
    Assert.assertEquals(1, annotations.size());
    IAnnotation annotation = annotations.get(0);
    int nreal = 0, nsynth = 0;
    for (IAnnotationValue v : annotation.getValues().values()) {
      nreal += (v.isSyntheticDefaultValue() ? 0 : 1);
      nsynth += (v.isSyntheticDefaultValue() ? 1 : 0);
    }
    Assert.assertEquals(1, nreal);
    Assert.assertEquals(2, nsynth);
    Assert.assertNotNull(annotation.getValue("values"));
    Assert.assertEquals(childClassType, annotation.getOwner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.getType().getName());

    // methodInChildClass annotation
    IMethod methodInChildClass = childClassType.getMethods().get(1);
    Assert.assertEquals(1, methodInChildClass.getAnnotations().size());
    annotation = methodInChildClass.getAnnotations().get(0);
    nreal = 0;
    nsynth = 0;
    for (IAnnotationValue v : annotation.getValues().values()) {
      nreal += (v.isSyntheticDefaultValue() ? 0 : 1);
      nsynth += (v.isSyntheticDefaultValue() ? 1 : 0);
    }
    Assert.assertEquals(2, nreal);
    Assert.assertEquals(1, nsynth);
    Assert.assertNotNull(annotation.getValue("values"));
    Assert.assertNotNull(annotation.getValue("en"));
    Assert.assertEquals(methodInChildClass, annotation.getOwner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.getType().getName());

    // firstCase annotation
    IMethod firstCase = childClassType.getMethods().get(2);
    Assert.assertEquals(1, firstCase.getAnnotations().size());
    annotation = firstCase.getAnnotations().get(0);
    Assert.assertEquals(1, annotation.getValues().size());
    Assert.assertNotNull(annotation.getValue("value"));
    Assert.assertEquals(firstCase, annotation.getOwner());
    Assert.assertEquals(SuppressWarnings.class.getName(), annotation.getType().getName());
  }

  @Test
  public void testAnnotationsWithAnnotationValues() {
    IType wildcardBaseClass = CoreTestingUtils.createJavaEnvironment().findType("org.eclipse.scout.sdk.core.fixture.WildcardBaseClass");
    IAnnotation testAnnot = wildcardBaseClass.getAnnotations().get(0);
    IAnnotationValue value = testAnnot.getValue("inner");
    Assert.assertNotNull(value);
    Assert.assertTrue(value.getMetaValue().getType() == MetaValueType.Array);

    IMetaValue[] arr = ((IArrayMetaValue) value.getMetaValue()).getMetaValueArray();
    Assert.assertEquals(2, arr.length);

    IAnnotation annot0 = arr[0].getObject(IAnnotation.class);
    Assert.assertEquals(wildcardBaseClass, annot0.getOwner());
    Assert.assertEquals(ValueAnnot.class.getName(), annot0.getType().getName());
    Assert.assertEquals("a", annot0.getValue("value").getMetaValue().getObject(String.class));

    IAnnotation annot1 = arr[1].getObject(IAnnotation.class);
    Assert.assertEquals(wildcardBaseClass, annot1.getOwner());
    Assert.assertEquals(ValueAnnot.class.getName(), annot1.getType().getName());
    Assert.assertEquals("b", annot1.getValue("value").getMetaValue().getObject(String.class));
  }

  @Test
  public void testToString() {
    IAnnotation annotation = CoreTestingUtils.getChildClassType().getMethods().get(2).getAnnotations().get(0);
    Assert.assertFalse(StringUtils.isBlank(annotation.toString()));

    annotation = CoreTestingUtils.getBaseClassType().getAnnotations().get(0);
    Assert.assertFalse(StringUtils.isBlank(annotation.toString()));
  }

  @Test
  public void testBaseClassAnnotations() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    // type annotation
    List<? extends IAnnotation> annotations = baseClassType.getAnnotations();
    Assert.assertEquals(1, annotations.size());
    IAnnotation annotation = annotations.get(0);
    int nreal = 0, nsynth = 0;
    for (IAnnotationValue v : annotation.getValues().values()) {
      nreal += (v.isSyntheticDefaultValue() ? 0 : 1);
      nsynth += (v.isSyntheticDefaultValue() ? 1 : 0);
    }
    Assert.assertEquals(1, nreal);
    Assert.assertEquals(2, nsynth);
    Assert.assertNotNull(annotation.getValue("values"));
    Assert.assertEquals(baseClassType, annotation.getOwner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.getType().getName());

    // methodInBaseClass annotation
    IMethod methodInBaseClass = baseClassType.getMethods().get(0);
    Assert.assertEquals(2, methodInBaseClass.getAnnotations().size());

    annotation = methodInBaseClass.getAnnotations().get(0);
    nreal = 0;
    nsynth = 0;
    for (IAnnotationValue v : annotation.getValues().values()) {
      nreal += (v.isSyntheticDefaultValue() ? 0 : 1);
      nsynth += (v.isSyntheticDefaultValue() ? 1 : 0);
    }
    Assert.assertEquals(1, nreal);
    Assert.assertEquals(2, nsynth);
    Assert.assertNotNull(annotation.getValue("values"));
    Assert.assertNotNull(annotation.getValue("en"));//default value TestEnum.A
    Assert.assertEquals(methodInBaseClass, annotation.getOwner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.getType().getName());

    annotation = methodInBaseClass.getAnnotations().get(1);
    Assert.assertEquals(0, annotation.getValues().size());
    Assert.assertEquals(methodInBaseClass, annotation.getOwner());
    Assert.assertEquals(MarkerAnnotation.class.getName(), annotation.getType().getName());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testDeprecatedAnnotations() {
    IType deprChildType = CoreTestingUtils.createJavaEnvironment().findType(org.eclipse.scout.sdk.core.fixture.DeprecatedChildClass.class.getName());
    Assert.assertNotNull(deprChildType);

    IType deprBaseType = deprChildType.getSuperClass();

    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprChildType.getFlags());
    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprBaseType.getFlags());

    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprChildType.getMethods().get(0).getFlags());
    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprBaseType.getMethods().get(0).getFlags());
  }
}
