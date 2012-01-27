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

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.template.InstallBinaryFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class CreateServerPluginOperation extends AbstractCreateScoutBundleOperation {
  public final static String PROP_BUNDLE_SERVER_NAME = "BUNDLE_SERVER_NAME";
  public final static String PROP_FS_ROOT = "FS_ROOT";
  public final static String PROP_PRODUCT_FILE_DEV = "SERVER_PROD_FILE_DEV";
  public final static String PROP_PRODUCT_FILE_PROD = "SERVER_PROD_FILE_PROD";

  public final static String BUNDLE_ID = "org.eclipse.scout.sdk.ui.ServerBundle";
  public final static String SERVER_PROJECT_NAME_SUFFIX = ".server";

  @Override
  public String getOperationName() {
    return "Create Server Plugin";
  }

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateServerPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    setSymbolicName(getPluginName(SERVER_PROJECT_NAME_SUFFIX));
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();

    getProperties().setProperty(PROP_BUNDLE_SERVER_NAME, getSymbolicName());
    String absoluteServerPluginPath = new File(project.getLocation().toOSString()).getAbsolutePath().replace('\\', '/');
    getProperties().setProperty(PROP_FS_ROOT, absoluteServerPluginPath);
    Map<String, String> props = getStringProperties();

    new InstallTextFileOperation("templates/server/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, props).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server/plugin.xml", "plugin.xml", project, props).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server/build.properties", "build.properties", project, props).run(monitor, workingCopyManager);

    // dev-product
    new InstallTextFileOperation("templates/server/products/development/config.ini", "products/development/config.ini", project, props).run(monitor, workingCopyManager);
    InstallTextFileOperation devProdInstallOp = new InstallTextFileOperation("templates/server/products/development/app-server-dev.product",
              "products/development/" + getProjectAlias() + "-server-dev.product", project, props);
    devProdInstallOp.run(monitor, workingCopyManager);
    getProperties().setProperty(PROP_PRODUCT_FILE_DEV, devProdInstallOp.getCreatedFile());

    // register development product as project launcher in project-property-part
    SdkProperties.addProjectProductLauncher(getScoutProjectName(), devProdInstallOp.getCreatedFile());

    // prod-product
    new InstallTextFileOperation("templates/server/products/production/config.ini", "products/production/config.ini", project, props).run(monitor, workingCopyManager);
    InstallTextFileOperation prodProdInstallOp = new InstallTextFileOperation("templates/server/products/production/app-server.product",
        "products/production/" + getProjectAlias() + "-server.product", project, props);
    prodProdInstallOp.run(monitor, workingCopyManager);
    getProperties().setProperty(PROP_PRODUCT_FILE_PROD, prodProdInstallOp.getCreatedFile());

    // resources
    new InstallTextFileOperation("templates/server/resources/html/index.html", "resources/html/index.html", project, props).run(monitor, workingCopyManager);
    try {
      new InstallBinaryFileOperation("templates/server/resources/html/scout.gif", project, "resources/html/scout.gif").run(monitor, workingCopyManager);
    }
    catch (MalformedURLException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not install 'resources/html/scout.gif'."));
    }
  }
}
