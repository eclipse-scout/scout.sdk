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
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.HandlerChainFilterWizard;
import org.eclipse.swt.widgets.Shell;

public class HandlerChainFilterEditAction extends AbstractLinkAction {

  private SunJaxWsBean m_sunJaxWsBean;
  private ScoutXmlElement m_xmlHandlerChain;
  private IScoutBundle m_bundle;

  public HandlerChainFilterEditAction() {
    super(Texts.get("EditFilter"), JaxWsSdk.getImageDescriptor(JaxWsIcons.Filter));
    setLinkText(Texts.get("EditFilter"));
    setToolTip(Texts.get("TooltipEditFilter"));
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
    HandlerChainFilterWizard wizard = new HandlerChainFilterWizard();
    wizard.init(m_bundle, m_sunJaxWsBean, m_xmlHandlerChain);

    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setHelpAvailable(false);
    wizardDialog.setPageSize(100, 350);
    wizardDialog.open();
    return null;
  }
}
