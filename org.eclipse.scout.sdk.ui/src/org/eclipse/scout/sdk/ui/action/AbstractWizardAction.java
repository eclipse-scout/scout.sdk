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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.wizard.ScoutWizardDialog;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractWizardAction extends AbstractScoutHandler {
  public AbstractWizardAction(String label) {
    this(label, null);
  }

  public AbstractWizardAction(String label, ImageDescriptor image) {
    this(label, image, null);
  }

  public AbstractWizardAction(String label, ImageDescriptor image, String keyStroke) {
    this(label, image, keyStroke, false);
  }

  public AbstractWizardAction(String label, ImageDescriptor image, String keyStroke, boolean multiSelectSupported) {
    this(label, image, keyStroke, multiSelectSupported, null);
  }

  public AbstractWizardAction(String label, ImageDescriptor image, String keyStroke, boolean multiSelectSupported, Category cat) {
    super(label, image, keyStroke, multiSelectSupported, cat);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    ScoutWizardDialog wizardDialog = new ScoutWizardDialog(getNewWizardInstance());
    wizardDialog.open();
    return null;
  }

  /**
   * Return a new wizard instance to show for the current action. used to ensure the wizard is only created, when the
   * menu is pressed.
   * 
   * @return
   */
  protected abstract IWizard getNewWizardInstance();
}
