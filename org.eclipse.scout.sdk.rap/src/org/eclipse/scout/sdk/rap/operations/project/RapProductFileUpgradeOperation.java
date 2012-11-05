package org.eclipse.scout.sdk.rap.operations.project;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.internal.PlatformVersionUtility;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.util.JettyProductFileUpgradeOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class RapProductFileUpgradeOperation extends AbstractScoutProjectNewOperation {

  private IFile[] m_rapProdFiles;

  @Override
  public boolean isRelevant() {
    return PlatformVersionUtility.isJunoOrLater(getTargetPlatformVersion()) && isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    ArrayList<IFile> productFiles = new ArrayList<IFile>(1);

    // only add dev-product as prod product has no jetty.
    IFile dev = getProperties().getProperty(CreateUiRapPluginOperation.PROP_PRODUCT_FILE_DEV, IFile.class);
    if (dev != null) productFiles.add(dev);

    m_rapProdFiles = productFiles.toArray(new IFile[productFiles.size()]);
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (m_rapProdFiles == null || m_rapProdFiles.length != 1) {
      throw new IllegalArgumentException("rap dev product file not found.");
    }
  }

  @Override
  public String getOperationName() {
    return "Upgrade RAP UI Products to Juno Level";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    JettyProductFileUpgradeOperation op = new JettyProductFileUpgradeOperation(m_rapProdFiles);
    op.validate();
    op.run(monitor, workingCopyManager);
  }
}
