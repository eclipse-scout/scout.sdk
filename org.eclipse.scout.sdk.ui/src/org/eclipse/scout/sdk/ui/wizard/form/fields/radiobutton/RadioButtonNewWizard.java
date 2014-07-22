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
package org.eclipse.scout.sdk.ui.wizard.form.fields.radiobutton;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.AbstractFormFieldWizard;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public class RadioButtonNewWizard extends AbstractFormFieldWizard {

  private RadioButtonNewWizardPage m_page1;

  public RadioButtonNewWizard() {
    setWindowTitle(Texts.get("NewRadioButtonNoPopup"));
  }

  @Override
  public void initWizard(IType declaringType) {
    m_page1 = new RadioButtonNewWizardPage(declaringType);
    addPage(m_page1);
  }

  @Override
  public void setSuperType(IType superType) {
    m_page1.setSuperType(superType);
  }

  @Override
  public void setTypeName(String name) {
    m_page1.setTypeName(name);
  }

  @Override
  public void setSibling(SiblingProposal sibling) {
    m_page1.setSibling(sibling);
  }

  @Override
  protected void postFinishDisplayThread() {
    IType createdField = m_page1.getCreatedRadioButton();
    if (TypeUtility.exists(createdField)) {
      ScoutSdkUi.showJavaElementInEditor(createdField, false);
    }
  }
}
