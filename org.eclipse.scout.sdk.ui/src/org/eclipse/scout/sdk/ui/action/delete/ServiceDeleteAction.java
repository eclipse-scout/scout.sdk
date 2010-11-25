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

import java.util.ArrayList;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.service.ServiceDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.IMemberSelectionChangedListener;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.swt.widgets.Shell;

public class ServiceDeleteAction extends Action {
  private MemberSelectionDialog m_confirmDialog;
  private final Shell m_shell;
  private final IType m_serviceImplementation;
  private IType m_serviceInterface;

  public ServiceDeleteAction(Shell shell, IType serviceInterface, IType serviceImplementation) {
    m_shell = shell;
    m_serviceInterface = serviceInterface;
    m_serviceImplementation = serviceImplementation;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_DELETE));
    setText("Delete...");
  }

  @Override
  public void run() {
    m_confirmDialog = new MemberSelectionDialog(m_shell, getText());
//    m_serviceInterface = findServiceInterface();
    ArrayList<IMember> members = new ArrayList<IMember>();
    if (TypeUtility.exists(m_serviceInterface)) {
      members.add(m_serviceInterface);
    }
    if (TypeUtility.exists(m_serviceImplementation)) {
      members.add(m_serviceImplementation);
    }
    m_confirmDialog.setMembers(members.toArray(new IMember[members.size()]));
    m_confirmDialog.setSelectedMembers(members.toArray(new IMember[members.size()]));
    m_confirmDialog.addMemberSelectionListener(new P_SelectionValidationListener());
    if (m_confirmDialog.open() == Dialog.OK) {
      ServiceDeleteOperation deleteOp = new ServiceDeleteOperation();
      for (IMember m : m_confirmDialog.getSelectedMembers()) {
        String qallifiedName = ((IType) m).getFullyQualifiedName();
        if (m_serviceInterface != null && m_serviceInterface.getFullyQualifiedName().equals(qallifiedName)) {
          deleteOp.setServiceInterface(m_serviceInterface);
        }
        else if (m_serviceImplementation.getFullyQualifiedName().equals(qallifiedName)) {
          deleteOp.setServiceImplementation(m_serviceImplementation);
        }
      }
      OperationJob job = new OperationJob(deleteOp);
      job.schedule();
    }
  }

  public IType getServiceImplementation() {
    return m_serviceImplementation;
  }

  private class P_SelectionValidationListener implements IMemberSelectionChangedListener {
    public void handleSelectionChanged(IMember[] selection) {
      m_confirmDialog.setMessage("");
      boolean canOk = true;
      if (selection == null || selection.length == 0) {
        canOk = false;
      }
      m_confirmDialog.getOkButton().setEnabled(canOk);

    }
  } // end class P_SelectionValidationListener
}
