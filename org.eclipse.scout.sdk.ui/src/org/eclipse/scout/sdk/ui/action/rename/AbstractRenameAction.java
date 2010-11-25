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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.RenameDialog;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractRenameAction extends Action {
  private RenameDialog m_dialog;
  private final String m_readOnlySuffix;
  private String m_oldName;
  private final Shell m_shell;

  public AbstractRenameAction(Shell shell, String name, String oldName, String readOnlySuffix) {
    super(name);
    m_shell = shell;
    setOldName(oldName);
    m_readOnlySuffix = readOnlySuffix;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_RENAME));
  }

  @Override
  public void run() {
    m_dialog = new RenameDialog(m_shell, "Rename...", getOldName(), getReadOnlySuffix());
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
  }

  protected abstract void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException;

  public RenameDialog getDialog() {
    return m_dialog;
  }

  public String getReadOnlySuffix() {
    return m_readOnlySuffix;
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

  protected abstract IStatus validate(String newName);

  protected IStatus getJavaNameStatus(String newName) {
    if (newName.equals(getReadOnlySuffix())) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "Name can not be null or empty");
    }
    if (newName.matches(Regex.REGEX_WELLFORMD_JAVAFIELD)) {
      return Status.OK_STATUS;
    }
    if (newName.matches(Regex.REGEX_JAVAFIELD)) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    return Status.OK_STATUS;
  }

  public void setOldName(String oldName) {
    m_oldName = oldName;
  }

  public String getOldName() {
    return m_oldName;
  }

  private class P_PropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(RenameDialog.PROP_NEW_NAME)) {
        validateInternal((String) evt.getNewValue());
      }
    }
  }
}
