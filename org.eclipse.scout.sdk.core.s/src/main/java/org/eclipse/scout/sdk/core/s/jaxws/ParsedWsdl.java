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

import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.xml.sax.InputSource;

/**
 * <h3>{@link ParsedWsdl}</h3> Parses the contents of a WSDL file and its referenced WSDLs and Schemas into a single
 * representation holding the most important objects.
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ParsedWsdl {

  private final Map<URI, String> m_referencedResourceUris;
  private final Map<Service, URI> m_services;
  private final Map<PortType, URI> m_portTypes;
  private final Map<Service, Map<String /*port name*/, QName /*port type name*/>> m_portTypesByService;
  private final Map<Service, WebServiceNames> m_namesByService;

  protected ParsedWsdl() {
    m_referencedResourceUris = new HashMap<>();
    m_services = new HashMap<>();
    m_portTypes = new HashMap<>();
    m_portTypesByService = new HashMap<>();
    m_namesByService = new HashMap<>();
  }

  /**
   * @return Gets a {@link Map} holding a value for all referenced resources of this WSDL.<br>
   *         The key of the map holds the absolute {@link URI} of the resource e.g. to access its content.<br>
   *         The value of the map holds the decoded file path (relative to the root WSDL). Decoded means all URI encoded
   *         characters (like '%20') have been decoded (to e.g. ' ').<br>
   *         The root WSDL itself is not part of the resulting {@link Map}.
   */
  public Map<URI, String> getReferencedResources() {
    return Collections.unmodifiableMap(m_referencedResourceUris);
  }

  /**
   * @return A {@link Map} holding a value for all web services that exist in this WSDL.<br>
   *         The key of the map holds the service and the corresponding value holds an {@link URI} pointing to the WSDL
   *         that contains the service. This may be different to the root WSDL if the service is included in an imported
   *         WSDL. In this case this {@link URI} can also be found in {@link #getReferencedResources()}.
   */
  public Map<Service, URI> getWebServices() {
    return Collections.unmodifiableMap(m_services);
  }

  /**
   * @return A {@link Map} holding a value for all port types that exist in this WSDL.<br>
   *         The key of the map holds the port type and the corresponding value holds an {@link URI} pointing to the
   *         WSDL that contains the port type. This may be different to the root WSDL if the port type is included in an
   *         imported WSDL. In this case this {@link URI} can also be found in {@link #getReferencedResources()}.
   */
  public Map<PortType, URI> getPortTypes() {
    return Collections.unmodifiableMap(m_portTypes);
  }

  /**
   * @return <code>true</code> if this WSDL does not contain any supported services or port types. <code>false</code>
   *         otherwise.
   */
  public boolean isEmpty() {
    return m_portTypes.isEmpty();
  }

  /**
   * Gets all {@link PortType}s of the given {@link Service}.
   *
   * @param service
   *          The {@link Service} for which the {@link PortType}s should be returned.
   * @return A {@link Set} holding all {@link PortType}s of the given {@link Service}.
   */
  public Set<PortType> getPortTypes(Service service) {
    Map<String, QName> portTypesByPort = m_portTypesByService.get(service);
    if (portTypesByPort == null || portTypesByPort.isEmpty()) {
      return Collections.emptySet();
    }
    Set<PortType> result = new HashSet<>(portTypesByPort.size());
    for (QName portTypeName : portTypesByPort.values()) {
      PortType portType = getPortType(portTypeName);
      if (portType != null) {
        result.add(portType);
      }
    }
    return result;
  }

  /**
   * @param service
   *          The service for which the ports should be returned.
   * @return Gets a {@link Map} holding all ports (key) and the corresponding port type name (value).
   */
  public Map<String, QName> getPorts(Service service) {
    Map<String, QName> map = m_portTypesByService.get(service);
    if (map == null) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(map);
  }

  /**
   * Gets the port name of the given port type within the given service
   *
   * @param service
   *          The {@link Service} for which the port name should be returned.
   * @param portType
   *          The port type whose port name should be returned.
   * @return The port name or <code>null</code>.
   */
  public String getPortName(Service service, PortType portType) {
    QName nameToSearch = portType.getQName();
    for (Entry<String, QName> entry : getPorts(service).entrySet()) {
      if (entry.getValue().equals(nameToSearch)) {
        return entry.getKey(); // port name
      }
    }
    return null;
  }

  /**
   * Gets the {@link PortType} with the given {@link QName}.
   *
   * @param name
   *          The qualified name of the {@link PortType} to return.
   * @return The {@link PortType} with the given name or <code>null</code> if it could not be found.
   */
  public PortType getPortType(QName name) {
    for (PortType candidate : m_portTypes.keySet()) {
      if (name.equals(candidate.getQName())) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * @return Gets a {@link Map} holding all {@link WebServiceNames} for all {@link Service}s of this WSDL.
   */
  public Map<Service, WebServiceNames> getServiceNames() {
    return Collections.unmodifiableMap(m_namesByService);
  }

  protected void putPortType(PortType portType, URI source) {
    m_portTypes.put(portType, source);
  }

  protected void putService(Service service, URI source) {
    m_services.put(service, source);
  }

  protected String putReferencedResource(URI absolutePath, URI pathFromWsdlRoot) throws UnsupportedEncodingException {
    return m_referencedResourceUris.put(absolutePath.normalize(), URLDecoder.decode(pathFromWsdlRoot.toString(), StandardCharsets.UTF_8.name()));
  }

  @SuppressWarnings("unchecked")
  protected void completeMapping() {
    Set<Service> usedServices = new HashSet<>(m_services.size());
    Set<PortType> usedPortTypes = new HashSet<>(m_portTypes.size());
    for (Service service : m_services.keySet()) {
      Map<String, Port> ports = service.getPorts();
      for (Port port : ports.values()) {
        for (ExtensibilityElement element : extensibilityElementsOf(port)) {
          if (element instanceof SOAPAddress) {
            @SuppressWarnings("squid:S2259")
            Binding binding = port.getBinding();
            if (isBindingSupported(binding)) {
              PortType portType = binding.getPortType();
              if (portType != null) {
                usedServices.add(service);
                usedPortTypes.add(portType);
                Map<String, QName> portTypesByPortName = m_portTypesByService.get(service);
                if (portTypesByPortName == null) {
                  portTypesByPortName = new HashMap<>();
                  m_portTypesByService.put(service, portTypesByPortName);
                }
                portTypesByPortName.put(port.getName(), portType.getQName());
              }
            }
          }
        }
      }
    }
    m_portTypes.keySet().retainAll(usedPortTypes);
    m_services.keySet().retainAll(usedServices);
    for (Service s : m_services.keySet()) {
      m_namesByService.put(s, new WebServiceNames(s.getQName().getLocalPart()));
    }
  }

  protected static Definition parseWsdl(URI documentBase, InputSource wsdl) throws WSDLException {
    WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
    reader.setFeature("javax.wsdl.importDocuments", true);
    reader.setFeature("javax.wsdl.verbose", false);
    String documentBaseUri = null;
    if (documentBase != null) {
      documentBaseUri = documentBase.toString();
    }
    return reader.readWSDL(documentBaseUri, wsdl);
  }

  /**
   * Create a new instance using the given WSDL base {@link URI} and WSDL content.
   *
   * @param documentBase
   *          The base {@link URI} of the given WSDL content. This is the location of the WSDL with the given content
   *          and is required to locate relative referenced resources in the given WSDL content.
   * @param wsdlContent
   *          The content of the WSDL file.
   * @param loadSchemas
   *          If {@code true} the XSD schemas are parsed recursively. If {@code false} no schemas are parsed. These will
   *          then be missing in {@link #getReferencedResources()}.
   * @return The created {@link ParsedWsdl} instance
   * @throws WSDLException
   * @throws UnsupportedEncodingException
   */
  public static ParsedWsdl create(URI documentBase, String wsdlContent, boolean loadSchemas) throws WSDLException, UnsupportedEncodingException {
    Definition wsdl = parseWsdl(documentBase, new InputSource(new StringReader(wsdlContent)));
    return create(wsdl, loadSchemas);
  }

  /**
   * Create a new instance using the given WSDL base {@link URI} and {@link InputStream} pointing to the WSDL content.
   *
   * @param documentBase
   *          The base {@link URI} of the given WSDL content. This is the location of the WSDL with the given content
   *          and is required to locate relative referenced resources in the given WSDL content.
   * @param is
   *          The {@link InputStream} that delivers the WSDL content.
   * @param loadSchemas
   *          If {@code true} the XSD schemas are parsed recursively. If {@code false} no schemas are parsed. These will
   *          then be missing in {@link #getReferencedResources()}.
   * @return The created {@link ParsedWsdl} instance
   * @throws WSDLException
   * @throws UnsupportedEncodingException
   */
  public static ParsedWsdl create(URI documentBase, InputStream is, boolean loadSchemas) throws WSDLException, UnsupportedEncodingException {
    Definition wsdl = parseWsdl(documentBase, new InputSource(Validate.notNull(is)));
    return create(wsdl, loadSchemas);
  }

  protected static ParsedWsdl create(Definition wsdl, boolean loadSchemas) throws UnsupportedEncodingException {
    if (wsdl == null) {
      return null;
    }

    ParsedWsdl result = new ParsedWsdl();
    URI rootDirUri = CoreUtils.getParentURI(URI.create(wsdl.getDocumentBaseURI()));
    parseWsdlRec(wsdl, rootDirUri, URI.create(""), result, loadSchemas);
    result.completeMapping();
    return result;
  }

  @SuppressWarnings("unchecked")
  protected static boolean isBindingSupported(Binding b) {
    if (b == null) {
      return false;
    }

    List<BindingOperation> ops = b.getBindingOperations();
    if (ops.isEmpty()) {
      return false;
    }

    for (BindingOperation op : ops) {
      List<ExtensibilityElement> opElements = extensibilityElementsOf(op);
      @SuppressWarnings("squid:S2259")
      List<ExtensibilityElement> outputElements = extensibilityElementsOf(op.getBindingOutput());
      List<ExtensibilityElement> inputElements = extensibilityElementsOf(op.getBindingInput());

      List<ExtensibilityElement> all = new ArrayList<>(opElements.size() + outputElements.size() + inputElements.size());
      all.addAll(opElements);
      all.addAll(outputElements);
      all.addAll(inputElements);
      for (ExtensibilityElement element : all) {
        // encoded is not supported in JAX WS 2.0
        if (element instanceof SOAPBody && "encoded".equalsIgnoreCase(((SOAPBody) element).getUse())) {
          return false;
        }
      }
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  protected static List<ExtensibilityElement> extensibilityElementsOf(ElementExtensible e) {
    if (e == null) {
      return Collections.emptyList();
    }
    return e.getExtensibilityElements();
  }

  @SuppressWarnings("unchecked")
  protected static void parseWsdlRec(Definition def, URI rootDefUri, URI relPath, ParsedWsdl collector, boolean loadSchemas) throws UnsupportedEncodingException {
    Map<String, List<Import>> imports = def.getImports();
    for (List<Import> iv : imports.values()) {
      for (Import i : iv) {
        Definition innerDef = i.getDefinition();
        if (innerDef != null) {
          URI pathRelativeToRoot = relPath.resolve(i.getLocationURI());
          URI pathAbsolute = URI.create(innerDef.getDocumentBaseURI());
          collector.putReferencedResource(pathAbsolute, pathRelativeToRoot);
          parseWsdlRec(innerDef, rootDefUri, CoreUtils.getParentURI(pathRelativeToRoot), collector, loadSchemas);
        }
      }
    }

    URI uriOfCurrentDefinition = URI.create(def.getDocumentBaseURI()); // may differ from the rootDefUri
    // search for services
    Map<QName, Service> services = def.getServices();
    for (Service service : services.values()) {
      collector.putService(service, uriOfCurrentDefinition);
    }

    // search for port types
    Map<QName, PortType> portTypes = def.getPortTypes();
    for (PortType pt : portTypes.values()) {
      collector.putPortType(pt, uriOfCurrentDefinition);
    }

    // search for referenced xsd files
    Types types = def.getTypes();
    if (types == null) {
      return;
    }

    if (loadSchemas) {
      for (ExtensibilityElement e : extensibilityElementsOf(types)) {
        if (e instanceof Schema) {
          parseSchemasRec((Schema) e, rootDefUri, relPath, collector);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected static void parseSchemasRec(Schema s, URI rootUri, URI relPath, ParsedWsdl collector) throws UnsupportedEncodingException {
    if (s == null) {
      return;
    }

    Map<String, List<SchemaImport>> imports = s.getImports();
    List<SchemaReference> includes = s.getIncludes();
    if (includes.isEmpty() && imports.isEmpty()) {
      return;
    }

    List<SchemaReference> references = new ArrayList<>();
    for (List<SchemaImport> is : imports.values()) {
      references.addAll(is);
    }
    references.addAll(includes);
    if (references.isEmpty()) {
      return;
    }

    for (SchemaReference ref : references) {
      String schemaLocationURI = ref.getSchemaLocationURI();
      URI pathRelativeToRoot = null;
      boolean exists = false;
      if (StringUtils.isBlank(schemaLocationURI)) {
        pathRelativeToRoot = relPath;
      }
      else {
        URI pathAbsolute = URI.create(schemaLocationURI);
        if (StringUtils.isNotBlank(pathAbsolute.getRawFragment())) {
          // the import is a schema-id inside the current document -> no need to import
          // see http://www.w3.org/TR/wsdl20-primer/#schemaIds.wsdl
          continue;
        }

        if (pathAbsolute.isAbsolute()) {
          // its an absolute path -> not possible to make relative
          pathRelativeToRoot = pathAbsolute;
        }
        else {
          pathAbsolute = rootUri.resolve(relPath).resolve(pathAbsolute).normalize();
          pathRelativeToRoot = CoreUtils.relativizeURI(rootUri, pathAbsolute);
        }

        exists = collector.putReferencedResource(pathAbsolute, pathRelativeToRoot) != null;
      }

      if (!exists) {
        parseSchemasRec(ref.getReferencedSchema(), rootUri, CoreUtils.getParentURI(pathRelativeToRoot), collector);
      }
    }
  }

  /**
   * Helper class to map names from the WSDL to java class names as used by Scout.
   */
  public static class WebServiceNames {

    private final String m_wsBaseName; // starts with upper case
    private final String m_wsNameFromWsdl;

    protected WebServiceNames(String wsNameLocalPart) {
      m_wsNameFromWsdl = wsNameLocalPart;
      m_wsBaseName = getBaseName(m_wsNameFromWsdl);
    }

    /**
     * @return The original web service name as it is stored in the WSDL.
     */
    public String getWebServiceNameFromWsdl() {
      return m_wsNameFromWsdl;
    }

    /**
     * @return The default web service class name
     */
    public String getWebServiceClassName() {
      return m_wsBaseName + ISdkProperties.SUFFIX_WS_SERVICE;
    }

    /**
     * @param portTypeNameFromWsdl
     *          The port type name as it appears in the WSDL.
     * @return The default port type class name for the given portType WSDL name
     */
    public String getPortTypeClassName(String portTypeNameFromWsdl) {
      return 'I' + getBaseName(portTypeNameFromWsdl) + ISdkProperties.SUFFIX_WS_PORT_TYPE;
    }

    /**
     * @param portTypeNameFromWsdl
     *          The port type name as it appears in the WSDL.
     * @return The entry point class name (for providers) for the given port type WSDL name.
     */
    public String getEntryPointClassName(String portTypeNameFromWsdl) {
      return getBaseName(portTypeNameFromWsdl) + ISdkProperties.SUFFIX_WS_ENTRY_POINT;
    }

    /**
     * @param portTypeNameFromWsdl
     *          The port type name as it appears in the WSDL.
     * @return The web service client class name (for consumers) for the given port type WSDL name.
     */
    public String getWebServiceClientClassName(String portTypeNameFromWsdl) {
      return getBaseName(portTypeNameFromWsdl) + ISdkProperties.SUFFIX_WS_CLIENT;
    }

    /**
     * @param portTypeNameFromWsdl
     *          The port type name as it appears in the WSDL.
     * @return The web service provider implementation class name for the given port type WSDL name.
     */
    public String getWebServiceProviderImplClassName(String portTypeNameFromWsdl) {
      return getBaseName(portTypeNameFromWsdl) + ISdkProperties.SUFFIX_WS_PROVIDER;
    }

    /**
     * @param portTypeNameFromWsdl
     *          The port type name as it appears in the WSDL.
     * @return The entry point definition class name (for providers) for the given port type WSDL name.
     */
    public String getEntryPointDefinitionClassName(String portTypeNameFromWsdl) {
      return 'I' + getBaseName(portTypeNameFromWsdl) + ISdkProperties.SUFFIX_WS_ENTRY_POINT_DEFINITION;
    }

    protected static String getBaseName(String wsNameFromWsdl) {
      return wsdlNameToJavaName(JaxWsUtils.removeCommonSuffixes(wsNameFromWsdl));
    }

    protected static String wsdlNameToJavaName(String name) {
      String[] parts = name.split("[^a-zA-Z0-9]");
      StringBuilder nameBuilder = new StringBuilder(name.length());
      for (String part : parts) {
        nameBuilder.append(CoreUtils.ensureStartWithUpperCase(part));
      }
      return nameBuilder.toString();
    }
  }
}
