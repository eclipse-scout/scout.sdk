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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class HandlerChainRemoveAction extends AbstractLinkAction {

  private SunJaxWsBean m_sunJaxWsBean;
  private ScoutXmlElement m_xmlHandlerChain;
  private IScoutBundle m_bundle;

  public HandlerChainRemoveAction() {
    super(Texts.get("RemoveChain"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolRemove));
    setLinkText(Texts.get("RemoveChain"));
    setToolTip(Texts.get("TooltipHandlerChainRemove"));
  }

  public void init(IScoutBundle bundle, SunJaxWsBean sunJaxWsBean, ScoutXmlElement xmlHandlerChain) {
    m_sunJaxWsBean = sunJaxWsBean;
    m_bundle = bundle;
    m_xmlHandlerChain = xmlHandlerChain;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    MessageBox messageBox = new MessageBox(ScoutSdkUi.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    messageBox.setMessage(Texts.get("QuestionRemoveHandlerChain"));
    if (messageBox.open() == SWT.YES) {
      ScoutXmlElement xmlHandlerChains = m_sunJaxWsBean.getXml().getChild(toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAINS));
      xmlHandlerChains.removeChild(m_xmlHandlerChain);

      // persist
      ResourceFactory.getSunJaxWsResource(m_bundle).storeXmlAsync(m_xmlHandlerChain.getDocument(), m_sunJaxWsBean.getAlias(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED);
    }
    return null;
  }

  private String toQualifiedName(String elementName) {
    return m_sunJaxWsBean.toQualifiedName(elementName);
  }
}
