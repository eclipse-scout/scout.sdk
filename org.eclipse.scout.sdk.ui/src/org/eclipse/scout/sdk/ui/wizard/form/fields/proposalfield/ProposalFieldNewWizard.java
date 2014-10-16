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
package org.eclipse.scout.sdk.ui.wizard.form.fields.proposalfield;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.AbstractInnerTypeWizard;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.form.fields.smartfield.SmartFieldNewWizardPage;
import org.eclipse.ui.IWorkbench;

public class ProposalFieldNewWizard extends AbstractInnerTypeWizard {

  private SmartFieldNewWizardPage m_page1;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("NewProposalField"));

    m_page1 = new SmartFieldNewWizardPage(getDeclaringType(), Texts.get("NewProposalField"), Texts.get("CreateANewProposalField"), RuntimeClasses.getSuperType(IRuntimeClasses.IProposalField, getDeclaringType().getJavaProject()));
    if (getSiblingProposal() != null) {
      m_page1.setSibling(getSiblingProposal());
    }
    if (getTypeName() != null) {
      m_page1.setTypeName(getTypeName());
    }
    if (getSuperType() != null) {
      m_page1.setSuperType(getSuperType());
    }
    addPage(m_page1);
  }

  @Override
  protected void postFinishDisplayThread() {
    ScoutSdkUi.showJavaElementInEditor(m_page1.getCreatedField(), false);
  }
}
