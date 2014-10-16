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
package org.eclipse.scout.sdk.ui.wizard.keystroke;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.extensions.AbstractInnerTypeWizard;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.ui.IWorkbench;

public class KeyStrokeNewWizard extends AbstractInnerTypeWizard {

  private KeyStrokeNewWizardPage m_page1;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("NewKeyStroke"));

    m_page1 = new KeyStrokeNewWizardPage(getDeclaringType());
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
    ScoutSdkUi.showJavaElementInEditor(m_page1.getCreatedKeystroke(), false);
  }
}
