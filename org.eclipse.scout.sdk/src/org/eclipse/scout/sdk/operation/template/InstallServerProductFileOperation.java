package org.eclipse.scout.sdk.operation.template;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

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

    Version frameworkVersion = JdtUtility.getFrameworkVersion();
    if (frameworkVersion.getMajor() == 4 || (frameworkVersion.getMajor() == 3 && frameworkVersion.getMinor() > 7)) {
      // change from Jetti 6 to Jetty 7 when eclipse >= 3.8
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
