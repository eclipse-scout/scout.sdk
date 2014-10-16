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

import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link DeleteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class DeleteExecutor extends AbstractExecutor {

  private Set<IType> m_selection;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_selection = UiUtility.getTypesFromSelection(selection, null);
    return isEditable(m_selection);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    JavaElementDeleteOperation deleteOperation = null;

    if (m_selection.size() == 1) {
      // delete one single type
      IType typeToDelete = CollectionUtility.firstElement(m_selection);
      if (TypeUtility.exists(typeToDelete)) {
        MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
        box.setText(Texts.get("Question"));
        box.setMessage(Texts.get("DeleteAction_ensureRequest", typeToDelete.getElementName()));
        if (box.open() == SWT.OK) {
          deleteOperation = new JavaElementDeleteOperation();
          deleteOperation.addMember(typeToDelete);
        }
      }
    }
    else {
      // delete multiple types
      MemberSelectionDialog confirmDialog = new MemberSelectionDialog(shell, Texts.get("DeleteAction_ensureRequestPlural"));
      confirmDialog.setMembers(m_selection);
      confirmDialog.setSelectedMembers(m_selection);
      if (confirmDialog.open() == Dialog.OK) {
        deleteOperation = new JavaElementDeleteOperation();
        deleteOperation.setMembers(confirmDialog.getSelectedMembers());
      }
    }

    if (deleteOperation != null) {
      new OperationJob(deleteOperation).schedule();
    }
    return null;
  }

}
