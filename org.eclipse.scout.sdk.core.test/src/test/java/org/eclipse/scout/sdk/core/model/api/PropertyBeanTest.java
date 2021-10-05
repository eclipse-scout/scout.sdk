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

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.fixture.PropertyTestClass;
import org.eclipse.scout.sdk.core.fixture.PropertyTestClass2;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link PropertyBeanTest}</h3>
 *
 * @since 6.1.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
@SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsWithItself", "ConstantConditions", "EqualsBetweenInconvertibleTypes", "unlikely-arg-type"})
public class PropertyBeanTest {
  @Test
  public void testPropertyBean(IJavaEnvironment env) {
    var propTestClass = env.requireType(PropertyTestClass.class.getName());
    var propTestClass2 = env.requireType(PropertyTestClass2.class.getName());
    var propertyBeans = PropertyBean.of(propTestClass)
        .sorted(comparing(PropertyBean::name))
        .collect(toList());

    assertEquals(5, propertyBeans.size());

    var aloneProp = propertyBeans.get(0);
    assertEquals("Alone", aloneProp.name());
    assertEquals(String.class.getName(), aloneProp.type().name());
    assertEquals(propTestClass, aloneProp.declaringType());
    assertFalse(aloneProp.readMethod().isPresent());
    assertTrue(aloneProp.writeMethod().isPresent());

    var falseProp = propertyBeans.get(1);
    assertEquals("False", falseProp.name());
    assertEquals(JavaTypes.Boolean, falseProp.type().name());
    assertEquals(propTestClass, falseProp.declaringType());
    assertTrue(falseProp.readMethod().isPresent());
    assertTrue(falseProp.writeMethod().isPresent());

    var onlyProp = propertyBeans.get(2);
    assertEquals("Only", onlyProp.name());
    assertEquals(JavaTypes.Integer, onlyProp.type().name());
    assertEquals(propTestClass, onlyProp.declaringType());
    assertTrue(onlyProp.readMethod().isPresent());
    assertFalse(onlyProp.writeMethod().isPresent());

    var stringProp = propertyBeans.get(3);
    assertEquals("String", stringProp.name());
    assertEquals(String.class.getName(), stringProp.type().name());
    assertEquals(propTestClass, stringProp.declaringType());
    assertTrue(stringProp.readMethod().isPresent());
    assertTrue(stringProp.writeMethod().isPresent());

    var trueProp = propertyBeans.get(4);
    assertEquals("True", trueProp.name());
    assertEquals(JavaTypes._boolean, trueProp.type().name());
    assertEquals(propTestClass, trueProp.declaringType());
    assertTrue(trueProp.readMethod().isPresent());
    assertTrue(trueProp.writeMethod().isPresent());

    var string2Prop = PropertyBean.of(propTestClass2).findAny().orElseThrow();
    assertEquals(PropertyTestClass2.class.getName() + '#' + "String", string2Prop.toString());

    assertFalse(aloneProp.equals(stringProp));
    assertFalse(string2Prop.equals(stringProp));
    assertTrue(aloneProp.equals(aloneProp));
    assertFalse(aloneProp.equals(trueProp));
    assertFalse(aloneProp.equals(""));
    assertFalse(aloneProp.equals(null));

    assertNotEquals(aloneProp.hashCode(), stringProp.hashCode());
    assertNotEquals(string2Prop.hashCode(), stringProp.hashCode());
    assertEquals(aloneProp.hashCode(), aloneProp.hashCode());

    var num = PropertyBean.of(propTestClass)
        .filter(b -> Character.isLowerCase(b.type().elementName().charAt(0)))
        .count();
    assertEquals(1, num);
  }

  @Test
  public void testDataTypeOf(IJavaEnvironment env) {
    var t = env.requireType(PropertyTestClass.class.getName());
    assertEquals(String.class.getName(), PropertyBean.dataTypeOf(t.methods().withName("getString").first().orElseThrow()).orElseThrow().name());
    assertEquals(String.class.getName(), PropertyBean.dataTypeOf(t.methods().withName("setString").first().orElseThrow()).orElseThrow().name());

    assertEquals(JavaTypes._boolean, PropertyBean.dataTypeOf(t.methods().withName("isTrue").first().orElseThrow()).orElseThrow().name());
    assertEquals(JavaTypes._boolean, PropertyBean.dataTypeOf(t.methods().withName("setTrue").first().orElseThrow()).orElseThrow().name());

    assertEquals(String.class.getName(), PropertyBean.dataTypeOf(t.methods().withName("setAlone").first().orElseThrow()).orElseThrow().name());
    assertFalse(PropertyBean.dataTypeOf(t.methods().withName("doAnything").first().orElseThrow()).isPresent());
    assertFalse(PropertyBean.dataTypeOf(null).isPresent());
  }

  @Test
  public void testGetterPrefixFor() {
    assertEquals("is", PropertyBean.getterPrefixFor(JavaTypes._boolean));
    assertEquals("get", PropertyBean.getterPrefixFor(JavaTypes.Boolean)); // must be "get" for java bean specification compliance!
    assertEquals("get", PropertyBean.getterPrefixFor(Object.class.getName()));
    assertEquals("get", PropertyBean.getterPrefixFor(null));
  }
}
