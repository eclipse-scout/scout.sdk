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

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageWithTableNodePage;
import org.eclipse.scout.sdk.ui.wizard.page.PageLinkWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 *
 */
public class PageLinkAction extends AbstractWizardAction {
  private IType m_type;
  private IScoutBundle m_scoutBundle;

  public PageLinkAction() {
    super(Texts.get("AddExistingPage"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PageLink), null, false, Category.NEW);
  }

  @Override
  public boolean isVisible() {
    return !TypeUtility.exists(TypeUtility.getMethod(m_type, PageWithTableNodePage.METHOD_EXEC_CREATE_CHILD_PAGE));
  }

  public void init(IScoutBundle scoutBundle, IType type) {
    m_type = type;
    m_scoutBundle = scoutBundle;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    PageLinkWizard linkWizard = new PageLinkWizard(m_scoutBundle);
    linkWizard.setHolderType(m_type);
    linkWizard.setHolderEnabled(false);
    return linkWizard;
  }
}
