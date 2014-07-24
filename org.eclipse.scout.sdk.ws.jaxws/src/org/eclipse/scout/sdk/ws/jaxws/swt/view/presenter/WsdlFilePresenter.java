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

import org.eclipse.core.resources.IFile;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.WsdlLocationWizard;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.widgets.Composite;

public class WsdlFilePresenter extends FilePresenter {

  private BuildJaxWsBean m_buildJaxWsBean;
  private SunJaxWsBean m_sunJaxWsBean;

  public WsdlFilePresenter(IScoutBundle bundle, Composite parent, PropertyViewFormToolkit toolkit) {
    super(parent, toolkit);
    setBundle(bundle);
    setLabel(Texts.get("WsdlFile"));
    setUseLinkAsLabel(true);
    setFileExtension("wsdl");
    if (m_sunJaxWsBean != null) {
      setFileDirectory(JaxWsSdkUtility.getFolder(bundle, JaxWsConstants.PATH_WSDL_PROVIDER, false));
    }
    else {
      setFileDirectory(JaxWsSdkUtility.getFolder(bundle, JaxWsConstants.PATH_WSDL_CONSUMER, false));
    }
  }

  @Override
  protected IFile execBrowseAction() {
    // browse for WSDL file or create new one
    WsdlLocationWizard wizard = new WsdlLocationWizard(m_bundle, m_buildJaxWsBean, m_sunJaxWsBean);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setPageSize(650, 350);
    wizardDialog.open();
    // as wizard operation is asynchronously, the presenter's value is updated by setting a new value with AbstractPropertyPresenter#setInput(Object)
    return null;
  }

  @Override
  protected String getConfiguredBrowseButtonLabel() {
    return Texts.get("Change");
  }

  public BuildJaxWsBean getBuildJaxWsBean() {
    return m_buildJaxWsBean;
  }

  public void setBuildJaxWsBean(BuildJaxWsBean buildJaxWsBean) {
    m_buildJaxWsBean = buildJaxWsBean;
  }

  public SunJaxWsBean getSunJaxWsBean() {
    return m_sunJaxWsBean;
  }

  /**
   * Used by providers
   *
   * @param sunJaxWsBean
   */
  public void setSunJaxWsBean(SunJaxWsBean sunJaxWsBean) {
    m_sunJaxWsBean = sunJaxWsBean;
  }
}
