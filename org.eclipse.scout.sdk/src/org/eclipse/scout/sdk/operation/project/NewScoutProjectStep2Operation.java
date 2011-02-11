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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

public class NewScoutProjectStep2Operation implements IOperation {

  private final NewBsiCaseGroupStep1Operation m_step1;
  private final ITemplateVariableSet m_templateBindings;

  public NewScoutProjectStep2Operation(NewBsiCaseGroupStep1Operation step1, ITemplateVariableSet templateBindings) {
    m_step1 = step1;
    m_templateBindings = templateBindings;
  }

  @Override
  public String getOperationName() {
    return "Fill Scout Project";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_step1 == null) {
      throw new IllegalArgumentException("previous step is null");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    List<IProject> projects = new ArrayList<IProject>();
    try {
      if (m_step1.getSharedProject() != null) {
        new FillSharedPluginOperation(m_step1.getSharedProject(), m_templateBindings).run(monitor, workingCopyManager);
        projects.add(m_step1.getSharedProject());
      }
      if (m_step1.getClientProject() != null) {
        new FillClientPluginOperation(m_step1.getClientProject(), m_templateBindings).run(monitor, workingCopyManager);
        projects.add(m_step1.getClientProject());
        if (m_step1.getClientTestProject() != null) {
          new FillClientTestPluginOperation(m_step1.getClientTestProject(), m_templateBindings).run(monitor, workingCopyManager);
          projects.add(m_step1.getClientTestProject());
        }
      }
      if (m_step1.getServerProject() != null) {
        new FillServerPluginOperation(m_step1.getServerProject(), m_templateBindings).run(monitor, workingCopyManager);
        projects.add(m_step1.getServerProject());
        if (m_step1.getServerTestProject() != null) {
          new FillServerTestPluginOperation(m_step1.getServerTestProject(), m_templateBindings).run(monitor, workingCopyManager);
          projects.add(m_step1.getServerTestProject());
        }
      }
      if (m_step1.getUiSwingProject() != null) {
        new FillUiSwingPluginOperation(m_step1.getUiSwingProject(), m_templateBindings).run(monitor, workingCopyManager);
        projects.add(m_step1.getUiSwingProject());
      }
      if (m_step1.getUiSwtProject() != null) {
        new FillUiSwtPluginOperation(m_step1.getUiSwtProject(), m_templateBindings).run(monitor, workingCopyManager);
        projects.add(m_step1.getUiSwtProject());
      }
      if (m_step1.getUiSwtAppProject() != null) {
        new FillUiSwtApplicationPluginOperation(m_step1.getUiSwtAppProject(), m_templateBindings).run(monitor, workingCopyManager);
        projects.add(m_step1.getUiSwtAppProject());
      }
    }
    finally {
      // refresh all
      for (IProject project : projects) {
        project.refreshLocal(IResource.DEPTH_INFINITE, null);
      }
    }
  }

}
