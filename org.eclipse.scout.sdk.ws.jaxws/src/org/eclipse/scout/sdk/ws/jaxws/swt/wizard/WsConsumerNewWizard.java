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
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.operation.service.ServiceRegistrationDescription;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.BindingFileCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.BuildJaxWsEntryCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.ExternalFileCopyOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsConsumerImplNewOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsStubGenerationOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsConsumerImplClassWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsPropertiesExistingWsdlWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsStubWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsdlLocationWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.util.GlobalBindingRegistrationHelper;
import org.eclipse.scout.sdk.ws.jaxws.util.GlobalBindingRegistrationHelper.SchemaCandidate;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtifact.TypeEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IOperationFinishedListener;

public class WsConsumerNewWizard extends AbstractWorkspaceWizard {

  public static final int TYPE_SERVICE_IMPLEMENTATION = 100;
  public static final int TYPE_SERVICE_REG_SERVER = 101;

  private IScoutBundle m_bundle;
  private WsdlLocationWizardPage m_wsdlLocationWizardPage;
  private WsPropertiesExistingWsdlWizardPage m_wsPropertiesExistingWsdlWizardPage;
  private WsConsumerImplClassWizardPage m_wsConsumerImplClassWizardPage;
  private WsStubWizardPage m_wsStubWizardPage;

  private ExternalFileCopyOperation[] m_copyOperations;
  private BuildJaxWsEntryCreateOperation m_buildJaxWsEntryCreateOperation;
  private WsStubGenerationOperation m_stubGenerationOperation;
  private WsConsumerImplNewOperation m_wsConsumerImplNewOperation;

  private QName m_serviceQName;
  private QName m_portTypeQName;

  private String m_alias;
  private boolean m_createBindingFile;
  private String m_wsdlFileName;

  public WsConsumerNewWizard(IScoutBundle bundle) {
    m_bundle = bundle;
    m_copyOperations = new ExternalFileCopyOperation[0];
    setWindowTitle(Texts.get("CreateWsConsumer"));
  }

  @Override
  public void addPages() {
    // WSDL selection
    m_wsdlLocationWizardPage = new WsdlLocationWizardPage(m_bundle);
    m_wsdlLocationWizardPage.setTitle(Texts.get("CreateWsConsumer"));
    m_wsdlLocationWizardPage.setWsdlFolderVisible(true);
    m_wsdlLocationWizardPage.setWsdlFolder(JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_CONSUMER, false)); // initial value
    m_wsdlLocationWizardPage.setRootWsdlFolder(JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_CONSUMER, false));
    m_wsdlLocationWizardPage.addPropertyChangeListener(new P_WsdlLocationPropertyListener());
    addPage(m_wsdlLocationWizardPage);

    // WS properties of existing WSDL Wizard Page
    m_wsPropertiesExistingWsdlWizardPage = new WsPropertiesExistingWsdlWizardPage(m_bundle, WebserviceEnum.Consumer);
    m_wsPropertiesExistingWsdlWizardPage.setTitle(Texts.get("CreateWsConsumer"));
    m_wsPropertiesExistingWsdlWizardPage.addPropertyChangeListener(new P_WsPropertiesPropertyListener());
    addPage(m_wsPropertiesExistingWsdlWizardPage);

    // WS Stub
    m_wsStubWizardPage = new WsStubWizardPage(m_bundle);
    m_wsStubWizardPage.setTitle(Texts.get("CreateWsConsumer"));
    addPage(m_wsStubWizardPage);

    // Implementing class
    m_wsConsumerImplClassWizardPage = new WsConsumerImplClassWizardPage(m_bundle);
    m_wsConsumerImplClassWizardPage.setTitle(Texts.get("CreateWsConsumer"));
    addPage(m_wsConsumerImplClassWizardPage);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    IFolder wsdlFolder = m_wsdlLocationWizardPage.getWsdlFolder();
    m_alias = m_wsConsumerImplClassWizardPage.getTypeName();
    File wsdlFile = m_wsdlLocationWizardPage.getWsdlFile();
    m_wsdlFileName = wsdlFile.getName();

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

    // prepare buildJaxWs.xml operation
    m_buildJaxWsEntryCreateOperation = new BuildJaxWsEntryCreateOperation(WebserviceEnum.Consumer);
    m_buildJaxWsEntryCreateOperation.setBundle(m_bundle);
    m_buildJaxWsEntryCreateOperation.setAlias(m_alias);
    m_buildJaxWsEntryCreateOperation.setWsdlProjectRelativePath(wsdlFolder.getProjectRelativePath().append(wsdlFile.getName()));

    m_createBindingFile = m_wsStubWizardPage.isCreateBindingFile();

    // build properties and binding file
    Map<String, List<String>> buildProperties = JaxWsSdkUtility.getDefaultBuildProperties();

    // custom stub package
    Definition wsdlDefinition = m_wsdlLocationWizardPage.getWsdlDefinition();
    String defaultPackageName = JaxWsSdkUtility.resolveStubPackageName(null, wsdlDefinition);

    if (!CompareUtility.equals(m_wsStubWizardPage.getPackageName(), defaultPackageName)) {
      JaxWsSdkUtility.addBuildProperty(buildProperties, JaxWsConstants.OPTION_PACKAGE, m_wsStubWizardPage.getPackageName());
    }
    m_buildJaxWsEntryCreateOperation.setBuildProperties(buildProperties);

    m_stubGenerationOperation = new WsStubGenerationOperation();
    m_stubGenerationOperation.setBundle(m_bundle);
    m_stubGenerationOperation.setAlias(m_alias);
    m_stubGenerationOperation.setWsdlFolder(wsdlFolder);
    m_stubGenerationOperation.setProperties(buildProperties);
    m_stubGenerationOperation.setWsdlFileName(wsdlFile.getName());
    m_stubGenerationOperation.addOperationFinishedListener(new P_StubGenerationFinishedListener());

    m_wsConsumerImplNewOperation = new WsConsumerImplNewOperation("I" + m_wsConsumerImplClassWizardPage.getTypeName(), m_wsConsumerImplClassWizardPage.getTypeName());
    m_wsConsumerImplNewOperation.setImplementationProject(m_bundle.getJavaProject());
    m_wsConsumerImplNewOperation.addServiceRegistration(new ServiceRegistrationDescription(m_bundle.getJavaProject()));
    m_wsConsumerImplNewOperation.setImplementationPackageName(m_wsConsumerImplClassWizardPage.getPackageName());
    m_wsConsumerImplNewOperation.setCreateScoutWebServiceAnnotation(m_wsConsumerImplClassWizardPage.isAnnotateImplClass());
    m_wsConsumerImplNewOperation.setAuthenticationHandlerQName(m_wsConsumerImplClassWizardPage.getAuthenticationHandler());

    m_portTypeQName = m_wsPropertiesExistingWsdlWizardPage.getPortType().getQName();
    if (m_wsPropertiesExistingWsdlWizardPage.getService() != null) {
      m_serviceQName = m_wsPropertiesExistingWsdlWizardPage.getService().getQName();
    }
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    JaxWsSdk.getDefault().getMarkerQueueManager().suspend();
    try {
      for (ExternalFileCopyOperation op : m_copyOperations) {
        op.validate();
        op.run(monitor, workingCopyManager);
      }

      // create binding file(s)
      if (m_createBindingFile) {
        Map<String, List<String>> buildProperties = m_buildJaxWsEntryCreateOperation.getBuildProperties();

        // get WSDL resource to determine associated XML schemas
        IFile wsdlFile = JaxWsSdkUtility.getFile(m_bundle, m_buildJaxWsEntryCreateOperation.getWsdlProjectRelativePath(), false);
        WsdlResource wsdlResource = new WsdlResource(m_bundle);
        wsdlResource.setFile(wsdlFile);

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
          if (candidate.getWsdlArtifact().getTypeEnum() == TypeEnum.ReferencedWsdl) {
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

      m_stubGenerationOperation.validate();
      m_stubGenerationOperation.run(monitor, workingCopyManager);
      TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(JaxWsRuntimeClasses.AbstractWebServiceClient)).invalidate();

      IFile stubJarFile = JaxWsSdkUtility.getStubJarFile(m_bundle, m_buildJaxWsEntryCreateOperation.getBuildProperties(), m_wsdlFileName);
      IType portTypeInterfaceType = JaxWsSdkUtility.resolvePortTypeInterfaceType(m_portTypeQName, stubJarFile);

      // wait for stub JAR to be part of classpth
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

      IType serviceType = JaxWsSdkUtility.resolveServiceType(m_serviceQName, stubJarFile);
      m_wsConsumerImplNewOperation.setJaxWsPortType(portTypeInterfaceType);
      m_wsConsumerImplNewOperation.setJaxWsServiceType(serviceType);

      m_wsConsumerImplNewOperation.validate();
      m_wsConsumerImplNewOperation.run(monitor, workingCopyManager);

      String fqnWebserviceClient = m_wsConsumerImplNewOperation.getCreatedServiceImplementation().getFullyQualifiedName();

      // wait for port type to be part of the hierarchy
      maxWaitLoops = 10;
      while (!TypeUtility.exists(TypeUtility.getType(fqnWebserviceClient)) && maxWaitLoops > 0) {
        try {
          Thread.sleep(500);
        }
        catch (InterruptedException e) {
          JaxWsSdk.logError(e);
        }
        maxWaitLoops--;
      }

      m_buildJaxWsEntryCreateOperation.validate();
      m_buildJaxWsEntryCreateOperation.run(monitor, workingCopyManager);
      return true;
    }
    catch (Exception e) {
      JaxWsSdk.logError(e);
      return false;
    }
    finally {
      JaxWsSdk.getDefault().getMarkerQueueManager().resume();
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

  private class P_WsPropertiesPropertyListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (WsPropertiesExistingWsdlWizardPage.PROP_PORT_TYPE.equals(event.getPropertyName())) {
        String portTypeName = null;
        PortType portType = (PortType) event.getNewValue();
        if (portType != null) {
          portTypeName = portType.getQName().getLocalPart();
          m_wsConsumerImplClassWizardPage.setTypeName(JaxWsSdkUtility.getPlainPortTypeName(portTypeName));
        }
        else {
          m_wsConsumerImplClassWizardPage.setTypeName(null);
        }
      }
    }
  }

  private class P_StubGenerationFinishedListener implements IOperationFinishedListener {

    @Override
    public void operationFinished(boolean success, Throwable exception) {
      // nop
    }
  }
}
