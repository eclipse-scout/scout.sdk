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
package org.eclipse.scout.nls.sdk.internal.model.workspace.nlsfile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.nls.sdk.NlsCore;

public class WorkspaceNlsFile extends AbstractNlsFile {

  private IFile m_file;

  /**
   * @param stream
   * @throws CoreException
   */
  public WorkspaceNlsFile(IFile file) throws CoreException {
    super(file.getContents(), file.getName());
    m_file = file;
    ResourcesPlugin.getWorkspace().addResourceChangeListener(new P_NlsFileChangeListener(), IResourceChangeEvent.POST_CHANGE);
  }

  public IProject getProject() {
    return m_file.getProject();
  }

  public boolean isReadOnly() {
    return false;
  }

  private class P_NlsFileChangeListener implements IResourceChangeListener {
    public void resourceChanged(IResourceChangeEvent event) {
      IResourceDelta delta = event.getDelta();
      try {
        if (delta != null) {
          delta.accept(new IResourceDeltaVisitor() {
            public boolean visit(IResourceDelta delta) {
              IResource resource = delta.getResource();
              if (resource != null && resource.equals(m_file)) {
                if (m_file.exists()) {
                  try {
                    parseInput(m_file.getContents(), m_file.getFullPath().toString());
                  }
                  catch (CoreException e) {
                    NlsCore.logError("could not open stream to read NLS file :'" + m_file.getFullPath() + "'");
                  }
                }
              }
              return true;
            }
          });
        }
      }
      catch (CoreException e) {
        NlsCore.logWarning(e);
      }
    }
  } // end class P_NlsFileChangeListener

}
