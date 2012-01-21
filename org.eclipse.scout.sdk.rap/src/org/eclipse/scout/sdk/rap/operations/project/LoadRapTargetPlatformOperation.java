package org.eclipse.scout.sdk.rap.operations.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.TargetPlatformUtility;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class LoadRapTargetPlatformOperation extends AbstractScoutProjectNewOperation {

  private IFile m_targetFile;

  @Override
  public String getOperationName() {
    return "Load Scout RAP Target Platform";
  }

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    m_targetFile = getProperties().getProperty(FillUiRapPluginOperation.PROP_TARGET_FILE, IFile.class);
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (m_targetFile == null) {
      throw new IllegalArgumentException("target file can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    TargetPlatformUtility.resolveTargetPlatform(m_targetFile, true, monitor);
  }
}
