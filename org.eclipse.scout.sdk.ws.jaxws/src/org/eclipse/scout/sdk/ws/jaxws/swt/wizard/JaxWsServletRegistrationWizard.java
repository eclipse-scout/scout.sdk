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
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.JaxWsServletRegistrationOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.JaxWsServletRegistrationWizardPage;

public class JaxWsServletRegistrationWizard extends AbstractWorkspaceWizard {

  private IScoutBundle m_bundle;
  private JaxWsServletRegistrationWizardPage m_wizardPage;
  private JaxWsServletRegistrationOperation m_operation;

  private SunJaxWsBean m_sunJaxWsBean;

  /**
   * To change JAX-WS servlet registration
   * 
   * @param bundle
   */
  public JaxWsServletRegistrationWizard(IScoutBundle bundle) {
    m_bundle = bundle;
    m_operation = new JaxWsServletRegistrationOperation();
    setWindowTitle(Texts.get("JaxWsServletRegistration"));
  }

  /**
   * To change JAX-WS servlet registration and URL pattern of the given SunJaxWs entry
   * 
   * @param bundle
   * @param sunJaxWsBean
   */
  public JaxWsServletRegistrationWizard(IScoutBundle bundle, SunJaxWsBean sunJaxWsBean) {
    m_bundle = bundle;
    m_sunJaxWsBean = sunJaxWsBean;
    m_operation = new JaxWsServletRegistrationOperation();
    m_operation.setSunJaxWsBean(m_sunJaxWsBean);
    setWindowTitle(Texts.get("JaxWsServletRegistration"));
  }

  @Override
  public void addPages() {
    m_wizardPage = new JaxWsServletRegistrationWizardPage(m_bundle, m_sunJaxWsBean != null);
    m_wizardPage.setTitle(Texts.get("ChangeJaxWsServletRegistration"));
    if (m_sunJaxWsBean != null) {
      m_wizardPage.setUrlPattern(m_sunJaxWsBean.getUrlPattern());
      m_wizardPage.setTitle(Texts.get("SpecifyUrlOfWsdl"));
    }
    else {
      m_wizardPage.setTitle(Texts.get("ChangeJaxWsServletRegistration"));
    }
    m_wizardPage.initializeDefaultValues(m_bundle);
    addPage(m_wizardPage);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_operation.setBundle(m_bundle);
    m_operation.setRegistrationBundle(m_wizardPage.getRegistrationBundle());
    m_operation.setJaxWsAlias(m_wizardPage.getAlias());
    if (m_sunJaxWsBean != null) {
      m_operation.setSunJaxWsBean(m_sunJaxWsBean);
      m_operation.setUrlPattern(m_wizardPage.getUrlPattern());
    }
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
