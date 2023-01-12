/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.importexport;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * <h4>NlsExportAction</h4>
 *
 * @since 1.1.0 (12.11.2010)
 */
public class TranslationExportAction extends Action {

  private final Shell m_parentShell;
  private final TranslationManager m_nlsProject;

  public TranslationExportAction(TranslationManager nlsProject, Shell shell) {
    super("Export Translations...");
    m_nlsProject = nlsProject;
    m_parentShell = shell;

    setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_EXPORT_WIZ));
    setEnabled(!TranslationImportExportExtensionPoint.getExporters().isEmpty());
  }

  @Override
  public void run() {
    var wizard = new TranslationImportExportWizard("Export Translations", "Please choose an exporter.",
        m_nlsProject, TranslationImportExportExtensionPoint.getExporters());
    Window dialog = new WizardDialog(m_parentShell, wizard);
    dialog.setBlockOnOpen(true);
    dialog.open();
  }
}
