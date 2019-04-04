/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.NlsTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

/**
 * <h4>NlsRefreshAction</h4>
 */
public class TranslationRefreshAction extends Action {

  private final TranslationStoreStack m_nlsProject;
  private final NlsTable m_table;

  public TranslationRefreshAction(TranslationStoreStack project, NlsTable table) {
    super("Refresh Translations");
    m_nlsProject = project;
    m_table = table;
    setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.Refresh));
  }

  @Override
  public void run() {
    if (m_nlsProject.isDirty()) {
      MessageBox msgBox = new MessageBox(m_table.getShell(), SWT.YES | SWT.NO | SWT.CANCEL);
      msgBox.setMessage("There are unsaved changes. By refreshing all changes will be lost.\nDo you want to save your changes before refreshing?");
      msgBox.setText("Save changes before refreshing?");
      int result = msgBox.open();
      if (result == SWT.CANCEL) {
        return;
      }
      if (result == SWT.YES) {
        runInEclipseEnvironment(m_nlsProject::flush).awaitDoneThrowingOnErrorOrCancel();
      }
    }

    runInEclipseEnvironment((env, progress) -> execute(progress)).awaitDoneThrowingOnErrorOrCancel();
  }

  protected void execute(IProgress progress) {
    m_nlsProject.reload(progress.init("Refresh translations", 100));
  }
}
