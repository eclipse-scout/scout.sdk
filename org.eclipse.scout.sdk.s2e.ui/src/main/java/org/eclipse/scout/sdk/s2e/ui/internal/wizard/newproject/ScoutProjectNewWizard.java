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
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.newproject;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.m2e.core.ui.internal.wizards.MappingDiscoveryJob;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.scout.sdk.s2e.workspace.newproject.ScoutProjectNewOperation;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link ScoutProjectNewWizard}</h3> Wizard that creates a new Scout project
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class ScoutProjectNewWizard extends AbstractWizard implements INewWizard {

  private static final String EXEC_ENV_PREFIX = "JavaSE-";
  private static final String MIN_JVM_VERSION = "1.7";

  private ScoutProjectNewWizardPage m_page1;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    setWindowTitle("New Scout Project");
    setHelpAvailable(true);
    setDefaultPageImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.ScoutProjectNewWizBanner));

    m_page1 = new ScoutProjectNewWizardPage();
    addPage(m_page1);
  }

  @Override
  public boolean performFinish() {
    // prepare operation
    final ScoutProjectNewOperation op = new ScoutProjectNewOperation();
    op.setDisplayName(m_page1.getDisplayName());
    op.setGroupId(m_page1.getGroupId());
    op.setArtifactId(m_page1.getArtifactId());
    op.setJavaVersion(Double.toString(getExecEnvVersion(getDefaultJvmExecutionEnvironment())));
    if (m_page1.isUseWorkspaceLocation()) {
      op.setTargetDirectory(ScoutProjectNewWizardPage.getWorkspaceLocation());
    }
    else {
      op.setTargetDirectory(m_page1.getTargetDirectory());
    }

    // remember folder
    String path = null;
    if (m_page1.getTargetDirectory() != null) {
      path = m_page1.getTargetDirectory().getAbsolutePath();
    }
    getDialogSettings().put(ScoutProjectNewWizardPage.SETTINGS_TARGET_DIR, path);

    // run operation
    final ResourceBlockingOperationJob projectCreationJob = new ResourceBlockingOperationJob(op);

    // append mapping discovery
    projectCreationJob.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        if (projectCreationJob.getResult().isOK()) {
          List<IProject> createdProjects = op.getCreatedProjects();
          if (createdProjects != null && !createdProjects.isEmpty()) {
            discoverMapping(createdProjects);
          }
        }
      }
    });
    projectCreationJob.schedule();

    return true;
  }

  protected void discoverMapping(List<IProject> projects) {
    MappingDiscoveryJob discoveryJob = new MappingDiscoveryJob(projects);
    discoveryJob.schedule();
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
}
