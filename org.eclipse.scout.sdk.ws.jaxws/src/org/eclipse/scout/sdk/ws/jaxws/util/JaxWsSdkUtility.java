/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jws.WebService;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.WebServiceClient;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.FolderSelectionDialog;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.ImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.operation.OverrideUnimplementedMethodsOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.part.AnnotationProperty;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.xml.internal.bind.api.impl.NameConverter;

@SuppressWarnings("restriction")
public final class JaxWsSdkUtility {

  private JaxWsSdkUtility() {
  }

  public static boolean existsFileInProject(IScoutBundle bundle, IFolder folder, File javaFile) {
    if (javaFile == null || folder == null) {
      return false;
    }

    IFile projectFile = bundle.getProject().getFile(folder.getProjectRelativePath().append(javaFile.getName()));
    if (projectFile.exists()) {
      IPath fullPathProjectFile = projectFile.getLocation();
      IPath fullPathJavaFile = new Path(javaFile.getPath());

      return fullPathProjectFile.equals(fullPathJavaFile);
    }
    return false;
  }

  public static Definition loadWsdlDefinition(IFileHandle fileHandle) {
    try {
      WSDLFactory factory = WSDLFactory.newInstance();
      WSDLReader reader = factory.newWSDLReader();
      return reader.readWSDL(fileHandle.getParent().getFullPath().toString(), new InputSource(fileHandle.getInputStream()));
    }
    catch (Exception e) {
      JaxWsSdk.logError(String.format("Could not load WSDL file '%s'", fileHandle.getName()), e);
      return null;
    }
  }

  public static org.w3c.dom.Document createNewXmlDocument(InputStream is) throws SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder docBuilder = factory.newDocumentBuilder();
      org.w3c.dom.Document document = docBuilder.parse(is);
      return document;
    }
    catch (ParserConfigurationException e) {
      return null;
    }
  }

  public static org.w3c.dom.Document createNewXmlDocument(String rootTagName) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder docBuilder = factory.newDocumentBuilder();
      org.w3c.dom.Document document = docBuilder.newDocument();
      document.appendChild(document.createElement(rootTagName));
      return document;
    }
    catch (ParserConfigurationException e) {
      return null;
    }
  }

  public static List<Element> getChildElementsWithAttributes(Element parent, String tagName, String requiredAttributeName, String requiredAttributeValue) {
    NodeList endpoints = parent.getElementsByTagName(tagName);
    LinkedList<Element> result = new LinkedList<Element>();
    for (int i = 0; i < endpoints.getLength(); i++) {
      Node n = endpoints.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element e = (Element) n;
        if (e.hasAttribute(requiredAttributeName)) {
          String val = e.getAttribute(requiredAttributeName);
          if (CompareUtility.equals(requiredAttributeValue, val)) {
            result.add(e);
          }
        }
      }
    }
    return result;
  }

  public static Element getParentElement(Node n) {
    Node node = n.getParentNode();
    while (node != null) {
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        return (Element) node;
      }
      node = node.getParentNode();
    }
    return null;
  }

  public static String getXmlPrefix(Element e) {
    if (e == null) {
      return null;
    }
    String n = e.getNodeName();
    int pos = n.indexOf(':');
    if (pos > 0) {
      return n.substring(0, pos);
    }
    return null;
  }

  public static String getXmlAttribute(Element e, String attributeName, String defaultValue) {
    if (e == null) {
      return defaultValue;
    }
    if (e.hasAttribute(attributeName)) {
      return e.getAttribute(attributeName);
    }
    return defaultValue;
  }

  public static Element getFirstChildElementByTagName(Element parent, String tagName) {
    NodeList children = parent.getElementsByTagName(tagName);
    for (int i = 0; i < children.getLength(); i++) {
      Node n = children.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        return (Element) n;
      }
    }
    return null;
  }

  public static String getXmlContent(Document xml) {
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      tf.setAttribute("indent-number", 3);
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(xml), new StreamResult(writer));
      return writer.getBuffer().toString();
    }
    catch (Exception e) {
      JaxWsSdk.logError("unable to get xml from DOM document", e);
      return "";
    }
  }

  public static List<Element> getChildElements(NodeList nodeList) {
    return getChildElements(nodeList, null);
  }

  public static void removeAllChildElements(Element e, String tagName) {
    for (Element c : getChildElements(e.getChildNodes(), tagName)) {
      e.removeChild(c);
    }
  }

  public static Element getChildElement(NodeList nodeList, String tagName) {
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node item = nodeList.item(i);
      if (item instanceof Element) {
        Element e = (Element) item;
        if (tagName == null || tagName.equals(e.getTagName())) {
          return e;
        }
      }
    }
    return null;
  }

  public static List<Element> getChildElements(NodeList nodeList, String tagName) {
    ArrayList<Element> elements = new ArrayList<Element>(nodeList.getLength());
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node item = nodeList.item(i);
      if (item instanceof Element) {
        Element e = (Element) item;
        if (tagName == null || tagName.equals(e.getTagName())) {
          elements.add(e);
        }
      }
    }
    return elements;
  }

  public static boolean exists(IResource resource) {
    refreshLocal(resource, IResource.DEPTH_ONE);
    return resource != null && resource.exists() && resource.isSynchronized(IResource.DEPTH_ONE);
  }

  public static void refreshLocal(IResource resource, int depth) {
    try {
      if (resource != null && !resource.isSynchronized(depth)) {
        resource.refreshLocal(depth, new NullProgressMonitor());
      }
    }
    catch (CoreException e) {
      JaxWsSdk.logWarning("The 'resource '" + resource.getFullPath().toString() + "' could not be synchronized with the filesystem.");
    }
  }

  public static IFile getFile(IScoutBundle scoutBundle, IPath projectRelativePath, boolean autoCreate) {
    if (projectRelativePath == null || projectRelativePath.isEmpty()) {
      return null;
    }
    IFile file = scoutBundle.getProject().getFile(projectRelativePath.makeRelative());
    ensureFileAccessibleAndRegistered(file, autoCreate);
    return file;
  }

  public static void ensureFileAccessibleAndRegistered(IFile file, boolean autoCreate) {
    refreshLocal(file, IResource.DEPTH_ZERO);

    try {
      if (!exists(file) && autoCreate) {
        // create the folders if they do not exist yet
        if (file.getParent() instanceof IFolder) {
          ResourceUtility.mkdirs(file.getParent(), new NullProgressMonitor());
        }
        // the file does not already exist. Therefore create an empty file
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        file.create(inputStream, true, new NullProgressMonitor());
      }

      // register folder in build properties
      if (autoCreate && file.getParent() instanceof IFolder) {
        IFolder folder = (IFolder) file.getParent();
        if (!folder.getProjectRelativePath().toString().contains("build")) {
          PluginModelHelper h = new PluginModelHelper(file.getProject());
          h.BuildProperties.addBinaryBuildEntry(folder);
          h.save();
        }
      }
    }
    catch (CoreException e) {
      JaxWsSdk.logError("Unable to create file '" + file.getFullPath().toOSString() + "'.", e);
    }
  }

  public static IFolder getFolder(IScoutBundle scoutBundle, IPath projectRelativePath, boolean autoCreate) {
    if (projectRelativePath == null || projectRelativePath.isEmpty()) {
      return null;
    }
    IFolder folder = scoutBundle.getProject().getFolder(projectRelativePath.makeRelative());
    ensureFolderAccessibleAndRegistered(folder, autoCreate);
    return folder;
  }

  public static void ensureFolderAccessibleAndRegistered(IFolder folder, boolean autoCreate) {
    refreshLocal(folder, IResource.DEPTH_INFINITE);

    try {
      if (!folder.exists() && autoCreate) {
        // create the folders if they do not exist yet
        ResourceUtility.mkdirs(folder, new NullProgressMonitor());
      }

      // register folder in build properties
      if (autoCreate && !folder.getProjectRelativePath().toString().contains("build")) {
        PluginModelHelper h = new PluginModelHelper(folder.getProject());
        h.BuildProperties.addBinaryBuildEntry(folder);
        h.save();
      }
    }
    catch (CoreException e) {
      JaxWsSdk.logError("Unable to create folder '" + folder.getFullPath().toOSString() + "'", e);
    }
  }

  /**
   * Creates an import directive in the compilation unit for the given {@link IType}.
   * The directive is only created if necessary.
   *
   * @param declaringType
   *          the type the directive is to be created in
   * @param typeForImportDirective
   *          the type the directive is to be created for
   */
  public static void createImportDirective(IType declaringType, IType typeForImportDirective) {
    try {
      String resolveTypeName = JaxWsSdkUtility.resolveTypeName(declaringType, typeForImportDirective);
      if (resolveTypeName == null) {
        return;
      }
      if (typeForImportDirective.getFullyQualifiedName().equals(resolveTypeName)) {
        return; // no import directive necessary, as type must be used fully qualified
      }
      declaringType.getCompilationUnit().createImport(typeForImportDirective.getFullyQualifiedName().replaceAll("\\$", "."), null, new NullProgressMonitor());
    }
    catch (Exception e) {
      JaxWsSdk.logError("Unable to create import directive", e);
    }
  }

  public static void organizeImports(IType type) {
    try {
      CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(type.getJavaProject());
      CompilationUnit astRoot = SharedASTProvider.getAST(type.getCompilationUnit(), SharedASTProvider.WAIT_ACTIVE_ONLY, null);

      IChooseImportQuery chooseImportQuery = new IChooseImportQuery() {
        @Override
        public TypeNameMatch[] chooseImports(TypeNameMatch[][] openChoices, ISourceRange[] ranges) {
          return new TypeNameMatch[0];
        }
      };

      OrganizeImportsOperation op = new OrganizeImportsOperation(type.getCompilationUnit(), astRoot, settings.importIgnoreLowercase, !type.getCompilationUnit().isWorkingCopy(), true, chooseImportQuery);
      op.run(new NullProgressMonitor());
    }
    catch (Exception e) {
      JaxWsSdk.logError(e);
    }
  }

  /**
   * Excludes the composite from the layout manager if it does not contain any children and triggers to relayout the
   * composite.
   *
   * @param composite
   */
  public static void doLayout(Composite composite) {
    if (composite != null && !composite.isDisposed()) {
      if (composite.getLayoutData() instanceof GridData) {
        if (composite.getChildren().length == 0) {
          ((GridData) composite.getLayoutData()).heightHint = 0; // therefore this workaround is used, but as a drawback, the section has to collapsed and expanded
        }
        else {
          ((GridData) composite.getLayoutData()).heightHint = SWT.DEFAULT; // therefore this workaround is used, but as a drawback, the section has to collapsed and expanded
        }
      }
      composite.layout(true, true);
    }
  }

  /**
   * workaround for proper redraw of sections which contain composites to be excluded / included depending one some
   * conditions.
   * This must take place when getForm()#redraw=true!
   *
   * @param section
   */
  public static void doLayoutSection(ISection section) {
    if (section.isExpanded()) {
      section.setExpanded(false);
      section.setExpanded(true);
    }
  }

  public static void disposeChildControls(Composite composite) {
    if (composite != null && !composite.isDisposed()) {
      for (Control child : composite.getChildren()) {
        child.dispose();
      }
    }
  }

  public static void setView(Composite composite, boolean enabled) {
    if (composite != null && !composite.isDisposed()) {
      composite.setEnabled(enabled);
      for (Control child : composite.getChildren()) {
        if (child instanceof Composite) {
          setView((Composite) child, enabled);
        }
        else {
          child.setEnabled(enabled);
        }
      }
    }
  }

  public static PortType getPortType(Definition wsdlDefinition, QName serviceQName, String portName) {
    if (wsdlDefinition == null || serviceQName == null || portName == null) {
      return null;
    }

    portName = QName.valueOf(portName).getLocalPart();

    Service service = wsdlDefinition.getService(serviceQName);
    if (service == null) {
      return null;
    }
    Port port = service.getPort(portName);
    if (port == null) {
      return null;
    }
    Binding binding = port.getBinding();
    if (binding == null) {
      return null;
    }
    return port.getBinding().getPortType();
  }

  /**
   * Resolves the requested PortType interface type located in the given jar file
   *
   * @param portTypeQName
   *          the PortType to be resolved or null to get all PortTypes
   * @param jarFile
   *          the jar file the PortType interface type to be searched in
   * @return
   */
  public static IType resolvePortTypeInterfaceType(final QName portTypeQName, IFile jarFile) {
    if (portTypeQName == null) {
      return null;
    }
    IType[] types = resolvePortTypeInterfaceTypes(portTypeQName, jarFile);
    if (types.length == 0) {
      return null;
    }
    else if (types.length > 1) {
      JaxWsSdk.logWarning("Multiple PortType interface types found for port type '" + portTypeQName + "'");
    }
    return types[0];
  }

  /**
   * Resolves PortType interface types located in the given jar file
   *
   * @param portTypeQName
   *          the PortType to be resolved or null to get all PortTypes
   * @param jarFile
   *          the jar file the PortType interface type to be searched in
   * @return
   */
  public static IType[] resolvePortTypeInterfaceTypes(final QName portTypeQName, IFile jarFile) {
    if (jarFile == null) {
      return new IType[0];
    }
    final Set<IType> types = new HashSet<IType>();
    try {
      new SearchEngine().search(
          SearchPattern.createPattern("*", IJavaSearchConstants.INTERFACE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH),
          new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
          new JarFileSearchScope(jarFile),
          new SearchRequestor() {
            @Override
            public void acceptSearchMatch(SearchMatch match) throws CoreException {
              if (!(match instanceof TypeDeclarationMatch)) {
                return;
              }

              IType candidate = (IType) match.getElement();
              if (!TypeUtility.exists(candidate) || !candidate.isBinary()) {
                // type must be binary
                return;
              }

              // candidates must be annotated WebService annotation
              IAnnotation annotation = JaxWsSdkUtility.getAnnotation(candidate, WebService.class.getName(), false);
              if (!TypeUtility.exists(annotation)) {
                return;
              }

              if (portTypeQName == null) {
                types.add(candidate);
                return;
              }

              // candidate must match the port type
              String serviceName = JdtUtility.getAnnotationValueString(annotation, "name");
              if (CompareUtility.equals(serviceName, portTypeQName.getLocalPart())) {
                types.add(candidate);
              }
            }
          },
          null
          );
    }
    catch (Exception e) {
      JaxWsSdk.logError("Failed to resolve portType interface type", e);
    }
    return types.toArray(new IType[types.size()]);
  }

  /**
   * Resolves the requested service type located in the given jar file
   *
   * @param serviceQName
   * @param jarFile
   * @return
   */
  public static IType resolveServiceType(final QName serviceQName, IFile jarFile) {
    if (serviceQName == null) {
      return null;
    }
    Set<IType> types = resolveServiceTypes(serviceQName, jarFile);
    if (types.size() == 0) {
      return null;
    }
    else if (types.size() > 1) {
      JaxWsSdk.logWarning("Multiple service types found for service '" + serviceQName + "'");
    }
    return CollectionUtility.firstElement(types);
  }

  /**
   * Resolves service types located in the given jar file
   *
   * @param serviceQName
   *          the service to be resolved or null to get all service types
   * @param jarFile
   *          the jar file the service type to be searched in
   * @return
   */
  public static Set<IType> resolveServiceTypes(final QName serviceQName, IFile jarFile) {
    final Set<IType> types = new HashSet<IType>();
    try {
      new SearchEngine().search(
          SearchPattern.createPattern("*", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_PATTERN_MATCH),
          new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
          new JarFileSearchScope(jarFile),
          new SearchRequestor() {
            @Override
            public void acceptSearchMatch(SearchMatch match) throws CoreException {
              if (!(match instanceof TypeDeclarationMatch)) {
                return;
              }
              IType candidate = (IType) match.getElement();
              if (!TypeUtility.exists(candidate) || !candidate.isBinary()) {
                // candidate must be binary
                return;
              }

              // candidate must be a concrete type
              if (!candidate.isClass() || Flags.isAbstract(candidate.getFlags())) {
                return;
              }

              // candidate must be of the type 'javax.xml.ws.Service'
              if (!JaxWsSdkUtility.isJdtSubType(javax.xml.ws.Service.class.getName(), candidate)) {
                return;
              }

              // candidates must be annotated WebServiceClient annotation
              IAnnotation annotation = JaxWsSdkUtility.getAnnotation(candidate, WebServiceClient.class.getName(), false);
              if (!TypeUtility.exists(annotation)) {
                return;
              }

              if (serviceQName == null) {
                types.add(candidate);
                return;
              }

              // candidate must match the service type
              String serviceName = JdtUtility.getAnnotationValueString(annotation, "name");
              if (CompareUtility.equals(serviceName, serviceQName.getLocalPart())) {
                types.add(candidate);
              }
            }
          },
          null
          );
    }
    catch (Exception e) {
      JaxWsSdk.logError("Failed to resolve portType interface type", e);
    }
    return types;
  }

  public static boolean isProviderAuthenticationSet(String fqn) {
    if (!StringUtility.hasText(fqn)) {
      return false;
    }
    fqn = fqn.replaceAll("\\$", "\\.");
    String noneAuthFqn = TypeUtility.getType(JaxWsRuntimeClasses.NullAuthenticationHandlerProvider).getFullyQualifiedName().replaceAll("\\$", "\\.");
    return TypeUtility.existsType(fqn) && !fqn.equals(noneAuthFqn);
  }

  public static IType extractGenericSuperType(IType type, int index) {
    try {
      if (!TypeUtility.exists(type)) {
        return null;
      }
      String superTypeSignature = type.getSuperclassTypeSignature();
      if (superTypeSignature == null) {
        return null;
      }
      String[] typeArguments = Signature.getTypeArguments(superTypeSignature);
      if (typeArguments.length == 0 || index >= typeArguments.length) {
        return null;
      }
      String signature = typeArguments[index];
      String fullyQualifiedName = JaxWsSdkUtility.getFullyQualifiedNameFromSignature(type, signature);

      if (TypeUtility.existsType(fullyQualifiedName)) {
        return TypeUtility.getType(fullyQualifiedName);
      }
      return null;
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError("could not extract generic super type", e);
    }
    return null;
  }

  public static QName extractServiceQNameFromWsClient(IType webserviceClientType) {
    IType serviceType = extractGenericSuperType(webserviceClientType, JaxWsConstants.GENERICS_WEBSERVICE_CLIENT_SERVICE_INDEX);
    if (!TypeUtility.exists(serviceType)) {
      return null;
    }

    // ensure service to be a subtype of Service
    if (!JaxWsSdkUtility.isJdtSubType(javax.xml.ws.Service.class.getName(), serviceType)) {
      return null;
    }

    IAnnotation annotation = JaxWsSdkUtility.getAnnotation(serviceType, WebServiceClient.class.getName(), false);
    return extractQNameFromAnnotation(annotation);
  }

  public static QName extractPortTypeQNameFromWsClient(IType webserviceClientType) {
    IType portTypeInterfaceType = extractGenericSuperType(webserviceClientType, JaxWsConstants.GENERICS_WEBSERVICE_CLIENT_PORT_TYPE_INDEX);
    if (!TypeUtility.exists(portTypeInterfaceType)) {
      return null;
    }

    IAnnotation annotation = JaxWsSdkUtility.getAnnotation(portTypeInterfaceType, WebService.class.getName(), false);
    return extractQNameFromAnnotation(annotation);
  }

  public static boolean isValidSourceFolder(IScoutBundle bundle, String sourceFolder) {
    if (sourceFolder == null) {
      return false;
    }

    try {
      for (IClasspathEntry classpathEntry : bundle.getJavaProject().getRawClasspath()) {
        if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE && sourceFolder.equals(classpathEntry.getPath().lastSegment())) {
          return true;
        }
      }
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError("Failed to validate source folder.", e);
    }
    return false;
  }

  public static String getPlainPortTypeName(String portTypeName) {
    if (portTypeName == null) {
      return null;
    }
    while (true) {
      if (portTypeName.toLowerCase().endsWith("porttype")) {
        portTypeName = portTypeName.substring(0, portTypeName.length() - "porttype".length());
        continue;
      }
      if (portTypeName.toLowerCase().endsWith("webservice")) {
        portTypeName = portTypeName.substring(0, portTypeName.length() - "webservice".length());
        continue;
      }
      if (portTypeName.toLowerCase().endsWith("service")) {
        portTypeName = portTypeName.substring(0, portTypeName.length() - "service".length());
        continue;
      }
      return portTypeName;
    }
  }

  public static String getRecommendedProviderImplPackageName(IScoutBundle bundle) {
    return StringUtility.join(".", bundle.getSymbolicName(), "services", "ws", "provider");
  }

  public static String getRecommendedHandlerPackageName(IScoutBundle bundle) {
    return StringUtility.join(".", bundle.getSymbolicName(), "services", "ws", "handler");
  }

  public static String getRecommendedConsumerImplPackageName(IScoutBundle bundle) {
    return StringUtility.join(".", bundle.getSymbolicName(), "services", "ws", "consumer");
  }

  public static String getRecommendedProviderSecurityPackageName(IScoutBundle bundle) {
    return StringUtility.join(".", bundle.getSymbolicName(), "services", "ws", "provider", "security");
  }

  public static String getRecommendedConsumerSecurityPackageName(IScoutBundle bundle) {
    return StringUtility.join(".", bundle.getSymbolicName(), "services", "ws", "consumer", "security");
  }

  public static String getRecommendedSessionPackageName(IScoutBundle bundle) {
    return StringUtility.join(".", bundle.getSymbolicName(), "services", "ws", "session");
  }

  public static String getRecommendedTargetNamespace(IScoutBundle bundle, String serviceName) {
    String[] segments = bundle.getSymbolicName().split("\\.");
    String projectSuffex = null;
    for (int i = segments.length - 1; i >= 0; i--) {
      String segment = segments[i];
      String segmentLow = segment.toLowerCase();
      // exclude node segment
      if ("server".equals(segmentLow)
          || "online".equals(segmentLow)
          || "offline".equals(segmentLow)) {
        continue;
      }
      projectSuffex = StringUtility.join(".", projectSuffex, segments[i]);
    }

    return "http://ws.services." + projectSuffex + "/" + serviceName + "/"; //trailing slash is required
  }

  /**
   * @param buildProperties
   *          to primary evaluate -p option of build properties. If null, JAX-WS mechanism is used to resolve package
   *          name
   * @param wsdlDefinition
   *          WSDL definition to resolve package name
   * @return
   */
  public static String resolveStubPackageName(Map<String, List<String>> buildProperties, Definition wsdlDefinition) {
    // global package specified by -p option in build properties
    String globalPackageName = JaxWsSdkUtility.getBuildProperty(buildProperties, JaxWsConstants.OPTION_PACKAGE);
    if (!StringUtility.isNullOrEmpty(globalPackageName)) {
      return globalPackageName;
    }

    if (wsdlDefinition == null) {
      return null;
    }

    // global package binding in WSDL file
    globalPackageName = getWsdlBindingPackageName(wsdlDefinition);
    if (!StringUtility.isNullOrEmpty(globalPackageName)) {
      return globalPackageName;
    }

    String targetNamespace = wsdlDefinition.getTargetNamespace();
    return JaxWsSdkUtility.targetNamespaceToPackageName(targetNamespace);
  }

  public static String targetNamespaceToPackageName(String targetNamespace) {
    if (targetNamespace == null) {
      return null;
    }

    NameConverter nameConverter = NameConverter.standard;
    try {
      return nameConverter.toPackageName(targetNamespace); // same mechanism as JAX-WS uses. WSDLModeler#getJavaPackage and XJC#getDefaultPackageName()
    }
    catch (Exception e) {
      JaxWsSdk.logError("failed to convert targetNamespace into package name");
    }
    return null;
  }

  public static Binding getBinding(Definition wsdlDefinition, QName serviceQName, String portName) {
    if (wsdlDefinition == null || serviceQName == null || portName == null) {
      return null;
    }

    portName = QName.valueOf(portName).getLocalPart();

    Service service = wsdlDefinition.getService(serviceQName);
    if (service == null) {
      return null;
    }
    Port port = service.getPort(portName);
    if (port == null) {
      return null;
    }
    return port.getBinding();
  }

  /**
   * To get an annotation on the given type. This is just a workaround as {@link IType#getAnnotation(String)} does not
   * work properly (changes are not reflected, e.g. after removing the annotation, it is still returned)
   *
   * @param declaringType
   * @param annotationName
   * @return
   */
  public static IAnnotation getAnnotation(IType declaringType, String fqnAnnotationName, boolean recursively) {
    ITypeHierarchy superTypeHierarchy = null;
    if (recursively) {
      superTypeHierarchy = TypeUtility.getSupertypeHierarchy(declaringType);
    }
    return getAnnotation(declaringType, fqnAnnotationName, recursively, superTypeHierarchy);
  }

  private static IAnnotation getAnnotation(IType declaringType, String fqnAnnotationName, boolean recursively, ITypeHierarchy superTypeHierarchy) {
    if (!TypeUtility.exists(declaringType)) {
      return null;
    }

    IAnnotation annotation = JdtUtility.getAnnotation(declaringType, fqnAnnotationName);
    if (TypeUtility.exists(annotation)) {
      return annotation;
    }

    if (recursively) {
      IType superType = superTypeHierarchy.getSuperclass(declaringType);
      return getAnnotation(superType, fqnAnnotationName, recursively, superTypeHierarchy);
    }
    return null;
  }

  public static boolean isAnnotationOnDeclaringType(IType declaringType, IAnnotation annotation) {
    if (annotation == null || declaringType == null) {
      return false;
    }
    IJavaElement parent = annotation.getParent();
    if (parent.getElementType() != IJavaElement.TYPE) {
      return false;
    }
    IType candidateType = (IType) parent;
    return candidateType.getFullyQualifiedName().equals(declaringType.getFullyQualifiedName());
  }

  public static Map<String, List<String>> getDefaultBuildProperties() {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    map.put("Xdebug", null);
    map.put("keep", null);
    map.put("verbose", null);

    List<String> values = new LinkedList<String>();
    values.add("2.1");
    map.put("target", values);
    return map;
  }

  public static IFile[] getBindingFiles(IScoutBundle bundle, Map<String, List<String>> buildProperties) {
    if (buildProperties == null || buildProperties.size() == 0) {
      return new IFile[0];
    }

    List<IFile> bindingFiles = new LinkedList<IFile>();
    for (Entry<String, List<String>> property : buildProperties.entrySet()) {
      if (property.getKey() == null || !property.getKey().equals(JaxWsConstants.OPTION_BINDING_FILE) || property.getValue() == null || property.getValue().size() == 0) {
        continue;
      }

      for (String bindingFileRaw : property.getValue()) {
        IFile bindingFile = JaxWsSdkUtility.getFile(bundle, new Path(bindingFileRaw), false);
        bindingFiles.add(bindingFile);
      }
    }
    return bindingFiles.toArray(new IFile[bindingFiles.size()]);
  }

  public static void addBuildProperty(Map<String, List<String>> buildProperties, String propertyName, String propertyValue) {
    if (!buildProperties.containsKey(propertyName)) {
      buildProperties.put(propertyName, new LinkedList<String>());
    }

    buildProperties.get(propertyName).add(propertyValue);
  }

  public static String getBuildProperty(Map<String, List<String>> buildProperties, String propertyName) {
    if (buildProperties == null || buildProperties.size() == 0) {
      return null;
    }

    for (Entry<String, List<String>> property : buildProperties.entrySet()) {
      if (CompareUtility.equals(property.getKey(), propertyName)) {
        List<String> values = property.getValue();
        if (values != null && values.size() > 0) {
          return values.get(0);
        }
      }
    }

    return null;
  }

  /**
   * To append an index to the markerGroupUUI
   *
   * @param markerGroupUUID
   * @param index
   * @return
   */
  public static String toMarkerGroupUUID(String markerGroupUUID, int index) {
    return StringUtility.join("_", markerGroupUUID, String.valueOf(index));
  }

  /**
   * To get a unique binding file name path within the build path
   *
   * @param bundle
   * @param alias
   * @return
   */
  public static IPath toUniqueProjectRelativeBindingFilePath(IScoutBundle bundle, String alias, String schemaTargetNamespace) {
    String filename = StringUtility.join("-", alias, schemaTargetNamespace, "bindings");
    filename = JaxWsSdkUtility.toValidFileName(filename);

    final IPath folderBindingFile = JaxWsConstants.PATH_BUILD;
    final IPath filePath = folderBindingFile.append(filename).addFileExtension("xml");
    IFile file = JaxWsSdkUtility.getFile(bundle, filePath, false);

    int i = 0;
    while (file.exists()) {
      final String newFileName = String.format("%s (%s)", filename, ++i);
      final IPath newFilePath = folderBindingFile.append(newFileName).addFileExtension("xml");
      file = JaxWsSdkUtility.getFile(bundle, newFilePath, false);
    }
    return file.getProjectRelativePath();
  }

  public static Color getColorLightGray() {
    Color color = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get("lightGray");
    if (color == null) {
      PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().put("lightGray", new RGB(245, 245, 245));
      color = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get("lightGray");
    }
    return color;
  }

  public static AnnotationProperty parseAnnotationTypeValue(IType declaringType, IAnnotation annotation, String property) {
    AnnotationProperty propertyValue = new AnnotationProperty();
    if (!TypeUtility.exists(declaringType)) {
      return propertyValue;
    }

    if (!TypeUtility.exists(annotation)) {
      return propertyValue;
    }

    try {
      for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
        if (pair.getMemberName().equals(property)) {
          String fqn = (String) pair.getValue();
          if (StringUtility.hasText(fqn)) {
            String propertySignature = SignatureCache.createTypeSignature((String) pair.getValue());
            String fullyQualifiedName = JaxWsSdkUtility.getFullyQualifiedNameFromSignature(declaringType, propertySignature);
            propertyValue.setInherited(false);
            propertyValue.setFullyQualifiedName(fullyQualifiedName);
            propertyValue.setDefined(true);
          }
          return propertyValue;
        }
      }

      // get default value
      propertyValue.setInherited(true);

      IType type = TypeUtility.getReferencedType(declaringType, annotation.getElementName(), false);
      if (TypeUtility.exists(type)) {
        String fqn = (String) type.getMethod(property, new String[0]).getDefaultValue().getValue();
        if (StringUtility.hasText(fqn)) {
          propertyValue.setFullyQualifiedName(fqn);
          propertyValue.setDefined(true);
        }
      }
      return propertyValue;
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError("failed to parse annotation property value", e);
    }
    return propertyValue;
  }

  /**
   * Supports SuperTypes to be at multiple locations
   *
   * @param bundle
   * @param superType
   * @return
   */
  public static IType[] getJdtSubTypes(IScoutBundle bundle, String fqnSuperType, boolean includeInterfaces, boolean includeAbstractTypes, boolean includeFinalTypes, boolean sameProject) {
    List<IType> types = new LinkedList<IType>();
    try {
      Set<IType> superTypes = TypeUtility.getTypes(fqnSuperType);
      for (IType superType : superTypes) {
        ITypeHierarchy hierarchy = TypeUtility.getSupertypeHierarchy(superType);
        Set<IType> candidates = hierarchy.getAllSubtypes(superType);
        for (IType candidate : candidates) {
          if (TypeUtility.exists(candidate)) {
            if (!includeInterfaces && Flags.isInterface(candidate.getFlags())) {
              continue;
            }
            if (!includeAbstractTypes && Flags.isAbstract(candidate.getFlags())) {
              continue;
            }
            if (!includeFinalTypes && Flags.isFinal(candidate.getFlags())) {
              continue;
            }
            if (sameProject && !bundle.getJavaProject().equals(candidate.getJavaProject())) {
              continue;
            }
            if (TypeUtility.isOnClasspath(candidate, bundle.getJavaProject())) {
              types.add(candidate);
            }
          }
        }
      }
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError("failed to get subclasses of '" + fqnSuperType + "'", e);
    }

    JaxWsSdkUtility.sortTypesByName(types, true);
    return types.toArray(new IType[types.size()]);
  }

  /**
   * Supports SuperTypes to be at multiple locations.
   * E.g. JAX-WS classes (i.e. javax.xml.ws.Service) which is defined in JRE but also might be defined in a WLS specific
   * library fragment.
   *
   * @param candidateToCheck
   * @param fqnSuperType
   * @return
   */
  public static boolean isJdtSubType(String fqnSuperType, IType candidateToCheck) {
    if (!TypeUtility.exists(candidateToCheck)) {
      return false;
    }

    IType superTypes = TypeUtility.getType(fqnSuperType);
    if (!TypeUtility.exists(superTypes)) {
      return false;
    }

    return TypeUtility.getSupertypeHierarchy(candidateToCheck).isSubtype(superTypes, candidateToCheck);
  }

  public static String resolveTypeName(IType declaringType, IType typeToBeResolved) throws CoreException {
    String typeSignature = SignatureCache.createTypeSignature(typeToBeResolved.getFullyQualifiedName());
    ImportValidator validator = new ImportValidator(declaringType.getCompilationUnit());
    return SignatureUtility.getTypeReference(typeSignature, validator);
  }

  public static boolean containsGlobalBindingSection(IScoutBundle bundle, Map<String, List<String>> propertiers, boolean checkForMultipleOccurences) {
    IFile[] bindingFiles = JaxWsSdkUtility.getBindingFiles(bundle, propertiers);

    List<XmlResource> bindingFileResources = new LinkedList<XmlResource>();
    for (IFile bindingFile : bindingFiles) {
      XmlResource xmlResource = new XmlResource(bundle);
      xmlResource.setFile(bindingFile);
      bindingFileResources.add(xmlResource);
    }

    return containsGlobalBindingSection(bindingFileResources.toArray(new XmlResource[bindingFileResources.size()]), checkForMultipleOccurences);
  }

  public static boolean containsGlobalBindingSection(XmlResource[] bindingFileResources, boolean checkForMultipleOccurences) {
    int count = 0;
    try {
      for (XmlResource bindingFileResource : bindingFileResources) {
        Document xmlBindingFile = bindingFileResource.loadXml();
        for (Element e : getChildElements(xmlBindingFile.getDocumentElement().getChildNodes())) {
          if (e.getTagName().endsWith("globalBindings")) {
            String prefix = JaxWsSdkUtility.getXmlPrefix(e);
            String namespace = xmlBindingFile.getDocumentElement().getAttribute("xmlns:" + prefix);
            if ("http://java.sun.com/xml/ns/jaxb".equals(namespace)) {
              count++;
              if (checkForMultipleOccurences) {
                if (count > 1) {
                  return true;
                }
              }
              else {
                return true;
              }
              break;
            }
          }
        }
      }
    }
    catch (Exception e) {
      // nop
    }
    return false;
  }

  public static IFile getStubJarFile(IScoutBundle bundle, BuildJaxWsBean buildJaxWsBean, String wsdlFileName) {
    Map<String, List<String>> buildProperties = null;
    if (buildJaxWsBean != null) {
      buildProperties = buildJaxWsBean.getPropertiers();
    }
    return JaxWsSdkUtility.getStubJarFile(bundle, buildProperties, wsdlFileName);
  }

  /**
   * <p>
   * To get the stub file for a provider or consumer. The stub file is resolved as follows:
   * </p>
   * <ol>
   * <li>build properties are looked for a {@link JaxWsConstants#OPTION_JAR} entry</li>
   * <li>JAR file name is derived from WSDL file name</li>
   * </ol>
   *
   * @param bundle
   * @param buildProperties
   * @param wsdlFileName
   * @return
   */
  public static IFile getStubJarFile(IScoutBundle bundle, Map<String, List<String>> buildProperties, String wsdlFileName) {
    String jarFileName = null;
    if (buildProperties != null) {
      // custom JAR name specified by -jar option in build properties
      String customJarFileName = JaxWsSdkUtility.getBuildProperty(buildProperties, JaxWsConstants.OPTION_JAR);
      if (StringUtility.hasText(customJarFileName)) {
        jarFileName = new Path(customJarFileName).removeFileExtension().lastSegment();
      }
    }
    // default jar name derived from WSDL file name
    if (jarFileName == null && wsdlFileName != null) {
      jarFileName = new Path(wsdlFileName).removeFileExtension().lastSegment();
    }
    if (jarFileName == null) {
      JaxWsSdk.logWarning("Failed to derive stub JAR file name. Ensure WSDL file to exist or specify the build property '" + JaxWsConstants.OPTION_JAR + "' with the JAR file name as its value");
      return null;
    }

    return JaxWsSdkUtility.toStubJarFile(bundle, jarFileName);
  }

  public static boolean registerJarLib(IScoutBundle bundle, IFile jarFile, boolean remove, IProgressMonitor monitor) {
    boolean success = true;
    JaxWsSdkUtility.refreshLocal(jarFile, IResource.DEPTH_ONE);

    IPath jarFilePath = jarFile.getProjectRelativePath();
    IJavaProject javaProject = bundle.getJavaProject();
    IProject project = bundle.getProject();

    // update Java class path in .classpath file
    try {
      List<IClasspathEntry> cpeList = new ArrayList<IClasspathEntry>();
      for (IClasspathEntry cpe : javaProject.getRawClasspath()) {
        if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
          if (!cpe.getPath().equals(project.getFullPath().append(jarFilePath))) {
            cpeList.add(cpe);
          }
        }
        else {
          cpeList.add(cpe);
        }
      }
      if (!remove) {
        cpeList.add(JavaCore.newLibraryEntry(project.getFullPath().append(jarFilePath), null, null, true));
      }
      javaProject.setRawClasspath(cpeList.toArray(new IClasspathEntry[cpeList.size()]), monitor);
    }
    catch (Exception e) {
      JaxWsSdk.logError("could not update Java class path in .classpath file", e);
    }

    // update Bundle-ClassPath in MANIFEST.MF
    PluginModelHelper h = new PluginModelHelper(project);
    try {
      if (remove) {
        h.Manifest.removeClasspathEntry(jarFile);
      }
      else {
        h.Manifest.addClasspathEntry(jarFile);
      }
      h.Manifest.addClasspathDefaultEntry();
    }
    catch (Exception e) {
      JaxWsSdk.logError("could not update Bundle-ClassPath in MANIFEST.MF", e);
      success = false;
    }

    // update bin.includes in build.properties
    try {
      // remove JAR file registration as folder is registered anyway
      h.BuildProperties.removeBinaryBuildEntry(jarFile);
      if (!remove) {
        // register folder, not file
        h.BuildProperties.addBinaryBuildEntry(JaxWsSdkUtility.getParentFolder(bundle, jarFile));
      }
    }
    catch (Exception e) {
      JaxWsSdk.logError("could not update bin.includes in build.properties", e);
      success = false;
    }
    h.save();

    return success;
  }

  public static void overrideUnimplementedMethodsAsync(IType type) {
    OverrideUnimplementedMethodsOperation op = new OverrideUnimplementedMethodsOperation();
    op.setType(type);
    new OperationJob(op).schedule();
  }

  public static void sortTypesByName(List<IType> list, boolean removeDuplicates) {
    if (removeDuplicates) {
      JaxWsSdkUtility.removeDuplicateEntries(list);
    }

    Collections.sort(list, new Comparator<IType>() {

      @Override
      public int compare(IType type1, IType type2) {
        return CompareUtility.compareTo(type1.getElementName(), type2.getElementName());
      }
    });
  }

  public static IFolder getParentFolder(IScoutBundle bundle, IFile file) {
    if (file == null) {
      return null;
    }
    IPath parentFolderPath = file.getProjectRelativePath().removeLastSegments(1);
    if (parentFolderPath.segmentCount() == 0) {
      return null;
    }
    return JaxWsSdkUtility.getFolder(bundle, parentFolderPath, false);
  }

  public static IFolder openProjectFolderDialog(IScoutBundle bundle, ViewerFilter filter, String title, String description, IFolder rootFolder, IFolder initialFolder) {
    if (!JaxWsSdkUtility.exists(rootFolder)) {
      rootFolder = JaxWsSdkUtility.getFolder(bundle, rootFolder.getProjectRelativePath(), true);
    }
    ILabelProvider labelProvider = new WorkbenchLabelProvider();
    ITreeContentProvider contentProvider = new WorkbenchContentProvider();
    FolderSelectionDialog dialog = new FolderSelectionDialog(ScoutSdkUi.getShell(), labelProvider, contentProvider);
    dialog.setTitle(title);
    dialog.setMessage(description);
    dialog.addFilter(filter);
    dialog.setHelpAvailable(false);
    dialog.setAllowMultiple(false);
    if (initialFolder != null) {
      dialog.setInitialSelection(initialFolder);
    }
    IPath parentFolderPath = rootFolder.getProjectRelativePath().removeLastSegments(1);
    dialog.setInput(JaxWsSdkUtility.getFolder(bundle, parentFolderPath, true));
    dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

    if (dialog.open() == Window.OK) {
      return (IFolder) dialog.getFirstResult();
    }
    return null;
  }

  /**
   * Workaround to the bug that sometimes the property page is not closed on structure change from external files.
   *
   * @param parentPage
   */
  public static void markStructureDirtyAndFixSelection(final IPage parentPage) {
    ScoutSdkUi.getDisplay().asyncExec(new Runnable() {

      @Override
      public void run() {
        try {
          // update selection if child has active selection
          IScoutExplorerPart explorer = ScoutSdkUi.getExplorer(false);
          if (explorer == null) {
            return;
          }
          IStructuredSelection selection = explorer.getSelection();
          if (selection.isEmpty()) {
            return;
          }
          if (new ArrayList<IPage>(parentPage.getChildren()).removeAll(selection.toList())) {
            // child node is selected -> update selection to the WebServiceProviderTablePage
            explorer.setSelection(new StructuredSelection(parentPage));
          }
        }
        catch (Exception e) {
          JaxWsSdk.logError(e);
        }
        parentPage.markStructureDirty();
      }
    });
  }

  /**
   * To obtain the fully qualified name
   *
   * @param declaringType
   *          the type which contains possible import directives
   * @param signature
   *          the signature obtained by {@link IField#getTypeSignature()} or {@link IMethod#getReturnType()},
   *          respectively
   * @return the fully qualified name
   * @throws JavaModelException
   */
  private static String getFullyQualifiedNameFromSignature(IType declaringType, String signature) throws JavaModelException {
    return TypeUtility.getReferencedTypeFqn(declaringType, StringUtility.join(".", Signature.getSignatureQualifier(signature), Signature.getSignatureSimpleName(signature)), false);
  }

  private static <T> void removeDuplicateEntries(List<T> list) {
    Set<T> elementsVisited = new HashSet<T>();
    Iterator<T> iterator = list.iterator();
    while (iterator.hasNext()) {
      T element = iterator.next();
      if (elementsVisited.contains(element)) {
        iterator.remove();
      }
      elementsVisited.add(element);
    }
  }

  private static IFile toStubJarFile(IScoutBundle bundle, String jarFileName) {
    if (jarFileName == null) {
      return null;
    }
    // ensure no file extension set
    jarFileName = new Path(jarFileName).removeFileExtension().lastSegment();
    // ensure valid filename
    jarFileName = JaxWsSdkUtility.toValidFileName(jarFileName);
    IPath path = JaxWsConstants.STUB_FOLDER.append(jarFileName).addFileExtension("jar");
    return JaxWsSdkUtility.getFile(bundle, path, false);
  }

  private static String toValidFileName(String name) {
    return name.replaceAll("[\\\\/\\:\\*\\?\\<\\>\"]", "");
  }

  private static String getWsdlBindingPackageName(Definition definition) {
    if (definition == null) {
      return null;
    }
    for (Object e : definition.getExtensibilityElements()) {
      if (e instanceof UnknownExtensibilityElement) {
        UnknownExtensibilityElement uee = (UnknownExtensibilityElement) e;
        if (uee.getElementType().equals(new QName("http://java.sun.com/xml/ns/jaxws", "bindings"))) {
          Element element = uee.getElement();
          NodeList nodes = element.getElementsByTagNameNS("http://java.sun.com/xml/ns/jaxws", "package");
          if (nodes.getLength() > 0) {
            Element globalPackageBindingElement = (Element) nodes.item(0);
            String name = globalPackageBindingElement.getAttribute("name");
            if (StringUtility.hasText(name)) {
              return name;
            }
          }
        }
      }
    }
    return null;
  }

  private static QName extractQNameFromAnnotation(IAnnotation annotation) {
    if (annotation == null || !annotation.exists()) {
      return null;
    }

    String localPart = null;
    String namespaceURI = null;
    try {
      for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
        if ("name".equals(pair.getMemberName())) {
          localPart = (String) pair.getValue();
        }
        else if ("targetNamespace".equals(pair.getMemberName())) {
          namespaceURI = (String) pair.getValue();
        }
        if (namespaceURI != null && localPart != null) {
          break;
        }
      }

      QName qname = null;
      if (namespaceURI != null && localPart != null) {
        qname = new QName(namespaceURI, localPart);
      }
      else if (localPart != null) {
        qname = new QName(localPart);
      }

      if (qname != null) {
        return qname;
      }
    }
    catch (Exception e) {
      JaxWsSdk.logError("could not extract QName from annotation '" + annotation.getElementName() + "'", e);
    }
    return null;
  }

  private static class JarFileSearchScope implements IJavaSearchScope {

    private IFile m_jarFile;

    public JarFileSearchScope(IFile jarFile) {
      m_jarFile = jarFile;
    }

    @Override
    public boolean encloses(String resourcePath) {
      return true;
    }

    @Override
    public boolean encloses(IJavaElement element) {
      return (element.getElementType() == IJavaElement.TYPE);
    }

    @Override
    public IPath[] enclosingProjectsAndJars() {
      return new IPath[]{m_jarFile.getFullPath()};
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean includesBinaries() {
      return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean includesClasspaths() {
      return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setIncludesBinaries(boolean includesBinaries) {
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setIncludesClasspaths(boolean includesClasspaths) {
    }

  }
}
