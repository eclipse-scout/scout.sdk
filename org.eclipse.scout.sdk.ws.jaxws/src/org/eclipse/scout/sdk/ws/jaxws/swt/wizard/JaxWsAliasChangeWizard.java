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
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.JaxWsServletRegistrationOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.JaxWsAliasChangeWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class JaxWsAliasChangeWizard extends AbstractWorkspaceWizard {

  private IScoutBundle m_bundle;
  private JaxWsAliasChangeWizardPage m_page;

  private JaxWsServletRegistrationOperation m_operation;

  public JaxWsAliasChangeWizard(IScoutBundle bundle) {
    m_bundle = bundle;
    setWindowTitle(Texts.get("JaxWsAlias"));
  }

  @Override
  public void addPages() {
    m_page = new JaxWsAliasChangeWizardPage();
    m_page.setTitle(Texts.get("JaxWsAlias"));
    m_page.setJaxWsAlias(JaxWsSdkUtility.getJaxWsAlias(m_bundle));
    addPage(m_page);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_operation = new JaxWsServletRegistrationOperation();
    m_operation.setBundle(m_bundle);
    m_operation.setJaxWsAlias(m_page.getJaxWsAlias());
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
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
