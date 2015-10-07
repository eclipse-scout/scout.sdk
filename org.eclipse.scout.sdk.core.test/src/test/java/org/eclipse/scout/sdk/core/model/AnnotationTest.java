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
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
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
    for (IAnnotationElement v : annotation.elements().values()) {
      nreal += (v.isDefault() ? 0 : 1);
      nsynth += (v.isDefault() ? 1 : 0);
    }
    Assert.assertEquals(1, nreal);
    Assert.assertEquals(2, nsynth);
    Assert.assertNotNull(annotation.element("values"));
    Assert.assertEquals(childClassType, annotation.owner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    // methodInChildClass annotation
    IMethod methodInChildClass = childClassType.methods().list().get(1);
    Assert.assertEquals(1, methodInChildClass.annotations().list().size());
    annotation = methodInChildClass.annotations().first();
    nreal = 0;
    nsynth = 0;
    for (IAnnotationElement v : annotation.elements().values()) {
      nreal += (v.isDefault() ? 0 : 1);
      nsynth += (v.isDefault() ? 1 : 0);
    }
    Assert.assertEquals(2, nreal);
    Assert.assertEquals(1, nsynth);
    Assert.assertNotNull(annotation.element("values"));
    Assert.assertNotNull(annotation.element("en"));
    Assert.assertEquals(methodInChildClass, annotation.owner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    // firstCase annotation
    IMethod firstCase = childClassType.methods().list().get(2);
    Assert.assertEquals(1, firstCase.annotations().list().size());
    annotation = firstCase.annotations().first();
    Assert.assertEquals(1, annotation.elements().size());
    Assert.assertNotNull(annotation.element("value"));
    Assert.assertEquals(firstCase, annotation.owner());
    Assert.assertEquals(SuppressWarnings.class.getName(), annotation.type().name());
  }

  @Test
  public void testAnnotationsWithAnnotationValues() {
    IType wildcardBaseClass = CoreTestingUtils.createJavaEnvironment().findType("org.eclipse.scout.sdk.core.fixture.WildcardBaseClass");
    IAnnotation testAnnot = wildcardBaseClass.annotations().first();
    IAnnotationElement value = testAnnot.element("inner");
    Assert.assertNotNull(value);
    Assert.assertTrue(value.value().type() == MetaValueType.Array);

    IMetaValue[] arr = ((IArrayMetaValue) value.value()).metaValueArray();
    Assert.assertEquals(2, arr.length);

    IAnnotation annot0 = arr[0].get(IAnnotation.class);
    Assert.assertEquals(wildcardBaseClass, annot0.owner());
    Assert.assertEquals(ValueAnnot.class.getName(), annot0.type().name());
    Assert.assertEquals("a", annot0.element("value").value().get(String.class));

    IAnnotation annot1 = arr[1].get(IAnnotation.class);
    Assert.assertEquals(wildcardBaseClass, annot1.owner());
    Assert.assertEquals(ValueAnnot.class.getName(), annot1.type().name());
    Assert.assertEquals("b", annot1.element("value").value().get(String.class));
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
    for (IAnnotationElement v : annotation.elements().values()) {
      nreal += (v.isDefault() ? 0 : 1);
      nsynth += (v.isDefault() ? 1 : 0);
    }
    Assert.assertEquals(1, nreal);
    Assert.assertEquals(2, nsynth);
    Assert.assertNotNull(annotation.element("values"));
    Assert.assertEquals(baseClassType, annotation.owner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    // methodInBaseClass annotation
    IMethod methodInBaseClass = baseClassType.methods().list().get(0);
    Assert.assertEquals(2, methodInBaseClass.annotations().list().size());

    annotation = methodInBaseClass.annotations().first();
    nreal = 0;
    nsynth = 0;
    for (IAnnotationElement v : annotation.elements().values()) {
      nreal += (v.isDefault() ? 0 : 1);
      nsynth += (v.isDefault() ? 1 : 0);
    }
    Assert.assertEquals(1, nreal);
    Assert.assertEquals(2, nsynth);
    Assert.assertNotNull(annotation.element("values"));
    Assert.assertNotNull(annotation.element("en"));//default value TestEnum.A
    Assert.assertEquals(methodInBaseClass, annotation.owner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.type().name());

    annotation = methodInBaseClass.annotations().list().get(1);
    Assert.assertEquals(0, annotation.elements().size());
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
