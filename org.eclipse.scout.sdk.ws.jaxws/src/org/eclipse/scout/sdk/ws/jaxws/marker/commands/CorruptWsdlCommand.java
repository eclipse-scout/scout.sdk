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
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.WsdlNewWizard;

public class CorruptWsdlCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;
  /**
   * User for WS provider
   */
  private SunJaxWsBean m_sunJaxWsBean;
  /**
   * User for WS consumer
   */
  private BuildJaxWsBean m_buildJaxWsBean;
  private WsdlResource m_wsdlResource;

  private WsdlNewWizard m_wizard;

  public CorruptWsdlCommand(IScoutBundle bundle, WsdlResource wsdlResource, SunJaxWsBean sunJaxWsBean) {
    super("Corrupt WSDL file '" + wsdlResource.getFile().getName() + "'");
    setSolutionDescription("Failed to parse WSDL resource. Please ensure the WSDL file to be valid.\nBy using this task, the existing WSDL file is replaced by a new one.");
    m_bundle = bundle;
    m_sunJaxWsBean = sunJaxWsBean;
    m_wsdlResource = wsdlResource;
  }

  public CorruptWsdlCommand(IScoutBundle bundle, WsdlResource wsdlResource, BuildJaxWsBean buildJaxWsBean) {
    super("Corrupt WSDL file '" + wsdlResource.getFile().getName() + "'");
    setSolutionDescription("Failed to parse WSDL resource. Please ensure the WSDL file to be valid.\nBy using this task, the existing WSDL file is replaced by a new one.");
    m_bundle = bundle;
    m_buildJaxWsBean = buildJaxWsBean;
    m_wsdlResource = wsdlResource;
  }

  @Override
  public boolean prepareForUi() throws CoreException {
    if (m_sunJaxWsBean != null) {
      m_wizard = new WsdlNewWizard(m_bundle, m_sunJaxWsBean, m_wsdlResource);
    }
    else {
      m_wizard = new WsdlNewWizard(m_bundle, m_buildJaxWsBean, m_wsdlResource);
    }
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(m_wizard);
    // TODO DWI remove hardcoded sizes
    wizardDialog.setPageSize(650, 410);
    return wizardDialog.open() == Window.OK;
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // operation executed within wizard
  }
}
