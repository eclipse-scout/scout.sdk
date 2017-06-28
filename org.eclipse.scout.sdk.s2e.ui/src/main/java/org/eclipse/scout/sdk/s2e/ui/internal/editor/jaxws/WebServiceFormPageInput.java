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
package org.eclipse.scout.sdk.s2e.ui.internal.editor.jaxws;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceNewOperation;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <h3>{@link WebServiceFormPageInput}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WebServiceFormPageInput implements Comparable<WebServiceFormPageInput> {

  private final IJavaProject m_javaProject;
  private final int m_hash;
  private final String m_displayName;
  private final Map<IType, ITypeHierarchy> m_superHierarchyCache;
  private final Map<IType, ITypeHierarchy> m_hierarchyCache;

  // general
  private final IFile m_wsdl;
  private final List<IFile> m_bindings;
  private final Map<IType /*web service type*/, Set<IType>> m_portTypes;
  private final Map<IType /*port type*/, QName> m_portTypeNamesInWsdl;
  private final Map<IType, QName /*service name in wsdl*/> m_webServices;
  private final List<IFile> m_jaxwsBindingFiles;
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

  public WebServiceFormPageInput(IFile wsdl, IJavaProject javaProject) {
    m_wsdl = Validate.notNull(wsdl);
    Validate.isTrue(m_wsdl.exists());
    Validate.isTrue(S2eUtils.exists(javaProject));
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

  public void load(IProgressMonitor monitor) throws CoreException {
    final SubMonitor progress = SubMonitor.convert(monitor, "Parse artifacts of " + getWsdl().getName(), 120);
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
    String name = getWsdl().getName();
    int lastDotPos = name.lastIndexOf('.');
    if (lastDotPos > 0) {
      String ext = getWsdl().getFileExtension();
      if (ext != null) {
        name = name.substring(0, name.length() - ext.length() - 1);
      }
    }
    name = JaxWsUtils.removeCommonSuffixes(name);
    String camelToWords = Pattern.compile("([A-Z])").matcher(name).replaceAll(" $1");
    camelToWords = Pattern.compile("[^A-Za-z0-9\\s]").matcher(camelToWords).replaceAll(" ").trim();
    String[] words = camelToWords.split("\\s");
    StringBuilder builder = new StringBuilder();
    for (String w : words) {
      if (StringUtils.isNotBlank(w)) {
        builder.append(CoreUtils.ensureStartWithUpperCase(w)).append(' ');
      }
    }
    return builder.toString().trim();
  }

  protected void loadWsdlServices() throws CoreException {
    try (InputStream in = getWsdl().getContents()) {
      setServicesFromWsdl(ParsedWsdl.create(getWsdl().getLocation().toFile().toURI(), in, false));
    }
    catch (IOException | WSDLException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  protected void loadWebServiceClient() throws JavaModelException {
    Predicate<IType> webServiceClientsFilter = new Predicate<IType>() {
      @Override
      public boolean test(IType element) {
        try {
          ITypeHierarchy superTypeHierarchy = getSuperTypeHierarchy(element);
          return S2eUtils.hierarchyContains(superTypeHierarchy, IScoutRuntimeTypes.AbstractWebServiceClient);
        }
        catch (JavaModelException e) {
          SdkLog.warning("Unable to check if element '{}' is a web service client.", element.getFullyQualifiedName(), e);
          return false;
        }
      }
    };
    for (Set<IType> portTypes : m_portTypes.values()) {
      for (IType portType : portTypes) {
        IType webServiceClient = getPortTypeChildClass(portType, webServiceClientsFilter);
        if (S2eUtils.exists(webServiceClient)) {
          m_webServiceClients.put(portType, webServiceClient);
        }
      }
    }
  }

  protected IType getPortTypeChildClass(IType portType, Predicate<IType> filter) throws JavaModelException {
    if (!S2eUtils.exists(portType)) {
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

  protected void loadEntryPoint() throws JavaModelException {
    Predicate<IType> portTypeFilter = new Predicate<IType>() {
      @Override
      public boolean test(IType element) {
        return S2eUtils.exists(S2eUtils.getAnnotation(element, IJavaRuntimeTypes.WebService));
      }
    };
    for (Set<IType> portTypes : m_portTypes.values()) {
      for (IType portType : portTypes) {
        IType entryPoint = getPortTypeChildClass(portType, portTypeFilter);
        if (S2eUtils.exists(entryPoint)) {
          m_entryPoints.put(portType, entryPoint);
        }
      }
    }
  }

  protected void loadEntryPointDefinitions() throws CoreException {
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
        if (S2eUtils.exists(entryPoint)) {
          String entryPointFqn = entryPoint.getFullyQualifiedName();
          for (IType entryPointDefCandidate : webServiceEntryPointDefinitions) {
            loadEntryPointDefinition(entryPointDefCandidate, portType, entryPointFqn);
          }
        }
      }
    }
  }

  protected void loadEntryPointDefinition(IType entryPointDefCandidate, IType portType, String entryPointFqn) throws JavaModelException {
    IAnnotation annotation = S2eUtils.getAnnotation(entryPointDefCandidate, IScoutRuntimeTypes.WebServiceEntryPoint);
    if (!S2eUtils.exists(annotation)) {
      return;
    }

    String pck = S2eUtils.getAnnotationValueString(annotation, JaxWsUtils.ENTRY_POINT_DEFINITION_PACKAGE_ATTRIBUTE);
    String name = S2eUtils.getAnnotationValueString(annotation, JaxWsUtils.ENTRY_POINT_DEFINITION_NAME_ATTRIBUTE);

    // use defaults as specified by the jaxws annotation processor (see WebServiceEntryPoint annotation documentation)
    if (StringUtils.isBlank(pck)) {
      pck = Signature.getQualifier(entryPointDefCandidate.getFullyQualifiedName());
    }
    if (StringUtils.isBlank(name)) {
      name = portType.getElementName() + "EntryPoint";
    }

    if (entryPointFqn.equals(pck + '.' + name)) {
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
            loadHandlersFromDefinition(Collections.singletonList((IAnnotation) value), entryPointDefCandidate, portType);
          }
        }
      }
    }
  }

  protected void loadHandlersFromDefinition(Collection<IAnnotation> handlers, IType owner, IType portType) throws JavaModelException {
    for (IAnnotation handler : handlers) {
      for (IMemberValuePair mvp : handler.getMemberValuePairs()) {
        if ("value".equals(mvp.getMemberName()) && mvp.getValueKind() == IMemberValuePair.K_ANNOTATION) {
          IAnnotation clazzAnnotation = (IAnnotation) mvp.getValue();
          IType handlerType = getClazzAnnotationValue(clazzAnnotation, owner);
          if (S2eUtils.exists(handlerType)) {
            List<IType> handlersOfPortType = m_handlersFromDefinitions.get(portType);
            if (handlersOfPortType == null) {
              handlersOfPortType = new ArrayList<>(3);
              m_handlersFromDefinitions.put(portType, handlersOfPortType);
            }
            handlersOfPortType.add(handlerType);
          }
        }
      }
    }
  }

  protected IType getClazzAnnotationValue(IAnnotation clazzAnnotation, IType owner) throws JavaModelException {
    if (!S2eUtils.exists(clazzAnnotation)) {
      return null;
    }
    String qualifiedName = S2eUtils.getAnnotationValueString(clazzAnnotation, "qualifiedName");
    IJavaProject javaProject = owner.getJavaProject();
    if (StringUtils.isNotBlank(qualifiedName)) {
      return javaProject.findType(qualifiedName);
    }

    String value = S2eUtils.getAnnotationValueString(clazzAnnotation, "value");
    if (StringUtils.isBlank(value)) {
      return null;
    }

    String nullClazzSimpleName = Signature.getSimpleName(IScoutRuntimeTypes.NullClazz.replace('$', '.'));
    if (value.equals(nullClazzSimpleName)
        || value.endsWith('.' + nullClazzSimpleName)
        || value.endsWith('$' + nullClazzSimpleName)) {
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
      if (S2eUtils.exists(result)) {
        return result;
      }
    }
    return null;
  }

  protected void loadServiceImplementations() throws JavaModelException {
    Predicate<IType> serviceImplFilter = new Predicate<IType>() {
      @Override
      public boolean test(IType element) {
        if (S2eUtils.exists(S2eUtils.getAnnotation(element, IJavaRuntimeTypes.WebService))) {
          return false; // exclude entry points
        }

        // exclude web service clients
        try {
          ITypeHierarchy superTypeHierarchy = getSuperTypeHierarchy(element);
          return !S2eUtils.hierarchyContains(superTypeHierarchy, IScoutRuntimeTypes.AbstractWebServiceClient);
        }
        catch (JavaModelException e) {
          SdkLog.warning("Unable to check if element '{}' is a web service client.", element.getFullyQualifiedName(), e);
          return false;
        }
      }
    };

    for (Set<IType> portTypes : m_portTypes.values()) {
      for (IType portType : portTypes) {
        IType serviceImpl = getPortTypeChildClass(portType, serviceImplFilter);
        if (S2eUtils.exists(serviceImpl)) {
          m_serviceImplementations.put(portType, serviceImpl);
        }
      }
    }
  }

  protected void loadWebServicesAndPortTypes() throws CoreException {
    if (getServicesFromWsdl().isEmpty()) {
      return;
    }
    Set<IType> candidates = findAllTypesAnnotatedWith(IJavaRuntimeTypes.WebServiceClient);
    if (candidates.isEmpty()) {
      return;
    }
    for (IType candidate : candidates) {
      IAnnotation annotation = S2eUtils.getAnnotation(candidate, IJavaRuntimeTypes.WebServiceClient);
      String name = S2eUtils.getAnnotationValueString(annotation, "name");
      String targetNamespace = S2eUtils.getAnnotationValueString(annotation, "targetNamespace");
      for (Service info : getServicesFromWsdl().getWebServices().keySet()) {
        QName webServiceName = info.getQName();
        if (Objects.equals(name, webServiceName.getLocalPart()) && Objects.equals(targetNamespace, webServiceName.getNamespaceURI())) {
          m_webServices.put(candidate, webServiceName);
          loadPortTypes(info, candidate);
        }
      }
    }
  }

  protected void loadPortTypes(Service service, IType webService) throws CoreException {
    if (getServicesFromWsdl().isEmpty()) {
      return;
    }
    Set<IType> candidates = findAllTypesAnnotatedWith(IJavaRuntimeTypes.WebService);
    if (candidates.isEmpty()) {
      return;
    }
    for (IType candidate : candidates) {
      if (candidate.isInterface()) {
        IAnnotation annotation = S2eUtils.getAnnotation(candidate, IJavaRuntimeTypes.WebService);
        String name = S2eUtils.getAnnotationValueString(annotation, "name");
        String targetNamespace = S2eUtils.getAnnotationValueString(annotation, "targetNamespace");
        for (Entry<String, QName> port : getServicesFromWsdl().getPorts(service).entrySet()) {
          QName portTypeName = port.getValue();
          if (Objects.equals(name, portTypeName.getLocalPart()) && Objects.equals(targetNamespace, portTypeName.getNamespaceURI())) {
            Set<IType> portTypesForCurrentService = m_portTypes.get(webService);
            if (portTypesForCurrentService == null) {
              portTypesForCurrentService = new LinkedHashSet<>(3);
              m_portTypes.put(webService, portTypesForCurrentService);
            }
            portTypesForCurrentService.add(candidate);
            m_portTypeNamesInWsdl.put(candidate, portTypeName);
          }
        }
      }
    }
  }

  protected Set<IType> findAllTypesAnnotatedWith(String fqn) throws CoreException {
    IJavaSearchScope scope = S2eUtils.createJavaSearchScope(new IJavaElement[]{getJavaProject()});
    return S2eUtils.findAllTypesAnnotatedWith(fqn, scope, null);
  }

  protected ITypeHierarchy getTypeHierarchy(IType t) throws JavaModelException {
    if (!S2eUtils.exists(t)) {
      return null;
    }
    ITypeHierarchy hierarchy = m_hierarchyCache.get(t);
    if (hierarchy != null) {
      return hierarchy;
    }
    hierarchy = t.newTypeHierarchy(null);
    m_hierarchyCache.put(t, hierarchy);
    return hierarchy;
  }

  protected ITypeHierarchy getSuperTypeHierarchy(IType t) throws JavaModelException {
    if (!S2eUtils.exists(t)) {
      return null;
    }
    ITypeHierarchy hierarchy = m_superHierarchyCache.get(t);
    if (hierarchy != null) {
      return hierarchy;
    }
    hierarchy = t.newSupertypeHierarchy(null);
    m_superHierarchyCache.put(t, hierarchy);
    return hierarchy;
  }

  protected void loadBindings() throws CoreException {
    Document pomDocument = S2eUtils.getPomDocument(getJavaProject().getProject());
    if (pomDocument == null) {
      return;
    }
    final IFolder bindingFolder = WebServiceNewOperation.getBindingRootFolder(getJavaProject().getProject());
    if (!bindingFolder.exists()) {
      return;
    }

    try {
      IPath wsdlFolderRelativePath = getWsdl().getProjectRelativePath().makeRelativeTo(WebServiceNewOperation.getWsdlRootFolder(getWsdl().getProject()).getProjectRelativePath());
      List<String> paths = JaxWsUtils.getBindingPathsFromPom(pomDocument, wsdlFolderRelativePath.toString());
      if (paths.isEmpty()) {
        // nothing specified in pom: load default paths
        bindingFolder.accept(new IResourceProxyVisitor() {
          @Override
          public boolean visit(IResourceProxy proxy) throws CoreException {
            if (proxy.getType() == IResource.FILE) {
              m_bindings.add((IFile) proxy.requestResource());
            }
            return bindingFolder.getName().equals(proxy.getName());
          }
        }, IResource.DEPTH_ZERO);
      }
      else {
        // load paths
        for (String bindingRelPath : paths) {
          IFile candidate = bindingFolder.getFile(new Path(bindingRelPath));
          if (candidate.exists()) {
            m_bindings.add(candidate);
          }
        }
      }
    }
    catch (XPathExpressionException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  protected void loadJaxwsBindingFile() throws CoreException {
    if (m_bindings.isEmpty()) {
      return;
    }

    for (IFile candidate : m_bindings) {
      Document document = S2eUtils.readXmlDocument(candidate);
      Element rootTag = document.getDocumentElement();
      if (JaxWsUtils.BINDINGS_ELEMENT_NAME.equals(rootTag.getLocalName())) {
        String rootNs = rootTag.getNamespaceURI();
        if (JaxWsUtils.JAX_WS_NAMESPACE.equals(rootNs)) {
          m_jaxwsBindingFiles.add(candidate);
        }
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

  public List<IFile> getJaxWsBindingFiles() {
    return Collections.unmodifiableList(m_jaxwsBindingFiles);
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

  public IFile getWsdl() {
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
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(result);
  }

  public Set<IType> getAllPortTypes() {
    Set<IType> result = new LinkedHashSet<>();
    for (Set<IType> portTypesByService : m_portTypes.values()) {
      result.addAll(portTypesByService);
    }
    return result;
  }

  public Set<IType> getWebServices() {
    return Collections.unmodifiableSet(m_webServices.keySet());
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

  public List<IFile> getBindings() {
    return Collections.unmodifiableList(m_bindings);
  }

  public List<IType> getHandlers(IType portType) {
    List<IType> list = m_handlersFromDefinitions.get(portType);
    if (list == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(list);
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
    int result = getWsdl().getName().compareTo(o.getWsdl().getName());
    if (result != 0) {
      return result;
    }
    return getWsdl().getFullPath().makeAbsolute().toOSString().compareTo(getWsdl().getFullPath().makeAbsolute().toOSString());
  }

  @Override
  public String toString() {
    return getWsdl().getFullPath().toString();
  }
}
