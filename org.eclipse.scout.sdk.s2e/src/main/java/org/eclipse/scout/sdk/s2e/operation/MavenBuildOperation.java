/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IContainer;
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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.MavenPlugin;
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
    Ensure.isFalse(getBuild().getGoals().isEmpty());
    Ensure.notNull(getBuild().getWorkingDirectory());

    progress.init(10, toString());

    m_artifactGenCompleted = new CountDownLatch(1);
    try {
      scheduleMavenBuild(progress);
      progress.setWorkRemaining(4);

      if (isWaitUntilCompleted()) {
        // wait until the forked java process has ended
        waitForArtifactBuildCompleted();

        for (var mavenReturnCode : m_mavenReturnCodes) {
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

  private final class DebugEventSetListener implements IDebugEventSetListener {

    private final Collection<IProcess> m_expectedProcesses;

    private DebugEventSetListener(Collection<IProcess> expectedProcesses) {
      m_expectedProcesses = expectedProcesses;
    }

    @Override
    public void handleDebugEvents(DebugEvent[] events) {
      synchronized (m_expectedProcesses) {
        for (var e : events) {
          var source = e.getSource();
          //noinspection SuspiciousMethodCalls
          if (source != null && m_expectedProcesses.contains(source)) {
            var changedProcess = (IProcess) source;
            if (changedProcess.isTerminated()) {
              var exitValue = -1;
              try {
                exitValue = changedProcess.getExitValue();
              }
              catch (DebugException ex) {
                SdkLog.error("Error reading exit value.", ex);
              }
              m_mavenReturnCodes.add(exitValue);
              m_expectedProcesses.remove(changedProcess);
            }
          }
        }

        if (m_expectedProcesses.isEmpty()) {
          // last process has ended -> finish
          DebugPlugin.getDefault().removeDebugEventListener(this);
          m_artifactGenCompleted.countDown(); // signal that all processes ended and the build has finished
        }
      }
    }
  }

  private final class LaunchListener implements ILaunchListener {

    private final Collection<IProcess> m_watchedProcesses;

    private LaunchListener() {
      m_watchedProcesses = new HashSet<>(1);
    }

    @Override
    public void launchRemoved(ILaunch launch) {
      // not interesting
    }

    @Override
    public void launchAdded(ILaunch launch) {
      // not interesting
    }

    @Override
    public void launchChanged(ILaunch launch) {
      var processes = launch.getProcesses();
      if (processes == null || processes.length < 1) {
        return;
      }

      var debugPlugin = DebugPlugin.getDefault();
      synchronized (m_watchedProcesses) {
        debugPlugin.addDebugEventListener(new DebugEventSetListener(m_watchedProcesses));
        addAll(m_watchedProcesses, processes);
        debugPlugin.getLaunchManager().removeLaunchListener(this);
      }
    }
  }

  protected void scheduleMavenBuild(IProgress progress) throws CoreException {
    var launchConfiguration = createLaunchConfiguration(toScoutProgress(progress.newChild(1)).monitor());
    SdkLog.debug("Executing embedded {}", getBuild().toString());

    DebugPlugin.getDefault().getLaunchManager().addLaunchListener(new LaunchListener());
    launchConfiguration.launch(ILaunchManager.RUN_MODE, toScoutProgress(progress.newChild(5)).monitor(), false, true);
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
    var containers = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(getBuild().getWorkingDirectory().toUri());
    if (containers.length < 1) {
      return null;
    }
    return containers[0];
  }

  protected ILaunchConfiguration createLaunchConfiguration(IProgressMonitor monitor) throws CoreException {
    var launchManager = DebugPlugin.getDefault().getLaunchManager();
    var launchConfigurationType = launchManager.getLaunchConfigurationType(LAUNCH_CONFIGURATION_TYPE_ID);
    var workingCopy = launchConfigurationType.newInstance(null, "-mavenBuild" + BUILD_NAME_NUM.getAndIncrement());
    var build = getBuild();
    var properties = build.getProperties();

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

    var container = getWorkspaceContainer();
    setProjectConfiguration(workingCopy, container, monitor);
    setJreContainerPath(workingCopy, container);

    return workingCopy;
  }

  protected static void setJreContainerPath(ILaunchConfigurationWorkingCopy workingCopy, IResource container) throws CoreException {
    var path = getJreContainerPath(container);
    if (path != null) {
      workingCopy.setAttribute(JRE_CONTAINER, path.toPortableString());
    }
  }

  protected static void setGoals(ILaunchConfigurationWorkingCopy workingCopy, Iterable<String> goals) {
    var goalBuilder = new StringBuilder();
    var iterator = goals.iterator();
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

    var projectManager = MavenPlugin.getMavenProjectRegistry();
    if (projectManager == null) {
      return;
    }

    var pomFile = basedir.getFile(new Path(IMavenConstants.POM));
    var projectFacade = projectManager.create(pomFile, false, monitor);
    if (projectFacade == null) {
      return;
    }

    var configuration = projectFacade.getResolverConfiguration();
    if (configuration == null) {
      return;
    }

    var selectedProfiles = configuration.getSelectedProfiles();
    if (Strings.hasText(selectedProfiles)) {
      workingCopy.setAttribute(M2_PROFILES, selectedProfiles);
    }
  }

  protected static IPath getJreContainerPath(IResource basedir) throws CoreException {
    if (basedir == null || !basedir.exists()) {
      return null;
    }

    var project = basedir.getProject();
    if (project == null || !project.hasNature(JavaCore.NATURE_ID)) {
      return null;
    }

    var javaProject = JavaCore.create(project);
    if (!JdtUtils.exists(javaProject)) {
      return null;
    }

    var entries = javaProject.getRawClasspath();
    return Arrays.stream(entries)
        .filter(entry -> JRE_CONTAINER.equals(entry.getPath().segment(0)))
        .findFirst()
        .map(IClasspathEntry::getPath)
        .orElse(null);
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
      var op = new MavenBuildOperation();
      op.setBuild(build);
      op.accept(env, progress);
    }
  }

  @Override
  public String toString() {
    return "Maven Build";
  }
}
