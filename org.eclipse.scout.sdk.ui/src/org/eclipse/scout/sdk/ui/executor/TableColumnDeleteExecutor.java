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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.field.table.TableColumnDeleteOperation;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link TableColumnDeleteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class TableColumnDeleteExecutor extends AbstractExecutor {

  private Set<IType> m_selectedColumns;

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {

    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    if (m_selectedColumns.size() == 1) {
      box.setMessage(Texts.get("Action_deleteNameX", CollectionUtility.firstElement(m_selectedColumns).getElementName()));
    }
    else {
      box.setMessage(Texts.get("ColumnDeleteConfirmationMessage"));
    }

    if (box.open() == SWT.OK) {
      TableColumnDeleteOperation delOp = new TableColumnDeleteOperation(m_selectedColumns);
      new OperationJob(delOp).schedule();
    }
    return null;
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_selectedColumns = UiUtility.getTypesFromSelection(selection, null);

    if (m_selectedColumns.size() < 1) {
      return false;
    }
    for (IType col : m_selectedColumns) {
      if (!isEditable(col)) {
        return false;
      }
    }
    return true;
  }
}
