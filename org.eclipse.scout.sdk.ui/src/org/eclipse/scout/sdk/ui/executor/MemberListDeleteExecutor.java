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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.ui.dialog.IMemberSelectionChangedListener;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.ServiceOperationNodePage;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedContextPropertyNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyNodePage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link MemberListDeleteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class MemberListDeleteExecutor extends AbstractExecutor {

  private Set<IMember> m_membersToDelete;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_membersToDelete = collectMembersToDelete(selection);
    return isEditable(m_membersToDelete);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    final MemberSelectionDialog confirmDialog = new MemberSelectionDialog(shell, "Delete");
    confirmDialog.addMemberSelectionListener(new IMemberSelectionChangedListener() {
      @Override
      public void handleSelectionChanged(Set<? extends IMember> s) {
        confirmDialog.setMessage("");
        confirmDialog.getOkButton().setEnabled(CollectionUtility.hasElements(s));
      }
    });

    confirmDialog.setMembers(m_membersToDelete);
    confirmDialog.setSelectedMembers(m_membersToDelete);

    if (confirmDialog.open() == Dialog.OK) {
      JavaElementDeleteOperation op = new JavaElementDeleteOperation();
      op.setMembers(confirmDialog.getSelectedMembers());
      new OperationJob(op).schedule();
    }
    return null;
  }

  protected Set<IMember> collectMembersToDelete(IStructuredSelection selection) {
    Set<IMember> membersToDelete = new LinkedHashSet<IMember>();
    Iterator iterator = selection.iterator();
    while (iterator.hasNext()) {
      Object selObject = iterator.next();
      if (selObject instanceof ServiceOperationNodePage) {
        ServiceOperationNodePage sonp = (ServiceOperationNodePage) selObject;
        if (TypeUtility.exists(sonp.getImplementationOpMethod())) {
          membersToDelete.add(sonp.getImplementationOpMethod());
        }
        if (TypeUtility.exists(sonp.getInterfaceOpMethod())) {
          membersToDelete.add(sonp.getInterfaceOpMethod());
        }
      }
      else if (selObject instanceof SharedContextPropertyNodePage) {
        SharedContextPropertyNodePage scpnp = (SharedContextPropertyNodePage) selObject;
        if (scpnp.getServerDesc() != null) {
          membersToDelete.addAll(scpnp.getServerDesc().getAllMembers());
        }
        if (scpnp.getClientDesc() != null) {
          membersToDelete.addAll(scpnp.getClientDesc().getAllMembers());
        }
      }
      else if (selObject instanceof BeanPropertyNodePage) {
        BeanPropertyNodePage bpnp = (BeanPropertyNodePage) selObject;
        membersToDelete.addAll(bpnp.getPropertyDescriptor().getAllMembers());
      }
    }
    return membersToDelete;
  }
}
