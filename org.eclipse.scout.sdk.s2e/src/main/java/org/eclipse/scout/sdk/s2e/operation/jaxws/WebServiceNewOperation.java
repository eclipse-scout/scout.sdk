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
package org.eclipse.scout.sdk.s2e.operation.jaxws;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl.WebServiceNames;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.s.sourcebuilder.jaxws.EntryPointDefinitionSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.jaxws.WebServiceClientSourceBuilder;
import org.eclipse.scout.sdk.core.s.sourcebuilder.jaxws.WebServiceProviderSourceBuilder;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.operation.ResourceWriteOperation;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutStatus;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * <h3>{@link WebServiceNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WebServiceNewOperation implements IOperation {

  // in
  private boolean m_isCreateNewModule;
  private boolean m_isCreateConsumer;
  private boolean m_isCreateEmptyWsdl;
  private IJavaProject m_serverModule;
  private String m_artifactId;
  private String m_package;
  private IJavaProject m_jaxWsProject;
  private URL m_wsdlUrl;
  private String m_wsdlName;

  // out
  private IFile m_createdWsdlFile;
  private IFile m_createdJaxbBindingFile;
  private final List<IFile> m_createdJaxwsBindingFiles;
  private final List<IType> m_createdWebServiceClients;
  private final List<IType> m_createdEntryPointDefinitions;
  private final List<IType> m_createdProviderServiceImpls;
  private final List<String> m_createdUrlProperties;

  private String m_wsdlContent;
  private ParsedWsdl m_parsedWsdl;

  public WebServiceNewOperation() {
    m_createdWebServiceClients = new ArrayList<>(2);
    m_createdEntryPointDefinitions = new ArrayList<>(m_createdWebServiceClients.size());
    m_createdProviderServiceImpls = new ArrayList<>(m_createdWebServiceClients.size());
    m_createdUrlProperties = new ArrayList<>(m_createdWebServiceClients.size());
    m_createdJaxwsBindingFiles = new ArrayList<>(m_createdWebServiceClients.size());
  }

  @Override
  public String getOperationName() {
    return "Create new Web Service";
  }

  @Override
  public void validate() {
    if (isCreateNewModule()) {
      Validate.notNull(getServerModule(), "Target module pom file must be specified when creating a new jaxws module.");
      Validate.isTrue(getServerModule().exists(), "Target module pom file could not be found.");
      Validate.isTrue(StringUtils.isNotBlank(getArtifactId()), "ArtifactId cannot be empty when creating a new jaxws module.");
    }
    else {
      Validate.notNull(getJaxWsProject(), "JaxWs project must be specified.");
      Validate.isTrue(getJaxWsProject().exists(), "JaxWs project does not exist.");
    }
    if (isCreateEmptyWsdl()) {
      Validate.isTrue(StringUtils.isNotBlank(getWsdlName()), "WSDL name cannot be empty when creating an empty WSDL.");
    }
    else {
      Validate.notNull(getWsdlUrl(), "WSDL URL cannot be null.");
    }
    Validate.isTrue(StringUtils.isNotBlank(getPackage()), "Target package cannot be empty.");
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    final SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 100);
    if (progress.isCanceled()) {
      return;
    }

    // create new java project
    if (isCreateNewModule()) {
      setJaxWsProject(createNewJaxWsModule(progress.newChild(39), workingCopyManager));
    }
    progress.setWorkRemaining(61);
    getJaxWsProject().getProject().refreshLocal(IResource.DEPTH_INFINITE, progress.newChild(1));
    String lineDelimiter = S2eUtils.lineSeparator(getJaxWsProject());
    if (progress.isCanceled()) {
      return;
    }

    // read wsdl data from remote
    String wsdlBaseName = getWsdlBaseName();
    if (isCreateEmptyWsdl()) {
      setWsdlContent(JaxWsUtils.getEmptyWsdl(wsdlBaseName, getPackage(), lineDelimiter).toString());
    }
    else {
      setWsdlContent(readXmlFromUrl(getWsdlUrl()));
    }
    setParsedWsdl(parseWsdl(getWsdlBaseUri(wsdlBaseName))); // parse the WSDL using remote URI first
    if (getParsedWsdl().isEmpty()) {
      SdkLog.warning("No service or port found in WSDL: Generation of web service aborted.");
      return;
    }
    progress.worked(10);
    if (progress.isCanceled()) {
      return;
    }

    // download all resources that belong the WSDL into the project and re-parse the created WSDL with the local paths
    IFolder bindingFolder = getBindingFolder(getJaxWsProject(), wsdlBaseName);
    setCreatedWsdlFile(writeWsdlToProject(wsdlBaseName, progress.newChild(2), workingCopyManager));
    copyReferencedResources(wsdlBaseName, progress.newChild(1), workingCopyManager);
    setParsedWsdl(parseWsdl(getTargetWsdlFileUri(wsdlBaseName))); // re-parse the WSDL using local URI

    // create bindings, add section to pom
    setCreatedJaxbBindingFile(createJaxbBinding(bindingFolder, lineDelimiter, progress.newChild(1), workingCopyManager));
    createJaxwsBindings(bindingFolder, lineDelimiter, progress.newChild(2), workingCopyManager);
    IPath wsdlFolderRelativePath = getCreatedWsdlFile().getProjectRelativePath().makeRelativeTo(getWsdlRootFolder(getJaxWsProject().getProject()).getProjectRelativePath());
    addWsdlToPom(wsdlFolderRelativePath.toString(), bindingFolder.getName(), progress.newChild(2), workingCopyManager);

    if (isCreateConsumer()) {
      // create web service client class
      createDerivedResources(progress.newChild(30), workingCopyManager);
      createWebServiceClients(progress.newChild(10), workingCopyManager);
    }
    else {
      // create web service provider
      enableApt(lineDelimiter, progress.newChild(2), workingCopyManager);
      createEntryPointDefinitions(progress.newChild(4), workingCopyManager);
      createDerivedResources(progress.newChild(30), workingCopyManager);
      createProviderServiceImplementations(progress.newChild(4), workingCopyManager);
    }

    if (isCreateNewModule()) {
      setIgnoreOptionalProblems("target/generated-sources/wsimport", progress.newChild(2));
    }
    else {
      S2eUtils.mavenUpdate(Collections.singleton(getJaxWsProject().getProject()), false, true, false, false, progress.newChild(2));
    }
  }

  protected void setIgnoreOptionalProblems(String entryPath, IProgressMonitor monitor) throws JavaModelException {
    final IJavaProject jaxWsProject = getJaxWsProject();
    final IClasspathEntry[] rawClasspathEntries = jaxWsProject.getRawClasspath();
    final List<IClasspathEntry> newEntries = new ArrayList<>(rawClasspathEntries.length);
    final org.eclipse.core.runtime.Path entryPathToSearch = new org.eclipse.core.runtime.Path('/' + jaxWsProject.getElementName() + '/' + entryPath);

    for (IClasspathEntry entry : rawClasspathEntries) {
      if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && entry.getPath().equals(entryPathToSearch)) {
        IClasspathAttribute[] origAttributes = entry.getExtraAttributes();
        List<IClasspathAttribute> newAttributes = new ArrayList<>(origAttributes.length + 1);
        for (IClasspathAttribute attrib : origAttributes) {
          if (!IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS.equals(attrib.getName())) {
            newAttributes.add(attrib);
          }
        }
        newAttributes.add(JavaCore.newClasspathAttribute(IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS, Boolean.TRUE.toString()));
        newEntries.add(JavaCore.newSourceEntry(entry.getPath(), entry.getInclusionPatterns(), entry.getExclusionPatterns(), entry.getOutputLocation(), newAttributes.toArray(new IClasspathAttribute[newAttributes.size()])));
      }
      else {
        newEntries.add(entry);
      }
    }
    jaxWsProject.setRawClasspath(newEntries.toArray(new IClasspathEntry[newEntries.size()]), monitor);
  }

  protected String getWsdlBaseName() {
    String wsdlFileName = getWsdlFileName();
    wsdlFileName = wsdlFileName.substring(0, wsdlFileName.length() - JaxWsUtils.WSDL_FILE_EXTENSION.length());
    return JaxWsUtils.removeCommonSuffixes(wsdlFileName);
  }

  protected void createDerivedResources(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    RebuildArtifactsOperation op = new RebuildArtifactsOperation();
    op.setJavaProject(getJaxWsProject());
    op.validate();
    op.run(monitor, workingCopyManager);
  }

  protected IJavaEnvironment createNewEnv() {
    return ScoutSdkCore.createJavaEnvironment(getJaxWsProject());
  }

  protected void createProviderServiceImplementations(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws JavaModelException {
    IJavaEnvironment env = createNewEnv(); // do create a new environment because the file system changed.
    for (Entry<Service, WebServiceNames> service : getParsedWsdl().getServiceNames().entrySet()) {
      WebServiceNames names = service.getValue();
      Set<PortType> portTypes = getParsedWsdl().getPortTypes(service.getKey());
      for (PortType pt : portTypes) {
        String portTypeName = pt.getQName().getLocalPart();
        WebServiceProviderSourceBuilder wspsb = new WebServiceProviderSourceBuilder(names.getWebServiceProviderImplClassName(portTypeName), getPackage(), env);
        wspsb.setPortTypeSignature(Signature.createTypeSignature(getPackage() + '.' + names.getPortTypeClassName(portTypeName)));
        wspsb.setup();
        m_createdProviderServiceImpls.add(S2eUtils.writeType(S2eUtils.getPrimarySourceFolder(getJaxWsProject()), wspsb, env, monitor, workingCopyManager));
      }
    }
  }

  protected void createEntryPointDefinitions(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws JavaModelException {
    IJavaEnvironment env = createNewEnv();
    for (Entry<Service, WebServiceNames> service : getParsedWsdl().getServiceNames().entrySet()) {
      WebServiceNames names = service.getValue();
      Set<PortType> portTypes = getParsedWsdl().getPortTypes(service.getKey());
      for (PortType pt : portTypes) {
        String portTypeName = pt.getQName().getLocalPart();
        EntryPointDefinitionSourceBuilder epdsb = new EntryPointDefinitionSourceBuilder(names.getEntryPointDefinitionClassName(portTypeName), getPackage(), env);
        epdsb.setPortTypeFqn(getPackage() + '.' + names.getPortTypeClassName(portTypeName));
        epdsb.setEntryPointPackage(getPackage());
        epdsb.setEntryPointName(names.getEntryPointClassName(portTypeName));
        epdsb.setPortName(getParsedWsdl().getPortName(service.getKey(), pt));
        epdsb.setServiceName(names.getWebServiceNameFromWsdl());
        epdsb.setup();
        m_createdEntryPointDefinitions.add(S2eUtils.writeType(S2eUtils.getPrimarySourceFolder(getJaxWsProject()), epdsb, env, monitor, workingCopyManager));
      }
    }
  }

  protected void createWebServiceClients(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IJavaEnvironment env = createNewEnv();
    for (Entry<Service, WebServiceNames> service : getParsedWsdl().getServiceNames().entrySet()) {
      WebServiceNames names = service.getValue();
      Set<PortType> portTypes = getParsedWsdl().getPortTypes(service.getKey());
      for (PortType pt : portTypes) {
        String portTypeName = pt.getQName().getLocalPart();
        WebServiceClientSourceBuilder wscsb = new WebServiceClientSourceBuilder(names.getWebServiceClientClassName(portTypeName), getPackage(), env);
        wscsb.setPortTypeSignature(Signature.createTypeSignature(getPackage() + '.' + names.getPortTypeClassName(portTypeName)));
        wscsb.setServiceSignature(Signature.createTypeSignature(getPackage() + '.' + names.getWebServiceClassName()));
        wscsb.setup();
        m_createdWebServiceClients.add(S2eUtils.writeType(S2eUtils.getPrimarySourceFolder(getJaxWsProject()), wscsb, env, monitor, workingCopyManager));
        m_createdUrlProperties.add(wscsb.getUrlPropertyName());
      }
    }
  }

  protected void enableApt(String lineDelimiter, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    getJaxWsProject().setOption("org.eclipse.jdt.core.compiler.processAnnotations", "enabled");

    IEclipsePreferences aptUiPluginPreferenceNode = new ProjectScope(getJaxWsProject().getProject()).getNode("org.eclipse.jdt.apt.core");
    aptUiPluginPreferenceNode.putBoolean("org.eclipse.jdt.apt.aptEnabled", true);
    aptUiPluginPreferenceNode.put("org.eclipse.jdt.apt.genSrcDir", "target/generated-sources/annotations");
    aptUiPluginPreferenceNode.putBoolean("org.eclipse.jdt.apt.reconcileEnabled", true);
    try {
      aptUiPluginPreferenceNode.flush();
    }
    catch (BackingStoreException e) {
      SdkLog.info("Unable to save the APT preferences of project '{}'.", getJaxWsProject().getElementName(), e);
    }
    createFactoryPath(lineDelimiter, monitor, workingCopyManager);
  }

  protected void createFactoryPath(String lineDelimiter, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IFile factoryPathFile = getJaxWsProject().getProject().getFile(".factorypath");
    if (factoryPathFile.exists()) {
      return;
    }

    String scoutVersion = ScoutProjectNewHelper.SCOUT_ARCHETYPES_VERSION;

    StringBuilder factoryPathBuilder = new StringBuilder();
    factoryPathBuilder.append("<factorypath>").append(lineDelimiter);
    factoryPathBuilder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/org/eclipse/scout/rt/org.eclipse.scout.jaxws.apt/").append(scoutVersion)
        .append("/org.eclipse.scout.jaxws.apt-").append(scoutVersion).append(".jar\" enabled=\"true\" runInBatchMode=\"false\"/>").append(lineDelimiter);
    factoryPathBuilder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/com/unquietcode/tools/jcodemodel/codemodel/1.0.3/codemodel-1.0.3.jar\" enabled=\"true\" runInBatchMode=\"false\"/>").append(lineDelimiter);
    factoryPathBuilder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/org/eclipse/scout/rt/org.eclipse.scout.rt.platform/").append(scoutVersion)
        .append("/org.eclipse.scout.rt.platform-").append(scoutVersion).append(".jar\" enabled=\"true\" runInBatchMode=\"false\"/>").append(lineDelimiter);
    factoryPathBuilder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/org/eclipse/scout/rt/org.eclipse.scout.rt.server.jaxws/").append(scoutVersion)
        .append("/org.eclipse.scout.rt.server.jaxws-").append(scoutVersion).append(".jar\" enabled=\"true\" runInBatchMode=\"false\"/>").append(lineDelimiter);
    factoryPathBuilder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar\" enabled=\"true\" runInBatchMode=\"false\"/>").append(lineDelimiter);
    factoryPathBuilder.append("  <factorypathentry kind=\"VARJAR\" id=\"M2_REPO/org/slf4j/slf4j-api/1.7.12/slf4j-api-1.7.12.jar\" enabled=\"true\" runInBatchMode=\"false\"/>").append(lineDelimiter);
    factoryPathBuilder.append("</factorypath>").append(lineDelimiter);

    ResourceWriteOperation writeFactoryPath = new ResourceWriteOperation(factoryPathFile, factoryPathBuilder.toString());
    writeFactoryPath.validate();
    writeFactoryPath.run(monitor, workingCopyManager);
  }

  protected void copyReferencedResources(String baseName, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (Entry<URI, String> resource : getParsedWsdl().getReferencedResources().entrySet()) {
      URI sourceUri = resource.getKey();
      if (resource.getValue().equals(resource.getKey().toString())) {
        // the rel path is also absolute (don't download absolutely referenced files)
        continue;
      }

      String relPath = resource.getValue();
      IFolder wsdlFolder = getWsdlFolder(baseName);
      IFile target = wsdlFolder.getFile(relPath);
      try {
        String content = readXmlFromUrl(sourceUri.toURL());
        ResourceWriteOperation writeWsdl = new ResourceWriteOperation(target, content);
        writeWsdl.validate();
        writeWsdl.run(monitor, workingCopyManager);
      }
      catch (MalformedURLException e) {
        throw new CoreException(new ScoutStatus(e));
      }
    }
  }

  protected ParsedWsdl parseWsdl(URI documentBase) throws CoreException {
    try {
      return ParsedWsdl.create(documentBase, getWsdlContent(), true);
    }
    catch (WSDLException | UnsupportedEncodingException e) {
      throw new CoreException(new ScoutStatus("Unable to parse WSDL.", e));
    }
  }

  protected URI getWsdlBaseUri(String baseName) throws CoreException {
    URL wsdlUrl = getWsdlUrl();
    if (wsdlUrl == null) {
      return getTargetWsdlFileUri(baseName);
    }
    try {
      return wsdlUrl.toURI();
    }
    catch (URISyntaxException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  protected IFolder getWsdlFolder(String wsBaseName) {
    String wsdlFolderName = wsBaseName.toLowerCase();
    return getWsdlRootFolder(getJaxWsProject().getProject()).getFolder(wsdlFolderName);
  }

  protected URI getTargetWsdlFileUri(String baseName) {
    IFolder wsdlFolder = getWsdlFolder(baseName);
    IFile wsdlFile = wsdlFolder.getFile(getWsdlFileName());
    return wsdlFile.getLocation().toFile().toURI();
  }

  protected void addWsdlToPom(String wsdlFilePath, String bindingFolderName, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IFile pom = getJaxWsProject().getProject().getFile(IMavenConstants.POM);
    if (!pom.exists()) {
      return;
    }
    List<String> bindingFileNames = new ArrayList<>(getCreatedJaxwsBindingFiles().size() + 1);
    for (IFile createdJaxWsBinding : getCreatedJaxwsBindingFiles()) {
      bindingFileNames.add(createdJaxWsBinding.getName());
    }
    bindingFileNames.add(getCreatedJaxbBindingFile().getName());
    Document document = S2eUtils.readXmlDocument(pom);
    JaxWsUtils.addWsdlToPom(document, wsdlFilePath, bindingFolderName, bindingFileNames);
    S2eUtils.writeXmlDocument(document, pom, monitor, workingCopyManager);
  }

  protected static IFolder getBindingFolder(IJavaProject jaxWsProject, String wsBaseName) {
    String bindingFolderName = wsBaseName.toLowerCase();
    return getBindingRootFolder(jaxWsProject.getProject()).getFolder(bindingFolderName);
  }

  protected void createJaxwsBindings(IFolder wsdlBindingsFolder, String lineDelimiter, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    URI parent = wsdlBindingsFolder.getLocation().toFile().toURI();
    Map<Path, StringBuilder> jaxwsBindingContents = JaxWsUtils.getJaxwsBindingContents(getParsedWsdl(), parent, lineDelimiter, getPackage());
    for (Entry<Path, StringBuilder> binding : jaxwsBindingContents.entrySet()) {
      IFile jaxwsBindingXmlFile = null;
      if (jaxwsBindingContents.size() == 1) {
        jaxwsBindingXmlFile = wsdlBindingsFolder.getFile(JaxWsUtils.JAXWS_BINDINGS_FILE_NAME);
      }
      else {
        Path pathFileName = binding.getKey().getFileName();
        if (pathFileName == null) {
          // should not happen because zero len paths are skipped by JaxWsUtils.getJaxwsBindingContents().
          throw new IllegalArgumentException("zero length path found.");
        }

        String partName = pathFileName.toString().toLowerCase();
        if (partName.endsWith(JaxWsUtils.WSDL_FILE_EXTENSION)) {
          partName = partName.substring(0, partName.length() - JaxWsUtils.WSDL_FILE_EXTENSION.length());
        }

        int lastDotPos = JaxWsUtils.JAXWS_BINDINGS_FILE_NAME.lastIndexOf('.');
        StringBuilder fileName = new StringBuilder();
        fileName.append(JaxWsUtils.JAXWS_BINDINGS_FILE_NAME.substring(0, lastDotPos));
        fileName.append('-').append(partName).append(JaxWsUtils.JAXWS_BINDINGS_FILE_NAME.substring(lastDotPos));
        jaxwsBindingXmlFile = wsdlBindingsFolder.getFile(fileName.toString());
      }
      ResourceWriteOperation writeJaxwsBinding = new ResourceWriteOperation(jaxwsBindingXmlFile, binding.getValue().toString());
      writeJaxwsBinding.validate();
      writeJaxwsBinding.run(monitor, workingCopyManager);
      m_createdJaxwsBindingFiles.add(jaxwsBindingXmlFile);
    }
  }

  protected IFile createJaxbBinding(IFolder wsdlBindingsFolder, String lineDelimiter, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IFile jaxbBindingXmlFile = wsdlBindingsFolder.getFile(JaxWsUtils.JAXB_BINDINGS_FILE_NAME);
    ResourceWriteOperation writJaxbBinding = new ResourceWriteOperation(jaxbBindingXmlFile, JaxWsUtils.getJaxbBindingContent(lineDelimiter).toString());
    writJaxbBinding.validate();
    writJaxbBinding.run(monitor, workingCopyManager);
    return jaxbBindingXmlFile;
  }

  protected String getWsdlFileName() {
    String wsdlFileName = null;
    URL wsdlUrl = getWsdlUrl();
    if (wsdlUrl != null) {
      wsdlFileName = wsdlUrl.getPath();
      int lastSlashPos = wsdlFileName.lastIndexOf('/');
      int lastDotPos = wsdlFileName.lastIndexOf('.');
      if (lastDotPos < lastSlashPos) {
        lastDotPos = wsdlFileName.length();
      }
      wsdlFileName = wsdlFileName.substring(lastSlashPos + 1, lastDotPos);
    }
    else {
      wsdlFileName = getWsdlName();
      if (wsdlFileName.endsWith(ISdkProperties.SUFFIX_WS_PROVIDER)) {
        wsdlFileName = wsdlFileName.substring(0, wsdlFileName.length() - ISdkProperties.SUFFIX_WS_PROVIDER.length());
      }
    }
    wsdlFileName = wsdlFileName + JaxWsUtils.WSDL_FILE_EXTENSION;
    return wsdlFileName;
  }

  protected IFile writeWsdlToProject(String baseName, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IFolder wsdlFolder = getWsdlFolder(baseName);
    IFile wsdlFile = wsdlFolder.getFile(getWsdlFileName());

    ResourceWriteOperation writeWsdl = new ResourceWriteOperation(wsdlFile, getWsdlContent());
    writeWsdl.validate();
    writeWsdl.run(monitor, workingCopyManager);

    return wsdlFile;
  }

  protected String readXmlFromUrl(URL url) throws CoreException {
    try {
      // use document builder to download the stream content because it correctly handles the xml encoding as specified in the xml declaration.
      DocumentBuilder docBuilder = CoreUtils.createDocumentBuilder();
      Document xmlDoc = null;
      try (InputStream in = url.openStream()) {
        xmlDoc = docBuilder.parse(in);
      }
      return CoreUtils.xmlDocumentToString(xmlDoc, false);
    }
    catch (IOException | SAXException | ParserConfigurationException | TransformerException e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

  protected IJavaProject createNewJaxWsModule(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    JaxWsModuleNewOperation op = new JaxWsModuleNewOperation();
    op.setArtifactId(getArtifactId());
    op.setServerModule(getServerModule());
    op.validate();
    op.run(monitor, workingCopyManager);
    return JavaCore.create(op.getCreatedProject());
  }

  public static IFolder getBindingRootFolder(IProject jaxWsProject) {
    return getWebInfFolder(jaxWsProject).getFolder(JaxWsUtils.BINDING_FOLDER_NAME);
  }

  public static IFolder getWsdlRootFolder(IProject jaxWsProject) {
    return getWebInfFolder(jaxWsProject).getFolder(JaxWsUtils.WSDL_FOLDER_NAME);
  }

  public static IFolder getWebInfFolder(IProject jaxWsProject) {
    return jaxWsProject.getFolder(JaxWsUtils.MODULE_REL_WEBINF_FOLDER_PATH);
  }

  public boolean isCreateNewModule() {
    return m_isCreateNewModule;
  }

  public void setCreateNewModule(boolean isCreateNewModule) {
    m_isCreateNewModule = isCreateNewModule;
  }

  public IJavaProject getServerModule() {
    return m_serverModule;
  }

  public void setServerModule(IJavaProject targetModule) {
    m_serverModule = targetModule;
  }

  public String getArtifactId() {
    return m_artifactId;
  }

  public void setArtifactId(String artifactId) {
    m_artifactId = artifactId;
  }

  public IJavaProject getJaxWsProject() {
    return m_jaxWsProject;
  }

  public void setJaxWsProject(IJavaProject jaxWsProject) {
    m_jaxWsProject = jaxWsProject;
  }

  public URL getWsdlUrl() {
    return m_wsdlUrl;
  }

  public void setWsdlUrl(URL wsdlUrl) {
    m_wsdlUrl = wsdlUrl;
  }

  public IFile getCreatedWsdlFile() {
    return m_createdWsdlFile;
  }

  protected void setCreatedWsdlFile(IFile createdWsdlFile) {
    m_createdWsdlFile = createdWsdlFile;
  }

  public IFile getCreatedJaxbBindingFile() {
    return m_createdJaxbBindingFile;
  }

  protected void setCreatedJaxbBindingFile(IFile createdJaxbBindingFile) {
    m_createdJaxbBindingFile = createdJaxbBindingFile;
  }

  public List<IFile> getCreatedJaxwsBindingFiles() {
    return Collections.unmodifiableList(m_createdJaxwsBindingFiles);
  }

  public String getWsdlContent() {
    return m_wsdlContent;
  }

  protected void setWsdlContent(String wsdlContent) {
    m_wsdlContent = wsdlContent;
  }

  public String getPackage() {
    return m_package;
  }

  public void setPackage(String targetPackage) {
    m_package = targetPackage;
  }

  public List<IType> getCreatedWebServiceClients() {
    return Collections.unmodifiableList(m_createdWebServiceClients);
  }

  public boolean isCreateConsumer() {
    return m_isCreateConsumer;
  }

  public void setCreateConsumer(boolean isCreateConsumer) {
    m_isCreateConsumer = isCreateConsumer;
  }

  public boolean isCreateEmptyWsdl() {
    return m_isCreateEmptyWsdl;
  }

  public void setCreateEmptyWsdl(boolean isCreateEmptyWsdl) {
    m_isCreateEmptyWsdl = isCreateEmptyWsdl;
  }

  public String getWsdlName() {
    return m_wsdlName;
  }

  public void setWsdlName(String wsdlName) {
    m_wsdlName = wsdlName;
  }

  public List<IType> getCreatedEntryPointDefinitions() {
    return Collections.unmodifiableList(m_createdEntryPointDefinitions);
  }

  public List<IType> getCreatedProviderServiceImpls() {
    return Collections.unmodifiableList(m_createdProviderServiceImpls);
  }

  public List<String> getCreatedUrlProperties() {
    return Collections.unmodifiableList(m_createdUrlProperties);
  }

  public ParsedWsdl getParsedWsdl() {
    return m_parsedWsdl;
  }

  protected void setParsedWsdl(ParsedWsdl parsedWsdl) {
    m_parsedWsdl = parsedWsdl;
  }
}
