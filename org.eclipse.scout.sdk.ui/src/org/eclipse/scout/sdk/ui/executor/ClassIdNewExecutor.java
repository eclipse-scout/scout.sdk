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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.ClassIdNewOperation;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link ClassIdNewExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class ClassIdNewExecutor extends AbstractExecutor {

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    box.setMessage(Texts.get("LongRunningOpConfirmationMsg"));
    if (box.open() == SWT.OK) {
      IScoutBundle startBundle = UiUtility.getScoutBundleFromSelection(selection);
      ClassIdNewOperation cno = new ClassIdNewOperation(startBundle);
      new OperationJob(cno).schedule();
    }
    return null;
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    return isEditable(UiUtility.getScoutBundleFromSelection(selection));
  }
}
