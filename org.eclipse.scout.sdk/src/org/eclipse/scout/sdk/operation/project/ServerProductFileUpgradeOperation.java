package org.eclipse.scout.sdk.operation.project;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.PlatformVersionUtility;
import org.eclipse.scout.sdk.operation.util.JettyProductFileUpgradeOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ServerProductFileUpgradeOperation extends AbstractScoutProjectNewOperation {

  private IFile[] m_serverProdFiles;

  @Override
  public boolean isRelevant() {
    return PlatformVersionUtility.isJunoOrLater(getTargetPlatformVersion()) && isNodeChecked(CreateServerPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    ArrayList<IFile> productFiles = new ArrayList<IFile>(1);

    // only add dev-product as prod product has no jetty.
    IFile dev = getProperties().getProperty(CreateServerPluginOperation.PROP_PRODUCT_FILE_DEV, IFile.class);
    if (dev != null) productFiles.add(dev);

    m_serverProdFiles = productFiles.toArray(new IFile[productFiles.size()]);
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (m_serverProdFiles == null) {
      throw new IllegalArgumentException("server development products cannot be null.");
    }
  }

  @Override
  public String getOperationName() {
    return "Upgrade Server Products to Juno Level";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    JettyProductFileUpgradeOperation op = new JettyProductFileUpgradeOperation(m_serverProdFiles);
    op.validate();
    op.run(monitor, workingCopyManager);
  }
}
