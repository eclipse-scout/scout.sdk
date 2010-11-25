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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.axis.util;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.holders.ObjectHolder;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class FileListDialog extends TitleAreaDialog {

  /**
   * see {@link #show(Shell, String, String, String, List)}
   */
  @SuppressWarnings("unchecked")
  public static List<IFile> showThreadSafe(final String title, final String subTitle, final String message, final List<IFile> files) {
    if (ScoutSdkUi.getDisplay().getThread() == Thread.currentThread()) {
      return show(ScoutSdkUi.getShell(), title, subTitle, message, files);
    }
    else {
      final ObjectHolder result = new ObjectHolder();
      ScoutSdkUi.getDisplay().syncExec(
          new Runnable() {
            public void run() {
              result.setValue(show(ScoutSdkUi.getShell(), title, subTitle, message, files));
            }
          }
          );
      return (List<IFile>) result.getValue();
    }
  }

  /**
   * Convenience that shows the dialog, returns null when cancelled and the checked file array on ok
   * 
   * @param shell
   *          is optional
   */
  public static List<IFile> show(Shell shell, String title, String subTitle, String message, List<IFile> files) {
    if (shell == null) shell = new Shell();
    FileListDialog dialog = new FileListDialog(shell);
    dialog.create();
    //
    dialog.getShell().setText(title);
    dialog.setTitle(subTitle);
    dialog.setMessage(message, IMessageProvider.INFORMATION);
    dialog.getFileListViewer().setFiles(files);
    dialog.getShell().setSize(800, 600);
    if (dialog.open() == Window.OK) {
      return dialog.getFileListViewer().getCheckedFiles();
    }
    else {
      return null;
    }
  }

  private FileListViewer m_viewer;

  public FileListDialog(Shell parent) {
    super(parent);
  }

  public FileListViewer getFileListViewer() {
    return m_viewer;
  }

  @Override
  protected boolean isResizable() {
    return true;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite c = (Composite) super.createDialogArea(parent);
    //
    m_viewer = new FileListViewer(c, SWT.NONE);
    m_viewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    //
    return c;
  }

  @Override
  protected void okPressed() {
    getFileListViewer().saveResult();
    super.okPressed();
  }

}
