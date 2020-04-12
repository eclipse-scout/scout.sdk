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

import static java.util.Collections.unmodifiableList;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.transform.TransformerException;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils.JaxWsBindingMapping;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl.WebServiceNames;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Xml;
import org.w3c.dom.Document;

/**
 * <h3>{@link AbstractWebServiceNewOperation}</h3>
 *
 * @since 5.2.0
 */
public abstract class AbstractWebServiceNewOperation implements BiConsumer<IEnvironment, IProgress> {

  // in
  private boolean m_isCreateNewModule;
  private boolean m_isCreateConsumer;
  private boolean m_isCreateEmptyWsdl;
  private String m_package;
  private Path m_projectRoot;
  private URL m_wsdlUrl;
  private String m_wsdlName;
  private IClasspathEntry m_sourceFolder;

  // out
  private Path m_createdWsdlFile;
  private Path m_createdJaxbBindingFile;
  private final List<Path> m_createdJaxwsBindingFiles;
  private final List<IType> m_createdWebServiceClients;
  private final List<IType> m_createdEntryPointDefinitions;
  private final List<IType> m_createdProviderServiceImpls;
  private final List<String> m_createdUrlProperties;

  private StringBuilder m_wsdlContent;
  private ParsedWsdl m_parsedWsdl;

  protected AbstractWebServiceNewOperation() {
    m_createdWebServiceClients = new ArrayList<>(2);
    m_createdEntryPointDefinitions = new ArrayList<>(m_createdWebServiceClients.size());
    m_createdProviderServiceImpls = new ArrayList<>(m_createdWebServiceClients.size());
    m_createdUrlProperties = new ArrayList<>(m_createdWebServiceClients.size());
    m_createdJaxwsBindingFiles = new ArrayList<>(m_createdWebServiceClients.size());
  }

  @Override
  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public void accept(IEnvironment env, IProgress progress) {
    if (!isCreateNewModule()) {
      Ensure.notNull(getProjectRoot());
    }
    if (isCreateEmptyWsdl()) {
      Ensure.notBlank(getWsdlName(), "WSDL name cannot be empty when creating an empty WSDL.");
    }
    else {
      Ensure.notNull(getWsdlUrl(), "WSDL URL cannot be null.");
    }
    Ensure.notBlank(getPackage(), "Target package cannot be empty.");

    progress.init(toString(), 100);

    // create new java project
    if (isCreateNewModule()) {
      setProjectRoot(createNewJaxWsModule(env, progress.newChild(39)));
      IClasspathEntry primarySrcFolderOfNewModule = env.findJavaEnvironment(getProjectRoot())
          .flatMap(IJavaEnvironment::primarySourceFolder)
          .orElseThrow(() -> newFail("Unable to find java environment for newly created jaxws project."));
      setSourceFolder(primarySrcFolderOfNewModule);
    }
    progress.setWorkRemaining(61);

    // read wsdl data from remote
    String wsdlBaseName = getWsdlBaseName();
    if (isCreateEmptyWsdl()) {
      EmptyWsdlGenerator generator = new EmptyWsdlGenerator()
          .withName(wsdlBaseName)
          .withPackage(getPackage());
      setWsdlContent(env.createResource(generator, getSourceFolder()));
    }
    else {
      setWsdlContent(readXmlFromUrl(getWsdlUrl()));
    }
    progress.worked(5);

    // parse the WSDL using remote URI first
    setParsedWsdl(parseWsdl(getWsdlBaseUri(wsdlBaseName)));
    if (getParsedWsdl().isEmpty()) {
      SdkLog.warning("No service or port found in WSDL: Generation of web service aborted.");
      return;
    }
    progress.worked(8);

    // download all resources that belong the WSDL into the project and re-parse the created WSDL with the local paths
    Path bindingFolder = getBindingFolder(getProjectRoot(), wsdlBaseName);
    setCreatedWsdlFile(writeWsdlToProject(wsdlBaseName, env, progress.newChild(2)));
    copyReferencedResources(wsdlBaseName, env, progress.newChild(1));
    setParsedWsdl(parseWsdl(getTargetWsdlFileUri(wsdlBaseName))); // re-parse the WSDL using local URI

    // create bindings, add section to pom
    setCreatedJaxbBindingFile(createJaxbBinding(bindingFolder, env, progress.newChild(1)));
    createJaxwsBindings(bindingFolder, env, progress.newChild(2));
    Path wsdlFolderRelativePath = getWsdlRootFolder(getProjectRoot()).relativize(getCreatedWsdlFile());
    addWsdlToPom(wsdlFolderRelativePath, bindingFolder.getFileName().toString(), env, progress.newChild(2));

    if (isCreateConsumer()) {
      // create web service client class
      createDerivedResources(env, progress.newChild(30));
      getSourceFolder().javaEnvironment().reload();
      createWebServiceClients(env, progress.newChild(10));
    }
    else {
      // create web service provider
      createEntryPointDefinitions(env, progress.newChild(5));
      createDerivedResources(env, progress.newChild(30));
      getSourceFolder().javaEnvironment().reload();
      createProviderServiceImplementations(env, progress.newChild(5));
    }
  }

  protected String getWsdlBaseName() {
    String wsdlFileName = getWsdlFileName();
    wsdlFileName = wsdlFileName.substring(0, wsdlFileName.length() - JaxWsUtils.WSDL_FILE_EXTENSION.length());
    return JaxWsUtils.removeCommonSuffixes(wsdlFileName);
  }

  protected abstract void createDerivedResources(IEnvironment env, IProgress progress);

  protected abstract Path createNewJaxWsModule(IEnvironment env, IProgress progress);

  protected void createProviderServiceImplementations(IEnvironment env, IProgress progress) {
    for (Entry<Service, WebServiceNames> service : getParsedWsdl().getServiceNames().entrySet()) {
      Set<PortType> portTypes = getParsedWsdl().getPortTypes(service.getKey());
      for (PortType pt : portTypes) {
        String portTypeName = pt.getQName().getLocalPart();
        WebServiceProviderGenerator<?> wspsb = new WebServiceProviderGenerator<>()
            .withElementName(WebServiceNames.getWebServiceProviderImplClassName(portTypeName))
            .withPackageName(getPackage())
            .withPortType(getPackage() + JavaTypes.C_DOT + WebServiceNames.getPortTypeClassName(portTypeName));
        m_createdProviderServiceImpls.add(env.writeCompilationUnit(wspsb, getSourceFolder(), progress));
      }
    }
  }

  protected void createEntryPointDefinitions(IEnvironment env, IProgress progress) {
    for (Entry<Service, WebServiceNames> service : getParsedWsdl().getServiceNames().entrySet()) {
      WebServiceNames names = service.getValue();
      Set<PortType> portTypes = getParsedWsdl().getPortTypes(service.getKey());
      for (PortType pt : portTypes) {
        String portTypeName = pt.getQName().getLocalPart();
        EntryPointDefinitionGenerator<?> epdsb = new EntryPointDefinitionGenerator<>()
            .withElementName(WebServiceNames.getEntryPointDefinitionClassName(portTypeName))
            .withPackageName(getPackage())
            .withPortTypeFqn(getPackage() + JavaTypes.C_DOT + WebServiceNames.getPortTypeClassName(portTypeName))
            .withEntryPointName(WebServiceNames.getEntryPointClassName(portTypeName))
            .withEntryPointPackage(getPackage())
            .withPortName(getParsedWsdl().getPortName(service.getKey(), pt))
            .withServiceName(names.getWebServiceNameFromWsdl());
        m_createdEntryPointDefinitions.add(env.writeCompilationUnit(epdsb, getSourceFolder(), progress));
      }
    }
  }

  protected void createWebServiceClients(IEnvironment env, IProgress progress) {
    for (Entry<Service, WebServiceNames> service : getParsedWsdl().getServiceNames().entrySet()) {
      WebServiceNames names = service.getValue();
      Set<PortType> portTypes = getParsedWsdl().getPortTypes(service.getKey());
      for (PortType pt : portTypes) {
        String portTypeName = pt.getQName().getLocalPart();
        WebServiceClientGenerator<?> wscsb = new WebServiceClientGenerator<>()
            .withElementName(WebServiceNames.getWebServiceClientClassName(portTypeName))
            .withPackageName(getPackage())
            .withPortType(getPackage() + JavaTypes.C_DOT + WebServiceNames.getPortTypeClassName(portTypeName))
            .withService(getPackage() + JavaTypes.C_DOT + names.getWebServiceClassName());
        m_createdWebServiceClients.add(env.writeCompilationUnit(wscsb, getSourceFolder(), progress));
        m_createdUrlProperties.add(wscsb.urlPropertyName());
      }
    }
  }

  protected void copyReferencedResources(String baseName, IEnvironment env, IProgress progress) {
    for (Entry<URI, String> resource : getParsedWsdl().getReferencedResources().entrySet()) {
      URI sourceUri = resource.getKey();
      if (resource.getValue().equals(resource.getKey().toString())) {
        // the rel path is also absolute (don't download absolutely referenced files)
        continue;
      }

      String relPath = resource.getValue();
      Path wsdlFolder = getWsdlFolder(baseName);
      Path target = wsdlFolder.resolve(relPath);
      try {
        StringBuilder content = readXmlFromUrl(sourceUri.toURL());
        env.writeResource(content, target, progress);
      }
      catch (MalformedURLException e) {
        throw new SdkException(e);
      }
    }
  }

  protected ParsedWsdl parseWsdl(URI documentBase) {
    try {
      return ParsedWsdl.create(documentBase, getWsdlContent(), true);
    }
    catch (WSDLException | UnsupportedEncodingException e) {
      throw new SdkException(e);
    }
  }

  protected URI getWsdlBaseUri(String baseName) {
    URL wsdlUrl = getWsdlUrl();
    if (wsdlUrl == null) {
      return getTargetWsdlFileUri(baseName);
    }
    try {
      return wsdlUrl.toURI();
    }
    catch (URISyntaxException e) {
      throw new SdkException(e);
    }
  }

  protected Path getWsdlFolder(String wsBaseName) {
    String wsdlFolderName = wsBaseName.toLowerCase(Locale.ENGLISH);
    return getWsdlRootFolder(getProjectRoot()).resolve(wsdlFolderName);
  }

  protected URI getTargetWsdlFileUri(String baseName) {
    Path wsdlFolder = getWsdlFolder(baseName);
    Path wsdlFile = wsdlFolder.resolve(getWsdlFileName());
    return wsdlFile.toUri();
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected void addWsdlToPom(Path wsdlFolderRelativePath, String bindingFolderName, IEnvironment env, IProgress progress) {
    Path pom = getProjectRoot().resolve(IMavenConstants.POM);
    if (!Files.isReadable(pom) || !Files.isRegularFile(pom)) {
      return;
    }
    Collection<String> bindingFileNames = new ArrayList<>(getCreatedJaxwsBindingFiles().size() + 1);
    for (Path createdJaxWsBinding : getCreatedJaxwsBindingFiles()) {
      bindingFileNames.add(createdJaxWsBinding.getFileName().toString());
    }
    bindingFileNames.add(getCreatedJaxbBindingFile().getFileName().toString());

    try {
      Document document = Xml.get(pom);
      //noinspection HardcodedFileSeparator
      JaxWsUtils.addWsdlToPom(document, wsdlFolderRelativePath.toString().replace('\\', '/'), bindingFolderName, bindingFileNames);
      env.writeResource(Xml.documentToString(document, true), pom, progress);
    }
    catch (IOException | TransformerException e) {
      throw new SdkException(e);
    }
  }

  protected static Path getBindingFolder(Path jaxWsProject, String wsBaseName) {
    String bindingFolderName = wsBaseName.toLowerCase(Locale.ENGLISH);
    return getBindingRootFolder(jaxWsProject).resolve(bindingFolderName);
  }

  protected void createJaxwsBindings(Path wsdlBindingsFolder, IEnvironment env, IProgress progress) {
    URI parent = wsdlBindingsFolder.toUri();
    Map<String, StringBuilder> jaxwsBindingContents = getJaxwsBindingContents(getParsedWsdl(), parent, getPackage(), env);
    for (Entry<String, StringBuilder> binding : jaxwsBindingContents.entrySet()) {
      Path jaxwsBindingXmlFile;
      if (jaxwsBindingContents.size() == 1) {
        jaxwsBindingXmlFile = wsdlBindingsFolder.resolve(JaxWsUtils.JAXWS_BINDINGS_FILE_NAME);
      }
      else {
        String pathFileName = binding.getKey();
        if (pathFileName == null) {
          // should not happen because zero len paths are skipped by JaxWsUtils.getJaxwsBindingContents().
          throw new IllegalArgumentException("zero length path found.");
        }

        String partName = pathFileName.toLowerCase(Locale.ENGLISH);
        if (partName.endsWith(JaxWsUtils.WSDL_FILE_EXTENSION)) {
          partName = partName.substring(0, partName.length() - JaxWsUtils.WSDL_FILE_EXTENSION.length());
        }

        int lastDotPos = JaxWsUtils.JAXWS_BINDINGS_FILE_NAME.lastIndexOf('.');
        StringBuilder fileName = new StringBuilder();
        fileName.append(JaxWsUtils.JAXWS_BINDINGS_FILE_NAME, 0, lastDotPos);
        fileName.append('-').append(partName).append(JaxWsUtils.JAXWS_BINDINGS_FILE_NAME.substring(lastDotPos));
        jaxwsBindingXmlFile = wsdlBindingsFolder.resolve(fileName.toString());
      }
      env.writeResource(binding.getValue(), jaxwsBindingXmlFile, progress);
      m_createdJaxwsBindingFiles.add(jaxwsBindingXmlFile);
    }
  }

  /**
   * Gets the JaxWs binding contents for the given {@link ParsedWsdl}.
   *
   * @param parsedWsdl
   *          The {@link ParsedWsdl} holding all wsdl artifacts.
   * @param rootWsdlUri
   *          The root {@link URI} of the wsdl.
   * @param targetPackage
   *          The target package in which all JaxWs artifacts should be stored.
   * @return A {@link Map} holding a {@link Path} for each created binding and a {@link StringBuilder} with the
   *         corresponding content.
   */
  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected Map<String, StringBuilder> getJaxwsBindingContents(ParsedWsdl parsedWsdl, URI rootWsdlUri, String targetPackage, IEnvironment env) {
    Map<URI, Set<JaxWsBindingMapping>> bindingsByFile = new HashMap<>();
    for (Entry<Service, URI> service : parsedWsdl.getWebServices().entrySet()) {
      WebServiceNames names = parsedWsdl.getServiceNames().get(service.getKey());
      bindingsByFile
          .computeIfAbsent(service.getValue(), k -> new HashSet<>())
          .add(new JaxWsBindingMapping(false, names.getWebServiceNameFromWsdl(), names.getWebServiceClassName()));

      Set<PortType> portTypesByService = parsedWsdl.getPortTypes(service.getKey());
      for (PortType portType : portTypesByService) {
        URI uriOfPortType = parsedWsdl.getPortTypes().get(portType);
        String portTypeName = portType.getQName().getLocalPart();
        bindingsByFile
            .computeIfAbsent(uriOfPortType, k -> new HashSet<>())
            .add(new JaxWsBindingMapping(true, portTypeName, WebServiceNames.getPortTypeClassName(portTypeName)));
      }
    }

    Map<String, StringBuilder> result = new HashMap<>(bindingsByFile.size());
    for (Entry<URI, Set<JaxWsBindingMapping>> binding : bindingsByFile.entrySet()) {
      URI uri = binding.getKey();
      URI relPath = CoreUtils.relativizeURI(rootWsdlUri, uri);
      Path path;

      if ("file".equals(uri.getScheme())) {
        path = Paths.get(uri);
      }
      else {
        path = Paths.get(uri.getPath());
      }
      if (path.getNameCount() < 1) {
        SdkLog.warning("Zero length path found for jax-ws binding content of URI '{}'. Skipping.", uri);
      }
      else {
        JaxwsBindingGenerator generator = new JaxwsBindingGenerator()
            .withNames(binding.getValue())
            .withWsdlLocation(relPath)
            .withWsPackage(targetPackage);
        StringBuilder jaxwsBindingContent = env.createResource(generator, getSourceFolder());
        result.put(path.getFileName().toString(), jaxwsBindingContent);
      }
    }
    return result;
  }

  protected static Path createJaxbBinding(Path wsdlBindingsFolder, IEnvironment env, IProgress progress) {
    Path jaxbBindingXmlFile = wsdlBindingsFolder.resolve(JaxWsUtils.JAXB_BINDINGS_FILE_NAME);
    env.writeResource(new JaxbBindingGenerator(), jaxbBindingXmlFile, progress);
    return jaxbBindingXmlFile;
  }

  protected String getWsdlFileName() {
    String wsdlFileName;
    URL wsdlUrl = getWsdlUrl();
    if (wsdlUrl != null) {
      wsdlFileName = wsdlUrl.getPath();
      //noinspection HardcodedFileSeparator
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
    wsdlFileName += JaxWsUtils.WSDL_FILE_EXTENSION;
    return wsdlFileName;
  }

  protected Path writeWsdlToProject(String baseName, IEnvironment env, IProgress progress) {
    Path wsdlFolder = getWsdlFolder(baseName);
    Path wsdlFile = wsdlFolder.resolve(getWsdlFileName());
    env.writeResource(getWsdlContent(), wsdlFile, progress);
    return wsdlFile;
  }

  protected static StringBuilder readXmlFromUrl(URL url) {
    try {
      return Xml.documentToString(Xml.get(url), false);
    }
    catch (IOException | TransformerException e) {
      throw new SdkException(e);
    }
  }

  public static Path getBindingRootFolder(Path jaxWsProject) {
    return getWebInfFolder(jaxWsProject).resolve(JaxWsUtils.BINDING_FOLDER_NAME);
  }

  public static Path getWsdlRootFolder(Path jaxWsProject) {
    return getWebInfFolder(jaxWsProject).resolve(JaxWsUtils.WSDL_FOLDER_NAME);
  }

  public static Path getWebInfFolder(Path jaxWsProject) {
    return jaxWsProject.resolve(JaxWsUtils.MODULE_REL_WEBINF_FOLDER_PATH);
  }

  public boolean isCreateNewModule() {
    return m_isCreateNewModule;
  }

  public void setCreateNewModule(boolean isCreateNewModule) {
    m_isCreateNewModule = isCreateNewModule;
  }

  public URL getWsdlUrl() {
    return m_wsdlUrl;
  }

  public void setWsdlUrl(URL wsdlUrl) {
    m_wsdlUrl = wsdlUrl;
  }

  public Path getCreatedWsdlFile() {
    return m_createdWsdlFile;
  }

  protected void setCreatedWsdlFile(Path createdWsdlFile) {
    m_createdWsdlFile = createdWsdlFile;
  }

  public Path getCreatedJaxbBindingFile() {
    return m_createdJaxbBindingFile;
  }

  protected void setCreatedJaxbBindingFile(Path createdJaxbBindingFile) {
    m_createdJaxbBindingFile = createdJaxbBindingFile;
  }

  public List<Path> getCreatedJaxwsBindingFiles() {
    return unmodifiableList(m_createdJaxwsBindingFiles);
  }

  public StringBuilder getWsdlContent() {
    return m_wsdlContent;
  }

  protected void setWsdlContent(StringBuilder wsdlContent) {
    m_wsdlContent = wsdlContent;
  }

  public String getPackage() {
    return m_package;
  }

  public void setPackage(String targetPackage) {
    m_package = targetPackage;
  }

  public List<IType> getCreatedWebServiceClients() {
    return unmodifiableList(m_createdWebServiceClients);
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
    return unmodifiableList(m_createdEntryPointDefinitions);
  }

  public List<IType> getCreatedProviderServiceImpls() {
    return unmodifiableList(m_createdProviderServiceImpls);
  }

  public List<String> getCreatedUrlProperties() {
    return unmodifiableList(m_createdUrlProperties);
  }

  public ParsedWsdl getParsedWsdl() {
    return m_parsedWsdl;
  }

  protected void setParsedWsdl(ParsedWsdl parsedWsdl) {
    m_parsedWsdl = parsedWsdl;
  }

  public Path getProjectRoot() {
    return m_projectRoot;
  }

  public void setProjectRoot(Path projectRoot) {
    m_projectRoot = projectRoot;
  }

  public IClasspathEntry getSourceFolder() {
    return m_sourceFolder;
  }

  public void setSourceFolder(IClasspathEntry sourceFolder) {
    m_sourceFolder = sourceFolder;
  }

  @Override
  public String toString() {
    return "Create new Web Service";
  }
}
