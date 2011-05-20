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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.AbstractFormFieldWizard;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

public class FormHandlerNewWizard extends AbstractFormFieldWizard {

  private FormHandlerNewWizardPage1 m_page1;
  private FormHandlerNewWizardPage2 m_page2;

  public FormHandlerNewWizard(IType formType) {
    setWindowTitle("New Form Handler");
    initWizard(formType);

  }

  @Override
  public void initWizard(IType declaringType) {
    super.initWizard(declaringType);
    m_page1 = new FormHandlerNewWizardPage1(getDeclaringType());
    addPage(m_page1);
    m_page2 = new FormHandlerNewWizardPage2(getDeclaringType());
    addPage(m_page2);
  }

  @Override
  public void setSuperType(IType superType) {
    m_page1.setSuperType(superType);
  }

  @Override
  protected void postFinishDisplayThread() {
    IType createdField = m_page2.getCreatedFormHandler();
    if (TypeUtility.exists(createdField)) {
      ScoutSdkUi.showJavaElementInEditor(createdField, false);
    }
  }
}
