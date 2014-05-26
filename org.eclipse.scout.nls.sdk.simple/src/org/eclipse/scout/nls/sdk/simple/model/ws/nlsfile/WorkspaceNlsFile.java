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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.sdk.util.resources.IResourceFilter;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
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
      try {
        // check if our file is part of the delta
        IResourceDelta delta = event.getDelta();
        List<IResource> matches = ResourceUtility.getAllResources(delta, new IResourceFilter() {
          @Override
          public boolean accept(IResourceProxy resource) {
            return m_file.equals(resource.requestResource());
          }
        });

        if (!matches.isEmpty()) {
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
