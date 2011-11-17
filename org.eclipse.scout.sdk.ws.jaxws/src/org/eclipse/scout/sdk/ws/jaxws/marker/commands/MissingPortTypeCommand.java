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
package org.eclipse.scout.sdk.ws.jaxws.marker.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.operation.WsProviderImplNewOperation;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderCodeFirstNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderNodePage;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class MissingPortTypeCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;
  private SunJaxWsBean m_sunJaxWsBean;
  private IType m_portTypeInterfaceType;
  private String m_markerGroupUUID;
  private String m_fqnPortType;

  public MissingPortTypeCommand(IScoutBundle bundle, String markerGroupUUID, SunJaxWsBean sunJaxWsBean, IType portTypeInterfaceType) {
    super("Missing or invalid port type");
    m_bundle = bundle;
    m_portTypeInterfaceType = portTypeInterfaceType;
    m_sunJaxWsBean = sunJaxWsBean;
    m_markerGroupUUID = markerGroupUUID;

    String portType = m_sunJaxWsBean.getImplementation();
    if (portType == null) {
      String name = m_sunJaxWsBean.getAlias();
      if (!name.toLowerCase().endsWith("service") ||
          !name.toLowerCase().endsWith("webservice")) {
        name += "WebService";
      }
      m_fqnPortType = StringUtility.join(".", JaxWsSdkUtility.getRecommendedProviderImplPackageName(m_bundle), name);
    }
    else {
      m_fqnPortType = portType;
    }

    setSolutionDescription("By using this task, the port type '" + m_fqnPortType + "' is created'.");
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    WsProviderImplNewOperation op = new WsProviderImplNewOperation();
    op.setBundle(m_bundle);
    op.setPackageName(Signature.getQualifier(m_fqnPortType));
    op.setTypeName(Signature.getSimpleName(m_fqnPortType));
    op.setCreateScoutWebServiceAnnotation(true);
    op.setSessionFactoryQName(JaxWsRuntimeClasses.DefaultServerSessionFactory.getFullyQualifiedName());
    op.setAuthenticationHandlerQName(JaxWsRuntimeClasses.BasicAuthenticationHandlerProvider.getFullyQualifiedName());
    op.setCredentialValidationStrategyQName(JaxWsRuntimeClasses.ConfigIniCredentialValidationStrategy.getFullyQualifiedName());
    op.setPortTypeInterfaceType(m_portTypeInterfaceType);
    op.validate();
    op.run(monitor, workingCopyManager);

    m_sunJaxWsBean.setImplementation(m_fqnPortType);
    XmlResource sunJaxWsResource = ResourceFactory.getSunJaxWsResource(m_bundle);
    sunJaxWsResource.storeXmlAsync(m_sunJaxWsBean.getXml().getDocument(), m_sunJaxWsBean.getAlias(), IResourceListener.EVENT_SUNJAXWS_URL_PATTERN_CHANGED);

    JaxWsSdk.getDefault().notifyPageReload(WebServiceProviderNodePage.class, m_markerGroupUUID, WebServiceProviderNodePage.DATA_JDT_TYPE);
    JaxWsSdk.getDefault().notifyPageReload(WebServiceProviderCodeFirstNodePage.class, m_markerGroupUUID, WebServiceProviderCodeFirstNodePage.DATA_JDT_TYPE);
  }
}
