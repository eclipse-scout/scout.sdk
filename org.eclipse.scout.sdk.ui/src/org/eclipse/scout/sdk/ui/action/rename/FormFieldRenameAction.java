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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

public class FormFieldRenameAction extends AbstractRenameAction {

  private IType m_formField;

  @Override
  protected void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException {
    // find getter
    transaction.add(getFormField(), newName);
    IMethod getter = ScoutTypeUtility.getFormFieldGetterMethod(getFormField());
    if (TypeUtility.exists(getter)) {
      transaction.add(getter, "get" + NamingUtility.ensureStartWithUpperCase(newName));
    }
  }

  @Override
  public boolean isVisible() {
    return isEditable(m_formField);
  }

  @Override
  protected IStatus validate(String newName) {
    return ScoutUtility.validateFormFieldName(newName, SdkProperties.SUFFIX_FORM_FIELD, m_formField);
  }

  public IType getFormField() {
    return m_formField;
  }

  public static IType findInnerType(IType declaringType, String simpleName, boolean ignoreCase) throws JavaModelException {
    if (ignoreCase) {
      if (declaringType.getElementName().equalsIgnoreCase(simpleName)) {
        return declaringType;
      }
    }
    else {
      if (declaringType.getElementName().equals(simpleName)) {
        return declaringType;
      }
    }
    for (IType innerType : declaringType.getTypes()) {
      return findInnerType(innerType, simpleName, ignoreCase);
    }
    return null;
  }

  public void setFormField(IType formField) {
    m_formField = formField;
  }
}
