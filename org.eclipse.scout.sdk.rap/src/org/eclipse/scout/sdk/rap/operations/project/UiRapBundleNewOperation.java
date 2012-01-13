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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.operation.project.AbstractCreateScoutBundleOperation;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.operation.template.InstallBinaryFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallJavaFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Bundle;

/**
 */
public class UiRapBundleNewOperation extends AbstractCreateScoutBundleOperation {

  private final ITemplateVariableSet m_templateBindings;
  private String m_localRapTargetFolder;
  private String m_remoteRapTargetUrl;

  public UiRapBundleNewOperation(ITemplateVariableSet templateBindings) {
    setSymbolicName(templateBindings.getVariable("BUNDLE_RAP_NAME"));
    m_templateBindings = templateBindings;
    setCreateResourcesFolder(false);
  }

  @Override
  public String getOperationName() {
    return "Create UI RAP Plugin";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    TemplateVariableSet bindings = TemplateVariableSet.createNew(project, m_templateBindings);
    try {
      Bundle uiRapBundle = Platform.getBundle(ScoutSdkRap.PLUGIN_ID);
      new InstallTextFileOperation("templates/ui.rap/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", uiRapBundle, project, bindings).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.rap/plugin.xml", "plugin.xml", uiRapBundle, project, bindings).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.rap/build.properties", "build.properties", uiRapBundle, project, bindings).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.rap/theme/application.css", "theme/application.css", uiRapBundle, project, bindings).run(monitor, workingCopyManager);

      new InstallBinaryFileOperation(ScoutSdkRap.PLUGIN_ID, "templates/ui.rap/web-resources/eclipseScout.gif", project, "web-resources/eclipseScout.gif").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation(ScoutSdkRap.PLUGIN_ID, "templates/ui.rap/web-resources/eclipseScoutBackground.png", project, "web-resources/eclipseScoutBackground.png").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation(ScoutSdkRap.PLUGIN_ID, "templates/ui.rap/web-resources/html-styles.css", project, "web-resources/html-styles.css").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation(ScoutSdkRap.PLUGIN_ID, "templates/ui.rap/web-resources/loading.gif", project, "web-resources/loading.gif").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation(ScoutSdkRap.PLUGIN_ID, "templates/ui.rap/web-resources/startup-body.html", project, "web-resources/startup-body.html").run(monitor, workingCopyManager);
      String destPathPref = "src/" + (getCreatedProject().getName().replace('.', '/')) + "/";
      new InstallJavaFileOperation("templates/ui.rap/src/Activator.java", destPathPref + "Activator.java", uiRapBundle, getCreatedProject(), bindings).run(monitor, workingCopyManager);
      new InstallJavaFileOperation("templates/ui.rap/src/StandaloneRwtEnvironment.java", destPathPref + "StandaloneRwtEnvironment.java", uiRapBundle, getCreatedProject(), bindings).run(monitor, workingCopyManager);

      // PRODUCT
      new InstallTextFileOperation("templates/ui.rap/products/development/config.ini", "products/development/config.ini", uiRapBundle, project, bindings).run(monitor, workingCopyManager);
      InstallTextFileOperation devProdInstallOp = new InstallTextFileOperation("templates/ui.rap/products/development/app-rap-dev.product", "products/development/app-rap-dev.product", uiRapBundle, project, bindings);
      devProdInstallOp.run(monitor, workingCopyManager);

      // register development product as project launcher in project-property-part
      SdkProperties.addProjectProductLauncher(m_templateBindings.getVariable(ITemplateVariableSet.VAR_PROJECT_NAME), devProdInstallOp.getCreatedFile());

      installTargetFile();
    }
    catch (MalformedURLException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdkRap.PLUGIN_ID, "could not install files in '" + project.getName() + "'.", e));
    }
  }

  private void installTargetFile() {
    if (getRemoteRapTargetUrl() != null) {
      // TODO
    }
    else if (getLocalRapTargetFolder() != null) {

    }
  }

  public void setRemoteRapTargetUrl(String remoteRapTargetUrl) {
    m_remoteRapTargetUrl = remoteRapTargetUrl;
  }

  public String getRemoteRapTargetUrl() {
    return m_remoteRapTargetUrl;
  }

  public void setLocalRapTargetFolder(String localRapTargetFolder) {
    m_localRapTargetFolder = localRapTargetFolder;
  }

  public String getLocalRapTargetFolder() {
    return m_localRapTargetFolder;
  }
}
