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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.widgets.Shell;

/**
 * <h4>TranslationNewAction</h4>
 *
 * @see INewLanguageContext
 */
public class TranslationNewAction extends AbstractWorkspaceAction {

  private final Shell m_shell;
  private final INlsProject m_project;
  private INewLanguageContext m_context;

  /**
   * @param name
   * @param interactWithUi
   */
  public TranslationNewAction(INlsProject project, Shell s) {
    super("New Language...", true);
    m_context = null;
    m_project = project;
    m_shell = s;
    setEnabled(project != null && !project.isReadOnly());
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return NlsCore.getImageDescriptor("fileadd_pending");
  }

  @Override
  protected boolean interactWithUi() {
    m_context = m_project.getTranslationCreationContext();
    return m_context.interactWithUi(m_shell);
  }

  @Override
  protected void execute(IProgressMonitor monitor) {
    m_context.execute(monitor);
    m_context = null;
  }
}
