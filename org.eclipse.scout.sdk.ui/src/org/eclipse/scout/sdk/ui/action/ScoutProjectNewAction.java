/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.newproject.ScoutProjectNewWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>ScoutProjectNewAction</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 10.03.2010
 */
public class ScoutProjectNewAction extends Action {

  private final Shell m_shell;

  public ScoutProjectNewAction(Shell shell) {
    super("New Scout Project...");
    m_shell = shell;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_ADD));
  }

  @Override
  public void run() {
    ScoutProjectNewWizard newProjectWizard = new ScoutProjectNewWizard();
    newProjectWizard.init(PlatformUI.getWorkbench(), null);
    WizardDialog dialog = new WizardDialog(m_shell, newProjectWizard);
    dialog.open();
  }
}
