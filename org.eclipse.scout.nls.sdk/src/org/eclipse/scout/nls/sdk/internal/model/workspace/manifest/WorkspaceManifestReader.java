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
package org.eclipse.scout.nls.sdk.internal.model.workspace.manifest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.nls.sdk.NlsCore;

/**
 * <h4>ManifestReader</h4> makes all manifest entries available.
 */
public class WorkspaceManifestReader extends AbstractManifest {

  private IFile m_manifestFile;

  /**
   * creates an accessible manifest file
   * 
   * @param manifest
   *          the manifest file
   * @throws CoreException
   */
  public WorkspaceManifestReader(IFile manifest) throws CoreException {
    super(manifest.getProject().getName(), manifest.getContents());
    m_manifestFile = manifest;
  }

  public WorkspaceManifestReader(IProject project) throws CoreException {
    super(project.getName(), project.getFile(new Path("META-INF/MANIFEST.MF")).getContents());
    m_manifestFile = project.getFile(new Path("META-INF/MANIFEST.MF"));
  }

  @Override
  public boolean isWriteable() {
    return true;
  }

  public void addImportBundle(String bundleName, IProgressMonitor monitor) throws CoreException, IOException {
    boolean appended = false;
    String newLine = System.getProperty("line.separator");
    StringBuffer buffer = new StringBuffer();
    BufferedReader reader = new BufferedReader(new InputStreamReader(m_manifestFile.getContents()));
    String line = reader.readLine();
    while (line != null) {
      buffer.append(line + newLine);
      if (line.startsWith("Require-Bundle:")) {
        line = reader.readLine();
        while (line != null && line.startsWith(" ")) {
          buffer.append(line + newLine);
          line = reader.readLine();
        }
        int index = buffer.lastIndexOf(newLine);
        buffer.insert(index, ",");
        buffer.append(" " + bundleName + newLine);
        appended = true;
        buffer.append(line);
      }
      line = reader.readLine();
    }
    if (!appended) {
      buffer.append("Require-Bundle: " + bundleName + newLine);
    }
    buffer.append(newLine);
    m_manifestFile.setContents(new ByteArrayInputStream(buffer.toString().getBytes()), true, true, monitor);
    m_manifestFile.refreshLocal(IFile.DEPTH_INFINITE, monitor);
  }

  @Override
  public IStatus store(IProgressMonitor monitor) {
    final String NL = System.getProperty("line.separator");
    StringBuffer writer = new StringBuffer();
    for (String entryId : m_entryOrder) {
      ManifestEntry entry = m_parsedEntries.get(entryId);
      writer.append(entry.getKey() + ": ");
      for (Iterator<ManifestElement> it = entry.getElements().iterator(); it.hasNext();) {
        ManifestElement element = it.next();
        writer.append(element.getValue());
        for (Entry<String, String> prop : element.getPropertyMap().entrySet()) {
          writer.append(";" + prop.getKey() + "=" + prop.getValue());
        }
        if (it.hasNext()) {
          writer.append("," + NL + "  ");
        }
        else {
          writer.append(NL);
        }
      }
    }
    try {
      m_manifestFile.setContents(new ByteArrayInputStream(writer.toString().getBytes()), true, false, monitor);
    }
    catch (CoreException e) {
      // TODO Auto-generated catch block
      NlsCore.logWarning(e);
      return Status.CANCEL_STATUS;
    }
    return Status.OK_STATUS;
  }

}
