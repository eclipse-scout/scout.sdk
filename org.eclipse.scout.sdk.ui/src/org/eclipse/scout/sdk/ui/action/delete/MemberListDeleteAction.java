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

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.dialog.IMemberSelectionChangedListener;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>MemberListDeleteAction</h3>
 */
public class MemberListDeleteAction extends AbstractScoutHandler {

  private final Set<IMember> m_membersToDelete;
  private MemberSelectionDialog m_confirmDialog;

  public MemberListDeleteAction() {
    super(Texts.get("DeleteWithPopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolRemove), "Delete", true, Category.DELETE);
    m_membersToDelete = new TreeSet<IMember>(new Comparator<IMember>() {
      @Override
      public int compare(IMember t1, IMember t2) {
        CompositeObject ct1 = new CompositeObject(t1.getElementName(), t1);
        CompositeObject ct2 = new CompositeObject(t2.getElementName(), t2);
        return ct1.compareTo(ct2);
      }
    });
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    m_confirmDialog = new MemberSelectionDialog(shell, "Delete");
    m_confirmDialog.addMemberSelectionListener(new IMemberSelectionChangedListener() {
      @Override
      public void handleSelectionChanged(IMember[] s) {
        m_confirmDialog.setMessage("");
        boolean canOk = s != null && s.length > 0;
        m_confirmDialog.getOkButton().setEnabled(canOk);
      }
    });

    m_confirmDialog.setMembers(m_membersToDelete.toArray(new IMember[m_membersToDelete.size()]));
    m_confirmDialog.setSelectedMembers(m_membersToDelete.toArray(new IMember[m_membersToDelete.size()]));

    if (m_confirmDialog.open() == Dialog.OK) {
      JavaElementDeleteOperation op = new JavaElementDeleteOperation();
      op.setMembers(m_confirmDialog.getSelectedMembers());
      OperationJob job = new OperationJob(op);
      job.schedule();
    }
    return null;
  }

  @Override
  public boolean isVisible() {
    if (m_membersToDelete.size() < 1) {
      return false;
    }
    for (IMember m : m_membersToDelete) {
      if (!isEditable(m)) {
        return false;
      }
    }
    return true;
  }

  public boolean addMemberToDelete(IMember m) {
    return m_membersToDelete.add(m);
  }

  public boolean removeMemberToDelete(IMember m) {
    return m_membersToDelete.remove(m);
  }
}
