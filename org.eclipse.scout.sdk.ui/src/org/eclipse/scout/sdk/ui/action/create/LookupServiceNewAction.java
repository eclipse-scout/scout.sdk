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

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.wizard.services.LookupServiceNewWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class LookupServiceNewAction extends AbstractWizardAction {

  private IScoutBundle m_bundle;

  public LookupServiceNewAction() {
    super(Texts.get("Action_newTypeX", "Lookup Service"), JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CLASS), null, false, Category.NEW);
  }

  @Override
  public boolean isVisible() {
    return !m_bundle.isBinary();
  }

  public void setScoutBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    return new LookupServiceNewWizard(m_bundle);
  }
}
