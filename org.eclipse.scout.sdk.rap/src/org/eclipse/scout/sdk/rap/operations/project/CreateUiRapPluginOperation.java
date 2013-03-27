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
package org.eclipse.scout.sdk.rap.operations.project;

import java.net.MalformedURLException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.operation.project.AbstractCreateScoutBundleOperation;
import org.eclipse.scout.sdk.operation.project.CreateSharedPluginOperation;
import org.eclipse.scout.sdk.operation.template.InstallBinaryFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallJavaFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Bundle;

public class CreateUiRapPluginOperation extends AbstractCreateScoutBundleOperation {
  public final static String PROP_BUNDLE_RAP_NAME = "BUNDLE_RAP_NAME";
  public final static String PROP_PRODUCT_FILE_DEV = "RAP_PROD_FILE_DEV";
  public final static String PROP_PRODUCT_FILE_PROD = "RAP_PROD_FILE_PROD";

  public final static String BUNDLE_ID = "org.eclipse.scout.sdk.ui.UiRapBundle";
  public final static String RAP_UI_PROJECT_NAME_SUFFIX = ".ui.rap";

  @Override
  public String getOperationName() {
    return "Create UI RAP Plugin";
  }

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    setSymbolicName(getPluginName(RAP_UI_PROJECT_NAME_SUFFIX));
    setCreateResourcesFolder(false);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    getProperties().setProperty(PROP_BUNDLE_RAP_NAME, getSymbolicName());
    try {
      Bundle uiRapBundle = ScoutSdkRap.getDefault().getBundle();
      new InstallTextFileOperation("templates/ui.rap/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", uiRapBundle, project, getStringProperties()).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.rap/plugin.xml", "plugin.xml", uiRapBundle, project, getStringProperties()).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.rap/build.properties", "build.properties", uiRapBundle, project, getStringProperties()).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.rap/theme/application.css", "theme/application.css", uiRapBundle, project, getStringProperties()).run(monitor, workingCopyManager);

      new InstallBinaryFileOperation(ScoutSdkRap.PLUGIN_ID, "templates/ui.rap/web-resources/eclipseScout.gif", project, "web-resources/eclipseScout.gif").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation(ScoutSdkRap.PLUGIN_ID, "templates/ui.rap/web-resources/eclipseScoutBackground.png", project, "web-resources/eclipseScoutBackground.png").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation(ScoutSdkRap.PLUGIN_ID, "templates/ui.rap/web-resources/html-styles.css", project, "web-resources/html-styles.css").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation(ScoutSdkRap.PLUGIN_ID, "templates/ui.rap/web-resources/loading.gif", project, "web-resources/loading.gif").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation(ScoutSdkRap.PLUGIN_ID, "templates/ui.rap/web-resources/startup-body.html", project, "web-resources/startup-body.html").run(monitor, workingCopyManager);
      String destPathPref = "src/" + (getCreatedProject().getName().replace('.', '/')) + "/";
      new InstallJavaFileOperation("templates/ui.rap/src/Activator.java", destPathPref + "Activator.java", uiRapBundle, getCreatedProject(), getStringProperties()).run(monitor, workingCopyManager);
      new InstallJavaFileOperation("templates/ui.rap/src/StandaloneRwtEnvironment.java", destPathPref + "StandaloneRwtEnvironment.java", uiRapBundle, getCreatedProject(), getStringProperties()).run(monitor, workingCopyManager);
      new InstallJavaFileOperation("templates/ui.rap/src/MobileStandaloneRwtEnvironment.java", destPathPref + "MobileStandaloneRwtEnvironment.java", uiRapBundle, getCreatedProject(), getStringProperties()).run(monitor, workingCopyManager);
      new InstallJavaFileOperation("templates/ui.rap/src/TabletStandaloneRwtEnvironment.java", destPathPref + "TabletStandaloneRwtEnvironment.java", uiRapBundle, getCreatedProject(), getStringProperties()).run(monitor, workingCopyManager);

      // dev product
      new InstallTextFileOperation("templates/ui.rap/products/development/config.ini", "products/development/config.ini", uiRapBundle, project, getStringProperties()).run(monitor, workingCopyManager);
      InstallTextFileOperation devProdInstallOp = new InstallTextFileOperation("templates/ui.rap/products/development/app-rap-dev.product",
          "products/development/" + getProjectAlias() + "-rap-dev.product", uiRapBundle, project, getStringProperties());
      devProdInstallOp.run(monitor, workingCopyManager);
      getProperties().setProperty(PROP_PRODUCT_FILE_DEV, devProdInstallOp.getCreatedFile());

      IJavaProject shared = getCreatedBundle(getProperties().getProperty(CreateSharedPluginOperation.PROP_BUNDLE_SHARED_NAME, String.class));
      if (shared != null) {
        // register development product as project launcher in project-property-part
        SdkProperties.addProjectProductLauncher(shared.getElementName(), devProdInstallOp.getCreatedFile());
      }

      // prod product
      new InstallTextFileOperation("templates/ui.rap/products/production/config.ini", "products/production/config.ini", uiRapBundle, project, getStringProperties()).run(monitor, workingCopyManager);
      InstallTextFileOperation prodProdInstallOp = new InstallTextFileOperation("templates/ui.rap/products/production/app-rap.product",
          "products/production/" + getProjectAlias() + "-rap.product", uiRapBundle, project, getStringProperties());
      prodProdInstallOp.run(monitor, workingCopyManager);
      getProperties().setProperty(PROP_PRODUCT_FILE_PROD, prodProdInstallOp.getCreatedFile());
    }
    catch (MalformedURLException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdkRap.PLUGIN_ID, "could not install files in '" + project.getName() + "'.", e));
    }
  }
}
