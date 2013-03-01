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

public class HandlerAddAction extends AbstractLinkAction {

  private IScoutBundle m_bundle;

  private SunJaxWsBean m_sunJaxWsBean;
  private ScoutXmlElement m_xmlHandlerChain;

  public HandlerAddAction() {
    super(Texts.get("AddHandler"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd));
    setLinkText(Texts.get("AddHandler"));
    setToolTip(Texts.get("TooltipHandlerNew"));
  }

  @Override
  public boolean isVisible() {
    return !m_bundle.isBinary();
  }

  public void init(IScoutBundle bundle, SunJaxWsBean sunJaxWsBean, ScoutXmlElement xmlHandlerChain) {
    m_bundle = bundle;
    m_sunJaxWsBean = sunJaxWsBean;
    m_xmlHandlerChain = xmlHandlerChain;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    ScoutXmlElement xmlHandler = m_xmlHandlerChain.addChild();
    xmlHandler.setName(m_sunJaxWsBean.toQualifiedName("handler"));

    // persist
    ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(m_sunJaxWsBean.getXml().getDocument(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED, m_sunJaxWsBean.getAlias());
    return null;
  }
}
