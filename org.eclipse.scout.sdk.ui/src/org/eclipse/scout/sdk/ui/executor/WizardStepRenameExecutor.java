/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.executor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link WizardStepRenameExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class WizardStepRenameExecutor extends AbstractRenameExecutor {

  private IType m_wizardStep;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    boolean superRun = super.canRun(selection);
    if (!superRun) {
      return false;
    }

    m_wizardStep = UiUtility.getTypeFromSelection(selection);

    return isEditable(m_wizardStep);
  }

  @Override
  protected void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException {
    transaction.add(m_wizardStep, newName);

    // find getter
    IMethod getter = ScoutTypeUtility.getWizardStepGetterMethod(m_wizardStep);
    if (TypeUtility.exists(getter)) {
      transaction.add(getter, "get" + NamingUtility.ensureStartWithUpperCase(newName));
    }
  }

  @Override
  protected IStatus validate(String newName) {
    IStatus inheritedStatus = ScoutUtility.validateJavaName(newName, getReadOnlySuffix());
    if (inheritedStatus.matches(IStatus.ERROR)) {
      return inheritedStatus;
    }
    if (TypeUtility.exists(m_wizardStep.getDeclaringType().getType(newName))) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    return inheritedStatus;
  }
}
