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
package org.eclipse.scout.sdk.operation.template;

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

public class InstallBinaryFileOperation implements IOperation {

  private String m_srcPath;
  private String m_dstPath;
  private final IProject m_dstProject;

  public InstallBinaryFileOperation(String srcPath, String dstPath, IProject dstProject) {
    m_srcPath = srcPath;
    m_dstPath = dstPath;
    m_dstProject = dstProject;
  }

  public String getOperationName() {
    return "Install binary file " + m_srcPath + " to /" + getDstProject().getName() + "/" + m_dstPath;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getDstProject() == null) {
      throw new IllegalArgumentException("project is null");
    }
  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      byte[] data = IOUtility.getContent(FileLocator.openStream(Platform.getBundle(ScoutSdk.PLUGIN_ID), new Path(m_srcPath), false));
      File f = new File(new File(getDstProject().getLocation().toOSString()), m_dstPath);
      f.getParentFile().mkdirs();
      IOUtility.writeContent(new FileOutputStream(f), data);
      getDstProject().getFile(new Path(m_dstPath)).refreshLocal(IResource.DEPTH_ZERO, monitor);
    }
    catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "", e));
    }
  }

  public IProject getDstProject() {
    return m_dstProject;
  }
}
