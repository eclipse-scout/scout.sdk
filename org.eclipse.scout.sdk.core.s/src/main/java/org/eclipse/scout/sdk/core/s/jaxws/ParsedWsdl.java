/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.jaxws;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.util.CharSequenceInputStream;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;
import org.xml.sax.InputSource;

/**
 * <h3>{@link ParsedWsdl}</h3> Parses the contents of a WSDL file and its referenced WSDLs and Schemas into a single
 * representation holding the most important objects.
 *
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
    return unmodifiableMap(m_referencedResourceUris);
  }

  /**
   * @return A {@link Map} holding a value for all web services that exist in this WSDL.<br>
   *         The key of the map holds the service and the corresponding value holds an {@link URI} pointing to the WSDL
   *         that contains the service. This may be different to the root WSDL if the service is included in an imported
   *         WSDL. In this case this {@link URI} can also be found in {@link #getReferencedResources()}.
   */
  public Map<Service, URI> getWebServices() {
    return unmodifiableMap(m_services);
  }

  /**
   * @return A {@link Map} holding a value for all port types that exist in this WSDL.<br>
   *         The key of the map holds the port type and the corresponding value holds an {@link URI} pointing to the
   *         WSDL that contains the port type. This may be different to the root WSDL if the port type is included in an
   *         imported WSDL. In this case this {@link URI} can also be found in {@link #getReferencedResources()}.
   */
  public Map<PortType, URI> getPortTypes() {
    return unmodifiableMap(m_portTypes);
  }

  /**
   * @return {@code true} if this WSDL does not contain any supported services or port types. {@code false} otherwise.
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
    var portTypesByPort = m_portTypesByService.get(service);
    if (portTypesByPort == null || portTypesByPort.isEmpty()) {
      return emptySet();
    }
    return portTypesByPort.values().stream()
        .map(this::getPortType)
        .filter(Objects::nonNull)
        .collect(toSet());
  }

  /**
   * @param service
   *          The service for which the ports should be returned.
   * @return Gets a {@link Map} holding all ports (key) and the corresponding port type name (value).
   */
  public Map<String, QName> getPorts(Service service) {
    var map = m_portTypesByService.get(service);
    if (map == null) {
      return emptyMap();
    }
    return unmodifiableMap(map);
  }

  /**
   * Gets the port name of the given port type within the given service
   *
   * @param service
   *          The {@link Service} for which the port name should be returned.
   * @param portType
   *          The port type whose port name should be returned.
   * @return The port name or {@code null}.
   */
  public String getPortName(Service service, PortType portType) {
    var nameToSearch = portType.getQName();
    // port name
    return getPorts(service).entrySet().stream()
        .filter(entry -> entry.getValue().equals(nameToSearch))
        .findFirst()
        .map(Entry::getKey)
        .orElse(null);
  }

  /**
   * Gets the {@link PortType} with the given {@link QName}.
   *
   * @param name
   *          The qualified name of the {@link PortType} to return.
   * @return The {@link PortType} with the given name or {@code null} if it could not be found.
   */
  public PortType getPortType(QName name) {
    return m_portTypes.keySet().stream()
        .filter(candidate -> name.equals(candidate.getQName()))
        .findFirst()
        .orElse(null);
  }

  /**
   * @return Gets a {@link Map} holding all {@link WebServiceNames} for all {@link Service}s of this WSDL.
   */
  public Map<Service, WebServiceNames> getServiceNames() {
    return unmodifiableMap(m_namesByService);
  }

  protected void putPortType(PortType portType, URI source) {
    m_portTypes.put(portType, source);
  }

  protected void putService(Service service, URI source) {
    m_services.put(service, source);
  }

  protected String putReferencedResource(URI absolutePath, URI pathFromWsdlRoot) {
    return m_referencedResourceUris.put(absolutePath.normalize(), URLDecoder.decode(pathFromWsdlRoot.toString(), StandardCharsets.UTF_8));
  }

  @SuppressWarnings("unchecked")
  protected void completeMapping() {
    Collection<Service> usedServices = new HashSet<>(m_services.size());
    Collection<PortType> usedPortTypes = new HashSet<>(m_portTypes.size());
    for (var service : m_services.keySet()) {
      Map<String, Port> ports = service.getPorts();
      for (var port : ports.values()) {
        for (var element : extensibilityElementsOf(port)) {
          if (element instanceof SOAPAddress || element instanceof SOAP12Address) {
            @SuppressWarnings("squid:S2259")
            var binding = port.getBinding();
            if (isBindingSupported(binding)) {
              var portType = binding.getPortType();
              if (portType != null) {
                usedServices.add(service);
                usedPortTypes.add(portType);
                m_portTypesByService.computeIfAbsent(service, k -> new HashMap<>()).put(port.getName(), portType.getQName());
              }
            }
          }
        }
      }
    }
    m_portTypes.keySet().retainAll(usedPortTypes);
    m_services.keySet().retainAll(usedServices);
    for (var s : m_services.keySet()) {
      m_namesByService.put(s, new WebServiceNames(s.getQName().getLocalPart()));
    }
  }

  protected static Definition parseWsdl(URI documentBase, InputSource wsdl) throws WSDLException {
    var reader = WSDLFactory.newInstance().newWSDLReader();
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
   *           on an error parsing the WSDL
   * @throws UnsupportedEncodingException
   *           if the encoding is not supported
   */
  public static ParsedWsdl create(URI documentBase, CharSequence wsdlContent, boolean loadSchemas) throws WSDLException, UnsupportedEncodingException {
    var wsdl = parseWsdl(documentBase, new InputSource(new CharSequenceInputStream(wsdlContent, StandardCharsets.UTF_8)));
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
   *           on an error parsing the WSDL
   * @throws UnsupportedEncodingException
   *           if the encoding is not supported
   */
  public static ParsedWsdl create(URI documentBase, InputStream is, boolean loadSchemas) throws WSDLException, UnsupportedEncodingException {
    var wsdl = parseWsdl(documentBase, new InputSource(Ensure.notNull(is)));
    return create(wsdl, loadSchemas);
  }

  protected static ParsedWsdl create(Definition wsdl, boolean loadSchemas) throws UnsupportedEncodingException {
    if (wsdl == null) {
      return null;
    }

    var result = new ParsedWsdl();
    var rootDirUri = CoreUtils.getParentURI(URI.create(wsdl.getDocumentBaseURI()));
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

    for (var op : ops) {
      var opElements = extensibilityElementsOf(op);
      @SuppressWarnings("squid:S2259")
      var outputElements = extensibilityElementsOf(op.getBindingOutput());
      var inputElements = extensibilityElementsOf(op.getBindingInput());

      Collection<ExtensibilityElement> all = new ArrayList<>(Stream.of(opElements, outputElements, inputElements).mapToInt(List::size).sum());
      all.addAll(opElements);
      all.addAll(outputElements);
      all.addAll(inputElements);
      for (var element : all) {
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
      return emptyList();
    }
    return e.getExtensibilityElements();
  }

  @SuppressWarnings("unchecked")
  protected static void parseWsdlRec(Definition def, URI rootDefUri, URI relPath, ParsedWsdl collector, boolean loadSchemas) {
    Map<String, List<Import>> imports = def.getImports();
    for (var iv : imports.values()) {
      for (var i : iv) {
        var innerDef = i.getDefinition();
        if (innerDef != null) {
          var pathRelativeToRoot = relPath.resolve(i.getLocationURI());
          var pathAbsolute = URI.create(innerDef.getDocumentBaseURI());
          collector.putReferencedResource(pathAbsolute, pathRelativeToRoot);
          parseWsdlRec(innerDef, rootDefUri, CoreUtils.getParentURI(pathRelativeToRoot), collector, loadSchemas);
        }
      }
    }

    var uriOfCurrentDefinition = URI.create(def.getDocumentBaseURI()); // may differ from the rootDefUri
    // search for services
    Map<QName, Service> services = def.getServices();
    for (var service : services.values()) {
      collector.putService(service, uriOfCurrentDefinition);
    }

    // search for port types
    Map<QName, PortType> portTypes = def.getPortTypes();
    for (var pt : portTypes.values()) {
      collector.putPortType(pt, uriOfCurrentDefinition);
    }

    // search for referenced xsd files
    var types = def.getTypes();
    if (types == null) {
      return;
    }

    if (loadSchemas) {
      for (var e : extensibilityElementsOf(types)) {
        if (e instanceof Schema) {
          parseSchemasRec((Schema) e, rootDefUri, relPath, collector);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected static void parseSchemasRec(Schema s, URI rootUri, URI relPath, ParsedWsdl collector) {
    if (s == null) {
      return;
    }

    Map<String, List<SchemaImport>> imports = s.getImports();
    List<SchemaReference> includes = s.getIncludes();
    if (includes.isEmpty() && imports.isEmpty()) {
      return;
    }

    Collection<SchemaReference> references = imports.values().stream()
        .flatMap(Collection::stream)
        .collect(toList());
    references.addAll(includes);
    if (references.isEmpty()) {
      return;
    }

    for (var ref : references) {
      var schemaLocationURI = ref.getSchemaLocationURI();
      URI pathRelativeToRoot;
      var exists = false;
      if (Strings.isBlank(schemaLocationURI)) {
        pathRelativeToRoot = relPath;
      }
      else {
        var pathAbsolute = URI.create(schemaLocationURI);
        if (Strings.hasText(pathAbsolute.getRawFragment())) {
          // the import is a schema-id inside the current document -> no need to import
          // see https://www.w3.org/TR/wsdl20-primer/#schemaIds.wsdl
          continue;
        }

        if (pathAbsolute.isAbsolute()) {
          // it's an absolute path -> not possible to make relative
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
     * @param portTypeNameFromWsdl
     *          The port type name as it appears in the WSDL.
     * @return The default port type class name for the given portType WSDL name
     */
    public static String getPortTypeClassName(String portTypeNameFromWsdl) {
      return 'I' + getBaseName(portTypeNameFromWsdl) + ISdkConstants.SUFFIX_WS_PORT_TYPE;
    }

    /**
     * @param portTypeNameFromWsdl
     *          The port type name as it appears in the WSDL.
     * @return The entry point class name (for providers) for the given port type WSDL name.
     */
    public static String getEntryPointClassName(String portTypeNameFromWsdl) {
      return getBaseName(portTypeNameFromWsdl) + ISdkConstants.SUFFIX_WS_ENTRY_POINT;
    }

    /**
     * @param portTypeNameFromWsdl
     *          The port type name as it appears in the WSDL.
     * @return The web service client class name (for consumers) for the given port type WSDL name.
     */
    public static String getWebServiceClientClassName(String portTypeNameFromWsdl) {
      return getBaseName(portTypeNameFromWsdl) + ISdkConstants.SUFFIX_WS_CLIENT;
    }

    /**
     * @param portTypeNameFromWsdl
     *          The port type name as it appears in the WSDL.
     * @return The web service provider implementation class name for the given port type WSDL name.
     */
    public static String getWebServiceProviderImplClassName(String portTypeNameFromWsdl) {
      return getBaseName(portTypeNameFromWsdl) + ISdkConstants.SUFFIX_WS_PROVIDER;
    }

    /**
     * @param portTypeNameFromWsdl
     *          The port type name as it appears in the WSDL.
     * @return The entry point definition class name (for providers) for the given port type WSDL name.
     */
    public static String getEntryPointDefinitionClassName(String portTypeNameFromWsdl) {
      return 'I' + getBaseName(portTypeNameFromWsdl) + ISdkConstants.SUFFIX_WS_ENTRY_POINT_DEFINITION;
    }

    protected static String getBaseName(String wsNameFromWsdl) {
      return wsdlNameToJavaName(JaxWsUtils.removeCommonSuffixes(wsNameFromWsdl));
    }

    protected static String wsdlNameToJavaName(CharSequence name) {
      var parts = Pattern.compile("[^a-zA-Z\\d]").split(name);
      return Arrays.stream(parts)
          .map(Strings::capitalize)
          .collect(joining());
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
      return m_wsBaseName + ISdkConstants.SUFFIX_WS_SERVICE;
    }
  }
}
