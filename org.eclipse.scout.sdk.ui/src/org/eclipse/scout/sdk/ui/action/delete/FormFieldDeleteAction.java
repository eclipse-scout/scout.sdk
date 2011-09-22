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

import java.util.LinkedList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.field.FormFieldDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class FormFieldDeleteAction extends AbstractScoutHandler {

  private LinkedList<IType> m_formFieldTypes;

  public FormFieldDeleteAction() {
    super(Texts.get("DeleteWithPopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormFieldRemove), "Delete", true, Category.DELETE);
    m_formFieldTypes = new LinkedList<IType>();
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    if (m_formFieldTypes.size() == 1) {
      box.setMessage(Texts.get("FieldDeleteConfirmation"));
    }
    else {
      box.setMessage(Texts.get("FieldDeleteConfirmationPlural"));
    }
    if (box.open() == SWT.OK) {
      for (IType t : m_formFieldTypes) {
        FormFieldDeleteOperation op = new FormFieldDeleteOperation(t, true);
        OperationJob job = new OperationJob(op);
        job.schedule();
      }
    }
    return null;
  }

  public void addFormFieldType(IType formFieldType) {
    m_formFieldTypes.add(formFieldType);
  }
}
