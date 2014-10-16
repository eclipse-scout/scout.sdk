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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.AbstractWorkspaceBlockingJob;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.ui.dialog.IMemberSelectionChangedListener;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link PageWithTableDeleteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 16.10.2014
 */
public class PageWithTableDeleteExecutor extends AbstractExecutor {

  private IType m_pageType;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_pageType = UiUtility.getTypeFromSelection(selection);
    return isEditable(m_pageType);
  }

  @Override
  public Object run(final Shell shell, final IStructuredSelection selection, final ExecutionEvent event) {
    new AbstractWorkspaceBlockingJob("Collect elements to delete") {
      @Override
      protected void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
        runAsync(shell, selection, event);
      }
    }.schedule();
    return null;
  }

  protected void runAsync(final Shell shell, final IStructuredSelection selection, final ExecutionEvent event) {
    final Set<IMember> members = new LinkedHashSet<IMember>();
    final Set<IMember> selectedMembers = new LinkedHashSet<IMember>();

    collectAffectedMembers(members, selectedMembers, selection);

    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        final MemberSelectionDialog confirmDialog = new MemberSelectionDialog(shell, Texts.get("Action_deleteTypeX", m_pageType.getElementName()));
        confirmDialog.setMembers(members);
        confirmDialog.setSelectedMembers(selectedMembers);
        confirmDialog.addMemberSelectionListener(new IMemberSelectionChangedListener() {
          @Override
          public void handleSelectionChanged(Set<? extends IMember> s) {
            confirmDialog.setMessage("");
            confirmDialog.getOkButton().setEnabled(CollectionUtility.hasElements(s));
          }
        });

        if (confirmDialog.open() == Dialog.OK) {
          JavaElementDeleteOperation op = new JavaElementDeleteOperation();
          op.setMembers(confirmDialog.getSelectedMembers());
          new OperationJob(op).schedule();
        }
      }
    });
  }

  protected void collectAffectedMembers(Set<IMember> members, Set<IMember> selectedMembers, IStructuredSelection selection) {
    // page
    members.add(m_pageType);
    selectedMembers.add(m_pageType);

    // page data
    IType pageData = null;
    try {
      pageData = ScoutTypeUtility.findPageDataForPage(m_pageType);
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError(e);
    }
    if (TypeUtility.exists(pageData)) {
      members.add(pageData);
      selectedMembers.add(pageData);
    }
  }
}
