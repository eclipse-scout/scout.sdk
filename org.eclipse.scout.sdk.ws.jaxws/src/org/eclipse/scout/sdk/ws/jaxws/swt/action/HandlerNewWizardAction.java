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
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.HandlerNewWizard;
import org.eclipse.swt.widgets.Shell;

public class HandlerNewWizardAction extends AbstractLinkAction {
  private int m_result;
  private IScoutBundle m_bundle;

  public HandlerNewWizardAction() {
    super(Texts.get("Action_newTypeX", Texts.get("Handler")), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd));
    setLeadingText(Texts.get("CreateNewHandlerByClicking"));
    setLinkText(Texts.get("here"));
  }

  public void init(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    IWizard wizard = new HandlerNewWizard(m_bundle);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setPageSize(680, 350);
    wizardDialog.setHelpAvailable(false);
    m_result = wizardDialog.open();
    return null;
  }

  public int getResult() {
    return m_result;
  }
}
