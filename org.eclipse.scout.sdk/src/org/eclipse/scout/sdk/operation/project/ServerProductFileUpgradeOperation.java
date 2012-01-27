package org.eclipse.scout.sdk.operation.project;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.internal.PlatformVersionUtility;
import org.eclipse.scout.sdk.operation.util.JettyProductFileUpgradeOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ServerProductFileUpgradeOperation extends AbstractScoutProjectNewOperation {

  private IFile[] m_serverProdFiles;

  @Override
  public boolean isRelevant() {
    return PlatformVersionUtility.isPlatformJuno() && isNodeChecked(CreateServerPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    ArrayList<IFile> productFiles = new ArrayList<IFile>(2);
    IFile dev = getProperties().getProperty(CreateServerPluginOperation.PROP_PRODUCT_FILE_DEV, IFile.class);
    if (dev != null) productFiles.add(dev);

    IFile prod = getProperties().getProperty(CreateServerPluginOperation.PROP_PRODUCT_FILE_PROD, IFile.class);
    if (prod != null) productFiles.add(prod);

    m_serverProdFiles = productFiles.toArray(new IFile[productFiles.size()]);
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (m_serverProdFiles == null || m_serverProdFiles.length != 2) {
      throw new IllegalArgumentException("dev or prod server product file not found.");
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
