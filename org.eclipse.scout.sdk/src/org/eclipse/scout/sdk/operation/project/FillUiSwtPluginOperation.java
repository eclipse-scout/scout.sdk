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
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * creates a swt application plugin with an application and a product
 * depends on a scout client project with a ClientSession object
 * for example
 * com.bsiag.rcp.ui.swt
 */
public class FillUiSwtPluginOperation implements IOperation {

  private final IProject m_project;
  private final ITemplateVariableSet m_templateBindings;

  public FillUiSwtPluginOperation(IProject project, ITemplateVariableSet bindings) {
    m_project = project;
    m_templateBindings = bindings;
  }

  @Override
  public String getOperationName() {
    return "Fill UI SWT Plugin";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getProject() == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    TemplateVariableSet bindings = TemplateVariableSet.createNew(getProject(), m_templateBindings);
    String destPathPref = "src/" + (getProject().getName().replace('.', '/')) + "/";
    new InstallJavaFileOperation("templates/ui.swt/src/Activator.java", destPathPref + "Activator.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/perspective/Perspective.java", destPathPref + "perspective/Perspective.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/views/CenterView.java", destPathPref + "views/CenterView.java", getProject(), bindings).run(monitor, workingCopyManager);

    new InstallJavaFileOperation("templates/ui.swt/src/views/OutlinePageView.java", destPathPref + "views/OutlinePageView.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/views/SearchView.java", destPathPref + "views/SearchView.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/views/TablePageView.java", destPathPref + "views/TablePageView.java", getProject(), bindings).run(monitor, workingCopyManager);

    new InstallJavaFileOperation("templates/ui.swt/src/SwtEnvironment.java", destPathPref + "SwtEnvironment.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/SwtStartup.java", destPathPref + "SwtStartup.java", getProject(), bindings).run(monitor, workingCopyManager);
    // application
    new InstallJavaFileOperation("templates/ui.swt/src/application/Application.java", destPathPref + "application/Application.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/application/ApplicationActionBarAdvisor.java", destPathPref + "application/ApplicationActionBarAdvisor.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/application/ApplicationWorkbenchAdvisor.java", destPathPref + "application/ApplicationWorkbenchAdvisor.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt/src/application/ApplicationWorkbenchWindowAdvisor.java", destPathPref + "application/ApplicationWorkbenchWindowAdvisor.java", getProject(), bindings).run(monitor, workingCopyManager);
    getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
  }

  public IProject getProject() {
    return m_project;
  }

}
