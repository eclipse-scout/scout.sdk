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
package org.eclipse.scout.nls.sdk.services.ui.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.nls.sdk.services.ui.page.TextServiceTablePage;
import org.eclipse.scout.nls.sdk.services.ui.wizard.NewNlsServiceWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.widgets.Shell;

public class TextProviderServiceNewAction extends AbstractWizardAction {

  private IScoutBundle m_bundle;

  public TextProviderServiceNewAction() {
    super(Texts.get("Action_newTypeX", "Text Provider Service"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TextAdd), null, false, Category.NEW);
  }

  @Override
  public boolean isVisible() {
    return !m_bundle.isBinary();
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    TextServiceTablePage page = (TextServiceTablePage) selection[0]; // size must be one -> no multi select allowed.
    m_bundle = page.getScoutResource();
    return super.execute(shell, selection, event);
  }

  @Override
  protected IWizard getNewWizardInstance() {
    return new NewNlsServiceWizard(m_bundle);
  }
}
