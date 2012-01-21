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
package org.eclipse.scout.nls.sdk.internal.ui.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NlsStatusDialog extends StatusDialog {

  public NlsStatusDialog(Shell parent, IStatus status) {
    super(parent);
    updateStatus(status);
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID,
        IDialogConstants.OK_LABEL, true);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    if (getStatus().isMultiStatus()) {
      return createMultiStatusContent(parent);
    }
    Composite rootArea = new Composite(parent, SWT.NONE);
    Label severityLabel = new Label(rootArea, SWT.NONE);
    severityLabel.setText("Severity");
    attacheGridData(severityLabel);
    Text severityText = new Text(rootArea, SWT.READ_ONLY);
    severityText.setText(severityToString(getStatus().getSeverity()));
    attacheGridData(severityText);
    Label messageLabel = new Label(rootArea, SWT.NONE);
    messageLabel.setText("Message");
    attacheGridData(messageLabel);
    Text message = new Text(rootArea, SWT.READ_ONLY);
    String messageText = getStatus().getMessage();
    if (messageText == null) {
      messageText = "";
    }
    message.setText(messageText);
    attacheGridData(message);

    // layout
    rootArea.setLayout(new GridLayout(2, true));
    return rootArea;
  }

  private String severityToString(int severity) {
    switch (severity) {
      case IStatus.ERROR:
        return "Error";
      case IStatus.WARNING:
        return "Warning";
      case IStatus.INFO:
        return "Info";
      case IStatus.OK:
        return "OK";
      case IStatus.CANCEL:
        return "Cancel";
      default:
        return "undefined";
    }
  }

  private Control createMultiStatusContent(Composite parent) {
    Composite rootArea = new Composite(parent, SWT.NONE);
    Label l = new Label(rootArea, SWT.NONE);
    l.setText("TODO multistatus handling in NlsStatusDialog");
    return rootArea;
  }

  private void attacheGridData(Control c) {
    GridData data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    c.setLayoutData(data);
  }

}
