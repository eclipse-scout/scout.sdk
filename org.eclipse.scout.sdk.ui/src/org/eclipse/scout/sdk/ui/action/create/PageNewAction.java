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

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageWithTableNodePage;
import org.eclipse.scout.sdk.ui.wizard.page.PageNewWizard;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class PageNewAction extends AbstractWizardAction {
  private IType m_type;
  private IScoutBundle m_scoutBundle;

  public PageNewAction() {
    super(Texts.get("Action_newTypeX", "Page"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PageAdd), null, false, Category.NEW);
  }

  @Override
  public boolean isVisible() {
    boolean isEditable = !m_scoutBundle.isBinary();
    if (getType() == null) return isEditable;
    return isEditable && !TypeUtility.exists(TypeUtility.getMethod(getType(), PageWithTableNodePage.METHOD_EXEC_CREATE_CHILD_PAGE));
  }

  public IType getType() {
    return m_type;
  }

  public void init(IScoutBundle scoutBundle) {
    init(scoutBundle, null);
  }

  public void init(IScoutBundle scoutBundle, IType type) {
    m_type = type;
    m_scoutBundle = scoutBundle;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    PageNewWizard p = new PageNewWizard(m_scoutBundle);
    if (m_type != null) {
      p.setHolderType(m_type);
    }
    return p;
  }
}
