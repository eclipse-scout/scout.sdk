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
import java.security.GeneralSecurityException;
import java.util.AbstractList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel1;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel2;
import org.eclipse.scout.sdk.core.fixture.MarkerAnnotation;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
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
    List<IType> innerTypes = CoreTestingUtils.getBaseClassType().innerTypes().withFlags(Flags.AccStatic).list();
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
  public void testGenerateKeyPair() throws GeneralSecurityException {
    String[] generateKeyPair = CoreUtils.generateKeyPair();
    Assert.assertEquals(2, generateKeyPair.length);
    Assert.assertTrue(StringUtils.isNotBlank(generateKeyPair[0]));
    Assert.assertTrue(StringUtils.isNotBlank(generateKeyPair[1]));
  }

  @Test
  public void testGetPrimaryType() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    Assert.assertSame(baseClassType, CoreUtils.getPrimaryType(baseClassType));
    Assert.assertSame(baseClassType, CoreUtils.getPrimaryType(baseClassType.innerTypes().first()));
    Assert.assertSame(null, CoreUtils.getPrimaryType(null));
  }

  @Test
  public void testBoxPrimitive() {
    Assert.assertEquals(IJavaRuntimeTypes.Boolean, CoreUtils.boxPrimitive(IJavaRuntimeTypes._boolean));
    Assert.assertEquals(IJavaRuntimeTypes.Byte, CoreUtils.boxPrimitive(IJavaRuntimeTypes._byte));
    Assert.assertEquals(IJavaRuntimeTypes.Character, CoreUtils.boxPrimitive(IJavaRuntimeTypes._char));
    Assert.assertEquals(IJavaRuntimeTypes.Double, CoreUtils.boxPrimitive(IJavaRuntimeTypes._double));
    Assert.assertEquals(IJavaRuntimeTypes.Float, CoreUtils.boxPrimitive(IJavaRuntimeTypes._float));
    Assert.assertEquals(IJavaRuntimeTypes.Integer, CoreUtils.boxPrimitive(IJavaRuntimeTypes._int));
    Assert.assertEquals(IJavaRuntimeTypes.Long, CoreUtils.boxPrimitive(IJavaRuntimeTypes._long));
    Assert.assertEquals(IJavaRuntimeTypes.Short, CoreUtils.boxPrimitive(IJavaRuntimeTypes._short));
    Assert.assertEquals(IJavaRuntimeTypes.Void, CoreUtils.boxPrimitive(IJavaRuntimeTypes._void));

    Assert.assertEquals(IJavaRuntimeTypes.Boolean, CoreUtils.boxPrimitive(IJavaRuntimeTypes.Boolean));
    Assert.assertEquals(IJavaRuntimeTypes.Byte, CoreUtils.boxPrimitive(IJavaRuntimeTypes.Byte));
    Assert.assertEquals(IJavaRuntimeTypes.Character, CoreUtils.boxPrimitive(IJavaRuntimeTypes.Character));
    Assert.assertEquals(IJavaRuntimeTypes.Double, CoreUtils.boxPrimitive(IJavaRuntimeTypes.Double));
    Assert.assertEquals(IJavaRuntimeTypes.Float, CoreUtils.boxPrimitive(IJavaRuntimeTypes.Float));
    Assert.assertEquals(IJavaRuntimeTypes.Integer, CoreUtils.boxPrimitive(IJavaRuntimeTypes.Integer));
    Assert.assertEquals(IJavaRuntimeTypes.Long, CoreUtils.boxPrimitive(IJavaRuntimeTypes.Long));
    Assert.assertEquals(IJavaRuntimeTypes.Short, CoreUtils.boxPrimitive(IJavaRuntimeTypes.Short));
    Assert.assertEquals(IJavaRuntimeTypes.Void, CoreUtils.boxPrimitive(IJavaRuntimeTypes.Void));
  }

  @Test
  public void testUnboxToPrimitive() {
    Assert.assertEquals(IJavaRuntimeTypes._boolean, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes._boolean));
    Assert.assertEquals(IJavaRuntimeTypes._byte, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes._byte));
    Assert.assertEquals(IJavaRuntimeTypes._char, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes._char));
    Assert.assertEquals(IJavaRuntimeTypes._double, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes._double));
    Assert.assertEquals(IJavaRuntimeTypes._float, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes._float));
    Assert.assertEquals(IJavaRuntimeTypes._int, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes._int));
    Assert.assertEquals(IJavaRuntimeTypes._long, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes._long));
    Assert.assertEquals(IJavaRuntimeTypes._short, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes._short));
    Assert.assertEquals(IJavaRuntimeTypes._void, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes._void));

    Assert.assertEquals(IJavaRuntimeTypes._boolean, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes.Boolean));
    Assert.assertEquals(IJavaRuntimeTypes._byte, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes.Byte));
    Assert.assertEquals(IJavaRuntimeTypes._char, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes.Character));
    Assert.assertEquals(IJavaRuntimeTypes._double, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes.Double));
    Assert.assertEquals(IJavaRuntimeTypes._float, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes.Float));
    Assert.assertEquals(IJavaRuntimeTypes._int, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes.Integer));
    Assert.assertEquals(IJavaRuntimeTypes._long, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes.Long));
    Assert.assertEquals(IJavaRuntimeTypes._short, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes.Short));
    Assert.assertEquals(IJavaRuntimeTypes._void, CoreUtils.unboxToPrimitive(IJavaRuntimeTypes.Void));
  }

  @Test
  public void testUserName() {
    String testUserName = "testuser";
    String backup = CoreUtils.getUsername();
    CoreUtils.setUsernameForThread(testUserName);
    try {
      Assert.assertEquals(testUserName, CoreUtils.getUsername());
      String content = "testcontent";
      String commentBlock = CoreUtils.getCommentBlock(content);
      Assert.assertTrue(commentBlock.contains("[" + testUserName + "] "));
      Assert.assertTrue(commentBlock.contains(content));
    }
    finally {
      CoreUtils.setUsernameForThread(backup);
    }
  }

  @Test
  public void testGetGetterMethodPrefix() {
    Assert.assertEquals("is", CoreUtils.getGetterMethodPrefix(ISignatureConstants.SIG_BOOLEAN));
    Assert.assertEquals("get", CoreUtils.getGetterMethodPrefix(Signature.createTypeSignature(IJavaRuntimeTypes.Boolean))); // must be get for java bean specification compliance!
    Assert.assertEquals("is", CoreUtils.getGetterMethodPrefix(Signature.createTypeSignature(IJavaRuntimeTypes._boolean)));
    Assert.assertEquals("get", CoreUtils.getGetterMethodPrefix(Signature.createTypeSignature(IJavaRuntimeTypes.Object)));
    Assert.assertEquals("get", CoreUtils.getGetterMethodPrefix(null));
  }

  @Test
  public void testFindInnerType() {
    Assert.assertEquals(CoreTestingUtils.getBaseClassType().innerTypes().first(), CoreTestingUtils.getBaseClassType().innerTypes().withSimpleName("InnerClass1").first());
  }

  @Test
  public void testGetAllSuperInterfaces() {
    Assert.assertEquals(2, CoreTestingUtils.getBaseClassType().superTypes().withSelf(false).withSuperClasses(false).list().size());
  }

  @Test
  public void testGetFields() {
    Assert.assertEquals(1, CoreTestingUtils.getChildClassType().fields().withName("m_test").list().size());
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
    IMethod methodInBaseClass = CoreTestingUtils.getChildClassType().methods().withSuperTypes(true).withAnnotation(MarkerAnnotation.class.getName()).first();
    Assert.assertNotNull(methodInBaseClass);
    Assert.assertEquals("methodInBaseClass", methodInBaseClass.elementName());
  }

  @Test
  public void testFindInnerTypeInSuperHierarchy() {
    IType innerTypeInSuperClass = CoreUtils.findInnerTypeInSuperHierarchy(CoreTestingUtils.getChildClassType(), new IFilter<IType>() {
      @Override
      public boolean evaluate(IType element) {
        return "InnerClass2".equals(element.elementName());
      }
    });
    Assert.assertNotNull(innerTypeInSuperClass);
    Assert.assertEquals("org.eclipse.scout.sdk.core.fixture.BaseClass$InnerClass2", innerTypeInSuperClass.name());
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
    Assert.assertNotNull(CoreTestingUtils.getBaseClassType().methods().first().annotations().withName(MarkerAnnotation.class.getName()).first());
  }

  @Test
  public void testGetDefaultValueOf() {
    Assert.assertEquals(null, CoreUtils.getDefaultValueOf(null));

    // primitives
    Assert.assertEquals(Boolean.FALSE.toString(), CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_BOOLEAN));
    Assert.assertEquals("0", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_BYTE));
    Assert.assertEquals("0", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_CHAR));
    Assert.assertEquals("0.0", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_DOUBLE));
    Assert.assertEquals("0.0f", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_FLOAT));
    Assert.assertEquals("0", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_INT));
    Assert.assertEquals("0L", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_LONG));
    Assert.assertEquals("0", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_SHORT));
    Assert.assertEquals(null, CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_VOID));
    Assert.assertEquals("null", CoreUtils.getDefaultValueOf(Signature.createTypeSignature(IJavaRuntimeTypes.Object)));

    // complex
    Assert.assertEquals("Boolean.FALSE", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_JAVA_LANG_BOOLEAN));
    Assert.assertEquals("Byte.valueOf((byte)0)", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_JAVA_LANG_BYTE));
    Assert.assertEquals("Character.valueOf((char)0)", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_JAVA_LANG_CHARACTER));
    Assert.assertEquals("Double.valueOf(0.0)", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_JAVA_LANG_DOUBLE));
    Assert.assertEquals("Float.valueOf(0.0f)", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_JAVA_LANG_FLOAT));
    Assert.assertEquals("Integer.valueOf(0)", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_JAVA_LANG_INTEGER));
    Assert.assertEquals("Long.valueOf(0L)", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_JAVA_LANG_LONG));
    Assert.assertEquals("Short.valueOf((short)0)", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_JAVA_LANG_SHORT));
    Assert.assertEquals("\"\"", CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_JAVA_LANG_STRING));
    Assert.assertEquals(null, CoreUtils.getDefaultValueOf(ISignatureConstants.SIG_JAVA_LANG_VOID));
  }

  @Test
  public void testIsOnClasspath() {
    IType baseClassType = CoreTestingUtils.getBaseClassType();
    IJavaEnvironment environment = baseClassType.javaEnvironment();
    Assert.assertTrue(CoreUtils.isOnClasspath(environment, baseClassType));
    Assert.assertFalse(CoreUtils.isOnClasspath(environment, (IType) null));

    Assert.assertTrue(CoreUtils.isOnClasspath(environment, IJavaRuntimeTypes.Long));
    Assert.assertTrue(CoreUtils.isOnClasspath(environment, org.eclipse.scout.sdk.core.fixture.Long.class.getName()));
    Assert.assertFalse(CoreUtils.isOnClasspath(environment, "not.existing.Type"));
    Assert.assertFalse(CoreUtils.isOnClasspath(environment, (String) null));
  }

  @Test
  public void testIsInstanceOf() {
    Assert.assertTrue(CoreTestingUtils.getChildClassType().isInstanceOf(BaseClass.class.getName()));
    Assert.assertTrue(CoreTestingUtils.getChildClassType().isInstanceOf(InterfaceLevel2.class.getName()));
    Assert.assertFalse(CoreTestingUtils.getChildClassType().isInstanceOf(org.eclipse.scout.sdk.core.fixture.Long.class.getName()));
    Assert.assertFalse(CoreTestingUtils.getChildClassType().isInstanceOf(IJavaRuntimeTypes.Long));
  }

  @Test
  public void testGetMethod() {
    Assert.assertNotNull(CoreTestingUtils.getBaseClassType().methods().withName("method2InBaseClass").first());
    Assert.assertNull(CoreTestingUtils.getBaseClassType().methods().withName("method2InBaseclass").first());
  }

  @Test
  public void testGetMethods() {
    List<IMethod> methods = CoreTestingUtils.getBaseClassType().methods().withFlags(Flags.AccSynchronized).list();
    Assert.assertEquals(1, methods.size());

    methods = CoreTestingUtils.getBaseClassType().methods().withFlags(Flags.AccPrivate).list();
    Assert.assertEquals(0, methods.size());
  }

  @Test
  public void testRemoveComments() {
    Assert.assertNull(CoreUtils.removeComments(null));
    Assert.assertEquals("int a = 4;", CoreUtils.removeComments("// my comment\nint a = 4;"));
    Assert.assertEquals(" int a = 4; ", CoreUtils.removeComments("/* my comment*/ int a = 4; "));
    Assert.assertEquals("int a = 4;", CoreUtils.removeComments("/** my comment*/int a = 4;"));
  }

  @Test
  public void testEscapeHtml() {
    Assert.assertEquals("", CoreUtils.escapeHtml(""));
    Assert.assertEquals("a&amp;&lt;&gt;&quot;&#47;&apos;&apos;b", CoreUtils.escapeHtml("a&<>\"/''b"));
    Assert.assertNull(CoreUtils.escapeHtml(null));
  }

  @Test
  public void testCreateDocumentBuilder() throws ParserConfigurationException {
    CoreUtils.createDocumentBuilder();
  }
}
