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
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.BindingFileCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.BuildJaxWsEntryCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.ExternalFileCopyOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.JaxWsServletRegistrationOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.SunJaxWsEntryCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsProviderImplNewOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsStubGenerationOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsdlCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.JaxWsServletRegistrationWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsPropertiesExistingWsdlWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsPropertiesNewWsdlWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsProviderImplClassWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsStubWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsdlLocationWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsdlSelectionWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.util.GlobalBindingRegistrationHelper;
import org.eclipse.scout.sdk.ws.jaxws.util.GlobalBindingRegistrationHelper.SchemaCandidate;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtifact.TypeEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.ServletRegistrationUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.ServletRegistrationUtility.Registration;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IOperationFinishedListener;
import org.eclipse.ui.IWorkbench;

public class WsProviderNewWizard extends AbstractWorkspaceWizard {

  private IScoutBundle m_bundle;

  private WsdlSelectionWizardPage m_wsdlSelectionWizardPage;
  private WsdlLocationWizardPage m_wsdlLocationWizardPage;
  private JaxWsServletRegistrationWizardPage m_servletRegistrationWizardPage;
  private WsPropertiesNewWsdlWizardPage m_wsPropertiesNewWsdlWizardPage;
  private WsPropertiesExistingWsdlWizardPage m_wsPropertiesExistingWsdlWizardPage;
  private WsProviderImplClassWizardPage m_wsProviderImplClassWizardPage;
  private WsStubWizardPage m_wsStubWizardPage;

  private JaxWsServletRegistrationOperation m_servletRegistrationOperation;
  private ExternalFileCopyOperation[] m_copyOperations;
  private BuildJaxWsEntryCreateOperation m_buildJaxWsEntryCreateOperation;
  private SunJaxWsEntryCreateOperation m_sunJaxWsEntryCreateOperation;
  private WsdlCreateOperation m_wsdlCreateOperation;
  private WsStubGenerationOperation m_stubGenerationOperation;
  private WsProviderImplNewOperation m_wsProviderImplNewOperation;

  private QName m_portTypeQName;
  private String m_alias;
  private boolean m_createBindingFile;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("CreateWsProvider"));

    m_copyOperations = new ExternalFileCopyOperation[0];
    m_bundle = UiUtility.getScoutBundleFromSelection(selection);
  }

  @Override
  public void addPages() {
    // new or existing WSDL
    m_wsdlSelectionWizardPage = new WsdlSelectionWizardPage(m_bundle);
    m_wsdlSelectionWizardPage.setTitle(Texts.get("CreateWsProvider"));
    m_wsdlSelectionWizardPage.setWsdlFolder(JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_PROVIDER, false)); // initial value
    m_wsdlSelectionWizardPage.setRootWsdlFolder(JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_PROVIDER, false));
    m_wsdlSelectionWizardPage.addPropertyChangeListener(new P_WsdlSelectionPropertyListener());
    addPage(m_wsdlSelectionWizardPage);

    // WSDL selection
    m_wsdlLocationWizardPage = new WsdlLocationWizardPage(m_bundle);
    m_wsdlLocationWizardPage.setTitle(Texts.get("CreateWsProvider"));
    m_wsdlLocationWizardPage.setExcludePage(m_wsdlSelectionWizardPage.isNewWsdl());
    m_wsdlLocationWizardPage.setWsdlFolderVisible(false);
    m_wsdlLocationWizardPage.setWsdlFolder(JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_PROVIDER, false)); // initial value
    m_wsdlLocationWizardPage.setRootWsdlFolder(JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_PROVIDER, false));
    m_wsdlLocationWizardPage.setExcludePage(true);
    m_wsdlLocationWizardPage.addPropertyChangeListener(new P_WsdlLocationPropertyListener());
    addPage(m_wsdlLocationWizardPage);

    // Servlet registration
    m_servletRegistrationWizardPage = new JaxWsServletRegistrationWizardPage(m_bundle, false);
    m_servletRegistrationWizardPage.setTitle(Texts.get("CreateWsProvider"));
    m_servletRegistrationWizardPage.addPropertyChangeListener(new P_ServletRegistrationPropertyListener());
    m_servletRegistrationWizardPage.initializeDefaultValues(m_bundle);
    Registration registration = ServletRegistrationUtility.getServletRegistration(m_bundle);
    m_servletRegistrationWizardPage.setExcludePage(registration != null && !StringUtility.isNullOrEmpty(registration.getAlias()));
    addPage(m_servletRegistrationWizardPage);

    // WS properties of new WSDL Wizard Page
    m_wsPropertiesNewWsdlWizardPage = new WsPropertiesNewWsdlWizardPage(m_bundle);
    m_wsPropertiesNewWsdlWizardPage.setTitle(Texts.get("CreateWsProvider"));
    m_wsPropertiesNewWsdlWizardPage.setJaxWsServletAlias(m_servletRegistrationWizardPage.getAlias());
    m_wsPropertiesNewWsdlWizardPage.addPropertyChangeListener(new P_WsPropertiesNewWsdlPropertyListener());
    m_wsPropertiesNewWsdlWizardPage.setExcludePage(true);
    addPage(m_wsPropertiesNewWsdlWizardPage);

    // WS properties of existing WSDL Wizard Page
    m_wsPropertiesExistingWsdlWizardPage = new WsPropertiesExistingWsdlWizardPage(m_bundle, WebserviceEnum.PROVIDER);
    m_wsPropertiesExistingWsdlWizardPage.setTitle(Texts.get("CreateWsProvider"));
    m_wsPropertiesExistingWsdlWizardPage.setJaxWsServletAlias(m_servletRegistrationWizardPage.getAlias());
    m_wsPropertiesExistingWsdlWizardPage.addPropertyChangeListener(new P_WsPropertiesExistingWsdlPropertyListener());
    m_wsPropertiesExistingWsdlWizardPage.setExcludePage(true);
    addPage(m_wsPropertiesExistingWsdlWizardPage);

    // WS Stub
    m_wsStubWizardPage = new WsStubWizardPage(m_bundle);
    m_wsStubWizardPage.setTitle(Texts.get("CreateWsProvider"));
    addPage(m_wsStubWizardPage);

    // Implementing class
    m_wsProviderImplClassWizardPage = new WsProviderImplClassWizardPage(m_bundle);
    m_wsProviderImplClassWizardPage.setTitle(Texts.get("CreateWsProvider"));
    addPage(m_wsProviderImplClassWizardPage);

    // set default value
    m_wsdlSelectionWizardPage.setNewWsdl(true);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    IFolder wsdlFolder = m_wsdlLocationWizardPage.getWsdlFolder();

    if (!m_wsdlSelectionWizardPage.isNewWsdl()) {
      File wsdlFile = m_wsdlLocationWizardPage.getWsdlFile();

      List<ExternalFileCopyOperation> copyOperations = new LinkedList<ExternalFileCopyOperation>();
      if (!JaxWsSdkUtility.existsFileInProject(m_bundle, wsdlFolder, wsdlFile)) {
        ExternalFileCopyOperation op = new ExternalFileCopyOperation();
        op.setBundle(m_bundle);
        op.setOverwrite(true);
        op.setExternalFile(m_wsdlLocationWizardPage.getWsdlFile());
        op.setWorkspacePath(wsdlFolder.getProjectRelativePath());
        copyOperations.add(op);
      }

      for (File file : m_wsdlLocationWizardPage.getAdditionalFiles()) {
        if (!JaxWsSdkUtility.existsFileInProject(m_bundle, wsdlFolder, file)) {
          ExternalFileCopyOperation op = new ExternalFileCopyOperation();
          op.setBundle(m_bundle);
          op.setOverwrite(true);
          op.setExternalFile(file);
          op.setWorkspacePath(wsdlFolder.getProjectRelativePath());
          copyOperations.add(op);
        }
      }
      m_copyOperations = copyOperations.toArray(new ExternalFileCopyOperation[copyOperations.size()]);
    }

    m_createBindingFile = m_wsStubWizardPage.isCreateBindingFile();

    QName serviceQName = null;
    QName portQName = null;
    String urlPattern;
    String wsdlFileName;
    String targetNamespace;

    // prepare jaxWsServletRegistrationOperation
    m_servletRegistrationOperation = new JaxWsServletRegistrationOperation();
    m_servletRegistrationOperation.setBundle(m_bundle);
    m_servletRegistrationOperation.setRegistrationBundle(m_servletRegistrationWizardPage.getRegistrationBundle());
    m_servletRegistrationOperation.setJaxWsAlias(m_servletRegistrationWizardPage.getAlias());

    // prepare buildJaxWs.xml operation
    m_buildJaxWsEntryCreateOperation = new BuildJaxWsEntryCreateOperation(WebserviceEnum.PROVIDER);
    m_buildJaxWsEntryCreateOperation.setBundle(m_bundle);
    if (m_wsdlSelectionWizardPage.isNewWsdl()) {
      m_alias = m_wsPropertiesNewWsdlWizardPage.getAlias();
      targetNamespace = m_wsPropertiesNewWsdlWizardPage.getTargetNamespace();
    }
    else {
      m_alias = m_wsPropertiesExistingWsdlWizardPage.getAlias();
      targetNamespace = m_wsdlLocationWizardPage.getWsdlDefinition().getTargetNamespace();
    }

    m_buildJaxWsEntryCreateOperation.setAlias(m_alias);

    // build properties and binding file
    Map<String, List<String>> buildProperties = JaxWsSdkUtility.getDefaultBuildProperties();

    // custom stub package
    String defaultPackageName;
    if (m_wsdlSelectionWizardPage.isNewWsdl()) {
      defaultPackageName = JaxWsSdkUtility.targetNamespaceToPackageName(targetNamespace);
    }
    else {
      Definition wsdlDefinition = m_wsdlLocationWizardPage.getWsdlDefinition();
      defaultPackageName = JaxWsSdkUtility.resolveStubPackageName(null, wsdlDefinition);
    }

    if (!CompareUtility.equals(m_wsStubWizardPage.getPackageName(), defaultPackageName)) {
      // register custom package name
      JaxWsSdkUtility.addBuildProperty(buildProperties, JaxWsConstants.OPTION_PACKAGE, m_wsStubWizardPage.getPackageName());
    }
    m_buildJaxWsEntryCreateOperation.setBuildProperties(buildProperties);

    // prepare sunJaxWs.xml operation
    if (m_wsdlSelectionWizardPage.isNewWsdl()) {
      // new WSDL file
      serviceQName = new QName(targetNamespace, m_wsPropertiesNewWsdlWizardPage.getServiceName());
      portQName = new QName(targetNamespace, m_wsPropertiesNewWsdlWizardPage.getPortName());
      m_portTypeQName = new QName(targetNamespace, m_wsPropertiesNewWsdlWizardPage.getPortTypeName());
      urlPattern = m_wsPropertiesNewWsdlWizardPage.getUrlPattern();
      wsdlFileName = m_wsPropertiesNewWsdlWizardPage.getWsdlName();

      m_wsdlCreateOperation = new WsdlCreateOperation();
      m_wsdlCreateOperation.setBundle(m_bundle);
      m_wsdlCreateOperation.setAlias(m_alias);

      WsdlResource wsdlResource = new WsdlResource(m_bundle);
      wsdlResource.setFile(JaxWsSdkUtility.getFile(m_bundle, wsdlFolder.getProjectRelativePath().append(m_wsPropertiesNewWsdlWizardPage.getWsdlName()), false));
      m_wsdlCreateOperation.setWsdlResource(wsdlResource);
      m_wsdlCreateOperation.setTargetNamespace(targetNamespace);
      m_wsdlCreateOperation.setService(m_wsPropertiesNewWsdlWizardPage.getServiceName());
      m_wsdlCreateOperation.setPortName(m_wsPropertiesNewWsdlWizardPage.getPortName());
      m_wsdlCreateOperation.setPortType(m_wsPropertiesNewWsdlWizardPage.getPortTypeName());
      m_wsdlCreateOperation.setBinding(m_wsPropertiesNewWsdlWizardPage.getBinding());
      m_wsdlCreateOperation.setUrlPattern(m_wsPropertiesNewWsdlWizardPage.getUrlPattern());
      m_wsdlCreateOperation.setServiceOperationName(m_wsPropertiesNewWsdlWizardPage.getServiceOperationName());
      m_wsdlCreateOperation.setWsdlStyle(m_wsPropertiesNewWsdlWizardPage.getWsdlStyle());
    }
    else {
      // existing WSDL file
      if (m_wsPropertiesExistingWsdlWizardPage.getService() != null) {
        serviceQName = m_wsPropertiesExistingWsdlWizardPage.getService().getQName();
      }
      if (m_wsPropertiesExistingWsdlWizardPage.getPort() != null) {
        if (serviceQName != null) {
          portQName = new QName(serviceQName.getNamespaceURI(), m_wsPropertiesExistingWsdlWizardPage.getPort().getName());
        }
        else {
          portQName = new QName(m_wsPropertiesExistingWsdlWizardPage.getPort().getName());
        }
      }
      m_portTypeQName = m_wsPropertiesExistingWsdlWizardPage.getPortType().getQName();
      urlPattern = m_wsPropertiesExistingWsdlWizardPage.getUrlPattern();
      wsdlFileName = m_wsdlLocationWizardPage.getWsdlFile().getName();
    }

    m_sunJaxWsEntryCreateOperation = new SunJaxWsEntryCreateOperation();
    m_sunJaxWsEntryCreateOperation.setBundle(m_bundle);
    m_sunJaxWsEntryCreateOperation.setImplTypeQualifiedName(StringUtility.join(".", m_wsProviderImplClassWizardPage.getPackageName(), m_wsProviderImplClassWizardPage.getTypeName()));
    m_sunJaxWsEntryCreateOperation.setAlias(m_alias);
    m_sunJaxWsEntryCreateOperation.setServiceQName(serviceQName);
    m_sunJaxWsEntryCreateOperation.setPortQName(portQName);
    m_sunJaxWsEntryCreateOperation.setUrlPattern(urlPattern);
    m_sunJaxWsEntryCreateOperation.setWsdlProjectRelativePath(wsdlFolder.getProjectRelativePath().append(wsdlFileName));

    m_stubGenerationOperation = new WsStubGenerationOperation();
    m_stubGenerationOperation.setBundle(m_bundle);
    m_stubGenerationOperation.setAlias(m_alias);
    m_stubGenerationOperation.setWsdlFolder(wsdlFolder);
    m_stubGenerationOperation.setProperties(buildProperties);
    m_stubGenerationOperation.setWsdlFileName(wsdlFileName);
    m_stubGenerationOperation.addOperationFinishedListener(new P_StubGenerationFinishedListener());

    m_wsProviderImplNewOperation = new WsProviderImplNewOperation();
    m_wsProviderImplNewOperation.setBundle(m_bundle);
    m_wsProviderImplNewOperation.setPackageName(m_wsProviderImplClassWizardPage.getPackageName());
    m_wsProviderImplNewOperation.setTypeName(m_wsProviderImplClassWizardPage.getTypeName());
    m_wsProviderImplNewOperation.setCreateScoutWebServiceAnnotation(m_wsProviderImplClassWizardPage.isAnnotateImplClass());
    m_wsProviderImplNewOperation.setSessionFactoryQName(m_wsProviderImplClassWizardPage.getSessionFactory());
    m_wsProviderImplNewOperation.setAuthenticationHandlerQName(m_wsProviderImplClassWizardPage.getAuthenticationHandler());
    m_wsProviderImplNewOperation.setCredentialValidationStrategyQName(m_wsProviderImplClassWizardPage.getCredentialValidationStrategy());

    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    JaxWsSdk.getDefault().getMarkerQueueManager().suspend();
    try {
      m_servletRegistrationOperation.run(monitor, workingCopyManager);

      for (ExternalFileCopyOperation op : m_copyOperations) {
        op.validate();
        op.run(monitor, workingCopyManager);
      }

      if (m_wsdlCreateOperation != null) {
        m_wsdlCreateOperation.validate();
        m_wsdlCreateOperation.run(monitor, workingCopyManager);
      }

      // create binding file(s)
      if (m_createBindingFile) {
        Map<String, List<String>> buildProperties = m_buildJaxWsEntryCreateOperation.getBuildProperties();

        // get WSDL resource to determine associated XML schemas
        WsdlResource wsdlResource;
        if (m_wsdlCreateOperation != null) {
          wsdlResource = m_wsdlCreateOperation.getWsdlResource();
        }
        else {
          IFile wsdlFile = JaxWsSdkUtility.getFile(m_bundle, m_sunJaxWsEntryCreateOperation.getWsdlProjectRelativePath(), false);
          wsdlResource = new WsdlResource(m_bundle);
          wsdlResource.setFile(wsdlFile);
        }

        // iterate through schemas to create respective binding files
        SchemaCandidate[] schemaCandidates = GlobalBindingRegistrationHelper.getSchemaCandidates(wsdlResource.getFile());
        for (int i = 0; i < schemaCandidates.length; i++) {
          SchemaCandidate candidate = schemaCandidates[i];

          // schema targetNamespace must only be specified if multiple schemas exist
          String schemaTargetNamespace = null;
          if (schemaCandidates.length > 1) {
            schemaTargetNamespace = SchemaUtility.getSchemaTargetNamespace(candidate.getSchema());
          }

          IPath bindingFilePath = JaxWsSdkUtility.toUniqueProjectRelativeBindingFilePath(m_bundle, m_alias, schemaTargetNamespace);

          BindingFileCreateOperation op = new BindingFileCreateOperation();
          op.setBundle(m_bundle);
          op.setWsdlDestinationFolder(m_wsdlLocationWizardPage.getWsdlFolder());
          op.setSchemaTargetNamespace(schemaTargetNamespace);
          if (candidate.getWsdlArtifact().getTypeEnum() == TypeEnum.REFERENCED_WSDL) {
            op.setWsdlLocation(candidate.getWsdlArtifact().getFileHandle().getFile());
          }
          op.setProjectRelativePath(bindingFilePath);
          JaxWsSdkUtility.addBuildProperty(buildProperties, JaxWsConstants.OPTION_BINDING_FILE, bindingFilePath.toString());

          // Global binding file section can only be defined in one file -> pick first
          if (i == 0) {
            op.setCreateGlobalBindingSection(true);
          }
          op.validate();
          op.run(monitor, workingCopyManager);
        }

        m_buildJaxWsEntryCreateOperation.setBuildProperties(buildProperties);
      }

      try {
        m_stubGenerationOperation.validate();
        m_stubGenerationOperation.run(monitor, workingCopyManager);

        IFile stubJarFile = JaxWsSdkUtility.getStubJarFile(m_bundle, m_buildJaxWsEntryCreateOperation.getBuildProperties(), m_sunJaxWsEntryCreateOperation.getWsdlProjectRelativePath().lastSegment());
        IType portTypeInterfaceType = JaxWsSdkUtility.resolvePortTypeInterfaceType(m_portTypeQName, stubJarFile);

        // wait for stub JAR to be part of the classpth
        int maxWaitLoops = 10;
        while (portTypeInterfaceType == null && maxWaitLoops > 0) {
          try {
            Thread.sleep(500);
          }
          catch (InterruptedException e) {
            JaxWsSdk.logError(e);
          }
          portTypeInterfaceType = JaxWsSdkUtility.resolvePortTypeInterfaceType(m_portTypeQName, stubJarFile);
          maxWaitLoops--;
        }

        m_wsProviderImplNewOperation.setPortTypeInterfaceType(portTypeInterfaceType);
        m_wsProviderImplNewOperation.validate();
        m_wsProviderImplNewOperation.run(monitor, workingCopyManager);

        String fqnPortType = m_wsProviderImplNewOperation.getCreatedType().getFullyQualifiedName();

        // wait for port type to be part of the hierarchy
        maxWaitLoops = 10;
        while (!TypeUtility.existsType(fqnPortType) && maxWaitLoops > 0) {
          try {
            Thread.sleep(500);
          }
          catch (InterruptedException e) {
            JaxWsSdk.logError(e);
          }
          maxWaitLoops--;
        }
      }
      catch (Exception e) {
        JaxWsSdk.logError(e);
      }

      m_buildJaxWsEntryCreateOperation.validate();
      m_buildJaxWsEntryCreateOperation.run(monitor, workingCopyManager);

      // creation of JAX-WS entry must be at the very end as this causes load of node page
      m_sunJaxWsEntryCreateOperation.validate();
      m_sunJaxWsEntryCreateOperation.run(monitor, workingCopyManager);

      return true;
    }
    finally {
      JaxWsSdk.getDefault().getMarkerQueueManager().resume();
    }
  }

  private class P_WsdlSelectionPropertyListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (WsdlSelectionWizardPage.PROP_CREATE_NEW_WSDL.equals(event.getPropertyName())) {
        boolean createNewWsdl = (Boolean) event.getNewValue();

        m_wsdlLocationWizardPage.setExcludePage(createNewWsdl);
        m_wsPropertiesNewWsdlWizardPage.setExcludePage(!createNewWsdl);
        m_wsPropertiesExistingWsdlWizardPage.setExcludePage(createNewWsdl);
      }
      else if (WsdlSelectionWizardPage.PROP_WSDL_FOLDER.equals(event.getPropertyName())) {
        m_wsdlLocationWizardPage.setWsdlFolder((IFolder) event.getNewValue());
      }
    }
  }

  private class P_WsdlLocationPropertyListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (WsdlLocationWizardPage.PROP_WSDL_DEFINITION.equals(event.getPropertyName())) {
        Definition wsdlDefinition = (Definition) event.getNewValue();
        m_wsPropertiesExistingWsdlWizardPage.setWsdlDefinition(wsdlDefinition);

        if (wsdlDefinition != null) {
          String defaultPackageName = JaxWsSdkUtility.resolveStubPackageName(null, wsdlDefinition);
          m_wsStubWizardPage.setDefaultPackageName(defaultPackageName);
          m_wsStubWizardPage.setPackageName(defaultPackageName);
        }
        else {
          m_wsStubWizardPage.setDefaultPackageName(null);
        }
      }
    }
  }

  private class P_WsPropertiesExistingWsdlPropertyListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (WsPropertiesExistingWsdlWizardPage.PROP_PORT_TYPE.equals(event.getPropertyName())) {
        String portTypeName = null;
        PortType portType = (PortType) event.getNewValue();
        if (portType != null) {
          portTypeName = portType.getQName().getLocalPart();
          m_wsProviderImplClassWizardPage.setTypeName(JaxWsSdkUtility.getPlainPortTypeName(portTypeName));
        }
        else {
          m_wsProviderImplClassWizardPage.setTypeName(null);
        }
      }
    }
  }

  private class P_WsPropertiesNewWsdlPropertyListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (WsPropertiesNewWsdlWizardPage.PROP_TARGET_NAMESPACE.equals(event.getPropertyName())) {
        String targetNamespace = (String) event.getNewValue();
        String defaultPackageName = JaxWsSdkUtility.targetNamespaceToPackageName(targetNamespace);
        m_wsStubWizardPage.setDefaultPackageName(defaultPackageName);
        m_wsStubWizardPage.setPackageName(defaultPackageName);
      }
      else if (WsPropertiesNewWsdlWizardPage.PROP_PORT_TYPE_NAME.equals(event.getPropertyName())) {
        m_wsProviderImplClassWizardPage.setTypeName(JaxWsSdkUtility.getPlainPortTypeName((String) event.getNewValue()));
      }
    }
  }

  private class P_ServletRegistrationPropertyListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (JaxWsServletRegistrationWizardPage.PROP_ALIAS.equals(event.getPropertyName())) {
        if (m_wsPropertiesNewWsdlWizardPage != null) {
          m_wsPropertiesNewWsdlWizardPage.setJaxWsServletAlias(m_servletRegistrationWizardPage.getAlias());
        }
        if (m_wsPropertiesExistingWsdlWizardPage != null) {
          m_wsPropertiesExistingWsdlWizardPage.setJaxWsServletAlias(m_servletRegistrationWizardPage.getAlias());
        }
      }
    }
  }

  private class P_StubGenerationFinishedListener implements IOperationFinishedListener {

    @Override
    public void operationFinished(boolean success, Throwable exception) {
      // nop at the time
    }
  }
}
