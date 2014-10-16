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
package org.eclipse.scout.nls.sdk.services.ui.executor;

import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.nls.sdk.services.operation.DeleteServiceNlsProjectOperation;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link TextProviderServiceDeleteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 10.10.2014
 */
public class TextProviderServiceDeleteExecutor extends AbstractExecutor {

  private Set<IType> m_selectedServices;

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    if (m_selectedServices.size() == 1) {
      box.setMessage(Texts.get("DeleteTextProviderService"));
    }
    else {
      box.setMessage(Texts.get("DeleteTextProviderServicePlural"));
    }

    if (box.open() == SWT.OK) {
      OperationJob job = new OperationJob();
      for (IType svc : m_selectedServices) {
        DeleteServiceNlsProjectOperation o = new DeleteServiceNlsProjectOperation(svc, ScoutTypeUtility.getScoutBundle(svc));
        job.addOperation(o);
      }
      job.schedule();
    }
    return null;
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_selectedServices = UiUtility.getTypesFromSelection(selection, TypeFilters.getMultiTypeFilterAnd(TypeFilters.getInWorkspaceFilter(), TypeFilters.getSubtypeFilter(IRuntimeClasses.AbstractDynamicNlsTextProviderService)));
    return isEditable(m_selectedServices);
  }
}
