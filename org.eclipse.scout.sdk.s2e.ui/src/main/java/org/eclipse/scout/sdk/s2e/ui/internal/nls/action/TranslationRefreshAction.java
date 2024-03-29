/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.NlsTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

/**
 * <h4>NlsRefreshAction</h4>
 */
public class TranslationRefreshAction extends Action {

  private final TranslationManager m_nlsProject;
  private final NlsTable m_table;

  public TranslationRefreshAction(TranslationManager project, NlsTable table) {
    super("Refresh Translations");
    m_nlsProject = project;
    m_table = table;
    setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.Refresh));
  }

  @Override
  public void run() {
    if (m_nlsProject.isDirty()) {
      var msgBox = new MessageBox(m_table.getShell(), SWT.YES | SWT.NO | SWT.CANCEL);
      //noinspection HardcodedLineSeparator
      msgBox.setMessage("There are unsaved changes. By refreshing all changes will be lost.\nDo you want to save your changes before refreshing?");
      msgBox.setText("Save changes before refreshing?");
      var result = msgBox.open();
      if (result == SWT.CANCEL) {
        return;
      }
      if (result == SWT.YES) {
        runInEclipseEnvironment(m_nlsProject::flush).awaitDoneThrowingOnErrorOrCancel();
      }
    }

    runInEclipseEnvironment(this::execute).awaitDoneThrowingOnErrorOrCancel();
  }

  protected void execute(IEnvironment env, IProgress progress) {
    m_nlsProject.reload(env, progress.init(10000, "Refresh translations"));
  }
}
