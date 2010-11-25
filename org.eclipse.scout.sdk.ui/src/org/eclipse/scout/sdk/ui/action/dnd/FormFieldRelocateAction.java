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
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldCopyOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldMoveOperation;
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
    super("Relocate Form Field");
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
      String message = "Enter a name of the " + ((isCreateCopy()) ? ("copied") : ("moved")) + " form field.";
      // show dialog
      RenameConfirmationDialog dialog = new RenameConfirmationDialog(m_shell, "Form Field Name", message);
      dialog.setNotAllowedNames(usedNames);
      dialog.setTypeName("CopyOf" + getFormField().getElementName());
      if (dialog.open() != Dialog.OK) {
        return;
      }
      fieldName = dialog.getTypeName();
    }
    IOperation operation = null;
    // copy
    if (isCreateCopy()) {
      FormFieldCopyOperation copyOp = new FormFieldCopyOperation(fieldName, getFormField(), getTargetDeclaringType());
      copyOp.setPosition(getLocation());
      copyOp.setPositionType(getNeighborField());
      operation = copyOp;
    }
    // move
    else {
      FormFieldMoveOperation moveOperation = new FormFieldMoveOperation(getFormField(), getTargetDeclaringType());
      moveOperation.setPosition(getLocation());
      moveOperation.setPositionField(getNeighborField());
      operation = moveOperation;
    }
    if (operation != null) {
      new OperationJob(operation).schedule();
    }

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
