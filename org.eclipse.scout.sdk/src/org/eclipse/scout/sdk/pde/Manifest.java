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
package org.eclipse.scout.sdk.pde;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.ScoutSdk;

/**
 * <h4>Manifest</h4> makes all manifest entries available for read and write
 * , Ivan Motsch
 */
public class Manifest extends RawManifest {

  public void read(IProject p) throws CoreException, FileNotFoundException {
    IFile file = p.getFile(MAINFEST_MF_PATH);
    if (file != null && file.exists()) {
      read(file);
    }
    else {
      throw new FileNotFoundException("no manifest found.");
    }
  }

  /**
   * @param file
   * @throws CoreException
   * @throws IOException
   */
  public void read(IFile file) throws CoreException {
    if (!file.exists()) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not find manifest of " + file.getProject().getName()));
    }
    file.refreshLocal(IResource.DEPTH_ONE, null);
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(file.getContents());
      read(reader);
    }
    catch (IOException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not read manifest of " + file.getProject().getName()));
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (Exception e) {
        }
      }
    }
  }

  public void write(IProject p) throws CoreException, IOException {
    write(p.getFile(MAINFEST_MF_PATH));
  }

  /**
   * @param file
   * @throws CoreException
   * @throws IOException
   */
  public void write(IFile file) throws CoreException, IOException {
    StringWriter w = new StringWriter();
    write(w);
    ByteArrayInputStream is = null;
    try {
      is = new ByteArrayInputStream(w.toString().getBytes("UTF-8"));
      file.setContents(is, true, false, null);
    }
    finally {
      if (is != null) {
        is.close();
      }
    }
    file.refreshLocal(IResource.DEPTH_ONE, null);
  }
}
