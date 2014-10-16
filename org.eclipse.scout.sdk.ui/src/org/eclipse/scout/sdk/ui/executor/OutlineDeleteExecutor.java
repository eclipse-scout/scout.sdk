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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.outline.OutlineDeleteOperation;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link OutlineDeleteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class OutlineDeleteExecutor extends AbstractExecutor {

  @Override
  public boolean canRun(IStructuredSelection selection) {
    return isEditable(UiUtility.getTypeFromSelection(selection));
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    IType selectedType = UiUtility.getTypeFromSelection(selection);
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    box.setMessage(Texts.get("OutlineDeleteConfirmation"));
    if (box.open() == SWT.OK) {
      OutlineDeleteOperation op = new OutlineDeleteOperation(selectedType);
      new OperationJob(op).schedule();
    }
    return null;
  }

}
