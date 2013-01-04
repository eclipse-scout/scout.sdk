/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class WsdlFolderViewerFilter extends ViewerFilter {

  private IScoutBundle m_bundle;
  private IFolder m_rootFolder;

  public WsdlFolderViewerFilter(IScoutBundle bundle, IFolder rootFolder) {
    m_bundle = bundle;
    m_rootFolder = rootFolder;
  }

  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element) {
    if (!(element instanceof IFolder)) {
      return false;
    }
    IFolder candidateFolder = (IFolder) element;
    // exclude hidden folders
    if (candidateFolder.getName().startsWith(".")) {
      return false;
    }

    if (m_rootFolder == null || !m_rootFolder.exists()) {
      return false;
    }

    // only accept subfolders of WSDL root folder
    IPath wsdlRootPath = m_rootFolder.getProjectRelativePath();
    IPath candidatePath = candidateFolder.getProjectRelativePath();
    candidatePath = candidatePath.makeRelativeTo(wsdlRootPath);
    if (candidatePath.toString().startsWith("..")) {
      return false;
    }
    return m_rootFolder.exists(candidatePath);
  }
}
