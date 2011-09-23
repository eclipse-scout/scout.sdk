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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.scout.sdk.operation.template.InstallTextFileOperation;
import org.eclipse.scout.sdk.pde.PdeUtility;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * Creates an eclipse project (without Manifest.mf, Activator.class and plugin.xml)
 */
public class CreateEclipseJavaPluginOperation extends CreateEclipseProjectOperation {

  private String m_execEnvId;
  private IJavaProject m_javaProject;

  @SuppressWarnings("restriction")
  public CreateEclipseJavaPluginOperation() {
    super();
    m_execEnvId = "JavaSE-1.6";
    addNature(org.eclipse.pde.internal.core.natures.PDE.PLUGIN_NATURE);
    addNature(JavaCore.NATURE_ID);
  }

  public void setExecutionEnvironment(String s) {
    m_execEnvId = s;
  }

  public String getExecutionEnvironment() {
    return m_execEnvId;
  }

  @Override
  public String getOperationName() {
    return "Create Eclipse Plugin";
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();
    // src folder
    IFolder folder = project.getFolder("src");
    if (!folder.exists()) {
      PdeUtility.createFolder(folder);
    }
    // resources folder
    folder = project.getFolder("resources");
    if (!folder.exists()) {
      PdeUtility.createFolder(folder);
    }
    //
    m_javaProject = JavaCore.create(project);
    IPath path = project.getFullPath().append("bin");
    m_javaProject.setOutputLocation(path, null);
    IClasspathEntry[] entries = new IClasspathEntry[3];
    IPath pathSrc = project.getFullPath().append("src");
    entries[0] = JavaCore.newSourceEntry(pathSrc);
    entries[1] = createJREEntry(m_execEnvId);
    entries[2] = createContainerEntry();
    m_javaProject.setRawClasspath(entries, null);
    // files
    new InstallTextFileOperation("templates/all/.settings/org.eclipse.jdt.core.prefs", ".settings/org.eclipse.jdt.core.prefs", project).run(monitor, workingCopyManager);
    new InstallTextFileOperation("templates/all/.settings/org.eclipse.jdt.ui.prefs", ".settings/org.eclipse.jdt.ui.prefs", project).run(monitor, workingCopyManager);
  }

  /**
   * J2SE-1.5 =
   * org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/J2SE-1.5
   * JavaSE-1.6 =
   * org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6
   */
  private IClasspathEntry createJREEntry(String envId) {
    IPath path = null;
    if (envId != null) {
      IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
      IExecutionEnvironment env = manager.getEnvironment(envId);
      if (env != null) {
        path = JavaRuntime.newJREContainerPath(env);
      }
    }
    if (path == null) {
      path = JavaRuntime.newDefaultJREContainerPath();
    }
    return JavaCore.newContainerEntry(path);
  }

  @SuppressWarnings("restriction")
  private IClasspathEntry createContainerEntry() {
    return JavaCore.newContainerEntry(org.eclipse.pde.internal.core.PDECore.REQUIRED_PLUGINS_CONTAINER_PATH);
  }

}
