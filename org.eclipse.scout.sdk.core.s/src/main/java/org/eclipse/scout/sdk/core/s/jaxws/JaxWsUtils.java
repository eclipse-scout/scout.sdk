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
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.apidef.IScoutVariousApi;
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
  public static final String BINDINGS_NAME_ATTRIBUTE = "name";
  public static final String BINDINGS_CLASS_ELEMENT_NAME = "class";
  public static final String BINDING_PACKAGE_ELEMENT_NAME = "package";
  public static final String BINDINGS_NODE_ATTRIBUTE_NAME = "node";
  public static final String JAX_WS_NAMESPACE = "http://java.sun.com/xml/ns/jaxws";
  public static final String JAX_B_NAMESPACE = "http://java.sun.com/xml/ns/jaxb";
  public static final String BINDINGS_ELEMENT_NAME = "bindings";
  public static final String GENERATE_ELEMENT_ATTRIBUTE_NAME = "generateElementProperty";
  public static final String GLOBAL_BINDINGS_ELEMENT_NAME = "globalBindings";
  public static final String MODULE_REL_WEB_INF_FOLDER_PATH = ISourceFolders.MAIN_RESOURCE_FOLDER + "/WEB-INF";
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
  public static final String WS_IMPORT_TOOL_NAME = "wsimport";
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
   * @param api
   *          The {@link IScoutApi} to use. Must not be {@code null}.
   */
  public static void addWsdlToPom(Document pomDocument, String wsdlFileName, String bindingFolderName, Iterable<String> bindingFileNames, IScoutVariousApi api) {
    var root = pomDocument.getDocumentElement();
    var executions = getExecutionsElement(root, api);
    var nextId = getNextExecutionId(executions);

    var newExecution = root.getOwnerDocument().createElement(IMavenConstants.EXECUTION);
    var idElement = getOrCreateElement(newExecution, IMavenConstants.ID);
    idElement.setTextContent(nextId);
    var goalsElement = getOrCreateElement(newExecution, IMavenConstants.GOALS);
    var goalElement = getOrCreateElement(goalsElement, IMavenConstants.GOAL);
    goalElement.setTextContent(WS_IMPORT_TOOL_NAME);
    var configurationElement = getOrCreateElement(newExecution, IMavenConstants.CONFIGURATION);
    var wsdlLocationElement = getOrCreateElement(configurationElement, WSDL_LOCATION);
    wsdlLocationElement.setTextContent("WEB-INF/" + WSDL_FOLDER_NAME + '/' + wsdlFileName);
    var wsdlFilesElement = getOrCreateElement(configurationElement, WSDL_FILES_ELEMENT_NAME);
    var wsdlFileElement = getOrCreateElement(wsdlFilesElement, WSDL_FILE_ELEMENT_NAME);
    wsdlFileElement.setTextContent(wsdlFileName);
    var bindingFilesElement = getOrCreateElement(configurationElement, BINDING_FILES_ELEMENT_NAME);

    var globalBindingFileElement = root.getOwnerDocument().createElement(BINDING_FILE_ELEMENT_NAME);
    globalBindingFileElement.setTextContent(GLOBAL_BINDINGS_FILE_NAME);
    bindingFilesElement.appendChild(globalBindingFileElement);

    for (var fileName : bindingFileNames) {
      var bindingFileElement = root.getOwnerDocument().createElement(BINDING_FILE_ELEMENT_NAME);
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
   * @param api
   *          The {@link IScoutApi} to use. Must not be {@code null}.
   * @return A {@link List} holding all binding paths that belong to the given WSDL.
   * @throws XPathExpressionException
   *           if there is an error in the xpath expression
   */
  public static List<String> getBindingPathsFromPom(Node pom, String wsdlFileName, IScoutVariousApi api) throws XPathExpressionException {
    if (wsdlFileName.indexOf('\'') >= 0) {
      throw new IllegalArgumentException("apos character (') is not allowed in a WSDL file name.");
    }
    var prefix = "p";
    var p = prefix + ':';
    var lc = wsdlFileName.toLowerCase(Locale.US);
    var uc = wsdlFileName.toUpperCase(Locale.US);
    var bindingFilesXPathBuilder = getJaxWsMavenPluginXPath(p, api)
        .append('/').append(p).append(IMavenConstants.EXECUTIONS).append('/').append(p).append(IMavenConstants.EXECUTION).append('/').append(p).append(IMavenConstants.CONFIGURATION)
        .append("[translate(./").append(p).append(WSDL_FILES_ELEMENT_NAME).append('/').append(p).append(WSDL_FILE_ELEMENT_NAME).append(", '").append(uc).append("', '").append(lc).append("')='").append(lc).append("']/").append(p)
        .append(BINDING_FILES_ELEMENT_NAME).append('/').append(p).append(BINDING_FILE_ELEMENT_NAME);

    var nl = Xml.evaluateXPath(bindingFilesXPathBuilder.toString(), pom, prefix, IMavenConstants.POM_XML_NAMESPACE);
    if (nl.isEmpty()) {
      return emptyList();
    }
    return nl.stream()
        .map(Node::getTextContent)
        .filter(Strings::hasText)
        .map(String::trim)
        .collect(toList());
  }

  /**
   * Gets a xpath {@link StringBuilder} selecting the jax-ws maven build plugin in a pom.xml
   * (project/build/plugins/plugin[groupId=jaxws and artifactId=jaxws]
   * 
   * @param p
   *          The xml tag namespace prefix including a trailing colon (:). Must not be {@code null} but may be an empty
   *          {@link String}.
   * @param api
   *          The {@link IScoutApi} to use. Must not be {@code null}.
   * @return A {@link StringBuilder} containing the xpath expression.
   */
  public static StringBuilder getJaxWsMavenPluginXPath(String p, IScoutVariousApi api) {
    return new StringBuilder().append(p).append(IMavenConstants.PROJECT).append('/').append(p).append(IMavenConstants.BUILD).append('/').append(p).append(IMavenConstants.PLUGINS).append('/').append(p).append(IMavenConstants.PLUGIN)
        .append("[./").append(p).append(IMavenConstants.GROUP_ID).append("='").append(api.JaxWsConstants().mavenPluginGroupId())
        .append("' and ./").append(p).append(IMavenConstants.ARTIFACT_ID).append("='").append(JAXWS_MAVEN_PLUGIN_ARTIFACT_ID).append("']");
  }

  static String getNextExecutionId(Node executions) {
    var curNum = 1;
    var idPrefix = WS_IMPORT_TOOL_NAME + '-';
    var children = executions.getChildNodes();
    while (isExecutionIdUsed(idPrefix + curNum, children)) {
      curNum++;
    }
    return idPrefix + curNum;
  }

  static boolean isExecutionIdUsed(String id, NodeList executionList) {
    return IntStream.range(0, executionList.getLength())
        .mapToObj(executionList::item)
        .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
        .map(node -> Xml.firstChildElement(node, IMavenConstants.ID))
        .flatMap(Optional::stream)
        .anyMatch(idElement -> id.equals(idElement.getTextContent().trim()));
  }

  static Element getExecutionsElement(Node root, IScoutVariousApi api) {
    var build = getOrCreateElement(root, IMavenConstants.BUILD);
    var plugins = getOrCreateElement(build, IMavenConstants.PLUGINS);
    var jaxWsMavenPluginElement = getOrCreateJaxWsMavenPluginElement(plugins, api);
    return getOrCreateElement(jaxWsMavenPluginElement, IMavenConstants.EXECUTIONS);
  }

  static Element getOrCreateJaxWsMavenPluginElement(Node pluginsElement, IScoutVariousApi api) {
    var plugins = pluginsElement.getChildNodes();
    var jaxWsMavenPluginGroupId = api.JaxWsConstants().mavenPluginGroupId();
    for (var i = 0; i < plugins.getLength(); i++) {
      var plugin = plugins.item(i);
      if (plugin.getNodeType() == Node.ELEMENT_NODE && IMavenConstants.PLUGIN.equals(plugin.getNodeName())) {
        var pluginCandidate = (Element) plugin;
        var group = Xml.firstChildElement(pluginCandidate, IMavenConstants.GROUP_ID);
        var artifact = Xml.firstChildElement(pluginCandidate, IMavenConstants.ARTIFACT_ID);
        if (group.isPresent() && jaxWsMavenPluginGroupId.equals(group.get().getTextContent())
            && artifact.isPresent() && JAXWS_MAVEN_PLUGIN_ARTIFACT_ID.equals(artifact.get().getTextContent())) {
          return pluginCandidate;
        }
      }
    }

    // plugin does not exist yet: create a new one
    var plugin = getOrCreateElement(pluginsElement, IMavenConstants.PLUGIN);
    var groupId = getOrCreateElement(plugin, IMavenConstants.GROUP_ID);
    groupId.setTextContent(jaxWsMavenPluginGroupId);
    var artifactId = getOrCreateElement(plugin, IMavenConstants.ARTIFACT_ID);
    artifactId.setTextContent(JAXWS_MAVEN_PLUGIN_ARTIFACT_ID);
    return plugin;
  }

  static Element getOrCreateElement(Node parent, String tagName) {
    return Xml.firstChildElement(parent, tagName)
        .orElseGet(() -> {
          var element = parent.getOwnerDocument().createElement(tagName);
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
    var xPath = new StringBuilder();
    var prefix = "jaxws";
    var p = prefix + ':';
    xPath.append(p).append(BINDINGS_ELEMENT_NAME).append('/').append(p).append(BINDINGS_ELEMENT_NAME);
    var bindings = Xml.evaluateXPath(xPath.toString(), document, prefix, JAX_WS_NAMESPACE);
    for (var binding : bindings) {
      var nodeAttribValue = binding.getAttribute(BINDINGS_NODE_ATTRIBUTE_NAME);
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
    for (var s : suffixes) {
      if (input.toLowerCase(Locale.US).endsWith(s)) {
        var newInputCandidate = input.substring(0, input.length() - s.length());
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
      var prime = 31;
      var result = 1;
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
      var other = (JaxWsBindingMapping) obj;
      return m_isPortType == other.m_isPortType
          && Objects.equals(m_className, other.m_className)
          && Objects.equals(m_wsdlName, other.m_wsdlName);
    }
  }
}
