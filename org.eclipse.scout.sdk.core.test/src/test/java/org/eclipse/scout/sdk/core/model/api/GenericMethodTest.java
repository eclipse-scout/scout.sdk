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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.scout.sdk.core.fixture.ClassWithTypeParameters;
import org.eclipse.scout.sdk.core.fixture.ClassWithTypeParametersAsTypeVariables;
import org.eclipse.scout.sdk.core.fixture.ClassWithTypeVariables;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link GenericMethodTest}</h3>
 * <p>
 * Used classes are {@link ClassWithTypeParameters} {@link ClassWithTypeParametersAsTypeVariables}
 * {@link ClassWithTypeVariables}
 *
 * @since 5.1.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class GenericMethodTest {

  @Test
  public void testTypeVariables(IJavaEnvironment env) {
    var t = env.requireType(ClassWithTypeVariables.class.getName());

    assertEquals(0, t.typeArguments().count());

    assertEquals(2, t.typeParameters().count());
    var p0 = t.typeParameters().findAny().orElseThrow();
    var p1 = t.typeParameters().skip(1).findAny().orElseThrow();
    assertEquals("IN", p0.elementName());
    assertEquals("OUT", p1.elementName());

    var f = t.fields().withName("m_value").first().orElseThrow();
    assertEquals("OUT", f.dataType().name());

    var m = t.methods().withName("transform").first().orElseThrow();
    assertEquals("IN", m.parameters().first().orElseThrow().dataType().name());
    assertEquals("OUT", m.requireReturnType().name());
  }

  @Test
  public void testTypeParameters(IJavaEnvironment env) {
    var t = env.requireType(ClassWithTypeParameters.class.getName());

    assertEquals(0, t.typeParameters().count());
    assertEquals(0, t.typeArguments().count());

    assertFalse(t.fields().withName("m_value").first().isPresent());
    assertFalse(t.methods().withName("transform").first().isPresent());
  }

  @Test
  public void testTypeParametersInSyntheticSuperType(IJavaEnvironment env) {
    var t = env.requireType(ClassWithTypeParameters.class.getName()).requireSuperClass();

    assertEquals(2, t.typeArguments().count());
    var arg0 = t.typeArguments().findAny().orElseThrow();
    var arg1 = t.typeArguments().skip(1).findAny().orElseThrow();
    assertEquals("java.lang.Integer", arg0.name());
    assertEquals("java.lang.String", arg1.name());

    assertEquals(2, t.typeParameters().count());
    var p0 = t.typeParameters().findAny().orElseThrow();
    var p1 = t.typeParameters().skip(1).findAny().orElseThrow();
    assertEquals("IN", p0.elementName());
    assertEquals("OUT", p1.elementName());

    var f = t.fields().withName("m_value").first().orElseThrow();
    assertEquals("java.lang.String", f.dataType().name());

    var m = t.methods().withName("transform").first().orElseThrow();
    assertEquals("java.lang.Integer", m.parameters().first().orElseThrow().dataType().name());
    assertEquals("java.lang.String", m.requireReturnType().name());
  }

  @Test
  public void testTypeParametersAsTypeVariables(IJavaEnvironment env) {
    var t = env.requireType(ClassWithTypeParametersAsTypeVariables.class.getName());

    assertEquals(2, t.typeParameters().count());
    var p0 = t.typeParameters().findAny().orElseThrow();
    var p1 = t.typeParameters().skip(1).findAny().orElseThrow();
    assertEquals("A", p0.elementName());
    assertEquals("B", p1.elementName());

    assertEquals(0, t.typeArguments().count());

    assertFalse(t.fields().withName("m_value").first().isPresent());
    assertFalse(t.methods().withName("transform").first().isPresent());
  }

  @Test
  public void testTypeParametersAsTypeVariablesInSyntheticSuperType(IJavaEnvironment env) {
    var t = env.requireType(ClassWithTypeParametersAsTypeVariables.class.getName()).requireSuperClass();

    assertEquals(2, t.typeArguments().count());
    var arg0 = t.typeArguments().findAny().orElseThrow();
    var arg1 = t.typeArguments().skip(1).findAny().orElseThrow();
    assertEquals("A", arg0.name());
    assertEquals("B", arg1.name());

    assertEquals(2, t.typeParameters().count());
    var p0 = t.typeParameters().findAny().orElseThrow();
    var p1 = t.typeParameters().skip(1).findAny().orElseThrow();
    assertEquals("IN", p0.elementName());
    assertEquals("OUT", p1.elementName());

    var f = t.fields().withName("m_value").first().orElseThrow();
    assertEquals("B", f.dataType().name());

    var m = t.methods().withName("transform").first().orElseThrow();
    assertEquals("A", m.parameters().first().orElseThrow().dataType().name());
    assertEquals("B", m.requireReturnType().name());
  }
}
