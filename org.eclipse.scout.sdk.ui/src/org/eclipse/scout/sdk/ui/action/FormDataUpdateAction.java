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
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.workspace.dto.FormDataDtoUpdateOperation;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 *
 */
public class FormDataUpdateAction extends AbstractOperationAction {
  private IType m_formDataOwner;

  public FormDataUpdateAction() {
    super(Texts.get("UpdateFormData"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolLoading), null, false, Category.UDPATE);
    getJob().setSystemProperty(OperationJob.SYSTEM_PROPERTY_USER_NAME, OperationJob.SCOUT_CODE_GEN_USER_NAME);
  }

  @Override
  public boolean isVisible() {
    return TypeUtility.exists(getFormDataOwner()) && getFormDataOwner().getDeclaringType() == null && isEditable(getFormDataOwner());
  }

  public void setFormDataOwner(IType formDataOwner) {
    m_formDataOwner = formDataOwner;
  }

  protected IType getFormDataOwner() {
    return m_formDataOwner;
  }

  private IType findFormDataType() {
    try {
      FormDataAnnotation formDataAnnotation = ScoutTypeUtility.findFormDataAnnotation(getFormDataOwner(), TypeUtility.getSuperTypeHierarchy(getFormDataOwner()));
      if (FormDataAnnotation.isCreate(formDataAnnotation)) {
        return formDataAnnotation.getFormDataType();
      }
    }
    catch (JavaModelException ex) {
      ScoutSdkUi.logError("Unable to parse formdata annotation in type '" + getFormDataOwner().getFullyQualifiedName() + "'.", ex);
    }
    return null;
  }

  @Override
  public boolean isActive() {
    IType formDataType = findFormDataType();
    if (!TypeUtility.exists(formDataType)) {
      return false;
    }

    try {
      setOperation(new FormDataDtoUpdateOperation(getFormDataOwner()));
      return true;
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("unable to calculate visibility for form data update context menu.", e);
      return false;
    }
  }
}
