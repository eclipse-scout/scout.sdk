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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformClientBundleOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformCodeTypesOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformFormsOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformLookupCallsOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformOutlinesOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformPagesOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformSearchFormsOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformServerBundleOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformSharedBundleOperation;
import org.eclipse.scout.sdk.operation.util.wellform.WellformWizardsOperation;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.BundleNodeGroupTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.ProjectsTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ClientLookupCallTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.ClientNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.OutlineTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.FormTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.SearchFormTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.AllPagesTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.wizard.WizardTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.ServerNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.CodeTypeTablePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedNodePage;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link WellformExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class WellformExecutor extends AbstractExecutor {

  private IOperation m_operation;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    Object selectedElement = selection.getFirstElement();
    IScoutBundle scoutBundle = UiUtility.getScoutBundleFromSelection(selection);
    if (selectedElement instanceof BundleNodeGroupTablePage) {
      Set<? extends IScoutBundle> childBundles = scoutBundle.getChildBundles(ScoutBundleFilters.getWorkspaceBundlesFilter(), true);
      m_operation = new WellformClientBundleOperation(childBundles);
    }
    else if (selectedElement instanceof ProjectsTablePage) {
      Set<IScoutBundle> clients = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT));
      m_operation = new WellformClientBundleOperation(clients);
    }
    else if (selectedElement instanceof ClientLookupCallTablePage) {
      m_operation = new WellformLookupCallsOperation(scoutBundle);
    }
    else if (selectedElement instanceof ClientNodePage) {
      m_operation = new WellformClientBundleOperation(CollectionUtility.hashSet(scoutBundle));
    }
    else if (selectedElement instanceof OutlineTablePage) {
      m_operation = new WellformOutlinesOperation(scoutBundle);
    }
    else if (selectedElement instanceof FormTablePage) {
      m_operation = new WellformFormsOperation(scoutBundle);
    }
    else if (selectedElement instanceof SearchFormTablePage) {
      m_operation = new WellformSearchFormsOperation(scoutBundle);
    }
    else if (selectedElement instanceof AllPagesTablePage) {
      m_operation = new WellformPagesOperation(scoutBundle);
    }
    else if (selectedElement instanceof WizardTablePage) {
      m_operation = new WellformWizardsOperation(scoutBundle);
    }
    else if (selectedElement instanceof ServerNodePage) {
      m_operation = new WellformServerBundleOperation(scoutBundle);
    }
    else if (selectedElement instanceof CodeTypeTablePage) {
      m_operation = new WellformCodeTypesOperation(scoutBundle);
    }
    else if (selectedElement instanceof SharedNodePage) {
      m_operation = new WellformSharedBundleOperation(scoutBundle);
    }
    return m_operation != null && isEditable(scoutBundle);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    // no specific type: show warning
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    box.setMessage(Texts.get("WellformConfirmationMessage"));
    if (box.open() == SWT.OK) {
      new OperationJob(m_operation).schedule();
    }
    return null;
  }

}
