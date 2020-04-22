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
package org.eclipse.scout.sdk.s2e.ui.internal.jaxws;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.wsdl.WSDLException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.IScoutSourceFolders;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.jaxws.AbstractWebServiceNewOperation;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.s.util.maven.Pom;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.environment.AbstractJob;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.PackageContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.resource.ResourceTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizardPage;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <h3>{@link WebServiceNewWizardPage}</h3>
 *
 * @since 5.2.0
 */
public class WebServiceNewWizardPage extends AbstractWizardPage {

  public static final String PROP_WEB_SERVICE_TYPE = "webServiceType";
  public static final String PROP_CONSUMER_WSDL_URL = "consumerWsdlUrl";
  public static final String PROP_PROVIDER_WSDL_URL = "providerWsdlUrl";
  public static final String PROP_PROVIDER_WSDL_NAME = "providerWsdlName";
  public static final String PROP_IS_CREATE_NEW_PROJECT = "isCreateNewProject";
  public static final String PROP_TARGET_PACKAGE = "targetPackage";
  public static final String PROP_EXISTING_JAXWS_PROJECT = "jaxwsProject";
  public static final String PROP_SERVER_PROJECT = "serverProject";
  public static final String PROP_ARTIFACT_ID = "artifactId";

  public enum WebServiceType {
    CONSUMER_FROM_EXISTING_WSDL,
    PROVIDER_FROM_EXISTING_WSDL,
    PROVIDER_FROM_EMPTY_WSDL
  }

  private ResourceTextField m_consumerWsdlUrlField;
  private ResourceTextField m_providerWsdlUrlField;
  private ProposalTextField m_existingJaxWsProjectField;
  private ProposalTextField m_packageField;
  private ProposalTextField m_serverProjectField;
  private StyledTextField m_artifactIdField;
  private StyledTextField m_providerWsdlNameField;
  private Button m_createConsumer;
  private Button m_createProviderFromExistingWsdl;
  private Button m_createProviderFromEmptyWsdl;
  private Button m_createNewProjectButton;
  private Button m_addToExistingProjectButton;

  private URL m_lastParsedWsdlUrl;
  private IStatus m_lastParsedWsdlUrlStatus;
  private boolean m_packageChanged;

  public WebServiceNewWizardPage() {
    super(WebServiceNewWizardPage.class.getName());
    setTitle("Create a new Web Service");
    setDescription(getTitle());
    initDefaults();
  }

  protected void initDefaults() {
    setPackageChanged(false);
    setIsCreateNewProjectInternal(true);
    setWebServiceTypeInternal(WebServiceType.CONSUMER_FROM_EXISTING_WSDL);
  }

  @Override
  protected void createContent(Composite parent) {
    GridLayoutFactory
        .swtDefaults()
        .applyTo(parent);

    int labelWidth = 130;
    createTypeGroup(parent, labelWidth);
    createProjectGroup(parent, labelWidth);
    createAttributesGroup(parent, labelWidth);
    setViewState();

    m_consumerWsdlUrlField.setFocus();

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_WEB_SERVICE_NEW_WIZARD_PAGE);
  }

  protected void createTypeGroup(Composite parent, int labelWidth) {
    Group typeGroupBox = FieldToolkit.createGroupBox(parent, "Type of Web Service");

    // radio button "new consumer"
    m_createConsumer = new Button(typeGroupBox, SWT.RADIO);
    m_createConsumer.setText("Create new Web Service Consumer");
    m_createConsumer.setSelection(WebServiceType.CONSUMER_FROM_EXISTING_WSDL == getWebServiceType());
    m_createConsumer.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setWebServiceTypeInternal(WebServiceType.CONSUMER_FROM_EXISTING_WSDL);
        setViewState();
        pingStateChanging();
      }
    });

    // Consumer WSDL
    m_consumerWsdlUrlField = FieldToolkit.createResourceField(typeGroupBox, "WSDL URL", TextField.TYPE_LABEL, labelWidth);
    m_consumerWsdlUrlField.setText(getConsumerWsdlUrl());
    m_consumerWsdlUrlField.addResourceChangedListener((newUrl, newFile) -> {
      String url = null;
      if (newUrl != null) {
        url = newUrl.toExternalForm();
      }
      setConsumerWsdlUrlInternal(url);
      pingStateChanging();
    });

    // radio button "new provider from wsdl"
    m_createProviderFromExistingWsdl = new Button(typeGroupBox, SWT.RADIO);
    m_createProviderFromExistingWsdl.setText("Create new Web Service Provider from existing WSDL");
    m_createProviderFromExistingWsdl.setSelection(WebServiceType.PROVIDER_FROM_EXISTING_WSDL == getWebServiceType());
    m_createProviderFromExistingWsdl.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setWebServiceTypeInternal(WebServiceType.PROVIDER_FROM_EXISTING_WSDL);
        setViewState();
        pingStateChanging();
      }
    });

    // Provider WSDL
    m_providerWsdlUrlField = FieldToolkit.createResourceField(typeGroupBox, "WSDL URL", TextField.TYPE_LABEL, labelWidth);
    m_providerWsdlUrlField.setText(getProviderWsdlUrl());
    m_providerWsdlUrlField.addResourceChangedListener((newUrl, newFile) -> {
      String url = null;
      if (newUrl != null) {
        url = newUrl.toExternalForm();
      }
      setProviderWsdlUrlInternal(url);
      pingStateChanging();
    });

    // radio button "new provider from empty wsdl"
    m_createProviderFromEmptyWsdl = new Button(typeGroupBox, SWT.RADIO);
    m_createProviderFromEmptyWsdl.setText("Create new Web Service Provider with empty WSDL");
    m_createProviderFromEmptyWsdl.setSelection(WebServiceType.PROVIDER_FROM_EMPTY_WSDL == getWebServiceType());
    m_createProviderFromEmptyWsdl.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setWebServiceTypeInternal(WebServiceType.PROVIDER_FROM_EMPTY_WSDL);
        setViewState();
        pingStateChanging();
      }
    });

    // Provider WSDL Name
    m_providerWsdlNameField = FieldToolkit.createStyledTextField(typeGroupBox, "Web Service Name", TextField.TYPE_LABEL, labelWidth);
    m_providerWsdlNameField.setText(getWsdlName());
    m_providerWsdlNameField.setReadOnlySuffix(ISdkProperties.SUFFIX_WS_PROVIDER);
    m_providerWsdlNameField.addModifyListener(e -> {
      setWsdlNameInternal(m_providerWsdlNameField.getText());
      pingStateChanging();
    });

    // layout
    GridLayoutFactory
        .swtDefaults()
        .applyTo(typeGroupBox);
    GridDataFactory
        .defaultsFor(typeGroupBox)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(typeGroupBox);
    GridDataFactory
        .defaultsFor(m_createConsumer)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_createConsumer);
    GridDataFactory
        .defaultsFor(m_consumerWsdlUrlField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_consumerWsdlUrlField);
    GridDataFactory
        .defaultsFor(m_createProviderFromExistingWsdl)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_createProviderFromExistingWsdl);
    GridDataFactory
        .defaultsFor(m_providerWsdlUrlField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_providerWsdlUrlField);
    GridDataFactory
        .defaultsFor(m_createProviderFromEmptyWsdl)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_createProviderFromEmptyWsdl);
    GridDataFactory
        .defaultsFor(m_providerWsdlNameField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_providerWsdlNameField);
  }

  protected void createProjectGroup(Composite parent, int labelWidth) {
    // project group box
    Group projectGroupBox = FieldToolkit.createGroupBox(parent, "Target Project");

    // radio button "create new project"
    m_createNewProjectButton = new Button(projectGroupBox, SWT.RADIO);
    m_createNewProjectButton.setText("Create new project for this Web Service");
    m_createNewProjectButton.setSelection(isCreateNewProject());
    m_createNewProjectButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setIsCreateNewProjectInternal(true);
        setViewState();
        pingStateChanging();
      }
    });

    // existing server project to add the web service dependency
    Predicate<IJavaProject> serverProjectsFilter = WebServiceNewWizardPage::isServerProject;
    m_serverProjectField = FieldToolkit.createProjectProposalField(projectGroupBox, "Add new Project to", serverProjectsFilter, labelWidth);
    m_serverProjectField.acceptProposal(getServerProject());
    m_serverProjectField.addProposalListener(proposal -> {
      IJavaProject jp = (IJavaProject) proposal;
      setServerProjectInternal(jp);
      pingStateChanging();
    });

    // artifact id
    m_artifactIdField = FieldToolkit.createStyledTextField(projectGroupBox, "Artifact Id", TextField.TYPE_LABEL, labelWidth);
    m_artifactIdField.setText(getArtifactId());
    m_artifactIdField.addModifyListener(e -> {
      setArtifactIdInternal(m_artifactIdField.getText());
      pingStateChanging();
    });

    // radio button "existing project"
    m_addToExistingProjectButton = new Button(projectGroupBox, SWT.RADIO);
    m_addToExistingProjectButton.setText("Add Web Service to an existing Project");
    m_addToExistingProjectButton.setSelection(!isCreateNewProject());
    m_addToExistingProjectButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setIsCreateNewProjectInternal(false);
        setViewState();
        pingStateChanging();
      }
    });

    // existing jaxws project
    Predicate<IJavaProject> jaxwsProjectsFilter = WebServiceNewWizardPage::isJaxWsProject;
    m_existingJaxWsProjectField = FieldToolkit.createProjectProposalField(projectGroupBox, "Web Service Project", jaxwsProjectsFilter, labelWidth);
    m_existingJaxWsProjectField.acceptProposal(getExistingJaxWsProject());
    m_existingJaxWsProjectField.addProposalListener(proposal -> {
      IJavaProject javaProject = (IJavaProject) proposal;
      setExistingJaxWsProjectInternal(javaProject);
      pingStateChanging();
    });

    // layout
    GridLayoutFactory
        .swtDefaults()
        .applyTo(projectGroupBox);
    GridDataFactory
        .defaultsFor(projectGroupBox)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(projectGroupBox);
    GridDataFactory
        .defaultsFor(m_createNewProjectButton)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_createNewProjectButton);
    GridDataFactory
        .defaultsFor(m_serverProjectField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_serverProjectField);
    GridDataFactory
        .defaultsFor(m_artifactIdField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_artifactIdField);
    GridDataFactory
        .defaultsFor(m_addToExistingProjectButton)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_addToExistingProjectButton);
    GridDataFactory
        .defaultsFor(m_existingJaxWsProjectField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_existingJaxWsProjectField);
  }

  protected void createAttributesGroup(Composite parent, int labelWidth) {
    // project group box
    Group attributesGroupBox = FieldToolkit.createGroupBox(parent, "Web Service Artifacts Package");

    // package field
    m_packageField = FieldToolkit.createPackageField(attributesGroupBox, "Target Package", getExistingJaxWsProject(), labelWidth);
    m_packageField.setText(getTargetPackage());
    m_packageField.addModifyListener(e -> {
      if (!isPackageChanged() && !isStateChanging()) {
        setPackageChanged(true); // mark as changed if it is manually changed by the user
      }
      setTargetPackageInternal(m_packageField.getText());
      pingStateChanging();
    });

    // layout
    GridLayoutFactory
        .swtDefaults()
        .applyTo(attributesGroupBox);
    GridDataFactory
        .defaultsFor(attributesGroupBox)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(attributesGroupBox);
    GridDataFactory
        .defaultsFor(m_packageField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_packageField);
  }

  protected void guessArtifactId() {
    if (Strings.hasText(getArtifactId())) {
      return; // already an artifact id defined.
    }
    if (!isCreateNewProject()) {
      return;
    }
    if (!JdtUtils.exists(getServerProject())) {
      return;
    }
    setArtifactId(getServerProject().getElementName() + ".ws");
  }

  @SuppressWarnings("pmd:NPathComplexity")
  protected void guessPackage() {
    if (isPackageChanged()) {
      return; // already changed manually. don't update
    }
    Optional<String> groupId = Optional.empty();
    Optional<String> artifactId = Optional.empty();
    if (isCreateNewProject()) {
      if (Strings.hasText(getArtifactId()) && JdtUtils.exists(getServerProject())) {
        Document pom = getPomDocument(getServerProject());
        groupId = Pom.groupId(pom);
        artifactId = Optional.ofNullable(getArtifactId());
      }
    }
    else if (JdtUtils.exists(getExistingJaxWsProject())) {
      Document pom = getPomDocument(getExistingJaxWsProject());
      groupId = Pom.groupId(pom);
      artifactId = Pom.artifactId(pom);
    }

    if (!artifactId.isPresent()) {
      return;
    }

    String baseName;
    if (WebServiceType.PROVIDER_FROM_EMPTY_WSDL == getWebServiceType()) {
      baseName = getWsdlName();
      if (Strings.hasText(baseName)) {
        baseName = JaxWsUtils.removeCommonSuffixes(baseName.toLowerCase(Locale.ENGLISH));
      }
    }
    else if (WebServiceType.PROVIDER_FROM_EXISTING_WSDL == getWebServiceType()) {
      baseName = getWebServiceNameFromUrl(getProviderWsdlUrl());
    }
    else {
      baseName = getWebServiceNameFromUrl(getConsumerWsdlUrl());
    }

    if (Strings.isBlank(baseName)) {
      return;
    }

    StringBuilder pckBuilder = new StringBuilder();
    if (groupId.isPresent() && !artifactId.get().startsWith(groupId.get())) {
      pckBuilder.append(groupId.get());
      pckBuilder.append(JavaTypes.C_DOT);
    }
    pckBuilder.append(artifactId.get());
    pckBuilder.append(JavaTypes.C_DOT);
    pckBuilder.append(baseName);
    setTargetPackage(pckBuilder.toString());
  }

  protected static String getWebServiceNameFromUrl(String url) {
    if (Strings.isBlank(url)) {
      return null;
    }
    try {
      String path = new URL(url).getPath();
      if (Strings.hasText(path)) {
        int lastSlashPos = path.lastIndexOf('/');
        if (lastSlashPos >= 0) {
          path = path.substring(lastSlashPos + 1);
        }
        path = path.toLowerCase(Locale.ENGLISH);

        int lastDotPos = path.lastIndexOf('.');
        if (lastDotPos > 0) {
          path = path.substring(0, lastDotPos);
        }
        return JaxWsUtils.removeCommonSuffixes(path);
      }
    }
    catch (Exception e) {
      SdkLog.debug("Invalid URL passed.", e);
    }
    return null;
  }

  protected static Document getPomDocument(IJavaProject project) {
    try {
      return S2eUtils.getPomDocument(project.getProject());
    }
    catch (RuntimeException e) {
      SdkLog.debug("Unable to load pom of project '{}'.", project.getElementName(), e);
      return null;
    }
  }

  protected static boolean isServerProject(IJavaProject jp) {
    try {
      return JdtUtils.exists(jp.findType(IScoutRuntimeTypes.IServerSession))
          && !jp.getProject().getFolder(IScoutSourceFolders.WEBAPP_RESOURCE_FOLDER + "/WEB-INF").exists()
          && !Files.exists(AbstractWebServiceNewOperation.getWsdlRootFolder(jp.getProject().getLocation().toFile().toPath()));
    }
    catch (JavaModelException e) {
      SdkLog.warning("Cannot check type of project '{}'. This project will be ignored.", jp.getElementName(), e);
      return false;
    }
  }

  protected static boolean isJaxWsProject(IJavaProject jp) {
    try {
      IProject project = jp.getProject();
      Path projectPath = project.getLocation().toFile().toPath();
      Path wsdlFolder = AbstractWebServiceNewOperation.getWsdlRootFolder(projectPath);
      if (!Files.isDirectory(wsdlFolder)) {
        return false;
      }
      Path bindingFolder = AbstractWebServiceNewOperation.getBindingRootFolder(projectPath);
      if (!Files.isDirectory(bindingFolder)) {
        return false;
      }

      Optional<IPackageFragmentRoot> primarySourceFolderOpt = S2eUtils.primarySourceFolder(jp);
      if (!primarySourceFolderOpt.isPresent()) {
        return false;
      }

      IPackageFragmentRoot primarySourceFolder = primarySourceFolderOpt.get();
      if (!JdtUtils.exists(primarySourceFolder) || !primarySourceFolder.getResource().getProjectRelativePath().toString().toLowerCase(Locale.ENGLISH).contains("java")) {
        return false;
      }
      if (!JdtUtils.exists(jp.findType(IScoutRuntimeTypes.AbstractWebServiceClient))) {
        return false;
      }

      String prefix = "p";
      String p = prefix + ':';
      StringBuilder bindingFilesXpathBuilder = new StringBuilder();
      bindingFilesXpathBuilder.append(p).append(IMavenConstants.PROJECT).append('/').append(p).append(IMavenConstants.BUILD).append('/').append(p).append(IMavenConstants.PLUGINS).append('/').append(p).append(IMavenConstants.PLUGIN)
          .append("[./").append(p).append(IMavenConstants.GROUP_ID).append("='").append(JaxWsUtils.JAXWS_MAVEN_PLUGIN_GROUP_ID).append("' and ./").append(p).append(IMavenConstants.ARTIFACT_ID).append("='")
          .append(JaxWsUtils.JAXWS_MAVEN_PLUGIN_ARTIFACT_ID).append("']");
      List<Element> elements = Xml.evaluateXPath(bindingFilesXpathBuilder.toString(), S2eUtils.getPomDocument(project), prefix, IMavenConstants.POM_XML_NAMESPACE);
      //noinspection RedundantIfStatement
      if (elements.isEmpty() && containsWsdls(wsdlFolder)) {
        // these are jaxws project that contain wsdls but they are not listed in the pom (auto discovery).
        // we do not support these because when adding a new wsdl to the pom, the existing ones will be ignored.
        return false;
      }
      return true;
    }
    catch (JavaModelException | XPathExpressionException e) {
      SdkLog.warning("Cannot check type of project '{}'. This project will be ignored.", jp.getElementName(), e);
      return false;
    }
  }

  protected static boolean containsWsdls(Path wsdlFolder) {
    try (Stream<Path> paths = Files.walk(wsdlFolder)) {
      return paths
          .filter(p -> p.getFileName().toString().endsWith(JaxWsUtils.WSDL_FILE_EXTENSION))
          .filter(Files::isRegularFile)
          .anyMatch(Files::isReadable);
    }
    catch (IOException e) {
      SdkLog.warning("Unable to search WSDL files in folder '{}'.", wsdlFolder, e);
      return false;
    }
  }

  protected void setViewState() {
    m_artifactIdField.setEnabled(isCreateNewProject());
    m_serverProjectField.setEnabled(isCreateNewProject());
    m_existingJaxWsProjectField.setEnabled(!isCreateNewProject());

    m_consumerWsdlUrlField.setEnabled(WebServiceType.CONSUMER_FROM_EXISTING_WSDL == getWebServiceType());
    m_providerWsdlUrlField.setEnabled(WebServiceType.PROVIDER_FROM_EXISTING_WSDL == getWebServiceType());
    m_providerWsdlNameField.setEnabled(WebServiceType.PROVIDER_FROM_EMPTY_WSDL == getWebServiceType());
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    super.validatePage(multiStatus);
    multiStatus.add(getStatusWsdlUrl());
    multiStatus.add(getStatusWsdlName());
    multiStatus.add(getStatusServerProject());
    multiStatus.add(getStatusArtifactId());
    multiStatus.add(getStatusExistingJaxWsProject());
    multiStatus.add(getStatusPackage());
  }

  protected IStatus getStatusPackage() {
    return CompilationUnitNewWizardPage.validatePackageName(getTargetPackage());
  }

  protected IStatus getStatusWsdlName() {
    if (WebServiceType.PROVIDER_FROM_EMPTY_WSDL != getWebServiceType()) {
      return Status.OK_STATUS;
    }
    return CompilationUnitNewWizardPage.validateJavaName(getWsdlName(), m_providerWsdlNameField.getReadOnlySuffix());
  }

  protected IStatus getStatusExistingJaxWsProject() {
    if (isCreateNewProject()) {
      return Status.OK_STATUS;
    }

    if (!JdtUtils.exists(getExistingJaxWsProject())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose the project in which the new Web Service should be created.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusServerProject() {
    if (!isCreateNewProject()) {
      return Status.OK_STATUS;
    }
    if (!JdtUtils.exists(getServerProject())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose for which server project a new Web Service project should be created.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusArtifactId() {
    if (!isCreateNewProject()) {
      return Status.OK_STATUS;
    }

    // check name pattern
    String msg = ScoutProjectNewHelper.getMavenArtifactIdErrorMessage(getArtifactId());
    if (msg != null) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, msg);
    }

    // check project existence in workspace
    for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      if (p.getName().equalsIgnoreCase(getArtifactId())) {
        return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, "A project with this Artifact Id already exists in the workspace.");
      }
    }

    // check for existence on the file system
    if (JdtUtils.exists(getServerProject())) {
      Path serverProjectFolder = getServerProject().getProject().getLocation().makeAbsolute().toFile().toPath();
      Path targetDir = serverProjectFolder.getParent();
      if (targetDir != null) {
        if (Files.isDirectory(targetDir.resolve(getArtifactId()))) {
          return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, "A project with this Artifact Id already exists in this target directory.");
        }
      }
      else {
        return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, "Invalid project location.");
      }
    }

    return Status.OK_STATUS;
  }

  protected IStatus getStatusWsdlUrl() {
    String wsdlUrl;
    ResourceTextField fieldToCheck;
    if (WebServiceType.CONSUMER_FROM_EXISTING_WSDL == getWebServiceType()) {
      fieldToCheck = m_consumerWsdlUrlField;
      wsdlUrl = getConsumerWsdlUrl();
    }
    else if (WebServiceType.PROVIDER_FROM_EXISTING_WSDL == getWebServiceType()) {
      fieldToCheck = m_providerWsdlUrlField;
      wsdlUrl = getProviderWsdlUrl();
    }
    else {
      return Status.OK_STATUS;
    }

    if (Strings.isBlank(wsdlUrl)) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please specify a WSDL URL.");
    }

    try {
      URL url = new URL(wsdlUrl);
      if (Objects.equals(m_lastParsedWsdlUrl, url)) {
        return m_lastParsedWsdlUrlStatus;
      }

      fieldToCheck.setEnabled(false);
      Job parseRemoteWsdl = new AbstractJob("parse WSDL") {
        @Override
        protected void execute(IProgressMonitor monitor) {
          try {
            String msg = validateWsdl(url);
            m_lastParsedWsdlUrl = url;
            if (msg == null) {
              m_lastParsedWsdlUrlStatus = Status.OK_STATUS;
            }
            else {
              m_lastParsedWsdlUrlStatus = new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, msg);
            }
          }
          finally {
            if (!fieldToCheck.isDisposed()) {
              fieldToCheck.getDisplay().asyncExec(() -> {
                if (!fieldToCheck.isDisposed()) {
                  revalidate();
                  fieldToCheck.setEnabled(true);
                }
              });
            }
          }
        }
      };
      parseRemoteWsdl.setSystem(true);
      parseRemoteWsdl.setUser(false);
      parseRemoteWsdl.schedule();
    }
    catch (MalformedURLException e) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The given WSDL URL is not valid.", e);
    }

    return Status.OK_STATUS;
  }

  protected static String validateWsdl(URL url) {
    try (InputStream in = url.openStream()) {
      ParsedWsdl info = ParsedWsdl.create(url.toURI(), in, false);
      if (info.isEmpty()) {
        return "Either this Web Service uses SOAP encoding (use=encoded) or contains no operations. Ensure the Web Service uses literal encoding.";
      }
    }
    catch (IOException | URISyntaxException | WSDLException e) {
      SdkLog.debug(e);
      return "The given WSDL cannot be parsed.";
    }
    return null;
  }

  public boolean isCreateNewProject() {
    return getPropertyBool(PROP_IS_CREATE_NEW_PROJECT);
  }

  public void setIsCreateNewProject(boolean createNewProject) {
    setPropertyWithChangingControl(m_createNewProjectButton, () -> setIsCreateNewProjectInternal(createNewProject), field -> {
      field.setSelection(createNewProject);
      m_addToExistingProjectButton.setSelection(!createNewProject);
    });
  }

  protected boolean setIsCreateNewProjectInternal(boolean createNewProject) {
    return setProperty(PROP_IS_CREATE_NEW_PROJECT, createNewProject);
  }

  public String getTargetPackage() {
    return getProperty(PROP_TARGET_PACKAGE, String.class);
  }

  public void setTargetPackage(String targetPackage) {
    setPropertyWithChangingControl(m_packageField, () -> setTargetPackageInternal(targetPackage), field -> field.setText(targetPackage));
  }

  protected boolean setTargetPackageInternal(String targetPackage) {
    return setProperty(PROP_TARGET_PACKAGE, targetPackage);
  }

  public IJavaProject getExistingJaxWsProject() {
    return getProperty(PROP_EXISTING_JAXWS_PROJECT, IJavaProject.class);
  }

  public void setExistingJaxWsProject(IJavaProject jp) {
    setPropertyWithChangingControl(m_existingJaxWsProjectField, () -> setExistingJaxWsProjectInternal(jp), field -> field.acceptProposal(jp));
  }

  protected boolean setExistingJaxWsProjectInternal(IJavaProject jp) {
    if (setProperty(PROP_EXISTING_JAXWS_PROJECT, jp)) {
      if (isControlCreated()) {
        ((PackageContentProvider) m_packageField.getContentProvider()).setJavaProject(jp);
      }
      guessPackage();
      return true;
    }
    return false;
  }

  public String getConsumerWsdlUrl() {
    return getProperty(PROP_CONSUMER_WSDL_URL, String.class);
  }

  public void setConsumerWsdlUrl(String wsdlUrl) {
    setPropertyWithChangingControl(m_consumerWsdlUrlField, () -> setConsumerWsdlUrlInternal(wsdlUrl), field -> field.setText(wsdlUrl));
  }

  protected boolean setConsumerWsdlUrlInternal(String wsdlUrl) {
    if (setProperty(PROP_CONSUMER_WSDL_URL, wsdlUrl)) {
      guessPackage();
      return true;
    }
    return false;
  }

  public String getArtifactId() {
    return getPropertyString(PROP_ARTIFACT_ID);
  }

  public void setArtifactId(String s) {
    setPropertyWithChangingControl(m_artifactIdField, () -> setArtifactIdInternal(s), field -> field.setText(s));
  }

  protected boolean setArtifactIdInternal(String s) {
    if (setPropertyString(PROP_ARTIFACT_ID, s)) {
      guessPackage();
      return true;
    }
    return false;
  }

  public IJavaProject getServerProject() {
    return getProperty(PROP_SERVER_PROJECT, IJavaProject.class);
  }

  public void setServerProject(IJavaProject jp) {
    setPropertyWithChangingControl(m_serverProjectField, () -> setServerProjectInternal(jp), field -> field.acceptProposal(jp));
  }

  protected boolean setServerProjectInternal(IJavaProject jp) {
    if (setProperty(PROP_SERVER_PROJECT, jp)) {
      guessArtifactId();
      guessPackage();
      return true;
    }
    return false;
  }

  public WebServiceType getWebServiceType() {
    return getProperty(PROP_WEB_SERVICE_TYPE, WebServiceType.class);
  }

  public void setWebServiceType(WebServiceType type) {
    setPropertyWithChangingControl(m_createConsumer, () -> setWebServiceTypeInternal(type), field -> {
      field.setSelection(WebServiceType.CONSUMER_FROM_EXISTING_WSDL == type);
      m_createProviderFromExistingWsdl.setSelection(WebServiceType.PROVIDER_FROM_EXISTING_WSDL == type);
      m_createProviderFromEmptyWsdl.setSelection(WebServiceType.PROVIDER_FROM_EMPTY_WSDL == type);
    });
  }

  protected boolean setWebServiceTypeInternal(WebServiceType type) {
    return setProperty(PROP_WEB_SERVICE_TYPE, type);
  }

  public String getWsdlName() {
    return getPropertyString(PROP_PROVIDER_WSDL_NAME);
  }

  public void setWsdlName(String s) {
    setPropertyWithChangingControl(m_providerWsdlNameField, () -> setWsdlNameInternal(s), field -> field.setText(s));
  }

  protected boolean setWsdlNameInternal(String s) {
    if (setPropertyString(PROP_PROVIDER_WSDL_NAME, s)) {
      guessPackage();
      return true;
    }
    return false;
  }

  public String getProviderWsdlUrl() {
    return getProperty(PROP_PROVIDER_WSDL_URL, String.class);
  }

  public void setProviderWsdlUrl(String wsdlUrl) {
    setPropertyWithChangingControl(m_providerWsdlUrlField, () -> setProviderWsdlUrlInternal(wsdlUrl), field -> field.setText(wsdlUrl));
  }

  protected boolean setProviderWsdlUrlInternal(String wsdlUrl) {
    if (setProperty(PROP_PROVIDER_WSDL_URL, wsdlUrl)) {
      guessPackage();
      return true;
    }
    return false;
  }

  protected boolean isPackageChanged() {
    return m_packageChanged;
  }

  protected void setPackageChanged(boolean packageChanged) {
    m_packageChanged = packageChanged;
  }
}
