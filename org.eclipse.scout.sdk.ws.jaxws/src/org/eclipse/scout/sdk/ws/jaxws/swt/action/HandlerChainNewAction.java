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
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.swt.widgets.Shell;

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
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    String handlerChainsQName = toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAINS);
    ScoutXmlElement xmlHandlerChains = m_sunJaxWsBean.getXml().getChild(handlerChainsQName);
    if (xmlHandlerChains == null) {
      xmlHandlerChains = m_sunJaxWsBean.getXml().addChild(SunJaxWsBean.XML_HANDLER_CHAINS);
      xmlHandlerChains.setNamespace(m_sunJaxWsBean.getXml().getRoot().getNamePrefix(), SunJaxWsBean.NS_HANDLER_CHAINS);
      xmlHandlerChains.setName(handlerChainsQName);
    }
    ScoutXmlElement xmlHandlerChain = xmlHandlerChains.addChild();
    xmlHandlerChain.setName(toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAIN));

    // persist
    ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(xmlHandlerChain.getDocument(), m_sunJaxWsBean.getAlias(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED);
    return null;
  }

  private String toQualifiedName(String elementName) {
    return m_sunJaxWsBean.toQualifiedName(elementName);
  }
}
