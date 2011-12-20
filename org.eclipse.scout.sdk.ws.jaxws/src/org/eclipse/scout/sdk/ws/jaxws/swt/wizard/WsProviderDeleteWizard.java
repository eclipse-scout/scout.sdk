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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsProviderDeleteOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ElementBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ResourceSelectionWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.DefinitionBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SchemaBean;

public class WsProviderDeleteWizard extends AbstractWorkspaceWizard {

  private ResourceSelectionWizardPage m_wizardPage;

  private IScoutBundle m_bundle;
  private SunJaxWsBean m_sunJaxWsBean;
  private BuildJaxWsBean m_buildJaxWsBean;
  private Definition m_wsdlDefinition;

  private WsProviderDeleteOperation m_operation;

  public WsProviderDeleteWizard() {
    setWindowTitle(Texts.get("DeleteWebServiceProvider"));
  }

  @Override
  public void addPages() {
    m_wizardPage = new ResourceSelectionWizardPage(Texts.get("DeleteWebServiceProvider"), Texts.get("QuestionDeletion"));
    m_wizardPage.setElements(getElementsToBeDeleted());
    addPage(m_wizardPage);
  }

  private List<ElementBean> getElementsToBeDeleted() {
    List<ElementBean> elements = new LinkedList<ElementBean>();
    // registration
    elements.add(new ElementBean(WsProviderDeleteOperation.ID_REGISTRATION, "Registration entry in sun-jaxws.xml and build-jaxws.xml", JaxWsSdk.getImageDescriptor(JaxWsIcons.SunJaxWsXmlFile), true));

    // port type
    String fqn = m_sunJaxWsBean.getImplementation();
    if (TypeUtility.existsType(fqn)) {
      IType portType = TypeUtility.getType(fqn);
      elements.add(new ElementBean(WsProviderDeleteOperation.ID_IMPL_TYPE, "Port type '" + portType.getFullyQualifiedName() + "'", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), portType, false));
    }

    // stub JAR file
    IFile stubJarFile = JaxWsSdkUtility.getStubJarFile(m_bundle, m_buildJaxWsBean, m_sunJaxWsBean.getWsdl());
    if (stubJarFile != null && stubJarFile.exists()) {
      elements.add(new ElementBean(WsProviderDeleteOperation.ID_STUB, "Stub JAR file '" + stubJarFile.getFullPath().toPortableString() + "'", JaxWsSdk.getImageDescriptor(JaxWsIcons.Jar), stubJarFile, false));
    }

    // WSDL file
    IFile wsdlFile = null;
    if (StringUtility.hasText(m_sunJaxWsBean.getWsdl())) {
      wsdlFile = JaxWsSdkUtility.getFile(m_bundle, m_sunJaxWsBean.getWsdl(), false);
    }
    if (wsdlFile != null && wsdlFile.exists()) {
      elements.add(new ElementBean(WsProviderDeleteOperation.ID_WSDL_FILE, "WSDL file '" + wsdlFile.getProjectRelativePath().toPortableString() + "'", JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), wsdlFile, false));
    }

    IPath wsdlFolderPath = null;
    if (wsdlFile != null) {
      wsdlFolderPath = wsdlFile.getProjectRelativePath().removeLastSegments(1);
    }

    // referenced WSDL files
    if (m_wsdlDefinition != null && wsdlFolderPath != null) {
      DefinitionBean[] relatedWsdlDefinitions = JaxWsSdkUtility.getRelatedDefinitions(m_bundle, wsdlFolderPath, m_wsdlDefinition);
      for (DefinitionBean relatedWsdlDefinition : relatedWsdlDefinitions) {
        IFile file = relatedWsdlDefinition.getFile();
        if (file != null && file.exists()) {
          elements.add(new ElementBean(WsProviderDeleteOperation.ID_REF_WSDL, "Referenced WSDL file '" + file.getProjectRelativePath().toPortableString() + "'", JaxWsSdk.getImageDescriptor(JaxWsIcons.WsdlFile), file, false));
        }
      }
    }
    // referenced XSD schemas
    if (wsdlFile != null && wsdlFile.exists() && wsdlFolderPath != null) {
      WsdlResource wsdlResource = new WsdlResource(m_bundle);
      wsdlResource.setFile(wsdlFile);

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
            elements.add(new ElementBean(WsProviderDeleteOperation.ID_REF_XSD, "Referenced XSD schema '" + file.getProjectRelativePath().toPortableString() + "'", JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), file, false));
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
              elements.add(new ElementBean(WsProviderDeleteOperation.ID_REF_XSD, "Referenced XSD schema '" + file.getProjectRelativePath().toPortableString() + "'", JaxWsSdk.getImageDescriptor(JaxWsIcons.XsdSchema), file, false));
            }
          }
        }
      }
    }

    // binding file
    if (m_buildJaxWsBean != null) {
      IFile[] bindingFiles = JaxWsSdkUtility.getBindingFiles(m_bundle, m_buildJaxWsBean.getPropertiers());
      for (IFile bindingFile : bindingFiles) {
        if (bindingFile != null && bindingFile.exists()) {
          elements.add(new ElementBean(WsProviderDeleteOperation.ID_BINDING_FILE, "Binding file '" + bindingFile.getProjectRelativePath().toPortableString() + "'", JaxWsSdk.getImageDescriptor(JaxWsIcons.BindingFile), bindingFile, false));
        }
      }
    }

    return elements;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_operation = new WsProviderDeleteOperation();
    m_operation.setBundle(m_bundle);
    m_operation.setSunJaxWsBean(m_sunJaxWsBean);
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

  public Definition getWsdlDefinition() {
    return m_wsdlDefinition;
  }

  public void setWsdlDefinition(Definition wsdlDefinition) {
    m_wsdlDefinition = wsdlDefinition;
  }
}
