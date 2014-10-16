/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.executor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.WsProviderNewWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link ProviderNewWizardExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class ProviderNewWizardExecutor extends AbstractExecutor {

  @Override
  public boolean canRun(IStructuredSelection selection) {
    return isEditable(UiUtility.getScoutBundleFromSelection(selection));
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    WsProviderNewWizard wizard = new WsProviderNewWizard();
    wizard.init(PlatformUI.getWorkbench(), selection);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setHelpAvailable(false);
    wizardDialog.setPageSize(680, 410);
    wizardDialog.open();
    return null;
  }

}
