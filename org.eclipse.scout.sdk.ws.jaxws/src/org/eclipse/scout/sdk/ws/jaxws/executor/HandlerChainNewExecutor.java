/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.executor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.XmlUtility;
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderHandlerNodePage;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

/**
 * <h3>{@link HandlerChainNewExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class HandlerChainNewExecutor extends AbstractExecutor {

  private IScoutBundle m_bundle;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_bundle = UiUtility.getScoutBundleFromSelection(selection);
    return isEditable(m_bundle);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    SunJaxWsBean sunJaxWsBean = null;
    Object el = selection.getFirstElement();
    if (el instanceof WebServiceProviderHandlerNodePage) {
      sunJaxWsBean = ((WebServiceProviderHandlerNodePage) el).getSunJaxWsBean();
    }
    else {
      return null;
    }

    String handlerChainsQName = sunJaxWsBean.toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAINS);
    Element xmlHandlerChains = XmlUtility.getFirstChildElement(sunJaxWsBean.getXml(), handlerChainsQName);
    if (xmlHandlerChains == null) {
      xmlHandlerChains = sunJaxWsBean.getXml().getOwnerDocument().createElementNS(SunJaxWsBean.NS_HANDLER_CHAINS, handlerChainsQName);
      sunJaxWsBean.getXml().appendChild(xmlHandlerChains);
    }

    Element xmlHandlerChain = xmlHandlerChains.getOwnerDocument().createElement(sunJaxWsBean.toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAIN));
    xmlHandlerChains.appendChild(xmlHandlerChain);

    // persist
    ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(xmlHandlerChain.getOwnerDocument(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED, sunJaxWsBean.getAlias());

    return null;
  }
}
