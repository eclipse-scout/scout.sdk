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
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.service.ServiceDeleteOperation;
import org.eclipse.scout.sdk.ui.dialog.IMemberSelectionChangedListener;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.view.outline.pages.project.server.service.AbstractServiceNodePage;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link ServiceDeleteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class ServiceDeleteExecutor extends AbstractExecutor {

  private IType m_serviceIfc = null;
  private IType m_serviceImpl = null;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    Object selectedElement = selection.getFirstElement();
    if (selectedElement instanceof AbstractServiceNodePage) {
      AbstractServiceNodePage asnp = (AbstractServiceNodePage) selectedElement;
      m_serviceIfc = asnp.getInterfaceType();
      m_serviceImpl = asnp.getType();
    }
    return isEditable(m_serviceImpl);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    final MemberSelectionDialog confirmDialog = new MemberSelectionDialog(shell, Texts.get("Delete"));
    confirmDialog.addMemberSelectionListener(new IMemberSelectionChangedListener() {
      @Override
      public void handleSelectionChanged(Set<? extends IMember> s) {
        confirmDialog.setMessage("");
        confirmDialog.getOkButton().setEnabled(CollectionUtility.hasElements(s));
      }
    });

    Set<IMember> members = new LinkedHashSet<IMember>(2);
    if (m_serviceIfc != null && !m_serviceIfc.isBinary()) {
      members.add(m_serviceIfc);
    }
    if (m_serviceImpl != null && !m_serviceImpl.isBinary()) {
      members.add(m_serviceImpl);
    }
    confirmDialog.setMembers(members);
    confirmDialog.setSelectedMembers(members);

    if (confirmDialog.open() == Dialog.OK) {
      ServiceDeleteOperation deleteOp = new ServiceDeleteOperation();
      for (IMember m : confirmDialog.getSelectedMembers()) {
        if (CompareUtility.equals(m, m_serviceIfc)) {
          deleteOp.setServiceInterface(m_serviceIfc);
        }
        else if (CompareUtility.equals(m, m_serviceImpl)) {
          deleteOp.setServiceImplementation(m_serviceImpl);
        }
      }

      new OperationJob(deleteOp).schedule();
    }
    return null;
  }
}
