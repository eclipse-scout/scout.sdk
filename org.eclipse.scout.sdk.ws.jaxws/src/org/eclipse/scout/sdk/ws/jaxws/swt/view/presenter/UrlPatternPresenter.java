/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.JaxWsServletRegistrationOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.JaxWsServletRegistrationWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class UrlPatternPresenter extends AbstractStringActionPresenter {

  private SunJaxWsBean m_sunJaxWsBean;

  public UrlPatternPresenter(Composite parent, FormToolkit toolkit) {
    super(parent, toolkit);
    setActionLinkTooltip(Texts.get("ChangeUrlOfWsdl"));
    setActionLinkEnabled(false);
  }

  @Override
  protected void execAction() throws CoreException {
    P_Wizard wizard = new P_Wizard(m_bundle);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setPageSize(650, 410);
    wizardDialog.open();
  }

  public void setSunJaxWsBean(SunJaxWsBean sunJaxWsBean) {
    m_sunJaxWsBean = sunJaxWsBean;
    setActionLinkEnabled(m_sunJaxWsBean != null);
  }

  private class P_Wizard extends AbstractWorkspaceWizard {

    private IScoutBundle m_bundle;
    private JaxWsServletRegistrationWizardPage m_wizardPage;
    private JaxWsServletRegistrationOperation m_operation;

    public P_Wizard(IScoutBundle bundle) {
      m_bundle = bundle;
      m_operation = new JaxWsServletRegistrationOperation();
      setWindowTitle(Texts.get("EndpointPublishConfiguration"));
    }

    @Override
    public void addPages() {
      m_wizardPage = new JaxWsServletRegistrationWizardPage(m_bundle, true);
      m_wizardPage.setTitle(Texts.get("SpecifyUrlOfWsdl"));
      m_wizardPage.setUrlPattern(getValue());
      m_wizardPage.initializeDefaultValues(m_bundle);
      addPage(m_wizardPage);
    }

    @Override
    protected boolean beforeFinish() throws CoreException {
      m_operation.setBundle(m_bundle);
      m_operation.setRegistrationBundle(m_wizardPage.getRegistrationBundle());
      m_operation.setJaxWsAlias(m_wizardPage.getAlias());
      m_operation.setSunJaxWsBean(m_sunJaxWsBean);
      m_operation.setUrlPattern(m_wizardPage.getUrlPattern());
      return true;
    }

    @Override
    protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      JaxWsSdk.getDefault().getMarkerQueueManager().suspend();
      try {
        m_operation.run(monitor, workingCopyManager);
      }
      finally {
        JaxWsSdk.getDefault().getMarkerQueueManager().resume();
      }
      return true;
    }
  }

}
