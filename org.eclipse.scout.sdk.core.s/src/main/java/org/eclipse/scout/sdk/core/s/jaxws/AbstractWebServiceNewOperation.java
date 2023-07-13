/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.jaxws;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.core.util.Strings.removeSuffix;

import java.io.IOException;
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
import java.util.Set;
import java.util.function.BiConsumer;

import javax.wsdl.WSDLException;
import javax.xml.transform.TransformerException;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutVariousApi;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils.JaxWsBindingMapping;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl.WebServiceNames;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;

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

  private CharSequence m_wsdlContent;
  private ParsedWsdl m_parsedWsdl;

  protected AbstractWebServiceNewOperation() {
    var capacity = 2;
    m_createdWebServiceClients = new ArrayList<>(capacity);
    m_createdEntryPointDefinitions = new ArrayList<>(capacity);
    m_createdProviderServiceImpls = new ArrayList<>(capacity);
    m_createdUrlProperties = new ArrayList<>(capacity);
    m_createdJaxwsBindingFiles = new ArrayList<>(capacity);
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

    progress.init(100, toString());

    // create new java project
    if (isCreateNewModule()) {
      setProjectRoot(createNewJaxWsModule(env, progress.newChild(39)));
      var primarySrcFolderOfNewModule = env.findJavaEnvironment(getProjectRoot())
          .flatMap(IJavaEnvironment::primarySourceFolder)
          .orElseThrow(() -> newFail("Unable to find java environment for newly created jaxws project."));
      setSourceFolder(primarySrcFolderOfNewModule);
    }
    progress.setWorkRemaining(61);
    var scoutApi = getSourceFolder().javaEnvironment().requireApi(IScoutApi.class);

    // read wsdl data from remote
    var wsdlBaseName = getWsdlBaseName();
    if (isCreateEmptyWsdl()) {
      var generator = new EmptyWsdlGenerator()
          .withName(wsdlBaseName)
          .withPackage(getPackage());
      setWsdlContent(env.executeGenerator(generator, getSourceFolder()));
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

    // download all resources that belong the WSDL into the project and reparse the created WSDL with the local paths
    var bindingFolder = getBindingFolder(getProjectRoot(), wsdlBaseName);
    setCreatedWsdlFile(writeWsdlToProject(wsdlBaseName, env, progress.newChild(2)));
    copyReferencedResources(wsdlBaseName, env, progress.newChild(1));
    setParsedWsdl(parseWsdl(getTargetWsdlFileUri(wsdlBaseName))); // reparse the WSDL using local URI

    // create bindings, add section to pom
    setCreatedJaxbBindingFile(createJaxbBinding(bindingFolder, env, progress.newChild(1)));
    createJaxwsBindings(bindingFolder, env, progress.newChild(2));
    var wsdlFolderRelativePath = getWsdlRootFolder(getProjectRoot()).relativize(getCreatedWsdlFile());
    addWsdlToPom(wsdlFolderRelativePath, bindingFolder.getFileName().toString(), env, progress.newChild(2), scoutApi);

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
    return JaxWsUtils.removeCommonSuffixes(removeSuffix(getWsdlFileName(), JaxWsUtils.WSDL_FILE_SUFFIX));
  }

  protected abstract void createDerivedResources(IEnvironment env, IProgress progress);

  protected abstract Path createNewJaxWsModule(IEnvironment env, IProgress progress);

  protected void createProviderServiceImplementations(IEnvironment env, IProgress progress) {
    for (var service : getParsedWsdl().getServiceNames().entrySet()) {
      var portTypes = getParsedWsdl().getPortTypes(service.getKey());
      for (var pt : portTypes) {
        var portTypeName = pt.getQName().getLocalPart();
        var wspsb = new WebServiceProviderGenerator<>()
            .withElementName(WebServiceNames.getWebServiceProviderImplClassName(portTypeName))
            .withPackageName(getPackage())
            .withPortType(getPackage() + JavaTypes.C_DOT + WebServiceNames.getPortTypeClassName(portTypeName));
        m_createdProviderServiceImpls.add(env.writeCompilationUnit(wspsb, getSourceFolder(), progress));
      }
    }
  }

  protected void createEntryPointDefinitions(IEnvironment env, IProgress progress) {
    for (var service : getParsedWsdl().getServiceNames().entrySet()) {
      var names = service.getValue();
      var portTypes = getParsedWsdl().getPortTypes(service.getKey());
      for (var pt : portTypes) {
        var portTypeName = pt.getQName().getLocalPart();
        var epdsb = new EntryPointDefinitionGenerator<>()
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
    for (var service : getParsedWsdl().getServiceNames().entrySet()) {
      var names = service.getValue();
      var portTypes = getParsedWsdl().getPortTypes(service.getKey());
      for (var pt : portTypes) {
        var portTypeName = pt.getQName().getLocalPart();
        var wscsb = new WebServiceClientGenerator<>()
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
    for (var resource : getParsedWsdl().getReferencedResources().entrySet()) {
      var sourceUri = resource.getKey();
      if (resource.getValue().equals(resource.getKey().toString())) {
        // the rel path is also absolute (don't download absolutely referenced files)
        continue;
      }

      var relPath = resource.getValue();
      var wsdlFolder = getWsdlFolder(baseName);
      var target = wsdlFolder.resolve(relPath);
      try {
        @SuppressWarnings("squid:S1149") // replace StringBuffer with StringBuilder
        var content = readXmlFromUrl(sourceUri.toURL());
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
    catch (WSDLException e) {
      throw new SdkException(e);
    }
  }

  protected URI getWsdlBaseUri(String baseName) {
    var wsdlUrl = getWsdlUrl();
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

  protected static Path getBindingFolder(Path jaxWsProject, String wsBaseName) {
    var bindingFolderName = wsBaseName.toLowerCase(Locale.US);
    return getBindingRootFolder(jaxWsProject).resolve(bindingFolderName);
  }

  protected URI getTargetWsdlFileUri(String baseName) {
    var wsdlFolder = getWsdlFolder(baseName);
    var wsdlFile = wsdlFolder.resolve(getWsdlFileName());
    return wsdlFile.toUri();
  }

  @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected void addWsdlToPom(Path wsdlFolderRelativePath, String bindingFolderName, IEnvironment env, IProgress progress, IScoutVariousApi api) {
    var pom = getProjectRoot().resolve(IMavenConstants.POM);
    if (!Files.isReadable(pom) || !Files.isRegularFile(pom)) {
      return;
    }
    Collection<String> bindingFileNames = getCreatedJaxwsBindingFiles().stream()
        .map(createdJaxWsBinding -> createdJaxWsBinding.getFileName().toString())
        .collect(toList());
    bindingFileNames.add(getCreatedJaxbBindingFile().getFileName().toString());

    try {
      var document = Xml.get(pom);
      JaxWsUtils.addWsdlToPom(document, wsdlFolderRelativePath.toString().replace('\\', '/'), bindingFolderName, bindingFileNames, api);
      env.writeResource(Xml.writeDocument(document, true), pom, progress);
    }
    catch (IOException | TransformerException e) {
      throw new SdkException(e);
    }
  }

  protected Path getWsdlFolder(String wsBaseName) {
    var wsdlFolderName = wsBaseName.toLowerCase(Locale.US);
    return getWsdlRootFolder(getProjectRoot()).resolve(wsdlFolderName);
  }

  protected void createJaxwsBindings(Path wsdlBindingsFolder, IEnvironment env, IProgress progress) {
    var parent = wsdlBindingsFolder.toUri();
    var jaxwsBindingContents = getJaxwsBindingContents(getParsedWsdl(), parent, getPackage(), env);
    for (var binding : jaxwsBindingContents.entrySet()) {
      Path jaxwsBindingXmlFile;
      if (jaxwsBindingContents.size() == 1) {
        jaxwsBindingXmlFile = wsdlBindingsFolder.resolve(JaxWsUtils.JAXWS_BINDINGS_FILE_NAME);
      }
      else {
        var pathFileName = binding.getKey();
        if (pathFileName == null) {
          // should not happen because zero len paths are skipped by JaxWsUtils.getJaxwsBindingContents().
          throw new IllegalArgumentException("zero length path found.");
        }

        var partName = Strings.removeSuffix(pathFileName.toLowerCase(Locale.US), JaxWsUtils.WSDL_FILE_SUFFIX);
        var lastDotPos = JaxWsUtils.JAXWS_BINDINGS_FILE_NAME.lastIndexOf('.');
        var fileName = new StringBuilder();
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
    for (var service : parsedWsdl.getWebServices().entrySet()) {
      var names = parsedWsdl.getServiceNames().get(service.getKey());
      bindingsByFile
          .computeIfAbsent(service.getValue(), k -> new HashSet<>())
          .add(new JaxWsBindingMapping(false, names.getWebServiceNameFromWsdl(), names.getWebServiceClassName()));

      var portTypesByService = parsedWsdl.getPortTypes(service.getKey());
      for (var portType : portTypesByService) {
        var uriOfPortType = parsedWsdl.getPortTypes().get(portType);
        var portTypeName = portType.getQName().getLocalPart();
        bindingsByFile
            .computeIfAbsent(uriOfPortType, k -> new HashSet<>())
            .add(new JaxWsBindingMapping(true, portTypeName, WebServiceNames.getPortTypeClassName(portTypeName)));
      }
    }

    Map<String, StringBuilder> result = new HashMap<>(bindingsByFile.size());
    for (var binding : bindingsByFile.entrySet()) {
      var uri = binding.getKey();
      var relPath = CoreUtils.relativizeURI(rootWsdlUri, uri);
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
        var generator = new JaxwsBindingGenerator()
            .withNames(binding.getValue())
            .withWsdlLocation(relPath)
            .withWsPackage(targetPackage);
        var jaxwsBindingContent = env.executeGenerator(generator, getSourceFolder());
        result.put(path.getFileName().toString(), jaxwsBindingContent);
      }
    }
    return result;
  }

  protected static Path createJaxbBinding(Path wsdlBindingsFolder, IEnvironment env, IProgress progress) {
    var jaxbBindingXmlFile = wsdlBindingsFolder.resolve(JaxWsUtils.JAXB_BINDINGS_FILE_NAME);
    env.writeResource(new JaxbBindingGenerator(), jaxbBindingXmlFile, progress);
    return jaxbBindingXmlFile;
  }

  protected String getWsdlFileName() {
    String wsdlFileName;
    var wsdlUrl = getWsdlUrl();
    if (wsdlUrl != null) {
      wsdlFileName = wsdlUrl.getPath();
      var lastSlashPos = wsdlFileName.lastIndexOf('/');
      var lastDotPos = wsdlFileName.lastIndexOf('.');
      if (lastDotPos < lastSlashPos) {
        lastDotPos = wsdlFileName.length();
      }
      wsdlFileName = wsdlFileName.substring(lastSlashPos + 1, lastDotPos);
    }
    else {
      wsdlFileName = Strings.removeSuffix(getWsdlName(), ISdkConstants.SUFFIX_WS_PROVIDER);
    }
    wsdlFileName += JaxWsUtils.WSDL_FILE_SUFFIX;
    return wsdlFileName;
  }

  protected Path writeWsdlToProject(String baseName, IEnvironment env, IProgress progress) {
    var wsdlFolder = getWsdlFolder(baseName);
    var wsdlFile = wsdlFolder.resolve(getWsdlFileName());
    env.writeResource(getWsdlContent(), wsdlFile, progress);
    return wsdlFile;
  }

  @SuppressWarnings("squid:S1149") // replace StringBuffer with StringBuilder
  protected static StringBuffer readXmlFromUrl(URL url) {
    try {
      return Xml.writeDocument(Xml.get(url), false);
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
    return jaxWsProject.resolve(JaxWsUtils.MODULE_REL_WEB_INF_FOLDER_PATH);
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

  public CharSequence getWsdlContent() {
    return m_wsdlContent;
  }

  protected void setWsdlContent(CharSequence wsdlContent) {
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
