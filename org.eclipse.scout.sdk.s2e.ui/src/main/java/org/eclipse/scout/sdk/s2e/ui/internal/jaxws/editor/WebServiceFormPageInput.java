/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.jaxws.editor;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.jaxws.AbstractWebServiceNewOperation;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.eclipse.scout.sdk.s2e.util.ApiHelper;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

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
  private final IScoutApi m_scoutApi;
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

  public WebServiceFormPageInput(Path wsdl, IJavaProject javaProject, IScoutApi api) {
    m_wsdl = Ensure.notNull(wsdl);
    Ensure.isTrue(JdtUtils.exists(javaProject));
    m_javaProject = javaProject;
    m_displayName = calcDisplayName();
    m_hash = m_wsdl.hashCode();
    m_scoutApi = Ensure.notNull(api);

    var defaultSize = 3;
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
    var progress = SubMonitor.convert(monitor, "Parse artifacts of " + getWsdl().getFileName(), 120);
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

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected String calcDisplayName() {
    var name = getWsdl().getFileName().toString();
    var lastDotPos = name.lastIndexOf('.');
    if (lastDotPos > 0) {
      name = name.substring(0, lastDotPos);
    }
    name = JaxWsUtils.removeCommonSuffixes(name);
    var words = Pattern.compile("([A-Z])").matcher(name).replaceAll(" $1");
    var cleanWords = Strings.trim(Pattern.compile("[^A-Za-z0-9\\s]").matcher(words).replaceAll(" "));
    return Pattern.compile("\\s").splitAsStream(cleanWords)
        .filter(Strings::hasText)
        .map(Strings::capitalize)
        .collect(joining(" "));
  }

  protected void loadWsdlServices() {
    try (var in = new BufferedInputStream(Files.newInputStream(getWsdl(), StandardOpenOption.READ))) {
      setServicesFromWsdl(ParsedWsdl.create(getWsdl().toUri(), in, false));
    }
    catch (IOException | WSDLException e) {
      throw new SdkException(e);
    }
  }

  protected boolean isWebServiceClient(IType element) {
    try {
      var superTypeHierarchy = getSuperTypeHierarchy(element);
      return JdtUtils.hierarchyContains(superTypeHierarchy, getScoutApi().AbstractWebServiceClient().fqn());
    }
    catch (SdkException e) {
      SdkLog.warning("Unable to check if element '{}' is a web service client.", element.getFullyQualifiedName(), e);
      return false;
    }
  }

  protected void loadWebServiceClient() {
    visitPortTypes(portType -> {
      var webServiceClient = getPortTypeChildClass(portType, this::isWebServiceClient);
      if (JdtUtils.exists(webServiceClient)) {
        m_webServiceClients.put(portType, webServiceClient);
      }
    });
  }

  protected IType getPortTypeChildClass(IType portType, Predicate<IType> filter) {
    if (!JdtUtils.exists(portType)) {
      return null;
    }

    var candidates = getTypeHierarchy(portType).getAllSubtypes(portType);
    return Arrays.stream(candidates)
        .filter(t -> filter == null || filter.test(t))
        .findFirst()
        .orElse(null);
  }

  protected boolean isPortType(IAnnotatable element) {
    return JdtUtils.exists(JdtUtils.getAnnotation(element, getScoutApi().WebService().fqn()));
  }

  protected void loadEntryPoint() {
    visitPortTypes(portType -> {
      var entryPoint = getPortTypeChildClass(portType, this::isPortType);
      if (JdtUtils.exists(entryPoint)) {
        m_entryPoints.put(portType, entryPoint);
      }
    });
  }

  protected void loadEntryPointDefinitions() throws JavaModelException {
    var webServiceEntryPointDefinitions = findAllTypesAnnotatedWith(getScoutApi().WebServiceEntryPoint().fqn());
    if (webServiceEntryPointDefinitions.isEmpty()) {
      return;
    }
    if (m_entryPoints.isEmpty()) {
      return;
    }

    for (var portTypes : m_portTypes.values()) {
      for (var portType : portTypes) {
        var entryPoint = getEntryPoint(portType);
        if (JdtUtils.exists(entryPoint)) {
          var entryPointFqn = entryPoint.getFullyQualifiedName();
          for (var entryPointDefCandidate : webServiceEntryPointDefinitions) {
            loadEntryPointDefinition(entryPointDefCandidate, portType, entryPointFqn);
          }
        }
      }
    }
  }

  protected void loadEntryPointDefinition(IType entryPointDefCandidate, IType portType, String entryPointFqn) throws JavaModelException {
    var entryPointApi = getScoutApi().WebServiceEntryPoint();
    var annotation = JdtUtils.getAnnotation(entryPointDefCandidate, entryPointApi.fqn());
    if (!JdtUtils.exists(annotation)) {
      return;
    }

    var pck = JdtUtils.getAnnotationValueString(annotation, entryPointApi.entryPointPackageElementName());
    var name = JdtUtils.getAnnotationValueString(annotation, entryPointApi.entryPointNameElementName());

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

      for (var mvp : annotation.getMemberValuePairs()) {
        if (entryPointApi.authenticationElementName().equals(mvp.getMemberName())) {
          var value = mvp.getValue();
          // load Auth
          if (value instanceof IAnnotation) {
            var authentication = (IAnnotation) value;

            for (var authElement : authentication.getMemberValuePairs()) {
              if (getScoutApi().Authentication().methodElementName().equals(authElement.getMemberName()) && authElement.getValueKind() == IMemberValuePair.K_ANNOTATION) {
                m_authMethodFromDefinitions.put(portType, getClazzAnnotationValue((IAnnotation) authElement.getValue(), entryPointDefCandidate));
              }
              else if (getScoutApi().Authentication().verifierElementName().equals(authElement.getMemberName()) && authElement.getValueKind() == IMemberValuePair.K_ANNOTATION) {
                m_authVerifierFromDefinitions.put(portType, getClazzAnnotationValue((IAnnotation) authElement.getValue(), entryPointDefCandidate));
              }
            }
          }
        }
        else if (entryPointApi.handlerChainElementName().equals(mvp.getMemberName())) {
          // load handlers
          var value = mvp.getValue();
          if (value instanceof Object[]) {
            var values = (Object[]) value;
            Collection<IAnnotation> handlers = Arrays.stream(values)
                .filter(o -> o instanceof IAnnotation)
                .map(o -> (IAnnotation) o)
                .collect(toList());
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
    var handlerValueElementName = getScoutApi().Handler().valueElementName();
    for (var handler : handlers) {
      for (var mvp : handler.getMemberValuePairs()) {
        if (handlerValueElementName.equals(mvp.getMemberName()) && mvp.getValueKind() == IMemberValuePair.K_ANNOTATION) {
          var clazzAnnotation = (IAnnotation) mvp.getValue();
          var handlerType = getClazzAnnotationValue(clazzAnnotation, owner);
          if (JdtUtils.exists(handlerType)) {
            m_handlersFromDefinitions.computeIfAbsent(portType, k -> new ArrayList<>(3)).add(handlerType);
          }
        }
      }
    }
  }

  protected IType getClazzAnnotationValue(IAnnotation clazzAnnotation, IType owner) throws JavaModelException {
    if (!JdtUtils.exists(clazzAnnotation)) {
      return null;
    }
    var clazzApi = getScoutApi().Clazz();
    var qualifiedName = JdtUtils.getAnnotationValueString(clazzAnnotation, clazzApi.qualifiedNameElementName());
    var javaProject = owner.getJavaProject();
    if (Strings.hasText(qualifiedName)) {
      return javaProject.findType(qualifiedName);
    }

    var value = JdtUtils.getAnnotationValueString(clazzAnnotation, clazzApi.valueElementName());
    if (Strings.isBlank(value)) {
      return null;
    }

    var nullClazzSimpleName = getScoutApi().NullClazz().simpleName().replace(JavaTypes.C_DOLLAR, JavaTypes.C_DOT);
    if (value.equals(nullClazzSimpleName) || value.endsWith(JavaTypes.C_DOT + nullClazzSimpleName) || value.endsWith(JavaTypes.C_DOLLAR + nullClazzSimpleName)) {
      return null;
    }

    return getReferencedType(owner, value);
  }

  protected static IType getReferencedType(IType declaringType, String typeName) throws JavaModelException {
    var resolvedTypeName = declaringType.resolveType(typeName);
    if (resolvedTypeName == null || resolvedTypeName.length < 1) {
      return null;
    }

    var javaProject = declaringType.getJavaProject();
    for (var fqn : resolvedTypeName) {
      var result = javaProject.findType(fqn[0], fqn[1]);
      if (JdtUtils.exists(result)) {
        return result;
      }
    }
    return null;
  }

  protected boolean isWebServiceImpl(IType element) {
    if (JdtUtils.exists(JdtUtils.getAnnotation(element, getScoutApi().WebService().fqn()))) {
      return false; // exclude entry points
    }

    // exclude web service clients
    try {
      var superTypeHierarchy = getSuperTypeHierarchy(element);
      return !JdtUtils.hierarchyContains(superTypeHierarchy, getScoutApi().AbstractWebServiceClient().fqn());
    }
    catch (SdkException e) {
      SdkLog.warning("Unable to check if element '{}' is a web service client.", element.getFullyQualifiedName(), e);
      return false;
    }
  }

  protected void loadServiceImplementations() {
    visitPortTypes(portType -> {
      var serviceImpl = getPortTypeChildClass(portType, this::isWebServiceImpl);
      if (JdtUtils.exists(serviceImpl)) {
        m_serviceImplementations.put(portType, serviceImpl);
      }
    });
  }

  protected void visitPortTypes(Consumer<IType> visitor) {
    for (var portTypes : m_portTypes.values()) {
      for (var portType : portTypes) {
        visitor.accept(portType);
      }
    }
  }

  protected void loadWebServicesAndPortTypes() throws JavaModelException {
    if (getServicesFromWsdl().isEmpty()) {
      return;
    }
    var webServiceClientApi = getScoutApi().WebServiceClient();
    var candidates = findAllTypesAnnotatedWith(webServiceClientApi.fqn());
    if (candidates.isEmpty()) {
      return;
    }
    for (var candidate : candidates) {
      var annotation = JdtUtils.getAnnotation(candidate, webServiceClientApi.fqn());
      var name = JdtUtils.getAnnotationValueString(annotation, webServiceClientApi.nameElementName());
      var targetNamespace = JdtUtils.getAnnotationValueString(annotation, webServiceClientApi.targetNamespaceElementName());
      for (var info : getServicesFromWsdl().getWebServices().keySet()) {
        var webServiceName = info.getQName();
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
    var webServiceApi = getScoutApi().WebService();
    var candidates = findAllTypesAnnotatedWith(webServiceApi.fqn());
    if (candidates.isEmpty()) {
      return;
    }
    for (var candidate : candidates) {
      if (candidate.isInterface()) {
        var annotation = JdtUtils.getAnnotation(candidate, webServiceApi.fqn());
        var name = JdtUtils.getAnnotationValueString(annotation, webServiceApi.nameElementName());
        var targetNamespace = JdtUtils.getAnnotationValueString(annotation, webServiceApi.targetNamespaceElementName());
        for (var port : getServicesFromWsdl().getPorts(service).entrySet()) {
          var portTypeName = port.getValue();
          if (Objects.equals(name, portTypeName.getLocalPart()) && Objects.equals(targetNamespace, portTypeName.getNamespaceURI())) {
            m_portTypes.computeIfAbsent(webService, k -> new LinkedHashSet<>(3)).add(candidate);
            m_portTypeNamesInWsdl.put(candidate, portTypeName);
          }
        }
      }
    }
  }

  protected Set<IType> findAllTypesAnnotatedWith(String fqn) {
    var scope = JdtUtils.createJavaSearchScope(getJavaProject());
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
    var pomDocument = S2eUtils.getPomDocument(getJavaProject().getProject());
    if (pomDocument == null) {
      return;
    }
    var projectPath = getJavaProject().getProject().getLocation().toFile().toPath();
    var bindingFolder = AbstractWebServiceNewOperation.getBindingRootFolder(projectPath);
    if (!Files.isDirectory(bindingFolder)) {
      return;
    }

    try {
      var wsdlFolderRelativePath = AbstractWebServiceNewOperation.getWsdlRootFolder(projectPath).relativize(getWsdl());
      var scoutApi = ApiHelper.requireScoutApiFor(getJavaProject());
      var paths = JaxWsUtils.getBindingPathsFromPom(pomDocument, wsdlFolderRelativePath.toString().replace('\\', '/'), scoutApi);
      if (paths.isEmpty()) {
        //noinspection NestedTryStatement
        try (var ps = Files.list(bindingFolder)) {
          ps.filter(Files::isReadable)
              .filter(Files::isRegularFile)
              .forEach(m_bindings::add);
        }
      }
      else {
        // load paths
        for (var bindingRelPath : paths) {
          var candidate = bindingFolder.resolve(bindingRelPath);
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

    for (var candidate : m_bindings) {
      try {
        var document = Xml.get(candidate);
        var rootTag = document.getDocumentElement();
        if (JaxWsUtils.BINDINGS_ELEMENT_NAME.equals(rootTag.getLocalName())) {
          var rootNs = rootTag.getNamespaceURI();
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
    return m_portTypes.entrySet().stream()
        .filter(entry -> entry.getValue().stream().anyMatch(pt -> pt.equals(portType)))
        .findFirst()
        .map(Entry::getKey)
        .orElse(null);
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
    return Stream.of(m_entryPoints, m_serviceImplementations, m_entryPointDefinitions)
        .anyMatch(iTypeITypeMap -> !iTypeITypeMap.isEmpty());
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
    var result = m_portTypes.get(webService);
    if (result == null) {
      return emptySet();
    }
    return unmodifiableSet(result);
  }

  public Set<IType> getAllPortTypes() {
    return m_portTypes.values().stream()
        .flatMap(Collection::stream)
        .collect(toCollection(LinkedHashSet::new));
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
    var list = m_handlersFromDefinitions.get(portType);
    if (list == null) {
      return emptyList();
    }
    return unmodifiableList(list);
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  public IScoutApi getScoutApi() {
    return m_scoutApi;
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
    var other = (WebServiceFormPageInput) obj;
    return m_wsdl.equals(other.m_wsdl);
  }

  @Override
  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public int compareTo(WebServiceFormPageInput o) {
    if (o.getWsdl() == getWsdl()) {
      return 0;
    }
    var result = getWsdl().getFileName().toString().compareTo(o.getWsdl().getFileName().toString());
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
