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
package org.eclipse.scout.sdk.ui.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.ui.wizard.IWizardPageContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class WizardPageDialog extends TitleAreaDialog implements IWizardPageContainer {

  private final AbstractWorkspaceWizardPage m_page;

  public WizardPageDialog(Shell shell, AbstractWorkspaceWizardPage page) {
    super(shell);
    m_page = page;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    if (m_page != null) {
      newShell.setText(m_page.getTitle());
    }
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite rootPane = new Composite(parent, SWT.NONE);
    rootPane.setLayout(new FillLayout());
    m_page.createControl(parent);
    return rootPane;
  }

  public Button getOkButton() {
    return getButton(OK);
  }

  public void setStatus(IStatus status, IStatus defaultOkStatus) {
    getOkButton().setEnabled(true);
    IStatus highestSeverityStatus = getHighestSeverityStatus(status, defaultOkStatus);
    int messagetype;
    switch (highestSeverityStatus.getSeverity()) {
      case IStatus.INFO:
        messagetype = IMessageProvider.INFORMATION;
        break;
      case IStatus.WARNING:
        messagetype = IMessageProvider.WARNING;
        break;
      case IStatus.ERROR:
        getOkButton().setEnabled(false);
        messagetype = IMessageProvider.ERROR;
        break;
      default:
        messagetype = IMessageProvider.NONE;
        break;
    }
    String message = highestSeverityStatus.getMessage();
    setMessage(message, messagetype);

  }

  private IStatus getHighestSeverityStatus(IStatus status, IStatus highestSeverity) {
    if (status.isMultiStatus()) {
      for (IStatus child : status.getChildren()) {
        highestSeverity = getHighestSeverityStatus(child, highestSeverity);
      }
      return highestSeverity;
    }
    else {
      if (highestSeverity.getSeverity() < status.getSeverity()) {
        highestSeverity = status;
      }
      return highestSeverity;
    }
  }
}
