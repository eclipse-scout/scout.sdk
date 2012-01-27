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
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.BuildPropertiesWizard;
import org.eclipse.swt.widgets.Shell;

public class BuildPropertiesEditAction extends AbstractLinkAction {

  private BuildJaxWsBean m_buildJaxWsBean;
  private IScoutBundle m_bundle;

  public BuildPropertiesEditAction() {
    super(Texts.get("EditBuildProperties"), JaxWsSdk.getImageDescriptor(JaxWsIcons.BuildProperties));
    setLinkText(Texts.get("EditBuildProperties"));
    setToolTip(Texts.get("TooltipEditBuildProperties"));
  }

  public void init(IScoutBundle bundle, BuildJaxWsBean buildJaxWsBean) {
    m_buildJaxWsBean = buildJaxWsBean;
    m_bundle = bundle;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    BuildPropertiesWizard wizard = new BuildPropertiesWizard(m_bundle, m_buildJaxWsBean);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setHelpAvailable(false);
    wizardDialog.setPageSize(100, 500);
    wizardDialog.open();
    return null;
  }
}
