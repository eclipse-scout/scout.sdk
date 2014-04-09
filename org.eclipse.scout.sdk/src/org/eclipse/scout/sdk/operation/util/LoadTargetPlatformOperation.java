package org.eclipse.scout.sdk.operation.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.TargetPlatformUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class LoadTargetPlatformOperation implements IOperation {

  private final IFile m_targetFile;

  public LoadTargetPlatformOperation(IFile targetFile) {
    m_targetFile = targetFile;
  }

  @Override
  public String getOperationName() {
    return "Load Target Platform";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getTargetFile() == null) {
      throw new IllegalArgumentException("target file cannot be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    TargetPlatformUtility.resolveTargetPlatform(getTargetFile(), true, monitor);
  }

  public IFile getTargetFile() {
    return m_targetFile;
  }
}
