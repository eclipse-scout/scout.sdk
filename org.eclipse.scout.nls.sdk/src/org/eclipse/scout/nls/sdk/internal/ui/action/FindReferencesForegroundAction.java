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
package org.eclipse.scout.nls.sdk.internal.ui.action;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtHandler;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.ProjectChooserDialog;
import org.eclipse.ui.IWorkbenchSite;

public class FindReferencesForegroundAction extends Action {
  private IWorkbenchSite m_site;
  private IJavaElement m_element;
  private boolean m_chooseProjects;

  public FindReferencesForegroundAction(String title, IWorkbenchSite site, IJavaElement element, boolean chooseProjects) {
    super(title);
    m_site = site;
    m_element = element;
    m_chooseProjects = chooseProjects;

  }

  @Override
  public void run() {
    Collection<IProject> projects = null;
    if (m_chooseProjects) {
      ProjectChooserDialog dailog = new ProjectChooserDialog(m_site.getShell(), "Project selection");
      dailog.open();
      projects = dailog.getSelection();
    }
    NlsJdtHandler.findReferencesForeground(m_site, m_element, projects);
  }
}
