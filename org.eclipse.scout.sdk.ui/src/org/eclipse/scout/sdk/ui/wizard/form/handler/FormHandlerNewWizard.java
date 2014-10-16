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
package org.eclipse.scout.sdk.ui.wizard.form.handler;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.AbstractInnerTypeWizard;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.ui.IWorkbench;

public class FormHandlerNewWizard extends AbstractInnerTypeWizard {

  private FormHandlerNewWizardPage1 m_page1;
  private FormHandlerNewWizardPage2 m_page2;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("NewFormHandler"));

    m_page1 = new FormHandlerNewWizardPage1(getDeclaringType());
    if (getSuperType() != null) {
      m_page1.setSuperType(getSuperType());
    }
    addPage(m_page1);
    m_page2 = new FormHandlerNewWizardPage2(getDeclaringType());
    if (getTypeName() != null) {
      m_page2.setTypeName(getTypeName());
    }
    addPage(m_page2);
  }

  @Override
  protected void postFinishDisplayThread() {
    ScoutSdkUi.showJavaElementInEditor(m_page2.getCreatedFormHandler(), false);
  }
}
