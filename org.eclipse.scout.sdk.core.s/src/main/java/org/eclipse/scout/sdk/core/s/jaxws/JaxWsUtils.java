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
package org.eclipse.scout.sdk.core.s.jaxws;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <h3>{@link JaxWsUtils}</h3>
 *
 * @since 5.2.0
 */
public final class JaxWsUtils {

  public static final String WSDL_FILE_EXTENSION = ".wsdl";
  public static final String ENTRY_POINT_DEFINITION_ENDPOINT_INTERFACE_ATTRIBUTE = "endpointInterface";
  public static final String ENTRY_POINT_DEFINITION_NAME_ATTRIBUTE = "entryPointName";
  public static final String ENTRY_POINT_DEFINITION_SERVICE_NAME_ATTRIBUTE = "serviceName";
  public static final String ENTRY_POINT_DEFINITION_PORT_NAME_ATTRIBUTE = "portName";
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
  public static final String MODULE_REL_WEBINF_FOLDER_PATH = ISourceFolders.MAIN_RESOURCE_FOLDER + "/WEB-INF";
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
  public static final String JAXWS_MAVEN_PLUGIN_GROUP_ID = "com.sun.xml.ws";
  public static final String JAXWS_MAVEN_PLUGIN_ARTIFACT_ID = "jaxws-maven-plugin";

  public static final String PACKAGE_XPATH = "wsdl:definitions";
  private static final String WEB_SERVICE_XPATH_START = "wsdl:definitions/wsdl:service[@name='";
  private static final String PORT_TYPE_XPATH_START = "wsdl:definitions/wsdl:portType[@name='";
  private static final String XPATH_END = "']";

  private JaxWsUtils() {
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

    Element newExecution = root.getOwnerDocument().createElement(IMavenConstants.EXECUTION);
    Element idElement = getOrCreateElement(newExecution, IMavenConstants.ID);
    idElement.setTextContent(nextId);
    Element goalsElement = getOrCreateElement(newExecution, IMavenConstants.GOALS);
    Element goalElement = getOrCreateElement(goalsElement, IMavenConstants.GOAL);
    goalElement.setTextContent(WSIMPORT_TOOL_NAME);
    Element configurationElement = getOrCreateElement(newExecution, IMavenConstants.CONFIGURATION);
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
   *           if there is an error in the xpath expression
   */
  public static List<String> getBindingPathsFromPom(Node pom, String wsdlFileName) throws XPathExpressionException {
    if (wsdlFileName.indexOf('\'') >= 0) {
      throw new IllegalArgumentException("apos character (') is not allowed in a WSDL file name.");
    }
    String prefix = "p";
    String p = prefix + ':';
    String lc = wsdlFileName.toLowerCase(Locale.ENGLISH);
    String uc = wsdlFileName.toUpperCase(Locale.ENGLISH);
    StringBuilder bindingFilesXpathBuilder = new StringBuilder();
    bindingFilesXpathBuilder.append(p).append(IMavenConstants.PROJECT).append('/').append(p).append(IMavenConstants.BUILD).append('/').append(p).append(IMavenConstants.PLUGINS).append('/').append(p).append(IMavenConstants.PLUGIN)
        .append("[./").append(p).append(IMavenConstants.GROUP_ID).append("='").append(JAXWS_MAVEN_PLUGIN_GROUP_ID).append("' and ./").append(p).append(IMavenConstants.ARTIFACT_ID).append("='").append(JAXWS_MAVEN_PLUGIN_ARTIFACT_ID)
        .append("']")
        .append('/').append(p).append(IMavenConstants.EXECUTIONS).append('/').append(p).append(IMavenConstants.EXECUTION).append('/').append(p).append(IMavenConstants.CONFIGURATION)
        .append("[translate(./").append(p).append(WSDL_FILES_ELEMENT_NAME).append('/').append(p).append(WSDL_FILE_ELEMENT_NAME).append(", '").append(uc).append("', '").append(lc).append("')='").append(lc).append("']/").append(p)
        .append(BINDING_FILES_ELEMENT_NAME).append('/').append(p).append(BINDING_FILE_ELEMENT_NAME);

    List<Element> nl = Xml.evaluateXPath(bindingFilesXpathBuilder.toString(), pom, prefix, IMavenConstants.POM_XML_NAMESPACE);
    if (nl.isEmpty()) {
      return emptyList();
    }

    List<String> paths = new ArrayList<>(nl.size());
    for (Element e : nl) {
      String content = e.getTextContent();
      if (Strings.hasText(content)) {
        paths.add(content.trim());
      }
    }
    return paths;
  }

  static String getNextExecutionId(Node executions) {
    int curNum = 1;
    String idPrefix = WSIMPORT_TOOL_NAME + '-';
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
        Optional<Element> idElement = Xml.firstChildElement(node, IMavenConstants.ID);
        if (idElement.isPresent() && id.equals(idElement.get().getTextContent())) {
          return true;
        }
      }
    }
    return false;
  }

  static Element getExecutionsElement(Node root) {
    Element build = getOrCreateElement(root, IMavenConstants.BUILD);
    Element plugins = getOrCreateElement(build, IMavenConstants.PLUGINS);
    Element jaxWsMavenPluginElement = getOrCreateJaxWsMavenPluginElement(plugins);
    return getOrCreateElement(jaxWsMavenPluginElement, IMavenConstants.EXECUTIONS);
  }

  static Element getOrCreateJaxWsMavenPluginElement(Node pluginsElement) {
    NodeList plugins = pluginsElement.getChildNodes();
    for (int i = 0; i < plugins.getLength(); i++) {
      Node plugin = plugins.item(i);
      if (plugin.getNodeType() == Node.ELEMENT_NODE && IMavenConstants.PLUGIN.equals(plugin.getNodeName())) {
        Element pluginCandidate = (Element) plugin;
        Optional<Element> group = Xml.firstChildElement(pluginCandidate, IMavenConstants.GROUP_ID);
        Optional<Element> artifact = Xml.firstChildElement(pluginCandidate, IMavenConstants.ARTIFACT_ID);
        if (group.isPresent() && JAXWS_MAVEN_PLUGIN_GROUP_ID.equals(group.get().getTextContent())
            && artifact.isPresent() && JAXWS_MAVEN_PLUGIN_ARTIFACT_ID.equals(artifact.get().getTextContent())) {
          return pluginCandidate;
        }
      }
    }

    // plugin does not exist yet: create a new one
    Element plugin = getOrCreateElement(pluginsElement, IMavenConstants.PLUGIN);
    Element groupId = getOrCreateElement(plugin, IMavenConstants.GROUP_ID);
    groupId.setTextContent(JAXWS_MAVEN_PLUGIN_GROUP_ID);
    Element artifactId = getOrCreateElement(plugin, IMavenConstants.ARTIFACT_ID);
    artifactId.setTextContent(JAXWS_MAVEN_PLUGIN_ARTIFACT_ID);
    return plugin;
  }

  static Element getOrCreateElement(Node parent, String tagName) {
    return Xml.firstChildElement(parent, tagName)
        .orElseGet(() -> {
          Element element = parent.getOwnerDocument().createElement(tagName);
          parent.appendChild(element);
          return element;
        });
  }

  /**
   * Gets the {@link Element} of the given JaxWs bindings {@link Document} whose node attribute has the given value.
   *
   * @param nodeValue
   *          The node attribute value to search.
   * @param document
   *          The document to search in.
   * @return The {@link Element} with the given node attribute value or {@code null} if it could not be found.
   * @throws XPathExpressionException
   *           if there is an error in the xpath expression
   */
  public static Element getJaxWsBindingElement(String nodeValue, Node document) throws XPathExpressionException {
    StringBuilder xPath = new StringBuilder();
    String prefix = "jaxws";
    String p = prefix + ':';
    xPath.append(p).append(BINDINGS_ELEMENT_NAME).append('/').append(p).append(BINDINGS_ELEMENT_NAME);
    List<Element> bindings = Xml.evaluateXPath(xPath.toString(), document, prefix, JAX_WS_NAMESPACE);
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
    if (Strings.isBlank(input)) {
      return input;
    }

    String[] suffixes = {"xml", "soap", "porttype", "port", "webservice", "services", "service"};
    for (String s : suffixes) {
      if (input.toLowerCase(Locale.ENGLISH).endsWith(s)) {
        String newInputCandidate = input.substring(0, input.length() - s.length());
        if (Strings.isBlank(newInputCandidate)) {
          return input; // cancel suffix removal if we come to an empty name
        }
        input = newInputCandidate;
      }
    }
    return input;
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
      int prime = 31;
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
      return m_isPortType == other.m_isPortType
          && Objects.equals(m_className, other.m_className)
          && Objects.equals(m_wsdlName, other.m_wsdlName);
    }
  }
}
