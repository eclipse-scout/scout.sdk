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
package org.eclipse.scout.nls.sdk.ui.action;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.language.TranslationFileNewDialog;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.language.TranslationFileNewModel;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.widgets.Shell;

/**
 * <h4>LanguageFileNewAction</h4>
 * 
 * @see TranslationFileNewDialog
 * @see TranslationFileNewModel
 */
public class TranslationFileNewAction extends AbstractWorkspaceAction {

  private final Shell m_parentShell;
  private final INlsProject m_nlsProject;
  private TranslationFileNewModel m_model;

  /**
   * @param name
   * @param interactWithUi
   */
  public TranslationFileNewAction(Shell parentShell, boolean interactWithUi, INlsProject project) {
    super("New Language...", interactWithUi);
    m_parentShell = parentShell;
    m_nlsProject = project;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return NlsCore.getImageDescriptor("fileadd_pending");
  }

  @Override
  protected boolean interactWithUi() {
    m_model = new TranslationFileNewModel(m_nlsProject);

    TranslationFileNewDialog dialog = new TranslationFileNewDialog(m_parentShell, m_model);
    return (dialog.open() == Dialog.OK);
  }

  @Override
  protected void execute(IProgressMonitor monitor) {
    try {

      m_nlsProject.createTranslationFile(m_model.getLanguage(), m_model.getFolder(), monitor);
    }
    catch (CoreException e) {
      // XXX log
      NlsCore.logWarning(e);
    }

  }

}
