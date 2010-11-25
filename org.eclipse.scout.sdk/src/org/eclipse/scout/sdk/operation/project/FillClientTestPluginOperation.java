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

public class FillClientTestPluginOperation implements IOperation {

  private final IProject m_project;

  private final ITemplateVariableSet m_templateBindings;

  public FillClientTestPluginOperation(IProject project, ITemplateVariableSet templateBindings) {
    m_project = project;
    m_templateBindings = templateBindings;
  }

  public String getOperationName() {
    return "Fill Client Test Plugin";
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
    new InstallJavaFileOperation("templates/client.test/src/Activator.java", destPathPref + "Activator.java", getProject(), bindings).run(monitor, workingCopyManager);
  }

  public IProject getProject() {
    return m_project;
  }

}
