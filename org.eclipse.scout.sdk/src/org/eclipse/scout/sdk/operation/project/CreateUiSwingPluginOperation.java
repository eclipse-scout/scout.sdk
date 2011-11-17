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

import java.net.MalformedURLException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.operation.template.InstallBinaryFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * creates a swing application plugin with an application and a product
 * depends on a bsi case client project with a ClientSession object
 * for example
 * com.google.rcp.ui.swing
 */
public class CreateUiSwingPluginOperation extends AbstractCreateScoutBundleOperation {

  private final ITemplateVariableSet m_templateBindings;

  public CreateUiSwingPluginOperation(ITemplateVariableSet templateBindings) {
    setSymbolicName(templateBindings.getVariable(ITemplateVariableSet.VAR_BUNDLE_SWING_NAME));
    m_templateBindings = templateBindings;
    setExecutionEnvironment("JavaSE-1.6");
  }

  @Override
  public String getOperationName() {
    return "Create UI Swing Plugin";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    TemplateVariableSet bindings = TemplateVariableSet.createNew(project, m_templateBindings);
    try {
      new InstallTextFileOperation("templates/ui.swing/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, bindings).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.swing/plugin.xml", "plugin.xml", project, bindings).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.swing/build.properties", "build.properties", project, bindings).run(monitor, workingCopyManager);
      new InstallBinaryFileOperation("templates/ui.swing/resources/icons/eclipse_scout.gif", project, "resources/icons/eclipse_scout.gif").run(monitor, workingCopyManager);
      //
      String projectAlias = bindings.getVariable(ITemplateVariableSet.VAR_PROJECT_ALIAS);
      new InstallTextFileOperation("templates/ui.swing/products/development/app-client-dev.product", "products/development/" + projectAlias + "-swing-client-dev.product", project, bindings).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.swing/products/development/config.ini", "products/development/config.ini", project, bindings).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.swing/products/production/app-client.product", "products/production/" + projectAlias + "-swing-client.product", project, bindings).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.swing/products/production/config.ini", "products/production/config.ini", project, bindings).run(monitor, workingCopyManager);
    }
    catch (MalformedURLException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not install files in '" + project.getName() + "'.", e));
    }
  }
}
