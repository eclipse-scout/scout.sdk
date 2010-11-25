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
 * Creates the <group>.projectSets project
 */
public class CreateProjectSetsProjectOperation extends CreateEclipseProjectOperation {

  private final ITemplateVariableSet m_templateBindings;

  public CreateProjectSetsProjectOperation(ITemplateVariableSet templateBindings) {
    setSymbolicName(templateBindings.getVariable(ITemplateVariableSet.VAR_BUNDLE_PROJECTSETS_NAME));
    m_templateBindings = templateBindings;
  }

  @Override
  public String getOperationName() {
    return "Create Project Sets";
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    TemplateVariableSet bindings = TemplateVariableSet.createNew(project, m_templateBindings);
    new InstallTextFileOperation("templates/projectSets/trunkProjectSet.psf", "trunkProjectSet.psf", project, bindings).run(monitor, workingCopyManager);
  }

}
