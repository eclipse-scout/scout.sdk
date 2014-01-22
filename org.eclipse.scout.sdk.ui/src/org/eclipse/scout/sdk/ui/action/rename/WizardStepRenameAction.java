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
package org.eclipse.scout.sdk.ui.action.rename;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

public class WizardStepRenameAction extends AbstractRenameAction {

  private IType m_wizardStep;

  public WizardStepRenameAction() {
    setReadOnlySuffix(SdkProperties.SUFFIX_WIZARD_STEP);
  }

  @Override
  protected void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException {
    // find getter
    transaction.add(getWizardStep(), newName);
    IMethod getter = ScoutTypeUtility.getWizardStepGetterMethod(getWizardStep());
    if (TypeUtility.exists(getter)) {
      transaction.add(getter, "get" + NamingUtility.ensureStartWithUpperCase(newName));
    }
  }

  @Override
  public boolean isVisible() {
    return isEditable(m_wizardStep);
  }

  @Override
  protected IStatus validate(String newName) {
    IStatus inheritedStatus = getJavaNameStatus(newName);
    if (inheritedStatus.matches(IStatus.ERROR)) {
      return inheritedStatus;
    }
    if (TypeUtility.exists(getWizardStep().getDeclaringType().getType(newName))) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    return inheritedStatus;
  }

  public IType getWizardStep() {
    return m_wizardStep;
  }

  public void setWizardStep(IType wizardStep) {
    m_wizardStep = wizardStep;
  }
}
