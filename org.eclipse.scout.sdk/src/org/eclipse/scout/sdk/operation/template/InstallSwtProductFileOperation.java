package org.eclipse.scout.sdk.operation.template;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class InstallSwtProductFileOperation extends InstallTextFileOperation {
  public InstallSwtProductFileOperation(String srcPath, String dstPath, IProject dstProject) {
    super(srcPath, dstPath, dstProject);
  }

  public InstallSwtProductFileOperation(String srcPath, String dstPath, IProject dstProject, ITemplateVariableSet templateBinding) {
    super(srcPath, dstPath, dstProject, templateBinding);
  }

  public InstallSwtProductFileOperation(String srcPath, String dstPath, Bundle sourceBoundle, IProject dstProject, ITemplateVariableSet templateBinding) {
    super(srcPath, dstPath, sourceBoundle, dstProject, templateBinding);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);

    Version frameworkVersion = JdtUtility.getFrameworkVersion();
    if (frameworkVersion.getMajor() == 4 || (frameworkVersion.getMajor() == 3 && frameworkVersion.getMinor() > 7)) {

      final String[] additionalE4Plugins = new String[]{
          "org.eclipse.e4.core.commands",
          "org.eclipse.e4.core.contexts",
          "org.eclipse.e4.core.di",
          "org.eclipse.e4.core.di.extensions",
          "org.eclipse.e4.core.services",
          "org.eclipse.e4.ui.bindings",
          "org.eclipse.e4.ui.css.core",
          "org.eclipse.e4.ui.css.swt",
          "org.eclipse.e4.ui.css.swt.theme",
          "org.eclipse.e4.ui.di",
          "org.eclipse.e4.ui.model.workbench",
          "org.eclipse.e4.ui.services",
          "org.eclipse.e4.ui.widgets",
          "org.eclipse.e4.ui.workbench",
          "org.eclipse.e4.ui.workbench.addons.swt",
          "org.eclipse.e4.ui.workbench.renderers.swt",
          "org.eclipse.e4.ui.workbench.swt",
          "org.eclipse.e4.ui.workbench3",
          "org.eclipse.emf.common",
          "org.eclipse.emf.ecore",
          "org.eclipse.emf.ecore.change",
          "org.eclipse.emf.ecore.xmi",
          "org.eclipse.equinox.concurrent",
          "org.eclipse.equinox.ds",
          "org.eclipse.equinox.event",
          "org.eclipse.equinox.util",
          "org.eclipse.platform",
          "org.eclipse.ui.intro",
          "org.w3c.css.sac",
          "org.w3c.dom.smil",
          "org.w3c.dom.svg",
          "javax.annotation",
          "javax.inject",
          "org.apache.batik.css",
          "org.apache.batik.util",
          "org.apache.batik.util.gui"
      };

      ProductFileModelHelper pfmh = new ProductFileModelHelper(getCreatedFile());

      // additional product file dependencies
      for (String plugin : additionalE4Plugins) {
        pfmh.ProductFile.addDependency(plugin);
      }

      // config.ini changes
      final String E4_ADDITIONAL_START = "org.eclipse.equinox.ds@3:start";
      final String INSERT_BEFORE = "org.eclipse.core.runtime@start";
      String oldEntry = pfmh.ConfigurationFile.getOsgiBundlesEntry();
      int pos = oldEntry.indexOf(INSERT_BEFORE);
      if (pos >= 0) {
        StringBuilder newEntry = new StringBuilder(oldEntry.substring(0, pos));
        newEntry.append(E4_ADDITIONAL_START);
        newEntry.append(",");
        newEntry.append(oldEntry.substring(pos));
        pfmh.ConfigurationFile.setOsgiBundlesEntry(newEntry.toString());
      }
      pfmh.save();
    }
  }
}
