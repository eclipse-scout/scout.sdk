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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsFilesMoveOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceConsumerNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ElementBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ResourceSelectionWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.IFileHandle;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.PathNormalizer;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaArtifactVisitor;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaImportArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaIncludeArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtifact;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WsFileMoveWizard extends AbstractWorkspaceWizard {

  private ResourceSelectionWizardPage m_wizardPage;

  private IScoutBundle m_bundle;
  private SunJaxWsBean m_sunJaxWsBean;
  private BuildJaxWsBean m_buildJaxWsBean;
  private WebserviceEnum m_webserviceEnum;
  private IFolder m_destination;
  private String m_markerGroupUUID;

  private WsFilesMoveOperation m_operation;

  public WsFileMoveWizard() {
    setWindowTitle(Texts.get("MovingFiles"));
  }

  @Override
  public void addPages() {
    m_wizardPage = new ResourceSelectionWizardPage(Texts.get("QuestionMoveFiles", m_destination.getProjectRelativePath().toString()), Texts.get("QuestionMove"));
    m_wizardPage.setElements(getElementsToBeMoved());
    addPage(m_wizardPage);
  }

  private List<ElementBean> getElementsToBeMoved() {
    final List<ElementBean> elements = new LinkedList<ElementBean>();
    // WSDL file
    IFile wsdlFile = null;
    if (m_webserviceEnum == WebserviceEnum.Provider) {
      if (m_sunJaxWsBean != null && StringUtility.hasText(m_sunJaxWsBean.getWsdl())) {
        wsdlFile = JaxWsSdkUtility.getFile(m_bundle, new Path(m_sunJaxWsBean.getWsdl()), false);
      }
    }
    else {
      if (m_buildJaxWsBean != null && StringUtility.hasText(m_buildJaxWsBean.getWsdl())) {
        wsdlFile = JaxWsSdkUtility.getFile(m_bundle, new Path(m_buildJaxWsBean.getWsdl()), false);
      }
    }
    if (wsdlFile == null || !wsdlFile.exists()) {
      return elements;
    }

    // registration in sun-jaxws.xml (provider) / build-jaxws.xml (consumer)
    if (m_webserviceEnum == WebserviceEnum.Provider) {
      elements.add(new ElementBean(WsFilesMoveOperation.ID_WSDL_SUNJAXWS_REGISTRATION, String.format("Change WSDL file registration in sun-jaxws.xml to '%s'", m_destination.getProjectRelativePath().append(wsdlFile.getName())), JaxWsSdk.getImageDescriptor(JaxWsIcons.SunJaxWsXmlFile), true));
    }
    else {
      elements.add(new ElementBean(WsFilesMoveOperation.ID_WSDL_BUILDJAXWS_REGISTRATION, String.format("Change WSDL file registration in build-jaxws.xml to '%s'", m_destination.getProjectRelativePath().append(wsdlFile.getName())), JaxWsSdk.getImageDescriptor(JaxWsIcons.BuildJaxWsXmlFile), true));
    }
    elements.add(new ElementBean(WsFilesMoveOperation.ID_WSDL_FILE, Texts.get("MoveXToY", "WSDL file", m_destination.getProjectRelativePath().append(wsdlFile.getName()).toString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), wsdlFile, true));

    SchemaUtility.visitArtifacts(wsdlFile, new SchemaArtifactVisitor<IFile>() {

      @Override
      public void onReferencedWsdlArtifact(WsdlArtifact<IFile> wsdlArtifact) {
        IFileHandle<IFile> fileHandle = wsdlArtifact.getFileHandle();
        if (fileHandle != null && fileHandle.exists()) {
          elements.add(new ElementBean(WsFilesMoveOperation.ID_REF_WSDL, Texts.get("MoveReferencedXToY", "WSDL file", m_destination.getProjectRelativePath().append(fileHandle.getName()).toString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), fileHandle.getFile(), false));
        }
      }

      @Override
      public void onSchemaIncludeArtifact(SchemaIncludeArtifact<IFile> schemaIncludeArtifact) {
        IFileHandle<IFile> fileHandle = schemaIncludeArtifact.getFileHandle();
        if (fileHandle != null && fileHandle.exists()) {
          elements.add(new ElementBean(WsFilesMoveOperation.ID_REF_XSD, Texts.get("MoveReferencedXToY", "XSD schema (included)", m_destination.getProjectRelativePath().append(fileHandle.getName()).toString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), fileHandle.getFile(), false));
        }
      }

      @Override
      public void onSchemaImportArtifact(SchemaImportArtifact<IFile> schemaImportArtifact) {
        IFileHandle<IFile> fileHandle = schemaImportArtifact.getFileHandle();
        if (fileHandle != null && fileHandle.exists()) {
          elements.add(new ElementBean(WsFilesMoveOperation.ID_REF_XSD, Texts.get("MoveReferencedXToY", "XSD schema (imported)", m_destination.getProjectRelativePath().append(fileHandle.getName()).toString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), fileHandle.getFile(), false));
        }
      }
    });

    // WSDL location in binding files
    IFile[] bindingFiles = JaxWsSdkUtility.getBindingFiles(m_bundle, m_buildJaxWsBean.getPropertiers());
    for (IFile bindingFile : bindingFiles) {
      XmlResource xmlResource = new XmlResource(m_bundle);
      xmlResource.setFile(bindingFile);
      Document xmlDocument = xmlResource.loadXml();
      String namespacePrefix = JaxWsSdkUtility.getXmlPrefix(xmlDocument.getDocumentElement());
      Element xmlBindings = JaxWsSdkUtility.getChildElement(xmlDocument.getDocumentElement().getChildNodes(), StringUtility.join(":", namespacePrefix, "bindings"));
      if (xmlBindings != null && xmlBindings.hasAttribute("wsdlLocation")) {
        String wsdlLocation = xmlBindings.getAttribute("wsdlLocation");
        String schemaDefiningFileName = new Path(wsdlLocation).toFile().getName();
        IPath relativeWsdlFolderPath = m_destination.getProjectRelativePath().makeRelativeTo(JaxWsSdkUtility.getParentFolder(m_bundle, bindingFile).getProjectRelativePath());
        String newWsdlLocation = PathNormalizer.toWsdlPath(relativeWsdlFolderPath.append(schemaDefiningFileName).toString());
        ElementBean elementBean = new ElementBean(WsFilesMoveOperation.ID_BINDING_FILE, "Change attribute 'wsdlLocation' in binding file '" + bindingFile.getName() + "' to '" + newWsdlLocation + "'", JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), bindingFile, false);
        elementBean.setData(newWsdlLocation);
        elements.add(elementBean);
      }
    }
    return elements;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_operation = new WsFilesMoveOperation();
    m_operation.setBundle(m_bundle);
    m_operation.setBuildJaxWsBean(m_buildJaxWsBean);
    m_operation.setSunJaxWsBean(m_sunJaxWsBean);
    m_operation.setDestination(m_destination);

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
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    m_operation.run(monitor, workingCopyManager);
    if (m_webserviceEnum == WebserviceEnum.Provider) {
      JaxWsSdk.getDefault().notifyPageReload(WebServiceProviderNodePage.class, m_markerGroupUUID, WebServiceProviderNodePage.DATA_WSDL_FILE | WebServiceProviderNodePage.DATA_SUN_JAXWS_ENTRY | WebServiceProviderNodePage.DATA_BINDING_FILE);
    }
    else {
      JaxWsSdk.getDefault().notifyPageReload(WebServiceProviderNodePage.class, m_markerGroupUUID, WebServiceConsumerNodePage.DATA_WSDL_FILE | WebServiceConsumerNodePage.DATA_BUILD_JAXWS_ENTRY | WebServiceConsumerNodePage.DATA_BINDING_FILE);
    }
    return true;
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public SunJaxWsBean getSunJaxWsBean() {
    return m_sunJaxWsBean;
  }

  public void setSunJaxWsBean(SunJaxWsBean sunJaxWsBean) {
    m_sunJaxWsBean = sunJaxWsBean;
  }

  public BuildJaxWsBean getBuildJaxWsBean() {
    return m_buildJaxWsBean;
  }

  public void setBuildJaxWsBean(BuildJaxWsBean buildJaxWsBean) {
    m_buildJaxWsBean = buildJaxWsBean;
  }

  public WebserviceEnum getWebserviceEnum() {
    return m_webserviceEnum;
  }

  public void setWebserviceEnum(WebserviceEnum webserviceEnum) {
    m_webserviceEnum = webserviceEnum;
  }

  public IFolder getDestination() {
    return m_destination;
  }

  public void setDestination(IFolder destination) {
    m_destination = destination;
  }

  public String getMarkerGroupUUID() {
    return m_markerGroupUUID;
  }

  public void setMarkerGroupUUID(String markerGroupUUID) {
    m_markerGroupUUID = markerGroupUUID;
  }
}
