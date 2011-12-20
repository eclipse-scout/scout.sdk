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
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsFilesMoveOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceConsumerNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ElementBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ResourceSelectionWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.DefinitionBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SchemaBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SeparatorType;

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
    m_wizardPage = new ResourceSelectionWizardPage(Texts.get("QuestionMoveFiles", m_destination.getProjectRelativePath().toPortableString()), Texts.get("QuestionMove"));
    m_wizardPage.setElements(getElementsToBeMoved());
    addPage(m_wizardPage);
  }

  private List<ElementBean> getElementsToBeMoved() {
    List<ElementBean> elements = new LinkedList<ElementBean>();
    // WSDL file
    IFile wsdlFile = null;
    IPath wsdlFolderPath = null;
    WsdlResource wsdlResource = null;
    Definition wsdlDefinition = null;
    if (m_webserviceEnum == WebserviceEnum.Provider) {
      if (m_sunJaxWsBean != null && StringUtility.hasText(m_sunJaxWsBean.getWsdl())) {
        wsdlFile = JaxWsSdkUtility.getFile(m_bundle, m_sunJaxWsBean.getWsdl(), false);
      }
    }
    else {
      if (m_buildJaxWsBean != null && StringUtility.hasText(m_buildJaxWsBean.getWsdl())) {
        wsdlFile = JaxWsSdkUtility.getFile(m_bundle, m_buildJaxWsBean.getWsdl(), false);
      }
    }
    if (wsdlFile == null || !wsdlFile.exists()) {
      return elements;
    }

    wsdlFolderPath = wsdlFile.getProjectRelativePath().removeLastSegments(1);
    wsdlResource = new WsdlResource(m_bundle);
    wsdlResource.setFile(wsdlFile);
    wsdlDefinition = wsdlResource.loadWsdlDefinition();

    // registration in sun-jaxws.xml (provider) / build-jaxws.xml (consumer)
    if (m_webserviceEnum == WebserviceEnum.Provider) {
      elements.add(new ElementBean(WsFilesMoveOperation.ID_WSDL_SUNJAXWS_REGISTRATION, "Change WSDL file registration in sun-jaxws.xml to '" + m_destination.getProjectRelativePath().append(wsdlFile.getName()) + "'", JaxWsSdk.getImageDescriptor(JaxWsIcons.SunJaxWsXmlFile), true));
    }
    else {
      elements.add(new ElementBean(WsFilesMoveOperation.ID_WSDL_BUILDJAXWS_REGISTRATION, "Change WSDL file registration in build-jaxws.xml to '" + m_destination.getProjectRelativePath().append(wsdlFile.getName()) + "'", JaxWsSdk.getImageDescriptor(JaxWsIcons.BuildJaxWsXmlFile), true));
    }

    elements.add(new ElementBean(WsFilesMoveOperation.ID_WSDL_FILE, Texts.get("MoveXToY", "WSDL file", m_destination.getProjectRelativePath().append(wsdlFile.getName()).toPortableString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), wsdlFile, true));

    // referenced WSDL files
    if (wsdlDefinition != null && wsdlFolderPath != null) {
      DefinitionBean[] relatedWsdlDefinitions = JaxWsSdkUtility.getRelatedDefinitions(m_bundle, wsdlFolderPath, wsdlDefinition);
      for (DefinitionBean relatedWsdlDefinition : relatedWsdlDefinitions) {
        IFile file = relatedWsdlDefinition.getFile();
        if (file != null && file.exists()) {
          elements.add(new ElementBean(WsFilesMoveOperation.ID_REF_WSDL, Texts.get("MoveReferencedXToY", "WSDL file", m_destination.getProjectRelativePath().append(file.getName()).toPortableString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), file, false));
        }
      }
    }
    // referenced XSD schemas
    if (wsdlFolderPath != null && wsdlResource != null) {
      SchemaBean[] schemas = JaxWsSdkUtility.getAllSchemas(m_bundle, wsdlResource);
      for (SchemaBean schemaBean : schemas) {
        Schema schema = schemaBean.getSchema();

        if (schema == null) {
          continue;
        }
        // included schemas
        @SuppressWarnings("unchecked")
        List<SchemaReference> schemaReferences = schema.getIncludes();
        for (SchemaReference schemaReference : schemaReferences) {
          String location = schemaReference.getSchemaLocationURI();
          IFile file = JaxWsSdkUtility.getFile(m_bundle, wsdlFolderPath.append(location).toPortableString(), false);
          if (file != null && file.exists()) {
            elements.add(new ElementBean(WsFilesMoveOperation.ID_REF_XSD, Texts.get("MoveReferencedXToY", "XSD schema", m_destination.getProjectRelativePath().append(file.getName()).toPortableString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), file, false));
          }
        }

        // imported schemas
        @SuppressWarnings("unchecked")
        Map<String, List<SchemaImport>> schemaImports = schema.getImports();
        for (List<SchemaImport> schemaImportList : schemaImports.values()) {
          for (SchemaImport schemaImport : schemaImportList) {
            String location = schemaImport.getSchemaLocationURI();
            IFile file = JaxWsSdkUtility.getFile(m_bundle, wsdlFolderPath.append(location).toPortableString(), false);
            if (file != null && file.exists()) {
              elements.add(new ElementBean(WsFilesMoveOperation.ID_REF_XSD, Texts.get("MoveReferencedXToY", "XSD schema", m_destination.getProjectRelativePath().append(file.getName()).toPortableString()), JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), file, false));
            }
          }
        }
      }
    }

    // WSDL location in binding files
    IFile[] bindingFiles = JaxWsSdkUtility.getBindingFiles(m_bundle, m_buildJaxWsBean.getPropertiers());
    for (IFile bindingFile : bindingFiles) {
      XmlResource xmlResource = new XmlResource(m_bundle);
      xmlResource.setFile(bindingFile);
      ScoutXmlDocument xmlDocument = xmlResource.loadXml();
      String namespacePrefix = xmlDocument.getRoot().getNamePrefix();
      ScoutXmlElement xmlBindings = xmlDocument.getChild(StringUtility.join(":", namespacePrefix, "bindings"));
      if (xmlBindings != null && xmlBindings.hasAttribute("wsdlLocation")) {
        String wsdlLocation = xmlBindings.getAttribute("wsdlLocation");
        String schemaDefiningFileName = new Path(wsdlLocation).toFile().getName();
        IPath relativeWsdlFolderPath = m_destination.getProjectRelativePath().makeRelativeTo(JaxWsSdkUtility.getParentFolder(m_bundle, bindingFile).getProjectRelativePath());
        String newWsdlLocation = JaxWsSdkUtility.normalizePath(relativeWsdlFolderPath.toPortableString(), SeparatorType.TrailingType) + schemaDefiningFileName;
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
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
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
