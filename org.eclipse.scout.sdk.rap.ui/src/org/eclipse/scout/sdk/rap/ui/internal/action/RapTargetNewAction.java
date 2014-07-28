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
package org.eclipse.scout.sdk.rap.ui.internal.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.rap.IScoutSdkRapConstants;
import org.eclipse.scout.sdk.rap.ui.internal.wizard.var.RapTargetNewWizard;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link RapTargetNewAction}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 15.04.2013
 */
public class RapTargetNewAction extends AbstractScoutHandler {
  public RapTargetNewAction() {
    super(Texts.get("CreateNewRAPTarget") + "...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd), null, false, Category.NEW);
  }

  @Override
  public boolean isVisible() {
    return Platform.getBundle(IScoutSdkRapConstants.ScoutRapTargetPlugin) != null;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    WizardDialog dialog = new WizardDialog(shell, new RapTargetNewWizard());
    dialog.open();
    return null;
  }
}
