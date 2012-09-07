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
package org.eclipse.scout.sdk.ws.jaxws.marker.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.JaxWsServletRegistrationWizard;

public class JaxWsServletRegistrationCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;
  private SunJaxWsBean m_sunJaxWsBean;

  /**
   * To fix JAX-WS servlet registration
   * 
   * @param bundle
   */
  public JaxWsServletRegistrationCommand(IScoutBundle bundle) {
    super("Missing or invalid JAX-WS Servlet registration");
    m_bundle = bundle;
    setSolutionDescription("Use this task to fix the JAX-WS servlet registration");
  }

  /**
   * To fix JAX-WS servlet registration and URL pattern of given sunJaxWs entry
   * 
   * @param bundle
   * @param sunJaxWsBean
   */
  public JaxWsServletRegistrationCommand(IScoutBundle bundle, SunJaxWsBean sunJaxWsBean) {
    super("Missing or invalid URL pattern");
    m_bundle = bundle;
    m_sunJaxWsBean = sunJaxWsBean;
    setSolutionDescription("Use this task to fix the URL pattern and/or change the JAX-WS servlet registration");
  }

  @Override
  public boolean prepareForUi() throws CoreException {
    JaxWsServletRegistrationWizard wizard;
    if (m_sunJaxWsBean != null) {
      wizard = new JaxWsServletRegistrationWizard(m_bundle, m_sunJaxWsBean);
    }
    else {
      wizard = new JaxWsServletRegistrationWizard(m_bundle);
    }
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setPageSize(650, 200);
    return wizardDialog.open() == Window.OK;
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // operation executed within wizard
  }
}
