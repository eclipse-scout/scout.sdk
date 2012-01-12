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
import org.eclipse.scout.sdk.operation.template.InstallSwtPluginXmlFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallSwtProductFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * creates a swt application plugin with an application and a product
 * depends on a bsi case client project with a ClientSession object
 * for example
 * com.google.rcp.ui.swt
 */
public class CreateUiSwtPluginOperation extends AbstractCreateScoutBundleOperation {

  private final ITemplateVariableSet m_templateBindings;

  public CreateUiSwtPluginOperation(ITemplateVariableSet templateBindings) {
    setSymbolicName(templateBindings.getVariable(ITemplateVariableSet.VAR_BUNDLE_SWT_NAME));
    m_templateBindings = templateBindings;
  }

  @Override
  public String getOperationName() {
    return "Create UI SWT Plugin";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    TemplateVariableSet bindings = TemplateVariableSet.createNew(project, m_templateBindings);
    try {
      new InstallTextFileOperation("templates/ui.swt/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, bindings).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/ui.swt/build.properties", "build.properties", project, bindings).run(monitor, workingCopyManager);
      new InstallSwtPluginXmlFileOperation("templates/ui.swt/plugin.xml", "plugin.xml", project, bindings).run(monitor, workingCopyManager);
      new InstallBinaryFileOperation("templates/ui.swt/splash.bmp", project, "splash.bmp").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation("templates/ui.swt/resources/icons/eclipse_scout.gif", project, "resources/icons/eclipse_scout.gif").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation("templates/ui.swt/resources/icons/eclipseScout16x16.gif", project, "resources/icons/eclipseScout16x16.gif").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation("templates/ui.swt/resources/icons/eclipseScout32x32.gif", project, "resources/icons/eclipseScout32x32.gif").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation("templates/ui.swt/resources/icons/progress_none.gif", project, "resources/icons/progress_none.gif").run(monitor, workingCopyManager);
    }
    catch (MalformedURLException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not install files in '" + project.getName() + "'.", e));
    }

    // products
    String projectAlias = bindings.getVariable(ITemplateVariableSet.VAR_PROJECT_ALIAS);
    new InstallTextFileOperation("templates/ui.swt/products/development/config.ini", "products/development/config.ini", project, bindings).run(monitor, workingCopyManager);
    InstallSwtProductFileOperation devProdInstallOp = new InstallSwtProductFileOperation("templates/ui.swt/products/development/app-client-dev.product", "products/development/" + projectAlias + "-swt-client-dev.product", project, bindings);
    devProdInstallOp.run(monitor, workingCopyManager);

    new InstallTextFileOperation("templates/ui.swt/products/production/config.ini", "products/production/config.ini", project, bindings).run(monitor, workingCopyManager);
    new InstallSwtProductFileOperation("templates/ui.swt/products/production/app-client.product", "products/production/" + projectAlias + "-swt-client.product", project, bindings).run(monitor, workingCopyManager);

    // register development product as project launcher in project-property-part
    SdkProperties.addProjectProductLauncher(m_templateBindings.getVariable(ITemplateVariableSet.VAR_PROJECT_NAME), devProdInstallOp.getCreatedFile());
  }
}
