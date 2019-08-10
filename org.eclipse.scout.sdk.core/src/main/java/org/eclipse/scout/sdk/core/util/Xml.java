/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static java.util.Collections.*;

/**
 * <h3>{@link Xml}</h3><br>
 * Contains helper methods to deal with XML related topics.
 *
 * @since 6.1.0
 */
public final class Xml {

  private Xml() {
  }

  /**
   * Transforms the specified {@link Document} into a {@link String}.
   *
   * @param document
   *          The document to transform.
   * @param format
   *          If the document should be formatted ({@code true}) or not ({@code false}).
   * @return The specified {@link Document} as {@link StringBuilder} optionally formatted.
   * @throws TransformerException
   *           if there is an error during transformation
   */
  public static StringBuilder documentToString(Document document, boolean format) throws TransformerException {
    try (StringWriter out = new StringWriter()) {
      Transformer transformer = createTransformer(format);
      transformer.transform(new DOMSource(document), new StreamResult(out));
      return new StringBuilder(out.getBuffer());
    }
    catch (IOException e) {
      throw new TransformerException(e);
    }
  }

  /**
   * Reads the content of the specified xml file into a XML {@link Document}.
   *
   * @param xmlFile
   *          A {@link Path} pointing to the xml file. Must not be {@code null}.
   * @return A {@link Document} with the content of the xml file.
   * @throws IOException
   *           if there is an exception reading the file into the document.
   */
  public static Document get(Path xmlFile) throws IOException {
    try (InputStream in = Files.newInputStream(Ensure.notNull(xmlFile), StandardOpenOption.READ)) {
      return get(in);
    }
  }

  /**
   * Reads the content of the specified {@link URL} into a XML {@link Document}.
   *
   * @param url
   *          The {@link URL} from where the content should be read. Must not be {@code null}.
   * @return A {@link Document} with the XML content of the url.
   * @throws IOException
   *           if there is an exception reading the file into the document.
   */
  public static Document get(URL url) throws IOException {
    // use document builder to download the stream content because it correctly handles the xml encoding as specified in the xml declaration.
    try (InputStream in = Ensure.notNull(url).openStream()) {
      return get(in);
    }
  }

  /**
   * Converts the specified {@link String} into a XML {@link Document}.
   *
   * @param rawXmlAsString
   *          The {@link String} holding the XML content. Must not be blank (see {@link Strings#isBlank(CharSequence)}.
   * @return A {@link Document} with the XML content of the specified {@link String}.
   * @throws IOException
   *           if there is an exception converting the {@link String}.
   */
  public static Document get(String rawXmlAsString) throws IOException {
    try (Reader r = new StringReader(Ensure.notBlank(rawXmlAsString))) {
      return createDocumentBuilder().parse(new InputSource(r));
    }
    catch (SAXException | ParserConfigurationException e) {
      throw new IOException(e);
    }
  }

  private static Document get(InputStream in) throws IOException {
    try {
      return createDocumentBuilder().parse(in);
    }
    catch (SAXException | ParserConfigurationException e) {
      throw new IOException(e);
    }
  }

  /**
   * Gets the first child {@link Element} of the specified parent {@link Element} having the specified local tag name
   * (ignoring namespaces).
   *
   * @param parent
   *          The parent {@link Element}
   * @param tagName
   *          The local tag name (see {@link Node#getLocalName()}
   * @return The first Element with this tag name or {@code null} if no such {@link Element} exists.
   */
  public static Optional<Element> firstChildElement(Node parent, String tagName) {
    if (parent == null) {
      return Optional.empty();
    }
    if (tagName == null) {
      return Optional.empty();
    }
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); ++i) {
      Node n = children.item(i);
      String nodeName = n.getLocalName();
      if (n.getNodeType() == Node.ELEMENT_NODE && Objects.equals(nodeName, tagName)) {
        return Optional.of((Element) n);
      }
    }
    return Optional.empty();
  }

  /**
   * Evaluates the specified xPath string on the specified {@link Document}.
   *
   * @param xPath
   *          The xPath expression
   * @param applyToDocument
   *          The {@link Document} to apply the xPath to.
   * @return All {@link Element}s that match the specified xPath expression.
   * @throws XPathExpressionException
   *           if there is an error during XPath evaluation
   */
  public static List<Element> evaluateXPath(String xPath, Node applyToDocument) throws XPathExpressionException {
    return evaluateXPath(xPath, applyToDocument, null);
  }

  /**
   * Evaluates the specified xPath string on the specified {@link Document}.
   *
   * @param xPath
   *          The xPath expression
   * @param applyToDocument
   *          The {@link Document} to apply the xPath to.
   * @param prefix
   *          The single namespace prefix that was used in the specified xPath expression
   * @param namespace
   *          The namespace the specified prefix maps to.
   * @return All {@link Element}s that match the specified xPath expression.
   * @throws XPathExpressionException
   *           if there is an error during XPath evaluation
   */
  public static List<Element> evaluateXPath(String xPath, Node applyToDocument, String prefix, String namespace) throws XPathExpressionException {
    return evaluateXPath(xPath, applyToDocument, singletonMap(prefix, namespace));
  }

  /**
   * Evaluates the specified xPath string on the specified {@link Document}.
   *
   * @param xPath
   *          The xPath expression
   * @param applyToDocument
   *          The {@link Document} to apply the xPath to.
   * @param usedPrefixToNamespaceMap
   *          A {@link Map} defining all namespace prefixes used in the specified xPath and their corresponding
   *          namespace.
   * @return All {@link Element}s that match the specified xPath expression.
   * @throws XPathExpressionException
   *           if there is an error during XPath evaluation
   */
  public static List<Element> evaluateXPath(String xPath, Node applyToDocument, Map<String, String> usedPrefixToNamespaceMap) throws XPathExpressionException {
    if (applyToDocument == null || Strings.isBlank(xPath)) {
      return emptyList();
    }

    XPathFactory xPathfactory = XPathFactory.newInstance();
    XPath xpath = xPathfactory.newXPath();
    xpath.setNamespaceContext(new NamespaceContext() {
      @Override
      public String getNamespaceURI(String prefix) {
        if (usedPrefixToNamespaceMap != null) {
          String ns = usedPrefixToNamespaceMap.get(prefix);
          if (ns != null) {
            return ns;
          }
        }
        return applyToDocument.lookupNamespaceURI(prefix);
      }

      @Override
      public Iterator<String> getPrefixes(String val) {
        return singletonList(getPrefix(val)).iterator();
      }

      @Override
      public String getPrefix(String uri) {
        if (usedPrefixToNamespaceMap != null) {
          for (Entry<String, String> entry : usedPrefixToNamespaceMap.entrySet()) {
            if (entry.getValue().equals(uri)) {
              return entry.getKey();
            }
          }
        }
        return applyToDocument.lookupPrefix(uri);
      }
    });

    XPathExpression expr = xpath.compile(xPath);
    NodeList result = (NodeList) expr.evaluate(applyToDocument, XPathConstants.NODESET);
    int size = result.getLength();
    if (size < 1) {
      return emptyList();
    }

    List<Element> elements = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      Node n = result.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        elements.add((Element) n);
      }
    }
    return elements;
  }

  /**
   * @return A safe pre configured {@link DocumentBuilderFactory}. All external entities are disabled to prevent XXE.
   */
  public static DocumentBuilderFactory createDocumentBuilderFactory() {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Map<String, Boolean> features = new HashMap<>(5);
    features.put("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
    features.put("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
    features.put("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
    features.put("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
    features.put(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
    dbf.setXIncludeAware(false);
    dbf.setExpandEntityReferences(false);
    dbf.setNamespaceAware(true); // required!
    try {
      dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    }
    catch (IllegalArgumentException e) {
      SdkLog.debug("Attribute '{}' is not supported in the current DocumentBuilderFactory: {}", XMLConstants.ACCESS_EXTERNAL_DTD, dbf.getClass().getName(), e);
    }
    try {
      dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    }
    catch (IllegalArgumentException e) {
      SdkLog.debug("Attribute '{}' is not supported in the current DocumentBuilderFactory: {}", XMLConstants.ACCESS_EXTERNAL_SCHEMA, dbf.getClass().getName(), e);
    }

    for (Entry<String, Boolean> a : features.entrySet()) {
      String feature = a.getKey();
      boolean enabled = a.getValue();
      try {
        dbf.setFeature(feature, enabled);
      }
      catch (ParserConfigurationException e) {
        SdkLog.debug("Feature '{}' is not supported in the current XML parser. Skipping.", feature, e);
      }
    }
    return dbf;
  }

  /**
   * Creates a new {@link DocumentBuilder} to create a DOM of an XML file.<br>
   * Use e.g. {@link DocumentBuilder#parse(InputStream)} to create a new {@link Document}.
   *
   * @return The created builder. All external entities are disabled to prevent XXE.
   * @throws ParserConfigurationException
   *           if a {@link DocumentBuilder} cannot be created which satisfies the configuration requested.
   */
  public static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
    return createDocumentBuilderFactory().newDocumentBuilder();
  }

  /**
   * Creates a new {@link Transformer}.<br>
   * Use {@link Transformer#transform(javax.xml.transform.Source, javax.xml.transform.Result)} to transform an XML
   * document.
   *
   * @param format
   *          {@code true} to have the document formatted (indent) during transformation. <code>false</otherwise>.
   * @return The created {@link Transformer}. All external entities are disabled to prevent XXE.
   * @throws TransformerConfigurationException
   *           When it is not possible to create a Transformer instance.
   */
  public static Transformer createTransformer(boolean format) throws TransformerConfigurationException {
    TransformerFactory tf = TransformerFactory.newInstance();
    int indent = 2;
    try {
      tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    }
    catch (TransformerConfigurationException e) {
      SdkLog.debug("Feature '{}' is not supported in the current TransformerFactory: {}", XMLConstants.FEATURE_SECURE_PROCESSING, tf.getClass().getName(), e);
    }

    Map<String, Object> attribs = new HashMap<>(3);
    attribs.put(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    attribs.put(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    if (format) {
      attribs.put("indent-number", indent);
    }

    for (Entry<String, Object> a : attribs.entrySet()) {
      try {
        tf.setAttribute(a.getKey(), a.getValue());
      }
      catch (IllegalArgumentException e) {
        SdkLog.debug("Attribute '{}' is not supported in the current TransformerFactory: {}", a.getKey(), tf.getClass().getName(), e);
      }
    }

    Transformer transformer = tf.newTransformer();

    Map<String, String> outputProps = new HashMap<>(4);
    outputProps.put(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
    outputProps.put(OutputKeys.METHOD, "xml");
    if (format) {
      outputProps.put(OutputKeys.INDENT, "yes");
      outputProps.put("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
    }
    else {
      outputProps.put(OutputKeys.INDENT, "no");
    }

    for (Entry<String, String> o : outputProps.entrySet()) {
      try {
        transformer.setOutputProperty(o.getKey(), o.getValue());
      }
      catch (IllegalArgumentException e) {
        SdkLog.debug("Error applying output property '{}' on transformer of class '{}'.", o.getKey(), transformer.getClass().getName(), e);
      }
    }
    return transformer;
  }
}
