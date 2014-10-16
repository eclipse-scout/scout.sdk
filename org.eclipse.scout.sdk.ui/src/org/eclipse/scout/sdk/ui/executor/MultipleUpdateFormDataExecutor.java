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
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.BundleNodeGroupTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ClientNodePage;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.dto.formdata.ClientBundleUpdateFormDataOperation;
import org.eclipse.scout.sdk.workspace.dto.formdata.ScoutBundlesUpdateFormDataOperation;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link MultipleUpdateFormDataExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class MultipleUpdateFormDataExecutor extends AbstractExecutor {

  private IOperation m_operation;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    Object selectedElement = selection.getFirstElement();
    IScoutBundle scoutBundle = UiUtility.getScoutBundleFromSelection(selection);
    if (selectedElement instanceof BundleNodeGroupTablePage) {
      m_operation = new ScoutBundlesUpdateFormDataOperation(scoutBundle);
    }
    else if (selectedElement instanceof ClientNodePage) {
      m_operation = new ClientBundleUpdateFormDataOperation(scoutBundle);
    }

    return m_operation != null && isEditable(scoutBundle);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    new OperationJob(m_operation).schedule();
    return null;
  }
}
