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
package org.eclipse.scout.sdk.ui.wizard.form.fields.buttonfield;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.AbstractFormFieldWizard;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public class ButtonFieldNewWizard extends AbstractFormFieldWizard {

  private ButtonFieldNewWizardPage m_page1;

  public ButtonFieldNewWizard() {
    setWindowTitle(Texts.get("NewButtonField"));
  }

  @Override
  public void initWizard(IType declaringType) {
    super.initWizard(declaringType);
    m_page1 = new ButtonFieldNewWizardPage(getDeclaringType());
    addPage(getButtonFieldWizardPage());
  }

  @Override
  public void setSuperType(IType superType) {
    getButtonFieldWizardPage().setSuperType(superType);
  }

  @Override
  protected void postFinishDisplayThread() {
    IType createdField = getButtonFieldWizardPage().getCreatedField();
    if (TypeUtility.exists(createdField)) {
      ScoutSdkUi.showJavaElementInEditor(createdField, false);
    }
  }

  /**
   * @return the page1
   */
  public ButtonFieldNewWizardPage getButtonFieldWizardPage() {
    return m_page1;
  }
}
