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
 * <h4>NlsImportAction</h4>
 */
public class TranslationImportAction extends Action {

  private final Shell m_parentShell;
  private final TranslationManager m_nlsProject;

  public TranslationImportAction(TranslationManager nlsProject, Shell parentShell) {
    super("Import Translations...");
    m_nlsProject = nlsProject;
    m_parentShell = parentShell;

    setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_IMPORT_WIZ));
    setEnabled(!TranslationImportExportExtensionPoint.getImporters().isEmpty() && m_nlsProject.isEditable());
  }

  @Override
  public void run() {
    var wizard = new TranslationImportExportWizard("Import Translations", "Please choose an importer.",
        m_nlsProject, TranslationImportExportExtensionPoint.getImporters());
    Window dialog = new WizardDialog(m_parentShell, wizard);
    dialog.setBlockOnOpen(true);
    dialog.open();
  }
}
