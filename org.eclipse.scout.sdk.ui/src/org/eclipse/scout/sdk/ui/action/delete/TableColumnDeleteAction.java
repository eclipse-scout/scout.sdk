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

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class TableColumnDeleteAction extends AbstractScoutHandler {
  private ArrayList<IType> m_tableColumns;

  public TableColumnDeleteAction() {
    super(Texts.get("DeleteWithPopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.TableColumnRemove), "Delete", true, Category.DELETE);
    m_tableColumns = new ArrayList<IType>();
  }

  @Override
  public boolean isVisible() {
    if (m_tableColumns == null || m_tableColumns.size() < 1) {
      return false;
    }
    for (IType col : m_tableColumns) {
      if (!isEditable(col)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    if (m_tableColumns.size() == 1) {
      box.setMessage(Texts.get("Action_deleteNameX", m_tableColumns.get(0).getElementName()));
    }
    else {
      box.setMessage(Texts.get("ColumnDeleteConfirmationMessage"));
    }
    if (box.open() == SWT.OK) {
      JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
      for (IType columnToDelete : m_tableColumns) {
        delOp.addMember(columnToDelete);
        IMethod getter = ScoutTypeUtility.getColumnGetterMethod(columnToDelete);
        if (TypeUtility.exists(getter)) {
          delOp.addMember(getter);
        }
      }
      OperationJob job = new OperationJob(delOp);
      job.schedule();
    }
    return null;
  }

  public void addTableColumn(IType tableColumn) {
    m_tableColumns.add(tableColumn);
  }
}
