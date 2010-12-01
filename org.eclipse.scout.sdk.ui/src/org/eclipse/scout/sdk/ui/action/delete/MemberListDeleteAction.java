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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.IMemberSelectionChangedListener;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>TypeDeleteAction</h3> ...
 */
public class MemberListDeleteAction extends Action {

  private final Shell m_shell;
  private IMember[] m_typesToDelete;
  private MemberSelectionDialog m_confirmDialog;

  public MemberListDeleteAction(String title, Shell shell) {
    super(title);
    m_typesToDelete = new IMember[0];
    m_shell = shell;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolRemove));
  }

  @Override
  public void run() {
    m_confirmDialog = new MemberSelectionDialog(m_shell, getText());
    m_confirmDialog.addMemberSelectionListener(new P_SelectionValidationListener());
    List<IMember> members = new ArrayList<IMember>();
    List<IMember> selectedMembers = new ArrayList<IMember>();
    collectAffectedMembers(members, selectedMembers);
    m_confirmDialog.setMembers(members.toArray(new IMember[members.size()]));
    m_confirmDialog.setSelectedMembers(selectedMembers.toArray(new IMember[selectedMembers.size()]));
    if (m_confirmDialog.open() == Dialog.OK) {
      JavaElementDeleteOperation op = new JavaElementDeleteOperation();
      op.setMembers(m_confirmDialog.getSelectedMembers());
      OperationJob job = new OperationJob(op);
      job.schedule();
    }
  }

  protected void collectAffectedMembers(List<IMember> members, List<IMember> selectedMembers) {
    if (getTypesToDelete() != null) {
      for (IMember t : getTypesToDelete()) {
        members.add(t);
        selectedMembers.add(t);
      }
    }
  }

  public boolean addMemberToDelete(IMember type) {
    HashSet<IMember> types = new HashSet<IMember>(Arrays.asList(getTypesToDelete()));
    boolean added = types.add(type);
    setTypesToDelete(types.toArray(new IMember[types.size()]));
    return added;
  }

  public boolean removeTypeToDelete(IMember type) {
    HashSet<IMember> types = new HashSet<IMember>(Arrays.asList(getTypesToDelete()));
    boolean removed = types.remove(type);
    setTypesToDelete(types.toArray(new IMember[types.size()]));
    return removed;
  }

  public void setTypesToDelete(IMember[] typesToDelete) {
    m_typesToDelete = typesToDelete;
  }

  public IMember[] getTypesToDelete() {
    return m_typesToDelete;
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
