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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
   * Reads the content of the specified xml file into a XML {@link Document}.
   *
   * @param xmlFile
   *          A {@link Path} pointing to the xml file. Must not be {@code null}.
   * @return A {@link Document} with the content of the xml file.
   * @throws IOException
   *           if there is an exception reading the file into the document.
   */
  public static Document get(Path xmlFile) throws IOException {
    try (var in = Files.newInputStream(Ensure.notNull(xmlFile), StandardOpenOption.READ)) {
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
    try (var in = Ensure.notNull(url).openStream()) {
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

  static Document get(InputStream in) throws IOException {
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
    var children = parent.getChildNodes();
    for (var i = 0; i < children.getLength(); ++i) {
      var n = children.item(i);
      var nodeName = n.getLocalName();
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
    var result = doEvaluateXPath(xPath, applyToDocument, usedPrefixToNamespaceMap);
    if (result == null) {
      return emptyList();
    }
    var size = result.getLength();
    if (size < 1) {
      return emptyList();
    }
    return IntStream.range(0, size)
        .mapToObj(result::item)
        .filter(n -> n.getNodeType() == Node.ELEMENT_NODE)
        .map(n -> (Element) n)
        .collect(toList());
  }

  static NodeList doEvaluateXPath(String xPath, Node applyToDocument, Map<String, String> usedPrefixToNamespaceMap) throws XPathExpressionException {
    if (applyToDocument == null || Strings.isBlank(xPath)) {
      return null;
    }

    var xPathfactory = XPathFactory.newDefaultInstance();
    var xpath = xPathfactory.newXPath();
    xpath.setNamespaceContext(new NamespaceContext() {
      @Override
      public String getNamespaceURI(String prefix) {
        if (usedPrefixToNamespaceMap != null) {
          var ns = usedPrefixToNamespaceMap.get(prefix);
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
          for (var entry : usedPrefixToNamespaceMap.entrySet()) {
            if (entry.getValue().equals(uri)) {
              return entry.getKey();
            }
          }
        }
        return applyToDocument.lookupPrefix(uri);
      }
    });

    var expr = xpath.compile(xPath);
    return (NodeList) expr.evaluate(applyToDocument, XPathConstants.NODESET);
  }

  /**
   * @return A safe pre configured {@link DocumentBuilderFactory}. All external entities are disabled to prevent XXE.
   */
  public static DocumentBuilderFactory createDocumentBuilderFactory() {
    var dbf = DocumentBuilderFactory.newDefaultInstance();
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

    Map<String, Boolean> features = new HashMap<>(5);
    features.put("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
    features.put("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
    features.put("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
    features.put("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
    features.put(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
    for (var a : features.entrySet()) {
      var feature = a.getKey();
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
   * Transforms the specified {@link Document} into a {@link StringBuffer}.
   *
   * @param document
   *          The document to transform. Must not be {@code null}.
   * @param format
   *          If the document should be formatted ({@code true}) or not ({@code false}).
   * @return The specified {@link Document} as {@link StringBuffer} optionally formatted.
   * @throws TransformerException
   *           if there is an error writing or transforming the document
   */
  public static StringBuffer writeDocument(Document document, boolean format) throws TransformerException {
    try (var out = new StringWriter()) {
      doWriteDocument(document, format, new StreamResult(out));
      return out.getBuffer();
    }
    catch (IOException e) {
      throw new TransformerException(e);
    }
  }

  /**
   * Writes the {@link Document} specified into the target file.
   * 
   * @param document
   *          The {@link Document} to transform. Must not be {@code null}.
   * @param format
   *          Specifies if the document should be formatted when writing. Please note: this also removes empty lines.
   * @param targetFile
   *          The file in which the {@link Document} should be written. Must not be {@code null}.
   * @throws TransformerException
   *           if there is an error writing or transforming the document
   */
  public static void writeDocument(Document document, boolean format, Path targetFile) throws TransformerException {
    try (var out = Files.newOutputStream(targetFile, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      writeDocument(document, format, out);
    }
    catch (IOException e) {
      throw new TransformerException(e);
    }
  }

  /**
   * Writes the {@link Document} specified into the given {@link Writer}.
   * 
   * @param document
   *          The {@link Document} to transform. Must not be {@code null}.
   * @param format
   *          Specifies if the document should be formatted when writing. Please note: this also removes empty lines.
   * @param out
   *          The {@link Writer} in which the {@link Document} should be written. Must not be {@code null}.
   * @throws TransformerException
   *           if there is an error writing or transforming the document
   */
  public static void writeDocument(Document document, boolean format, Writer out) throws TransformerException {
    doWriteDocument(document, format, new StreamResult(out));
  }

  /**
   * Writes the {@link Document} specified into the given {@link OutputStream}.
   * 
   * @param document
   *          The {@link Document} to transform. Must not be {@code null}.
   * @param format
   *          Specifies if the document should be formatted when writing. Please note: this also removes empty lines.
   * @param out
   *          The {@link OutputStream} in which the {@link Document} should be written. Must not be {@code null}.
   * @throws TransformerException
   *           if there is an error writing or transforming the document
   */
  public static void writeDocument(Document document, boolean format, OutputStream out) throws TransformerException {
    doWriteDocument(document, format, new StreamResult(out));
  }

  static void doWriteDocument(Document document, boolean format, Result out) throws TransformerException {
    if (format) {
      document.normalize();
      try {
        var nodeList = doEvaluateXPath("//text()[normalize-space()='']", document, null);
        for (var i = 0; i < nodeList.getLength(); ++i) {
          var node = nodeList.item(i);
          node.getParentNode().removeChild(node);
        }
      }
      catch (XPathExpressionException e) {
        throw new TransformerException(e);
      }
    }
    createTransformer(format).transform(new DOMSource(document), out);
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
    var tf = TransformerFactory.newDefaultInstance();
    try {
      tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    }
    catch (TransformerConfigurationException e) {
      SdkLog.debug("Feature '{}' is not supported in the current TransformerFactory: {}", XMLConstants.FEATURE_SECURE_PROCESSING, tf.getClass().getName(), e);
    }

    Map<String, Object> attribs = new HashMap<>(2);
    attribs.put(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    attribs.put(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    for (var a : attribs.entrySet()) {
      try {
        tf.setAttribute(a.getKey(), a.getValue());
      }
      catch (IllegalArgumentException e) {
        SdkLog.debug("Attribute '{}' is not supported in the current TransformerFactory: {}", a.getKey(), tf.getClass().getName(), e);
      }
    }

    var transformer = tf.newTransformer();

    Map<String, String> outputProps = new HashMap<>(4);
    outputProps.put(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
    outputProps.put(OutputKeys.METHOD, "xml");
    if (format) {
      outputProps.put(OutputKeys.INDENT, "yes");
      outputProps.put("{http://xml.apache.org/xslt}indent-amount", Integer.toString(2));
    }
    else {
      outputProps.put(OutputKeys.INDENT, "no");
    }

    for (var o : outputProps.entrySet()) {
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
