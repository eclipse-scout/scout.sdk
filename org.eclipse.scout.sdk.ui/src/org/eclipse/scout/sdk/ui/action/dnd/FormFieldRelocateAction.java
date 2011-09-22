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
package org.eclipse.scout.sdk.ui.action.dnd;

import java.util.HashSet;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.dnd.FormFieldDndOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.RenameConfirmationDialog;
import org.eclipse.swt.widgets.Shell;

public class FormFieldRelocateAction extends Action {
  private boolean createCopy;
  private int m_location;
  private IType m_formField;
  private IType m_targetDeclaringType;
  private IType m_neighborField;
  private String m_newFieldName;
  private final Shell m_shell;

  public FormFieldRelocateAction(IType formFieldToMove, Shell shell) {
    super(Texts.get("RelocateFormField"));
    m_formField = formFieldToMove;
    m_shell = shell;
  }

  @Override
  public void run() {
    String fieldName = getFormField().getElementName();
    HashSet<String> usedNames = new HashSet<String>();
    try {
      for (IType t : m_targetDeclaringType.getCompilationUnit().getAllTypes()) {
        usedNames.add(t.getElementName());
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("During finding used names.", e);
    }
    if (getFormField().getCompilationUnit().equals(getTargetDeclaringType().getCompilationUnit()) && !isCreateCopy()) {
      usedNames.remove(getFormField().getElementName());
    }
    if (usedNames.contains(getFormField().getElementName())) {
      String message = Texts.get("EnterFieldName", isCreateCopy() ? Texts.get("Copied") : Texts.get("Moved"));
      // show dialog
      RenameConfirmationDialog dialog = new RenameConfirmationDialog(m_shell, Texts.get("FormFieldName"), message);
      dialog.setNotAllowedNames(usedNames);
      dialog.setTypeName(Texts.get("CopyOf") + getFormField().getElementName());
      if (dialog.open() != Dialog.OK) {
        return;
      }
      fieldName = dialog.getTypeName();
    }
    // operation
    int mode = FormFieldDndOperation.MODE_MOVE;
    if (isCreateCopy()) {
      mode = FormFieldDndOperation.MODE_COPY;
    }
    FormFieldDndOperation dndOp = new FormFieldDndOperation(getFormField(), getTargetDeclaringType(), fieldName, mode);
    dndOp.setPosition(getLocation());
    dndOp.setPositionType(getNeighborField());
    new OperationJob(dndOp).schedule();

  }

  public void setFormField(IType formField) {
    m_formField = formField;
  }

  public IType getFormField() {
    return m_formField;
  }

  public void setTargetDeclaringType(IType targetDeclaringType) {
    m_targetDeclaringType = targetDeclaringType;
  }

  public IType getTargetDeclaringType() {
    return m_targetDeclaringType;
  }

  public void setNeighborField(IType neighborField) {
    m_neighborField = neighborField;
  }

  public IType getNeighborField() {
    return m_neighborField;
  }

  public void setCreateCopy(boolean createCopy) {
    this.createCopy = createCopy;
  }

  public boolean isCreateCopy() {
    return createCopy;
  }

  public void setLocation(int location) {
    m_location = location;
  }

  public int getLocation() {
    return m_location;
  }

}
