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
import java.util.List;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.field.BoxDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.IMemberSelectionChangedListener;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.util.ScoutSourceUtilities;
import org.eclipse.swt.widgets.Shell;

public class BoxDeleteAction extends Action {
  private Shell m_shell;
  private MemberSelectionDialog m_confirmDialog;
  private final IType m_boxType;

  public BoxDeleteAction(IType boxType, Shell shell) {
    super(Texts.get("Process_deleteX", ScoutSourceUtilities.getTranslatedMethodStringValue(boxType, "getConfiguredLabel")));
    m_boxType = boxType;
    m_shell = shell;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_DELETE));
  }

  @Override
  public void run() {
    m_confirmDialog = new MemberSelectionDialog(m_shell, getText());
    List<IMember> members = new ArrayList<IMember>();
    List<IMember> selectedMembers = new ArrayList<IMember>();
    collectAffectedMembers(members, selectedMembers);
    m_confirmDialog.setMembers(members.toArray(new IMember[members.size()]));
    m_confirmDialog.setSelectedMembers(selectedMembers.toArray(new IMember[selectedMembers.size()]));
    if (m_confirmDialog.open() == Dialog.OK) {
      BoxDeleteOperation op = new BoxDeleteOperation(getBoxType());
      OperationJob job = new OperationJob(op);
      job.schedule();
    }
  }

  protected void collectAffectedMembers(List<IMember> members, List<IMember> selectedMembers) {
    members.add(getBoxType());
    selectedMembers.add(getBoxType());
  }

  public IType getBoxType() {
    return m_boxType;
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
