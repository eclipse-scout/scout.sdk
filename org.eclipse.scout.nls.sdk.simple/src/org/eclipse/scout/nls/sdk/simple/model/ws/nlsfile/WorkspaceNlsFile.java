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
package org.eclipse.scout.nls.sdk.simple.model.ws.nlsfile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.sdk.util.resources.WeakResourceChangeListener;

public class WorkspaceNlsFile extends AbstractNlsFile {

  private final IFile m_file;
  private final IResourceChangeListener m_translationFileChangedListener;

  /**
   * @param stream
   * @throws CoreException
   */
  public WorkspaceNlsFile(IFile file) throws CoreException {
    super(file);
    m_file = file;
    m_translationFileChangedListener = new P_NlsFileChangeListener();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(new WeakResourceChangeListener(m_translationFileChangedListener), IResourceChangeEvent.POST_CHANGE);
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  private class P_NlsFileChangeListener implements IResourceChangeListener {
    @Override
    public void resourceChanged(IResourceChangeEvent event) {
      IResourceDelta delta = event.getDelta();
      try {
        if (delta != null) {
          delta.accept(new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta d) {
              IResource resource = d.getResource();
              if (resource != null && resource.equals(m_file)) {
                if (m_file.exists()) {
                  parseInput(m_file);
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
