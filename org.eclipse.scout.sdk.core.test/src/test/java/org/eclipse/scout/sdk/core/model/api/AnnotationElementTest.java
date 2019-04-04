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
package org.eclipse.scout.sdk.core.model.api;

import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class AnnotationElementTest {
  @Test
  public void testChildClassAnnotationValues(IJavaEnvironment env) {
    IType childClassType = env.requireType(ChildClass.class.getName());

    // ChildClass Annotation
    assertEquals(3, childClassType.annotations().first().get().elements().size());

    IAnnotationElement inner = childClassType.annotations().first().get().element("inner").get();
    assertArrayEquals(new Object[]{}, inner.value().as(Object[].class));
    assertEquals(MetaValueType.Array, inner.value().type());

    IAnnotationElement testAnnotValues = childClassType.annotations().first().get().element("values").get();
    assertEquals("values", testAnnotValues.elementName());
    assertEquals(MetaValueType.Array, testAnnotValues.value().type());
    assertEquals(childClassType.annotations().first().get(), testAnnotValues.declaringAnnotation());

    IArrayMetaValue arrValue = (IArrayMetaValue) testAnnotValues.value();
    IMetaValue[] arr = arrValue.metaValueArray();
    assertEquals(2, arr.length);
    assertEquals("{ Serializable.class, Runnable.class }", arrValue.toString());

    assertEquals(MetaValueType.Type, arr[0].type());
    assertEquals(Serializable.class.getName(), arr[0].as(IType.class).name());

    assertEquals(MetaValueType.Type, arr[1].type());
    assertEquals(Runnable.class.getName(), arr[1].as(IType.class).name());

    // methodInChildClass annotation values
    Map<String, IAnnotationElement> methodInChildClassValueMap = childClassType.methods().item(1).get().annotations().first().get().elements();
    assertArrayEquals(new String[]{"values", "en", "inner"}, methodInChildClassValueMap.keySet().toArray());
    IAnnotationElement methodInChildClassValue1 = methodInChildClassValueMap.get("values");
    assertEquals("values = Long.class", methodInChildClassValue1.toWorkingCopy().toSource(identity(), new BuilderContext()).toString());

    assertEquals("values", methodInChildClassValue1.elementName());
    assertEquals(MetaValueType.Type, methodInChildClassValue1.value().type());
    assertEquals(childClassType.methods().item(1).get().annotations().first().get(), methodInChildClassValue1.declaringAnnotation());
    assertEquals(org.eclipse.scout.sdk.core.fixture.Long.class.getName(), methodInChildClassValue1.value().as(IType.class).name());
    assertEquals("Long.class", methodInChildClassValue1.sourceOfExpression().get().asCharSequence().toString());

    IAnnotationElement methodInChildClassValue2 = methodInChildClassValueMap.get("en");
    assertEquals("en", methodInChildClassValue2.elementName());
    assertEquals(MetaValueType.Enum, methodInChildClassValue2.value().type());
    assertEquals(childClassType.methods().item(1).get().annotations().first().get(), methodInChildClassValue2.declaringAnnotation());
    assertEquals("A", methodInChildClassValue2.value().as(IField.class).elementName());

    IAnnotationElement methodInChildClassValue3 = methodInChildClassValueMap.get("inner");
    assertTrue(methodInChildClassValue3.isDefault());
    assertEquals("", methodInChildClassValue3.toWorkingCopy().toSource(identity(), new BuilderContext()).toString());

    // firstCase annotation value
    IAnnotationElement suppressWarningValue = childClassType.methods().item(2).get().annotations().first().get().element("value").get();
    assertEquals("value", suppressWarningValue.elementName());
    assertEquals(MetaValueType.String, suppressWarningValue.value().type());
    assertEquals(childClassType.methods().item(2).get().annotations().first().get(), suppressWarningValue.declaringAnnotation());
    assertEquals("unused", suppressWarningValue.value().as(String.class));
  }

  @Test
  public void testToString(IJavaEnvironment env) {
    List<IAnnotationElement> values = new ArrayList<>(env.requireType(ChildClass.class.getName()).methods().item(1).get().annotations().first().get().elements().values());
    IAnnotationElement methodInChildClassValue1 = values.get(0);
    assertFalse(Strings.isBlank(methodInChildClassValue1.toString()));

    IAnnotationElement testAnnotValues = env.requireType(ChildClass.class.getName()).requireSuperClass().annotations().first().get().element("values").get();
    assertFalse(Strings.isBlank(testAnnotValues.toString()));
  }

  @Test
  public void testBaseClassAnnotationValues(IJavaEnvironment env) {
    IType baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();

    // BaseClass annotation
    IAnnotationElement testAnnotValues = baseClassType.annotations().first().get().element("values").get();
    assertEquals("values", testAnnotValues.elementName());
    assertEquals(MetaValueType.Array, testAnnotValues.value().type());
    assertEquals(baseClassType.annotations().first().get(), testAnnotValues.declaringAnnotation());

    IMetaValue[] arr = ((IArrayMetaValue) testAnnotValues.value()).metaValueArray();
    assertEquals(2, arr.length);

    assertEquals(MetaValueType.Type, arr[0].type());
    assertEquals(Serializable.class.getName(), arr[0].as(IType.class).name());

    assertEquals(MetaValueType.Type, arr[1].type());
    assertEquals(Runnable.class.getName(), arr[1].as(IType.class).name());

    // methodInBaseClass annotation
    IAnnotatable methodInBaseClass = baseClassType.methods().first().get();
    testAnnotValues = methodInBaseClass.annotations().first().get().element("values").get();
    assertEquals("values", testAnnotValues.elementName());
    assertEquals(MetaValueType.Array, testAnnotValues.value().type());
    assertEquals(methodInBaseClass.annotations().first().get(), testAnnotValues.declaringAnnotation());

    arr = ((IArrayMetaValue) testAnnotValues.value()).metaValueArray();
    assertEquals(2, arr.length);

    assertEquals(MetaValueType.Type, arr[0].type());
    assertEquals(Serializable.class.getName(), arr[0].as(IType.class).name());

    assertEquals(MetaValueType.Type, arr[1].type());
    assertEquals(Runnable.class.getName(), arr[1].as(IType.class).name());
  }
}
