package org.eclipse.scout.sdk.operation.project;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.internal.PlatformVersionUtility;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
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
    // jetty plugins have been renamed: jetty <= 6 is named "org.mortbay..." while jetty > 6 is called "org.eclipse.jetty..."
    // see http://wiki.eclipse.org/Jetty/Getting_Started/Porting_to_Jetty_7/Refactoring
    // eclipse 3.8 and 4.2 uses jetty >= 7 -> rename jetty plugins
    final String[] oldPluginsToRemove = new String[]{
            "org.mortbay.jetty.server",
            "org.mortbay.jetty.util"
        };

    final String[] additionalE4Plugins = new String[]{
            "org.eclipse.jetty.continuation",
            "org.eclipse.jetty.http",
            "org.eclipse.jetty.io",
            "org.eclipse.jetty.security",
            "org.eclipse.jetty.server",
            "org.eclipse.jetty.servlet",
            "org.eclipse.jetty.util"
        };

    for (IFile prodFile : m_serverProdFiles) {
      ProductFileModelHelper pfmh = new ProductFileModelHelper(prodFile);
      for (String plugin : oldPluginsToRemove) {
        pfmh.ProductFile.removeDependency(plugin);
      }

      for (String plugin : additionalE4Plugins) {
        pfmh.ProductFile.addDependency(plugin);
      }
      pfmh.save();
    }
  }
}
