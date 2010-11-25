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
package org.eclipse.scout.sdk.ui.action.delete;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.field.FormFieldDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class FormFieldDeleteAction extends Action {
  private Shell m_shell;
  private MemberSelectionDialog m_confirmDialog;
  private final IType m_formFieldType;
  private final String m_fieldName;

  public FormFieldDeleteAction(IType formFieldType, String fieldName, Shell shell) {
    super(Texts.get("Action_deleteTypeX", fieldName));
    m_formFieldType = formFieldType;
    m_fieldName = fieldName;
    m_shell = shell;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_FIELD_DEFAULT_DELETE));
  }

  @Override
  public void run() {
    MessageBox box = new MessageBox(m_shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    box.setMessage("Are you sure you want to delete '" + m_fieldName + "'?");
    if (box.open() == SWT.OK) {
      FormFieldDeleteOperation op = new FormFieldDeleteOperation(getFormFieldType(), true);
      OperationJob job = new OperationJob(op);
      job.schedule();
    }
  }

  public IType getFormFieldType() {
    return m_formFieldType;
  }

}
