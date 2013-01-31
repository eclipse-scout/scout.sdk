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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsdlCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsPropertiesNewWsdlWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.util.PathNormalizer;

public class WsdlNewWizard extends AbstractWorkspaceWizard {

  private IScoutBundle m_bundle;
  // used for WS provider
  private SunJaxWsBean m_sunJaxWsBean;
  // used for WS consumer
  private BuildJaxWsBean m_buildJaxWsBean;
  private WsdlResource m_wsdlResource;
  private WsPropertiesNewWsdlWizardPage m_wizardPage;
  private String m_alias;

  private WsdlCreateOperation m_operation;

  /**
   * Used for WS provider
   * 
   * @param bundle
   * @param sunJaxWsBean
   * @param wsdlResource
   */
  public WsdlNewWizard(IScoutBundle bundle, SunJaxWsBean sunJaxWsBean, WsdlResource wsdlResource) {
    m_bundle = bundle;
    m_sunJaxWsBean = sunJaxWsBean;
    m_wsdlResource = wsdlResource;
    m_alias = m_sunJaxWsBean.getAlias();
    setWindowTitle(Texts.get("CreateWsdlFile"));
  }

  /**
   * Used for WS consumer
   * 
   * @param bundle
   * @param buildJaxWsBean
   * @param wsdlResource
   */
  public WsdlNewWizard(IScoutBundle bundle, BuildJaxWsBean buildJaxWsBean, WsdlResource wsdlResource) {
    m_bundle = bundle;
    m_buildJaxWsBean = buildJaxWsBean;
    m_wsdlResource = wsdlResource;
    m_alias = m_buildJaxWsBean.getAlias();
    setWindowTitle(Texts.get("CreateWsdlFile"));
  }

  @Override
  public void addPages() {
    m_wizardPage = new WsPropertiesNewWsdlWizardPage(m_bundle);
    m_wizardPage.setShowOnlyWsdlProperties(true);
    m_wizardPage.setAlias(m_alias);
    m_wizardPage.setWsdlName(m_wsdlResource.getFile().getName());
    addPage(m_wizardPage);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    String targetNamespace = PathNormalizer.toTargetNamespace(m_wizardPage.getTargetNamespace());// TODO dwi verify

    m_operation = new WsdlCreateOperation();
    m_operation.setBundle(m_bundle);
    m_operation.setAlias(m_alias);
    m_operation.setWsdlResource(m_wsdlResource);
    m_operation.setTargetNamespace(targetNamespace);
    m_operation.setService(m_wizardPage.getServiceName());
    m_operation.setPortName(m_wizardPage.getPortName());
    m_operation.setPortType(m_wizardPage.getPortTypeName());
    m_operation.setBinding(m_wizardPage.getBinding());
    m_operation.setUrlPattern(m_wizardPage.getUrlPattern());
    m_operation.setServiceOperationName(m_wizardPage.getServiceOperationName());

    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    m_operation.validate();
    m_operation.run(monitor, workingCopyManager);

    if (m_sunJaxWsBean != null) {
      // update entry in sunJaxWs.xml
      m_sunJaxWsBean.setWsdl(PathNormalizer.toWsdlPath(m_wsdlResource.getFile().getProjectRelativePath().toString())); // TODO dwi verify
      ResourceFactory.getSunJaxWsResource(m_bundle).storeXml(m_sunJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_SUNJAXWS_WSDL_CHANGED, monitor, m_sunJaxWsBean.getAlias());
    }
    else {
      // update entry in buildJaxWs.xml
      m_buildJaxWsBean.setWsdl(PathNormalizer.toWsdlPath(m_wsdlResource.getFile().getProjectRelativePath().toString())); // TODO dwi verify
      ResourceFactory.getBuildJaxWsResource(m_bundle).storeXml(m_buildJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_BUILDJAXWS_WSDL_CHANGED, monitor, m_buildJaxWsBean.getAlias());
    }
    return true;
  }
}
