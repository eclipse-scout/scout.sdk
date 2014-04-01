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
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

public class HandlerChainNewAction extends AbstractLinkAction {

  private SunJaxWsBean m_sunJaxWsBean;
  private IScoutBundle m_bundle;

  public HandlerChainNewAction() {
    super(Texts.get("HandlerChainNewAction"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd));
    setLinkText(Texts.get("HandlerChainNewAction"));
    setToolTip(Texts.get("TooltipHanderChainNew"));
  }

  public void init(IScoutBundle bundle, SunJaxWsBean sunJaxWsBean) {
    m_sunJaxWsBean = sunJaxWsBean;
    m_bundle = bundle;
  }

  @Override
  public boolean isVisible() {
    return !m_bundle.isBinary();
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    String handlerChainsQName = toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAINS);
    Element xmlHandlerChains = JaxWsSdkUtility.getChildElement(m_sunJaxWsBean.getXml().getChildNodes(), handlerChainsQName);
    if (xmlHandlerChains == null) {
      xmlHandlerChains = m_sunJaxWsBean.getXml().getOwnerDocument().createElementNS(SunJaxWsBean.NS_HANDLER_CHAINS, handlerChainsQName);
      m_sunJaxWsBean.getXml().appendChild(xmlHandlerChains);
    }

    Element xmlHandlerChain = xmlHandlerChains.getOwnerDocument().createElement(toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAIN));
    xmlHandlerChains.appendChild(xmlHandlerChain);

    // persist
    ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(xmlHandlerChain.getOwnerDocument(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED, m_sunJaxWsBean.getAlias());
    return null;
  }

  private String toQualifiedName(String elementName) {
    return m_sunJaxWsBean.toQualifiedName(elementName);
  }
}
