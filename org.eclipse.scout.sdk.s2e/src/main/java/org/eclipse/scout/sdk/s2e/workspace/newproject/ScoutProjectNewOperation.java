/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.workspace.newproject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.scout.sdk.core.s.project.ScoutProjectNewHelper;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;
import org.eclipse.scout.sdk.s2e.workspace.IOperation;
import org.eclipse.scout.sdk.s2e.workspace.IWorkingCopyManager;

/**
 * <h3>{@link ScoutProjectNewOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class ScoutProjectNewOperation implements IOperation {

  public static final String TEMPLATE_GROUP_ID = "org.eclipse.scout.archetype.groupId";
  public static final String TEMPLATE_ARTIFACT_ID = "org.eclipse.scout.archetype.artifactId";
  public static final String TEMPLATE_VERSION = "org.eclipse.scout.archetype.version";

  private static final String EXEC_ENV_PREFIX = "JavaSE-";
  private static final String MIN_JVM_VERSION = "1.7";

  private String m_symbolicName;
  private String m_displayName;
  private File m_targetDirectory;

  @Override
  public String getOperationName() {
    return "Creating new Scout project...";
  }

  @Override
  public void validate() {
    // is done in ScoutProjectNewHelper
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    try {
      String groupId = S2ESdkActivator.getDefault().getBundle().getBundleContext().getProperty(TEMPLATE_GROUP_ID);
      String artifactId = S2ESdkActivator.getDefault().getBundle().getBundleContext().getProperty(TEMPLATE_ARTIFACT_ID);
      String version = S2ESdkActivator.getDefault().getBundle().getBundleContext().getProperty(TEMPLATE_VERSION);
      if (StringUtils.isBlank(groupId) || StringUtils.isBlank(artifactId) || StringUtils.isBlank(version)) {
        // use default
        groupId = null;
        artifactId = null;
        version = null;
      }

      monitor.beginTask(getOperationName(), 100);
      ScoutProjectNewHelper.createProject(getTargetDirectory(), getSymbolicName(), getDisplayName(), Double.toString(getExecEnvVersion(getDefaultJvmExecutionEnvironment())), groupId, artifactId, version);
      monitor.worked(10);

      importIntoWorkspace(new SubProgressMonitor(monitor, 90));
    }
    catch (Exception e) {
      throw new CoreException(new ScoutStatus("Unable to create Scout Project.", e));
    }
    finally {
      monitor.done();
    }
  }

  /**
   * Gets the default execution environment (e.g. "JavaSE-1.8") supported in the current default JVMs and the given
   * target platform.<br>
   * Use {@link #getExecEnvVersion(String)} to parse the execution environment to a double.
   *
   * @param targetPlatformVersion
   *          The target platform to which the execution environment must be compatible or <code>null</code> if no
   *          compatibility should be ensured.
   * @return A string like "JavaSE-1.8" with the latest version supported in the current default JVMs and the given
   *         target platform.
   * @see #getExecEnvVersion(String)
   */
  protected static String getDefaultJvmExecutionEnvironment() {
    // defaults
    String execEnv = EXEC_ENV_PREFIX + MIN_JVM_VERSION;
    double execEnvVersion = getExecEnvVersion(execEnv);

    IVMInstall defaultVm = JavaRuntime.getDefaultVMInstall();
    if (defaultVm != null) {
      for (IExecutionEnvironment env : JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments()) {
        String executionEnvId = env.getId();
        if (env.isStrictlyCompatible(defaultVm)) {
          double envVersion = getExecEnvVersion(executionEnvId);
          if (envVersion > execEnvVersion) {
            execEnv = executionEnvId; // take the newest
          }
        }
      }
    }
    return execEnv;
  }

  /**
   * Takes an java execution environment (e.g. "JavaSE-1.8") and parses the version as double (in this example 1.8).<br>
   * If an invalid value is passed, always 1.7 is returned as minimal version.<br>
   * Use {@link #getDefaultJvmExecutionEnvironment()} to get the default execution environment in the current workspace.
   *
   * @param executionEnvId
   *          The execution environment to parse.
   * @return The version as double.
   */
  protected static double getExecEnvVersion(String executionEnvId) {
    if (executionEnvId != null && executionEnvId.startsWith(EXEC_ENV_PREFIX)) {
      String numPart = executionEnvId.substring(EXEC_ENV_PREFIX.length());
      if (StringUtils.isNotBlank(numPart)) {
        try {
          double ret = Double.parseDouble(numPart);
          return ret;
        }
        catch (NumberFormatException e) {
          //nop
        }
      }
    }
    return Double.parseDouble(MIN_JVM_VERSION);
  }

  /**
   * Imports the extracted projects into the workspace using m2e import
   *
   * @throws CoreException
   */
  protected void importIntoWorkspace(IProgressMonitor monitor) throws CoreException {
    File baseFolder = new File(getTargetDirectory(), getSymbolicName());
    File[] subFolders = baseFolder.listFiles();
    Collection<MavenProjectInfo> projects = new ArrayList<>(subFolders.length);
    for (File subFolder : subFolders) {
      File pom = new File(subFolder, "pom.xml");
      if (pom.isFile()) {
        projects.add(new MavenProjectInfo(subFolder.getName(), pom, null, null));
      }
    }

    MavenPlugin.getProjectConfigurationManager().importProjects(projects, new ProjectImportConfiguration(), monitor);
  }

  public String getSymbolicName() {
    return m_symbolicName;
  }

  public void setSymbolicName(String symbolicName) {
    m_symbolicName = symbolicName;
  }

  public String getDisplayName() {
    return m_displayName;
  }

  public void setDisplayName(String displayName) {
    m_displayName = displayName;
  }

  public File getTargetDirectory() {
    return m_targetDirectory;
  }

  public void setTargetDirectory(File targetDirectory) {
    m_targetDirectory = targetDirectory;
  }
}
