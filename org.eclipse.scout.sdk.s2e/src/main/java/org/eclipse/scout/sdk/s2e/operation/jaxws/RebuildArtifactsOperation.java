/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.operation.jaxws;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.project.ResolverConfiguration;
import org.eclipse.scout.sdk.core.s.IMavenConstants;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.operation.IOperation;
import org.eclipse.scout.sdk.s2e.operation.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link RebuildArtifactsOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class RebuildArtifactsOperation implements IOperation {

  public static final String WORKING_DIRECTORY = "org.eclipse.jdt.launching.WORKING_DIRECTORY";
  public static final String M2_PROFILES = "M2_PROFILES";
  public static final String JRE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER";
  /**
   * see org.eclipse.m2e.actions.MavenLaunchConstants in plug-in 'org.eclipse.m2e.launching'.
   */
  public static final String LAUNCH_CONFIGURATION_TYPE_ID = "org.eclipse.m2e.Maven2LaunchConfigurationType";
  public static final String ATTR_GOALS = "M2_GOALS";

  private final CountDownLatch m_artifactGenCompleted;
  private IJavaProject m_javaProject;

  public RebuildArtifactsOperation() {
    m_artifactGenCompleted = new CountDownLatch(1);
  }

  @Override
  public String getOperationName() {
    return "Rebuild all Web Service Artifacts of Project " + getJavaProject().getElementName();
  }

  @Override
  public void validate() {
    Validate.isTrue(S2eUtils.exists(getJavaProject()), "Java Project must exist.");
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 100);
    IProject project = getJavaProject().getProject();

    // refresh project
    project.refreshLocal(IResource.DEPTH_INFINITE, progress.newChild(2));
    if (progress.isCanceled()) {
      return;
    }

    // delete /target folder contents
    deleteOutputFolderContents(project, progress.newChild(4));
    if (progress.isCanceled()) {
      return;
    }

    // schedule maven build 'clean compile'
    scheduleArtifactBuild(project, progress.newChild(44));
    if (progress.isCanceled()) {
      return;
    }

    // wait until the forked java process has ended
    progress.setTaskName("Wait for Maven build...");
    waitForArtifactBuildCompleted();
    if (progress.isCanceled()) {
      return;
    }
    progress.worked(48);

    // refresh the project to 'see' the new artifacts
    progress.setTaskName("Refresh project");
    project.refreshLocal(IResource.DEPTH_INFINITE, progress.newChild(2));
  }

  protected void waitForArtifactBuildCompleted() {
    try {
      m_artifactGenCompleted.await(3, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      SdkLog.debug(e);
    }
  }

  protected void scheduleArtifactBuild(IProject project, final IProgressMonitor monitor) throws CoreException {
    final DebugPlugin debugPlugin = DebugPlugin.getDefault();
    final ILaunchManager launchManager = debugPlugin.getLaunchManager();
    final Set<IProcess> watchedProcesses = new HashSet<>(1);
    final ILaunchConfiguration launchConfiguration = createLaunchConfiguration(project);
    final IDebugEventSetListener eventSetListener = new IDebugEventSetListener() {
      @Override
      public void handleDebugEvents(DebugEvent[] events) {
        synchronized (watchedProcesses) {
          for (DebugEvent e : events) {
            Object source = e.getSource();
            if (source != null && watchedProcesses.contains(source)) {
              IProcess changedProcess = (IProcess) source;
              if (changedProcess.isTerminated()) {
                int exitValue = 0;
                try {
                  exitValue = changedProcess.getExitValue();
                }
                catch (DebugException ex) {
                  SdkLog.error("Error reading exit value.", ex);
                }
                if (exitValue != 0) {
                  SdkLog.warning("Maven build failed with error code {}. See Maven Console for details.", exitValue);
                }
                watchedProcesses.remove(changedProcess);
              }
            }
          }

          if (watchedProcesses.isEmpty()) {
            // last process has ended -> finish
            debugPlugin.removeDebugEventListener(this);
            m_artifactGenCompleted.countDown(); // signal that all processes ended and the build has finished
          }
        }
      }
    };

    final ILaunchListener launchListener = new ILaunchListener() {
      @Override
      public void launchRemoved(ILaunch launch) {
      }

      @Override
      public void launchChanged(ILaunch launch) {
        IProcess[] processes = launch.getProcesses();
        if (processes == null || processes.length < 1) {
          return;
        }

        synchronized (watchedProcesses) {
          debugPlugin.addDebugEventListener(eventSetListener);
          for (IProcess p : processes) {
            watchedProcesses.add(p);
          }
          launchManager.removeLaunchListener(this);
        }
      }

      @Override
      public void launchAdded(ILaunch launch) {
      }
    };

    launchManager.addLaunchListener(launchListener);
    launchConfiguration.launch("run", monitor, false, true);
  }

  protected static ILaunchConfiguration createLaunchConfiguration(IContainer basedir) throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType launchConfigurationType = launchManager.getLaunchConfigurationType(LAUNCH_CONFIGURATION_TYPE_ID);
    ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType.newInstance(null, "-cleanCompile");
    workingCopy.setAttribute(WORKING_DIRECTORY, basedir.getLocation().toOSString());
    workingCopy.setAttribute(ATTR_GOALS, "clean compile");
    workingCopy.setAttribute(ILaunchManager.ATTR_PRIVATE, true);
    setProjectConfiguration(workingCopy, basedir);
    IPath path = getJreContainerPath(basedir);
    if (path != null) {
      workingCopy.setAttribute(JRE_CONTAINER, path.toPortableString());
    }
    return workingCopy;
  }

  protected static void setProjectConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IContainer basedir) {
    IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
    IFile pomFile = basedir.getFile(new Path(IMavenConstants.POM));
    IMavenProjectFacade projectFacade = projectManager.create(pomFile, false, new NullProgressMonitor());
    if (projectFacade != null) {
      ResolverConfiguration configuration = projectFacade.getResolverConfiguration();
      String selectedProfiles = configuration.getSelectedProfiles();
      if (StringUtils.isNotBlank(selectedProfiles)) {
        workingCopy.setAttribute(M2_PROFILES, selectedProfiles);
      }
    }
  }

  protected static IPath getJreContainerPath(IContainer basedir) throws CoreException {
    IProject project = basedir.getProject();
    if (project != null && project.hasNature(JavaCore.NATURE_ID)) {
      IJavaProject javaProject = JavaCore.create(project);
      if (S2eUtils.exists(javaProject)) {
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        for (int i = 0; i < entries.length; ++i) {
          IClasspathEntry entry = entries[i];
          if (JRE_CONTAINER.equals(entry.getPath().segment(0))) {
            return entry.getPath();
          }
        }
      }
    }
    return null;
  }

  protected void deleteOutputFolderContents(IProject project, SubMonitor progress) throws CoreException {
    final Set<IResource> resourcesToDelete = new HashSet<>();
    final IFolder outFolder = project.getFolder("target");
    if (!outFolder.exists()) {
      return;
    }

    outFolder.accept(new IResourceVisitor() {
      @Override
      public boolean visit(IResource resource) throws CoreException {
        boolean isOutFolder = Objects.equals(outFolder, resource);
        if (!isOutFolder) {
          resourcesToDelete.add(resource);
        }
        return isOutFolder;
      }
    });
    progress.beginTask("Delete existing Artifacts", resourcesToDelete.size());
    for (IResource r : resourcesToDelete) {
      try {
        r.delete(IResource.FORCE, progress.newChild(1));
      }
      catch (CoreException e) {
        SdkLog.warning("Unable to delete resource '{}'.", r.getFullPath().toOSString(), e);
      }
    }
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  public void setJavaProject(IJavaProject javaProject) {
    m_javaProject = javaProject;
  }
}
