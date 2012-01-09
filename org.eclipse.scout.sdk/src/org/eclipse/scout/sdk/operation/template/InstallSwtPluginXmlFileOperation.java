package org.eclipse.scout.sdk.operation.template;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IExtensionsModelFactory;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Bundle;

public class InstallSwtPluginXmlFileOperation extends InstallTextFileOperation {
  public InstallSwtPluginXmlFileOperation(String srcPath, String dstPath, IProject dstProject) {
    super(srcPath, dstPath, dstProject);
  }

  public InstallSwtPluginXmlFileOperation(String srcPath, String dstPath, IProject dstProject, ITemplateVariableSet templateBinding) {
    super(srcPath, dstPath, dstProject, templateBinding);
  }

  public InstallSwtPluginXmlFileOperation(String srcPath, String dstPath, Bundle sourceBoundle, IProject dstProject, ITemplateVariableSet templateBinding) {
    super(srcPath, dstPath, sourceBoundle, dstProject, templateBinding);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);

    if (JdtUtility.isPlatformE4()) {
      final String[][] additionalE4Properties = new String[][]{
          {"applicationXMI", "org.eclipse.platform/LegacyIDE.e4xmi"},
          {"cssTheme", "org.eclipse.e4.ui.css.theme.e4_default"},
          {"applicationCSSResources", "platform:/plugin/org.eclipse.platform/images/"}
      };

      ResourcesPlugin.getWorkspace().checkpoint(false);
      PluginModelHelper pmh = new PluginModelHelper(getDstProject());
      IPluginElement productExtension = pmh.PluginXml.getSimpleExtension("org.eclipse.core.runtime.products", "product");
      if (productExtension != null) {
        IExtensionsModelFactory extensionFactory = productExtension.getPluginModel().getFactory();
        for (String[] kvp : additionalE4Properties) {
          IPluginElement property = extensionFactory.createElement(productExtension);
          property.setName("property");
          property.setAttribute("name", kvp[0]);
          property.setAttribute("value", kvp[1]);
          productExtension.add(property);
        }

        pmh.save();
      }
    }
  }
}
