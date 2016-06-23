/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.jaxws;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl.WebServiceNames;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <h3>{@link JaxWsUtils}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class JaxWsUtils implements IMavenConstants {

  public static final String WSDL_FILE_EXTENSION = ".wsdl";
  public static final String ENTRY_POINT_DEFINITION_ENDPOINTINTERFACE_ATTRIBUTE = "endpointInterface";
  public static final String ENTRY_POINT_DEFINITION_NAME_ATTRIBUTE = "entryPointName";
  public static final String ENTRY_POINT_DEFINITION_PACKAGE_ATTRIBUTE = "entryPointPackage";
  public static final String ENTRY_POINT_DEFINITION_AUTH_ATTRIBUTE = "authentication";
  public static final String ENTRY_POINT_DEFINITION_HANDLER_CHAIN_ATTRIBUTE = "handlerChain";
  public static final String BINDINGS_NAME_ATTRIBUTE = "name";
  public static final String BINDINGS_CLASS_ELEMENT_NAME = "class";
  public static final String BINDING_PACKAGE_ELEMENT_NAME = "package";
  public static final String BINDINGS_NODE_ATTRIBUTE_NAME = "node";
  public static final String JAX_WS_NAMESPACE = "http://java.sun.com/xml/ns/jaxws";
  public static final String JAX_B_NAMESPACE = "http://java.sun.com/xml/ns/jaxb";
  public static final String BINDINGS_ELEMENT_NAME = "bindings";
  public static final String GENERATE_ELEMENT_ATTRIBUTE_NAME = "generateElementProperty";
  public static final String GLOBAL_BINDINGS_ELEMENT_NAME = "globalBindings";
  public static final String MODULE_REL_WEBINF_FOLDER_PATH = "src/main/resources/WEB-INF";
  public static final String BINDING_FILE_ELEMENT_NAME = "bindingFile";
  public static final String BINDING_FILES_ELEMENT_NAME = "bindingFiles";
  public static final String WSDL_FILE_ELEMENT_NAME = "wsdlFile";
  public static final String WSDL_FILES_ELEMENT_NAME = "wsdlFiles";
  public static final String WSDL_LOCATION = "wsdlLocation";
  public static final String GLOBAL_BINDINGS_FILE_NAME = "global-binding.xml";
  public static final String JAXB_BINDINGS_FILE_NAME = "jaxb-binding.xml";
  public static final String JAXWS_BINDINGS_FILE_NAME = "jaxws-binding.xml";
  public static final String WSDL_FOLDER_NAME = "wsdl";
  public static final String BINDING_FOLDER_NAME = "binding";
  public static final String WSIMPORT_TOOL_NAME = "wsimport";
  public static final String CODEHAUS_GROUP_ID = "org.codehaus.mojo";
  public static final String JAXWS_MAVEN_PLUGIN_ARTIFACT_ID = "jaxws-maven-plugin";

  public static final String PACKAGE_XPATH = "wsdl:definitions";
  private static final String WEB_SERVICE_XPATH_START = "wsdl:definitions/wsdl:service[@name='";
  private static final String PORT_TYPE_XPATH_START = "wsdl:definitions/wsdl:portType[@name='";
  private static final String XPATH_END = "']";

  private JaxWsUtils() {
  }

  /**
   * Creates the content of a new and empty JaxB binding.
   *
   * @param lineDelimiter
   *          The line delimiter to use.
   * @return A {@link StringBuilder} holding the content.
   */
  public static StringBuilder getJaxbBindingContent(String lineDelimiter) {
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>").append(lineDelimiter);
    sb.append("<!-- binding to customize xsd schema artifacts (jaxb-namespace: http://java.sun.com/xml/ns/jaxb) -->").append(lineDelimiter);
    sb.append("<bindings xmlns=\"http://java.sun.com/xml/ns/jaxb\" version=\"2.1\">").append(lineDelimiter);
    sb.append("</bindings>").append(lineDelimiter);
    return sb;
  }

  /**
   * Gets an empty wsdl with one sample operation (using document/literal)
   *
   * @param name
   *          The name of the wsdl
   * @param packageName
   *          The package name of the wsdl (reverse of the namespace).
   * @param lineDelimiter
   *          The line delimiter to use.
   * @return A {@link StringBuilder} holding the empty wsdl content.
   */
  public static StringBuilder getEmptyWsdl(String name, String packageName, String lineDelimiter) {
    String[] parts = packageName.split("\\.");
    if (parts.length > 0 && name.equalsIgnoreCase(parts[parts.length - 1])) {
      parts = Arrays.copyOf(parts, parts.length - 1);
    }
    ArrayUtils.reverse(parts);
    String nameSpace = StringUtils.join(parts, '.');

    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>").append(lineDelimiter);
    sb.append("<wsdl:definitions name=\"").append(name).append("\"").append(lineDelimiter);
    sb.append("  xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"").append(lineDelimiter);
    sb.append("  xmlns:tns=\"http://").append(nameSpace).append('/').append(name).append("/\"").append(lineDelimiter);
    sb.append("  xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"").append(lineDelimiter);
    sb.append("  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"").append(lineDelimiter);
    sb.append("  targetNamespace=\"http://").append(nameSpace).append('/').append(name).append("/\">").append(lineDelimiter);
    sb.append("  <wsdl:types>").append(lineDelimiter);
    sb.append("    <xsd:schema targetNamespace=\"http://").append(nameSpace).append('/').append(name).append("/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">").append(lineDelimiter);
    sb.append("      <xsd:element name=\"InElement\" type=\"xsd:string\"/>").append(lineDelimiter);
    sb.append("      <xsd:element name=\"OutElement\" type=\"xsd:string\"/>").append(lineDelimiter);
    sb.append("    </xsd:schema>").append(lineDelimiter);
    sb.append("  </wsdl:types>").append(lineDelimiter);
    sb.append("  <wsdl:message name=\"NewOperationRequest\">").append(lineDelimiter);
    sb.append("    <wsdl:part element=\"tns:InElement\" name=\"in\"/>").append(lineDelimiter);
    sb.append("  </wsdl:message>").append(lineDelimiter);
    sb.append("  <wsdl:message name=\"NewOperationResponse\">").append(lineDelimiter);
    sb.append("    <wsdl:part element=\"tns:OutElement\" name=\"ret\"/>").append(lineDelimiter);
    sb.append("  </wsdl:message>").append(lineDelimiter);
    sb.append("  <wsdl:portType name=\"").append(name).append("PortType\">").append(lineDelimiter);
    sb.append("    <wsdl:operation name=\"NewOperation\">").append(lineDelimiter);
    sb.append("      <wsdl:input message=\"tns:NewOperationRequest\"/>").append(lineDelimiter);
    sb.append("      <wsdl:output message=\"tns:NewOperationResponse\"/>").append(lineDelimiter);
    sb.append("    </wsdl:operation>").append(lineDelimiter);
    sb.append("  </wsdl:portType>").append(lineDelimiter);
    sb.append("  <wsdl:binding name=\"").append(name).append("PortSoapBinding\" type=\"tns:").append(name).append("PortType\">").append(lineDelimiter);
    sb.append("    <soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>").append(lineDelimiter);
    sb.append("    <wsdl:operation name=\"NewOperation\">").append(lineDelimiter);
    sb.append("      <soap:operation soapAction=\"http://").append(nameSpace).append('/').append(name).append("/NewOperation\"/>").append(lineDelimiter);
    sb.append("      <wsdl:input>").append(lineDelimiter);
    sb.append("        <soap:body use=\"literal\"/>").append(lineDelimiter);
    sb.append("      </wsdl:input>").append(lineDelimiter);
    sb.append("      <wsdl:output>").append(lineDelimiter);
    sb.append("        <soap:body use=\"literal\"/>").append(lineDelimiter);
    sb.append("      </wsdl:output>").append(lineDelimiter);
    sb.append("    </wsdl:operation>").append(lineDelimiter);
    sb.append("  </wsdl:binding>").append(lineDelimiter);
    sb.append("  <wsdl:service name=\"").append(name).append("WebService\">").append(lineDelimiter);
    sb.append("    <wsdl:port binding=\"tns:").append(name).append("PortSoapBinding\" name=\"").append(name).append("Port\">").append(lineDelimiter);
    sb.append("      <soap:address location=\"http://").append(nameSpace).append("/\"/>").append(lineDelimiter);
    sb.append("    </wsdl:port>").append(lineDelimiter);
    sb.append("  </wsdl:service>").append(lineDelimiter);
    sb.append("</wsdl:definitions>").append(lineDelimiter);

    return sb;
  }

  /**
   * Gets the JaxWs binding contents for the given {@link ParsedWsdl}.
   *
   * @param parsedWsdl
   *          The {@link ParsedWsdl} holding all wsdl artifacts.
   * @param rootWsdlUri
   *          The root {@link URI} of the wsdl.
   * @param lineDelimiter
   *          The line delimiter to use.
   * @param targetPackage
   *          The target package in which all JaxWs artifacts should be stored.
   * @return A {@link Map} holding a {@link Path} for each created binding and a {@link StringBuilder} with the
   *         corresponding content.
   */
  public static Map<Path, StringBuilder> getJaxwsBindingContents(ParsedWsdl parsedWsdl, URI rootWsdlUri, String lineDelimiter, String targetPackage) {
    Map<URI, Set<JaxWsBindingMapping>> bindingsByFile = new HashMap<>();
    for (Entry<Service, URI> service : parsedWsdl.getWebServices().entrySet()) {
      WebServiceNames names = parsedWsdl.getServiceNames().get(service.getKey());
      Set<JaxWsBindingMapping> bindings = bindingsByFile.get(service.getValue());
      if (bindings == null) {
        bindings = new HashSet<>();
        bindingsByFile.put(service.getValue(), bindings);
      }
      bindings.add(new JaxWsBindingMapping(false, names.getWebServiceNameFromWsdl(), names.getWebServiceClassName()));

      Set<PortType> portTypesByService = parsedWsdl.getPortTypes(service.getKey());
      for (PortType portType : portTypesByService) {
        URI uriOfPortType = parsedWsdl.getPortTypes().get(portType);
        bindings = bindingsByFile.get(uriOfPortType);
        if (bindings == null) {
          bindings = new HashSet<>();
          bindingsByFile.put(uriOfPortType, bindings);
        }
        String portTypeName = portType.getQName().getLocalPart();
        bindings.add(new JaxWsBindingMapping(true, portTypeName, names.getPortTypeClassName(portTypeName)));
      }
    }

    Map<Path, StringBuilder> result = new HashMap<>(bindingsByFile.size());
    for (Entry<URI, Set<JaxWsBindingMapping>> binding : bindingsByFile.entrySet()) {
      URI relPath = CoreUtils.relativizeURI(rootWsdlUri, binding.getKey());
      StringBuilder jaxwsBindingContent = JaxWsUtils.getJaxwsBindingContent(targetPackage, relPath, binding.getValue(), lineDelimiter);
      result.put(Paths.get(binding.getKey()), jaxwsBindingContent);
    }
    return result;
  }

  static StringBuilder getJaxwsBindingContent(String wsPackage, URI wsdlLocation, Collection<JaxWsBindingMapping> names, String lineDelimiter) {
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>").append(lineDelimiter);
    sb.append("<!-- binding to customize webservice artifacts (jaxws-namespace: http://java.sun.com/xml/ns/jaxws) -->").append(lineDelimiter);
    sb.append("<jaxws:bindings wsdlLocation=\"").append(wsdlLocation).append("\"").append(lineDelimiter);
    sb.append("  xmlns:jaxws=\"http://java.sun.com/xml/ns/jaxws\"").append(lineDelimiter);
    sb.append("  xmlns:jaxb=\"http://java.sun.com/xml/ns/jaxb\"").append(lineDelimiter);
    sb.append("  xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"").append(lineDelimiter);
    sb.append("  xmlns:xjc=\"http://java.sun.com/xml/ns/jaxb/xjc\"").append(lineDelimiter);
    sb.append("  xmlns:javaee=\"http://java.sun.com/xml/ns/javaee\"").append(lineDelimiter);
    sb.append("  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">").append(lineDelimiter);
    sb.append("  <jaxws:bindings node=\"wsdl:definitions\">").append(lineDelimiter);
    sb.append("    <jaxws:package name=\"").append(wsPackage).append("\"/>").append(lineDelimiter);
    sb.append("  </jaxws:bindings>").append(lineDelimiter);
    for (JaxWsBindingMapping mapping : names) {
      String nodeAttr = null;
      if (mapping.isPortType()) {
        nodeAttr = getPortTypeXPath(mapping.getWsdlName());
      }
      else {
        nodeAttr = getWebServiceXPath(mapping.getWsdlName());
      }
      sb.append("  <jaxws:bindings node=\"").append(nodeAttr).append("\">").append(lineDelimiter);
      sb.append("    <jaxws:class name=\"").append(mapping.getClassName()).append("\" />").append(lineDelimiter);
      sb.append("  </jaxws:bindings>").append(lineDelimiter);
    }
    sb.append("</jaxws:bindings>").append(lineDelimiter);
    return sb;
  }

  /**
   * <h3>{@link JaxWsBindingMapping}</h3> Binding mapping between a name of the WSDL and the corresponding Java class
   * name.
   */
  public static class JaxWsBindingMapping {
    private final boolean m_isPortType;
    private final String m_wsdlName;
    private final String m_className;

    public JaxWsBindingMapping(boolean isPortType, String wsdlName, String className) {
      m_isPortType = isPortType;
      m_wsdlName = wsdlName;
      m_className = className;
    }

    public boolean isPortType() {
      return m_isPortType;
    }

    public String getWsdlName() {
      return m_wsdlName;
    }

    public String getClassName() {
      return m_className;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((m_className == null) ? 0 : m_className.hashCode());
      result = prime * result + (m_isPortType ? 1231 : 1237);
      result = prime * result + ((m_wsdlName == null) ? 0 : m_wsdlName.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      JaxWsBindingMapping other = (JaxWsBindingMapping) obj;
      if (m_className == null) {
        if (other.m_className != null) {
          return false;
        }
      }
      else if (!m_className.equals(other.m_className)) {
        return false;
      }
      if (m_isPortType != other.m_isPortType) {
        return false;
      }
      if (m_wsdlName == null) {
        if (other.m_wsdlName != null) {
          return false;
        }
      }
      else if (!m_wsdlName.equals(other.m_wsdlName)) {
        return false;
      }
      return true;
    }
  }

  /**
   * Gets an XPath {@link String} that resolves the given web service in a WSDL.
   *
   * @param webServiceNameInWsdl
   *          The name of the service the resulting XPath should evaluate to.
   * @return The XPath {@link String} that will evaluate to the given web service within a WSDL.
   */
  public static String getWebServiceXPath(String webServiceNameInWsdl) {
    return WEB_SERVICE_XPATH_START + webServiceNameInWsdl + XPATH_END;
  }

  /**
   * Gets an XPath {@link String} that resolves the given port type in a WSDL.
   *
   * @param portTypeNameInWsdl
   *          The name of the port type the resulting XPath should evaluate to.
   * @return The XPath {@link String} that will evaluate to the given port type within a WSDL.
   */
  public static String getPortTypeXPath(String portTypeNameInWsdl) {
    return PORT_TYPE_XPATH_START + portTypeNameInWsdl + XPATH_END;
  }

  /**
   * Adds the given WSDL details to the given pom.
   *
   * @param pomDocument
   *          The {@link Document} that represents the pom.xml
   * @param wsdlFileName
   *          The WSDL file path relative to the WEB-INF/wsdl/ folder (e.g. myservice/MyService.wsdl)
   * @param bindingFolderName
   *          The name of the binding folder
   * @param bindingFileNames
   *          The file names of all bindings in the binding folder.
   */
  public static void addWsdlToPom(Document pomDocument, String wsdlFileName, String bindingFolderName, Iterable<String> bindingFileNames) {
    Element root = pomDocument.getDocumentElement();
    Element executions = getExecutionsElement(root);
    String nextId = getNextExecutionId(executions);

    Element newExecution = root.getOwnerDocument().createElement(EXECUTION);
    Element idElement = getOrCreateElement(newExecution, ID);
    idElement.setTextContent(nextId);
    Element goalsElement = getOrCreateElement(newExecution, GOALS);
    Element goalElement = getOrCreateElement(goalsElement, GOAL);
    goalElement.setTextContent(WSIMPORT_TOOL_NAME);
    Element configurationElement = getOrCreateElement(newExecution, CONFIGURATION);
    Element wsdlLocationElement = getOrCreateElement(configurationElement, WSDL_LOCATION);
    wsdlLocationElement.setTextContent("WEB-INF/" + WSDL_FOLDER_NAME + '/' + wsdlFileName);
    Element wsdlFilesElement = getOrCreateElement(configurationElement, WSDL_FILES_ELEMENT_NAME);
    Element wsdlFileElement = getOrCreateElement(wsdlFilesElement, WSDL_FILE_ELEMENT_NAME);
    wsdlFileElement.setTextContent(wsdlFileName);
    Element bindingFilesElement = getOrCreateElement(configurationElement, BINDING_FILES_ELEMENT_NAME);

    Element globalBindingFileElement = root.getOwnerDocument().createElement(BINDING_FILE_ELEMENT_NAME);
    globalBindingFileElement.setTextContent(GLOBAL_BINDINGS_FILE_NAME);
    bindingFilesElement.appendChild(globalBindingFileElement);

    for (String fileName : bindingFileNames) {
      Element bindingFileElement = root.getOwnerDocument().createElement(BINDING_FILE_ELEMENT_NAME);
      bindingFileElement.setTextContent(bindingFolderName + '/' + fileName);
      bindingFilesElement.appendChild(bindingFileElement);
    }

    executions.appendChild(newExecution);
  }

  /**
   * Gets all contents of the &lt;bindingFile&gt; tags of the the given WSDL within the given pom.xml.
   *
   * @param pom
   *          The pom.xml {@link Document}.
   * @param wsdlFileName
   *          The WSDL file name as it appears in the &lt;wsdlFile&gt; tag of the pom.
   * @return A {@link List} holding all binding paths that belong to the given WSDL.
   * @throws XPathExpressionException
   */
  public static List<String> getBindingPathsFromPom(Document pom, String wsdlFileName) throws XPathExpressionException {
    if (wsdlFileName.indexOf('\'') >= 0) {
      throw new IllegalArgumentException("apos character (') is not allowed in a WSDL file name.");
    }
    final String prefix = "p";
    final String p = prefix + ":";
    String lc = wsdlFileName.toLowerCase();
    String uc = wsdlFileName.toUpperCase();
    StringBuilder bindingFilesXpathBuilder = new StringBuilder();
    bindingFilesXpathBuilder.append(p).append(PROJECT).append('/').append(p).append(BUILD).append('/').append(p).append(PLUGINS).append('/').append(p).append(PLUGIN)
        .append("[./").append(p).append(GROUP_ID).append("='").append(CODEHAUS_GROUP_ID).append("' and ./").append(p).append(ARTIFACT_ID).append("='").append(JAXWS_MAVEN_PLUGIN_ARTIFACT_ID).append("']")
        .append('/').append(p).append(EXECUTIONS).append('/').append(p).append(EXECUTION).append('/').append(p).append(CONFIGURATION)
        .append("[translate(./").append(p).append(WSDL_FILES_ELEMENT_NAME).append('/').append(p).append(WSDL_FILE_ELEMENT_NAME).append(", '").append(uc).append("', '").append(lc).append("')='").append(lc).append("']/").append(p)
        .append(BINDING_FILES_ELEMENT_NAME).append('/').append(p).append(BINDING_FILE_ELEMENT_NAME);

    List<Element> nl = CoreUtils.evaluateXPath(bindingFilesXpathBuilder.toString(), pom, prefix, POM_XML_NAMESPACE);
    if (nl.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> paths = new ArrayList<>(nl.size());
    for (Element e : nl) {
      String content = e.getTextContent();
      if (StringUtils.isNotBlank(content)) {
        paths.add(content.trim());
      }
    }
    return paths;
  }

  static String getNextExecutionId(Element executions) {
    int curNum = 1;
    final String idPrefix = WSIMPORT_TOOL_NAME + '-';
    NodeList children = executions.getChildNodes();
    while (isExecutionIdUsed(idPrefix + curNum, children)) {
      curNum++;
    }
    return idPrefix + curNum;
  }

  static boolean isExecutionIdUsed(String id, NodeList executionList) {
    for (int i = 0; i < executionList.getLength(); i++) {
      Node node = executionList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element idElement = CoreUtils.getFirstChildElement((Element) node, ID);
        if (idElement != null && id.equals(idElement.getTextContent())) {
          return true;
        }
      }
    }
    return false;
  }

  static Element getExecutionsElement(Element root) {
    Element build = getOrCreateElement(root, BUILD);
    Element plugins = getOrCreateElement(build, PLUGINS);
    Element jaxWsMavenPluginElement = getOrCreateJaxWsMavenPluginElement(plugins);
    return getOrCreateElement(jaxWsMavenPluginElement, EXECUTIONS);
  }

  static Element getOrCreateJaxWsMavenPluginElement(Element pluginsElement) {
    NodeList plugins = pluginsElement.getChildNodes();
    for (int i = 0; i < plugins.getLength(); i++) {
      Node plugin = plugins.item(i);
      if (plugin.getNodeType() == Node.ELEMENT_NODE && PLUGIN.equals(plugin.getNodeName())) {
        Element pluginCandidate = (Element) plugin;
        Element group = CoreUtils.getFirstChildElement(pluginCandidate, GROUP_ID);
        Element artifact = CoreUtils.getFirstChildElement(pluginCandidate, ARTIFACT_ID);
        if (group != null && CODEHAUS_GROUP_ID.equals(group.getTextContent()) && artifact != null && JAXWS_MAVEN_PLUGIN_ARTIFACT_ID.equals(artifact.getTextContent())) {
          return pluginCandidate;
        }
      }
    }

    // plugin does not exist yet: create a new one
    Element plugin = getOrCreateElement(pluginsElement, PLUGIN);
    Element groupId = getOrCreateElement(plugin, GROUP_ID);
    groupId.setTextContent(CODEHAUS_GROUP_ID);
    Element artifactId = getOrCreateElement(plugin, ARTIFACT_ID);
    artifactId.setTextContent(JAXWS_MAVEN_PLUGIN_ARTIFACT_ID);
    return plugin;
  }

  static Element getOrCreateElement(Element parent, String tagName) {
    Element element = CoreUtils.getFirstChildElement(parent, tagName);
    if (element != null) {
      return element;
    }
    element = parent.getOwnerDocument().createElement(tagName);
    parent.appendChild(element);
    return element;
  }

  /**
   * Gets the {@link Element} of the given JaxWs bindings {@link Document} whose node attribute has the given value.
   *
   * @param nodeValue
   *          The node attribute value to search.
   * @param document
   *          The document to search in.
   * @return The {@link Element} with the given node attribute value or <code>null</code> if it could not be found.
   * @throws XPathExpressionException
   */
  public static Element getJaxWsBindingElement(String nodeValue, Document document) throws XPathExpressionException {
    StringBuilder xPath = new StringBuilder();
    final String prefix = "jaxws";
    final String p = prefix + ':';
    xPath.append(p).append(BINDINGS_ELEMENT_NAME).append('/').append(p).append(BINDINGS_ELEMENT_NAME);
    List<Element> bindings = CoreUtils.evaluateXPath(xPath.toString(), document, prefix, JAX_WS_NAMESPACE);
    for (Element binding : bindings) {
      String nodeAttribValue = binding.getAttribute(BINDINGS_NODE_ATTRIBUTE_NAME);
      if (nodeValue.equals(nodeAttribValue)) {
        return binding;
      }
    }
    return null;
  }

  /**
   * Removes a set of common suffixes used in web services. It is ensured that the resulting string is not empty unless
   * the input is already empty.
   *
   * @param input
   *          The input for which the suffixes should be removed.
   * @return The input with all common suffixes removed.
   */
  public static String removeCommonSuffixes(String input) {
    if (StringUtils.isBlank(input)) {
      return input;
    }

    Collection<String> suffixes = new LinkedList<>();
    suffixes.add("xml");
    suffixes.add("soap");
    suffixes.add("porttype");
    suffixes.add("port");
    suffixes.add("webservice");
    suffixes.add("services");
    suffixes.add("service");

    for (String s : suffixes) {
      if (input.toLowerCase().endsWith(s)) {
        String newInputCandidate = input.substring(0, input.length() - s.length());
        if (StringUtils.isBlank(newInputCandidate)) {
          return input; // cancel suffix removal if we come to an empty name
        }
        input = newInputCandidate;
      }
    }
    return input;
  }
}
