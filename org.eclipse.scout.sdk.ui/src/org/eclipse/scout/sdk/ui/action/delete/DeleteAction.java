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

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IDeleteOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class DeleteAction extends Action {
  private Shell m_shell;
  private MemberSelectionDialog m_confirmDialog;
  private String m_name;
  private final IDeleteOperation m_operation;

  public DeleteAction(String name, Shell parentShell, IDeleteOperation operation) {
    super(Texts.get("Action_deleteTypeX", name));
    m_name = name;
    m_shell = parentShell;
    m_operation = operation;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_DELETE));
  }

  @Override
  public void run() {
    MessageBox box = new MessageBox(m_shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    box.setMessage("Are you sure you want to delete '" + m_name + "'?");
    if (box.open() == SWT.OK) {
      OperationJob job = new OperationJob(getOperation());
      job.schedule();
    }
  }

  public IOperation getOperation() {
    return m_operation;
  }

}
