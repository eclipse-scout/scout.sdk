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
import org.eclipse.scout.commons.XmlUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MissingBuildJaxWsEntryCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;
  private String m_alias;
  private WebserviceEnum m_webserviceEnum;

  public MissingBuildJaxWsEntryCommand(IScoutBundle bundle, String alias, WebserviceEnum webserviceEnum) {
    super("Missing entry in " + JaxWsConstants.PATH_BUILD_JAXWS);
    m_bundle = bundle;
    m_alias = alias;
    m_webserviceEnum = webserviceEnum;
    setSolutionDescription("By using this task, the missing entry is created for the webservice '" + alias + "'.");
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    Document xmlDocument = ResourceFactory.getBuildJaxWsResource(m_bundle).loadXml();

    Element xml;
    if (m_webserviceEnum == WebserviceEnum.PROVIDER) {
      xml = XmlUtility.getFirstChildElement(xmlDocument.getDocumentElement(), BuildJaxWsBean.XML_PROVIDER);
    }
    else {
      xml = XmlUtility.getFirstChildElement(xmlDocument.getDocumentElement(), BuildJaxWsBean.XML_CONSUMER);
    }
    BuildJaxWsBean bean = new BuildJaxWsBean(xml, m_webserviceEnum);
    bean.setAlias(m_alias);

    ResourceFactory.getBuildJaxWsResource(m_bundle).storeXml(bean.getXml().getOwnerDocument(), IResourceListener.EVENT_BUILDJAXWS_ENTRY_ADDED, monitor, m_alias);
  }
}
