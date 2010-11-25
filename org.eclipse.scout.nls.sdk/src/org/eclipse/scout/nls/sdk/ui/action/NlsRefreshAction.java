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
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.workspace.project.NlsProject;

/**
 * <h4>NlsRefreshAction</h4>
 */
public class NlsRefreshAction extends AbstractWorkspaceAction {

  private NlsProject m_nlsProject;

  private final boolean m_reloadFiles;

  /**
   * @param name
   * @param interactWithUi
   */
  public NlsRefreshAction(NlsProject project, boolean reloadFiles) {
    super("Refresh NLS Project...", true);
    m_nlsProject = project;
    m_reloadFiles = reloadFiles;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return NlsCore.getImageDescriptor(NlsCore.ICON_TOOL_REFRESH);
  }

  @Override
  protected void execute(IProgressMonitor monitor) {
    m_nlsProject.fullRefresh(m_reloadFiles);
  }

  public NlsProject getNlsProject() {
    return m_nlsProject;
  }

}
