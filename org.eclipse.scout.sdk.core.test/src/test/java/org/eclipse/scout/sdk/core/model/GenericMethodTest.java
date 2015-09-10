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
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link GenericMethodTest}</h3>
 * <p>
 * Used classes are {@link ClassWithTypeParameters} {@link ClassWithTypeParametersAsTypeVariables}
 * {@link ClassWithTypeVariables}
 *
 * @author imo
 * @since 5.1.0
 */
public class GenericMethodTest {

  @Test
  public void testTypeVariables() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeVariables.class.getName());

    Assert.assertEquals(0, t.getTypeArguments().size());

    Assert.assertEquals(2, t.getTypeParameters().size());
    ITypeParameter p0 = t.getTypeParameters().get(0);
    ITypeParameter p1 = t.getTypeParameters().get(1);
    Assert.assertEquals("IN", p0.getElementName());
    Assert.assertEquals("OUT", p1.getElementName());

    IField f = CoreUtils.getField(t, "m_value");
    Assert.assertEquals("OUT", f.getDataType().getName());

    IMethod m = CoreUtils.getMethod(t, "transform");
    Assert.assertEquals("IN", m.getParameters().get(0).getDataType().getName());
    Assert.assertEquals("OUT", m.getReturnType().getName());
  }

  @Test
  public void testTypeParameters() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParameters.class.getName());

    Assert.assertEquals(0, t.getTypeParameters().size());

    Assert.assertEquals(0, t.getTypeArguments().size());

    IField f = CoreUtils.getField(t, "m_value");
    Assert.assertNull(f);

    IMethod m = CoreUtils.getMethod(t, "transform");
    Assert.assertNull(m);
  }

  @Test
  public void testTypeParametersInSyntheticSuperType() {//this is a parameterized type
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParameters.class.getName()).getSuperClass();

    Assert.assertEquals(2, t.getTypeArguments().size());
    IType arg0 = t.getTypeArguments().get(0);
    IType arg1 = t.getTypeArguments().get(1);
    Assert.assertEquals("java.lang.Integer", arg0.getName());
    Assert.assertEquals("java.lang.String", arg1.getName());

    Assert.assertEquals(2, t.getTypeParameters().size());
    ITypeParameter p0 = t.getTypeParameters().get(0);
    ITypeParameter p1 = t.getTypeParameters().get(1);
    Assert.assertEquals("IN", p0.getElementName());
    Assert.assertEquals("OUT", p1.getElementName());

    IField f = CoreUtils.getField(t, "m_value");
    Assert.assertEquals("java.lang.String", f.getDataType().getName());

    IMethod m = CoreUtils.getMethod(t, "transform");
    Assert.assertEquals("java.lang.Integer", m.getParameters().get(0).getDataType().getName());
    Assert.assertEquals("java.lang.String", m.getReturnType().getName());
  }

  @Test
  public void testTypeParametersInOriginalSuperType() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParameters.class.getName()).getSuperClass().getOriginalType();

    Assert.assertEquals(0, t.getTypeArguments().size());

    Assert.assertEquals(2, t.getTypeParameters().size());
    ITypeParameter p0 = t.getTypeParameters().get(0);
    ITypeParameter p1 = t.getTypeParameters().get(1);
    Assert.assertEquals("IN", p0.getElementName());
    Assert.assertEquals("OUT", p1.getElementName());

    IField f = CoreUtils.getField(t, "m_value");
    Assert.assertEquals("OUT", f.getDataType().getName());

    IMethod m = CoreUtils.getMethod(t, "transform");
    Assert.assertEquals("IN", m.getParameters().get(0).getDataType().getName());
    Assert.assertEquals("OUT", m.getReturnType().getName());
  }

  @Test
  public void testTypeParametersAsTypeVariables() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParametersAsTypeVariables.class.getName());

    Assert.assertEquals(2, t.getTypeParameters().size());
    ITypeParameter p0 = t.getTypeParameters().get(0);
    ITypeParameter p1 = t.getTypeParameters().get(1);
    Assert.assertEquals("A", p0.getElementName());
    Assert.assertEquals("B", p1.getElementName());

    Assert.assertEquals(0, t.getTypeArguments().size());

    IField f = CoreUtils.getField(t, "m_value");
    Assert.assertNull(f);

    IMethod m = CoreUtils.getMethod(t, "transform");
    Assert.assertNull(m);
  }

  @Test
  public void testTypeParametersAsTypeVariablesInSyntheticSuperType() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParametersAsTypeVariables.class.getName()).getSuperClass();

    Assert.assertEquals(2, t.getTypeArguments().size());
    IType arg0 = t.getTypeArguments().get(0);
    IType arg1 = t.getTypeArguments().get(1);
    Assert.assertEquals("A", arg0.getName());
    Assert.assertEquals("B", arg1.getName());

    Assert.assertEquals(2, t.getTypeParameters().size());
    ITypeParameter p0 = t.getTypeParameters().get(0);
    ITypeParameter p1 = t.getTypeParameters().get(1);
    Assert.assertEquals("IN", p0.getElementName());
    Assert.assertEquals("OUT", p1.getElementName());

    IField f = CoreUtils.getField(t, "m_value");
    Assert.assertEquals("B", f.getDataType().getName());

    IMethod m = CoreUtils.getMethod(t, "transform");
    Assert.assertEquals("A", m.getParameters().get(0).getDataType().getName());
    Assert.assertEquals("B", m.getReturnType().getName());
  }

  @Test
  public void testTypeParametersAsTypeVariablesInOriginalSuperType() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    IType t = env.findType(ClassWithTypeParametersAsTypeVariables.class.getName()).getSuperClass().getOriginalType();

    Assert.assertEquals(0, t.getTypeArguments().size());

    Assert.assertEquals(2, t.getTypeParameters().size());
    ITypeParameter p0 = t.getTypeParameters().get(0);
    ITypeParameter p1 = t.getTypeParameters().get(1);
    Assert.assertEquals("IN", p0.getElementName());
    Assert.assertEquals("OUT", p1.getElementName());

    IField f = CoreUtils.getField(t, "m_value");
    Assert.assertEquals("OUT", f.getDataType().getName());

    IMethod m = CoreUtils.getMethod(t, "transform");
    Assert.assertEquals("IN", m.getParameters().get(0).getDataType().getName());
    Assert.assertEquals("OUT", m.getReturnType().getName());
  }

}
