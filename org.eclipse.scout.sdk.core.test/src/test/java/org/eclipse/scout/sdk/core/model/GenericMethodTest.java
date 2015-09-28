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

import org.eclipse.scout.sdk.core.fixture.ClassWithTypeParameters;
import org.eclipse.scout.sdk.core.fixture.ClassWithTypeParametersAsTypeVariables;
import org.eclipse.scout.sdk.core.fixture.ClassWithTypeVariables;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link GenericMethodTest}</h3>
 * <p>
 * Used classes are {@link ClassWithTypeParameters} {@link ClassWithTypeParametersAsTypeVariables}
 * {@link ClassWithTypeVariables}
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class GenericMethodTest {

  @Test
  public void testTypeVariables() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeVariables.class.getName());

    Assert.assertEquals(0, t.typeArguments().size());

    Assert.assertEquals(2, t.typeParameters().size());
    ITypeParameter p0 = t.typeParameters().get(0);
    ITypeParameter p1 = t.typeParameters().get(1);
    Assert.assertEquals("IN", p0.elementName());
    Assert.assertEquals("OUT", p1.elementName());

    IField f = t.fields().withName("m_value").first();
    Assert.assertEquals("OUT", f.dataType().name());

    IMethod m = t.methods().withName("transform").first();
    Assert.assertEquals("IN", m.parameters().first().dataType().name());
    Assert.assertEquals("OUT", m.returnType().name());
  }

  @Test
  public void testTypeParameters() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParameters.class.getName());

    Assert.assertEquals(0, t.typeParameters().size());

    Assert.assertEquals(0, t.typeArguments().size());

    IField f = t.fields().withName("m_value").first();
    Assert.assertNull(f);

    IMethod m = t.methods().withName("transform").first();
    Assert.assertNull(m);
  }

  @Test
  public void testTypeParametersInSyntheticSuperType() {//this is a parameterized type
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParameters.class.getName()).superClass();

    Assert.assertEquals(2, t.typeArguments().size());
    IType arg0 = t.typeArguments().get(0);
    IType arg1 = t.typeArguments().get(1);
    Assert.assertEquals("java.lang.Integer", arg0.name());
    Assert.assertEquals("java.lang.String", arg1.name());

    Assert.assertEquals(2, t.typeParameters().size());
    ITypeParameter p0 = t.typeParameters().get(0);
    ITypeParameter p1 = t.typeParameters().get(1);
    Assert.assertEquals("IN", p0.elementName());
    Assert.assertEquals("OUT", p1.elementName());

    IField f = t.fields().withName("m_value").first();
    Assert.assertEquals("java.lang.String", f.dataType().name());

    IMethod m = t.methods().withName("transform").first();
    Assert.assertEquals("java.lang.Integer", m.parameters().first().dataType().name());
    Assert.assertEquals("java.lang.String", m.returnType().name());
  }

  @Test
  public void testTypeParametersInOriginalSuperType() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParameters.class.getName()).superClass().originalType();

    Assert.assertEquals(0, t.typeArguments().size());

    Assert.assertEquals(2, t.typeParameters().size());
    ITypeParameter p0 = t.typeParameters().get(0);
    ITypeParameter p1 = t.typeParameters().get(1);
    Assert.assertEquals("IN", p0.elementName());
    Assert.assertEquals("OUT", p1.elementName());

    IField f = t.fields().withName("m_value").first();
    Assert.assertEquals("OUT", f.dataType().name());

    IMethod m = t.methods().withName("transform").first();
    Assert.assertEquals("IN", m.parameters().first().dataType().name());
    Assert.assertEquals("OUT", m.returnType().name());
  }

  @Test
  public void testTypeParametersAsTypeVariables() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParametersAsTypeVariables.class.getName());

    Assert.assertEquals(2, t.typeParameters().size());
    ITypeParameter p0 = t.typeParameters().get(0);
    ITypeParameter p1 = t.typeParameters().get(1);
    Assert.assertEquals("A", p0.elementName());
    Assert.assertEquals("B", p1.elementName());

    Assert.assertEquals(0, t.typeArguments().size());

    IField f = t.fields().withName("m_value").first();
    Assert.assertNull(f);

    IMethod m = t.methods().withName("transform").first();
    Assert.assertNull(m);
  }

  @Test
  public void testTypeParametersAsTypeVariablesInSyntheticSuperType() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParametersAsTypeVariables.class.getName()).superClass();

    Assert.assertEquals(2, t.typeArguments().size());
    IType arg0 = t.typeArguments().get(0);
    IType arg1 = t.typeArguments().get(1);
    Assert.assertEquals("A", arg0.name());
    Assert.assertEquals("B", arg1.name());

    Assert.assertEquals(2, t.typeParameters().size());
    ITypeParameter p0 = t.typeParameters().get(0);
    ITypeParameter p1 = t.typeParameters().get(1);
    Assert.assertEquals("IN", p0.elementName());
    Assert.assertEquals("OUT", p1.elementName());

    IField f = t.fields().withName("m_value").first();
    Assert.assertEquals("B", f.dataType().name());

    IMethod m = t.methods().withName("transform").first();
    Assert.assertEquals("A", m.parameters().first().dataType().name());
    Assert.assertEquals("B", m.returnType().name());
  }

  @Test
  public void testTypeParametersAsTypeVariablesInOriginalSuperType() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParametersAsTypeVariables.class.getName()).superClass().originalType();

    Assert.assertEquals(0, t.typeArguments().size());

    Assert.assertEquals(2, t.typeParameters().size());
    ITypeParameter p0 = t.typeParameters().get(0);
    ITypeParameter p1 = t.typeParameters().get(1);
    Assert.assertEquals("IN", p0.elementName());
    Assert.assertEquals("OUT", p1.elementName());

    IField f = t.fields().withName("m_value").first();
    Assert.assertEquals("OUT", f.dataType().name());

    IMethod m = t.methods().withName("transform").first();
    Assert.assertEquals("IN", m.parameters().first().dataType().name());
    Assert.assertEquals("OUT", m.returnType().name());
  }

}
