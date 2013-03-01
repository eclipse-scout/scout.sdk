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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.dialog.IMemberSelectionChangedListener;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.swt.widgets.Shell;

public class PropertyBeanDeleteAction extends AbstractScoutHandler {

  private MemberSelectionDialog m_confirmDialog;
  private IPropertyBean m_beanDesc;

  public PropertyBeanDeleteAction() {
    super(Texts.get("DeleteWithPopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.VariableRemove), "Delete", false, Category.DELETE);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    m_confirmDialog = new MemberSelectionDialog(shell, getLabel());
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
    return null;
  }

  protected void collectAffectedMembers(List<IMember> members, List<IMember> selectedMembers) {
    IMember[] beanMembers = getBeanDesc().getAllMembers();
    members.addAll(Arrays.asList(beanMembers));
    selectedMembers.addAll(Arrays.asList(beanMembers));
  }

  public IPropertyBean getBeanDesc() {
    return m_beanDesc;
  }

  @Override
  public boolean isVisible() {
    return m_beanDesc != null && isEditable(m_beanDesc.getDeclaringType());
  }

  public void setBeanDesc(IPropertyBean beanDesc) {
    m_beanDesc = beanDesc;
  }

  private class P_SelectionValidationListener implements IMemberSelectionChangedListener {
    @Override
    public void handleSelectionChanged(IMember[] selection) {
      m_confirmDialog.setMessage("");
      boolean canOk = true;
      if (selection == null || selection.length == 0) {
        canOk = false;
      }
      else {
        HashSet<IMember> members = new HashSet<IMember>(Arrays.asList(selection));
        if (members.contains(getBeanDesc().getField())) {
          if ((getBeanDesc().getReadMethod() != null && !members.contains(getBeanDesc().getReadMethod())) ||
              (getBeanDesc().getWriteMethod() != null && !members.contains(getBeanDesc().getWriteMethod()))) {
            canOk = false;
            m_confirmDialog.setMessage(Texts.get("FieldCanBeDeletedWithWriteMethod"), IMessageProvider.ERROR);
          }
        }
      }
      m_confirmDialog.getOkButton().setEnabled(canOk);

    }
  } // end class P_SelectionValidationListener

}
