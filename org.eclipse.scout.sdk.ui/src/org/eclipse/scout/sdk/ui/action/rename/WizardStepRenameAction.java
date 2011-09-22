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
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

public class WizardStepRenameAction extends AbstractRenameAction {

  private IType m_wizardStep;

  public WizardStepRenameAction() {
    setReadOnlySuffix(ScoutIdeProperties.SUFFIX_WIZARD_STEP);
  }

  @Override
  protected void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException {
    // find getter
    transaction.add(getWizardStep(), newName);
    IMethod getter = SdkTypeUtility.getWizardStepGetterMethod(getWizardStep());
    if (TypeUtility.exists(getter)) {
      transaction.add(getter, "get" + Character.toUpperCase(newName.charAt(0)) + newName.substring(1));
    }
  }

  @Override
  protected IStatus validate(String newName) {
    IStatus inheritedStatus = getJavaNameStatus(newName);
    if (inheritedStatus.matches(IStatus.ERROR)) {
      return inheritedStatus;
    }
    if (TypeUtility.exists(getWizardStep().getDeclaringType().getType(newName))) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Name already in use.");
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
