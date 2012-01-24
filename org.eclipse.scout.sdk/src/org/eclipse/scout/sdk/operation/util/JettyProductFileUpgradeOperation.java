package org.eclipse.scout.sdk.operation.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class JettyProductFileUpgradeOperation implements IOperation {

  private final IFile[] m_prodFiles;

  public JettyProductFileUpgradeOperation(IFile[] prodFiles) {
    m_prodFiles = prodFiles;
  }

  @Override
  public String getOperationName() {
    return "Upgrade Jetty Plugins in Product Files to Juno Level";
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (m_prodFiles == null || m_prodFiles.length == 0) {
      return;
    }

    // jetty plugins have been renamed: jetty <= 6 is named "org.mortbay..." while jetty > 6 is called "org.eclipse.jetty..."
    // see http://wiki.eclipse.org/Jetty/Getting_Started/Porting_to_Jetty_7/Refactoring
    // eclipse 3.8 and 4.2 uses jetty >= 7 -> rename jetty plugins
    final String[] oldPluginsToRemove = new String[]{
            "org.mortbay.jetty.server",
            "org.mortbay.jetty.util"
        };

    final String[] additionalJunoPlugins = new String[]{
            "org.eclipse.jetty.continuation",
            "org.eclipse.jetty.http",
            "org.eclipse.jetty.io",
            "org.eclipse.jetty.security",
            "org.eclipse.jetty.server",
            "org.eclipse.jetty.servlet",
            "org.eclipse.jetty.util"
        };

    for (IFile prodFile : m_prodFiles) {
      ProductFileModelHelper pfmh = new ProductFileModelHelper(prodFile);
      for (String plugin : oldPluginsToRemove) {
        pfmh.ProductFile.removeDependency(plugin);
      }

      for (String plugin : additionalJunoPlugins) {
        pfmh.ProductFile.addDependency(plugin);
      }
      pfmh.save();
    }
  }
}
