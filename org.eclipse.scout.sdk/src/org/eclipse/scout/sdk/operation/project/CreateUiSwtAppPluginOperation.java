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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.operation.template.InstallBinaryFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * creates a swt application plugin with an application and a product
 * depends on a bsi case client project with a ClientSession object
 * for example
 * com.google.rcp.ui.swt
 */
public class CreateUiSwtAppPluginOperation extends AbstractCreateScoutBundleOperation {
  private final ITemplateVariableSet m_templateBindings;

  public CreateUiSwtAppPluginOperation(String bundleName, ITemplateVariableSet templateBinding) {
    setSymbolicName(bundleName);
    m_templateBindings = templateBinding;
  }

  @Override
  public String getOperationName() {
    return "Create Swt Application Plugin";
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    TemplateVariableSet bindings = TemplateVariableSet.createNew(project, m_templateBindings);
    new InstallTextFileOperation("templates/ui.swt.app/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/ui.swt.app/plugin.xml", "plugin.xml", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/ui.swt.app/build.properties", "build.properties", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/ui.swt.app/plugin_customization.ini", "plugin_customization.ini", project, bindings).run(monitor, workingCopyManager);
    new InstallBinaryFileOperation("templates/ui.swt.app/splash.bmp", "splash.bmp", project).run(monitor, workingCopyManager);
    new InstallBinaryFileOperation("templates/ui.swt.app/resources/icons/eclipse_scout.gif", "resources/icons/eclipse_scout.gif", project).run(monitor, workingCopyManager);
    //
    String projectAlias = bindings.getVariable(ITemplateVariableSet.VAR_PROJECT_ALIAS);
    new InstallTextFileOperation("templates/ui.swt.app/products/development/app-client-dev.product", "products/development/" + projectAlias + "-client-dev.product", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/ui.swt.app/products/development/config.ini", "products/development/config.ini", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/ui.swt.app/products/production/app-client.product", "products/production/" + projectAlias + "-client.product", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/ui.swt.app/products/production/config.ini", "products/production/config.ini", project, bindings).run(monitor, workingCopyManager);
  }
}
