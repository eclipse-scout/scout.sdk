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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * Creates an eclipse project (without Manifest.mf, Activator.class and plugin.xml)
 */
public class CreateEclipseProjectOperation implements IOperation {

  private String m_symbolicName;
  private HashSet<String> m_natures = new HashSet<String>();
  private IProject m_createdProject;

  public CreateEclipseProjectOperation() {

  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getSymbolicName())) {
      throw new IllegalArgumentException("symbolic name can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    // check for exist
    if (ResourcesPlugin.getWorkspace().getRoot().getProject(getSymbolicName()).exists()) {
      ScoutSdk.logError("Bundle: " + getSymbolicName() + " exists already!");
      throw new CoreException(new ScoutStatus("Bundle: " + getSymbolicName() + " exists already!"));
    }
    m_createdProject = null;
    m_createdProject = createProject(monitor, workingCopyManager);
  }

  public String getSymbolicName() {
    return m_symbolicName;
  }

  public void setSymbolicName(String symbolicName) {
    m_symbolicName = symbolicName;
  }

  public void addNature(String name) {
    m_natures.add(name);
  }

  public Set<String> getNatures() {
    return Collections.unmodifiableSet(m_natures);
  }

  @Override
  public String getOperationName() {
    return "Create Eclipse Project";
  }

  public IProject getCreatedProject() {
    return m_createdProject;
  }

  protected IProject createProject(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getSymbolicName());
    project.create(monitor);
    project.open(monitor);
    // set the natures of the project
    IProjectDescription description = project.getDescription();
    description.setNatureIds(m_natures.toArray(new String[0]));
    project.setDescription(description, monitor);
    return project;
  }

}
