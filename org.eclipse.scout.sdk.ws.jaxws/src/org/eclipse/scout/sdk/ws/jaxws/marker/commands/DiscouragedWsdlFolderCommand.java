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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.WsFileMoveWizard;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;

public class DiscouragedWsdlFolderCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;
  private SunJaxWsBean m_sunJaxWsBean;
  private BuildJaxWsBean m_buildJaxWsBean;
  private IFolder m_newFolder;
  private String m_markerGroupUUID;
  private WebserviceEnum m_webserviceEnum;

  /**
   * Used for webservice providers
   * 
   * @param bundle
   * @param markerGroupUUID
   * @param buildJaxWsBean
   * @param sunJaxWsBean
   */
  public DiscouragedWsdlFolderCommand(IScoutBundle bundle, String markerGroupUUID, BuildJaxWsBean buildJaxWsBean, SunJaxWsBean sunJaxWsBean) {
    super("Discouraged WSDL folder");
    m_bundle = bundle;
    m_newFolder = JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_PROVIDER, false);
    m_markerGroupUUID = markerGroupUUID;
    m_buildJaxWsBean = buildJaxWsBean;
    m_sunJaxWsBean = sunJaxWsBean;
    m_webserviceEnum = WebserviceEnum.PROVIDER;
    setSolutionDescription(Texts.get("DescriptionTaskMoveFiles", m_newFolder.getProjectRelativePath().toString()));
  }

  /**
   * Used for webservice consumers
   * 
   * @param bundle
   * @param markerGroupUUID
   * @param buildJaxWsBean
   */
  public DiscouragedWsdlFolderCommand(IScoutBundle bundle, String markerGroupUUID, BuildJaxWsBean buildJaxWsBean) {
    super("Discouraged WSDL folder");
    m_bundle = bundle;
    m_newFolder = JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_CONSUMER, false);
    m_markerGroupUUID = markerGroupUUID;
    m_buildJaxWsBean = buildJaxWsBean;
    m_webserviceEnum = WebserviceEnum.CONSUMER;
    setSolutionDescription("By using this task, a new WSDL file is created.");
  }

  @Override
  public boolean prepareForUi() throws CoreException {
    WsFileMoveWizard wizard = new WsFileMoveWizard();
    wizard.setBundle(m_bundle);
    wizard.setWebserviceEnum(m_webserviceEnum);
    wizard.setBuildJaxWsBean(m_buildJaxWsBean);
    wizard.setSunJaxWsBean(m_sunJaxWsBean);
    wizard.setDestination(m_newFolder);
    wizard.setMarkerGroupUUID(m_markerGroupUUID);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    return (wizardDialog.open() == SWT.OK);
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // operation executed within wizard
  }
}
