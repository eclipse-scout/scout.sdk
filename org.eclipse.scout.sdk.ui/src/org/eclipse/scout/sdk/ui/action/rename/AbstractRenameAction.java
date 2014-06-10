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
package org.eclipse.scout.sdk.ui.action.rename;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.dialog.RenameDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractRenameAction extends AbstractScoutHandler {
  private RenameDialog m_dialog;
  private String m_oldName;
  private String m_readOnlySuffix;

  public AbstractRenameAction() {
    super(Texts.get("RenameWithPopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolRename), "ALT+SHIFT+R", false, Category.RENAME);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    m_dialog = new RenameDialog(shell, getOldName(), getReadOnlySuffix());
    m_dialog.addPropertyChangeListener(new P_PropertyListener());
    getDialog().create();
    validateInternal(getDialog().getNewName());
    if (getDialog().open() == IDialogConstants.OK_ID) {
      try {
        JdtRenameTransaction tx = new JdtRenameTransaction();
        fillTransaction(tx, getDialog().getNewName());
        tx.commit(ScoutSdkUi.getShell());
      }
      catch (CoreException e) {
        ScoutSdkUi.logError("rename failed.", e);
      }
    }
    return null;
  }

  protected abstract void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException;

  protected abstract IStatus validate(String newName);

  public RenameDialog getDialog() {
    return m_dialog;
  }

  private void validateInternal(String newName) {
    IStatus status = validate(newName);
    getDialog().getOkButton().setEnabled(true);
    switch (status.getSeverity()) {
      case IStatus.ERROR:
        getDialog().setMessage(status.getMessage(), IMessageProvider.ERROR);
        getDialog().getOkButton().setEnabled(false);
        break;
      case IStatus.WARNING:
        getDialog().setMessage(status.getMessage(), IMessageProvider.WARNING);
        break;
      case IStatus.INFO:
        getDialog().setMessage(status.getMessage(), IMessageProvider.INFORMATION);
        break;
      default:
        getDialog().setMessage("", IMessageProvider.NONE);
        break;
    }
  }

  private class P_PropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(RenameDialog.PROP_NEW_NAME)) {
        validateInternal((String) evt.getNewValue());
      }
    }
  }

  public String getOldName() {
    return m_oldName;
  }

  public void setOldName(String oldName) {
    m_oldName = oldName;
  }

  public String getReadOnlySuffix() {
    return m_readOnlySuffix;
  }

  public void setReadOnlySuffix(String readOnlySuffix) {
    m_readOnlySuffix = readOnlySuffix;
  }
}
