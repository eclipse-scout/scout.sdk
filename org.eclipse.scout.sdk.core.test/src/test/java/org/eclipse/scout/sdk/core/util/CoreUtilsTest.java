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
package org.eclipse.scout.sdk.core.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.eclipse.scout.sdk.core.TypeNames;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel1;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel2;
import org.eclipse.scout.sdk.core.fixture.MarkerAnnotation;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class CoreUtilsTest {
  @Test
  public void testGetInnerTypes() {
    List<IType> innerTypes = CoreUtils.getInnerTypes(CoreTestingUtils.getBaseClassType(), TypeFilters.flags(Flags.AccStatic), TypeComparators.typeNameComparator());
    Assert.assertEquals(1, innerTypes.size());
  }

  @Test
  public void testToStringLiteral() {
    Assert.assertEquals("\"a\\nb\"", CoreUtils.toStringLiteral("a\nb"));
    Assert.assertEquals("\"a\\\"b\"", CoreUtils.toStringLiteral("a\"b"));
  }

  @Test
  public void testFromStringLiteral() {
    Assert.assertNull(CoreUtils.fromStringLiteral(null));
    Assert.assertNull(CoreUtils.fromStringLiteral("a"));
    Assert.assertEquals("a\nb", CoreUtils.fromStringLiteral("\"a\\nb\""));
    Assert.assertEquals("a\"b", CoreUtils.fromStringLiteral("\"a\\\"b\""));
  }

  @Test
  public void testFindInnerType() {
    Assert.assertEquals(CoreTestingUtils.getBaseClassType().getTypes().get(0), CoreUtils.findInnerType(CoreTestingUtils.getBaseClassType(), "InnerClass1"));
  }

  @Test
  public void testGetAllSuperInterfaces() {
    Assert.assertEquals(2, CoreUtils.getAllSuperInterfaces(CoreTestingUtils.getBaseClassType()).size());
  }

  @Test
  public void testIsGenericType() {
    Assert.assertTrue(CoreUtils.isGenericType(CoreTestingUtils.getChildClassType()));
    Assert.assertTrue(CoreUtils.isGenericType(CoreTestingUtils.getBaseClassType()));
    Assert.assertFalse(CoreUtils.isGenericType(CoreTestingUtils.getBaseClassType().getTypes().get(0)));
  }

  @Test
  public void testGetFields() {
    Assert.assertEquals(1, CoreUtils.getFields(CoreTestingUtils.getChildClassType(), FieldFilters.name("m_test")).size());
  }

  @Test
  public void testGetResolvedTypeParamValueSignature() {
    List<String> resolvedTypeParamValueSignature = CoreUtils.getResolvedTypeParamValueSignature(CoreTestingUtils.getChildClassType(), InterfaceLevel1.class.getName(), 0);
    Assert.assertEquals(1, resolvedTypeParamValueSignature.size());
    Assert.assertEquals(Signature.createTypeSignature(org.eclipse.scout.sdk.core.fixture.Long.class.getName()), resolvedTypeParamValueSignature.get(0));

    resolvedTypeParamValueSignature = CoreUtils.getResolvedTypeParamValueSignature(CoreTestingUtils.getChildClassType(), BaseClass.class.getName(), 0);
    Assert.assertEquals(3, resolvedTypeParamValueSignature.size());
    Assert.assertEquals(Signature.createTypeSignature(AbstractList.class.getName() + "<" + String.class.getName() + ">"), resolvedTypeParamValueSignature.get(0));
    Assert.assertEquals(Signature.createTypeSignature(Runnable.class.getName()), resolvedTypeParamValueSignature.get(1));
    Assert.assertEquals(Signature.createTypeSignature(Serializable.class.getName()), resolvedTypeParamValueSignature.get(2));
  }

  @Test
  public void testFindMethodInSuperHierarchy() {
    IMethod methodInBaseClass = CoreUtils.findMethodInSuperHierarchy(CoreTestingUtils.getChildClassType(), MethodFilters.annotationName(MarkerAnnotation.class.getName()));
    Assert.assertNotNull(methodInBaseClass);
    Assert.assertEquals("methodInBaseClass", methodInBaseClass.getElementName());
  }

  @Test
  public void testFindInnerTypeInSuperHierarchy() {
    IType innerTypeInSuperClass = CoreUtils.findInnerTypeInSuperHierarchy(CoreTestingUtils.getChildClassType(), TypeFilters.simpleName("InnerClass2"));
    Assert.assertNotNull(innerTypeInSuperClass);
    Assert.assertEquals("org.eclipse.scout.sdk.core.fixture.BaseClass$InnerClass2", innerTypeInSuperClass.getName());
  }

  @Test
  public void testEnsureStartWithLowerCase() {
    Assert.assertEquals(null, CoreUtils.ensureStartWithLowerCase(null));
    Assert.assertEquals("", CoreUtils.ensureStartWithLowerCase(""));
    Assert.assertEquals("  ", CoreUtils.ensureStartWithLowerCase("  "));
    Assert.assertEquals("a", CoreUtils.ensureStartWithLowerCase("a"));
    Assert.assertEquals("ab", CoreUtils.ensureStartWithLowerCase("ab"));
    Assert.assertEquals("a", CoreUtils.ensureStartWithLowerCase("A"));
    Assert.assertEquals("ab", CoreUtils.ensureStartWithLowerCase("Ab"));
    Assert.assertEquals("aBC", CoreUtils.ensureStartWithLowerCase("ABC"));
  }

  @Test
  public void testEnsureStartWithUpperCase() {
    Assert.assertEquals(null, CoreUtils.ensureStartWithUpperCase(null));
    Assert.assertEquals("", CoreUtils.ensureStartWithUpperCase(""));
    Assert.assertEquals("  ", CoreUtils.ensureStartWithUpperCase("  "));
    Assert.assertEquals("A", CoreUtils.ensureStartWithUpperCase("a"));
    Assert.assertEquals("Ab", CoreUtils.ensureStartWithUpperCase("ab"));
    Assert.assertEquals("A", CoreUtils.ensureStartWithUpperCase("A"));
    Assert.assertEquals("Ab", CoreUtils.ensureStartWithUpperCase("Ab"));
    Assert.assertEquals("ABC", CoreUtils.ensureStartWithUpperCase("ABC"));
    Assert.assertEquals("Abc", CoreUtils.ensureStartWithUpperCase("abc"));
    Assert.assertEquals("ABC", CoreUtils.ensureStartWithUpperCase("aBC"));
  }

  @Test
  public void testEnsureValidParameterName() {
    Assert.assertEquals(null, CoreUtils.ensureValidParameterName(null));
    Assert.assertEquals("", CoreUtils.ensureValidParameterName(""));
    Assert.assertEquals("  ", CoreUtils.ensureValidParameterName("  "));
    Assert.assertEquals("abc", CoreUtils.ensureValidParameterName("abc"));
    Assert.assertEquals("floatA", CoreUtils.ensureValidParameterName("floatA"));
    Assert.assertEquals("floatValue", CoreUtils.ensureValidParameterName("float"));
    Assert.assertEquals("floatValue", CoreUtils.ensureValidParameterName("float"));
    Assert.assertEquals("FLOATValue", CoreUtils.ensureValidParameterName("FLOAT"));
  }

  @Test
  public void testGetAnnotation() {
    Assert.assertNotNull(CoreUtils.getAnnotation(CoreTestingUtils.getBaseClassType().getMethods().get(0), MarkerAnnotation.class.getName()));
    Assert.assertNotNull(CoreUtils.getAnnotation(CoreTestingUtils.getBaseClassType().getMethods().get(0), MarkerAnnotation.class.getSimpleName()));
  }

  @Test
  public void testIsOnClasspath() {
    Assert.assertTrue(CoreUtils.isOnClasspath(CoreTestingUtils.getChildClassIcu().getJavaEnvironment(), TypeNames.java_lang_Long));
    Assert.assertTrue(CoreUtils.isOnClasspath(CoreTestingUtils.getChildClassIcu().getJavaEnvironment(), org.eclipse.scout.sdk.core.fixture.Long.class.getName()));
    Assert.assertFalse(CoreUtils.isOnClasspath(CoreTestingUtils.getChildClassIcu().getJavaEnvironment(), "not.existing.Type"));
  }

  @Test
  public void testIsInstanceOf() {
    Assert.assertTrue(CoreUtils.isInstanceOf(CoreTestingUtils.getChildClassType(), BaseClass.class.getName()));
    Assert.assertTrue(CoreUtils.isInstanceOf(CoreTestingUtils.getChildClassType(), InterfaceLevel2.class.getName()));
    Assert.assertFalse(CoreUtils.isInstanceOf(CoreTestingUtils.getChildClassType(), org.eclipse.scout.sdk.core.fixture.Long.class.getName()));
    Assert.assertFalse(CoreUtils.isInstanceOf(CoreTestingUtils.getChildClassType(), TypeNames.java_lang_Long));
  }

  @Test
  public void testGetMethod() {
    Assert.assertNotNull(CoreUtils.getMethod(CoreTestingUtils.getBaseClassType(), "method2InBaseClass"));
    Assert.assertNull(CoreUtils.getMethod(CoreTestingUtils.getBaseClassType(), "method2InBaseclass"));
  }

  @Test
  public void testGetMethods() {
    List<IMethod> methods = CoreUtils.getMethods(CoreTestingUtils.getBaseClassType(), MethodFilters.flags(Flags.AccSynchronized));
    Assert.assertEquals(1, methods.size());

    methods = CoreUtils.getMethods(CoreTestingUtils.getBaseClassType(), MethodFilters.flags(Flags.AccPrivate));
    Assert.assertEquals(0, methods.size());
  }

  @Test
  public void testRemoveComments() {
    Assert.assertNull(CoreUtils.removeComments(null));
    Assert.assertEquals("int a = 4;", CoreUtils.removeComments("// my comment\nint a = 4;"));
    Assert.assertEquals(" int a = 4; ", CoreUtils.removeComments("/* my comment*/ int a = 4; "));
    Assert.assertEquals("int a = 4;", CoreUtils.removeComments("/** my comment*/int a = 4;"));
  }
}
