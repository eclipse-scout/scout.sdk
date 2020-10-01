/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.environment;

import static java.util.Collections.singletonList;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.util.CharSequenceInputStream;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link ResourceWriteOperation}</h3>
 * <p>
 * Change the content of a resource file
 *
 * @since 5.1.0
 */
public class ResourceWriteOperation implements IResourceWriteOperation {
  private final IFile m_file;
  private final CharSequence m_content;

  protected ResourceWriteOperation(IFile file, CharSequence content) {
    m_file = Ensure.notNull(file, "File cannot be null");
    m_content = Ensure.notNull(content, "File content cannot be null");
  }

  @Override
  public IFile getFile() {
    return m_file;
  }

  @Override
  public void accept(EclipseProgress progress) {
    progress.init(3, "Write {}", m_file.getProjectRelativePath());

    try {
      // check if write is necessary
      if (areContentsEqual(m_file, m_content)) {
        return;
      }
      progress.setWorkRemaining(2);
      writeFile(progress.newChild(2));
    }
    catch (CoreException | IOException e) {
      SdkLog.error("could not store '{}'.", m_file.getProjectRelativePath(), e);
    }
  }

  protected void writeFile(@SuppressWarnings("TypeMayBeWeakened") EclipseProgress progress) throws IOException, CoreException {
    String charsetName = m_file.getCharset();
    try (InputStream stream = new CharSequenceInputStream(m_content, charsetName)) {
      if (!m_file.exists()) {
        mkdirs(m_file.getParent(), progress.newChild(1).monitor());
        m_file.create(stream, true, progress.newChild(1).monitor());
      }
      else {
        IStatus result = S2eUtils.makeCommittable(singletonList(m_file));
        if (result.isOK()) {
          m_file.setContents(stream, true, true, progress.newChild(2).monitor());
        }
        else {
          SdkLog.warning("Unable to make all resources committable. Save will be skipped.", new CoreException(result));
        }
      }
    }
  }

  public static boolean areContentsEqual(IFile file, CharSequence newContent) {
    if (file == null || !file.exists()) {
      return false;
    }
    try (InputStream in = file.getContents()) {
      StringBuilder fileContent = Strings.fromInputStream(in, file.getCharset());
      return Strings.equals(fileContent, newContent);
    }
    catch (IOException | CoreException e) {
      SdkLog.warning("Unable to read contents of file '{}'.", file, e);
      return false; // anyway try to write it
    }
  }

  public static void mkdirs(IResource dir, IProgressMonitor monitor) throws CoreException {
    if (dir.getType() != IResource.FOLDER) {
      return;
    }
    if (!dir.getParent().exists()) {
      mkdirs(dir.getParent(), monitor);
    }
    if (!dir.exists()) {
      ((IFolder) dir).create(true, false, monitor);
    }
  }

  @Override
  public IResource getAffectedResource() {
    IResource curResource = m_file;
    while (curResource != null && !curResource.exists()) {
      curResource = curResource.getParent();
    }
    return curResource;
  }

  @Override
  public String toString() {
    return "Write " + m_file.getProjectRelativePath();
  }
}
