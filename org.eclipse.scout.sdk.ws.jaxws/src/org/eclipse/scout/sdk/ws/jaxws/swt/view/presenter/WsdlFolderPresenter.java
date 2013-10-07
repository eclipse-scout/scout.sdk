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
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import org.eclipse.core.resources.IFolder;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.WsFileMoveWizard;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WsdlFolderViewerFilter;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class WsdlFolderPresenter extends FolderPresenter {

  private WebserviceEnum m_webserviceEnum;
  private SunJaxWsBean m_sunJaxWsBean;
  private BuildJaxWsBean m_buildJaxWsBean;

  public WsdlFolderPresenter(Composite parent, PropertyViewFormToolkit toolkit, WebserviceEnum webserviceEnum) {
    super(parent, toolkit, DEFAULT_LABEL_WIDTH, false);
    m_webserviceEnum = webserviceEnum;
    callInitializer();
  }

  @Override
  protected String getConfiguredBrowseButtonLabel() {
    return Texts.get("Change");
  }

  @Override
  protected IFolder execBrowseAction() {
    IFolder rootFolder = null;
    if (m_webserviceEnum == WebserviceEnum.Provider) {
      rootFolder = JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_PROVIDER, true);
    }
    else {
      rootFolder = JaxWsSdkUtility.getFolder(m_bundle, JaxWsConstants.PATH_WSDL_CONSUMER, true);
    }

    IFolder folder = JaxWsSdkUtility.openProjectFolderDialog(
        m_bundle,
        new WsdlFolderViewerFilter(rootFolder),
        Texts.get("MovingFiles"),
        Texts.get("MoveWsdlFileAndArtefacts"),
        rootFolder,
        getValue());
    if (folder != null && (getValue() == null || !folder.getProjectRelativePath().equals(getValue().getProjectRelativePath()))) {
      WsFileMoveWizard wizard = new WsFileMoveWizard();
      wizard.setBundle(m_bundle);
      wizard.setWebserviceEnum(m_webserviceEnum);
      wizard.setBuildJaxWsBean(m_buildJaxWsBean);
      wizard.setSunJaxWsBean(m_sunJaxWsBean);
      wizard.setDestination(folder);
      wizard.setMarkerGroupUUID(getMarkerGroupUUID());
      ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
      if (wizardDialog.open() == SWT.OK) {
        return folder;
      }
    }
    return null;
  }

  public SunJaxWsBean getSunJaxWsBean() {
    return m_sunJaxWsBean;
  }

  public void setSunJaxWsBean(SunJaxWsBean sunJaxWsBean) {
    m_sunJaxWsBean = sunJaxWsBean;
    calculateAccessiblity();
  }

  public BuildJaxWsBean getBuildJaxWsBean() {
    return m_buildJaxWsBean;
  }

  public void setBuildJaxWsBean(BuildJaxWsBean buildJaxWsBean) {
    m_buildJaxWsBean = buildJaxWsBean;
    calculateAccessiblity();
  }

  private void calculateAccessiblity() {
    if (m_buildJaxWsBean == null) {
      setEnabled(false);
      return;
    }
    if (m_webserviceEnum == WebserviceEnum.Provider && m_sunJaxWsBean == null) {
      setEnabled(false);
      return;
    }
    setEnabled(true);
  }
}
