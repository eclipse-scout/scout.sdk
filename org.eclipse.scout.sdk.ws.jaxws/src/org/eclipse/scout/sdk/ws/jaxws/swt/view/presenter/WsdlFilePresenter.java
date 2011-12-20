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
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.WsdlLocationWizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class WsdlFilePresenter extends FilePresenter {

  private BuildJaxWsBean m_buildJaxWsBean;

  public WsdlFilePresenter(Composite parent, FormToolkit toolkit) {
    super(parent, toolkit);
    setLabel(Texts.get("WsdlFile"));
    setUseLinkAsLabel(true);
    setFileExtension("wsdl");
    setFileDirectory(JaxWsConstants.PATH_WSDL);
  }

  @Override
  protected IFile execBrowseAction() {
    // browse for WSDL file or create new one
    WsdlLocationWizard wizard = new WsdlLocationWizard(m_bundle, m_buildJaxWsBean);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setPageSize(650, 350);
    wizardDialog.open();
    // as wizard operation is asynchronously, the presenter's value is updated by setting a new value with {@link AbstractPropertyPresenter#setInput(Object)}
    return null;
  }

  public BuildJaxWsBean getBuildJaxWsBean() {
    return m_buildJaxWsBean;
  }

  public void setBuildJaxWsBean(BuildJaxWsBean buildJaxWsBean) {
    m_buildJaxWsBean = buildJaxWsBean;
  }
}
