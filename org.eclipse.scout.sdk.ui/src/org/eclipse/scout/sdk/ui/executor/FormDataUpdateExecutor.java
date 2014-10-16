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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.workspace.dto.formdata.FormDataDtoUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link FormDataUpdateExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class FormDataUpdateExecutor extends AbstractExecutor {

  private IType m_formDataOwner;

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {

    try {
      FormDataAnnotation formDataAnnotation = ScoutTypeUtility.findFormDataAnnotation(m_formDataOwner, TypeUtility.getSupertypeHierarchy(m_formDataOwner));
      if (FormDataAnnotation.isCreate(formDataAnnotation) && TypeUtility.exists(formDataAnnotation.getFormDataType())) {
        new OperationJob(new FormDataDtoUpdateOperation(m_formDataOwner, formDataAnnotation)).schedule();
      }
      else {
        MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        box.setMessage(Texts.get("CheckFormDataAnnot"));
        box.open();
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("unable to calculate form data type for update of element '" + m_formDataOwner + "'.", e);
    }
    return null;
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_formDataOwner = UiUtility.getTypeFromSelection(selection);
    return isEditable(m_formDataOwner) && m_formDataOwner.getDeclaringType() == null;
  }

}
