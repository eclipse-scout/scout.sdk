/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.executor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.dialog.RenameDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.shared.SharedContextPropertyNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.basic.beanproperty.BeanPropertyNodePage;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link AbstractRenameExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public abstract class AbstractRenameExecutor extends AbstractExecutor {

  private RenameDialog m_dialog;
  private String m_oldName;
  private String m_readOnlySuffix;

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    m_dialog = new RenameDialog(shell, m_oldName, getReadOnlySuffix());
    m_dialog.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (RenameDialog.PROP_NEW_NAME.equals(evt.getPropertyName())) {
          validateInternal((String) evt.getNewValue());
        }
      }
    });
    m_dialog.create();
    validateInternal(m_dialog.getNewName());

    if (m_dialog.open() == IDialogConstants.OK_ID) {
      try {
        JdtRenameTransaction tx = new JdtRenameTransaction();
        fillTransaction(tx, m_dialog.getNewName());
        tx.commit(ScoutSdkUi.getShell());
      }
      catch (CoreException e) {
        ScoutSdkUi.logError("rename failed.", e);
      }
    }

    return null;
  }

  private void validateInternal(String newName) {
    IStatus status = validate(newName);
    m_dialog.getOkButton().setEnabled(true);
    switch (status.getSeverity()) {
      case IStatus.ERROR:
        m_dialog.setMessage(status.getMessage(), IMessageProvider.ERROR);
        m_dialog.getOkButton().setEnabled(false);
        break;
      case IStatus.WARNING:
        m_dialog.setMessage(status.getMessage(), IMessageProvider.WARNING);
        break;
      case IStatus.INFO:
        m_dialog.setMessage(status.getMessage(), IMessageProvider.INFORMATION);
        break;
      default:
        m_dialog.setMessage("", IMessageProvider.NONE);
        break;
    }
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    Object selectedElement = selection.getFirstElement();
    if (selectedElement instanceof AbstractScoutTypePage) {
      AbstractScoutTypePage astp = (AbstractScoutTypePage) selectedElement;

      m_oldName = astp.getType().getElementName();
      m_readOnlySuffix = astp.getReadOnlySuffix();
      return true;
    }
    else if (selectedElement instanceof SharedContextPropertyNodePage) {
      SharedContextPropertyNodePage scpnp = (SharedContextPropertyNodePage) selectedElement;
      IPropertyBean serverDesc = scpnp.getServerDesc();
      if (serverDesc != null) {
        m_oldName = serverDesc.getBeanName();
        return true;
      }
      else {
        IPropertyBean clientDesc = scpnp.getClientDesc();
        if (clientDesc != null) {
          m_oldName = clientDesc.getBeanName();
          return true;
        }
      }
    }
    else if (selectedElement instanceof BeanPropertyNodePage) {
      BeanPropertyNodePage bpnp = (BeanPropertyNodePage) selectedElement;
      m_oldName = bpnp.getPropertyDescriptor().getBeanName();
      return true;
    }
    return false;
  }

  /**
   * Executed when the {@link RenameDialog} has been closed using the Ok button.
   *
   * @param transaction
   *          The {@link JdtRenameTransaction} that will be executed afterwards.
   * @param newName
   *          The new name the user selected in the dialog
   * @param selection
   * @throws CoreException
   */
  protected abstract void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException;

  /**
   * Validation while the {@link RenameDialog} is open.
   *
   * @param newName
   *          The new name that is currently visible in the dialog
   * @return The {@link IStatus} of the validation.
   */
  protected abstract IStatus validate(String newName);

  public String getOldName() {
    return m_oldName;
  }

  public void setOldName(String newOldName) {
    m_oldName = newOldName;
  }

  public String getReadOnlySuffix() {
    return m_readOnlySuffix;
  }
}
