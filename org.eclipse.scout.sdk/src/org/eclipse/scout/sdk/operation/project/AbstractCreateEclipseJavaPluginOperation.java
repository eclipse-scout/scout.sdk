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

import java.util.Map;

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
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * Creates an eclipse project (without Manifest.mf, Activator.class and plugin.xml)
 */
public abstract class AbstractCreateEclipseJavaPluginOperation extends AbstractCreateEclipseProjectOperation {

  private IJavaProject m_javaProject;
  private boolean m_createResourcesFolder = false;

  @SuppressWarnings("restriction")
  public AbstractCreateEclipseJavaPluginOperation() {
    super();
    addNature(org.eclipse.pde.internal.core.natures.PDE.PLUGIN_NATURE);
    addNature(JavaCore.NATURE_ID);
    m_createResourcesFolder = true;
  }

  @Override
  public String getOperationName() {
    return "Create Eclipse Plugin";
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    IProject project = getCreatedProject();

    // src folder
    IFolder folder = project.getFolder(TypeUtility.DEFAULT_SOURCE_FOLDER_NAME);
    if (!folder.exists()) {
      ResourceUtility.mkdirs(folder, monitor);
    }

    // resources folder
    folder = project.getFolder("resources");
    if (isCreateResourcesFolder() && !folder.exists()) {
      ResourceUtility.mkdirs(folder, monitor);
    }

    m_javaProject = JavaCore.create(project);
    IPath path = project.getFullPath().append("bin");
    m_javaProject.setOutputLocation(path, null);
    IClasspathEntry[] entries = new IClasspathEntry[3];
    IPath pathSrc = project.getFullPath().append(TypeUtility.DEFAULT_SOURCE_FOLDER_NAME);
    entries[0] = JavaCore.newSourceEntry(pathSrc);
    entries[1] = createJREEntry(getExecutionEnvironment());
    entries[2] = createContainerEntry();
    m_javaProject.setRawClasspath(entries, null);

    if (isUseDefaultJdtPrefs()) {
      // default scout preferences
      Map<String, String> props = getStringProperties();
      new InstallTextFileOperation("templates/all/.settings/org.eclipse.jdt.core.prefs", ".settings/org.eclipse.jdt.core.prefs", project, props).run(monitor, workingCopyManager);
      new InstallTextFileOperation("templates/all/.settings/org.eclipse.jdt.ui.prefs", ".settings/org.eclipse.jdt.ui.prefs", project, props).run(monitor, workingCopyManager);
    }

    addCreatedBundle(getJavaProject());
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

  public void setCreateResourcesFolder(boolean createResourcesFolder) {
    m_createResourcesFolder = createResourcesFolder;
  }

  public boolean isCreateResourcesFolder() {
    return m_createResourcesFolder;
  }
}