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
package org.eclipse.scout.nls.sdk.simple.model.ws.translationfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.AbstractTranslationResource;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.TranslationResourceEvent;
import org.eclipse.scout.nls.sdk.simple.internal.NlsSdkSimple;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;

/**
 * <h4>WorkspaceTranslationFile</h4>
 */
public class WorkspaceTranslationFile extends AbstractTranslationResource {

  private final IFile m_file;

  public WorkspaceTranslationFile(IFile file) {
    super(NlsSdkSimple.getLanguage(file.getName()));
    Assert.isTrue(file != null);
    Assert.isTrue(file.exists());
    m_file = file;
    ResourcesPlugin.getWorkspace().addResourceChangeListener(new P_TranslationFileChangedListener(), IResourceChangeEvent.POST_CHANGE);
    reload(new NullProgressMonitor());
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void reload(IProgressMonitor monitor) {
    InputStream io = null;
    try {
      m_file.refreshLocal(IResource.DEPTH_ZERO, monitor);
      io = m_file.getContents();
      parseResource(io);
    }
    catch (Exception e) {
      NlsCore.logError("cold not reload translation file: " + m_file.getName(), e);
    }
    finally {
      if (io != null) {
        try {
          io.close();
        }
        catch (IOException e) {
          NlsSdkSimple.logWarning("could not close input stream of file '" + m_file.getFullPath() + "'.", e);
        }
      }
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
    super.setTranslation(key, null, false, monitor);
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
      }

      Properties prop = new Properties();
      String NL = ResourceUtility.getLineSeparator(m_file);

      Map<String, String> allTranslations = getAllTranslations();
      prop.putAll(allTranslations);

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try {
        prop.store(os, null);
        String[] lines = os.toString().split(NL);
        int i = 0;
        if (lines.length > 0 && lines[0].startsWith("#")) {
          i++;
        }

        Arrays.sort(lines);

        StringBuilder builder = new StringBuilder();
        for (; i < lines.length; i++) {
          // remove all newline characters because java.lang.Properties class uses OS dependent line delimiters.
          // but we would like to use project dependent line delimiters -> remove all first, add project dependent delimiter afterwards.
          builder.append(lines[i].replace("\n", "").replace("\r", ""));
          builder.append(NL);
        }

        m_file.setContents(new ByteArrayInputStream(builder.toString().getBytes()), IFile.KEEP_HISTORY, monitor);
        m_file.refreshLocal(IResource.DEPTH_ONE, monitor);
      }
      catch (IOException e1) {
        NlsCore.logError("could not refresh file: " + m_file.getName());
      }
      catch (CoreException e1) {
        NlsCore.logError("could not refresh file: " + m_file.getName());
      }
    }
  }

  private class P_TranslationFileChangedListener implements IResourceChangeListener {
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
                if (d.getKind() == IResourceDelta.REMOVED) {
                  fireTranslationResourceChanged(new TranslationResourceEvent(WorkspaceTranslationFile.this, TranslationResourceEvent.TYPE_ENTRY_REMOVED));
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
