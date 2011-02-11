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
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.operation.template.InstallBinaryFileOperation;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * creates plugins like for example
 * com.google.rcp.client
 */
public class CreateClientBundleOperation extends AbstractCreateScoutBundleOperation {

  private final ITemplateVariableSet m_templateBindings;

  public CreateClientBundleOperation(ITemplateVariableSet templateBindings) {
    setSymbolicName(templateBindings.getVariable(ITemplateVariableSet.VAR_BUNDLE_CLIENT_NAME));
    m_templateBindings = templateBindings;

  }

  @Override
  public String getOperationName() {
    return "Create Client Plugin";
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    TemplateVariableSet bindings = TemplateVariableSet.createNew(project, m_templateBindings);
    try {
      new InstallTextFileOperation("templates/client/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, bindings).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/client/plugin.xml", "plugin.xml", project, bindings).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/client/build.properties", "build.properties", project, bindings).run(monitor, workingCopyManager);
      new InstallBinaryFileOperation("templates/client/resources/icons/eye.png", project, "resources/icons/eye.png").run(monitor, workingCopyManager);
      new InstallBinaryFileOperation("templates/client/resources/icons/eclipse_scout.gif", project, "resources/icons/eclipse_scout.gif").run(monitor, workingCopyManager);
    }
    catch (MalformedURLException e) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "could not install files in '" + project.getName() + "'.", e));
    }
  }

}
