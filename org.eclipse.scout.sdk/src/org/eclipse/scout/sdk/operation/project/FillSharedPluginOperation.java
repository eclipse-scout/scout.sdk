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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.operation.template.InstallJavaFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class FillSharedPluginOperation implements IOperation {

  private final IProject m_project;

  private final ITemplateVariableSet m_templateBindings;

  public FillSharedPluginOperation(IProject sharedProject, ITemplateVariableSet templateBindings) {
    m_project = sharedProject;
    m_templateBindings = templateBindings;
  }

  @Override
  public String getOperationName() {
    return "Fill Scout Shared Plugin";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getProject() == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    TemplateVariableSet bindings = TemplateVariableSet.createNew(getProject(), m_templateBindings);
    String destPathPref = "src/" + (getProject().getName().replace('.', '/')) + "/";
    new InstallJavaFileOperation("templates/shared/src/Activator.java", destPathPref + "Activator.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/shared/src/Icons.java", destPathPref + "Icons.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/shared/src/DefaultTextProviderService.java", destPathPref + "services/common/text/DefaultTextProviderService.java", getProject(), bindings).run(monitor, workingCopyManager);
    getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
  }

  public IProject getProject() {
    return m_project;
  }

}
