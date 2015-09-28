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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipInputStream;

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

  public static final String CUSTOM_TEMPLATE_PROP_NAME = "scoutProjectTemplatePath";
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
    ZipInputStream sourceTemplate = null;
    try {
      String customTemplateLocation = S2ESdkActivator.getDefault().getBundle().getBundleContext().getProperty(CUSTOM_TEMPLATE_PROP_NAME);
      if (StringUtils.isNotBlank(customTemplateLocation)) {
        File template = new File(customTemplateLocation);
        if (template.isFile()) {
          sourceTemplate = new ZipInputStream(new BufferedInputStream(new FileInputStream(template)));
        }
      }

      monitor.beginTask(getOperationName(), 100);
      ScoutProjectNewHelper.createProject(getTargetDirectory(), getSymbolicName(), getDisplayName(), sourceTemplate, Double.toString(getExecEnvVersion(getDefaultJvmExecutionEnvironment())));
      monitor.worked(1);

      importIntoWorkspace(new SubProgressMonitor(monitor, 99));
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus("Unable to create Scout Project.", e));
    }
    finally {
      if (sourceTemplate != null) {
        try {
          sourceTemplate.close();
        }
        catch (IOException e) {
          // nop;
        }
      }
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
    File[] subFolders = getTargetDirectory().listFiles();
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
