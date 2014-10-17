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
package org.eclipse.scout.sdk.ui.executor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.wizard.ScoutWizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link AbstractWizardExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 08.10.2014
 */
public abstract class AbstractWizardExecutor extends AbstractExecutor {
  @Override
  public final Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    INewWizard newWizardInstance = getNewWizardInstance();
    if (newWizardInstance != null) {
      newWizardInstance.init(PlatformUI.getWorkbench(), selection);
      ScoutWizardDialog wizardDialog = new ScoutWizardDialog(shell, newWizardInstance);
      wizardDialog.open();
    }
    return null;
  }

  /**
   * Return a new wizard instance to show for the current action. used to ensure the wizard is only created, when the
   * menu is pressed.
   *
   * @return
   */
  public abstract INewWizard getNewWizardInstance();
}
