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

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.CoreTestingUtils;
import org.eclipse.scout.sdk.core.fixture.MarkerAnnotation;
import org.eclipse.scout.sdk.core.fixture.TestAnnotation;
import org.eclipse.scout.sdk.core.fixture.ValueAnnot;
import org.eclipse.scout.sdk.core.testing.TestingUtils;
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
    ListOrderedSet/*<IAnnotation>*/ annotations = childClassType.getAnnotations();
    Assert.assertEquals(1, annotations.size());
    IAnnotation annotation = (IAnnotation) annotations.get(0);
    Assert.assertEquals(1, annotation.getValues().size());
    Assert.assertNotNull(annotation.getValue("values"));
    Assert.assertEquals(childClassType, annotation.getOwner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.getType().getName());

    // methodInChildClass annotation
    IMethod methodInChildClass = (IMethod) childClassType.getMethods().get(1);
    Assert.assertEquals(1, methodInChildClass.getAnnotations().size());
    annotation = (IAnnotation) methodInChildClass.getAnnotations().get(0);
    Assert.assertEquals(2, annotation.getValues().size());
    Assert.assertNotNull(annotation.getValue("values"));
    Assert.assertNotNull(annotation.getValue("en"));
    Assert.assertEquals(methodInChildClass, annotation.getOwner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.getType().getName());

    // firstCase annotation
    IMethod firstCase = (IMethod) childClassType.getMethods().get(2);
    Assert.assertEquals(1, firstCase.getAnnotations().size());
    annotation = (IAnnotation) firstCase.getAnnotations().get(0);
    Assert.assertEquals(1, annotation.getValues().size());
    Assert.assertNotNull(annotation.getValue("value"));
    Assert.assertEquals(firstCase, annotation.getOwner());
    Assert.assertEquals(SuppressWarnings.class.getName(), annotation.getType().getName());
  }

  @Test
  public void testAnnotationsWithAnnotationValues() {
    IType wildcardBaseClass = TestingUtils.getType("org.eclipse.scout.sdk.core.fixture.WildcardBaseClass", CoreTestingUtils.SOURCE_FOLDER);
    IAnnotation testAnnot = (IAnnotation) wildcardBaseClass.getAnnotations().get(0);
    IAnnotationValue value = testAnnot.getValue("inner");
    Assert.assertNotNull(value);
    Assert.assertTrue(value.getValue() instanceof IAnnotationValue[]);

    IAnnotationValue[] vals = (IAnnotationValue[]) value.getValue();
    Assert.assertEquals(2, vals.length);

    Object value0 = vals[0].getValue();
    Assert.assertTrue(value0 instanceof IAnnotation);
    IAnnotation annot0 = (IAnnotation) value0;
    Assert.assertEquals(wildcardBaseClass, annot0.getOwner());
    Assert.assertEquals(ValueAnnot.class.getName(), annot0.getType().getName());
    Assert.assertEquals("a", annot0.getValue("value").getValue());

    Object value1 = vals[1].getValue();
    Assert.assertTrue(value1 instanceof IAnnotation);
    IAnnotation annot1 = (IAnnotation) value1;
    Assert.assertEquals(wildcardBaseClass, annot1.getOwner());
    Assert.assertEquals(ValueAnnot.class.getName(), annot1.getType().getName());
    Assert.assertEquals("b", annot1.getValue("value").getValue());
  }

  @Test
  public void testToString() {
    IAnnotation annotation = (IAnnotation) ((IMethod) CoreTestingUtils.getChildClassType().getMethods().get(2)).getAnnotations().get(0);
    Assert.assertFalse(StringUtils.isBlank(annotation.toString()));

    annotation = (IAnnotation) CoreTestingUtils.getBaseClassType().getAnnotations().get(0);
    Assert.assertFalse(StringUtils.isBlank(annotation.toString()));
  }

  @Test
  public void testBaseClassAnnotations() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(baseClassType);

    // type annotation
    ListOrderedSet/*<IAnnotation>*/ annotations = baseClassType.getAnnotations();
    Assert.assertEquals(1, annotations.size());
    IAnnotation annotation = (IAnnotation) annotations.get(0);
    Assert.assertEquals(1, annotation.getValues().size());
    Assert.assertNotNull(annotation.getValue("values"));
    Assert.assertEquals(baseClassType, annotation.getOwner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.getType().getName());

    // methodInBaseClass annotation
    IMethod methodInBaseClass = (IMethod) baseClassType.getMethods().get(2);
    Assert.assertEquals(2, methodInBaseClass.getAnnotations().size());

    annotation = (IAnnotation) methodInBaseClass.getAnnotations().get(0);
    Assert.assertEquals(1, annotation.getValues().size());
    Assert.assertNotNull(annotation.getValue("values"));
    Assert.assertNull(annotation.getValue("en"));
    Assert.assertEquals(methodInBaseClass, annotation.getOwner());
    Assert.assertEquals(TestAnnotation.class.getName(), annotation.getType().getName());

    annotation = (IAnnotation) methodInBaseClass.getAnnotations().get(1);
    Assert.assertEquals(0, annotation.getValues().size());
    Assert.assertEquals(methodInBaseClass, annotation.getOwner());
    Assert.assertEquals(MarkerAnnotation.class.getName(), annotation.getType().getName());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testDeprecatedAnnotations() {
    IType deprChildType = TestingUtils.getType(org.eclipse.scout.sdk.core.fixture.DeprecatedChildClass.class.getName(), CoreTestingUtils.SOURCE_FOLDER);
    Assert.assertNotNull(deprChildType);

    IType deprBaseType = deprChildType.getSuperClass();

    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprChildType.getFlags());
    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, deprBaseType.getFlags());

    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, ((IMember) deprChildType.getMethods().get(1)).getFlags());
    Assert.assertEquals(Flags.AccPublic | Flags.AccDeprecated, ((IMember) deprBaseType.getMethods().get(1)).getFlags());
  }
}
