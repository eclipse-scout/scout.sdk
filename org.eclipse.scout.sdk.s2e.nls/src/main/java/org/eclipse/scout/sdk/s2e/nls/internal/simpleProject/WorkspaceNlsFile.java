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
package org.eclipse.scout.sdk.s2e.nls.internal.simpleProject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.util.WeakResourceChangeListener;

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
      try {
        // check if our file is part of the delta
        IResourceDelta delta = event.getDelta();
        final boolean[] myFileFound = new boolean[1];
        delta.accept(new IResourceDeltaVisitor() {
          @Override
          public boolean visit(IResourceDelta d) throws CoreException {
            if (myFileFound[0]) {
              return false;
            }

            myFileFound[0] = m_file.equals(d.getResource());
            return !myFileFound[0];
          }
        });

        if (myFileFound[0]) {
          // it is part of the delta: parse again
          parseInput(m_file);
        }
      }
      catch (CoreException e) {
        NlsCore.logWarning(e);
      }
    }
  } // end class P_NlsFileChangeListener
}
