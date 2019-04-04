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
package org.eclipse.scout.sdk.s2e.ui.internal.jaxws.editor;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.jaxws.AbstractWebServiceNewOperation;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <h3>{@link WebServiceFormPageInput}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceFormPageInput implements Comparable<WebServiceFormPageInput> {

  private final IJavaProject m_javaProject;
  private final int m_hash;
  private final String m_displayName;
  private final Map<IType, ITypeHierarchy> m_superHierarchyCache;
  private final Map<IType, ITypeHierarchy> m_hierarchyCache;

  // general
  private final Path m_wsdl;
  private final List<Path> m_bindings;
  private final Map<IType /*web service type*/, Set<IType>> m_portTypes;
  private final Map<IType /*port type*/, QName> m_portTypeNamesInWsdl;
  private final Map<IType, QName /*service name in wsdl*/> m_webServices;
  private final List<Path> m_jaxwsBindingFiles;
  private ParsedWsdl m_servicesFromWsdl;

  // consumer
  private final Map<IType /*port type*/, IType> m_webServiceClients;

  // provider
  private final Map<IType /*port type*/, IType> m_entryPoints;
  private final Map<IType /*port type*/, IType> m_entryPointDefinitions;
  private final Map<IType /*port type*/, IType> m_serviceImplementations;
  private final Map<IType /*port type*/, String> m_entryPointNameFromDefinitions;
  private final Map<IType /*port type*/, String> m_entryPointPackageFromDefinitions;
  private final Map<IType /*port type*/, IType> m_authMethodFromDefinitions;
  private final Map<IType /*port type*/, IType> m_authVerifierFromDefinitions;
  private final Map<IType /*port type*/, List<IType>> m_handlersFromDefinitions;

  public WebServiceFormPageInput(Path wsdl, IJavaProject javaProject) {
    m_wsdl = Ensure.notNull(wsdl);
    Ensure.isFile(m_wsdl);
    Ensure.isTrue(JdtUtils.exists(javaProject));
    m_javaProject = javaProject;
    m_displayName = calcDisplayName();
    m_hash = m_wsdl.hashCode();

    int defaultSize = 3;
    m_bindings = new ArrayList<>(defaultSize);
    m_jaxwsBindingFiles = new ArrayList<>(defaultSize);
    m_webServices = new HashMap<>(defaultSize);
    m_superHierarchyCache = new HashMap<>(defaultSize);
    m_hierarchyCache = new HashMap<>(defaultSize);
    m_webServiceClients = new HashMap<>(defaultSize);
    m_entryPoints = new HashMap<>(defaultSize);
    m_portTypes = new HashMap<>(defaultSize);
    m_serviceImplementations = new HashMap<>(defaultSize);
    m_entryPointDefinitions = new HashMap<>(defaultSize);
    m_entryPointNameFromDefinitions = new HashMap<>(defaultSize);
    m_entryPointPackageFromDefinitions = new HashMap<>(defaultSize);
    m_authMethodFromDefinitions = new HashMap<>(defaultSize);
    m_authVerifierFromDefinitions = new HashMap<>(defaultSize);
    m_handlersFromDefinitions = new HashMap<>(defaultSize);
    m_portTypeNamesInWsdl = new HashMap<>(defaultSize);
  }

  public void load(IProgressMonitor monitor) throws JavaModelException {
    SubMonitor progress = SubMonitor.convert(monitor, "Parse artifacts of " + getWsdl().getFileName(), 120);
    S2eUtils.waitForJdt();

    // general
    loadWsdlServices();
    progress.worked(50);

    loadWebServicesAndPortTypes();
    progress.worked(10);

    loadBindings();
    progress.worked(10);

    loadJaxwsBindingFile();
    progress.worked(10);

    // consumer
    loadWebServiceClient();
    progress.worked(10);

    // provider
    loadEntryPoint();
    progress.worked(10);

    loadEntryPointDefinitions();
    progress.worked(10);

    loadServiceImplementations();
    progress.worked(10);
  }

  protected String calcDisplayName() {
    String name = getWsdl().getFileName().toString();
    int lastDotPos = name.lastIndexOf('.');
    if (lastDotPos > 0) {
      name = name.substring(0, lastDotPos);
    }
    name = JaxWsUtils.removeCommonSuffixes(name);
    String camelToWords = Pattern.compile("([A-Z])").matcher(name).replaceAll(" $1");
    camelToWords = Pattern.compile("[^A-Za-z0-9\\s]").matcher(camelToWords).replaceAll(" ").trim();
    String[] words = Pattern.compile("\\s").split(camelToWords);
    StringBuilder builder = new StringBuilder();
    for (String w : words) {
      if (Strings.hasText(w)) {
        builder.append(Strings.ensureStartWithUpperCase(w)).append(' ');
      }
    }
    return builder.toString().trim();
  }

  protected void loadWsdlServices() {
    try (InputStream in = Files.newInputStream(getWsdl(), StandardOpenOption.READ)) {
      setServicesFromWsdl(ParsedWsdl.create(getWsdl().toUri(), in, false));
    }
    catch (IOException | WSDLException e) {
      throw new SdkException(e);
    }
  }

  protected void loadWebServiceClient() {
    Predicate<IType> webServiceClientsFilter = element -> {
      try {
        ITypeHierarchy superTypeHierarchy = getSuperTypeHierarchy(element);
        return JdtUtils.hierarchyContains(superTypeHierarchy, IScoutRuntimeTypes.AbstractWebServiceClient);
      }
      catch (SdkException e) {
        SdkLog.warning("Unable to check if element '{}' is a web service client.", element.getFullyQualifiedName(), e);
        return false;
      }
    };
    visitPortTypes(portType -> {
      IType webServiceClient = getPortTypeChildClass(portType, webServiceClientsFilter);
      if (JdtUtils.exists(webServiceClient)) {
        m_webServiceClients.put(portType, webServiceClient);
      }
    });
  }

  protected IType getPortTypeChildClass(IType portType, Predicate<IType> filter) {
    if (!JdtUtils.exists(portType)) {
      return null;
    }

    IType[] candidates = getTypeHierarchy(portType).getAllSubtypes(portType);
    for (IType t : candidates) {
      if (filter == null || filter.test(t)) {
        return t;
      }
    }
    return null;
  }

  protected void loadEntryPoint() {
    Predicate<IType> portTypeFilter = element -> JdtUtils.exists(JdtUtils.getAnnotation(element, IScoutRuntimeTypes.WebService));
    visitPortTypes(portType -> {
      IType entryPoint = getPortTypeChildClass(portType, portTypeFilter);
      if (JdtUtils.exists(entryPoint)) {
        m_entryPoints.put(portType, entryPoint);
      }
    });
  }

  protected void loadEntryPointDefinitions() throws JavaModelException {
    Set<IType> webServiceEntryPointDefinitions = findAllTypesAnnotatedWith(IScoutRuntimeTypes.WebServiceEntryPoint);
    if (webServiceEntryPointDefinitions.isEmpty()) {
      return;
    }
    if (m_entryPoints.isEmpty()) {
      return;
    }

    for (Set<IType> portTypes : m_portTypes.values()) {
      for (IType portType : portTypes) {
        IType entryPoint = getEntryPoint(portType);
        if (JdtUtils.exists(entryPoint)) {
          String entryPointFqn = entryPoint.getFullyQualifiedName();
          for (IType entryPointDefCandidate : webServiceEntryPointDefinitions) {
            loadEntryPointDefinition(entryPointDefCandidate, portType, entryPointFqn);
          }
        }
      }
    }
  }

  protected void loadEntryPointDefinition(IType entryPointDefCandidate, IType portType, String entryPointFqn) throws JavaModelException {
    IAnnotation annotation = JdtUtils.getAnnotation(entryPointDefCandidate, IScoutRuntimeTypes.WebServiceEntryPoint);
    if (!JdtUtils.exists(annotation)) {
      return;
    }

    String pck = JdtUtils.getAnnotationValueString(annotation, JaxWsUtils.ENTRY_POINT_DEFINITION_PACKAGE_ATTRIBUTE);
    String name = JdtUtils.getAnnotationValueString(annotation, JaxWsUtils.ENTRY_POINT_DEFINITION_NAME_ATTRIBUTE);

    // use defaults as specified by the jaxws annotation processor (see WebServiceEntryPoint annotation documentation)
    if (Strings.isBlank(pck)) {
      pck = JavaTypes.qualifier(entryPointDefCandidate.getFullyQualifiedName());
    }
    if (Strings.isBlank(name)) {
      name = portType.getElementName() + "EntryPoint";
    }

    if (entryPointFqn.equals(pck + JavaTypes.C_DOT + name)) {
      m_entryPointDefinitions.put(portType, entryPointDefCandidate);
      m_entryPointNameFromDefinitions.put(portType, name);
      m_entryPointPackageFromDefinitions.put(portType, pck);

      for (IMemberValuePair mvp : annotation.getMemberValuePairs()) {
        if (JaxWsUtils.ENTRY_POINT_DEFINITION_AUTH_ATTRIBUTE.equals(mvp.getMemberName())) {
          Object value = mvp.getValue();
          // load Auth
          if (value instanceof IAnnotation) {
            IAnnotation authentication = (IAnnotation) value;
            for (IMemberValuePair authElement : authentication.getMemberValuePairs()) {
              if ("method".equals(authElement.getMemberName()) && authElement.getValueKind() == IMemberValuePair.K_ANNOTATION) {
                m_authMethodFromDefinitions.put(portType, getClazzAnnotationValue((IAnnotation) authElement.getValue(), entryPointDefCandidate));
              }
              else if ("verifier".equals(authElement.getMemberName()) && authElement.getValueKind() == IMemberValuePair.K_ANNOTATION) {
                m_authVerifierFromDefinitions.put(portType, getClazzAnnotationValue((IAnnotation) authElement.getValue(), entryPointDefCandidate));
              }
            }
          }
        }
        else if (JaxWsUtils.ENTRY_POINT_DEFINITION_HANDLER_CHAIN_ATTRIBUTE.equals(mvp.getMemberName())) {
          // load handlers
          Object value = mvp.getValue();
          if (value instanceof Object[]) {
            Object[] values = (Object[]) value;
            Collection<IAnnotation> handlers = new ArrayList<>(values.length);
            for (Object o : values) {
              if (o instanceof IAnnotation) {
                handlers.add((IAnnotation) o);
              }
            }
            loadHandlersFromDefinition(handlers, entryPointDefCandidate, portType);
          }
          else if (value instanceof IAnnotation) {
            loadHandlersFromDefinition(singletonList((IAnnotation) value), entryPointDefCandidate, portType);
          }
        }
      }
    }
  }

  protected void loadHandlersFromDefinition(Iterable<IAnnotation> handlers, IType owner, IType portType) throws JavaModelException {
    for (IAnnotation handler : handlers) {
      for (IMemberValuePair mvp : handler.getMemberValuePairs()) {
        if ("value".equals(mvp.getMemberName()) && mvp.getValueKind() == IMemberValuePair.K_ANNOTATION) {
          IAnnotation clazzAnnotation = (IAnnotation) mvp.getValue();
          IType handlerType = getClazzAnnotationValue(clazzAnnotation, owner);
          if (JdtUtils.exists(handlerType)) {
            m_handlersFromDefinitions.computeIfAbsent(portType, k -> new ArrayList<>(3)).add(handlerType);
          }
        }
      }
    }
  }

  protected static IType getClazzAnnotationValue(IAnnotation clazzAnnotation, IType owner) throws JavaModelException {
    if (!JdtUtils.exists(clazzAnnotation)) {
      return null;
    }
    String qualifiedName = JdtUtils.getAnnotationValueString(clazzAnnotation, "qualifiedName");
    IJavaProject javaProject = owner.getJavaProject();
    if (Strings.hasText(qualifiedName)) {
      return javaProject.findType(qualifiedName);
    }

    String value = JdtUtils.getAnnotationValueString(clazzAnnotation, "value");
    if (Strings.isBlank(value)) {
      return null;
    }

    String nullClazzSimpleName = JavaTypes.simpleName(IScoutRuntimeTypes.NullClazz.replace(JavaTypes.C_DOLLAR, JavaTypes.C_DOT));
    if (value.equals(nullClazzSimpleName)
        || value.endsWith(JavaTypes.C_DOT + nullClazzSimpleName)
        || value.endsWith(JavaTypes.C_DOLLAR + nullClazzSimpleName)) {
      return null;
    }

    return getReferencedType(owner, value);
  }

  protected static IType getReferencedType(IType declaringType, String typeName) throws JavaModelException {
    String[][] resolvedTypeName = declaringType.resolveType(typeName);
    if (resolvedTypeName == null || resolvedTypeName.length < 1) {
      return null;
    }

    IJavaProject javaProject = declaringType.getJavaProject();
    for (String[] fqn : resolvedTypeName) {
      IType result = javaProject.findType(fqn[0], fqn[1]);
      if (JdtUtils.exists(result)) {
        return result;
      }
    }
    return null;
  }

  protected void loadServiceImplementations() {
    Predicate<IType> serviceImplFilter = element -> {
      if (JdtUtils.exists(JdtUtils.getAnnotation(element, IScoutRuntimeTypes.WebService))) {
        return false; // exclude entry points
      }

      // exclude web service clients
      try {
        ITypeHierarchy superTypeHierarchy = getSuperTypeHierarchy(element);
        return !JdtUtils.hierarchyContains(superTypeHierarchy, IScoutRuntimeTypes.AbstractWebServiceClient);
      }
      catch (SdkException e) {
        SdkLog.warning("Unable to check if element '{}' is a web service client.", element.getFullyQualifiedName(), e);
        return false;
      }
    };

    visitPortTypes(portType -> {
      IType serviceImpl = getPortTypeChildClass(portType, serviceImplFilter);
      if (JdtUtils.exists(serviceImpl)) {
        m_serviceImplementations.put(portType, serviceImpl);
      }
    });
  }

  protected void visitPortTypes(Consumer<IType> visitor) {
    for (Set<IType> portTypes : m_portTypes.values()) {
      for (IType portType : portTypes) {
        visitor.accept(portType);
      }
    }
  }

  protected void loadWebServicesAndPortTypes() throws JavaModelException {
    if (getServicesFromWsdl().isEmpty()) {
      return;
    }
    Set<IType> candidates = findAllTypesAnnotatedWith(IScoutRuntimeTypes.WebServiceClient);
    if (candidates.isEmpty()) {
      return;
    }
    for (IType candidate : candidates) {
      IAnnotation annotation = JdtUtils.getAnnotation(candidate, IScoutRuntimeTypes.WebServiceClient);
      String name = JdtUtils.getAnnotationValueString(annotation, "name");
      String targetNamespace = JdtUtils.getAnnotationValueString(annotation, "targetNamespace");
      for (Service info : getServicesFromWsdl().getWebServices().keySet()) {
        QName webServiceName = info.getQName();
        if (Objects.equals(name, webServiceName.getLocalPart()) && Objects.equals(targetNamespace, webServiceName.getNamespaceURI())) {
          m_webServices.put(candidate, webServiceName);
          loadPortTypes(info, candidate);
        }
      }
    }
  }

  protected void loadPortTypes(Service service, IType webService) throws JavaModelException {
    if (getServicesFromWsdl().isEmpty()) {
      return;
    }
    Set<IType> candidates = findAllTypesAnnotatedWith(IScoutRuntimeTypes.WebService);
    if (candidates.isEmpty()) {
      return;
    }
    for (IType candidate : candidates) {
      if (candidate.isInterface()) {
        IAnnotation annotation = JdtUtils.getAnnotation(candidate, IScoutRuntimeTypes.WebService);
        String name = JdtUtils.getAnnotationValueString(annotation, "name");
        String targetNamespace = JdtUtils.getAnnotationValueString(annotation, "targetNamespace");
        for (Entry<String, QName> port : getServicesFromWsdl().getPorts(service).entrySet()) {
          QName portTypeName = port.getValue();
          if (Objects.equals(name, portTypeName.getLocalPart()) && Objects.equals(targetNamespace, portTypeName.getNamespaceURI())) {
            m_portTypes.computeIfAbsent(webService, k -> new LinkedHashSet<>(3)).add(candidate);
            m_portTypeNamesInWsdl.put(candidate, portTypeName);
          }
        }
      }
    }
  }

  protected Set<IType> findAllTypesAnnotatedWith(String fqn) {
    IJavaSearchScope scope = JdtUtils.createJavaSearchScope(getJavaProject());
    return JdtUtils.findAllTypesAnnotatedWith(fqn, scope, null);
  }

  protected ITypeHierarchy getTypeHierarchy(IType t) {
    if (!JdtUtils.exists(t)) {
      return null;
    }

    return m_hierarchyCache.computeIfAbsent(t, key -> {
      try {
        return key.newTypeHierarchy(null);
      }
      catch (JavaModelException e) {
        throw new SdkException(e);
      }
    });
  }

  protected ITypeHierarchy getSuperTypeHierarchy(IType t) {
    if (!JdtUtils.exists(t)) {
      return null;
    }
    return m_superHierarchyCache.computeIfAbsent(t, key -> {
      try {
        return key.newSupertypeHierarchy(null);
      }
      catch (JavaModelException e) {
        throw new SdkException(e);
      }
    });
  }

  protected void loadBindings() {
    Document pomDocument = S2eUtils.getPomDocument(getJavaProject().getProject());
    if (pomDocument == null) {
      return;
    }
    Path projectPath = getJavaProject().getProject().getLocation().toFile().toPath();
    Path bindingFolder = AbstractWebServiceNewOperation.getBindingRootFolder(projectPath);
    if (!Files.isDirectory(bindingFolder)) {
      return;
    }

    try {
      Path wsdlFolderRelativePath = AbstractWebServiceNewOperation.getWsdlRootFolder(projectPath).relativize(getWsdl());
      List<String> paths = JaxWsUtils.getBindingPathsFromPom(pomDocument, wsdlFolderRelativePath.toString().replace('\\', '/'));
      if (paths.isEmpty()) {
        try (Stream<Path> ps = Files.list(bindingFolder)) {
          ps.filter(Files::isReadable)
              .filter(Files::isRegularFile)
              .forEach(m_bindings::add);
        }
      }
      else {
        // load paths
        for (String bindingRelPath : paths) {
          Path candidate = bindingFolder.resolve(bindingRelPath);
          if (Files.isReadable(candidate) && Files.isRegularFile(candidate)) {
            m_bindings.add(candidate);
          }
        }
      }
    }
    catch (XPathExpressionException | IOException e) {
      throw new SdkException(e);
    }
  }

  protected void loadJaxwsBindingFile() {
    if (m_bindings.isEmpty()) {
      return;
    }

    for (Path candidate : m_bindings) {
      try {
        Document document = Xml.get(candidate);
        Element rootTag = document.getDocumentElement();
        if (JaxWsUtils.BINDINGS_ELEMENT_NAME.equals(rootTag.getLocalName())) {
          String rootNs = rootTag.getNamespaceURI();
          if (JaxWsUtils.JAX_WS_NAMESPACE.equals(rootNs)) {
            m_jaxwsBindingFiles.add(candidate);
          }
        }
      }
      catch (IOException e) {
        throw new SdkException(e);
      }
    }
  }

  public IType getWebService(IType portType) {
    for (Entry<IType, Set<IType>> entry : m_portTypes.entrySet()) {
      for (IType pt : entry.getValue()) {
        if (pt.equals(portType)) {
          return entry.getKey();
        }
      }
    }
    return null;
  }

  public QName getWebServiceNameInWsdl(IType webService) {
    return m_webServices.get(webService);
  }

  public QName getPortTypeNameInWsdl(IType portType) {
    return m_portTypeNamesInWsdl.get(portType);
  }

  public List<Path> getJaxWsBindingFiles() {
    return unmodifiableList(m_jaxwsBindingFiles);
  }

  public boolean hasConsumerElements() {
    return !m_webServiceClients.isEmpty();
  }

  public boolean hasProviderElements() {
    return !m_entryPoints.isEmpty()
        || !m_serviceImplementations.isEmpty()
        || !m_entryPointDefinitions.isEmpty();
  }

  public String getDisplayName() {
    return m_displayName;
  }

  public Path getWsdl() {
    return m_wsdl;
  }

  public IType getWebServiceClient(IType portType) {
    return m_webServiceClients.get(portType);
  }

  public IType getEntryPoint(IType portType) {
    return m_entryPoints.get(portType);
  }

  public IType getEntryPointDefinition(IType portType) {
    return m_entryPointDefinitions.get(portType);
  }

  public IType getServiceImplementation(IType portType) {
    return m_serviceImplementations.get(portType);
  }

  public Set<IType> getPortTypes(IType webService) {
    Set<IType> result = m_portTypes.get(webService);
    if (result == null) {
      return emptySet();
    }
    return unmodifiableSet(result);
  }

  public Set<IType> getAllPortTypes() {
    Set<IType> result = new LinkedHashSet<>();
    for (Set<IType> portTypesByService : m_portTypes.values()) {
      result.addAll(portTypesByService);
    }
    return result;
  }

  public Set<IType> getWebServices() {
    return unmodifiableSet(m_webServices.keySet());
  }

  protected ParsedWsdl getServicesFromWsdl() {
    return m_servicesFromWsdl;
  }

  protected void setServicesFromWsdl(ParsedWsdl namesFromWsdl) {
    m_servicesFromWsdl = namesFromWsdl;
  }

  public String getEntryPointNameFromDefinition(IType portType) {
    return m_entryPointNameFromDefinitions.get(portType);
  }

  public String getEntryPointPackageFromDefinition(IType portType) {
    return m_entryPointPackageFromDefinitions.get(portType);
  }

  public IType getAuthMethodFromDefinition(IType portType) {
    return m_authMethodFromDefinitions.get(portType);
  }

  public IType getAuthVerifierFromDefinition(IType portType) {
    return m_authVerifierFromDefinitions.get(portType);
  }

  public List<Path> getBindings() {
    return unmodifiableList(m_bindings);
  }

  public List<IType> getHandlers(IType portType) {
    List<IType> list = m_handlersFromDefinitions.get(portType);
    if (list == null) {
      return emptyList();
    }
    return unmodifiableList(list);
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  @Override
  public int hashCode() {
    return m_hash;
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
    WebServiceFormPageInput other = (WebServiceFormPageInput) obj;
    return m_wsdl.equals(other.m_wsdl);
  }

  @Override
  public int compareTo(WebServiceFormPageInput o) {
    if (o.getWsdl() == getWsdl()) {
      return 0;
    }
    int result = getWsdl().getFileName().toString().compareTo(o.getWsdl().getFileName().toString());
    if (result != 0) {
      return result;
    }
    return getWsdl().toString().compareTo(o.getWsdl().toString());
  }

  @Override
  public String toString() {
    return getWsdl().toString();
  }
}
