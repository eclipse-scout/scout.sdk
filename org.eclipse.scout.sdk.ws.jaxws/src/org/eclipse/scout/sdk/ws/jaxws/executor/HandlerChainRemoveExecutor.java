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
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.executor.param.HandlerParams;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;

/**
 * <h3>{@link HandlerChainRemoveExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class HandlerChainRemoveExecutor extends AbstractExecutor {

  private HandlerParams m_params;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    Object firstElement = selection.getFirstElement();
    if (firstElement instanceof HandlerParams) {
      m_params = (HandlerParams) firstElement;
    }
    return m_params != null && isEditable(m_params.getBundle());
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    SunJaxWsBean sunJaxWsBean = m_params.getSunJaxWsBean();
    Element xmlHandlerChain = m_params.getXmlHandlerChain();

    MessageBox messageBox = new MessageBox(ScoutSdkUi.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    messageBox.setMessage(Texts.get("QuestionRemoveHandlerChain"));
    if (messageBox.open() == SWT.YES) {

      Element xmlHandlerChains = JaxWsSdkUtility.getChildElement(sunJaxWsBean.getXml().getChildNodes(), sunJaxWsBean.toQualifiedName(SunJaxWsBean.XML_HANDLER_CHAINS));
      xmlHandlerChains.removeChild(xmlHandlerChain);

      // persist
      ResourceFactory.getSunJaxWsResource(m_params.getBundle()).storeXmlAsync(xmlHandlerChain.getOwnerDocument(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED, sunJaxWsBean.getAlias());
    }
    return null;
  }

}
