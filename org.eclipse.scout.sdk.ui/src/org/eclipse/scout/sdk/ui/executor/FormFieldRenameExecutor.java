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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link FormFieldRenameExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class FormFieldRenameExecutor extends AbstractRenameExecutor {

  private IType m_formField;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    boolean superRun = super.canRun(selection);
    if (!superRun) {
      return false;
    }

    m_formField = UiUtility.getTypeFromSelection(selection);
    return isEditable(m_formField);
  }

  @Override
  protected void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException {
    transaction.add(m_formField, newName);

    // find getter
    IMethod getter = ScoutTypeUtility.getFormFieldGetterMethod(m_formField);
    if (TypeUtility.exists(getter)) {
      transaction.add(getter, "get" + NamingUtility.ensureStartWithUpperCase(newName));
    }
  }

  @Override
  protected IStatus validate(String newName) {
    return ScoutUtility.validateFormFieldName(newName, SdkProperties.SUFFIX_FORM_FIELD, m_formField);
  }

}
