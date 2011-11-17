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

public class FillClientPluginOperation implements IOperation {

  private final IProject m_clientProject;

  private final ITemplateVariableSet m_templateBindings;

  public FillClientPluginOperation(IProject clientProject, ITemplateVariableSet templateBindings) {
    m_clientProject = clientProject;
    m_templateBindings = templateBindings;
  }

  @Override
  public String getOperationName() {
    return "Fill Scout Client Plugin";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getClientProject() == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    TemplateVariableSet bindings = TemplateVariableSet.createNew(getClientProject(), m_templateBindings);
    String destPathPref = "src/" + (getClientProject().getName().replace('.', '/')) + "/";
    new InstallJavaFileOperation("templates/client/src/Activator.java", destPathPref + "Activator.java", getClientProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/client/src/ClientSession.java", destPathPref + "ClientSession.java", getClientProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/client/src/ui/desktop/Desktop.java", destPathPref + "ui/desktop/Desktop.java", getClientProject(), bindings).run(monitor, workingCopyManager);
    getClientProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
  }

  public IProject getClientProject() {
    return m_clientProject;
  }

}
