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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel1;
import org.eclipse.scout.sdk.core.fixture.InterfaceLevel2;
import org.eclipse.scout.sdk.core.fixture.MarkerAnnotation;
import org.eclipse.scout.sdk.core.fixture.PropertyTestClass;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IPropertyBean;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
    Assert.assertNull(CoreUtils.toStringLiteral(null));
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

    Assert.assertNull(CoreUtils.boxPrimitive(null));
    Assert.assertNull(CoreUtils.boxPrimitive("whatever"));
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

    Assert.assertNull(CoreUtils.unboxToPrimitive(null));
    Assert.assertNull(CoreUtils.unboxToPrimitive("whatever"));
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
      Assert.assertEquals("// TODO [" + testUserName + "] " + content, commentBlock);
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
    Assert.assertNull(CoreUtils.findInnerTypeInSuperHierarchy(null, null));
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
  public void testCreateTransformer() throws TransformerConfigurationException {
    Assert.assertNotNull(CoreUtils.createTransformer(true));
    Assert.assertNotNull(CoreUtils.createTransformer(false));
  }

  @Test
  public void testEvaluateXPath() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
    final String ns = "http://java.sun.com/xml/ns/jaxws";
    DocumentBuilder b = CoreUtils.createDocumentBuilder();
    Document prefixExplicit = b
        .parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><p:root xmlns:p=\"" + ns + "\"><p:element>whatever</p:element><p:element>another</p:element></p:root>")));
    List<Element> result = CoreUtils.evaluateXPath("p:root/p:element", prefixExplicit, "p", ns);
    Assert.assertEquals(2, result.size());

    Document prefixXmlns = b
        .parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root xmlns=\"" + ns + "\"><element>whatever</element><element>another</element></root>")));
    result = CoreUtils.evaluateXPath("p:root/p:element", prefixXmlns, "p", ns);
    Assert.assertEquals(2, result.size());

    Document prefixDifferent = b
        .parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><a:root xmlns:a=\"" + ns + "\"><a:element>whatever</a:element><a:element>another</a:element></a:root>")));
    result = CoreUtils.evaluateXPath("p:root/p:element", prefixDifferent, "p", ns);
    Assert.assertEquals(2, result.size());

    Document noNamespaces = b
        .parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root> <element>whatever</element> <element>another</element>  <!--comment --></root>")));
    result = CoreUtils.evaluateXPath("root/element", noNamespaces, null, null);
    Assert.assertEquals(2, result.size());

    Document notMatching = b
        .parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root><element>whatever</element><element>another</element></root>")));
    result = CoreUtils.evaluateXPath("root/elementa", notMatching, null, null);
    Assert.assertEquals(0, result.size());

    Document multipleNamespaces =
        b.parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><p:root xmlns:bb=\"http://other.name.space/something\" xmlns:p=\"" + ns
            + "\"><bb:another>content</bb:another><p:element>whatever</p:element><p:element>another</p:element></p:root>")));
    result = CoreUtils.evaluateXPath("p:root/bb:another", multipleNamespaces, "p", ns);
    Assert.assertEquals(1, result.size());

    Document emptyDoc = b
        .parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root></root>")));
    result = CoreUtils.evaluateXPath("root/element", emptyDoc);
    Assert.assertEquals(0, result.size());

    result = CoreUtils.evaluateXPath("root/element", null, null, null);
    Assert.assertEquals(0, result.size());

    result = CoreUtils.evaluateXPath(null, null, null, null);
    Assert.assertEquals(0, result.size());
  }

  @Test
  public void testRelativizeURI() {
    Assert.assertEquals("../../e/f/another.test", CoreUtils.relativizeURI(URI.create("a/b/c/d/test.txt"), URI.create("a/b/e/f/another.test")).toString());
    Assert.assertEquals("sub/sub2", CoreUtils.relativizeURI(URI.create("a/b/c/d/"), URI.create("a/b/c/d/sub/sub2")).toString());
    Assert.assertEquals("../../e/f/another.test", CoreUtils.relativizeURI(URI.create("http://user:pw@host:port/a/b/c/d/test.txt"), URI.create("http://user:pw@host:port/a/b/e/f/another.test")).toString());
    Assert.assertEquals("sub/sub2", CoreUtils.relativizeURI(URI.create("http://user:pw@host:port/a/b/c/d/"), URI.create("http://user:pw@host:port/a/b/c/d/sub/sub2")).toString());
    Assert.assertEquals("../../../../e/f/g", CoreUtils.relativizeURI(URI.create("/a/b/c/d/test.txt"), URI.create("/e/f/g")).toString());
    Assert.assertEquals("/a/b/c/d", CoreUtils.relativizeURI(URI.create("http://user:pw@host:port"), URI.create("http://user:pw@host:port/a/b/c/d")).toString());
    Assert.assertEquals("/a/b/c/d", CoreUtils.relativizeURI(URI.create("http://user:pw@host:port/"), URI.create("http://user:pw@host:port/a/b/c/d")).toString());
    Assert.assertEquals("../../../a/b", CoreUtils.relativizeURI(URI.create("http://user:pw@host:port/d/e/f/g"), URI.create("http://user:pw@host:port/a/b")).toString());
    Assert.assertEquals("../../../../a/b", CoreUtils.relativizeURI(URI.create("http://user:pw@host:port/d/e/f/g/"), URI.create("http://user:pw@host:port/a/b")).toString());

    Assert.assertEquals("../", CoreUtils.relativizeURI(URI.create("http://user:pw@host:port/g/h/i/j/k/"), URI.create("http://user:pw@host:port/g/h/i/j/")).toString());
    Assert.assertEquals("../", CoreUtils.relativizeURI(URI.create("http://user:pw@host:port/g/h/i/j/k/"), URI.create("http://user:pw@host:port/g/h/i/j")).toString());
    Assert.assertEquals("", CoreUtils.relativizeURI(URI.create("http://user:pw@host:port/g/h/i/j/k"), URI.create("http://user:pw@host:port/g/h/i/j")).toString());

    // dif scheme or authority
    String child1 = "http://user:pw@host2:port/a/b/e/f/another.test";
    Assert.assertEquals(child1, CoreUtils.relativizeURI(URI.create("http://user:pw@host:port/a/b/c/d/test.txt"), URI.create(child1)).toString());
    String child2 = "http://user:pw@host2:port/a/b/c/d/sub/sub2";
    Assert.assertEquals(child2, CoreUtils.relativizeURI(URI.create("http://user:pw@host:port/a/b/c/d/"), URI.create(child2)).toString());
    String child3 = "file://user:pw@host:port/a/b/c/d/sub/sub2";
    Assert.assertEquals(child3, CoreUtils.relativizeURI(URI.create("http://user:pw@host:port/a/b/c/d/"), URI.create(child3)).toString());
  }

  @Test
  public void testPropertyBean() {
    IType propTestClass = CoreTestingUtils.createJavaEnvironment().findType(PropertyTestClass.class.getName());
    Assert.assertNotNull(propTestClass);

    List<IPropertyBean> propertyBeans = new ArrayList<>(CoreUtils.getPropertyBeans(propTestClass, null, new Comparator<IPropertyBean>() {
      @Override
      public int compare(IPropertyBean o1, IPropertyBean o2) {
        return o1.name().compareTo(o2.name());
      }
    }));
    Assert.assertEquals(5, propertyBeans.size());

    IPropertyBean aloneProp = propertyBeans.get(0);
    Assert.assertEquals("Alone", aloneProp.name());
    Assert.assertEquals(String.class.getName(), aloneProp.type().name());
    Assert.assertEquals(propTestClass, aloneProp.declaringType());
    Assert.assertNull(aloneProp.readMethod());
    Assert.assertNotNull(aloneProp.writeMethod());

    IPropertyBean falseProp = propertyBeans.get(1);
    Assert.assertEquals("False", falseProp.name());
    Assert.assertEquals(IJavaRuntimeTypes.Boolean, falseProp.type().name());
    Assert.assertEquals(propTestClass, falseProp.declaringType());
    Assert.assertNotNull(falseProp.readMethod());
    Assert.assertNotNull(falseProp.writeMethod());

    IPropertyBean onlyProp = propertyBeans.get(2);
    Assert.assertEquals("Only", onlyProp.name());
    Assert.assertEquals(IJavaRuntimeTypes.Integer, onlyProp.type().name());
    Assert.assertEquals(propTestClass, onlyProp.declaringType());
    Assert.assertNotNull(onlyProp.readMethod());
    Assert.assertNull(onlyProp.writeMethod());

    IPropertyBean stringProp = propertyBeans.get(3);
    Assert.assertEquals("String", stringProp.name());
    Assert.assertEquals(String.class.getName(), stringProp.type().name());
    Assert.assertEquals(propTestClass, stringProp.declaringType());
    Assert.assertNotNull(stringProp.readMethod());
    Assert.assertNotNull(stringProp.writeMethod());

    IPropertyBean trueProp = propertyBeans.get(4);
    Assert.assertEquals("True", trueProp.name());
    Assert.assertEquals("boolean", trueProp.type().name());
    Assert.assertEquals(propTestClass, trueProp.declaringType());
    Assert.assertNotNull(trueProp.readMethod());
    Assert.assertNotNull(trueProp.writeMethod());

    List<IPropertyBean> propertyBeans2 = CoreUtils.getPropertyBeans(propTestClass, new IFilter<IPropertyBean>() {
      @Override
      public boolean evaluate(IPropertyBean element) {
        return Character.isLowerCase(element.type().elementName().charAt(0));
      }
    }, null);
    Assert.assertEquals(1, propertyBeans2.size());
  }

  @Test
  public void testGetCommentAutoGeneratedMethodStub() {
    String testUserName = "testuser";
    String backup = CoreUtils.getUsername();
    CoreUtils.setUsernameForThread(testUserName);
    try {
      Assert.assertEquals(testUserName, CoreUtils.getUsername());
      String commentBlock = CoreUtils.getCommentAutoGeneratedMethodStub();
      Assert.assertEquals("// TODO [" + testUserName + "] Auto-generated method stub.", commentBlock);
    }
    finally {
      CoreUtils.setUsernameForThread(backup);
    }
  }

  @Test
  public void testInputStreamToString() throws IOException {
    String testData = "my test data";
    Assert.assertEquals(testData, CoreUtils.inputStreamToString(new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_16LE)), StandardCharsets.UTF_16LE).toString());
    Assert.assertEquals(testData, CoreUtils.inputStreamToString(new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_16BE)), StandardCharsets.UTF_16BE.name()).toString());
  }

  @Test(expected = IOException.class)
  public void testInputStreamToStringWrongCharset() throws IOException {
    CoreUtils.inputStreamToString(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_16LE)), "not-existing");
  }

  @Test
  public void testCreateJavaCode() {
    final String testData = "testData";
    PropertyMap context = new PropertyMap();
    context.setProperty(testData, testData);
    ISourceBuilder builder = new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap c, IImportValidator validator) {
        source.append(c.getProperty(testData, String.class)).append(lineDelimiter);
      }
    };
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();

    Assert.assertEquals(testData + "\n", CoreUtils.createJavaCode(builder, env, "\n", context));
    Assert.assertEquals(testData + "\n", CoreUtils.createJavaCode(builder, env, null, context));
    Assert.assertNull(testData + "\n", CoreUtils.createJavaCode(builder, null, null, context));
    Assert.assertNull(testData + "\n", CoreUtils.createJavaCode(null, env, null, context));
  }

  @Test
  public void testGetFirstChildElement() throws SAXException, IOException, ParserConfigurationException {
    DocumentBuilder b = CoreUtils.createDocumentBuilder();
    Document xml = b.parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root><!--comment--><element>whatever</element><element>another</element></root>")));
    Element element = CoreUtils.getFirstChildElement(xml.getDocumentElement(), "element");
    Assert.assertNotNull(element);
    Assert.assertEquals("whatever", element.getTextContent());

    element = CoreUtils.getFirstChildElement(xml.getDocumentElement(), "notexisting");
    Assert.assertNull(element);

    element = CoreUtils.getFirstChildElement(null, "element");
    Assert.assertNull(element);

    element = CoreUtils.getFirstChildElement(xml.getDocumentElement(), null);
    Assert.assertNull(element);
  }

  @Test
  public void testXmlDocumentToString() throws TransformerException, ParserConfigurationException, SAXException, IOException {
    DocumentBuilder b = CoreUtils.createDocumentBuilder();
    String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root><!--comment--><element>whatever</element><element>another</element></root>";
    Document xml = b.parse(new InputSource(new StringReader(xmlContent)));

    String xmlString = CoreUtils.xmlDocumentToString(xml, false);
    Assert.assertEquals(xmlContent, xmlString);
  }

  @Test
  public void testGetParentURI() throws URISyntaxException {
    Assert.assertNull(CoreUtils.getParentURI(null));
    Assert.assertEquals(new URI(""), CoreUtils.getParentURI(new URI("")));
    Assert.assertEquals(new URI("http://www.test.com/"), CoreUtils.getParentURI(new URI("http://www.test.com/myFile.txt")));
    Assert.assertEquals(new URI("http://www.test.com/sub1/sub2/"), CoreUtils.getParentURI(new URI("http://www.test.com//sub1/sub2/myFile.txt")));
    Assert.assertEquals(new URI("http://www.test.com/sub1/"), CoreUtils.getParentURI(new URI("http://www.test.com//sub1/sub2/")));
    Assert.assertEquals(new URI("one/two/three/"), CoreUtils.getParentURI(new URI("one/two/three/four")));
    Assert.assertEquals(new URI("one/two/three/"), CoreUtils.getParentURI(new URI("one/two/three/file.ext")));
    Assert.assertEquals(new URI(""), CoreUtils.getParentURI(new URI("one")));
    Assert.assertEquals(new URI(""), CoreUtils.getParentURI(new URI("one/")));
  }

  @Test
  public void testDirectoryMoveAndDeleteSameFileSystem() throws IOException {
    Path folderToMove = Files.createTempDirectory("folderToMove");
    Path targetDirectory = Files.createTempDirectory("targetDir");

    try {
      File root = folderToMove.toFile();
      String subDirs = "dir/anotherdir/whateverdir/";
      File subFolder = new File(root, subDirs);
      boolean success = subFolder.mkdirs();
      if (!success) {
        throw new IOException("unable to create dirs");
      }
      String fileName = "content.txt";
      success = new File(subFolder, fileName).createNewFile();
      if (!success) {
        throw new IOException("unable to create file");
      }

      Assert.assertTrue(root.exists());
      CoreUtils.moveDirectory(root, targetDirectory.toFile());
      Assert.assertFalse(root.exists());
      File[] newContent = targetDirectory.toFile().listFiles();
      Assert.assertEquals(1, newContent.length);
      File movedDir = newContent[0];
      Assert.assertEquals(root.getName(), movedDir.getName());
      Assert.assertTrue(new File(movedDir, subDirs + fileName).exists());
    }
    finally {
      CoreUtils.deleteDirectory(folderToMove.toFile());
      CoreUtils.deleteDirectory(targetDirectory.toFile());
    }
  }
}
