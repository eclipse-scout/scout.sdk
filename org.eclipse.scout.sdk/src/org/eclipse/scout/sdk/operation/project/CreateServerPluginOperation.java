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
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * creates plugins like for example
 * com.google.rcp.client
 */

public class CreateServerPluginOperation extends AbstractCreateScoutBundleOperation {

  private final ITemplateVariableSet m_templateBinding;

  public CreateServerPluginOperation(ITemplateVariableSet templateBinding) {
    setSymbolicName(templateBinding.getVariable(ITemplateVariableSet.VAR_BUNDLE_SERVER_NAME));
    m_templateBinding = templateBinding;

  }

  @Override
  public String getOperationName() {
    return "Create Server Plugin";
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    TemplateVariableSet bindings = TemplateVariableSet.createNew(project, m_templateBinding);
    String projectAlias = bindings.getVariable(ITemplateVariableSet.VAR_PROJECT_ALIAS);
    new InstallTextFileOperation("templates/server/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server/plugin.xml", "plugin.xml", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server/build.properties", "build.properties", project, bindings).run(monitor, workingCopyManager);
    // products
    new InstallTextFileOperation("templates/server/products/development/app-server-dev.product", "products/development/" + projectAlias + "-server-dev.product", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server/products/development/config.ini", "products/development/config.ini", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server/products/production/app-server.product", "products/production/" + projectAlias + "-server.product", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server/products/production/config.ini", "products/production/config.ini", project, bindings).run(monitor, workingCopyManager);
  }
}
