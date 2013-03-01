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
import org.eclipse.scout.sdk.ui.wizard.form.fields.groupbox.GroupBoxNewWizard;

/**
 *
 */
public class GroupBoxNewAction extends AbstractWizardAction {
  private IType m_type;

  public GroupBoxNewAction() {
    super(Texts.get("Action_newTypeX", "GroupBox"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TabboxTabAdd), null, false, Category.NEW);
  }

  public void setType(IType t) {
    m_type = t;
  }

  @Override
  public boolean isVisible() {
    return isEditable(m_type);
  }

  @Override
  protected IWizard getNewWizardInstance() {
    GroupBoxNewWizard wizard = new GroupBoxNewWizard();
    wizard.initWizard(m_type);
    return wizard;
  }
}
