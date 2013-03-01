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
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.WsProviderNewWizard;
import org.eclipse.swt.widgets.Shell;

public class ProviderNewWizardAction extends AbstractLinkAction {

  private IScoutBundle m_bundle;

  public ProviderNewWizardAction() {
    super(Texts.get("Action_newTypeX", Texts.get("Provider")), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd));
    setLeadingText(Texts.get("CreateNewWsProviderByClicking"));
    setLinkText(Texts.get("here"));
  }

  public void init(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  @Override
  public boolean isVisible() {
    return !m_bundle.isBinary();
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    IWizard wizard = new WsProviderNewWizard(m_bundle);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setHelpAvailable(false);
    wizardDialog.setPageSize(680, 410);
    wizardDialog.open();
    return null;
  }
}
