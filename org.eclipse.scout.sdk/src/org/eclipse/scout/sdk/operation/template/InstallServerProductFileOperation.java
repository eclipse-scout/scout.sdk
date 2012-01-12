package org.eclipse.scout.sdk.operation.template;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.util.PlatformUtility;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Bundle;

public class InstallServerProductFileOperation extends InstallTextFileOperation {
  public InstallServerProductFileOperation(String srcPath, String dstPath, IProject dstProject) {
    super(srcPath, dstPath, dstProject);
  }

  public InstallServerProductFileOperation(String srcPath, String dstPath, IProject dstProject, ITemplateVariableSet templateBinding) {
    super(srcPath, dstPath, dstProject, templateBinding);
  }

  public InstallServerProductFileOperation(String srcPath, String dstPath, Bundle sourceBoundle, IProject dstProject, ITemplateVariableSet templateBinding) {
    super(srcPath, dstPath, sourceBoundle, dstProject, templateBinding);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);

    if (PlatformUtility.isPlatformJuno()) {
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

      ProductFileModelHelper pfmh = new ProductFileModelHelper(getCreatedFile());
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
