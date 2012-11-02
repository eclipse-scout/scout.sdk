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
package org.eclipse.scout.sdk.ui.action.create;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.wizard.newproject.ScoutProjectNewWizard;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>ScoutProjectNewAction</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 10.03.2010
 */
public class ScoutProjectNewAction extends AbstractScoutHandler {

  public ScoutProjectNewAction() {
    super(Texts.get("NewScoutProject"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ScoutProjectAdd), "ALT+SHIFT+N", false, Category.NEW);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    ScoutProjectNewWizard newProjectWizard = new ScoutProjectNewWizard();
    newProjectWizard.init(PlatformUI.getWorkbench(), null);
    WizardDialog dialog = new WizardDialog(shell, newProjectWizard);
    dialog.open();
    return null;
  }
}
