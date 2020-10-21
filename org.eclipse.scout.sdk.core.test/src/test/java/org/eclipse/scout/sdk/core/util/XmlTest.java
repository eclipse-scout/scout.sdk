/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import static org.eclipse.scout.sdk.core.testing.CoreTestingUtils.normalizeWhitespace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <h3>{@link XmlTest}</h3>
 *
 * @since 6.1.0
 */
public class XmlTest {

  @Test
  public void testGetFirstChildElement() throws SAXException, IOException, ParserConfigurationException {
    var b = Xml.createDocumentBuilder();
    var xml = b.parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root><!--comment--><element>whatever</element><element>another</element></root>")));
    var element = Xml.firstChildElement(xml.getDocumentElement(), "element").get();
    assertEquals("whatever", element.getTextContent());

    var opt = Xml.firstChildElement(xml.getDocumentElement(), "notexisting");
    assertFalse(opt.isPresent());

    opt = Xml.firstChildElement(null, "element");
    assertFalse(opt.isPresent());

    opt = Xml.firstChildElement(xml.getDocumentElement(), null);
    assertFalse(opt.isPresent());
  }

  @Test
  public void testWriteDocumentToString() throws TransformerException, ParserConfigurationException, SAXException, IOException {
    var b = Xml.createDocumentBuilder();
    var xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
        + "<root>"
        + "<!--comment-->"
        + "<element>whatever</element>"
        + "<element>another</element>"
        + "</root>";
    var xml = b.parse(new InputSource(new StringReader(xmlContent)));

    var xmlString = Xml.writeDocument(xml, false).toString();
    assertEquals(xmlContent, xmlString);

    var expected = normalizeWhitespace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
        "<root>\n" +
        "  <!--comment-->\n" +
        "  <element>whatever</element>\n" +
        "  <element>another</element>\n" +
        "</root>\n");
    var xmlStringFormatted = normalizeWhitespace(Xml.writeDocument(xml, true));
    assertEquals(expected, xmlStringFormatted);
  }

  @Test
  public void testWriteDocument() throws TransformerException, IOException {
    var xml = Files.createTempFile("XmlTest", ".xml");
    try {
      var document = Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root></root>");
      assertNotNull(document);
      Xml.writeDocument(document, true, xml);
    }
    finally {
      Files.deleteIfExists(xml);
    }
  }

  @Test
  public void testCreateTransformer() throws TransformerConfigurationException {
    assertNotNull(Xml.createTransformer(true));
    assertNotNull(Xml.createTransformer(false));
  }

  @Test
  public void testGet() throws IOException {
    var xml = Files.createTempFile("XmlTest", ".xml");
    try {
      Files.write(xml, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root></root>".getBytes(StandardCharsets.UTF_8));
      var document = Xml.get(xml);
      assertNotNull(document);
      document = Xml.get(xml.toUri().toURL());
      assertNotNull(document);
    }
    finally {
      Files.deleteIfExists(xml);
    }
  }

  @Test
  public void testEvaluateXPath() throws XPathExpressionException, IOException {
    var ns = "http://java.sun.com/xml/ns/jaxws";
    var prefixExplicit = Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><p:root xmlns:p=\"" + ns + "\"><p:element>whatever</p:element><p:element>another</p:element></p:root>");
    var result = Xml.evaluateXPath("p:root/p:element", prefixExplicit, "p", ns);
    assertEquals(2, result.size());

    var prefixXmlns = Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root xmlns=\"" + ns + "\"><element>whatever</element><element>another</element></root>");
    result = Xml.evaluateXPath("p:root/p:element", prefixXmlns, "p", ns);
    assertEquals(2, result.size());

    var prefixDifferent = Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><a:root xmlns:a=\"" + ns + "\"><a:element>whatever</a:element><a:element>another</a:element></a:root>");
    result = Xml.evaluateXPath("p:root/p:element", prefixDifferent, "p", ns);
    assertEquals(2, result.size());

    var noNamespaces = Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root> <element>whatever</element> <element>another</element>  <!--comment --></root>");
    result = Xml.evaluateXPath("root/element", noNamespaces, null, null);
    assertEquals(2, result.size());

    var notMatching = Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root><element>whatever</element><element>another</element></root>");
    result = Xml.evaluateXPath("root/elementa", notMatching, null, null);
    assertEquals(0, result.size());

    var multipleNamespaces =
        Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><p:root xmlns:bb=\"http://other.name.space/something\" xmlns:p=\"" + ns
            + "\"><bb:another>content</bb:another><p:element>whatever</p:element><p:element>another</p:element></p:root>");
    result = Xml.evaluateXPath("p:root/bb:another", multipleNamespaces, "p", ns);
    assertEquals(1, result.size());

    var emptyDoc = Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root></root>");
    result = Xml.evaluateXPath("root/element", emptyDoc);
    assertEquals(0, result.size());

    result = Xml.evaluateXPath("root/element", null, null, null);
    assertEquals(0, result.size());

    result = Xml.evaluateXPath(null, null, null, null);
    assertEquals(0, result.size());
  }
}
