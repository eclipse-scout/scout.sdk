/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.operation;

import static java.util.Collections.addAll;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.toScoutProgress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenRunnerSpi;
import org.eclipse.scout.sdk.core.s.util.maven.MavenBuild;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link MavenBuildOperation}</h3>
 *
 * @since 5.2.0
 */
public class MavenBuildOperation implements BiConsumer<IEnvironment, IProgress> {

  /**
   * see org.eclipse.m2e.actions.MavenLaunchConstants in plug-in 'org.eclipse.m2e.launching'.
   */
  public static final String LAUNCH_CONFIGURATION_TYPE_ID = "org.eclipse.m2e.Maven2LaunchConfigurationType";
  /**
   * see org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants
   */
  public static final String WORKING_DIRECTORY = "org.eclipse.jdt.launching.WORKING_DIRECTORY";
  public static final String JRE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER";

  public static final String M2_PROFILES = "M2_PROFILES";
  public static final String M2_GOALS = "M2_GOALS";
  public static final String M2_PROPERTIES = "M2_PROPERTIES";
  public static final String M2_OFFLINE = "M2_OFFLINE";
  public static final String M2_UPDATE_SNAPSHOTS = "M2_UPDATE_SNAPSHOTS";
  public static final String M2_SKIP_TESTS = "M2_SKIP_TESTS";
  public static final String M2_NON_RECURSIVE = "M2_NON_RECURSIVE";
  public static final String M2_WORKSPACE_RESOLUTION = "M2_WORKSPACE_RESOLUTION";
  public static final String M2_DEBUG_OUTPUT = "M2_DEBUG_OUTPUT";
  public static final String M2_THREADS = "M2_THREADS";

  private static final AtomicLong BUILD_NAME_NUM = new AtomicLong(0L);

  private MavenBuild m_build;
  private volatile CountDownLatch m_artifactGenCompleted;
  private boolean m_waitUntilCompleted;
  private final List<Integer> m_mavenReturnCodes;

  public MavenBuildOperation() {
    m_mavenReturnCodes = new ArrayList<>(1);
    m_waitUntilCompleted = true; // default to wait
  }

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    Ensure.notNull(getBuild());
    Ensure.isTrue(!getBuild().getGoals().isEmpty());
    Ensure.notNull(getBuild().getWorkingDirectory());

    progress.init(toString(), 10);

    m_artifactGenCompleted = new CountDownLatch(1);
    try {
      scheduleMavenBuild(progress);
      progress.setWorkRemaining(4);

      if (isWaitUntilCompleted()) {
        // wait until the forked java process has ended
        waitForArtifactBuildCompleted();

        for (Integer mavenReturnCode : m_mavenReturnCodes) {
          if (mavenReturnCode == null || mavenReturnCode != 0) {
            throw newFail("Maven build failed with error code {}. See Maven Console for details.", mavenReturnCode);
          }
        }
      }
      progress.worked(4);
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
  }

  protected void scheduleMavenBuild(IProgress progress) throws CoreException {
    DebugPlugin debugPlugin = DebugPlugin.getDefault();
    ILaunchManager launchManager = debugPlugin.getLaunchManager();
    Collection<IProcess> watchedProcesses = new HashSet<>(1);
    ILaunchConfiguration launchConfiguration = createLaunchConfiguration(toScoutProgress(progress.newChild(1)).monitor());
    IDebugEventSetListener eventSetListener = new IDebugEventSetListener() {
      @Override
      public void handleDebugEvents(DebugEvent[] events) {
        synchronized (watchedProcesses) {
          for (DebugEvent e : events) {
            Object source = e.getSource();
            //noinspection SuspiciousMethodCalls
            if (source != null && watchedProcesses.contains(source)) {
              IProcess changedProcess = (IProcess) source;
              if (changedProcess.isTerminated()) {
                int exitValue = -1;
                try {
                  exitValue = changedProcess.getExitValue();
                }
                catch (DebugException ex) {
                  SdkLog.error("Error reading exit value.", ex);
                }
                m_mavenReturnCodes.add(exitValue);
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

    ILaunchListener launchListener = new ILaunchListener() {
      @Override
      public void launchRemoved(ILaunch launch) {
        // not interesting
      }

      @Override
      public void launchChanged(ILaunch launch) {
        IProcess[] processes = launch.getProcesses();
        if (processes == null || processes.length < 1) {
          return;
        }

        synchronized (watchedProcesses) {
          debugPlugin.addDebugEventListener(eventSetListener);
          addAll(watchedProcesses, processes);
          launchManager.removeLaunchListener(this);
        }
      }

      @Override
      public void launchAdded(ILaunch launch) {
        // not interesting
      }
    };

    launchManager.addLaunchListener(launchListener);

    SdkLog.debug("Executing embedded {}", getBuild().toString());
    launchConfiguration.launch("run", toScoutProgress(progress.newChild(5)).monitor(), false, true);
  }

  protected void waitForArtifactBuildCompleted() {
    try {
      m_artifactGenCompleted.await(15, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      SdkLog.debug(e);
    }
  }

  protected IContainer getWorkspaceContainer() {
    IContainer[] containers = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(getBuild().getWorkingDirectory().toUri());
    if (containers.length < 1) {
      return null;
    }
    return containers[0];
  }

  protected ILaunchConfiguration createLaunchConfiguration(IProgressMonitor monitor) throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType launchConfigurationType = launchManager.getLaunchConfigurationType(LAUNCH_CONFIGURATION_TYPE_ID);
    ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType.newInstance(null, "-mavenBuild" + BUILD_NAME_NUM.getAndIncrement());
    MavenBuild build = getBuild();
    Map<String, String> properties = build.getProperties();

    workingCopy.setAttribute(WORKING_DIRECTORY, build.getWorkingDirectory().toAbsolutePath().toString());
    workingCopy.setAttribute(ILaunchManager.ATTR_PRIVATE, true);
    workingCopy.setAttribute(M2_UPDATE_SNAPSHOTS, build.hasOption(MavenBuild.OPTION_UPDATE_SNAPSHOTS));
    workingCopy.setAttribute(M2_OFFLINE, build.hasOption(MavenBuild.OPTION_OFFLINE));
    workingCopy.setAttribute(M2_SKIP_TESTS, properties.containsKey(MavenBuild.PROPERTY_SKIP_TESTS) || properties.containsKey(MavenBuild.PROPERTY_SKIP_TEST_CREATION));
    workingCopy.setAttribute(M2_NON_RECURSIVE, build.hasOption(MavenBuild.OPTION_NON_RECURSIVE));
    workingCopy.setAttribute(M2_WORKSPACE_RESOLUTION, true);
    workingCopy.setAttribute(M2_DEBUG_OUTPUT, SdkLog.isDebugEnabled() || build.hasOption(MavenBuild.OPTION_DEBUG));
    workingCopy.setAttribute(M2_THREADS, 1);
    // not supported yet: "M2_PROFILES" and "M2_USER_SETTINGS"
    workingCopy.setAttribute(M2_PROPERTIES, build.getPropertiesAsList());

    setGoals(workingCopy, build.getGoals());

    IContainer container = getWorkspaceContainer();
    setProjectConfiguration(workingCopy, container, monitor);
    setJreContainerPath(workingCopy, container);

    return workingCopy;
  }

  protected static void setJreContainerPath(ILaunchConfigurationWorkingCopy workingCopy, IResource container) throws CoreException {
    IPath path = getJreContainerPath(container);
    if (path != null) {
      workingCopy.setAttribute(JRE_CONTAINER, path.toPortableString());
    }
  }

  protected static void setGoals(ILaunchConfigurationWorkingCopy workingCopy, Iterable<String> goals) {
    StringBuilder goalBuilder = new StringBuilder();
    Iterator<String> iterator = goals.iterator();
    goalBuilder.append(iterator.next());
    while (iterator.hasNext()) {
      goalBuilder.append(' ').append(iterator.next());
    }
    workingCopy.setAttribute(M2_GOALS, goalBuilder.toString());
  }

  protected static void setProjectConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IContainer basedir, IProgressMonitor monitor) {
    if (basedir == null || !basedir.exists() || basedir.getType() != IResource.PROJECT) {
      return;
    }

    IMavenProjectRegistry projectManager = MavenPlugin.getMavenProjectRegistry();
    if (projectManager == null) {
      return;
    }

    IFile pomFile = basedir.getFile(new Path(IMavenConstants.POM));
    IMavenProjectFacade projectFacade = projectManager.create(pomFile, false, monitor);
    if (projectFacade == null) {
      return;
    }

    ResolverConfiguration configuration = projectFacade.getResolverConfiguration();
    if (configuration == null) {
      return;
    }

    String selectedProfiles = configuration.getSelectedProfiles();
    if (Strings.hasText(selectedProfiles)) {
      workingCopy.setAttribute(M2_PROFILES, selectedProfiles);
    }
  }

  protected static IPath getJreContainerPath(IResource basedir) throws CoreException {
    if (basedir == null || !basedir.exists()) {
      return null;
    }

    IProject project = basedir.getProject();
    if (project == null || !project.hasNature(JavaCore.NATURE_ID)) {
      return null;
    }

    IJavaProject javaProject = JavaCore.create(project);
    if (!JdtUtils.exists(javaProject)) {
      return null;
    }

    IClasspathEntry[] entries = javaProject.getRawClasspath();
    for (IClasspathEntry entry : entries) {
      if (JRE_CONTAINER.equals(entry.getPath().segment(0))) {
        return entry.getPath();
      }
    }
    return null;
  }

  public boolean isWaitUntilCompleted() {
    return m_waitUntilCompleted;
  }

  public void setWaitUntilCompleted(boolean waitUntilCompleted) {
    m_waitUntilCompleted = waitUntilCompleted;
  }

  public MavenBuild getBuild() {
    return m_build;
  }

  public void setBuild(MavenBuild build) {
    m_build = build;
  }

  public static final class M2eMavenRunner implements IMavenRunnerSpi {
    @Override
    public void execute(MavenBuild build, IEnvironment env, IProgress progress) {
      MavenBuildOperation op = new MavenBuildOperation();
      op.setBuild(build);
      op.accept(env, progress);
    }
  }

  @Override
  public String toString() {
    return "Maven Build";
  }
}
