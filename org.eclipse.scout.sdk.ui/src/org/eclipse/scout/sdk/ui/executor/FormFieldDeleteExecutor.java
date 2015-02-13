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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.form.field.BoxDeleteOperation;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link FormFieldDeleteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class FormFieldDeleteExecutor extends AbstractExecutor {

  private Set<IType> m_fieldTypes;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_fieldTypes = UiUtility.getTypesFromSelection(selection, null);
    return isEditable(m_fieldTypes);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    MemberSelectionDialog confirmDialog = new MemberSelectionDialog(shell, Texts.get("DeleteFormField"), Texts.get("SelectFieldsToRemove"));
    confirmDialog.setMembers(m_fieldTypes);
    confirmDialog.setSelectedMembers(m_fieldTypes);

    if (confirmDialog.open() == Dialog.OK) {
      Set<? extends IMember> selectedMembers = confirmDialog.getSelectedMembers();
      List<IOperation> deleteOps = new ArrayList<>(selectedMembers.size());
      for (IMember m : selectedMembers) {
        deleteOps.add(new BoxDeleteOperation((IType) m));
      }

      new OperationJob(deleteOps).schedule();
    }
    return null;
  }

}
