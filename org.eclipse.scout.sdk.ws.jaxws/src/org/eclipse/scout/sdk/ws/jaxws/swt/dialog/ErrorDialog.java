/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.dialog;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ErrorDialog extends Dialog {
  private static final String DEFAULT_ERROR_MESSAGE = "An unexpected error occured";

  private String m_title;
  private String m_message;
  private String m_stackTrace;
  private int m_severity;

  public ErrorDialog(String title) {
    super(ScoutSdkUi.getDisplay().getActiveShell());
    m_title = title;
    setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE);
  }

  @Override
  protected final void configureShell(Shell shell) {
    Rectangle clientArea = shell.getDisplay().getClientArea();
    int width = NumberUtility.nvl(700, shell.getBounds().width);
    int height = NumberUtility.nvl(500, shell.getBounds().height);
    shell.setLocation((clientArea.width - width) / 2, (clientArea.height - height) / 2);
    shell.setSize(width, height);

    super.configureShell(shell);

    shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    shell.setText(m_title);
    shell.setLayout(new FillLayout());

  }

  @Override
  protected final Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.INHERIT_FORCE);
    Composite content = new Composite(container, SWT.INHERIT_FORCE);

    Label iconLabel = new Label(content, SWT.NONE);
    iconLabel.setImage(getImageStatus(m_severity));
    Label messageLabel = new Label(content, SWT.NONE);
    messageLabel.setText(m_message);
    messageLabel.setFont(getFont(JFaceResources.DIALOG_FONT, true));

    Text stackTraceText = new Text(content, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
    stackTraceText.setEditable(false);
    stackTraceText.setText(m_stackTrace);
    stackTraceText.setBackground(content.getDisplay().getSystemColor(SWT.COLOR_WHITE));

    Button button = new Button(container, SWT.PUSH);
    button.setText("OK");
    button.addSelectionListener(new CloseSelectionListener());
    getShell().setDefaultButton(button);

    // layout
    container.setLayout(new GridLayout());
    content.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

    // button
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    gridData.horizontalAlignment = SWT.CENTER;
    gridData.widthHint = button.getBounds().width + 75;
    button.setLayoutData(gridData);

    // content
    content.setLayout(new FormLayout());
    FormData data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(0, 0);
    data.right = new FormAttachment(0, 30);
    iconLabel.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(0, 0);
    data.left = new FormAttachment(iconLabel, 10, SWT.RIGHT);
    data.right = new FormAttachment(100, -10);
    messageLabel.setLayoutData(data);

    data = new FormData();
    data.top = new FormAttachment(messageLabel, 10, SWT.BOTTOM);
    data.left = new FormAttachment(messageLabel, 0, SWT.LEFT);
    data.right = new FormAttachment(100, -10);
    data.bottom = new FormAttachment(100, 0);
    stackTraceText.setLayoutData(data);

    return container;
  }

  public void setError(String error, int severity) {
    m_severity = severity;
    m_message = error;
    m_stackTrace = "";
  }

  public void setError(Throwable e) {
    m_severity = IStatus.ERROR;
    m_message = DEFAULT_ERROR_MESSAGE;
    m_stackTrace = getStackTrace(e);
  }

  public void setError(String message, Throwable e) {
    m_severity = IStatus.ERROR;
    m_message = StringUtility.nvl(message, DEFAULT_ERROR_MESSAGE);
    m_stackTrace = getStackTrace(e);
  }

  private Font getFont(String symbolicName, boolean bold) {
    if (bold) {
      return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().getBold(symbolicName);
    }
    else {
      return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(symbolicName);
    }
  }

  private Image getImageStatus(int s) {
    Image image = null;
    switch (s) {
      case IStatus.ERROR:
        image = ScoutSdkUi.getImage(ScoutSdkUi.StatusError);
        break;
      case IStatus.WARNING:
        image = ScoutSdkUi.getImage(ScoutSdkUi.StatusWarning);
        break;
      case IStatus.INFO:
        image = ScoutSdkUi.getImage(ScoutSdkUi.StatusInfo);
        break;
    }
    return image;
  }

  private String getStackTrace(Throwable e) {
    StringWriter writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    return writer.toString();
  }

  /**
   * listener for closing the window
   */
  private class CloseSelectionListener extends SelectionAdapter {

    @Override
    public void widgetSelected(SelectionEvent e) {
      setReturnCode(Window.OK);
      close();
    }
  }
}
