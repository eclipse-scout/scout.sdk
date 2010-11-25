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
package org.eclipse.scout.nls.sdk.internal.model.workspace.translationfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.TranslationFileEvent;

/**
 * <h4>WorkspaceTranslationFile</h4>
 */
public class WorkspaceTranslationFile extends AbstractTranslationFile {

  private final IFile m_file;

  public WorkspaceTranslationFile(IFile file) {
    super(NlsCore.getLanguage(file.getName()));
    Assert.isTrue(file != null);
    Assert.isTrue(file.exists());
    m_file = file;
    ResourcesPlugin.getWorkspace().addResourceChangeListener(new P_TranslationFileChangedListener(), IResourceChangeEvent.POST_CHANGE);
    reload(new NullProgressMonitor());
  }

  public boolean isReadOnly() {
    return false;
  }

  public IFile getFile() {
    return m_file;
  }

  public String getName() {
    return m_file.getName();
  }

  @Override
  public void reload(IProgressMonitor monitor) {
    try {
      m_file.refreshLocal(IResource.DEPTH_ZERO, monitor);
      parseFile(m_file.getContents());
    }
    catch (Exception e) {
      NlsCore.logError("cold not reload translation file: " + m_file.getName(), e);
    }

  }

  public void updateTextNoFire(String key, String newText, IProgressMonitor monitor) {
    setTranslation(key, newText, false, monitor);
  }

  @Override
  public void updateText(String key, String newText, IProgressMonitor monitor) {
    setTranslation(key, newText, monitor);
  }

  @Override
  public IStatus remove(String key, IProgressMonitor monitor) {
    super.setTranslation(key, null, monitor);
    return Status.OK_STATUS;
  }

  @Override
  public IStatus updateKey(String oldKey, String newKey, IProgressMonitor monitor) {
    // remove old
    String translation = getTranslation(oldKey);
    setTranslation(oldKey, null, monitor);
    // add new
    setTranslation(newKey, translation, monitor);
    return Status.OK_STATUS;
  }

  @Override
  public void commitChanges(IProgressMonitor monitor) {
    synchronized (m_file) {

      if (!m_file.exists()) {
        NlsCore.logError("File: " + m_file.getName() + " not found!");
        // return new Status(IStatus.WARNING, NlsCore.PLUGIN_ID, IStatus.OK, "File: " + m_file.getName() + " not found!", null);
      }

      Properties prop = new Properties();

      Map<String, String> allTranslations = getAllTranslations();
      prop.putAll(allTranslations);

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try {
        prop.store(os, null);
        String[] lines = os.toString().split(System.getProperty("line.separator"));
        int i = 0;
        if (lines.length > 0 && lines[0].startsWith("#")) {
          i++;
        }

        Arrays.sort(lines);
        String NL = System.getProperty("line.separator");
        StringBuffer buffer = new StringBuffer();
        for (; i < lines.length; i++) {
          buffer.append(lines[i] + NL);
        }

        m_file.setContents(new ByteArrayInputStream(buffer.toString().getBytes()), IFile.KEEP_HISTORY, monitor);
        m_file.refreshLocal(IResource.DEPTH_ONE, monitor);
        // return Status.OK_STATUS;
      }
      catch (IOException e1) {
        NlsCore.logError("could not refresh file: " + m_file.getName());
        // return new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, IStatus.OK, "could not refresh file: " + m_file.getName(), null);
      }
      catch (CoreException e1) {
        NlsCore.logError("could not refresh file: " + m_file.getName());
        // return new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, IStatus.OK, "could not refresh file: " + m_file.getName(), null);
      }

    }
  }

  private class P_TranslationFileChangedListener implements IResourceChangeListener {
    public void resourceChanged(IResourceChangeEvent event) {
      IResourceDelta delta = event.getDelta();
      try {
        if (delta != null) {
          delta.accept(new IResourceDeltaVisitor() {
            public boolean visit(IResourceDelta delta) {
              IResource resource = delta.getResource();
              if (resource != null && resource.equals(m_file)) {
                if (delta.getKind() == IResourceDelta.REMOVED) {
                  fireTranslationFileChanged(new TranslationFileEvent(WorkspaceTranslationFile.this, TranslationFileEvent.TYPE_FILE_REMOVED));
                }
                else {
                  reload(new NullProgressMonitor());
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

  }
}
