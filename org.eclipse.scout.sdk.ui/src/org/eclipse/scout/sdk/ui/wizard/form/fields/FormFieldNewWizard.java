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
package org.eclipse.scout.sdk.ui.wizard.form.fields;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.extensions.AbstractFormFieldWizard;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;

public class FormFieldNewWizard extends AbstractFormFieldWizard {

  private FormFieldSelectionWizardPage m_page1;

  public FormFieldNewWizard(IType declaringType) {
    super();
    initWizard(declaringType);
  }

  @Override
  public void initWizard(IType declaringType) {
    super.initWizard(declaringType);
    m_page1 = new FormFieldSelectionWizardPage(declaringType);
    addPage(m_page1);
  }

  @Override
  public boolean needsPreviousAndNextButtons() {
    return true;
  }

  public FormFieldSelectionWizardPage getFormFieldSelectionWizardPage() {
    return m_page1;
  }

  @Override
  public boolean canFinish() {
    return false;
  }

  @Override
  public void setSuperType(IType superType) {
    // void
  }

  @Override
  public void setTypeName(String name) {
    // void
  }

  @Override
  public void setSibling(SiblingProposal sibling) {
    // void
  }
}
