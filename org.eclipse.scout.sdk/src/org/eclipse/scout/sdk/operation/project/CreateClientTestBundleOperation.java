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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * Creates the <group>.client.test plugin
 */
public class CreateClientTestBundleOperation extends CreateEclipseJavaPluginOperation {
  private final ITemplateVariableSet m_templateBindings;

  @SuppressWarnings("restriction")
  public CreateClientTestBundleOperation(ITemplateVariableSet templateBindings) {
    setSymbolicName(templateBindings.getVariable(ITemplateVariableSet.VAR_BUNDLE_CLIENT_TEST_NAME));
    m_templateBindings = templateBindings;
    addNature(org.eclipse.pde.internal.core.natures.PDE.PLUGIN_NATURE);
    addNature(JavaCore.NATURE_ID);
  }

  @Override
  public String getOperationName() {
    return "Create Client Test Plugin";
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    TemplateVariableSet bindings = TemplateVariableSet.createNew(project, m_templateBindings);
    new InstallTextFileOperation("templates/client.test/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/client.test/plugin.xml", "plugin.xml", project, bindings).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/client.test/build.properties", "build.properties", project, bindings).run(monitor, workingCopyManager);
  }
}
