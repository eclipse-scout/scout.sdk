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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsConsumerDeleteOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ElementBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ResourceSelectionWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.util.IFileHandle;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaArtefactVisitor;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaImportArtefact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaIncludeArtefact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtefact;

public class WsConsumerDeleteWizard extends AbstractWorkspaceWizard {

  private ResourceSelectionWizardPage m_wizardPage;

  private IScoutBundle m_bundle;
  private IType m_type;
  private BuildJaxWsBean m_buildJaxWsBean;

  private WsConsumerDeleteOperation m_operation;

  public WsConsumerDeleteWizard() {
    setWindowTitle(Texts.get("DeleteWebserviceConsumer"));
  }

  @Override
  public void addPages() {
    m_wizardPage = new ResourceSelectionWizardPage(Texts.get("DeleteWebserviceConsumer"), Texts.get("QuestionDeletion"));
    m_wizardPage.setElements(getElementsToBeDeleted());
    addPage(m_wizardPage);
  }

  private List<ElementBean> getElementsToBeDeleted() {
    final List<ElementBean> elements = new LinkedList<ElementBean>();
    // registration
    elements.add(new ElementBean(WsConsumerDeleteOperation.ID_REGISTRATION, "Registration entry in build-jaxws.xml", JaxWsSdk.getImageDescriptor(JaxWsIcons.BuildJaxWsXmlFile), true));

    // service registration
    elements.add(new ElementBean(WsConsumerDeleteOperation.ID_SERVICE_REGISTRATION, "Service registration in plugin.xml of webservice client '" + m_type.getFullyQualifiedName() + "'", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServiceLocator), m_type, true));

    // type
    elements.add(new ElementBean(WsConsumerDeleteOperation.ID_TYPE, "Webservice client type '" + m_type.getFullyQualifiedName() + "'", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), m_type, true));

    // stub JAR file
    String wsdlFileName = null;
    if (m_buildJaxWsBean != null) {
      wsdlFileName = m_buildJaxWsBean.getWsdl();
    }

    IFile stubJarFile = JaxWsSdkUtility.getStubJarFile(m_bundle, m_buildJaxWsBean, wsdlFileName);
    if (stubJarFile != null && stubJarFile.exists()) {
      elements.add(new ElementBean(WsConsumerDeleteOperation.ID_STUB, "Stub JAR file '" + stubJarFile.getFullPath().toString() + "'", JaxWsSdk.getImageDescriptor(JaxWsIcons.Jar), stubJarFile, false));
    }

    // WSDL file
    IFile wsdlFile = null;
    if (m_buildJaxWsBean != null && StringUtility.hasText(m_buildJaxWsBean.getWsdl())) {
      wsdlFile = JaxWsSdkUtility.getFile(m_bundle, new Path(m_buildJaxWsBean.getWsdl()), false);
    }
    if (wsdlFile != null && wsdlFile.exists()) {
      elements.add(new ElementBean(WsConsumerDeleteOperation.ID_WSDL_FILE, "WSDL file '" + wsdlFile.getProjectRelativePath().toString() + "'", JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), wsdlFile, false));
    }

    SchemaUtility.visitArtefacts(wsdlFile, new SchemaArtefactVisitor<IFile>() {

      @Override
      public void onReferencedWsdlArtefact(WsdlArtefact<IFile> wsdlArtefact) {
        IFileHandle<IFile> fileHandle = wsdlArtefact.getFileHandle();
        if (fileHandle != null && fileHandle.exists()) {
          elements.add(new ElementBean(WsConsumerDeleteOperation.ID_REF_WSDL, String.format("Referenced WSDL file '%s'", fileHandle.getFullPath().toString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), fileHandle.getFile(), false));
        }
      }

      @Override
      public void onSchemaIncludeArtefact(SchemaIncludeArtefact<IFile> schemaIncludeArtefact) {
        IFileHandle<IFile> fileHandle = schemaIncludeArtefact.getFileHandle();
        if (fileHandle != null && fileHandle.exists()) {
          elements.add(new ElementBean(WsConsumerDeleteOperation.ID_REF_XSD, String.format("Included XSD schema '%s'", fileHandle.getFullPath().toString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), fileHandle.getFile(), false));
        }
      }

      @Override
      public void onSchemaImportArtefact(SchemaImportArtefact<IFile> schemaImportArtefact) {
        IFileHandle<IFile> fileHandle = schemaImportArtefact.getFileHandle();
        if (fileHandle != null && fileHandle.exists()) {
          elements.add(new ElementBean(WsConsumerDeleteOperation.ID_REF_XSD, String.format("Imported XSD schema '%s'", fileHandle.getFullPath().toString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), fileHandle.getFile(), false));
        }
      }

    });

    // binding file
    if (m_buildJaxWsBean != null) {
      IFile[] bindingFiles = JaxWsSdkUtility.getBindingFiles(m_bundle, m_buildJaxWsBean.getPropertiers());
      for (IFile bindingFile : bindingFiles) {
        if (bindingFile != null && bindingFile.exists()) {
          elements.add(new ElementBean(WsConsumerDeleteOperation.ID_BINDING_FILE, String.format("Binding file '%s'", bindingFile.getProjectRelativePath().toString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.BindingFile), bindingFile, false));
        }
      }
    }

    return elements;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_operation = new WsConsumerDeleteOperation();
    m_operation.setBundle(m_bundle);
    m_operation.setBuildJaxWsBean(m_buildJaxWsBean);

    List<ElementBean> elements = new LinkedList<ElementBean>();
    for (ElementBean element : m_wizardPage.getElements()) {
      if (element.isChecked() || element.isMandatory()) {
        elements.add(element);
      }
    }
    m_operation.setElements(elements);
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    OperationJob job = new OperationJob(m_operation);
    job.schedule();
    return true;
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public IType getType() {
    return m_type;
  }

  public void setType(IType type) {
    m_type = type;
  }

  public BuildJaxWsBean getBuildJaxWsBean() {
    return m_buildJaxWsBean;
  }

  public void setBuildJaxWsBean(BuildJaxWsBean buildJaxWsBean) {
    m_buildJaxWsBean = buildJaxWsBean;
  }
}
