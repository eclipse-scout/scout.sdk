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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.field.BoxDeleteOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;

public class FormFieldDeleteAction extends AbstractScoutHandler {

  private final Set<IType> m_formFieldTypes;

  public FormFieldDeleteAction() {
    super(Texts.get("DeleteWithPopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormFieldRemove), "Delete", true, Category.DELETE);
    m_formFieldTypes = new LinkedHashSet<IType>();
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    IType[] members = m_formFieldTypes.toArray(new IType[m_formFieldTypes.size()]);

    MemberSelectionDialog m_confirmDialog = new MemberSelectionDialog(shell, Texts.get("DeleteFormField"), Texts.get("SelectFieldsToRemove"));
    m_confirmDialog.setMembers(members);
    m_confirmDialog.setSelectedMembers(members);

    if (m_confirmDialog.open() == Dialog.OK) {
      IMember[] selectedMembers = m_confirmDialog.getSelectedMembers();
      BoxDeleteOperation[] deleteOperations = new BoxDeleteOperation[selectedMembers.length];
      for (int i = 0; i < selectedMembers.length; i++) {
        deleteOperations[i] = new BoxDeleteOperation((IType) selectedMembers[i]);
      }

      OperationJob job = new OperationJob(deleteOperations);
      job.schedule();
    }
    return null;
  }

  @Override
  public boolean isVisible() {
    for (IType field : m_formFieldTypes) {
      if (!isEditable(field)) {
        return false;
      }
    }
    return true;
  }

  public void addFormFieldType(IType formFieldType) {
    m_formFieldTypes.add(formFieldType);
  }
}
