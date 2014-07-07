/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.operation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class FileDeleteOperation implements IOperation {

  private IFile m_file;

  @Override
  public void validate() {
    if (m_file == null) {
      throw new IllegalArgumentException("no file set");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    m_file.delete(true, true, monitor);
  }

  @Override
  public String getOperationName() {
    return FileDeleteOperation.class.getName();
  }

  public IFile getFile() {
    return m_file;
  }

  public void setFile(IFile file) {
    m_file = file;
  }
}
