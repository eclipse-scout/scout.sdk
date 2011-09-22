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
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.util.ScoutSourceUtilities;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

public class FormFieldRenameAction extends AbstractRenameAction {

  private IType m_formField;

  @Override
  protected void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException {
    // find getter
    transaction.add(getFormField(), newName);
    IMethod getter = SdkTypeUtility.getFormFieldGetterMethod(getFormField());
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
    try {
      if (ScoutSourceUtilities.findInnerType(getFormField().getCompilationUnit().getAllTypes()[0], newName, true) != null) {
        return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Name already in use.");
      }
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("error during finding already existing types in form.", e);
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Exception in validation (see logfile).");
    }
    return inheritedStatus;
  }

  public IType getFormField() {
    return m_formField;
  }

  public void setFormField(IType formField) {
    m_formField = formField;
  }
}
