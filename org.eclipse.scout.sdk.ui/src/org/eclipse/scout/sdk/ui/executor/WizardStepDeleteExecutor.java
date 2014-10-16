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
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link WizardStepDeleteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class WizardStepDeleteExecutor extends AbstractExecutor {

  private Set<IType> m_selectedSteps;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_selectedSteps = UiUtility.getTypesFromSelection(selection, null);
    return isEditable(m_selectedSteps);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    if (m_selectedSteps.size() == 1) {
      box.setMessage(Texts.get("StepDeleteConfirmationMessage"));
    }
    else {
      box.setMessage(Texts.get("StepDeleteConfirmationMessagePlural"));
    }

    if (box.open() == SWT.OK) {
      JavaElementDeleteOperation delOp = new JavaElementDeleteOperation();
      for (IType stepToDelete : m_selectedSteps) {
        delOp.addMember(stepToDelete);
        IMethod getter = ScoutTypeUtility.getWizardStepGetterMethod(stepToDelete);
        if (TypeUtility.exists(getter)) {
          delOp.addMember(getter);
        }
      }

      new OperationJob(delOp).schedule();
    }
    return null;
  }

}
