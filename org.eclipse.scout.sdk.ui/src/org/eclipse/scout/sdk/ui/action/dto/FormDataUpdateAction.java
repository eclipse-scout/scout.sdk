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
package org.eclipse.scout.sdk.ui.action.dto;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataDtoUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class FormDataUpdateAction extends AbstractScoutHandler {
  private IType m_formDataOwner;

  public FormDataUpdateAction() {
    super(Texts.get("UpdateFormData"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolLoading), null, false, Category.UDPATE);
  }

  @Override
  public boolean isVisible() {
    return isEditable(getFormDataOwner()) && getFormDataOwner().getDeclaringType() == null;
  }

  public void setFormDataOwner(IType formDataOwner) {
    m_formDataOwner = formDataOwner;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    try {
      FormDataAnnotation formDataAnnotation = ScoutTypeUtility.findFormDataAnnotation(getFormDataOwner(), TypeUtility.getSuperTypeHierarchy(getFormDataOwner()));
      if (FormDataAnnotation.isCreate(formDataAnnotation) && TypeUtility.exists(formDataAnnotation.getFormDataType())) {
        OperationJob job = new OperationJob(new FormDataDtoUpdateOperation(getFormDataOwner(), formDataAnnotation));
        job.schedule();
      }
      else {
        MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        box.setMessage(Texts.get("CheckFormDataAnnot"));
        box.open();
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("unable to calculate form data type for update of element '" + getFormDataOwner().getFullyQualifiedName() + "'.", e);
    }
    return null;
  }

  private IType getFormDataOwner() {
    return m_formDataOwner;
  }
}
