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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * creates a eclipse plugin
 */
public class CreateEclipseFeatureOperation implements IOperation {

  private String m_symbolicName;
  private HashSet<String> m_natures;
  private IProject m_createdProject;

  @SuppressWarnings("restriction")
  public CreateEclipseFeatureOperation(String symbolicName) {
    m_symbolicName = symbolicName;
    m_natures = new HashSet<String>();
    addNature(org.eclipse.pde.internal.core.natures.PDE.FEATURE_NATURE);
  }

  @Override
  public void validate() throws IllegalArgumentException {
    // check for exist
    if (ResourcesPlugin.getWorkspace().getRoot().getProject(m_symbolicName).exists()) {
      throw new IllegalArgumentException("Bundle: " + m_symbolicName + " exists already!");
    }

  }

  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    m_createdProject = null;
    try {
      m_createdProject = createProject(monitor);
    }
    catch (CoreException e) {
      throw e;
    }
    catch (Exception e1) {
      throw new CoreException(new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, "error during createing project", e1));
    }
  }

  @SuppressWarnings("restriction")
  private IProject createProject(IProgressMonitor monitor) throws CoreException, IOException, InvocationTargetException, InterruptedException {
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(m_symbolicName);
    project.create(monitor);
    project.open(monitor);
    // set the natures of the project
    IProjectDescription description = project.getDescription();
    description.setNatureIds(m_natures.toArray(new String[0]));
    org.eclipse.core.internal.events.BuildCommand buildCommand = new org.eclipse.core.internal.events.BuildCommand();
    buildCommand.setName("org.eclipse.pde.FeatureBuilder");
    description.setBuildSpec(new ICommand[]{buildCommand});
    project.setDescription(description, monitor);
    return project;
  }

  public String getSymbolicName() {
    return m_symbolicName;
  }

  public IProject getCreatedProject() {
    return m_createdProject;
  }

  public void addNature(String name) {
    m_natures.add(name);
  }

  public Set<String> getNatures() {
    return Collections.unmodifiableSet(m_natures);
  }

  public String getOperationName() {
    return "Create Eclipse Feature";
  }

}
