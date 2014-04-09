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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.template.InstallBinaryFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * creates a swing application plugin with an application and a product
 * depends on a scout client project with a ClientSession object
 * for example
 * com.google.rcp.ui.swing
 */
public class CreateUiSwingPluginOperation extends AbstractCreateScoutBundleOperation {
  public static final String PROP_BUNDLE_SWING_NAME = "BUNDLE_SWING_NAME";
  public static final String PROP_PRODUCT_FILE_DEV = "SWING_PROD_FILE_DEV";
  public static final String PROP_PRODUCT_FILE_PROD = "SWING_PROD_FILE_PROD";

  public static final String BUNDLE_ID = "org.eclipse.scout.sdk.ui.UiSwingBundle";
  public static final String SWING_UI_PROJECT_NAME_SUFFIX = ".ui.swing";

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiSwingPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    setSymbolicName(getPluginName(SWING_UI_PROJECT_NAME_SUFFIX));
  }

  @Override
  public String getOperationName() {
    return "Create UI Swing Plugin";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    getProperties().setProperty(PROP_BUNDLE_SWING_NAME, getSymbolicName());
    Map<String, String> props = getStringProperties();
    try {
      new InstallTextFileOperation("templates/ui.swing/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, props).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.swing/plugin.xml", "plugin.xml", project, props).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.swing/build.properties", "build.properties", project, props).run(monitor, workingCopyManager);
      new InstallBinaryFileOperation("templates/ui.swing/resources/icons/eclipse_scout.gif", project, "resources/icons/eclipse_scout.gif").run(monitor, workingCopyManager);

      // product files
      new InstallTextFileOperation("templates/ui.swing/products/development/config.ini", "products/development/config.ini", project, props).run(monitor, workingCopyManager);
      InstallTextFileOperation devProdInstallOp = new InstallTextFileOperation("templates/ui.swing/products/development/app-client-dev.product", "products/development/" + getProjectAlias() + "-swing-client-dev.product", project, props);
      devProdInstallOp.run(monitor, workingCopyManager);
      getProperties().setProperty(PROP_PRODUCT_FILE_DEV, devProdInstallOp.getCreatedFile());
      addCreatedProductFile(devProdInstallOp.getCreatedFile());

      new InstallTextFileOperation("templates/ui.swing/products/production/config.ini", "products/production/config.ini", project, props).run(monitor, workingCopyManager);
      InstallTextFileOperation prodProdInstallOp = new InstallTextFileOperation("templates/ui.swing/products/production/app-client.product", "products/production/" + getProjectAlias() + "-swing-client.product", project, props);
      prodProdInstallOp.run(monitor, workingCopyManager);
      getProperties().setProperty(PROP_PRODUCT_FILE_PROD, prodProdInstallOp.getCreatedFile());
      addCreatedProductFile(prodProdInstallOp.getCreatedFile());
    }
    catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not install files in '" + project.getName() + "'.", e));
    }
  }
}
