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
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.operation.template.InstallJavaFileOperation;
import org.eclipse.scout.sdk.operation.template.TemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * creates a swt application plugin with an application and a product
 * depends on a bsi case client project with a ClientSession object
 * for example
 * com.google.rcp.ui.swt
 */
public class FillUiSwtApplicationPluginOperation implements IOperation {
  private final IProject m_project;
  private final ITemplateVariableSet m_templateBindings;

  public FillUiSwtApplicationPluginOperation(IProject project, ITemplateVariableSet bindings) {
    m_project = project;
    m_templateBindings = bindings;
  }

  public String getOperationName() {
    return "Fill UI SWT Plugin";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getProject() == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    TemplateVariableSet bindings = TemplateVariableSet.createNew(getProject(), m_templateBindings);
    String destPathPref = "src/" + (getProject().getName().replace('.', '/')) + "/";
    new InstallJavaFileOperation("templates/ui.swt.app/src/Activator.java", destPathPref + "Activator.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt.app/src/ApplicationActionBarAdvisor.java", destPathPref + "ApplicationActionBarAdvisor.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt.app/src/ApplicationWorkbenchAdvisor.java", destPathPref + "ApplicationWorkbenchAdvisor.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt.app/src/Application.java", destPathPref + "Application.java", getProject(), bindings).run(monitor, workingCopyManager);
    new InstallJavaFileOperation("templates/ui.swt.app/src/ApplicationWorkbenchWindowAdvisor.java", destPathPref + "ApplicationWorkbenchWindowAdvisor.java", getProject(), bindings).run(monitor, workingCopyManager);
  }

  public IProject getProject() {
    return m_project;
  }

}
