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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.field.BoxDeleteOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;

public class BoxDeleteAction extends AbstractScoutHandler {
  private MemberSelectionDialog m_confirmDialog;
  private IType m_boxType;

  public BoxDeleteAction() {
    super(Texts.get("DeleteWithPopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.GroupboxRemove), "Delete", false, Category.DELETE);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    m_confirmDialog = new MemberSelectionDialog(shell, getLabel());
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
    return null;
  }

  protected void collectAffectedMembers(List<IMember> members, List<IMember> selectedMembers) {
    members.add(getBoxType());
    selectedMembers.add(getBoxType());
  }

  public IType getBoxType() {
    return m_boxType;
  }

  public void setBoxType(IType boxType) {
    m_boxType = boxType;
  }
}
