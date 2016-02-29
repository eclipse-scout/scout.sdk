/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.simpleproject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
import org.eclipse.scout.sdk.s2e.nls.resource.AbstractTranslationResource;
import org.eclipse.scout.sdk.s2e.nls.resource.TranslationResourceEvent;
import org.eclipse.scout.sdk.s2e.operation.ResourceWriteOperation;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.WeakResourceChangeListener;

/**
 * <h4>WorkspaceTranslationFile</h4>
 */
public class WorkspaceTranslationFile extends AbstractTranslationResource {

  private final IFile m_file;
  private final IResourceChangeListener m_translationFileChangedListener;

  public WorkspaceTranslationFile(IFile file) {
    super(Language.parse(file.getName()));
    Validate.notNull(file);
    Validate.isTrue(file.exists());
    m_file = file;
    m_translationFileChangedListener = new P_TranslationFileChangedListener();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(new WeakResourceChangeListener(m_translationFileChangedListener), IResourceChangeEvent.POST_CHANGE);
    reload(new NullProgressMonitor());
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void reload(IProgressMonitor monitor) {
    try {
      m_file.refreshLocal(IResource.DEPTH_ZERO, monitor);
    }
    catch (CoreException e) {
      SdkLog.warning("Could not refresh file '{}'.", m_file.getLocation().toOSString(), e);
    }

    try (InputStream io = m_file.getContents()) {
      parseResource(io);
    }
    catch (Exception e) {
      SdkLog.error("cold not reload translation file: {}", m_file.getName(), e);
    }
  }

  @Override
  public void updateText(String key, String newText, boolean fireEvent, IProgressMonitor monitor) {
    setTranslation(key, newText, fireEvent, monitor);
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
        SdkLog.error("File: {} not found!", m_file.getName());
      }

      Properties prop = new Properties();
      String nl = NlsCore.getLineSeparator(m_file);

      Map<String, String> allTranslations = getAllTranslations();
      prop.putAll(allTranslations);

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try {
        prop.store(os, null);
        String[] lines = os.toString("8859_1").split(nl); // Properties.store() uses 8859_1 encoding
        int i = 0;
        if (lines.length > 0 && lines[0].startsWith("#")) {
          i++;
        }

        Arrays.sort(lines);

        StringBuilder builder = new StringBuilder();
        for (; i < lines.length; i++) {
          // remove all newline characters because java.lang.Properties class uses OS dependent line delimiters.
          // but we would like to use project dependent line delimiters -> remove all first, add project dependent delimiter afterwards.
          String lineContent = StringUtils.replaceEach(lines[i], new String[]{
              "\n", "\r"
          }, new String[]{
              "", ""
          });
          builder.append(lineContent);
          builder.append(nl);
        }

        S2eUtils.writeResources(Collections.singletonList(new ResourceWriteOperation(m_file, builder.toString())), monitor, true);
      }
      catch (IOException | CoreException e) {
        SdkLog.error("could not refresh file: {}", m_file.getName(), e);
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
        SdkLog.warning(e);
      }
    }
  }
}
