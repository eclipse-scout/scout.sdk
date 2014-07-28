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
package org.eclipse.scout.sdk.ui.internal.service;

import org.eclipse.scout.commons.holders.IntegerHolder;
import org.eclipse.scout.sdk.service.IMessageBoxService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * SWT-based {@link IMessageBoxService} implementation that presents a message box to the user.
 *
 * @since 3.10.0
 */
public class SwtMessageBoxService implements IMessageBoxService {

  /**
   * Shows an SWT message box with question mark as well as a <em>Yes</em> and <em>No</em> button. The default answer is
   * returned if there is no active shell.
   */
  @Override
  public YesNo showYesNoQuestion(String title, String message, YesNo defaultAnswer) {
    int button = showMessageBox(title, message, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    switch (button) {
      case SWT.YES:
        return YesNo.YES;
      case SWT.NO:
        return YesNo.NO;
      default:
        return defaultAnswer;
    }
  }

  @Override
  public void showWarning(String title, String message) {
    showMessageBox(title, message, SWT.ICON_WARNING | SWT.OK);
  }

  protected int showMessageBox(final String title, final String message, final int style) {
    final IntegerHolder result = new IntegerHolder(SWT.NONE);
    final Display display = getDisplay();
    if (display != null) {
      display.syncExec(new Runnable() {
        @Override
        public void run() {
          Shell shell = display.getActiveShell();
          if (shell == null) {
            return;
          }
          MessageBox box = new MessageBox(shell, style);
          box.setText(title);
          box.setMessage(message);
          result.setValue(box.open());
        }
      });
    }
    return result.getValue();
  }

  protected Display getDisplay() {
    Display d = Display.getDefault();
    if (d == null) {
      d = PlatformUI.getWorkbench().getDisplay();
    }
    return d;
  }
}
