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
 * Creates the <group>.server.app plugin
 */
public class CreateServerAppPluginOperation extends AbstractCreateScoutBundleOperation {

  private final ITemplateVariableSet m_templateBindings;

  public CreateServerAppPluginOperation(String bundleName, ITemplateVariableSet templateBindings) {
    setSymbolicName(bundleName);
    m_templateBindings = templateBindings;

  }

  @Override
  public String getOperationName() {
    return "Create Server App";
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    TemplateVariableSet bindings = TemplateVariableSet.createNew(project, m_templateBindings);

    String projectAlias = bindings.getVariable(ITemplateVariableSet.VAR_PROJECT_ALIAS);
    new InstallTextFileOperation("templates/server.app/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/build.properties", "build.properties", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/plugin.xml", "plugin.xml", project, bindings).run(monitor, workingCopyManager);
    //
    new InstallTextFileOperation("templates/server.app/j2ee/ear/war/WEB-INF/web.xml", "j2ee/ear/war/WEB-INF/web.xml", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/j2ee/ear/war/WEB-INF/eclipse/launch.ini", "j2ee/ear/war/WEB-INF/eclipse/launch.ini", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/j2ee/ear/war/WEB-INF/eclipse/README.txt", "j2ee/ear/war/WEB-INF/eclipse/README.txt", project, bindings).run(monitor, workingCopyManager);
    new InstallBinaryFileOperation("templates/server.app/j2ee/ear/war/WEB-INF/lib/servletbridge.jar", "j2ee/ear/war/WEB-INF/lib/servletbridge.jar", project).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/j2ee/ear/war/WEB-INF/remotefiles/calendar/holidays.xml", "j2ee/ear/war/WEB-INF/remotefiles/calendar/holidays.xml", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/j2ee/ear/war/WEB-INF/remotefiles/html/index.html", "j2ee/ear/war/WEB-INF/remotefiles/html/index.html", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/j2ee/ear/war/WEB-INF/remotefiles/templates/README.txt", "j2ee/ear/war/WEB-INF/remotefiles/templates/README.txt", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/j2ee/ear/war/WEB-INF/remotefiles/updatesite/root.xml", "j2ee/ear/war/WEB-INF/remotefiles/updatesite/root.xml", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/j2ee/ear/war/WEB-INF/remotefiles/updatesite/repository/repository.xml", "j2ee/ear/war/WEB-INF/remotefiles/updatesite/" + projectAlias + "/" + projectAlias + ".xml", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/j2ee/ear/war/WEB-INF/remotefiles/updatesite/repository/BuildUpdatesite.cmd", "j2ee/ear/war/WEB-INF/remotefiles/updatesite/" + projectAlias + "/BuildUpdatesite.cmd", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/j2ee/ear/war/WEB-INF/remotefiles/updatesite/repository/StageToProduction.cmd", "j2ee/ear/war/WEB-INF/remotefiles/updatesite/" + projectAlias + "/StageToProduction.cmd", project, bindings).run(monitor, workingCopyManager);
    //
    new InstallTextFileOperation("templates/server.app/products/development/app-server-dev.product", "products/development/" + projectAlias + "-server-dev.product", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/products/development/config.ini", "products/development/config.ini", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/products/production/app-server.product", "products/production/" + projectAlias + "-server.product", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/server.app/products/production/config.ini", "products/production/config.ini", project, bindings).run(monitor, workingCopyManager);
  }

}
