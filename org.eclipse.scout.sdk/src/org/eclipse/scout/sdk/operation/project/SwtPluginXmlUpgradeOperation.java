/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IExtensionsModelFactory;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.scout.sdk.compatibility.PlatformVersionUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class SwtPluginXmlUpgradeOperation extends AbstractScoutProjectNewOperation {

  private IProject m_project;

  @Override
  public boolean isRelevant() {
    return PlatformVersionUtility.isE4(getTargetPlatformVersion()) && isNodeChecked(CreateUiSwtPluginOperation.BUNDLE_ID);
  }

  @Override
  public void validate() {
    super.validate();
    if (m_project == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
  }

  @Override
  public void init() {
    String swtPluginName = getProperties().getProperty(CreateUiSwtPluginOperation.PROP_BUNDLE_SWT_NAME, String.class);
    m_project = getCreatedBundle(swtPluginName).getProject();
  }

  @Override
  public String getOperationName() {
    return "Upgrade SWT plugin.xml";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    final String[][] additionalE4Properties = new String[][]{
        {"applicationXMI", "org.eclipse.platform/LegacyIDE.e4xmi"},
        {"cssTheme", "org.eclipse.e4.ui.css.theme.e4_default"},
        {"applicationCSSResources", "platform:/plugin/org.eclipse.platform/images/"}
    };

    ResourcesPlugin.getWorkspace().checkpoint(false);
    PluginModelHelper pmh = new PluginModelHelper(m_project);
    IPluginElement productExtension = pmh.PluginXml.getSimpleExtension(IRuntimeClasses.EXTENSION_POINT_PRODUCTS, IRuntimeClasses.EXTENSION_ELEMENT_PRODUCT);
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
