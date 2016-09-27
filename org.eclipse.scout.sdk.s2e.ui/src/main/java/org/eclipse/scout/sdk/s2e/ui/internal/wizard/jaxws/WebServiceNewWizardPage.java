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
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.jaxws;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.jaxws.JaxWsUtils;
import org.eclipse.scout.sdk.core.s.jaxws.ParsedWsdl;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.core.s.util.CoreScoutUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;
import org.eclipse.scout.sdk.s2e.operation.jaxws.WebServiceNewOperation;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.IProposalListener;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.PackageContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.resource.IResourceChangedListener;
import org.eclipse.scout.sdk.s2e.ui.fields.resource.ResourceTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizardPage;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
 * @author Matthias Villiger
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

  public static enum WebServiceType {
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
    Group typeGroupBox = getFieldToolkit().createGroupBox(parent, "Type of Web Service");

    // radio button "new consumer"
    m_createConsumer = new Button(typeGroupBox, SWT.RADIO);
    m_createConsumer.setText("Create new Web Service Consumer");
    m_createConsumer.setSelection(WebServiceType.CONSUMER_FROM_EXISTING_WSDL.equals(getWebServiceType()));
    m_createConsumer.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          setStateChanging(true);
          setWebServiceTypeInternal(WebServiceType.CONSUMER_FROM_EXISTING_WSDL);
          setViewState();
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // Consumer WSDL
    m_consumerWsdlUrlField = getFieldToolkit().createResourceField(typeGroupBox, "WSDL URL", TextField.TYPE_LABEL, labelWidth);
    m_consumerWsdlUrlField.setText(getConsumerWsdlUrl());
    m_consumerWsdlUrlField.addResourceChangedListener(new IResourceChangedListener() {
      @Override
      public void resourceChanged(URL newUrl, File newFile) {
        try {
          setStateChanging(true);
          String url = null;
          if (newUrl != null) {
            url = newUrl.toExternalForm();
          }
          setConsumerWsdlUrlInternal(url);
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // radio button "new provider from wsdl"
    m_createProviderFromExistingWsdl = new Button(typeGroupBox, SWT.RADIO);
    m_createProviderFromExistingWsdl.setText("Create new Web Service Provider from existing WSDL");
    m_createProviderFromExistingWsdl.setSelection(WebServiceType.PROVIDER_FROM_EXISTING_WSDL.equals(getWebServiceType()));
    m_createProviderFromExistingWsdl.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          setStateChanging(true);
          setWebServiceTypeInternal(WebServiceType.PROVIDER_FROM_EXISTING_WSDL);
          setViewState();
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // Provider WSDL
    m_providerWsdlUrlField = getFieldToolkit().createResourceField(typeGroupBox, "WSDL URL", TextField.TYPE_LABEL, labelWidth);
    m_providerWsdlUrlField.setText(getProviderWsdlUrl());
    m_providerWsdlUrlField.addResourceChangedListener(new IResourceChangedListener() {
      @Override
      public void resourceChanged(URL newUrl, File newFile) {
        try {
          setStateChanging(true);
          String url = null;
          if (newUrl != null) {
            url = newUrl.toExternalForm();
          }
          setProviderWsdlUrlInternal(url);
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // radio button "new provider from empty wsdl"
    m_createProviderFromEmptyWsdl = new Button(typeGroupBox, SWT.RADIO);
    m_createProviderFromEmptyWsdl.setText("Create new Web Service Provider with empty WSDL");
    m_createProviderFromEmptyWsdl.setSelection(WebServiceType.PROVIDER_FROM_EMPTY_WSDL.equals(getWebServiceType()));
    m_createProviderFromEmptyWsdl.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          setStateChanging(true);
          setWebServiceTypeInternal(WebServiceType.PROVIDER_FROM_EMPTY_WSDL);
          setViewState();
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // Provider WSDL Name
    m_providerWsdlNameField = getFieldToolkit().createStyledTextField(typeGroupBox, "Web Service Name", TextField.TYPE_LABEL, labelWidth);
    m_providerWsdlNameField.setText(getWsdlName());
    m_providerWsdlNameField.setReadOnlySuffix(ISdkProperties.SUFFIX_WS_PROVIDER);
    m_providerWsdlNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        try {
          setStateChanging(true);
          setWsdlNameInternal(m_providerWsdlNameField.getText());
        }
        finally {
          setStateChanging(false);
        }
      }
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
    Group projectGroupBox = getFieldToolkit().createGroupBox(parent, "Target Project");

    // radio button "create new project"
    m_createNewProjectButton = new Button(projectGroupBox, SWT.RADIO);
    m_createNewProjectButton.setText("Create new project for this Web Service");
    m_createNewProjectButton.setSelection(isCreateNewProject());
    m_createNewProjectButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          setStateChanging(true);
          setIsCreateNewProjectInternal(true);
          setViewState();
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // existing server project to add the web service dependency
    Predicate<IJavaProject> serverProjectsFilter = new Predicate<IJavaProject>() {
      @Override
      public boolean test(IJavaProject element) {
        return isServerProject(element);
      }
    };
    m_serverProjectField = getFieldToolkit().createProjectProposalField(projectGroupBox, "Add new Project to", serverProjectsFilter, labelWidth);
    m_serverProjectField.acceptProposal(getServerProject());
    m_serverProjectField.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        try {
          setStateChanging(true);
          IJavaProject jp = (IJavaProject) proposal;
          setServerProjectInternal(jp);
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // artifact id
    m_artifactIdField = getFieldToolkit().createStyledTextField(projectGroupBox, "Artifact Id", TextField.TYPE_LABEL, labelWidth);
    m_artifactIdField.setText(getArtifactId());
    m_artifactIdField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        try {
          setStateChanging(true);
          setArtifactIdInternal(m_artifactIdField.getText());
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // radio button "existing project"
    m_addToExistingProjectButton = new Button(projectGroupBox, SWT.RADIO);
    m_addToExistingProjectButton.setText("Add Web Service to an existing Project");
    m_addToExistingProjectButton.setSelection(!isCreateNewProject());
    m_addToExistingProjectButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          setStateChanging(true);
          setIsCreateNewProjectInternal(false);
          setViewState();
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // existing jaxws project
    Predicate<IJavaProject> jaxwsProjectsFilter = new Predicate<IJavaProject>() {
      @Override
      public boolean test(IJavaProject element) {
        return isJaxWsProject(element);
      }
    };
    m_existingJaxWsProjectField = getFieldToolkit().createProjectProposalField(projectGroupBox, "Web Service Project", jaxwsProjectsFilter, labelWidth);
    m_existingJaxWsProjectField.acceptProposal(getExistingJaxWsProject());
    m_existingJaxWsProjectField.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        try {
          setStateChanging(true);
          IJavaProject javaProject = (IJavaProject) proposal;
          setExistingJaxWsProjectInternal(javaProject);
        }
        finally {
          setStateChanging(false);
        }
      }
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
    Group attributesGroupBox = getFieldToolkit().createGroupBox(parent, "Web Service Artifacts Package");

    // package field
    m_packageField = getFieldToolkit().createPackageField(attributesGroupBox, "Target Package", getExistingJaxWsProject(), labelWidth);
    m_packageField.setText(getTargetPackage());
    m_packageField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        if (!isPackageChanged() && !isStateChanging()) {
          setPackageChanged(true); // mark as changed if it is manually changed by the user
        }
        try {
          setStateChanging(true);
          setTargetPackageInternal(m_packageField.getText());
        }
        finally {
          setStateChanging(false);
        }
      }
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
    if (StringUtils.isNotBlank(getArtifactId())) {
      return; // already an artifact id defined.
    }
    if (!isCreateNewProject()) {
      return;
    }
    if (!S2eUtils.exists(getServerProject())) {
      return;
    }
    setArtifactId(getServerProject().getElementName() + ".jaxws");
  }

  @SuppressWarnings("pmd:NPathComplexity")
  protected void guessPackage() {
    if (isPackageChanged()) {
      return; // already changed manually. don't update
    }
    String groupId = null;
    String artifactId = null;
    if (isCreateNewProject()) {
      if (StringUtils.isNotBlank(getArtifactId()) && S2eUtils.exists(getServerProject())) {
        Document pom = getPomDocument(getServerProject());
        groupId = CoreScoutUtils.getGroupIdOfPom(pom);
        artifactId = getArtifactId();
      }
    }
    else if (S2eUtils.exists(getExistingJaxWsProject())) {
      Document pom = getPomDocument(getExistingJaxWsProject());
      groupId = CoreScoutUtils.getGroupIdOfPom(pom);
      artifactId = CoreScoutUtils.getArtifactIdOfPom(pom);
    }

    if (artifactId == null || StringUtils.isBlank(artifactId)) {
      return;
    }

    String baseName = null;
    if (WebServiceType.PROVIDER_FROM_EMPTY_WSDL.equals(getWebServiceType())) {
      baseName = getWsdlName();
      if (StringUtils.isNotBlank(baseName)) {
        baseName = JaxWsUtils.removeCommonSuffixes(baseName.toLowerCase());
      }
    }
    else if (WebServiceType.PROVIDER_FROM_EXISTING_WSDL.equals(getWebServiceType())) {
      baseName = getWebServiceNameFromUrl(getProviderWsdlUrl());
    }
    else {
      baseName = getWebServiceNameFromUrl(getConsumerWsdlUrl());
    }

    if (StringUtils.isBlank(baseName)) {
      return;
    }

    StringBuilder pckBuilder = new StringBuilder();
    if (StringUtils.isNotBlank(groupId) && !artifactId.startsWith(groupId)) {
      pckBuilder.append(groupId);
      pckBuilder.append('.');
    }
    pckBuilder.append(artifactId);
    pckBuilder.append('.');
    pckBuilder.append(baseName);
    setTargetPackage(pckBuilder.toString());
  }

  protected static String getWebServiceNameFromUrl(String url) {
    if (StringUtils.isBlank(url)) {
      return null;
    }
    try {
      String file = new Path(new URL(url).getPath()).lastSegment();
      if (StringUtils.isNotBlank(file)) {
        file = file.toLowerCase();

        int lastDotPos = file.lastIndexOf('.');
        if (lastDotPos > 0) {
          file = file.substring(0, lastDotPos);
        }
        return JaxWsUtils.removeCommonSuffixes(file);
      }
    }
    catch (MalformedURLException e) {
      SdkLog.debug("Invalid URL passed.", e);
    }
    return null;
  }

  protected static Document getPomDocument(IJavaProject project) {
    try {
      return S2eUtils.getPomDocument(project.getProject());
    }
    catch (CoreException e) {
      SdkLog.debug("Unable to load pom of project '{}'.", project.getElementName(), e);
      return null;
    }
  }

  protected boolean isServerProject(IJavaProject jp) {
    try {
      return S2eUtils.exists(jp.findType(IScoutRuntimeTypes.IServerSession))
          && !jp.getProject().getFolder("src/main/webapp/WEB-INF").exists()
          && !WebServiceNewOperation.getWsdlRootFolder(jp.getProject()).exists();
    }
    catch (JavaModelException e) {
      SdkLog.warning("Cannot check type of project '{}'. This project will be ignored.", jp.getElementName(), e);
      return false;
    }
  }

  protected boolean isJaxWsProject(IJavaProject jp) {
    try {
      IProject project = jp.getProject();
      IFolder wsdlFolder = WebServiceNewOperation.getWsdlRootFolder(project);
      if (!wsdlFolder.exists()) {
        return false;
      }
      IFolder bindingFolder = WebServiceNewOperation.getBindingRootFolder(project);
      if (!bindingFolder.exists()) {
        return false;
      }
      IPackageFragmentRoot primarySourceFolder = S2eUtils.getPrimarySourceFolder(jp);
      if (!S2eUtils.exists(primarySourceFolder) || !primarySourceFolder.getResource().getProjectRelativePath().toString().toLowerCase().contains("java")) {
        return false;
      }
      if (!S2eUtils.exists(jp.findType(IScoutRuntimeTypes.AbstractWebServiceClient))) {
        return false;
      }

      final String prefix = "p";
      final String p = prefix + ":";
      StringBuilder bindingFilesXpathBuilder = new StringBuilder();
      bindingFilesXpathBuilder.append(p).append(IMavenConstants.PROJECT).append('/').append(p).append(IMavenConstants.BUILD).append('/').append(p).append(IMavenConstants.PLUGINS).append('/').append(p).append(IMavenConstants.PLUGIN)
          .append("[./").append(p).append(IMavenConstants.GROUP_ID).append("='").append(JaxWsUtils.CODEHAUS_GROUP_ID).append("' and ./").append(p).append(IMavenConstants.ARTIFACT_ID).append("='")
          .append(JaxWsUtils.JAXWS_MAVEN_PLUGIN_ARTIFACT_ID).append("']");
      List<Element> elements = CoreUtils.evaluateXPath(bindingFilesXpathBuilder.toString(), S2eUtils.getPomDocument(project), prefix, IMavenConstants.POM_XML_NAMESPACE);
      if (elements.isEmpty() && containsWsdls(wsdlFolder)) {
        // these are jaxws project that contain wsdls but they are not listed in the pom (auto discovery).
        // we do not support these because when adding a new wsdl to the pom, the existing ones will be ignored.
        return false;
      }
      return true;
    }
    catch (CoreException | XPathExpressionException e) {
      SdkLog.warning("Cannot check type of project '{}'. This project will be ignored.", jp.getElementName(), e);
      return false;
    }
  }

  protected static boolean containsWsdls(IFolder wsdlFolder) {
    final boolean[] result = new boolean[1];
    try {
      wsdlFolder.accept(new IResourceProxyVisitor() {
        @Override
        public boolean visit(IResourceProxy proxy) throws CoreException {
          if (proxy.getType() == IResource.FILE && proxy.getName().endsWith(JaxWsUtils.WSDL_FILE_EXTENSION)) {
            result[0] = true;
          }
          return !result[0];
        }
      }, 0);
    }
    catch (CoreException e) {
      SdkLog.warning("Unable to search WSDL files in project '{}'.", wsdlFolder.getProject().getName(), e);
      return false;
    }
    return result[0];
  }

  protected void setViewState() {
    m_artifactIdField.setEnabled(isCreateNewProject());
    m_serverProjectField.setEnabled(isCreateNewProject());
    m_existingJaxWsProjectField.setEnabled(!isCreateNewProject());

    m_consumerWsdlUrlField.setEnabled(WebServiceType.CONSUMER_FROM_EXISTING_WSDL.equals(getWebServiceType()));
    m_providerWsdlUrlField.setEnabled(WebServiceType.PROVIDER_FROM_EXISTING_WSDL.equals(getWebServiceType()));
    m_providerWsdlNameField.setEnabled(WebServiceType.PROVIDER_FROM_EMPTY_WSDL.equals(getWebServiceType()));
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
    if (!WebServiceType.PROVIDER_FROM_EMPTY_WSDL.equals(getWebServiceType())) {
      return Status.OK_STATUS;
    }
    return CompilationUnitNewWizardPage.validateJavaName(getWsdlName(), m_providerWsdlNameField.getReadOnlySuffix());
  }

  protected IStatus getStatusExistingJaxWsProject() {
    if (isCreateNewProject()) {
      return Status.OK_STATUS;
    }

    if (!S2eUtils.exists(getExistingJaxWsProject())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose the project in which the new Web Service should be created.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusServerProject() {
    if (!isCreateNewProject()) {
      return Status.OK_STATUS;
    }
    if (!S2eUtils.exists(getServerProject())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose for which server project a new Web Service project should be created.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusArtifactId() {
    if (!isCreateNewProject()) {
      return Status.OK_STATUS;
    }

    // check name pattern
    String msg = ScoutProjectNewHelper.getMavenNameErrorMessage(getArtifactId(), "Artifact Id");
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
    if (S2eUtils.exists(getServerProject())) {
      File serverProjectFolder = getServerProject().getProject().getLocation().makeAbsolute().toFile();
      File targetDir = serverProjectFolder.getParentFile();
      if (targetDir != null) {
        if (new File(targetDir, getArtifactId()).isDirectory()) {
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
    String wsdlUrl = null;
    ResourceTextField fieldToCheck = null;
    if (WebServiceType.CONSUMER_FROM_EXISTING_WSDL.equals(getWebServiceType())) {
      fieldToCheck = m_consumerWsdlUrlField;
      wsdlUrl = getConsumerWsdlUrl();
    }
    else if (WebServiceType.PROVIDER_FROM_EXISTING_WSDL.equals(getWebServiceType())) {
      fieldToCheck = m_providerWsdlUrlField;
      wsdlUrl = getProviderWsdlUrl();
    }
    else {
      return Status.OK_STATUS;
    }

    if (StringUtils.isBlank(wsdlUrl)) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please specify a WSDL URL.");
    }

    try {
      final URL url = new URL(wsdlUrl);
      if (Objects.equals(m_lastParsedWsdlUrl, url)) {
        return m_lastParsedWsdlUrlStatus;
      }

      final ResourceTextField currentUrlField = fieldToCheck;
      currentUrlField.setEnabled(false);
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
            if (!currentUrlField.isDisposed()) {
              currentUrlField.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                  if (!currentUrlField.isDisposed()) {
                    revalidate();
                    currentUrlField.setEnabled(true);
                  }
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
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The given WSDL URL is not valid.");
    }

    return Status.OK_STATUS;
  }

  protected String validateWsdl(URL url) {
    try (InputStream in = url.openStream()) {
      ParsedWsdl info = ParsedWsdl.create(url.toURI(), in);
      if (info.isEmpty()) {
        return "Either this Web Service uses SOAP encoding (use=encoded) or contains no operations. Ensure the Web Service uses literal encoding.";
      }
    }
    catch (Exception e) {
      SdkLog.debug(e);
      return "The given WSDL cannot be parsed.";
    }
    return null;
  }

  public boolean isCreateNewProject() {
    return getProperty(PROP_IS_CREATE_NEW_PROJECT, Boolean.class);
  }

  public void setIsCreateNewProject(boolean createNewProject) {
    try {
      setStateChanging(true);
      setIsCreateNewProjectInternal(createNewProject);
      if (isControlCreated()) {
        m_createNewProjectButton.setSelection(createNewProject);
        m_addToExistingProjectButton.setSelection(!createNewProject);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setIsCreateNewProjectInternal(boolean createNewProject) {
    setProperty(PROP_IS_CREATE_NEW_PROJECT, Boolean.valueOf(createNewProject));
  }

  public String getTargetPackage() {
    return getProperty(PROP_TARGET_PACKAGE, String.class);
  }

  public void setTargetPackage(String targetPackage) {
    try {
      setStateChanging(true);
      setTargetPackageInternal(targetPackage);
      if (isControlCreated() && m_packageField != null) {
        m_packageField.setText(targetPackage);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setTargetPackageInternal(String targetPackage) {
    setProperty(PROP_TARGET_PACKAGE, targetPackage);
  }

  public IJavaProject getExistingJaxWsProject() {
    return getProperty(PROP_EXISTING_JAXWS_PROJECT, IJavaProject.class);
  }

  public void setExistingJaxWsProject(IJavaProject jp) {
    try {
      setStateChanging(true);
      setExistingJaxWsProjectInternal(jp);
      if (isControlCreated()) {
        m_existingJaxWsProjectField.acceptProposal(jp);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setExistingJaxWsProjectInternal(IJavaProject jp) {
    if (setProperty(PROP_EXISTING_JAXWS_PROJECT, jp)) {
      if (isControlCreated()) {
        ((PackageContentProvider) m_packageField.getContentProvider()).setJavaProject(jp);
      }
      guessPackage();
    }
  }

  public String getConsumerWsdlUrl() {
    return getProperty(PROP_CONSUMER_WSDL_URL, String.class);
  }

  public void setConsumerWsdlUrl(String wsdlUrl) {
    try {
      setStateChanging(true);
      setConsumerWsdlUrlInternal(wsdlUrl);
      if (isControlCreated()) {
        m_consumerWsdlUrlField.setText(wsdlUrl);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setConsumerWsdlUrlInternal(String wsdlUrl) {
    if (setProperty(PROP_CONSUMER_WSDL_URL, wsdlUrl)) {
      guessPackage();
    }
  }

  public String getArtifactId() {
    return getPropertyString(PROP_ARTIFACT_ID);
  }

  public void setArtifactId(String s) {
    try {
      setStateChanging(true);
      setArtifactIdInternal(s);
      if (isControlCreated()) {
        m_artifactIdField.setText(s);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setArtifactIdInternal(String s) {
    if (setPropertyString(PROP_ARTIFACT_ID, s)) {
      guessPackage();
    }
  }

  public IJavaProject getServerProject() {
    return getProperty(PROP_SERVER_PROJECT, IJavaProject.class);
  }

  public void setServerProject(IJavaProject jp) {
    try {
      setStateChanging(true);
      setServerProjectInternal(jp);
      if (isControlCreated()) {
        m_serverProjectField.acceptProposal(jp);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setServerProjectInternal(IJavaProject jp) {
    if (setProperty(PROP_SERVER_PROJECT, jp)) {
      guessArtifactId();
      guessPackage();
    }
  }

  public WebServiceType getWebServiceType() {
    return getProperty(PROP_WEB_SERVICE_TYPE, WebServiceType.class);
  }

  public void setWebServiceType(WebServiceType type) {
    try {
      setStateChanging(true);
      setWebServiceTypeInternal(type);
      if (isControlCreated()) {
        m_createConsumer.setSelection(WebServiceType.CONSUMER_FROM_EXISTING_WSDL.equals(type));
        m_createProviderFromExistingWsdl.setSelection(WebServiceType.PROVIDER_FROM_EXISTING_WSDL.equals(type));
        m_createProviderFromEmptyWsdl.setSelection(WebServiceType.PROVIDER_FROM_EMPTY_WSDL.equals(type));
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setWebServiceTypeInternal(WebServiceType type) {
    setProperty(PROP_WEB_SERVICE_TYPE, type);
  }

  public String getWsdlName() {
    return getPropertyString(PROP_PROVIDER_WSDL_NAME);
  }

  public void setWsdlName(String s) {
    try {
      setStateChanging(true);
      setWsdlNameInternal(s);
      if (isControlCreated()) {
        m_providerWsdlNameField.setText(s);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setWsdlNameInternal(String s) {
    if (setPropertyString(PROP_PROVIDER_WSDL_NAME, s)) {
      guessPackage();
    }
  }

  public String getProviderWsdlUrl() {
    return getProperty(PROP_PROVIDER_WSDL_URL, String.class);
  }

  public void setProviderWsdlUrl(String wsdlUrl) {
    try {
      setStateChanging(true);
      setProviderWsdlUrlInternal(wsdlUrl);
      if (isControlCreated()) {
        m_providerWsdlUrlField.setText(wsdlUrl);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setProviderWsdlUrlInternal(String wsdlUrl) {
    if (setProperty(PROP_PROVIDER_WSDL_URL, wsdlUrl)) {
      guessPackage();
    }
  }

  protected boolean isPackageChanged() {
    return m_packageChanged;
  }

  protected void setPackageChanged(boolean packageChanged) {
    m_packageChanged = packageChanged;
  }
}
